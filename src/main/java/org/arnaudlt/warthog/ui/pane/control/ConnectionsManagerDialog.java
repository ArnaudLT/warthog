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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionType;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class ConnectionsManagerDialog {


    private final ConnectionsCollection connectionsCollection;

    private Stage dialog;

    private TreeView<Connection> connectionsList;

    private TextField connectionName;

    private ComboBox<ConnectionType> connectionType;

    private TextField configurationFilePath;

    private TextField host;

    private TextField port;

    private TextField database;

    private ComboBox<String> databaseType;

    private TextField user;

    private PasswordField password;


    @Autowired
    public ConnectionsManagerDialog(ConnectionsCollection connectionsCollection) {
        this.connectionsCollection = connectionsCollection;
    }


    public void buildConnectionsManagerDialog(Stage stage) {

        dialog = new Stage();
        dialog.setTitle("Connection Manager");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setResizable(false);

        HBox hBox = new HBox();

        TreeItem<Connection> root = new TreeItem<>();
        connectionsList = new TreeView<>(root);
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

        Node databaseConnectionNode = getDatabaseConnectionNode();
        Node azureStorageConnectionNode = getAzureStorageConnectionNode();

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


    public Node getAzureStorageConnectionNode() {

        GridPane grid = GridFactory.buildGrid();

        int i = 0;

        Label outputLabel = new Label("Spn file :");
        configurationFilePath = new TextField();
        configurationFilePath.setMinWidth(300);
        Button outputButton = new Button("...");
        outputButton.setOnAction(event -> {

            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text file", "*.txt"));
            File exportFile = fc.showOpenDialog(dialog);

            if (exportFile == null) return;
            configurationFilePath.setText(exportFile.getAbsolutePath());
        });
        grid.addRow(i++, outputLabel, configurationFilePath, outputButton);

        grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 2, 1);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            TreeItem<Connection> selectedConnectionItem = this.connectionsList.getSelectionModel().getSelectedItem();

            Connection connection = selectedConnectionItem.getValue();
            connection.clean(); // not mandatory if the connection type has not changed
            connection.setName(connectionName.getText());
            connection.setConnectionType(connectionType.getValue());
            connection.setConfigurationFilePath(configurationFilePath.getText());
            log.info("Save ... {}", connection.toExtraString());

            try {
                this.connectionsCollection.persist();
            } catch (IOException e) {
                AlertError.showFailureAlert(e, "Unable to save connections");
            }

        });
        grid.addRow(i, saveButton);

        return grid;
    }


    public Node getDatabaseConnectionNode() {

        GridPane grid = GridFactory.buildGrid();

        int i = 0;

        // =============== Database connection ================

        Label hostLabel = new Label("Host :");
        host = new TextField();

        Label portLabel = new Label("Port :");
        portLabel.setMaxWidth(30);
        port = new TextField();
        port.setMaxWidth(60);

        grid.add(hostLabel, 0, i);
        grid.add(host, 1, i, 2, 1);
        grid.add(portLabel, 3, i, 1, 1);
        grid.add(port, 4, i, 1, 1);
        i++;

        Label databaseLabel = new Label("Database :");
        database = new TextField();
        databaseType = new ComboBox<>(
                FXCollections.observableArrayList(ExportDatabaseSettings.getKnownDatabaseType()));
        databaseType.visibleProperty().bind(connectionType.valueProperty().isEqualTo(ConnectionType.ORACLE_DATABASE));
        grid.addRow(i++, databaseLabel, database, databaseType);

        Label userLabel = new Label("User :");
        user = new TextField();
        grid.addRow(i++, userLabel, user);

        Label passwordLabel = new Label("Password :");
        password = new PasswordField();
        grid.addRow(i++, passwordLabel, password);

        grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 5, 1);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            TreeItem<Connection> selectedConnectionItem = this.connectionsList.getSelectionModel().getSelectedItem();

            Connection connection = selectedConnectionItem.getValue();
            connection.clean(); // not mandatory if the connection type has not changed
            connection.setName(connectionName.getText());
            connection.setConnectionType(connectionType.getValue());
            connection.setHost(host.getText());
            connection.setPort(port.getText());
            connection.setDatabase(database.getText());
            connection.setDatabaseType(databaseType.getValue());
            connection.setUser(user.getText());
            connection.setPassword(password.getText());
            log.info("Save ... {}", connection.toExtraString());

            try {
                this.connectionsCollection.persist();
            } catch (IOException e) {
                AlertError.showFailureAlert(e, "Unable to save connections");
            }
        });
        grid.addRow(i, saveButton);
        return grid;
    }


    private void displayConnection(Connection connection) {

        this.connectionName.setText(connection.getName());
        this.connectionType.setValue(connection.getConnectionType());

        clear();

        switch (connection.getConnectionType()) {

            case ORACLE_DATABASE:
            case POSTGRESQL:
                this.host.setText(connection.getHost());
                this.port.setText(connection.getPort());
                this.database.setText(connection.getDatabase());
                this.databaseType.setValue(connection.getDatabaseType());
                this.user.setText(connection.getUser());
                this.password.setText(connection.getPassword());
                break;
            case AZURE_STORAGE:
                this.configurationFilePath.setText(connection.getConfigurationFilePath());
                break;
            default:
                throw new IllegalStateException("Unexpected connection type : " + connection.getConnectionType());
        }
    }


    public void clear() {

        this.configurationFilePath.setText("");
        this.host.setText("");
        this.port.setText("");
        this.database.setText("");
        this.databaseType.setValue("");
        this.user.setText("");
        this.password.setText("");
    }


    public void showConnectionsManagerDialog() {

        this.dialog.show();
    }
}
