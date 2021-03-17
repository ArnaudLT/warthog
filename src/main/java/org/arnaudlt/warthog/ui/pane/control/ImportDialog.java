package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionType;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromDatabaseService;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ImportDialog {

    private final ConnectionsCollection connectionsCollection;

    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final ExplorerPane explorerPane;

    private ComboBox<Connection> connectionsListBox;

    private Stage dialog;


    @Autowired
    public ImportDialog(ConnectionsCollection connectionsCollection, NamedDatasetManager namedDatasetManager, PoolService poolService, ExplorerPane explorerPane) {
        this.connectionsCollection = connectionsCollection;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.explorerPane = explorerPane;
    }


    public void buildImportDialog(Stage stage) {

        this.dialog = new Stage();
        this.dialog.setTitle("Import");
        this.dialog.initModality(Modality.APPLICATION_MODAL);
        this.dialog.initOwner(stage);
        this.dialog.setResizable(false);

        GridPane common = GridFactory.buildGrid(new Insets(20,20,0,20));

        int i = 0;

        Label connectionLabel = new Label("Connection :");
        connectionsListBox = new ComboBox<>(connectionsCollection.getConnections());
        connectionsListBox.setMinWidth(220);
        connectionsListBox.setMaxWidth(220);
        common.addRow(i++, connectionLabel, connectionsListBox);

        common.add(new Separator(Orientation.HORIZONTAL), 0, i, 3, 1);

        // =============== Import from database ===============
        GridPane gridDatabase = GridFactory.buildGrid();

        int j = 0;

        Label tableNameLabel = new Label("Table name :");
        TextField tableName = new TextField();
        tableName.setMinWidth(220);
        tableName.setMaxWidth(220);
        gridDatabase.addRow(j++, tableNameLabel, tableName);

        gridDatabase.add(new Separator(Orientation.HORIZONTAL), 0, j++, 3, 1);

        Button importButton = new Button("Import");
        importButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            if (selectedConnection != null) {

                importTable(selectedConnection, tableName.getText());
                dialog.close();
            }
        });
        gridDatabase.addRow(j, importButton);
        // ==============================

        // =============== Import from Azure storage ===============
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


        Scene dialogScene = new Scene(new VBox(common, new Group(gridDatabase, gridAzureStorage)), 350, 190);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        dialog.setScene(dialogScene);
    }


    public void showImportDatabaseDialog() {

        Utils.refreshComboBoxAllItems(connectionsListBox);
        dialog.show();
    }


    public void importTable(Connection connection, String tableName) {

        NamedDatasetImportFromDatabaseService importService = new NamedDatasetImportFromDatabaseService(namedDatasetManager, connection, tableName);
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Not able to add the dataset '"+ tableName +"'"));
        importService.setExecutor(this.poolService.getExecutor());
        importService.start();
    }

}
