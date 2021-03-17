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
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionType;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Component
public class ConnectionsManagerDialog {


    private final ConnectionsCollection connectionsCollection;

    private Stage connectionManagerStage;

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

        connectionManagerStage = new Stage();
        connectionManagerStage.setTitle("Connections Manager");
        connectionManagerStage.initModality(Modality.APPLICATION_MODAL);
        connectionManagerStage.initOwner(stage);
        connectionManagerStage.setResizable(false);

        HBox hBox = new HBox();

        TreeItem<Connection> root = new TreeItem<>();
        connectionsList = new TreeView<>(root);
        connectionsList.setMinWidth(200);
        connectionsList.setShowRoot(false);
        connectionsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        connectionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            if (newSelection == null || newSelection.getValue() == null) {
                clearConnectionDetailsView();
            } else {
                displayConnection(newSelection.getValue());
            }
        });

        for (Connection oneConnection : this.connectionsCollection) {
            root.getChildren().add(new TreeItem<>(oneConnection));
        }

        GridPane connectionTypeHeader = GridFactory.buildGrid(new Insets(20,20,0,20));

        connectionType = new ComboBox<>(FXCollections.observableArrayList(ConnectionType.values()));

        Label connectionNameLabel = new Label("Name :");
        connectionName = new TextField();
        connectionName.setPrefWidth(250);
        int i = 0;
        connectionTypeHeader.addRow(i++, connectionNameLabel, connectionName, connectionType);

        connectionTypeHeader.add(new Separator(Orientation.HORIZONTAL), 0, i, 4, 1);

        Node databaseConnectionNode = getDatabaseConnectionNode();
        Node azureStorageConnectionNode = getAzureStorageConnectionNode();

        databaseConnectionNode.visibleProperty().bind(connectionType.valueProperty().isEqualTo(ConnectionType.ORACLE_DATABASE)
                .or(connectionType.valueProperty().isEqualTo(ConnectionType.POSTGRESQL)));
        azureStorageConnectionNode.visibleProperty().bind(connectionType.valueProperty().isEqualTo(ConnectionType.AZURE_STORAGE));

        Group group = new Group(databaseConnectionNode, azureStorageConnectionNode);


        Button createConnectionButton = new Button("", new MDL2IconFont("\uE710"));
        createConnectionButton.setTooltip(new Tooltip("Create a new connection"));
        createConnectionButton.setOnAction(event -> {

            Connection newConnection = new Connection("new connection", ConnectionType.ORACLE_DATABASE);
            this.connectionsCollection.getConnections().add(newConnection);

            TreeItem<Connection> newConnectionItem = new TreeItem<>(newConnection);
            this.connectionsList.getRoot().getChildren().add(newConnectionItem);
            this.connectionsList.getSelectionModel().select(newConnectionItem);
        });

        Button cloneConnectionButton = new Button("", new MDL2IconFont("\uE8C8"));
        cloneConnectionButton.setTooltip(new Tooltip("Clone selected connection"));
        cloneConnectionButton.setOnAction(event -> {

            TreeItem<Connection> connectionToClone = connectionsList.getSelectionModel().getSelectedItem();
            if (connectionToClone != null && connectionToClone.getValue() != null) {

                Connection newConnection = new Connection(connectionToClone.getValue());
                this.connectionsCollection.getConnections().add(newConnection);

                TreeItem<Connection> newConnectionItem = new TreeItem<>(newConnection);
                this.connectionsList.getRoot().getChildren().add(newConnectionItem);
                this.connectionsList.getSelectionModel().select(newConnectionItem);
            }
        });

        Button deleteConnectionButton = new Button("", new MDL2IconFont("\uE74D"));
        deleteConnectionButton.setTooltip(new Tooltip("Delete selected connection"));
        deleteConnectionButton.setOnAction(event -> {

            TreeItem<Connection> connectionToDeleteItem = connectionsList.getSelectionModel().getSelectedItem();
            if (connectionToDeleteItem != null && connectionToDeleteItem.getValue() != null) {

                AlertError.showConfirmationAlert("Do you want to delete connection : " + connectionToDeleteItem.getValue())
                        .filter(button -> button == ButtonType.OK)
                        .ifPresent(b -> {
                            this.connectionsCollection.getConnections().remove(connectionToDeleteItem.getValue());
                            this.connectionsList.getRoot().getChildren().remove(connectionToDeleteItem);
                            try {
                                this.connectionsCollection.persist();
                            } catch (IOException e) {
                                AlertError.showFailureAlert(e, "Unable to save changes");
                            }
                        });
            }
        });

        this.connectionsList.getSelectionModel().selectFirst();

        HBox controlButtons = new HBox(createConnectionButton, cloneConnectionButton, deleteConnectionButton);
        VBox connectionDetails = new VBox(connectionTypeHeader, group);

        hBox.getChildren().add(new VBox(controlButtons, connectionsList));
        hBox.getChildren().add(connectionDetails);
        Scene dialogScene = new Scene(hBox, 750, 400);
        JMetro metro = new JMetro(Style.LIGHT);
        metro.setAutomaticallyColorPanes(true);
        metro.setScene(dialogScene);
        connectionManagerStage.setScene(dialogScene);
    }


    public Node getAzureStorageConnectionNode() {

        GridPane grid = GridFactory.buildGrid(new Insets(10,20,0,20));

        int i = 0;

        Label outputLabel = new Label("SPN configuration file :");
        configurationFilePath = new TextField();
        configurationFilePath.setMinWidth(300);
        Button outputButton = new Button("...");
        outputButton.setOnAction(event -> {

            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Configuration file", "*.conf"));
            File exportFile = fc.showOpenDialog(connectionManagerStage);

            if (exportFile == null) return;
            configurationFilePath.setText(exportFile.getAbsolutePath());
        });
        grid.addRow(i++, outputLabel, configurationFilePath, outputButton);

        grid.add(new Separator(Orientation.HORIZONTAL), 0, i++, 2, 1);

        Button saveButton = new Button("Save");
        saveButton.setOnAction(event -> {

            TreeItem<Connection> selectedConnectionItem = this.connectionsList.getSelectionModel().getSelectedItem();

            Connection connection = selectedConnectionItem.getValue();
            connection.cleanUselessAttributs(); // not mandatory if the connection type has not changed
            connection.setName(connectionName.getText());
            connection.setConnectionType(connectionType.getValue());
            connection.setConfigurationFilePath(configurationFilePath.getText());
            log.info("Saving {}", connection);

            try {
                this.connectionsCollection.persist();
            } catch (IOException e) {
                AlertError.showFailureAlert(e, "Unable to save connections");
            }
            Utils.refreshTreeViewAllItems(this.connectionsList);

        });
        grid.addRow(i, saveButton);

        return grid;
    }


    public Node getDatabaseConnectionNode() {

        GridPane grid = GridFactory.buildGrid(new Insets(10,20,0,20));

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
                FXCollections.observableArrayList(Connection.getKnownDatabaseType()));
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
            connection.cleanUselessAttributs(); // not mandatory if the connection type has not changed
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
            Utils.refreshTreeViewAllItems(this.connectionsList);
        });
        grid.addRow(i, saveButton);
        return grid;
    }


    private void displayConnection(Connection connection) {

        this.connectionName.setText(connection.getName());
        this.connectionType.setValue(connection.getConnectionType());

        clearConnectionDetailsView();

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


    public void clearConnectionDetailsView() {

        this.configurationFilePath.setText("");
        this.host.setText("");
        this.port.setText("");
        this.database.setText("");
        this.databaseType.setValue("");
        this.user.setText("");
        this.password.setText("");
    }


    public void showConnectionsManagerDialog() {

        this.connectionManagerStage.show();
    }
}
