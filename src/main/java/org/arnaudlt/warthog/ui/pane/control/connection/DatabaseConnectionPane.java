package org.arnaudlt.warthog.ui.pane.control.connection;

import javafx.collections.FXCollections;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionType;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Getter
@Component
public class DatabaseConnectionPane {

    private final ConnectionsCollection connectionsCollection;

    private TextField host;

    private TextField port;

    private TextField database;

    private ComboBox<String> databaseType;

    private TextField user;

    private PasswordField password;

    private Button saveButton;

    private TreeView<Connection> connectionsList;


    @Autowired
    public DatabaseConnectionPane(ConnectionsCollection connectionsCollection) {
        this.connectionsCollection = connectionsCollection;
    }


    public Node getDatabaseConnectionNode(TreeView<Connection> connectionsList, TextField connectionName, ComboBox<ConnectionType> connectionType) {

        this.connectionsList = connectionsList;

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

        saveButton = new Button("Save");
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


    public void load(Connection connection) {

        log.info("Load {} in database connection pane", connection);
        this.host.setText(connection.getHost());
        this.port.setText(connection.getPort());
        this.database.setText(connection.getDatabase());
        this.databaseType.setValue(connection.getDatabaseType());
        this.user.setText(connection.getUser());
        this.password.setText(connection.getPassword());
    }


    public void clear() {

        this.host.setText("");
        this.port.setText("");
        this.database.setText("");
        this.databaseType.setValue("");
        this.user.setText("");
        this.password.setText("");
    }
}
