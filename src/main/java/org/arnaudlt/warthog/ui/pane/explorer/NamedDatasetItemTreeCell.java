package org.arnaudlt.warthog.ui.pane.explorer;

import javafx.beans.property.StringProperty;
import javafx.concurrent.WorkerStateEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.arnaudlt.warthog.model.dataset.Decoration;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.arnaudlt.warthog.ui.util.Utils;

import java.text.DecimalFormat;
import java.util.stream.Collectors;

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

                MenuItem helpMenuItem = buildInfoMenuItem(namedDatasetItem);
                contextMenu.getItems().add(helpMenuItem);
            }

            setContextMenu(contextMenu);
            setText(namedDatasetItem.getLabel());
            setGraphic(null);
        }
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

            String origin = decoration.getBasePath();
            grid.addRow(rowIdx++, new Label("Source :"), new Label(origin));

            String firstPart = decoration.getParts().get(0);
            if (decoration.getParts().size() > 1) {
                firstPart = firstPart + ",...";
            }
            grid.addRow(rowIdx++, new Label("Part(s) :"), new Label(firstPart));

            int partsCount = decoration.getParts().size();
            grid.addRow(rowIdx++, new Label("Parts count :"), new Label(String.valueOf(partsCount)));

            String format = decoration.getFormatAsString();
            grid.addRow(rowIdx++, new Label("Format :"), new Label(format));

            DecimalFormat formatter = new DecimalFormat("#.##");
            String formattedSizeInMB = decoration.getSizeInMegaBytes() != null ? formatter.format(decoration.getSizeInMegaBytes()) : "?";
            grid.addRow(rowIdx++, new Label("Size :"), new Label( formattedSizeInMB + "MB"));

            String selectAll = namedDatasetItem.getChild().stream()
                    .map(ndi -> "\t" + ndi.getCleanedSqlName())
                    .collect(Collectors.joining(",\n"));
            TextArea stack = new TextArea("SELECT \n"+ selectAll + "\nFROM " + namedDatasetItem.getCleanedSqlName() + ";\n");
            stack.maxHeight(80);
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
            newNameButton.setOnAction(rneEvt -> {

                this.explorerPane.renameSqlView(namedDatasetItem, newNameText.getText(), renameViewStage::close);
            });
            grid.addRow(rowIdx++, newNameText, newNameButton);

            Scene dialogScene = StageFactory.buildScene(new VBox(grid), -1d, -1d);
            renameViewStage.setScene(dialogScene);
            renameViewStage.show();
        });

        return renameMenuItem;
    }

}
