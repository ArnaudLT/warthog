package org.arnaudlt.warthog.ui.pane.control;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.pane.output.OutputPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ControlPane {

    private Stage stage;

    private NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final ExportDatabaseDialog exportDatabaseDialog;

    private final GlobalSettings globalSettings;

    private final ExportFileDialog exportFileDialog;

    private final SettingsDialog settingsDialog;

    private final ConnectionsManagerDialog connectionsManagerDialog;

    private ExplorerPane explorerPane;

    private TransformPane transformPane;

    private OutputPane outputPane;


    @Autowired
    public ControlPane(NamedDatasetManager namedDatasetManager, PoolService poolService,
                       ExportDatabaseDialog exportDatabaseDialog, GlobalSettings globalSettings,
                       ExportFileDialog exportFileDialog, SettingsDialog settingsDialog,
                       ConnectionsManagerDialog connectionsManagerDialog) {

        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.exportDatabaseDialog = exportDatabaseDialog;
        this.globalSettings = globalSettings;
        this.exportFileDialog = exportFileDialog;
        this.settingsDialog = settingsDialog;
        this.connectionsManagerDialog = connectionsManagerDialog;
    }


    public Node buildControlPane(Stage stage) {

        this.stage = stage;

        MenuBar menuBar = buildMenuBar();

        ProgressBar progressBar = new ProgressBar();
        progressBar.visibleProperty().bind(poolService.tickTackProperty().greaterThan(0));

        HBox hBox = new HBox(10, menuBar, progressBar);
        hBox.setMaxHeight(30);
        hBox.setMinHeight(30);
        hBox.setAlignment(Pos.BASELINE_LEFT); // bas

        this.exportDatabaseDialog.buildExportDatabaseDialog(stage);
        this.exportFileDialog.buildExportFileDialog(stage);
        this.connectionsManagerDialog.buildConnectionsManagerDialog(stage);

        return hBox;
    }


    private MenuBar buildMenuBar() {
        Menu fileMenu = new Menu("File");

        MenuItem openFileItem = new MenuItem("Import file...");
        openFileItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+O"));
        openFileItem.setOnAction(requestImportFile);

        MenuItem openParquetItem = new MenuItem("Import directory...");
        openParquetItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+SHIFT+O"));
        openParquetItem.setOnAction(requestImportFolder);

        MenuItem connectionManagerItem = new MenuItem("Connections Manager...");
        connectionManagerItem.setOnAction(getConnectionsManagerActionEventHandler());

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setAccelerator(KeyCodeCombination.valueOf("DELETE"));
        deleteItem.setOnAction(requestDelete);

        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+ALT+S"));
        settingsItem.setOnAction(getSettingsActionEventHandler());

        fileMenu.getItems().addAll(
                openFileItem,
                openParquetItem,
                new SeparatorMenuItem(),
                connectionManagerItem,
                settingsItem,
                new SeparatorMenuItem(),
                deleteItem);

        Menu runMenu = new Menu("Run");

        MenuItem overviewItem = new MenuItem("Overview");
        overviewItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+ENTER"));
        overviewItem.setOnAction(getOverviewActionEventHandler());

        SeparatorMenuItem separator3 = new SeparatorMenuItem();

        MenuItem exportCsvItem = new MenuItem("Export as Csv...");
        exportCsvItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+E"));
        exportCsvItem.setOnAction(getExportToCsvActionEventHandler());

        SeparatorMenuItem separator4 = new SeparatorMenuItem();

        MenuItem exportToFileItem = new MenuItem("Export to file...");
        exportToFileItem.setOnAction(getExportToFileActionEventHandler());

        MenuItem exportDbItem = new MenuItem("Export to database...");
        exportDbItem.setOnAction(getExportToDatabaseActionEventHandler());

        runMenu.getItems().addAll(overviewItem, separator3, exportCsvItem, separator4, exportToFileItem, exportDbItem);

        return new MenuBar(fileMenu, runMenu);
    }


    private EventHandler<ActionEvent> getConnectionsManagerActionEventHandler() {

        return actionEvent -> this.connectionsManagerDialog.showConnectionsManagerDialog();
    }


    private EventHandler<ActionEvent> getExportToDatabaseActionEventHandler() {

        return actionEvent -> this.exportDatabaseDialog.showExportDatabaseDialog();
    }


    private EventHandler<ActionEvent> getSettingsActionEventHandler() {

        return actionEvent -> {
            this.settingsDialog.buildSettingsDialog(stage);
            this.settingsDialog.showSettingsDialog();
        };
    }


    private EventHandler<ActionEvent> getOverviewActionEventHandler() {

        return event -> {

            NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
            if (selectedNamedDataset == null) {

                final String sqlQuery = this.transformPane.getSqlQuery();
                SqlOverviewService overviewService = new SqlOverviewService(namedDatasetManager, sqlQuery, globalSettings.getOverviewRows());
                overviewService.setOnSucceeded(success -> this.outputPane.fill(overviewService.getValue()));
                overviewService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the overview"));
                overviewService.setExecutor(poolService.getExecutor());
                overviewService.start();
            } else {

                NamedDatasetOverviewService overviewService = new NamedDatasetOverviewService(selectedNamedDataset, globalSettings.getOverviewRows());
                overviewService.setOnSucceeded(success -> this.outputPane.fill(overviewService.getValue()));
                overviewService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the overview"));
                overviewService.setExecutor(poolService.getExecutor());
                overviewService.start();
            }
            event.consume();
        };
    }


    private EventHandler<ActionEvent> getExportToCsvActionEventHandler() {

        return event -> {

            FileChooser fc = new FileChooser();
            File exportFile = fc.showSaveDialog(this.stage);

            if (exportFile == null) return;
            String filePath = exportFile.getAbsolutePath();
            ExportFileSettings exportFileSettings = new ExportFileSettings(filePath, Format.CSV, "Overwrite", ";", true);

            NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
            if (selectedNamedDataset == null) {

                final String sqlQuery = this.transformPane.getSqlQuery();
                SqlExportToFileService exportService = new SqlExportToFileService(namedDatasetManager, sqlQuery, exportFileSettings);
                exportService.setOnSucceeded(success -> log.info("Csv Export succeeded"));
                exportService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the Csv export"));
                exportService.setExecutor(poolService.getExecutor());
                exportService.start();
            } else {

                NamedDatasetExportToFileService exportService = new NamedDatasetExportToFileService(namedDatasetManager, selectedNamedDataset, exportFileSettings);
                exportService.setOnSucceeded(success -> log.info("Csv Export succeeded"));
                exportService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the Csv export"));
                exportService.setExecutor(poolService.getExecutor());
                exportService.start();
            }
        };
    }


    private EventHandler<ActionEvent> getExportToFileActionEventHandler() {

        return event -> this.exportFileDialog.showExportFileDialog();
    }


    private final EventHandler<ActionEvent> requestImportFile = actionEvent -> {

        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(this.getStage());
        if (files != null) {

            for (File file : files) {

                importFile(file);
            }
        }
    };


    private final EventHandler<ActionEvent> requestImportFolder = actionEvent -> {

        DirectoryChooser chooser = new DirectoryChooser();
        File file = chooser.showDialog(this.getStage());
        if (file != null) {

            importFile(file);
        }
    };


    public void importFile(File file) {

        NamedDatasetImportService importService = new NamedDatasetImportService(namedDatasetManager, file);
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to add the dataset '"+ file.getName() +"'"));
        importService.setExecutor(this.poolService.getExecutor());
        importService.start();
    }


    private final EventHandler<ActionEvent> requestDelete = actionEvent -> {

        Set<NamedDataset> selectedItems = this.explorerPane.getSelectedItems();
        for (NamedDataset selectedNamedDataset : selectedItems) {

            log.info("Request to close named dataset {}", selectedNamedDataset.getName());
            this.transformPane.closeNamedDataset(selectedNamedDataset);
            this.explorerPane.removeNamedDataset(selectedNamedDataset);
            this.namedDatasetManager.deregisterNamedDataset(selectedNamedDataset);
        }
    };


    public void setExplorerPane(ExplorerPane explorerPane) {
        this.explorerPane = explorerPane;
    }


    public void setTransformPane(TransformPane transformPane) {
        this.transformPane = transformPane;
    }


    public void setOutputPane(OutputPane outputPane) {
        this.outputPane = outputPane;
    }


    public Stage getStage() {
        return stage;
    }
}
