package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetExportToFileService;
import org.arnaudlt.warthog.ui.service.SqlExportToFileService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;


@Slf4j
@Component
public class ExportFileDialog {


    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final TransformPane transformPane;

    private Stage owner;

    private Stage dialog;


    @Autowired
    public ExportFileDialog(NamedDatasetManager namedDatasetManager, PoolService poolService, TransformPane transformPane) {
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.transformPane = transformPane;
    }


    public void buildExportFileDialog(Stage owner) {

        this.owner = owner;
        this.dialog = StageFactory.buildModalStage(owner, "Export to file");

        GridPane grid = GridFactory.buildGrid();

        int i = 0;

        Label outputLabel = new Label("Output directory :");
        TextField output = new TextField();

        Button outputButton = new Button("...");
        outputButton.setOnAction(event -> {

            FileChooser fc = new FileChooser();
            File exportFile = fc.showSaveDialog(this.dialog);

            if (exportFile == null) return;
            output.setText(exportFile.getAbsolutePath());
        });

        grid.add(outputLabel, 0, i);
        grid.add(output, 1, i, 3, 1);
        grid.add(outputButton, 4, i);
        i++;

        Label formatLabel = new Label("Format :");
        ComboBox<Format> format = new ComboBox<>(FXCollections.observableArrayList(Format.values()));
        format.setValue(Format.CSV);
        format.setMinWidth(100);

        Label modeLabel = new Label("Mode :");
        ComboBox<String> saveMode = new ComboBox<>(FXCollections.observableArrayList("Overwrite", "Append"));
        saveMode.setValue("Overwrite");
        saveMode.setMinWidth(100);

        grid.addRow(i++, formatLabel, format, modeLabel, saveMode);

        Label partitionByLabel = new Label("Partitions :");
        TextField partitionBy = new TextField();
        partitionBy.setTooltip(new Tooltip("Comma separated list of attributs"));

        grid.add(partitionByLabel, 0, i);
        grid.add(partitionBy, 1, i, 3, 1);
        i++;

        // Start conditional display
        BooleanBinding csvSelected = format.valueProperty().isEqualTo(Format.CSV);
        Separator s = new Separator(Orientation.HORIZONTAL);
        s.visibleProperty().bind(csvSelected);
        grid.add(s, 0, i++, 4, 1);

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
        grid.addRow(i++, separatorLabel, separator, headerLabel, header);
        // End conditional display

        grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 4, 1);

        Button exportButton = new Button("Export");
        exportButton.setOnAction(event -> {

            ExportFileSettings exportFileSettings = new ExportFileSettings(output.getText(), format.getValue(),
                    saveMode.getValue(), partitionBy.getText(), separator.getText(), header.isSelected());
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
            exportService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to generate the export"));
            exportService.setExecutor(poolService.getExecutor());
            exportService.start();
        } else {

            NamedDatasetExportToFileService exportService = new NamedDatasetExportToFileService(namedDatasetManager, selectedNamedDataset, exportFileSettings);
            exportService.setOnSucceeded(success -> log.info("Export succeeded"));
            exportService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to generate the export"));
            exportService.setExecutor(poolService.getExecutor());
            exportService.start();
        }
    }

}
