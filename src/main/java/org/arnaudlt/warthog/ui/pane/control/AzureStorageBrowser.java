package org.arnaudlt.warthog.ui.pane.control;

import com.azure.storage.file.datalake.models.PathItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzurePathItem;
import org.arnaudlt.warthog.model.azure.AzurePathItems;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.service.AzureDirectoryListingService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.ButtonFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class AzureStorageBrowser {

    private final Stage owner;

    private final PoolService poolService;

    private final Connection connection;

    private final String azureContainer;

    private final String azureStartingDirectory;


    public AzureStorageBrowser(Stage owner, PoolService poolService, Connection connection, String azureContainer, String azureStartingDirectory) {


        this.owner = owner;
        this.poolService = poolService;
        this.connection = connection;
        this.azureContainer = azureContainer;
        this.azureStartingDirectory = azureStartingDirectory;
    }


    public void browseAndSelect(AzurePathItems azurePathItems) {

        Stage dialog = StageFactory.buildModalStage(owner, "Azure storage browser", Modality.APPLICATION_MODAL, true);

        ObservableList<AzureSelectableItem> filesObservableList = FXCollections.observableArrayList();
        TableView<AzureSelectableItem> filesView = new TableView<>(filesObservableList);
        filesView.setEditable(true);
        filesView.setPlaceholder(new Label("No content"));
        VBox.setVgrow(filesView, Priority.ALWAYS);

        TableColumn<AzureSelectableItem, Boolean> checkBoxColumn = new TableColumn<>();
        checkBoxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkBoxColumn));
        checkBoxColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        checkBoxColumn.setEditable(true);
        filesView.getColumns().add(checkBoxColumn);

        TableColumn<AzureSelectableItem, String> itemName = new TableColumn<>("Name");
        itemName.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPathItem().getName()));
        filesView.getColumns().add(itemName);

        TableColumn<AzureSelectableItem, String> itemLastModification = new TableColumn<>("Last modified");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss");
        itemLastModification.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPathItem().getLastModified().format(dateFormatter)));
        filesView.getColumns().add(itemLastModification);

        itemName.prefWidthProperty().bind(
                filesView.widthProperty()
                        .subtract(checkBoxColumn.widthProperty())
                        .subtract(itemLastModification.widthProperty())
                        .subtract(2)
        );

        AzureDirectoryListingService azureDirectoryListingService = new AzureDirectoryListingService(
                poolService, connection, azureContainer, azureStartingDirectory);

        azureDirectoryListingService.setOnSucceeded(success -> {

            List<AzureSelectableItem> azureSelectableItems = azureDirectoryListingService.getValue().getItems()
                    .stream()
                    .map(api -> new AzureSelectableItem(api.getPathItem()))
                    .toList();

            log.info("Listing Azure directory content succeeded");

            filesObservableList.clear();
            filesObservableList.addAll(azureSelectableItems);
        });

        azureDirectoryListingService.setOnFailed(fail -> {

            log.error("Unable to list Azure directory content {}/{}", azureContainer, azureStartingDirectory);
            AlertFactory.showFailureAlert(owner, fail, "Not able to list Azure directory content");
            filesView.setPlaceholder(new Label("Failed to list Azure directory content"));
        });

        azureDirectoryListingService.setOnCancelled(cancel -> {

            log.warn("Listing Azure directory {}/{} cancelled", azureContainer, azureStartingDirectory);
            filesView.setPlaceholder(new Label("Listing Azure directory content cancelled"));
        });

        azureDirectoryListingService.setOnRunning(running -> {

            filesView.setPlaceholder(new ProgressBar(-1));
        });
        azureDirectoryListingService.start();

        HBox topControlBar = new HBox();
        TextField currentDirectory = new TextField(azureStartingDirectory);
        currentDirectory.setDisable(true);
        HBox.setHgrow(currentDirectory, Priority.ALWAYS);

        Button cancelNavigation = ButtonFactory.buildSegoeButton("\uF78A", "Cancel navigation");
        cancelNavigation.setOnAction(event -> azureDirectoryListingService.cancel());
        cancelNavigation.disableProperty().bind(azureDirectoryListingService.runningProperty().not());

        topControlBar.getChildren().addAll(cancelNavigation, currentDirectory);

        HBox bottomControlBar = new HBox();
        Button okButton = new Button("Ok");
        okButton.setOnAction(evt -> {

            azurePathItems.getItems().addAll(
                    filesView.getItems().stream()
                        .filter(p -> p.selected.getValue())
                        .toList()
            );
        });
        bottomControlBar.getChildren().addAll(okButton);
        bottomControlBar.setAlignment(Pos.CENTER_RIGHT);

        Scene dialogScene = StageFactory.buildScene(new VBox(topControlBar, filesView, bottomControlBar), 750, 400);
        filesView.prefWidthProperty().bind(dialogScene.widthProperty()); // TODO useless ?
        dialog.setScene(dialogScene);
        dialog.show();
    }


    public static class AzureSelectableItem extends AzurePathItem {

        private final BooleanProperty selected;


        public AzureSelectableItem(PathItem pathItem) {
            super(pathItem);
            this.selected = new SimpleBooleanProperty(true);
        }

        public boolean isSelected() {
            return selected.get();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        @Override
        public String toString() {
            return pathItem.getName() + ", selected = " + isSelected();
        }

    }
}
