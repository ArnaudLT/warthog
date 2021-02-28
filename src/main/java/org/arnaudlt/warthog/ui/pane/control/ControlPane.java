package org.arnaudlt.warthog.ui.pane.control;

import javafx.collections.FXCollections;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
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
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.service.*;
import org.arnaudlt.warthog.ui.pane.output.OutputPane;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;

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
            grid.addRow(i++, tableNameLabel, tableName);

            Label saveModeLabel = new Label("Save mode:");
            ComboBox<String> saveMode = new ComboBox<>(FXCollections.observableArrayList("Overwrite", "Append"));
            saveMode.setValue("Overwrite");
            grid.addRow(i++, saveModeLabel, saveMode);

            grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 2, 1);

            Label driverLabel = new Label("Driver :");
            ComboBox<String> driver = new ComboBox<>(
                    FXCollections.observableArrayList("org.postgresql.Driver", "oracle.jdbc.driver.OracleDriver"));
            driver.setValue("org.postgresql.Driver");
            grid.addRow(i++, driverLabel, driver);

            Label urlLabel = new Label("Url :");
            TextField url = new TextField();
            url.setText("jdbc:postgresql://localhost:5432/postgres");
            grid.addRow(i++, urlLabel, url);

            Label userLabel = new Label("User :");
            TextField user = new TextField();
            user.setText("postgres");
            grid.addRow(i++, userLabel, user);

            Label passwordLabel = new Label("Password :");
            PasswordField password = new PasswordField();
            password.setText("admin");
            grid.addRow(i++, passwordLabel, password);

            grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 2, 1);

            Button exportButton = new Button("Export");
            exportButton.setOnAction(event -> {

                DatabaseSettings dbSettings = new DatabaseSettings(url.getText(), user.getText(), password.getText(),
                        driver.getValue(), saveMode.getValue(), tableName.getText());
                exportToDatabase(dbSettings);
                dialog.close();
            });
            grid.addRow(i++, exportButton);

            Scene dialogScene = new Scene(grid, 320, 300);
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
            sqlExportToDatabaseService.setOnFailed(fail -> failToGenerate(fail, "database export"));
            sqlExportToDatabaseService.setExecutor(poolService.getExecutor());
            sqlExportToDatabaseService.start();
        } else {

            NamedDatasetExportToDatabaseService namedDatasetExportToDatabaseService =
                    new NamedDatasetExportToDatabaseService(namedDatasetManager, selectedNamedDataset, databaseSettings);
            namedDatasetExportToDatabaseService.setOnSucceeded(success -> log.info("Database export succeeded"));
            namedDatasetExportToDatabaseService.setOnFailed(fail -> failToGenerate(fail, "database export"));
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
                overviewService.setOnFailed(fail -> failToGenerate(fail, "overview"));
                overviewService.setExecutor(poolService.getExecutor());
                overviewService.start();
            } else {

                NamedDatasetOverviewService overviewService = new NamedDatasetOverviewService(selectedNamedDataset);
                overviewService.setOnSucceeded(success -> this.outputPane.fill(overviewService.getValue()));
                overviewService.setOnFailed(fail -> failToGenerate(fail, "overview"));
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
                exportService.setOnFailed(fail -> failToGenerate(fail,"export"));
                exportService.setExecutor(poolService.getExecutor());
                exportService.start();
            } else {

                NamedDatasetExportToCsvService exportService = new NamedDatasetExportToCsvService(namedDatasetManager, selectedNamedDataset, filePath);
                exportService.setOnSucceeded(success -> log.info("Csv export succeeded"));
                exportService.setOnFailed(fail -> failToGenerate(fail, "export"));
                exportService.setExecutor(poolService.getExecutor());
                exportService.start();
            }
        };
    }


    private void failToGenerate(WorkerStateEvent fail, String context) {

        log.error("Failed to generate output", fail.getSource().getException());
        Alert sqlAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        sqlAlert.setHeaderText("Not able to generate the "+ context);
        TextArea stack = new TextArea(fail.getSource().getException().toString());
        sqlAlert.getDialogPane().setContent(stack);
        sqlAlert.show();
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
        importService.setOnFailed(fail -> failToImport(file, fail));
        importService.setExecutor(this.poolService.getExecutor());
        importService.start();
    }


    private void failToImport(File file, WorkerStateEvent fail) {

        log.error("Failed to import", fail.getSource().getException());
        Alert datasetCreationAlert = new Alert(Alert.AlertType.ERROR, "", ButtonType.CLOSE);
        datasetCreationAlert.setHeaderText("Not able to add the dataset '"+ file.getName() +"'");
        TextArea stack = new TextArea(fail.getSource().getException().toString());
        datasetCreationAlert.getDialogPane().setContent(stack);
        datasetCreationAlert.show();
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
