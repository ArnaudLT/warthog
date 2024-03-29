package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.user.GlobalSettings;
import org.arnaudlt.warthog.model.util.Compression;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.SqlExportToFileService;
import org.arnaudlt.warthog.ui.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Slf4j
@Component
public class ExportFileDialog {


    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final TransformPane transformPane;

    private final GlobalSettings globalSettings;

    private Stage owner;

    private Stage dialog;


    @Autowired
    public ExportFileDialog(NamedDatasetManager namedDatasetManager, PoolService poolService, GlobalSettings globalSettings, TransformPane transformPane) {
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.globalSettings = globalSettings;
        this.transformPane = transformPane;
    }


    public void buildExportFileDialog(Stage owner) {

        this.owner = owner;
        this.dialog = StageFactory.buildModalStage(owner, "Export to directory");

        // Basic settings
        GridPane basicGrid = GridFactory.buildGrid();
        int rowIndex = 0;

        Label outputLabel = new Label("Output directory :");
        TextField output = new TextField(globalSettings.getUser().getPreferredExportDirectory());

        Button outputButton = ButtonFactory.buildExplorerButton();
        outputButton.setOnAction(event -> {

            DirectoryChooser dc = new DirectoryChooser();
            Utils.setInitialDirectory(dc, output.getText());
            File exportFile = dc.showDialog(this.dialog);

            if (exportFile == null) return;
            output.setText(exportFile.getAbsolutePath());
        });

        basicGrid.add(outputLabel, 0, rowIndex);
        basicGrid.add(output, 1, rowIndex, 3, 1);
        basicGrid.add(outputButton, 4, rowIndex);
        rowIndex++;

        Label formatLabel = new Label("Format :");
        ComboBox<Format> format = new ComboBox<>(FXCollections.observableArrayList(Format.values()));
        format.setValue(Format.CSV);
        format.setMinWidth(100);

        Label modeLabel = new Label("Mode :");
        ComboBox<String> saveMode = new ComboBox<>(FXCollections.observableArrayList("Overwrite", "Append"));
        saveMode.setValue("Overwrite");
        saveMode.setMinWidth(100);
        basicGrid.addRow(rowIndex++, formatLabel, format, modeLabel, saveMode);

        Separator s = new Separator(Orientation.HORIZONTAL);
        basicGrid.add(s, 0, rowIndex++, 5, 1);

        // Start conditional display
        BooleanBinding csvSelected = format.valueProperty().isEqualTo(Format.CSV);

        Label separatorLabel = new Label("Separator :");
        separatorLabel.visibleProperty().bind(csvSelected);
        TextField separator = new TextField(";");
        separator.setMaxWidth(50);
        separator.visibleProperty().bind(csvSelected);

        Label headerLabel = new Label("Header :");
        headerLabel.visibleProperty().bind(csvSelected);
        CheckBox header = new CheckBox();
        header.setSelected(true);
        header.visibleProperty().bind(csvSelected);
        basicGrid.addRow(rowIndex++, separatorLabel, separator, headerLabel, header);
        // End conditional display

        Tab basicSettingsTab = new Tab("Settings", basicGrid);
        basicSettingsTab.setGraphic(LabelFactory.buildSettingsLabel());

        // Advanced settings
        GridPane advancedGrid = GridFactory.buildGrid();
        rowIndex = 0;

        Label partitionByLabel = new Label("Partitions :");
        TextField partitionBy = new TextField();
        partitionBy.setMinWidth(300);
        partitionBy.setMaxWidth(300);
        partitionBy.setTooltip(new Tooltip("Comma separated list of attributes"));

        advancedGrid.addRow(rowIndex++, partitionByLabel, partitionBy);

        Label repartitionLabel = new Label("Repartition :");
        Spinner<Integer> repartition = new Spinner<>(1, 1000, 1, 1);
        repartition.setEditable(true);
        advancedGrid.addRow(rowIndex++, repartitionLabel, repartition);


        Label compressionLabel = new Label("Compression :");
        ComboBox<Compression> compression = new ComboBox<>(FXCollections.observableArrayList(Compression.values()));
        compression.setValue(Compression.SNAPPY);

        BooleanBinding parquetSelected = format.valueProperty().isEqualTo(Format.PARQUET);
        compressionLabel.visibleProperty().bind(parquetSelected);
        compression.visibleProperty().bind(parquetSelected);

        advancedGrid.addRow(rowIndex++, compressionLabel, compression);

        Tab advancedSettingsTab = new Tab("Advanced", advancedGrid);
        advancedSettingsTab.setGraphic(LabelFactory.buildAdvancedLabel());

        TabPane tabPane = new TabPane(basicSettingsTab, advancedSettingsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        GridPane bottomGrid = GridFactory.buildGrid();
        Button exportButton = new Button("Export");
        exportButton.setOnAction(event -> {

            if ("Overwrite".equals(saveMode.getValue())) {

                Path outputDirectoryPath = Paths.get(output.getText());

                boolean atLeastOneFileInOutputDirectory = false;
                try {
                    atLeastOneFileInOutputDirectory = Files.list(outputDirectoryPath)
                            .findFirst()
                            .isPresent();
                } catch (IOException e) {
                    log.warn("Unable to check output directory", e);
                }

                if (atLeastOneFileInOutputDirectory) {

                    boolean cancelled = AlertFactory.showConfirmationAlert(owner,
                             """
                             All data in the following directory will be lost:
                             '%s'
                                                 
                             Do you want to continue ?
                             """.formatted(output.getText()))
                            .filter(button -> button != ButtonType.OK)
                            .isPresent();

                    if (cancelled) return;
                }
            }

            ExportFileSettings exportFileSettings = new ExportFileSettings(output.getText(), format.getValue(),
                    saveMode.getValue(), partitionBy.getText(), repartition.getValue(), separator.getText(),
                    header.isSelected(), compression.getValue());
            exportToFile(exportFileSettings);
            dialog.close();
        });
        bottomGrid.addRow(0, exportButton);

        Scene dialogScene = StageFactory.buildScene(new VBox(tabPane, bottomGrid));
        dialog.setScene(dialogScene);
    }


    public void showExportFileDialog() {

        dialog.show();
    }


    private void exportToFile(ExportFileSettings exportFileSettings) {

        final String sqlQuery = this.transformPane.getSqlQuery();
        SqlExportToFileService exportService = new SqlExportToFileService(poolService, namedDatasetManager, sqlQuery, exportFileSettings);
        exportService.setOnSucceeded(success -> log.info("Export succeeded"));
        exportService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to generate the export"));
        exportService.start();
    }

}
