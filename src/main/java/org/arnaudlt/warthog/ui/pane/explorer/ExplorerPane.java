package org.arnaudlt.warthog.ui.pane.explorer;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedColumn;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.ui.pane.control.ControlPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ExplorerPane {


    private final Stage stage;

    private TransformPane transformPane;

    private ControlPane controlPane;

    private TreeView<NamedDatasetItem> treeExplorer;

    private final Map<NamedDataset, TreeItem<NamedDatasetItem>> namedDatasetToTreeItem;


    public ExplorerPane(Stage stage) {

        this.stage = stage;
        this.namedDatasetToTreeItem = new HashMap<>();
    }


    public Node buildExplorerPane() {

        this.treeExplorer = buildTreeView();

        VBox vBox = new VBox(treeExplorer);
        this.treeExplorer.prefHeightProperty().bind(vBox.heightProperty());
        //this.treeExplorer.setEditable(true);
        //this.treeExplorer.setCellFactory(RenameMenuTreeCell::new);

        return vBox;
    }


    private TreeView<NamedDatasetItem> buildTreeView() {

        TreeItem<NamedDatasetItem> root = new TreeItem<>();

        TreeView<NamedDatasetItem> tree = new TreeView<>(root);
        tree.setShowRoot(false);
        tree.addEventFilter(MouseEvent.MOUSE_PRESSED, requestOpenSelectedNamedDatasets);

        tree.setOnDragOver(dragEvent -> {

            if (dragEvent.getDragboard().hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.ANY);
            }
        });

        tree.setOnDragDropped(dragEvent -> {

            List<File> files = dragEvent.getDragboard().getFiles();
            for (File file : files) {

                this.controlPane.importFile(file);
            }
        });

        final KeyCombination keyCodeCopy = KeyCodeCombination.valueOf("CTRL+C");
        tree.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event)) {
                copySelectionToClipboard();
            }
        });

        return tree;
    }


    private void copySelectionToClipboard() {

        String content;
        TreeItem<NamedDatasetItem> selectedItem = this.treeExplorer.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            content = "";
        } else {
            content = selectedItem.getValue().getSqlName();
        }
        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
    }


    public void addNamedDatasetItem(NamedDataset namedDataset) {

        TreeItem<NamedDatasetItem> item = new TreeItem<>(new NamedDatasetItem(
                namedDataset,
                namedDataset.getLocalTemporaryViewName(),
                namedDataset.getLocalTemporaryViewName()));

        for (NamedColumn namedColumn : namedDataset.getCatalog().getColumns()) {

            NamedDatasetItem child = new NamedDatasetItem(namedDataset, namedColumn.getName() + " - " + namedColumn.getType(), namedColumn.getName());
            item.getChildren().add(new TreeItem<>(child));
        }
        this.treeExplorer.getRoot().getChildren().add(item);
        this.treeExplorer.getSelectionModel().select(item);
        this.namedDatasetToTreeItem.put(namedDataset, item);
    }


    public Set<NamedDataset> getSelectedItems() {

        return this.treeExplorer.getSelectionModel().getSelectedItems().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    if (item.getParent() != null && item.getParent() != this.treeExplorer.getRoot()) {
                        return item.getParent().getValue().getNamedDataset();
                    } else {
                        return item.getValue().getNamedDataset();
                    }
                })
                .collect(Collectors.toSet());
    }


    public void removeNamedDataset(NamedDataset namedDataset) {

        TreeItem<NamedDatasetItem> namedDatasetTreeItem = this.namedDatasetToTreeItem.get(namedDataset);
        this.namedDatasetToTreeItem.remove(namedDataset);
        this.treeExplorer.getRoot().getChildren().remove(namedDatasetTreeItem);
    }


    private final EventHandler<MouseEvent> requestOpenSelectedNamedDatasets = event -> {

        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2 && event.getTarget() != null) {

            event.consume(); // Avoid expand/collapse on double click on the namedDataset !
            ObservableList<TreeItem<NamedDatasetItem>> selectedItems = this.treeExplorer.getSelectionModel().getSelectedItems();
            for (TreeItem<NamedDatasetItem> selectedItem : selectedItems) {

                if (selectedItem == null) continue;
                NamedDataset selectedNamedDataset = selectedItem.getValue().getNamedDataset();
                log.info("Request to open named dataset {}", selectedNamedDataset.getName());
                this.transformPane.openNamedDataset(selectedNamedDataset);
            }
        }
    };


    public void setTransformPane(TransformPane transformPane) {
        this.transformPane = transformPane;
    }


    public void setControlPane(ControlPane controlPane) {
        this.controlPane = controlPane;
    }


    private static class RenameMenuTreeCell extends TextFieldTreeCell<NamedDatasetItem> {

        private final ContextMenu menu = new ContextMenu();

        public RenameMenuTreeCell(TreeView<NamedDatasetItem> treeView) {
            super(new StringConverter<>() {
                @Override
                public String toString(NamedDatasetItem namedDatasetItem) {
                    return namedDatasetItem.getLabel();
                }

                @Override
                public NamedDatasetItem fromString(String s) {
                    NamedDatasetItem editingItem = treeView.getSelectionModel().getSelectedItem().getValue();
                    editingItem.setLabel(s);
                    return editingItem;
                }
            });

            MenuItem renameItem = new MenuItem("Rename");
            menu.getItems().add(renameItem);
            renameItem.setOnAction(arg0 -> startEdit());
        }

        @Override
        public void updateItem(NamedDatasetItem item, boolean empty) {
            super.updateItem(item, empty);

            if (!isEditing()) {
                setContextMenu(menu);
            }
        }
    }
}
