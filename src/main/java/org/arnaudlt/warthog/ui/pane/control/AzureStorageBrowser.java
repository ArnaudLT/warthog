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
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.ui.util.StageFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AzureStorageBrowser {

    private final Stage owner;

    private final Connection connection;

    private final String startingDirectory;


    public AzureStorageBrowser(Stage owner, Connection connection, String startingDirectory) {


        this.owner = owner;
        this.connection = connection;
        this.startingDirectory = startingDirectory;
    }


    public void browseAndSelect(List<AzurePathItem> selectedAzureFiles) {

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

        // TODO Scan and fill the list with real data
        filesObservableList.addAll(AzureSelectableItem.sampleData());

        HBox topControlBar = new HBox();
        TextField currentDirectory = new TextField(startingDirectory);
        currentDirectory.setDisable(true);
        HBox.setHgrow(currentDirectory, Priority.ALWAYS);

        Button magic1Button = new Button("Magic trick 1 !");
        magic1Button.setOnAction(evt -> filesView.getItems().forEach(asi -> asi.setSelected(true)));
        Button magic2Button = new Button("Magic trick 2 !");
        magic2Button.setOnAction(evt -> filesView.getItems().forEach(asi -> asi.setSelected(false)));

        topControlBar.getChildren().addAll(magic1Button, magic2Button, currentDirectory);

        HBox bottomControlBar = new HBox();
        Button okButton = new Button("Ok");
        okButton.setOnAction(evt -> {

            selectedAzureFiles.addAll(
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

        static List<AzureSelectableItem> sampleData() {

            return List.of(
                    new AzureSelectableItem(new PathItem("eTag", OffsetDateTime.now(), 8_192, "admin", false, "toto.json", "arnaud", "rwx-rw-r")),
                    new AzureSelectableItem(new PathItem("eTag", OffsetDateTime.now(), 0, "power_users", true, "data", "camille", "rwx-rw-r")),
                    new AzureSelectableItem(new PathItem("eTag", OffsetDateTime.now(), 23_496, "power_users", false, "titi.json", "camille", "rwx-rw-r")),
                    new AzureSelectableItem(new PathItem("eTag", OffsetDateTime.now(), 1_024, "admin", false, "tete.json", "arnaud", "rwx-rw-r")),
                    new AzureSelectableItem(new PathItem("eTag", OffsetDateTime.now(), 16_384, "user", false, "tata.json", "virginie", "rwx-rw-r")));
        }
    }
}
