package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.ui.pane.alert.AlertError;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetExportToFileService;
import org.arnaudlt.warthog.ui.service.SqlExportToFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;


@Slf4j
@Component
public class ExportFileDialog {


    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final TransformPane transformPane;

    private Stage dialog;


    @Autowired
    public ExportFileDialog(NamedDatasetManager namedDatasetManager, PoolService poolService, TransformPane transformPane) {
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.transformPane = transformPane;
    }


    public void buildExportFileDialog(Stage stage) {

        this.dialog = new Stage();
        this.dialog.setTitle("Export to File");
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initOwner(stage);
        this.dialog.setResizable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(20,20,20,20));
        grid.setHgap(10);
        grid.setVgap(10);

        int i = 0;

        Label outputLabel = new Label("Output directory :");
        TextField output = new TextField();
        output.setMinWidth(300);
        Button outputButton = new Button("...");
        outputButton.setOnAction(event -> {

            FileChooser fc = new FileChooser();
            File exportFile = fc.showSaveDialog(this.dialog);

            if (exportFile == null) return;
            output.setText(exportFile.getAbsolutePath());
        });
        grid.addRow(i++, outputLabel, output, outputButton);

        Label formatLabel = new Label("Export type :");
        ComboBox<String> format = new ComboBox<>(FXCollections.observableArrayList("csv", "json", "parquet", "orc"));
        format.setValue("csv");
        format.setMinWidth(100);
        grid.addRow(i++, formatLabel, format);

        Label modeLabel = new Label("Mode :");
        ComboBox<String> saveMode = new ComboBox<>(FXCollections.observableArrayList("Overwrite", "Append"));
        saveMode.setValue("Overwrite");
        saveMode.setMinWidth(100);
        grid.addRow(i++, modeLabel, saveMode);

        // Start conditional display
        BooleanBinding csvSelected = format.valueProperty().isEqualTo("csv");
        Separator s = new Separator(Orientation.HORIZONTAL);
        s.visibleProperty().bind(csvSelected);
        grid.add(s, 0, i++, 3, 1);

        Label separatorLabel = new Label("Separator :");
        separatorLabel.visibleProperty().bind(csvSelected);
        TextField separator = new TextField(";");
        separator.setMaxWidth(50);
        separator.visibleProperty().bind(csvSelected);
        grid.addRow(i++, separatorLabel, separator);

        Label headerLabel = new Label("Header :");
        headerLabel.visibleProperty().bind(csvSelected);
        CheckBox header = new CheckBox();
        header.visibleProperty().bind(csvSelected);
        grid.addRow(i++, headerLabel, header);
        // End conditional display

        grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 3, 1);

        Button exportButton = new Button("Export");
        exportButton.setOnAction(event -> {

            ExportFileSettings exportFileSettings = new ExportFileSettings(output.getText(), format.getValue(),
                    saveMode.getValue(), separator.getText(), header.isSelected());
            exportToFile(exportFileSettings);
            dialog.close();
        });
        grid.addRow(i, exportButton);

        Scene dialogScene = new Scene(grid);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        dialog.setScene(dialogScene);
    }


    public void showExportFileDialog() {

        dialog.show();
    }


    private void exportToFile(ExportFileSettings exportFileSettings) {

        NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
        if (selectedNamedDataset == null) {

            final String sqlQuery = this.transformPane.getSqlQuery();
            SqlExportToFileService exportService = new SqlExportToFileService(namedDatasetManager, sqlQuery, exportFileSettings);
            exportService.setOnSucceeded(success -> log.info("Export succeeded"));
            exportService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the export"));
            exportService.setExecutor(poolService.getExecutor());
            exportService.start();
        } else {

            NamedDatasetExportToFileService exportService = new NamedDatasetExportToFileService(namedDatasetManager, selectedNamedDataset, exportFileSettings);
            exportService.setOnSucceeded(success -> log.info("Export succeeded"));
            exportService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the export"));
            exportService.setExecutor(poolService.getExecutor());
            exportService.start();
        }
    }

}
