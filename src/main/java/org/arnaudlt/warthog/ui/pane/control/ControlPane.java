package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.history.SqlHistoryCollection;
import org.arnaudlt.warthog.model.user.GlobalSettings;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.MainPane;
import org.arnaudlt.warthog.ui.service.ActiveThreadsCountService;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromFileService;
import org.arnaudlt.warthog.ui.service.SqlOverviewService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
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

    private final SqlHistoryCollection sqlHistoryCollection;

    private final SqlHistoryDialog sqlHistoryDialog;

    private final ExportFileDialog exportFileDialog;

    private final SettingsDialog settingsDialog;

    private final ConnectionsManagerDialog connectionsManagerDialog;

    private final BackgroundTasksDialog backgroundTasksDialog;

    private ImportDialog importDialog;

    private ImportLocalDialog importLocalDialog;


    @Autowired
    public ControlPane(NamedDatasetManager namedDatasetManager, PoolService poolService,
                       ExportDialog exportDialog, GlobalSettings globalSettings,
                       SqlHistoryCollection sqlHistoryCollection, SqlHistoryDialog sqlHistoryDialog, ExportFileDialog exportFileDialog, SettingsDialog settingsDialog,
                       ConnectionsManagerDialog connectionsManagerDialog, ImportDialog importDialog,
                       ImportLocalDialog importLocalDialog, BackgroundTasksDialog backgroundTasksDialog) {

        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.exportDialog = exportDialog;
        this.globalSettings = globalSettings;
        this.sqlHistoryCollection = sqlHistoryCollection;
        this.sqlHistoryDialog = sqlHistoryDialog;
        this.exportFileDialog = exportFileDialog;
        this.settingsDialog = settingsDialog;
        this.connectionsManagerDialog = connectionsManagerDialog;
        this.importDialog = importDialog;
        this.importLocalDialog = importLocalDialog;
        this.backgroundTasksDialog = backgroundTasksDialog;
    }


    public Node buildControlPane(Stage stage) {

        this.stage = stage;

        MenuBar menuBar = buildNewMenuBar();

        ProgressBar progressBar = new ProgressBar();
        progressBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                this.backgroundTasksDialog.showTasksManagerDialog();
            }
        });

        ActiveThreadsCountService activeThreadsCountService = new ActiveThreadsCountService(poolService);
        activeThreadsCountService.setRestartOnFailure(true);
        activeThreadsCountService.setDelay(Duration.millis(500));
        activeThreadsCountService.start();

        progressBar.visibleProperty().bind(Bindings.notEqual(0, activeThreadsCountService.lastValueProperty()));

        HBox hBox = new HBox(10, menuBar, progressBar);
        hBox.setMaxHeight(25);
        hBox.setMinHeight(25);
        hBox.setAlignment(Pos.CENTER_LEFT);

        this.exportDialog.buildExportDialog(stage);
        this.exportFileDialog.buildExportFileDialog(stage);
        this.connectionsManagerDialog.buildConnectionsManagerDialog(stage);
        this.importDialog.buildImportDialog(stage);
        this.importLocalDialog.buildImportLocalDialog(stage);
        this.backgroundTasksDialog.buildBackgroundTasksDialog(stage);
        this.sqlHistoryDialog.buildBackgroundTasksDialog(stage);

        return hBox;
    }


    private MenuBar buildNewMenuBar() {

        Menu fileMenu = new Menu("File");

        MenuItem newSqlTabItem = new MenuItem("New SQL sheet");
        newSqlTabItem.setAccelerator(KeyCombination.valueOf("CTRL+N"));
        newSqlTabItem.setOnAction(requestNewSqlTab);

        MenuItem openSqlTabItem = new MenuItem("Open SQL sheet...");
        openSqlTabItem.setAccelerator(KeyCombination.valueOf("CTRL+O"));
        openSqlTabItem.setOnAction(requestOpenSqlTab);

        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(KeyCombination.valueOf("CTRL+S"));
        saveItem.setOnAction(requestSave);

        MenuItem saveAsItem = new MenuItem("Save as...");
        saveAsItem.setAccelerator(KeyCombination.valueOf("CTRL+ALT+S"));
        saveAsItem.setOnAction(requestSaveAs);

        MenuItem importFromLocal = new MenuItem("Import from local...");
        importFromLocal.setAccelerator(KeyCombination.valueOf("CTRL+I"));
        importFromLocal.setOnAction(requestImportLocal);

        MenuItem importFromItem = new MenuItem("Import...");
        importFromItem.setAccelerator(KeyCombination.valueOf("CTRL+SHIFT+I"));
        importFromItem.setOnAction(requestImportFrom);

        MenuItem removeDatasetItem = new MenuItem("Remove dataset");
        removeDatasetItem.setAccelerator(KeyCombination.valueOf("DELETE"));
        removeDatasetItem.setOnAction(requestRemoveDataset);

        fileMenu.getItems().addAll(newSqlTabItem, openSqlTabItem, new SeparatorMenuItem(), saveItem, saveAsItem, new SeparatorMenuItem(), importFromLocal, importFromItem, new SeparatorMenuItem(), removeDatasetItem);

        Menu viewMenu = new Menu("View");

        MenuItem connectionManagerItem = new MenuItem("Connections manager...");
        connectionManagerItem.setOnAction(getConnectionsManagerActionEventHandler());

        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setOnAction(getSettingsActionEventHandler());

        MenuItem backgroundTasksItem = new MenuItem("Background tasks...");
        backgroundTasksItem.setOnAction(getBackgroundTasksActionEventHandler());

        MenuItem sqlHistoryItem = new MenuItem("SQL history...");
        sqlHistoryItem.setAccelerator(KeyCombination.valueOf("F8"));
        sqlHistoryItem.setOnAction(getSqlHistoryActionEventHandler());

        viewMenu.getItems().addAll(connectionManagerItem, settingsItem, new SeparatorMenuItem(), sqlHistoryItem,
                new SeparatorMenuItem(), backgroundTasksItem);

        Menu runMenu = new Menu("Run");

        MenuItem overviewItem = new MenuItem("Overview");
        overviewItem.setAccelerator(KeyCombination.valueOf("CTRL+ENTER"));
        overviewItem.setOnAction(getOverviewActionEventHandler());

        MenuItem exportToFileItem = new MenuItem("Export locally...");
        exportToFileItem.setAccelerator(KeyCombination.valueOf("CTRL+E"));
        exportToFileItem.setOnAction(getExportToFileActionEventHandler());

        MenuItem exportItem = new MenuItem("Export...");
        exportItem.setOnAction(getExportActionEventHandler());

        runMenu.getItems().addAll(overviewItem, new SeparatorMenuItem(), exportToFileItem, exportItem);

        return new MenuBar(fileMenu, viewMenu, runMenu);
    }


    private EventHandler<ActionEvent> getSqlHistoryActionEventHandler() {

        return actionEvent -> this.sqlHistoryDialog.showTasksManagerDialog();
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

            final String sqlQuery = this.mainPane.getTransformPane().getSqlQuery();
            SqlOverviewService overviewService = new SqlOverviewService(poolService, namedDatasetManager, sqlQuery, globalSettings, sqlHistoryCollection);
            overviewService.setOnSucceeded(success -> this.mainPane.getOutputPane().fill(overviewService.getValue()));
            overviewService.setOnFailed(fail -> AlertFactory.showFailureAlert(stage, fail, "Not able to generate the overview"));
            overviewService.start();

            event.consume();
        };
    }


    private EventHandler<ActionEvent> getExportToFileActionEventHandler() {

        return event -> this.exportFileDialog.showExportFileDialog();
    }


    private final EventHandler<ActionEvent> requestNewSqlTab = actionEvent ->
            this.mainPane.getTransformPane().addSqlNewTab();


    private final EventHandler<ActionEvent> requestImportLocal = actionEvent ->
            this.importLocalDialog.showImportLocalDialog();


    private final EventHandler<ActionEvent> requestImportFrom = actionEvent ->
            this.importDialog.showImportDialog();


    private final EventHandler<ActionEvent> requestOpenSqlTab = actionEvent -> {

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQL file (*.sql)", "*.sql")
        );
        File chosenSqlFile = fc.showOpenDialog(stage);
        if (chosenSqlFile != null) {

            this.mainPane.getTransformPane().openSqlFile(chosenSqlFile);
        }
    };


    private final EventHandler<ActionEvent> requestSave = actionEvent -> {

        this.mainPane.getTransformPane().saveSqlTabToFile();
    };


    private final EventHandler<ActionEvent> requestSaveAs = actionEvent -> {

        this.mainPane.getTransformPane().saveAsSqlTabToFile();
    };


    public void importFile(File file) {

        NamedDatasetImportFromFileService importService = new NamedDatasetImportFromFileService(poolService, namedDatasetManager, file);
        importService.setOnSucceeded(success -> mainPane.getExplorerPane().addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(stage, fail, "Not able to add the dataset '" + file.getName() + "'"));
        importService.start();
    }


    private final EventHandler<ActionEvent> requestRemoveDataset = actionEvent -> {

        Set<NamedDataset> selectedItems = this.mainPane.getExplorerPane().getSelectedItems();
        for (NamedDataset selectedNamedDataset : selectedItems) {

            log.info("Request to close named dataset {}", selectedNamedDataset.getName());
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
