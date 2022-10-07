package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.StringProperty;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzurePathItems;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.connection.ConnectionType;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ImportAzureDfsStorageSettings;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.pane.explorer.ExplorerPane;
import org.arnaudlt.warthog.ui.service.AzureDirectoryStatisticsService;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromAzureDfsStorageService;
import org.arnaudlt.warthog.ui.service.NamedDatasetImportFromDatabaseService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.GridFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;


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

    private AzurePathItems azurePathItems;


    @Autowired
    public ImportDialog(ConnectionsCollection connectionsCollection, NamedDatasetManager namedDatasetManager, PoolService poolService, ExplorerPane explorerPane) {
        this.connectionsCollection = connectionsCollection;
        this.namedDatasetManager = namedDatasetManager;
        this.poolService = poolService;
        this.explorerPane = explorerPane;
        this.azurePathItems = new AzurePathItems();
    }


    public void buildImportDialog(Stage owner) {

        this.owner = owner;
        this.dialog = StageFactory.buildModalStage(owner, "Import");

        GridPane common = GridFactory.buildGrid();

        int i = 0;

        Label connectionLabel = new Label("Connection :");
        connectionsListBox = new ComboBox<>(connectionsCollection.getConnections());
        connectionsListBox.setMinWidth(300);
        connectionsListBox.setMaxWidth(300);
        connectionsListBox.getSelectionModel().selectFirst();

        common.addRow(i++, connectionLabel, connectionsListBox);

        // =============== Import from database ===============
        Node nodeDatabase = getDatabaseImportNode();

        // ==============================

        // =============== Import from Azure storage ===============
        Node nodeAzureStorage = getAzureStorageImportNode();

        // ===============

        nodeDatabase.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            Connection selectedConnection = connectionsListBox.getSelectionModel().selectedItemProperty().get();
            return selectedConnection != null && (
                    selectedConnection.getConnectionType() == ConnectionType.ORACLE_DATABASE ||
                            selectedConnection.getConnectionType() == ConnectionType.POSTGRESQL);
        }, connectionsListBox.getSelectionModel().selectedItemProperty()));

        nodeAzureStorage.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            Connection selectedConnection = connectionsListBox.getSelectionModel().selectedItemProperty().get();
            return selectedConnection != null &&
                    selectedConnection.getConnectionType() == ConnectionType.AZURE_STORAGE;
        }, connectionsListBox.getSelectionModel().selectedItemProperty()));

        connectionsListBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {

            this.dialog.getScene().getWindow().sizeToScene();
        });

        Scene dialogScene = StageFactory.buildScene(new VBox(common, new Group(nodeDatabase, nodeAzureStorage)));
        dialog.setScene(dialogScene);
    }


    public Node getDatabaseImportNode() {

        GridPane basicSettingsNode = GridFactory.buildGrid();

        Label tableNameLabel = new Label("Table name :");
        TextField tableName = new TextField();
        tableName.setMinWidth(300);
        tableName.setMaxWidth(300);
        basicSettingsNode.addRow(0, tableNameLabel, tableName);

        Tab basicSettingsTab = new Tab("Settings", basicSettingsNode);
        TabPane tabPane = new TabPane(basicSettingsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        GridPane bottomGrid = GridFactory.buildGrid();
        Button importTableButton = new Button("Import");
        importTableButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            if (selectedConnection != null) {

                importTable(selectedConnection, tableName.getText());
                dialog.close();
            }
        });
        bottomGrid.addRow(0, importTableButton);

        return new VBox(tabPane, bottomGrid);
    }


    public Node getAzureStorageImportNode() {

        // Basic settings
        GridPane basicSettingsNode = GridFactory.buildGrid();
        int rowIndex = 0;

        Label azContainerLabel = new Label("Azure container :");
        TextField azContainerField = new TextField();
        azContainerField.setMinWidth(300);
        azContainerField.setMaxWidth(300);
        basicSettingsNode.addRow(rowIndex++, azContainerLabel, azContainerField);

        Label azDirectoryLabel = new Label("Azure directory :");
        TextField azDirectoryField = new TextField();
        Button azureDirectoryBrowserButton = new Button("...");

        Connection initiallySelectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
        if (initiallySelectedConnection != null && initiallySelectedConnection.getConnectionType() == ConnectionType.AZURE_STORAGE) {

            azContainerField.setText(initiallySelectedConnection.getPreferredContainer());
            azDirectoryField.setText(initiallySelectedConnection.getPreferredAzureDirectory());
        }

        connectionsListBox.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {

            if (newValue != null && newValue.getConnectionType() == ConnectionType.AZURE_STORAGE) {

                    azContainerField.setText(newValue.getPreferredContainer());
                    azDirectoryField.setText(newValue.getPreferredAzureDirectory());
            }
        });

        azureDirectoryBrowserButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            String azureContainer = azContainerField.getText().strip();
            AzureStorageBrowser azureStorageBrowser = new AzureStorageBrowser(
                    dialog, poolService, selectedConnection, azureContainer, azDirectoryField.textProperty());
            azurePathItems = azureStorageBrowser.browseAndSelect();
        });
        basicSettingsNode.addRow(rowIndex++, azDirectoryLabel, azDirectoryField, azureDirectoryBrowserButton);

        basicSettingsNode.add(new Separator(Orientation.HORIZONTAL), 0, rowIndex++, 3, 1);

        Label localDirectoryLabel = new Label("Local directory :");
        TextField localDirectoryField = new TextField();
        Button directoryChooserButton = new Button("...");
        directoryChooserButton.setOnAction(event -> {

            DirectoryChooser fc = new DirectoryChooser();
            File exportFile = fc.showDialog(this.dialog);
            if (localDirectoryField.getText() != null) {
                fc.setInitialDirectory(new File(localDirectoryField.getText()));
            }
            if (exportFile == null) return;
            localDirectoryField.setText(exportFile.getAbsolutePath());
        });


        basicSettingsNode.addRow(rowIndex++, localDirectoryLabel, localDirectoryField, directoryChooserButton);

        Label nameLabel = new Label("Name :");
        TextField nameField = new TextField();
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.matches("^[a-zA-Z0-9_]*$")) {

                ((StringProperty)observable).setValue(oldValue);
            }
        });
        basicSettingsNode.addRow(rowIndex++, nameLabel, nameField);

        Tab basicSettingsTab = new Tab("Settings", basicSettingsNode);

        // Advanced settings
        GridPane advancedSettingsNode = GridFactory.buildGrid();
        rowIndex = 0;

        Label basePathLabel = new Label("Import base path :");
        TextField basePathField = new TextField();
        basePathField.setMinWidth(250);
        basePathField.setMaxWidth(250);

        CheckBox automaticBasePathCheckBox = new CheckBox("Auto");
        automaticBasePathCheckBox.setSelected(true);
        basePathField.disableProperty().bind(automaticBasePathCheckBox.selectedProperty());

        StringBinding basePathFieldBind = Bindings.createStringBinding(() ->
                        Paths.get(
                                Objects.requireNonNull(localDirectoryField.getText(), ""),
                                Objects.requireNonNull(azContainerField.getText(), ""),
                                Objects.requireNonNull(azDirectoryField.getText(), "")).toString(),
                localDirectoryField.textProperty(), azContainerField.textProperty(), azDirectoryField.textProperty());

        basePathField.textProperty().bind(basePathFieldBind);
        automaticBasePathCheckBox.selectedProperty().addListener((selectedProperty, oldValue, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                basePathField.textProperty().bind(basePathFieldBind);
            } else {
                basePathField.textProperty().unbind();
            }
        });

        advancedSettingsNode.addRow(rowIndex++, basePathLabel, basePathField, automaticBasePathCheckBox);

        Tab advancedSettingsTab = new Tab("Advanced", advancedSettingsNode);
        TabPane tabPane = new TabPane(basicSettingsTab, advancedSettingsTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        GridPane bottomGrid = GridFactory.buildGrid();
        Button importAzureButton = new Button("Import");
        importAzureButton.setOnAction(event -> {

            Connection selectedConnection = connectionsListBox.getSelectionModel().getSelectedItem();
            if (selectedConnection == null) return;

            final String azContainer = azContainerField.getText().strip();
            final String azDirectoryPath = azDirectoryField.getText().strip();
            final String localDirectory = localDirectoryField.getText().strip();
            final String basePath = basePathField.getText().strip();
            final String name = nameField.getText().strip();

            ImportAzureDfsStorageSettings importAzureDfsStorageSettings = new ImportAzureDfsStorageSettings(
                    azContainer, azDirectoryPath, azurePathItems, localDirectory, basePath, name);

            AzureDirectoryStatisticsService directoryStatisticsService = new AzureDirectoryStatisticsService(poolService, selectedConnection, importAzureDfsStorageSettings);
            directoryStatisticsService.setOnSucceeded(success -> {

                AzureDirectoryStatisticsService.DirectoryStatistics statistics = directoryStatisticsService.getValue();
                AlertFactory.showConfirmationAlert(owner, "Do you want to download " + statistics.filesCount + " files for " + Utils.format2Decimals(statistics.bytes / 1_000_000d) + " MB ?")
                        .filter(button -> button == ButtonType.OK)
                        .ifPresent(b -> {

                            importFromAzure(selectedConnection, importAzureDfsStorageSettings, statistics);
                            dialog.close();
                        });
                importAzureButton.setDisable(false);
            });
            directoryStatisticsService.setOnFailed(fail -> {
                importAzureButton.setDisable(false);
                AlertFactory.showFailureAlert(owner, fail, "Not able to check directory size '" + azDirectoryPath + "'");
            });
            directoryStatisticsService.setOnCancelled(cancel -> importAzureButton.setDisable(false));
            directoryStatisticsService.start();
            importAzureButton.setDisable(true);
        });

        bottomGrid.addRow(0, importAzureButton);

        return new VBox(tabPane, bottomGrid);
    }


    public void showImportDialog() {

        Utils.refreshComboBoxItems(connectionsListBox);
        dialog.show();
    }


    public void importTable(Connection connection, String tableName) {

        NamedDatasetImportFromDatabaseService importService = new NamedDatasetImportFromDatabaseService(poolService, namedDatasetManager, connection, tableName);
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Not able to import the dataset '" + tableName + "'"));
        importService.start();
    }


    public void importFromAzure(Connection connection, ImportAzureDfsStorageSettings importAzureDfsStorageSettings,
                                AzureDirectoryStatisticsService.DirectoryStatistics statistics) {

        NamedDatasetImportFromAzureDfsStorageService importService = new NamedDatasetImportFromAzureDfsStorageService(
                poolService, namedDatasetManager, connection, importAzureDfsStorageSettings, statistics);
        importService.setOnSucceeded(success -> explorerPane.addNamedDatasetItem(importService.getValue()));
        importService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail,
                "Not able to import the dataset '" + importAzureDfsStorageSettings.azDirectoryPath() + "'"));
        importService.start();
    }


}
