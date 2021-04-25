package org.arnaudlt.warthog.ui.pane.explorer;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.MDL2IconFont;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.types.*;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.pane.control.ControlPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.collection.Iterator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExplorerPane {


    private TransformPane transformPane;

    private ControlPane controlPane;

    private TreeView<NamedDatasetItem> datasetsExplorer;

    private final Map<NamedDataset, TreeItem<NamedDatasetItem>> namedDatasetToTreeItem;

    private final ListView<Service<?>> tasksExplorer;


    @Autowired
    public ExplorerPane(PoolService poolService) {

        this.namedDatasetToTreeItem = new HashMap<>();
        this.tasksExplorer = new ListView<>(poolService.getServices());
    }


    public Node buildExplorerPane(Stage stage) {

        TabPane global = new TabPane();
        global.setSide(Side.LEFT);
        global.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.datasetsExplorer = buildTreeView();
        VBox vBox = new VBox(datasetsExplorer);
        this.datasetsExplorer.prefHeightProperty().bind(vBox.heightProperty());

        global.getTabs().add(new Tab("Datasets", this.datasetsExplorer));

        this.tasksExplorer.setCellFactory(x ->new ExplorerPane.ServiceCell());
        VBox vBox2 = new VBox(tasksExplorer);
        this.tasksExplorer.prefHeightProperty().bind(vBox2.heightProperty());

        global.getTabs().add(new Tab("Tasks", this.tasksExplorer));
        return global;
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
        TreeItem<NamedDatasetItem> selectedItem = this.datasetsExplorer.getSelectionModel().getSelectedItem();
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

        this.datasetsExplorer.getRoot().getChildren().add(item);
        this.datasetsExplorer.getSelectionModel().select(item);
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

        return this.datasetsExplorer.getSelectionModel().getSelectedItems().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    if (item.getParent() != null && item.getParent() != this.datasetsExplorer.getRoot()) {
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
        this.datasetsExplorer.getRoot().getChildren().remove(namedDatasetTreeItem);
    }


    private final EventHandler<MouseEvent> requestOpenSelectedNamedDatasets = event -> {

        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2 && event.getTarget() != null) {

            event.consume(); // Avoid expand/collapse on double click on the namedDataset !
            ObservableList<TreeItem<NamedDatasetItem>> selectedItems = this.datasetsExplorer.getSelectionModel().getSelectedItems();
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


    //https://stackoverflow.com/questions/15661500/javafx-listview-item-with-an-image-button
    @Slf4j
    private static class ServiceCell extends ListCell<Service<?>> {

        private final VBox content;

        private final Label label;

        private final ProgressIndicator progressIndicator;

        private final Button cancelButton;


        public ServiceCell() {

            super();

            this.label = new Label();
            Pane pane = new Pane();
            this.cancelButton = new Button("", new MDL2IconFont("\uE711"));

            HBox hBox = new HBox(this.label, pane, cancelButton);
            hBox.setAlignment(Pos.CENTER);
            HBox.setHgrow(pane, Priority.ALWAYS);

            this.progressIndicator = new ProgressBar();
            this.content = new VBox(0, hBox, this.progressIndicator);

            setStyle("-fx-padding: 0px");
        }


        @Override
        protected void updateItem(Service<?> item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                setGraphic(null);
            } else {

                // TODO not clean. Why should I rebind that for each update ... Unbind needed ?
                this.label.textProperty().unbind();
                this.progressIndicator.progressProperty().unbind();

                this.label.textProperty().bind(item.messageProperty());
                this.progressIndicator.progressProperty().bind(item.progressProperty());
                this.cancelButton.setOnAction(event -> {
                    log.info("Request cancel task : {}", item);
                    item.cancel();
                });

                setGraphic(content);
            }
        }



    }


}
