package org.arnaudlt.warthog.ui.pane.control;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
import org.arnaudlt.warthog.ui.pane.control.connection.AzureConnectionPane;
import org.arnaudlt.warthog.ui.pane.control.connection.DatabaseConnectionPane;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConnectionsManagerDialog {


    private final ConnectionsCollection connectionsCollection;

    private final DatabaseConnectionPane databaseConnectionPane;

    private final AzureConnectionPane azureConnectionPane;

    private Stage dialog;

    private TextField connectionName;

    private ComboBox<ConnectionType> connectionType;


    @Autowired
    public ConnectionsManagerDialog(ConnectionsCollection connectionsCollection,
                                    DatabaseConnectionPane databaseConnectionPane, AzureConnectionPane azureConnectionPane) {
        this.connectionsCollection = connectionsCollection;
        this.databaseConnectionPane = databaseConnectionPane;
        this.azureConnectionPane = azureConnectionPane;
    }


    public void buildConnectionsManagerDialog(Stage stage) {

        dialog = new Stage();
        dialog.setTitle("Connection Manager");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setResizable(false);

        HBox hBox = new HBox();

        TreeItem<Connection> root = new TreeItem<>();
        TreeView<Connection> connectionsList = new TreeView<>(root);
        connectionsList.setMinWidth(200);
        connectionsList.setShowRoot(false);
        connectionsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        connectionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            if (newSelection == null || newSelection.getValue() == null) return;
            displayConnection(newSelection.getValue());
        });

        for (Connection oneConnection : this.connectionsCollection) {
            root.getChildren().add(new TreeItem<>(oneConnection));
        }

/*
        Connection fakeAZConnection1 = new Connection("Azure storage sample", ConnectionType.AZURE_STORAGE);
        fakeAZConnection1.setConfigurationFilePath("C:\\Users\\Arnaud\\Downloads\\samples\\config.snp");
        root.getChildren().add(new TreeItem<>(fakeAZConnection1));
        Connection fakeORAConnection2 = new Connection("Oracle database sample", ConnectionType.ORACLE_DATABASE);

        root.getChildren().add(new TreeItem<>(fakeORAConnection2));
        Connection fakePGConnection3 = new Connection("PostgreSQL sample", ConnectionType.POSTGRESQL);
        root.getChildren().add(new TreeItem<>(fakePGConnection3));

        connectionsCollection.getConnections().addAll(Arrays.asList(fakeAZConnection1, fakeORAConnection2, fakePGConnection3));
        try {
            connectionsCollection.persist();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/


        GridPane connectionTypeHeader = GridFactory.buildGrid(new Insets(20,20,0,20));

        connectionType = new ComboBox<>(FXCollections.observableArrayList(ConnectionType.values()));

        Label connectionNameLabel = new Label("Name :");
        connectionName = new TextField();
        int i = 0;
        connectionTypeHeader.addRow(i++, connectionNameLabel, connectionName, connectionType);

        connectionTypeHeader.add(new Separator(Orientation.HORIZONTAL), 0, i, 4, 1);

        Node databaseConnectionNode = this.databaseConnectionPane.getDatabaseConnectionNode(connectionsList, connectionName, connectionType);
        Node azureStorageConnectionNode = this.azureConnectionPane.getAzureStorageConnectionNode(connectionsList, dialog, connectionName, connectionType);

        databaseConnectionNode.visibleProperty().bind(connectionType.valueProperty().isEqualTo(ConnectionType.ORACLE_DATABASE)
                .or(connectionType.valueProperty().isEqualTo(ConnectionType.POSTGRESQL)));
        azureStorageConnectionNode.visibleProperty().bind(connectionType.valueProperty().isEqualTo(ConnectionType.AZURE_STORAGE));

        Group group = new Group(databaseConnectionNode, azureStorageConnectionNode);
        VBox connectionDetails = new VBox(connectionTypeHeader, group);

        hBox.getChildren().add(connectionsList);
        hBox.getChildren().add(connectionDetails);
        Scene dialogScene = new Scene(hBox, 750, 400);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        dialog.setScene(dialogScene);
    }


    private void displayConnection(Connection connection) {

        this.connectionName.setText(connection.getName());
        this.connectionType.setValue(connection.getConnectionType());

        databaseConnectionPane.clear();
        azureConnectionPane.clear();

        switch (connection.getConnectionType()) {

            case ORACLE_DATABASE:
            case POSTGRESQL:
                databaseConnectionPane.load(connection);
                break;
            case AZURE_STORAGE:
                azureConnectionPane.load(connection);
                break;
            default:
                throw new IllegalStateException("Unexpected connection type : " + connection.getConnectionType());
        }
    }


    public void showConnectionsManagerDialog() {

        this.dialog.show();
    }
}
