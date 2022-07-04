package org.arnaudlt.warthog.ui.pane.explorer;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.types.*;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.MainPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetRenameViewService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.collection.Iterator;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExplorerPane {

    private Stage stage;

    private MainPane mainPane;

    private final PoolService poolService;

    private final NamedDatasetManager namedDatasetManager;

    private TreeView<NamedDatasetItem> treeExplorer;

    private final Map<NamedDataset, TreeItem<NamedDatasetItem>> namedDatasetToTreeItem;


    @Autowired
    public ExplorerPane(PoolService poolService, NamedDatasetManager namedDatasetManager) {

        this.poolService = poolService;
        this.namedDatasetManager = namedDatasetManager;
        this.namedDatasetToTreeItem = new HashMap<>();
    }


    public Node buildExplorerPane(Stage stage) {

        this.stage = stage;
        this.treeExplorer = buildTreeView();

        VBox vBox = new VBox(treeExplorer);
        this.treeExplorer.prefHeightProperty().bind(vBox.heightProperty());

        return vBox;
    }


    private TreeView<NamedDatasetItem> buildTreeView() {

        TreeItem<NamedDatasetItem> root = new TreeItem<>();

        TreeView<NamedDatasetItem> tree = new TreeView<>(root);
        tree.setCellFactory(x -> new NamedDatasetItemTreeCell(stage, this));
        tree.setShowRoot(false);

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

        final KeyCombination keyCodeCopy = KeyCombination.valueOf("CTRL+C");
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
            content = selectedItem.getValue().getCleanedSqlName();
        }
        Utils.copyStringToClipboard(content);
    }


    public void addNamedDatasetItem(NamedDataset namedDataset) {

        NamedDatasetItem rootNamedDatasetItem = new NamedDatasetItem(
                namedDataset,
                namedDataset.getLocalTemporaryViewName(),
                namedDataset.getLocalTemporaryViewName(),
                null);
        TreeItem<NamedDatasetItem> item = new TreeItem<>(rootNamedDatasetItem);

        StructType schema = namedDataset.getDataset().schema();
        for (StructField field : schema.fields()) {

            NamedDatasetItem child = new NamedDatasetItem(namedDataset,
                    field.name() + " - " + field.dataType().typeName(), field.name(), field.dataType());

            rootNamedDatasetItem.getChild().add(child);

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
            case "struct" -> {
                Iterator<StructField> iterator = ((StructType) dataType).iterator();
                while (iterator.hasNext()) {

                    StructField field = iterator.next();

                    NamedDatasetItem childNamedItem = new NamedDatasetItem(
                            parent.getValue().getNamedDataset(),
                            field.name() + " - " + field.dataType().typeName(),
                            field.name(), field.dataType());

                    TreeItem<NamedDatasetItem> child = new TreeItem<>(childNamedItem);
                    parent.getValue().getChild().add(childNamedItem);
                    parent.getChildren().add(child);
                    addSubItems(child, field.dataType());
                }
            }
            case "map" -> {
                MapType mapType = (MapType) dataType;
                parent.getValue().setLabel(parent.getValue().getLabel() +
                        "<" + mapType.keyType().typeName() + "," + mapType.valueType().typeName() + ">");
                addSubItems(parent, mapType.valueType());
            }
            case "array" -> {
                ArrayType arrayType = (ArrayType) dataType;
                parent.getValue().setLabel(parent.getValue().getLabel() +
                        "<" + arrayType.elementType().typeName() + ">");
                addSubItems(parent, arrayType.elementType());
            }
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


    protected void renameSqlView(NamedDatasetItem namedDatasetItem, String renameProposal, Runnable onSuccess) {

        NamedDatasetRenameViewService namedDatasetRenameViewService = new NamedDatasetRenameViewService(
            poolService, namedDatasetManager, namedDatasetItem.getNamedDataset(), renameProposal);

        namedDatasetRenameViewService.setOnSucceeded(success -> {
            namedDatasetItem.setLabel(renameProposal);
            namedDatasetItem.setSqlName(renameProposal);
            this.treeExplorer.refresh();
            onSuccess.run();
        });
        namedDatasetRenameViewService.setOnFailed(fail ->
            AlertFactory.showFailureAlert(stage, fail, "Not able to rename the dataset to " + renameProposal));
        namedDatasetRenameViewService.setOnCancelled(cancel ->
            log.warn("Renaming dataset to "+ renameProposal + " cancelled"));
        namedDatasetRenameViewService.start();
    }


    public void setMainPane(MainPane mainPane) {

        this.mainPane = mainPane;
    }
}
