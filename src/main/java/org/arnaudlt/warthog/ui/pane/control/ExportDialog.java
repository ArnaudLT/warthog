package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionType;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.pane.transform.TransformPane;
import org.arnaudlt.warthog.ui.service.SqlExportToDatabaseService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ExportDialog {

    private final ConnectionsCollection connectionsCollection;

    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final TransformPane transformPane;

    private Stage owner;

    private ComboBox<Connection> connectionsListBox;

    private Stage dialog;


    @Autowired
    public ExportDialog(ConnectionsCollection connectionsCollection, NamedDatasetManager namedDatasetManager,
                        PoolService poolService, TransformPane transformPane) {
        this.connectionsCollection = connectionsCollection;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.transformPane = transformPane;
    }


    public void buildExportDialog(Stage owner) {

        this.owner = owner;
        this.dialog = StageFactory.buildModalStage(owner, "Export");

        GridPane common = GridFactory.buildGrid(new Insets(20,20,0,20));

        int i = 0;

        Label connectionLabel = new Label("Connection :");
        connectionsListBox = new ComboBox<>(connectionsCollection.getConnections());
        connectionsListBox.getSelectionModel().selectFirst();
        connectionsListBox.setMinWidth(220);
        connectionsListBox.setMaxWidth(220);
        common.addRow(i++, connectionLabel, connectionsListBox);

        common.add(new Separator(Orientation.HORIZONTAL), 0, i, 3, 1);

        // =============== Export to database ===============
        GridPane gridDatabase = GridFactory.buildGrid();

        int j = 0;

        Label tableNameLabel = new Label("Table name :");
        TextField tableName = new TextField();
        tableName.setMinWidth(220);
        tableName.setMaxWidth(220);
        gridDatabase.addRow(j++, tableNameLabel, tableName);

        Label saveModeBoxLabel = new Label("Mode :");

        ComboBox<String> saveModeBox = new ComboBox<>(FXCollections.observableArrayList("Overwrite", "Append"));
        saveModeBox.setValue("Overwrite");

        gridDatabase.addRow(j++, saveModeBoxLabel, saveModeBox);

        gridDatabase.add(new Separator(Orientation.HORIZONTAL), 0, j++, 3, 1);

        Button exportButton = new Button("Export");
        exportButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            if (selectedConnection != null) {
                ExportDatabaseSettings exportSettings = new ExportDatabaseSettings(tableName.getText(), saveModeBox.getValue());
                exportToDatabase(selectedConnection, exportSettings);
                dialog.close();
            }
        });
        gridDatabase.addRow(j, exportButton);
        // ==============================

        // =============== Export to Azure storage ===============
        GridPane gridAzureStorage = GridFactory.buildGrid();
        int k = 0;
        Label featureIncomingLabel = new Label("Feature coming soon ;-)");
        gridAzureStorage.addRow(k, featureIncomingLabel);
        // ===============


        gridDatabase.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
                Connection selectedConnection = connectionsListBox.getSelectionModel().selectedItemProperty().get();
                return selectedConnection != null && (
                        selectedConnection.getConnectionType() == ConnectionType.ORACLE_DATABASE ||
                        selectedConnection.getConnectionType() == ConnectionType.POSTGRESQL);
            }, connectionsListBox.getSelectionModel().selectedItemProperty()));

        gridAzureStorage.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            Connection selectedConnection = connectionsListBox.getSelectionModel().selectedItemProperty().get();
            return selectedConnection != null &&
                    selectedConnection.getConnectionType() == ConnectionType.AZURE_STORAGE;
        }, connectionsListBox.getSelectionModel().selectedItemProperty()));

        connectionsListBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) ->
                this.dialog.getScene().getWindow().sizeToScene());

        Scene dialogScene = StageFactory.buildScene(new VBox(common, new Group(gridDatabase, gridAzureStorage)));
        dialog.setScene(dialogScene);
    }


    public void showExportDatabaseDialog() {

        Utils.refreshComboBoxItems(connectionsListBox);
        dialog.show();
    }


    private void exportToDatabase(Connection selectedConnection, ExportDatabaseSettings exportDatabaseSettings) {

            final String sqlQuery = this.transformPane.getSqlQuery();
            SqlExportToDatabaseService sqlExportToDatabaseService = new SqlExportToDatabaseService(poolService, namedDatasetManager,
                    sqlQuery, selectedConnection, exportDatabaseSettings);
            sqlExportToDatabaseService.setOnSucceeded(success -> log.info("Database export succeeded"));
            sqlExportToDatabaseService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to generate the database export"));
            sqlExportToDatabaseService.start();
    }

}
