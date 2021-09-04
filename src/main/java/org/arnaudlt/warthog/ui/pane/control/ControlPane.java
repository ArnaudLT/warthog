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
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.MainPane;
import org.arnaudlt.warthog.ui.service.*;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class ControlPane {

    private MainPane mainPane;

    private Stage stage;

    private NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final ExportDialog exportDialog;

    private final GlobalSettings globalSettings;

    private final ExportFileDialog exportFileDialog;

    private final SettingsDialog settingsDialog;

    private final ConnectionsManagerDialog connectionsManagerDialog;

    private final BackgroundTasksDialog backgroundTasksDialog;

    private ImportDialog importDialog;


    @Autowired
    public ControlPane(NamedDatasetManager namedDatasetManager, PoolService poolService,
                       ExportDialog exportDialog, GlobalSettings globalSettings,
                       ExportFileDialog exportFileDialog, SettingsDialog settingsDialog,
                       ConnectionsManagerDialog connectionsManagerDialog, ImportDialog importDialog,
                       BackgroundTasksDialog backgroundTasksDialog) {

        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.exportDialog = exportDialog;
        this.globalSettings = globalSettings;
        this.exportFileDialog = exportFileDialog;
        this.settingsDialog = settingsDialog;
        this.connectionsManagerDialog = connectionsManagerDialog;
        this.importDialog = importDialog;
        this.backgroundTasksDialog = backgroundTasksDialog;
    }


    public Node buildControlPane(Stage stage) {

        this.stage = stage;

        MenuBar menuBar = buildNewMenuBar();

        ProgressBar progressBar = new ProgressBar();
        progressBar.visibleProperty().bind(poolService.tickTackProperty().greaterThan(0));

        HBox hBox = new HBox(10, menuBar, progressBar);
        hBox.setMaxHeight(30);
        hBox.setMinHeight(30);
        hBox.setAlignment(Pos.BASELINE_LEFT); // bas

        this.exportDialog.buildExportDialog(stage);
        this.exportFileDialog.buildExportFileDialog(stage);
        this.connectionsManagerDialog.buildConnectionsManagerDialog(stage);
        this.importDialog.buildImportDialog(stage);
        this.backgroundTasksDialog.buildBackgroundTasksDialog(stage);

        return hBox;
    }


    private MenuBar buildNewMenuBar() {

        Menu fileMenu = new Menu("File");

        MenuItem openFileItem = new MenuItem("Import local file...");
        openFileItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+O"));
        openFileItem.setOnAction(requestImportFile);

        MenuItem openFolderItem = new MenuItem("Import local directory...");
        openFolderItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+SHIFT+O"));
        openFolderItem.setOnAction(requestImportFolder);

        MenuItem importFromItem = new MenuItem("Import...");
        importFromItem.setOnAction(requestImportFrom);

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setAccelerator(KeyCodeCombination.valueOf("DELETE"));
        deleteItem.setOnAction(requestDelete);

        fileMenu.getItems().addAll(openFileItem, openFolderItem,  new SeparatorMenuItem(),
                importFromItem, new SeparatorMenuItem(),
                deleteItem);

        Menu editMenu = new Menu("Edit");

        MenuItem connectionManagerItem = new MenuItem("Connections Manager...");
        connectionManagerItem.setOnAction(getConnectionsManagerActionEventHandler());

        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+ALT+S"));
        settingsItem.setOnAction(getSettingsActionEventHandler());

        editMenu.getItems().addAll(connectionManagerItem, settingsItem);

        Menu runMenu = new Menu("Run");

        MenuItem overviewItem = new MenuItem("Overview");
        overviewItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+ENTER"));
        overviewItem.setOnAction(getOverviewActionEventHandler());

        MenuItem exportToFileItem = new MenuItem("Export locally...");
        exportToFileItem.setOnAction(getExportToFileActionEventHandler());

        MenuItem exportItem = new MenuItem("Export...");
        exportItem.setOnAction(getExportActionEventHandler());

        MenuItem backgroundTasksItem = new MenuItem("Background tasks...");
        backgroundTasksItem.setOnAction(getBackgroundTasksActionEventHandler());

        runMenu.getItems().addAll(overviewItem, new SeparatorMenuItem(), exportToFileItem, exportItem, new SeparatorMenuItem(), backgroundTasksItem);

        return new MenuBar(fileMenu, editMenu, runMenu);
    }


    private EventHandler<ActionEvent> getBackgroundTasksActionEventHandler() {

            return actionEvent -> this.backgroundTasksDialog.showTasksManagerDialog();
    }


    private EventHandler<ActionEvent> getConnectionsManagerActionEventHandler() {

        return actionEvent -> this.connectionsManagerDialog.showConnectionsManagerDialog();
    }


    private EventHandler<ActionEvent> getExportActionEventHandler() {

        return actionEvent -> this.exportDialog.showExportDatabaseDialog();
    }


    private EventHandler<ActionEvent> getSettingsActionEventHandler() {

        return actionEvent -> {
            this.settingsDialog.buildSettingsDialog(stage);
            this.settingsDialog.showSettingsDialog();
        };
    }


    private EventHandler<ActionEvent> getOverviewActionEventHandler() {

        return event -> {

            NamedDataset selectedNamedDataset = this.mainPane.getTransformPane().getSelectedNamedDataset();
            if (selectedNamedDataset == null) {

                final String sqlQuery = this.mainPane.getTransformPane().getSqlQuery();
                SqlOverviewService overviewService = new SqlOverviewService(poolService, namedDatasetManager, sqlQuery, globalSettings.getOverviewRows());
                overviewService.setOnSucceeded(success -> this.mainPane.getOutputPane().fill(overviewService.getValue()));
                overviewService.setOnFailed(fail -> AlertFactory.showFailureAlert(stage, fail, "Not able to generate the overview"));
                overviewService.start();
            } else {

                NamedDatasetOverviewService overviewService = new NamedDatasetOverviewService(poolService, selectedNamedDataset, globalSettings.getOverviewRows());
                overviewService.setOnSucceeded(success -> this.mainPane.getOutputPane().fill(overviewService.getValue()));
                overviewService.setOnFailed(fail -> AlertFactory.showFailureAlert(stage, fail, "Not able to generate the overview"));
                overviewService.start();
            }
            event.consume();
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


    private final EventHandler<ActionEvent> requestImportFrom = actionEvent ->
        this.importDialog.showImportDatabaseDialog();


    public void importFile(File file) {

        NamedDatasetImportFromFileService importService = new NamedDatasetImportFromFileService(poolService, namedDatasetManager, file);
        importService.setOnSucceeded(success -> mainPane.getExplorerPane().addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(stage, fail, "Not able to add the dataset '"+ file.getName() +"'"));
        importService.start();
    }


    private final EventHandler<ActionEvent> requestDelete = actionEvent -> {

        Set<NamedDataset> selectedItems = this.mainPane.getExplorerPane().getSelectedItems();
        for (NamedDataset selectedNamedDataset : selectedItems) {

            log.info("Request to close named dataset {}", selectedNamedDataset.getName());
            this.mainPane.getTransformPane().closeNamedDataset(selectedNamedDataset);
            this.mainPane.getExplorerPane().removeNamedDataset(selectedNamedDataset);
            this.namedDatasetManager.deregisterNamedDataset(selectedNamedDataset);
        }
    };


    public void setMainPane(MainPane mainPane) {

        this.mainPane = mainPane;
    }


    public Stage getStage() {
        return stage;
    }
}
