package org.arnaudlt.warthog.ui.pane.explorer;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.types.*;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.ui.MainPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.collection.Iterator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExplorerPane {

    private MainPane mainPane;

    private TreeView<NamedDatasetItem> treeExplorer;

    private final Map<NamedDataset, TreeItem<NamedDatasetItem>> namedDatasetToTreeItem;


    @Autowired
    public ExplorerPane() {

        this.namedDatasetToTreeItem = new HashMap<>();
    }


    public Node buildExplorerPane(Stage stage) {

        this.treeExplorer = buildTreeView();

        VBox vBox = new VBox(treeExplorer);
        this.treeExplorer.prefHeightProperty().bind(vBox.heightProperty());

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

                this.mainPane.getControlPane().importFile(file);
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

        StructType schema = namedDataset.getDataset().schema();
        for (StructField field : schema.fields()) {

            NamedDatasetItem child = new NamedDatasetItem(namedDataset,
                    field.name() + " - " + field.dataType().typeName(), field.name());

            TreeItem<NamedDatasetItem> childItem = new TreeItem<>(child);
            item.getChildren().add(childItem);
            addSubItems(childItem, field.dataType());

        }

        this.treeExplorer.getRoot().getChildren().add(item);
        this.treeExplorer.getSelectionModel().select(item);
        this.namedDatasetToTreeItem.put(namedDataset, item);
    }


    private void addSubItems(TreeItem<NamedDatasetItem> parent, DataType dataType) {

        switch (dataType.typeName()) {

            case "struct":
                Iterator<StructField> iterator = ((StructType) dataType).iterator();
                while (iterator.hasNext()) {

                    StructField field = iterator.next();
                    TreeItem<NamedDatasetItem> child = new TreeItem<>(new NamedDatasetItem(
                            parent.getValue().getNamedDataset(),
                            field.name() + " - " + field.dataType().typeName(),
                            field.name()));
                    parent.getChildren().add(child);
                    addSubItems(child, field.dataType());
                }
                break;
            case "map":
                MapType mapType = (MapType) dataType;
                parent.getValue().setLabel(parent.getValue().getLabel() +
                        "<" + mapType.keyType().typeName() + "," + mapType.valueType().typeName() + ">");

                addSubItems(parent, mapType.valueType());
                break;
            case "array":
                ArrayType arrayType = (ArrayType) dataType;
                parent.getValue().setLabel(parent.getValue().getLabel() +
                        "<" + arrayType.elementType().typeName() + ">");

                addSubItems(parent, arrayType.elementType());
                break;
            default:
        }
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
                this.mainPane.getTransformPane().openNamedDataset(selectedNamedDataset);
            }
        }
    };


    public void setMainPane(MainPane mainPane) {

        this.mainPane = mainPane;
    }
}
