package org.arnaudlt.warthog.ui.pane.explorer;

import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.decoration.DatabaseDecoration;
import org.arnaudlt.warthog.model.dataset.decoration.Decoration;
import org.arnaudlt.warthog.model.dataset.decoration.LocalDecoration;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.arnaudlt.warthog.ui.util.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Slf4j
public class NamedDatasetItemTreeCell extends TreeCell<NamedDatasetItem> {


    private final Stage stage;

    private final ExplorerPane explorerPane;


    public NamedDatasetItemTreeCell(Stage stage, ExplorerPane explorerPane) {

        super();
        this.stage = stage;
        this.explorerPane = explorerPane;
    }


    @Override
    public void updateItem(NamedDatasetItem namedDatasetItem, boolean empty) {

        super.updateItem(namedDatasetItem, empty);
        if (empty) {

            setContextMenu(null);
            setText(null);
            setGraphic(null);
        } else {

            MenuItem copyMenuItem = buildCopyMenuItem(namedDatasetItem);
            ContextMenu contextMenu = new ContextMenu(copyMenuItem);

            if (namedDatasetItem.getDataType() == null) {

                MenuItem renameMenuItem = buildRenameMenuItem(namedDatasetItem);
                contextMenu.getItems().add(renameMenuItem);

                if (namedDatasetItem.getNamedDataset().getDecoration() instanceof LocalDecoration deco) {
                    MenuItem openFileLocation = buildOpenFileLocationMenuItem(deco);
                    contextMenu.getItems().add(openFileLocation);
                }

                MenuItem helpMenuItem = buildInfoMenuItem(namedDatasetItem);
                contextMenu.getItems().add(helpMenuItem);

            } /*else if ("struct".equals(namedDatasetItem.getDataType().typeName())) {

            } else if ("map".equals(namedDatasetItem.getDataType().typeName())) {

            } else if ("array".equals(namedDatasetItem.getDataType().typeName())) {

            }*/

            setContextMenu(contextMenu);
            setText(namedDatasetItem.getLabel());
            setGraphic(null);
        }
    }


    private MenuItem buildOpenFileLocationMenuItem(LocalDecoration decoration) {

        MenuItem openFileLocationItem = new MenuItem("Open folder...");
        openFileLocationItem.setOnAction(evt -> {
            try {
                if (decoration.parts() != null && decoration.parts().size() == 1) {
                    Runtime.getRuntime().exec("explorer /select, " + Paths.get(decoration.basePath(), decoration.parts().get(0)));
                } else {
                    Runtime.getRuntime().exec("explorer /select, " + decoration.basePath());
                }
            } catch (IOException e) {
                log.error("Unable to open base path", e);
            }
        });
        return openFileLocationItem;
    }


    private MenuItem buildCopyMenuItem(NamedDatasetItem namedDatasetItem) {

        MenuItem copyMenuItem = new MenuItem("Copy");
        copyMenuItem.setOnAction(evt -> Utils.copyStringToClipboard(namedDatasetItem.getCleanedSqlName()));
        return copyMenuItem;
    }


    private MenuItem buildInfoMenuItem(NamedDatasetItem namedDatasetItem) {

        MenuItem infoMenuItem = new MenuItem("Info...");
        infoMenuItem.setOnAction(evt -> {

            GridPane grid = GridFactory.buildGrid();
            int rowIdx = 0;

            Decoration decoration = namedDatasetItem.getNamedDataset().getDecoration();
            if (decoration instanceof LocalDecoration localDecoration) {

                String basePath = localDecoration.basePath();
                grid.addRow(rowIdx++, new Label("Base path :"), new Label(basePath));

                String firstPart = localDecoration.parts().get(0);

                boolean isMultiParts = localDecoration.parts().size() > 1;
                if (isMultiParts) {
                    grid.addRow(rowIdx++, new Label("Parts :"), new Label(firstPart + ",..."));
                } else {
                    grid.addRow(rowIdx++, new Label("Part :"), new Label(firstPart));
                }

                int partsCount = localDecoration.parts().size();
                grid.addRow(rowIdx++, new Label("Part count :"), new Label(String.valueOf(partsCount)));

                Format format = localDecoration.format();
                grid.addRow(rowIdx++, new Label("Format :"), new Label(format.getLabel()));

                String formattedSizeInMB = Utils.format2Decimals(localDecoration.sizeInMegaBytes());
                grid.addRow(rowIdx++, new Label("Size :"), new Label( formattedSizeInMB + "MB"));

            }  else if (decoration instanceof DatabaseDecoration databaseDecoration) {

                grid.addRow(rowIdx++, new Label("Source :"), new Label(databaseDecoration.source()));
                grid.addRow(rowIdx++, new Label("Table :"), new Label(databaseDecoration.tableName()));

            }

            String selectAll = namedDatasetItem.getChild().stream()
                    .map(ndi -> "\t" + ndi.getCleanedSqlName())
                    .collect(Collectors.joining(",\n"));
            TextArea stack = new TextArea("SELECT \n"+ selectAll + "\nFROM " + namedDatasetItem.getCleanedSqlName() + ";\n");
            stack.setEditable(false);

            Scene dialogScene = StageFactory.buildScene(new VBox(grid, stack), -1d, -1d);
            Stage datasetInformation = StageFactory.buildModalStage(stage, "Dataset information");
            datasetInformation.setScene(dialogScene);
            datasetInformation.show();
        });
        return infoMenuItem;
    }


    private MenuItem buildRenameMenuItem(NamedDatasetItem namedDatasetItem) {

        MenuItem renameMenuItem = new MenuItem("Rename...");
        renameMenuItem.setOnAction(evt -> {

            Stage renameViewStage = StageFactory.buildModalStage(stage, "Rename dataset ");

            GridPane grid = GridFactory.buildGrid();
            int rowIdx = 0;

            grid.add(new Label("Only alphanumerical and underscore characters are allowed."), 0, rowIdx++, 2, 1);

            TextField newNameText = new TextField(namedDatasetItem.getSqlName());
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(constraints);
            newNameText.textProperty().addListener((observable, oldValue, newValue) -> {

                if (!newValue.matches("^[a-zA-Z0-9_]*$")) {

                    ((StringProperty)observable).setValue(oldValue);
                }
            });
            Button newNameButton = new Button("Rename");
            newNameButton.setOnAction(rneEvt ->
                this.explorerPane.renameSqlView(namedDatasetItem, newNameText.getText(), renameViewStage::close)
            );
            grid.addRow(rowIdx++, newNameText, newNameButton);

            Scene dialogScene = StageFactory.buildScene(new VBox(grid), -1d, -1d);
            renameViewStage.setScene(dialogScene);
            renameViewStage.show();
        });

        return renameMenuItem;
    }

}
