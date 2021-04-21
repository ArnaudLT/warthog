package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
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
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromAzureDfsStorageService;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromDatabaseService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Random;


@Slf4j
@Component
public class ImportDialog {

    private final ConnectionsCollection connectionsCollection;

    private final NamedDatasetManager namedDatasetManager;

    private final PoolService poolService;

    private final ExplorerPane explorerPane;

    private Stage owner;

    private ComboBox<Connection> connectionsListBox;

    private Stage dialog;


    @Autowired
    public ImportDialog(ConnectionsCollection connectionsCollection, NamedDatasetManager namedDatasetManager, PoolService poolService, ExplorerPane explorerPane) {
        this.connectionsCollection = connectionsCollection;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.explorerPane = explorerPane;
    }


    public void buildImportDialog(Stage owner) {

        this.owner = owner;
        this.dialog = StageFactory.buildModalStage(owner, "Import");

        GridPane common = GridFactory.buildGrid(new Insets(20,20,0,20));

        int i = 0;

        Label connectionLabel = new Label("Connection :");
        connectionsListBox = new ComboBox<>(connectionsCollection.getConnections());
        connectionsListBox.getSelectionModel().selectFirst();
        connectionsListBox.setMinWidth(220);
        connectionsListBox.setMaxWidth(220);
        common.addRow(i++, connectionLabel, connectionsListBox);

        common.add(new Separator(Orientation.HORIZONTAL), 0, i, 3, 1);

        // =============== Import from database ===============
        GridPane gridDatabase = GridFactory.buildGrid();

        int j = 0;

        Label tableNameLabel = new Label("Table name :");
        TextField tableName = new TextField();
        tableName.setMinWidth(200);
        tableName.setMaxWidth(200);
        gridDatabase.addRow(j++, tableNameLabel, tableName);

        gridDatabase.add(new Separator(Orientation.HORIZONTAL), 0, j++, 2, 1);

        Button importTableButton = new Button("Import");
        importTableButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            if (selectedConnection != null) {

                importTable(selectedConnection, tableName.getText());
                dialog.close();
            }
        });
        gridDatabase.addRow(j, importTableButton);
        // ==============================

        // =============== Import from Azure storage ===============
        GridPane gridAzureStorage = GridFactory.buildGrid();
        int k = 0;

        Label containerLabel = new Label("Container :");
        TextField container = new TextField();

        gridAzureStorage.addRow(k++, containerLabel, container);

        Label pathLabel = new Label("Path :");
        TextField azPath = new TextField();
        azPath.setMinWidth(200);
        azPath.setMaxWidth(200);

        gridAzureStorage.addRow(k++, pathLabel, azPath);

        gridAzureStorage.add(new Separator(Orientation.HORIZONTAL), 0, k++, 2, 1);

        Button importAzureButton = new Button("Import...");
        importAzureButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            if (selectedConnection != null) {

                DirectoryChooser dc = new DirectoryChooser();
                File targetDirectory = dc.showDialog(owner);
                if (targetDirectory != null) {

                    log.info("Download from Azure not yet implemented ! (fake inc)");
                    importFromAzure(selectedConnection, container.getText(), azPath.getText(), targetDirectory.getAbsolutePath());
                    dialog.close();
                }
            }
        });
        gridAzureStorage.addRow(k, importAzureButton);

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


        Scene dialogScene = new Scene(new VBox(common, new Group(gridDatabase, gridAzureStorage)), 350, 220);
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
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to import the dataset '"+ tableName +"'"));
        importService.setExecutor(this.poolService.getExecutor());
        importService.start();
    }


    public void importFromAzure(Connection connection, String container, String path, String targetDirectory) {

        NamedDatasetImportFromAzureDfsStorageService importService = new NamedDatasetImportFromAzureDfsStorageService(
                namedDatasetManager, connection, container, path, targetDirectory);
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to import the dataset '"+ path +"'"));
        importService.setExecutor(this.poolService.getExecutor());
        importService.start();
    }

}
