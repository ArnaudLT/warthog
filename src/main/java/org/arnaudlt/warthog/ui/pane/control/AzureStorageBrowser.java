package org.arnaudlt.warthog.ui.pane.control;

import com.azure.storage.file.datalake.models.PathItem;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
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
import org.arnaudlt.warthog.ui.util.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
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

    private CheckBox selectAll;


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

        selectAll = new CheckBox();
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

        TableColumn<AzureSelectableItem, LastModified> itemLastModification = new TableColumn<>("Last modified");
        itemLastModification.setPrefWidth(130);
        itemLastModification.setCellValueFactory(param -> new SimpleObjectProperty<>(new LastModified(param.getValue().getPathItem().getLastModified())));
        itemLastModification.setSortType(TableColumn.SortType.DESCENDING);
        filesView.getSortOrder().add(itemLastModification);
        filesView.getColumns().add(itemLastModification);

        TableColumn<AzureSelectableItem, ContentSize> itemContentSize = new TableColumn<>("Size");
        itemContentSize.setCellValueFactory(param -> new SimpleObjectProperty<>(new ContentSize(param.getValue().getPathItem())));
        filesView.getColumns().add(itemContentSize);

        itemName.prefWidthProperty().bind(
                filesView.widthProperty()
                        .subtract(checkBoxColumn.widthProperty())
                        .subtract(itemLastModification.widthProperty())
                        .subtract(itemContentSize.widthProperty())
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

        Button parentDirectoryButton = ButtonFactory.buildSegoeButton("\uE752", "Parent directory", 14);
        parentDirectoryButton.setOnAction(event -> {

            String azureParentDirectoryString = getAzureParentDirectory();
            azureCurrentDirectory.set(azureParentDirectoryString);
            startAzureDirectoryListingService();
        });

        topControlBar.getChildren().addAll(parentDirectoryButton, currentDirectory);

        HBox bottomControlBar = new HBox();
        Button validateButton = new Button("Validate");
        validateButton.setPrefWidth(150);
        validateButton.setOnAction(evt -> {

            selectedAzurePathItems.getItems().addAll(
                    filesView.getItems().stream()
                        .filter(p -> p.selected.getValue())
                        .toList()
            );
            dialog.close();
        });
        bottomControlBar.getChildren().addAll(validateButton);
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
            icon = LabelFactory.buildSegoeLabel("\uED42", "goldenrod");
        } else {
            icon = LabelFactory.buildSegoeLabel("\uE7C3", "black");
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
            filesView.sort();
            selectAll.setSelected(true);
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


    private static class IconAndName extends HBox implements Comparable<IconAndName> {

        private final String icon;

        private final String name;

        public IconAndName(double spacing, Label icon, Label name) {
            super(spacing, icon, name);
            this.setAlignment(Pos.BASELINE_LEFT);
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


    private static class LastModified implements Comparable<LastModified> {

        private final OffsetDateTime date;


        public LastModified(OffsetDateTime date) {
            this.date = date;
        }

        @Override
        public String toString() {
            return date.format(DateTimeFormatter.ofPattern("dd-M-yyyy hh:mm:ss"));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            LastModified that = (LastModified) o;

            return Objects.equals(date, that.date);
        }

        @Override
        public int hashCode() {
            return date != null ? date.hashCode() : 0;
        }

        @Override
        public int compareTo(@NotNull AzureStorageBrowser.LastModified o) {
            return date.compareTo(o.date);
        }
    }


    private static class ContentSize implements Comparable<ContentSize> {

        private final long size;

        public ContentSize(PathItem pathItem) {

            if (pathItem.isDirectory()) {
                size = - 1;
            } else {
                size = pathItem.getContentLength();
            }
        }

        @Override
        public String toString() {
            if (size < 0) {
                return "";
            } else {
                return Utils.format2Decimals(size / 1_000_000d) + " MB";
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ContentSize that = (ContentSize) o;

            return size == that.size;
        }

        @Override
        public int hashCode() {
            return (int) (size ^ (size >>> 32));
        }

        @Override
        public int compareTo(@NotNull AzureStorageBrowser.ContentSize o) {

            return Long.compare(size, o.size);
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
