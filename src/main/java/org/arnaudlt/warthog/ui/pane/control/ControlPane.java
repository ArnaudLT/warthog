package org.arnaudlt.warthog.ui.pane.control;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.database.DatabaseSettings;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.ui.pane.alert.AlertError;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.pane.output.OutputPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.*;

import java.io.File;
import java.util.List;
import java.util.Set;

@Slf4j
public class ControlPane {

    private final Stage stage;

    private NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private ExplorerPane explorerPane;

    private TransformPane transformPane;

    private OutputPane outputPane;


    public ControlPane(Stage stage, NamedDatasetManager namedDatasetManager, PoolService poolService) {

        this.stage = stage;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
    }


    public Node buildControlPane() {

        MenuBar menuBar = buildMenuBar();

        ProgressBar progressBar = new ProgressBar();
        progressBar.visibleProperty().bind(poolService.tickTackProperty().greaterThan(0));

        HBox hBox = new HBox(10, menuBar, progressBar);
        hBox.setMaxHeight(30);
        hBox.setMinHeight(30);
        hBox.setAlignment(Pos.BASELINE_LEFT); // bas

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

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setAccelerator(KeyCodeCombination.valueOf("DELETE"));
        deleteItem.setOnAction(requestDelete);

        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+S"));
        settingsItem.setOnAction(getSettingsActionEventHandler());

        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        SeparatorMenuItem separator2 = new SeparatorMenuItem();

        fileMenu.getItems().addAll(openFileItem, openParquetItem, separator1, settingsItem, separator2, deleteItem);

        Menu runMenu = new Menu("Run");

        MenuItem overviewItem = new MenuItem("Overview");
        overviewItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+ENTER"));
        overviewItem.setOnAction(getOverviewActionEventHandler());

        MenuItem exportCsvItem = new MenuItem("Export to Csv...");
        exportCsvItem.setAccelerator(KeyCodeCombination.valueOf("CTRL+E"));
        exportCsvItem.setOnAction(getExportToCsvActionEventHandler());

        MenuItem exportDbItem = new MenuItem("Export to Database...");
        exportDbItem.setOnAction(getExportMenuActionEventHandler());

        runMenu.getItems().addAll(overviewItem, exportCsvItem, exportDbItem);

        return new MenuBar(fileMenu, runMenu);
    }


    private EventHandler<ActionEvent> getExportMenuActionEventHandler() {

        return actionEvent -> {

            final Stage dialog = new Stage();
            dialog.setTitle("Export to Database");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this.stage);
            dialog.setResizable(false);

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);

            int i = 0;

            Label tableNameLabel = new Label("Table name :");
            TextField tableName = new TextField();

            ComboBox<String> saveMode = new ComboBox<>(FXCollections.observableArrayList("Overwrite", "Append"));
            saveMode.setValue("Overwrite");
            grid.addRow(i++, tableNameLabel, tableName, saveMode);

            grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 5, 1);

            Label connectionTypeLabel = new Label("Type :");
            ComboBox<String> connectionType = new ComboBox<>(
                    FXCollections.observableArrayList(DatabaseSettings.getKnownConnectionTypes()));
            connectionType.setValue("PostgreSQL");

            grid.addRow(i++, connectionTypeLabel, connectionType);

            Label hostLabel = new Label("Host :");
            TextField host = new TextField();
            host.setText("localhost");

            Label portLabel = new Label("Port :");
            portLabel.setMaxWidth(30);
            TextField port = new TextField();
            port.setMaxWidth(60);
            port.setText("5432");

            grid.add(hostLabel, 0, i);
            grid.add(host, 1, i, 2, 1);
            grid.add(portLabel, 3, i, 1, 1);
            grid.add(port, 4, i, 1, 1);
            i++;

            Label databaseLabel = new Label("Database :");
            TextField database = new TextField();
            database.setText("postgres");
            ComboBox<String> databaseType = new ComboBox<>(
                    FXCollections.observableArrayList(DatabaseSettings.getKnownDatabaseType()));
            databaseType.setValue("SID");
            databaseType.visibleProperty().bind(connectionType.valueProperty().isEqualTo("Oracle"));
            grid.addRow(i++, databaseLabel, database, databaseType);

            Label userLabel = new Label("User :");
            TextField user = new TextField();
            user.setText("postgres");
            grid.addRow(i++, userLabel, user);

            Label passwordLabel = new Label("Password :");
            PasswordField password = new PasswordField();
            password.setText("admin");
            grid.addRow(i++, passwordLabel, password);

            grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 5, 1);

            Button exportButton = new Button("Export");
            exportButton.setOnAction(event -> {

                DatabaseSettings dbSettings = new DatabaseSettings(connectionType.getValue(), host.getText(),
                        port.getText(), database.getText(), databaseType.getValue(), user.getText(), password.getText(),
                        saveMode.getValue(), tableName.getText());
                exportToDatabase(dbSettings);
                dialog.close();
            });
            grid.addRow(i++, exportButton);

            Scene dialogScene = new Scene(grid, 500, 300);
            JMetro metro = new JMetro(Style.LIGHT);
            metro.setAutomaticallyColorPanes(true);
            metro.setScene(dialogScene);
            dialog.setScene(dialogScene);
            dialog.show();
        };
    }


    private void exportToDatabase(DatabaseSettings databaseSettings) {

        NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
        if (selectedNamedDataset == null) {

            final String sqlQuery = this.transformPane.getSqlQuery();
            SqlExportToDatabaseService sqlExportToDatabaseService = new SqlExportToDatabaseService(namedDatasetManager, sqlQuery, databaseSettings);
            sqlExportToDatabaseService.setOnSucceeded(success -> log.info("Database export succeeded"));
            sqlExportToDatabaseService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the database export"));
            sqlExportToDatabaseService.setExecutor(poolService.getExecutor());
            sqlExportToDatabaseService.start();
        } else {

            NamedDatasetExportToDatabaseService namedDatasetExportToDatabaseService =
                    new NamedDatasetExportToDatabaseService(namedDatasetManager, selectedNamedDataset, databaseSettings);
            namedDatasetExportToDatabaseService.setOnSucceeded(success -> log.info("Database export succeeded"));
            namedDatasetExportToDatabaseService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the database export"));
            namedDatasetExportToDatabaseService.setExecutor(poolService.getExecutor());
            namedDatasetExportToDatabaseService.start();
        }
    }


    private EventHandler<ActionEvent> getSettingsActionEventHandler() {

        return actionEvent -> {

            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(this.stage);
            HBox dialogHBox = new HBox(new Text("Coming soon :-)"));

            Scene dialogScene = new Scene(dialogHBox, 800, 450);
            JMetro metro = new JMetro(Style.LIGHT);
            metro.setAutomaticallyColorPanes(true);
            metro.setScene(dialogScene);
            dialog.setScene(dialogScene);
            dialog.show();
        };
    }


    private EventHandler<ActionEvent> getOverviewActionEventHandler() {

        return event -> {

            NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
            if (selectedNamedDataset == null) {

                final String sqlQuery = this.transformPane.getSqlQuery();
                SqlOverviewService overviewService = new SqlOverviewService(namedDatasetManager, sqlQuery);
                overviewService.setOnSucceeded(success -> this.outputPane.fill(overviewService.getValue()));
                overviewService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the overview"));
                overviewService.setExecutor(poolService.getExecutor());
                overviewService.start();
            } else {

                NamedDatasetOverviewService overviewService = new NamedDatasetOverviewService(selectedNamedDataset);
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

            NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
            if (selectedNamedDataset == null) {

                final String sqlQuery = this.transformPane.getSqlQuery();
                SqlExportToCsvService exportService = new SqlExportToCsvService(namedDatasetManager, sqlQuery, filePath);
                exportService.setOnSucceeded(success -> log.info("Csv export succeeded"));
                exportService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the export"));
                exportService.setExecutor(poolService.getExecutor());
                exportService.start();
            } else {

                NamedDatasetExportToCsvService exportService = new NamedDatasetExportToCsvService(namedDatasetManager, selectedNamedDataset, filePath);
                exportService.setOnSucceeded(success -> log.info("Csv export succeeded"));
                exportService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the export"));
                exportService.setExecutor(poolService.getExecutor());
                exportService.start();
            }
        };
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
