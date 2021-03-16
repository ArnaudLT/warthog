package org.arnaudlt.warthog.ui.pane.control;

import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetExportToDatabaseService;
import org.arnaudlt.warthog.ui.service.SqlExportToDatabaseService;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ExportDialog {

    private final ConnectionsCollection connectionsCollection;

    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final TransformPane transformPane;

    private Stage dialog;


    @Autowired
    public ExportDialog(ConnectionsCollection connectionsCollection, NamedDatasetManager namedDatasetManager,
                        PoolService poolService, TransformPane transformPane) {
        this.connectionsCollection = connectionsCollection;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.transformPane = transformPane;
    }


    public void buildExportDatabaseDialog(Stage stage) {

        this.dialog = new Stage();
        this.dialog.setTitle("Export");
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initOwner(stage);
        this.dialog.setResizable(false);

        GridPane grid = GridFactory.buildGrid();

        int i = 0;

        Label connectionLabel = new Label("Connection :");

        ComboBox<Connection> connectionsListBox = new ComboBox<>(connectionsCollection.getConnections());

        grid.addRow(i++, connectionLabel, connectionsListBox);

        grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 3, 1);

        Label tableNameLabel = new Label("Table name :");
        TextField tableName = new TextField();
        ComboBox<String> saveModeBox = new ComboBox<>(FXCollections.observableArrayList("Overwrite", "Append"));
        saveModeBox.setValue("Overwrite");

        grid.addRow(i++, tableNameLabel, tableName, saveModeBox);

        grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 3, 1);

        Button exportButton = new Button("Export");
        exportButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            if (selectedConnection != null) {
                // TODO encapsuler les details de l'export ? (table, mode, ... ?)
                exportToDatabase(selectedConnection, tableName.getText(), saveModeBox.getValue());
                dialog.close();
            }
        });
        grid.addRow(i, exportButton);

        Scene dialogScene = new Scene(grid);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        dialog.setScene(dialogScene);
    }


    public void showExportDatabaseDialog() {

        dialog.show();
    }


    private void exportToDatabase(Connection selectedConnection, String table, String saveMode) {

        NamedDataset selectedNamedDataset = this.transformPane.getSelectedNamedDataset();
        if (selectedNamedDataset == null) {

            final String sqlQuery = this.transformPane.getSqlQuery();
            SqlExportToDatabaseService sqlExportToDatabaseService = new SqlExportToDatabaseService(namedDatasetManager,
                    sqlQuery, selectedConnection, table, saveMode);
            sqlExportToDatabaseService.setOnSucceeded(success -> log.info("Database export succeeded"));
            sqlExportToDatabaseService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the database export"));
            sqlExportToDatabaseService.setExecutor(poolService.getExecutor());
            sqlExportToDatabaseService.start();
        } else {

            NamedDatasetExportToDatabaseService namedDatasetExportToDatabaseService =
                    new NamedDatasetExportToDatabaseService(namedDatasetManager, selectedNamedDataset, selectedConnection, table, saveMode);
            namedDatasetExportToDatabaseService.setOnSucceeded(success -> log.info("Database export succeeded"));
            namedDatasetExportToDatabaseService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to generate the database export"));
            namedDatasetExportToDatabaseService.setExecutor(poolService.getExecutor());
            namedDatasetExportToDatabaseService.start();
        }
    }

}
