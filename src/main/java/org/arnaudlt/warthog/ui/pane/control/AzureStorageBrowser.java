package org.arnaudlt.warthog.ui.pane.control;

import com.azure.storage.file.datalake.models.PathItem;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import org.arnaudlt.warthog.ui.util.LabelFactory;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
public class AzureStorageBrowser {

    private final Stage owner;

    private final PoolService poolService;

    private final Connection connection;

    private TableView<AzureSelectableItem> filesView;

    private final AzurePathItems selectedAzurePathItems;

    private final String azureContainer;

    private final StringProperty azureCurrentDirectory;


    public AzureStorageBrowser(Stage owner, PoolService poolService, Connection connection, String azureContainer, StringProperty azureCurrentDirectory) {


        this.owner = owner;
        this.poolService = poolService;
        this.connection = connection;
        this.selectedAzurePathItems = new AzurePathItems();
        this.azureContainer = azureContainer;
        this.azureCurrentDirectory = azureCurrentDirectory;
    }


    public AzurePathItems browseAndSelect() {

        Stage dialog = StageFactory.buildModalStage(owner, "Azure storage browser", Modality.APPLICATION_MODAL, true);

        ObservableList<AzureSelectableItem> filesObservableList = FXCollections.observableArrayList();
        filesView = new TableView<>(filesObservableList);
        filesView.setEditable(true);
        filesView.setPlaceholder(new Label("No content"));
        VBox.setVgrow(filesView, Priority.ALWAYS);

        TableColumn<AzureSelectableItem, Boolean> checkBoxColumn = new TableColumn<>();

        CheckBox selectAll = new CheckBox();
        selectAll.setSelected(true);
        selectAll.selectedProperty().addListener((obs, oldValue, newValue) ->
                setAllCheckBoxAzurePathItems(!Boolean.TRUE.equals(oldValue)));
        checkBoxColumn.setGraphic(selectAll);
        checkBoxColumn.setPrefWidth(40);
        checkBoxColumn.setCellFactory(CheckBoxTableCell.forTableColumn(checkBoxColumn));
        checkBoxColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        checkBoxColumn.setEditable(true);
        filesView.getColumns().add(checkBoxColumn);

        TableColumn<AzureSelectableItem, Node> itemName = new TableColumn<>("Name");

        itemName.setCellValueFactory(param -> {

            Label icon = getIcon(param);
            Label name = new Label(param.getValue().getItemShortName());
            return new SimpleObjectProperty<>(new IconAndName(10, icon, name));
        });
        filesView.getColumns().add(itemName);

        TableColumn<AzureSelectableItem, String> itemLastModification = new TableColumn<>("Last modified");
        itemLastModification.setPrefWidth(130);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss");
        itemLastModification.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getPathItem().getLastModified().format(dateFormatter)));
        filesView.getColumns().add(itemLastModification);

        itemName.prefWidthProperty().bind(
                filesView.widthProperty()
                        .subtract(checkBoxColumn.widthProperty())
                        .subtract(itemLastModification.widthProperty())
                        .subtract(2)
        );

        filesView.setRowFactory(tv -> {
            TableRow<AzureSelectableItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    AzureSelectableItem clickedAzureItem = row.getItem();
                    if (clickedAzureItem.getPathItem().isDirectory()) {
                        azureCurrentDirectory.set(clickedAzureItem.getPathItem().getName());
                        startAzureDirectoryListingService();
                    }
                }
            });
            return row;
        });

        startAzureDirectoryListingService();

        HBox topControlBar = new HBox();
        TextField currentDirectory = new TextField();
        currentDirectory.textProperty().bind(azureCurrentDirectory);
        currentDirectory.setDisable(true);
        HBox.setHgrow(currentDirectory, Priority.ALWAYS);

        Button parentDirectoryButton = ButtonFactory.buildSegoeButton("\uE752", "Parent directory");
        parentDirectoryButton.setOnAction(event -> {

            String azureParentDirectoryString = getAzureParentDirectory();
            azureCurrentDirectory.set(azureParentDirectoryString);
            startAzureDirectoryListingService();
        });

        topControlBar.getChildren().addAll(parentDirectoryButton, currentDirectory);

        HBox bottomControlBar = new HBox();
        Button okButton = new Button("Ok");
        okButton.setOnAction(evt -> {

            selectedAzurePathItems.getItems().addAll(
                    filesView.getItems().stream()
                        .filter(p -> p.selected.getValue())
                        .toList()
            );
            dialog.close();
        });
        bottomControlBar.getChildren().addAll(okButton);
        bottomControlBar.setAlignment(Pos.CENTER_RIGHT);

        Scene dialogScene = StageFactory.buildScene(new VBox(topControlBar, filesView, bottomControlBar), 750, 400);
        filesView.prefWidthProperty().bind(dialogScene.widthProperty()); // TODO useless ?
        dialog.setScene(dialogScene);
        dialog.show();
        return selectedAzurePathItems;
    }


    @NotNull
    private String getAzureParentDirectory() {

        String currentAzureDirectory = azureCurrentDirectory.getValue();
        Path parentDirectoryPath = Paths.get(currentAzureDirectory).getParent();
        if (parentDirectoryPath == null) {
            parentDirectoryPath = Paths.get("");
        }
        return parentDirectoryPath.toString().replace("\\", "/");
    }


    @NotNull
    private Label getIcon(TableColumn.CellDataFeatures<AzureSelectableItem, Node> param) {

        Label icon;
        if (param.getValue().getPathItem().isDirectory()) {
            icon = LabelFactory.buildSegoeLabel("\uF12B");
            icon.setStyle("-fx-text-fill: #F1C40F;");
        } else {
            icon = LabelFactory.buildSegoeLabel("\uE7C3");
        }
        return icon;
    }


    private void setAllCheckBoxAzurePathItems(boolean select) {

        filesView.getItems().forEach(asi -> asi.setSelected(select));
    }


    private void startAzureDirectoryListingService() {

        AzureDirectoryListingService azureDirectoryListingService = new AzureDirectoryListingService(
                poolService, connection, azureContainer, azureCurrentDirectory.getValue());

        azureDirectoryListingService.setOnSucceeded(success -> {

            List<AzureSelectableItem> azureSelectableItems = azureDirectoryListingService.getValue().getItems()
                    .stream()
                    .map(api -> new AzureSelectableItem(api.getPathItem()))
                    .toList();

            log.info("Listing Azure directory content succeeded");

            filesView.getItems().clear();
            filesView.getItems().addAll(azureSelectableItems);
        });

        azureDirectoryListingService.setOnFailed(fail -> {

            log.error("Unable to list Azure directory content {}/{}", azureContainer, azureCurrentDirectory);
            AlertFactory.showFailureAlert(owner, fail, "Not able to list Azure directory content");
            filesView.setPlaceholder(new Label("Failed to list Azure directory content"));
        });

        azureDirectoryListingService.setOnCancelled(cancel -> {

            log.warn("Listing Azure directory {}/{} cancelled", azureContainer, azureCurrentDirectory);
            filesView.setPlaceholder(new Label("Listing Azure directory content cancelled"));
        });

        azureDirectoryListingService.setOnRunning(running -> {

            filesView.setPlaceholder(new ProgressBar(-1));
        });
        azureDirectoryListingService.start();
    }


    public static class IconAndName extends HBox implements Comparable<IconAndName> {

        private final String icon;

        private final String name;

        public IconAndName(double spacing, Label icon, Label name) {
            super(spacing, icon, name);
            this.icon = icon.getText();
            this.name = name.getText();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IconAndName that = (IconAndName) o;

            if (!Objects.equals(icon, that.icon)) return false;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            int result = icon != null ? icon.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }

        @Override
        public int compareTo(@NotNull AzureStorageBrowser.IconAndName o) {
            return this.name.compareTo(o.name);
        }
    }


    public static class AzureSelectableItem extends AzurePathItem {

        private final BooleanProperty selected;

        private final String itemShortName;

        public AzureSelectableItem(PathItem pathItem) {
            super(pathItem);
            this.itemShortName = Paths.get(pathItem.getName()).getFileName().toString();
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

        public String getItemShortName() {
            return itemShortName;
        }

        @Override
        public String toString() {
            return pathItem.getName() + ", selected = " + isSelected();
        }

    }
}
