package org.arnaudlt.projectdse.ui.pane.explorer;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.MDL2IconFont;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.projectdse.PoolService;
import org.arnaudlt.projectdse.model.dataset.NamedColumn;
import org.arnaudlt.projectdse.model.dataset.NamedDataset;
import org.arnaudlt.projectdse.model.dataset.NamedDatasetManager;
import org.arnaudlt.projectdse.ui.pane.transform.TransformPane;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ExplorerPane {


    private final Stage stage;

    private NamedDatasetManager namedDatasetManager;

    private PoolService poolService;

    private TransformPane transformPane;

    private TreeView<NamedDatasetItem> treeExplorer;


    public ExplorerPane(Stage stage, NamedDatasetManager namedDatasetManager, PoolService poolService) {

        this.stage = stage;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
    }


    public Node buildExplorerPane() {

        Node buttonsBar = buildButtonsBar();
        this.treeExplorer = buildTreeView();

        VBox vBox = new VBox(buttonsBar, treeExplorer);

        this.treeExplorer.prefHeightProperty().bind(vBox.heightProperty());

        return vBox;
    }


    private TreeView<NamedDatasetItem> buildTreeView() {

        TreeItem<NamedDatasetItem> root = new TreeItem<>(null);

        TreeView<NamedDatasetItem> tree = new TreeView<>(root);
        tree.setShowRoot(false);
        tree.addEventFilter(MouseEvent.MOUSE_PRESSED, requestOpenSelectedNamedDatasets);

        return tree;
    }


    private Node buildButtonsBar() {

        Button importButton = new Button("_Import File", new MDL2IconFont("\uE8E5"));
        importButton.setOnAction(this.requestImportFile);

        Button importFolderButton = new Button("Import _Parquet", new MDL2IconFont("\uED25"));
        importFolderButton.setOnAction(this.requestImportFolder);

        Button deleteButton = new Button("_Delete", new MDL2IconFont("\uE74D"));
        deleteButton.setOnAction(this.requestDelete);

        return new HBox(2, importButton, importFolderButton, deleteButton);
    }


    private void addNamedDatasetItem(NamedDataset namedDataset) {

        TreeItem<NamedDatasetItem> item = new TreeItem<>(new NamedDatasetItem(namedDataset, namedDataset.getName()));
        for (NamedColumn namedColumn : namedDataset.getCatalog().getColumns()) {

            NamedDatasetItem child = new NamedDatasetItem(namedDataset, namedColumn.getName() + " - " + namedColumn.getType());
            item.getChildren().add(new TreeItem<>(child, new MDL2IconFont("\uEA81")));
        }
        this.treeExplorer.getRoot().getChildren().add(item);
        this.treeExplorer.getSelectionModel().select(item);
    }


    private void failToImport(File file) {

        Alert datasetCreationAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        datasetCreationAlert.setHeaderText(String.format("Not able to add the dataset '%s'", file.getName()));
        datasetCreationAlert.setContentText("Please check the file format and its integrity");
        datasetCreationAlert.show();
    }


    private final EventHandler<ActionEvent> requestImportFile = actionEvent -> {

        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(this.getStage());
        if (files != null) {

            for (File selectedFile : files) {

                NamedDatasetImportService importService = new NamedDatasetImportService(namedDatasetManager, selectedFile);
                importService.setOnSucceeded(success -> addNamedDatasetItem(importService.getValue()));
                importService.setOnFailed(fail -> failToImport(selectedFile));
                importService.setExecutor(this.poolService.getExecutor());
                importService.start();
            }
        }
    };


    private final EventHandler<ActionEvent> requestImportFolder = actionEvent -> {

        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(this.getStage());
        if (file != null) {

            NamedDatasetImportService importService = new NamedDatasetImportService(namedDatasetManager, file);
            importService.setOnSucceeded(success -> addNamedDatasetItem(importService.getValue()));
            importService.setOnFailed(fail -> failToImport(file));
            importService.setExecutor(this.poolService.getExecutor());
            importService.start();

        }
    };


    private final EventHandler<ActionEvent> requestDelete = actionEvent -> {

        ObservableList<TreeItem<NamedDatasetItem>> selectedItems = this.treeExplorer.getSelectionModel().getSelectedItems();
        // TODO better solution ? Need a non observable copy to avoid NPE during the for loop
        List<TreeItem<NamedDatasetItem>> selectedItemsCopy = new ArrayList<>(selectedItems);
        for (TreeItem<NamedDatasetItem> selectedItem : selectedItemsCopy) {

            if (selectedItem == null) continue;
            if (selectedItem.getParent() != null && selectedItem.getParent() != this.treeExplorer.getRoot()) {
                selectedItem = selectedItem.getParent();
            }
            NamedDataset selectedNamedDataset = selectedItem.getValue().getNamedDataset();
            log.info("Request to close named dataset {}", selectedNamedDataset.getName());
            this.transformPane.closeNamedDataset(selectedNamedDataset);
            this.treeExplorer.getRoot().getChildren().remove(selectedItem);
            this.namedDatasetManager.deregisterNamedDataset(selectedNamedDataset);
        }
    };


    private final EventHandler<MouseEvent> requestOpenSelectedNamedDatasets = event -> {

        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2 && event.getTarget() != null) {

            event.consume(); // Avoid expand/collapse on double click on the namedDataset !
            ObservableList<TreeItem<NamedDatasetItem>> selectedItems = this.treeExplorer.getSelectionModel().getSelectedItems();
            for (TreeItem<NamedDatasetItem> selectedItem : selectedItems) {

                NamedDataset selectedNamedDataset = selectedItem.getValue().getNamedDataset();
                log.info("Request to open named dataset {}", selectedNamedDataset.getName());
                this.transformPane.openNamedDataset(selectedNamedDataset);
            }
        }
    };


    public void setTransformPane(TransformPane transformPane) {
        this.transformPane = transformPane;
    }


    private Stage getStage() {
        return stage;
    }

}
