package org.arnaudlt.warthog.ui.pane.control;

import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
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
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetExportToDatabaseService;
import org.arnaudlt.warthog.ui.service.SqlExportToDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ExportDatabaseDialog {


    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final TransformPane transformPane;

    private Stage dialog;


    @Autowired
    public ExportDatabaseDialog(NamedDatasetManager namedDatasetManager, PoolService poolService, TransformPane transformPane) {
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.transformPane = transformPane;
    }


    public void buildDatabaseSettingsDialog(Stage stage) {

        this.dialog = new Stage();
        this.dialog.setTitle("Export to Database");
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initOwner(stage);
        this.dialog.setResizable(false);

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
    }


    public void showDatabaseSettingsDialog() {

        dialog.show();
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

}
