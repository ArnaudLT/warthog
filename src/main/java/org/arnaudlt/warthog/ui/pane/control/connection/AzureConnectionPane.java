package org.arnaudlt.warthog.ui.pane.control.connection;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionType;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Slf4j
@Getter
@Component
public class AzureConnectionPane {

    private final ConnectionsCollection connectionsCollection;

    private TextField configurationFilePath;

    private Button saveButton;

    private TreeView<Connection> connectionsList;


    @Autowired
    public AzureConnectionPane(ConnectionsCollection connectionsCollection) {
        this.connectionsCollection = connectionsCollection;
    }


    public Node getAzureStorageConnectionNode(TreeView<Connection> connectionsList, Stage dialog, TextField connectionName, ComboBox<ConnectionType> connectionType) {

        this.connectionsList = connectionsList;

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

        saveButton = new Button("Save");
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


    public void load(Connection connection) {

        log.info("Load {}", connection);
        this.configurationFilePath.setText(connection.getConfigurationFilePath());
    }


    public void clear() {

        this.configurationFilePath.setText("");
    }
}
