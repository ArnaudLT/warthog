package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ImportDirectorySettings;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromLocalService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.LabelFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;


@Slf4j
@Component
public class ImportLocalDialog {

    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final ExplorerPane explorerPane;

    private Stage owner;

    private Stage dialog;


    @Autowired
    public ImportLocalDialog(NamedDatasetManager namedDatasetManager, PoolService poolService, ExplorerPane explorerPane) {
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.explorerPane = explorerPane;
    }


    public void buildImportLocalDialog(Stage owner) {

        this.owner = owner;
        this.dialog = StageFactory.buildModalStage(owner, "Import from local");

        // Basic settings
        GridPane basicGrid = GridFactory.buildGrid();
        int rowIndex = 0;

        Label inputLabel = new Label("Input directory :");
        TextField input = new TextField();

        Button inputButton = new Button("...");
        inputButton.setOnAction(event -> {

            DirectoryChooser dc = new DirectoryChooser();
            File importFile = dc.showDialog(this.dialog);

            if (importFile == null) return;
            input.setText(importFile.getAbsolutePath());
        });

        basicGrid.add(inputLabel, 0, rowIndex);
        basicGrid.add(input, 1, rowIndex, 3, 1);
        basicGrid.add(inputButton, 4, rowIndex);
        rowIndex++;

        Label nameLabel = new Label("Name :");
        TextField name = new TextField();
        name.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.matches("^[a-zA-Z0-9_]*$")) {

                ((StringProperty)observable).setValue(oldValue);
            }
        });

        Label formatLabel = new Label("Format :");
        ComboBox<Format> format = new ComboBox<>(FXCollections.observableArrayList(Format.values()));
        format.setValue(Format.CSV);
        format.setMinWidth(100);

        basicGrid.addRow(rowIndex++, nameLabel, name, formatLabel, format);

        // Start conditional display

        BooleanBinding csvSelected = format.valueProperty().isEqualTo(Format.CSV);
        GridPane csvBasicGrid = GridFactory.buildGrid();
        csvBasicGrid.visibleProperty().bind(csvSelected);

        Label separatorLabel = new Label("Separator :");
        separatorLabel.visibleProperty().bind(csvSelected);
        TextField separator = new TextField(";");
        separator.setMaxWidth(50);

        Label headerLabel = new Label("Header :");
        headerLabel.visibleProperty().bind(csvSelected);
        CheckBox header = new CheckBox();
        header.setSelected(true);
        csvBasicGrid.addRow(0, separatorLabel, separator, headerLabel, header);

        BooleanBinding jsonSelected = format.valueProperty().isEqualTo(Format.JSON);
        GridPane jsonBasicGrid = GridFactory.buildGrid();
        jsonBasicGrid.visibleProperty().bind(jsonSelected);

        Label multiLineLabel = new Label("Multi-line :");
        CheckBox multiLine = new CheckBox();
        multiLine.setSelected(false);
        jsonBasicGrid.addRow(0, multiLineLabel, multiLine);
        // End conditional display

        Tab basicSettingsTab = new Tab("Settings", new VBox(
                basicGrid,
                new Separator(Orientation.HORIZONTAL),
                new Group(csvBasicGrid, jsonBasicGrid)));
        basicSettingsTab.setGraphic(LabelFactory.buildSegoeLabel("\uE713"));

        // Advanced settings
        GridPane advancedGrid = GridFactory.buildGrid();
        rowIndex = 0;

        Label basePathLabel = new Label("Import base path :");
        TextField basePathField = new TextField();
        basePathField.setMinWidth(250);
        basePathField.setMaxWidth(250);

        CheckBox automaticBasePathCheckBox = new CheckBox("Auto");
        automaticBasePathCheckBox.setSelected(true);
        basePathField.disableProperty().bind(automaticBasePathCheckBox.selectedProperty());

        StringBinding basePathFieldBind = Bindings.createStringBinding(() ->
                        Paths.get(input.getText()).toString(), input.textProperty());

        basePathField.textProperty().bind(basePathFieldBind);
        automaticBasePathCheckBox.selectedProperty().addListener((selectedProperty, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                basePathField.textProperty().bind(basePathFieldBind);
            } else {
                basePathField.textProperty().unbind();
            }
        });

        advancedGrid.addRow(rowIndex++, basePathLabel, basePathField, automaticBasePathCheckBox);

        Tab advancedSettingsTab = new Tab("Advanced", advancedGrid);

        TabPane tabPane = new TabPane(basicSettingsTab, advancedSettingsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMinHeight(180);

        GridPane bottomGrid = GridFactory.buildGrid();
        Button exportButton = new Button("Import");
        exportButton.setOnAction(event -> {

            ImportDirectorySettings importDirectorySettings = new ImportDirectorySettings(
                    input.getText(),
                    format.getValue(),
                    name.getText(),
                    separator.getText(),
                    header.isSelected(),
                    multiLine.isSelected(),
                    basePathField.getText());
            importDirectory(importDirectorySettings);
            dialog.close();
        });
        bottomGrid.addRow(0, exportButton);

        Scene dialogScene = StageFactory.buildScene(new VBox(tabPane, bottomGrid));
        dialog.setScene(dialogScene);
    }


    public void showImportLocalDialog() {

        dialog.show();
    }


    private void importDirectory(ImportDirectorySettings importDirectorySettings) {

        NamedDatasetImportFromLocalService importService = new NamedDatasetImportFromLocalService(poolService, namedDatasetManager, importDirectorySettings);
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to add the dataset '" + importDirectorySettings.getName() + "'"));
        importService.start();
    }


}
