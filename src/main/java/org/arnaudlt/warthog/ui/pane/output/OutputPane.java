package org.arnaudlt.warthog.ui.pane.output;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.MDL2IconFont;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.arnaudlt.warthog.model.util.PoolService;
import org.arnaudlt.warthog.ui.service.DatasetCountRowsService;
import org.arnaudlt.warthog.ui.util.AlertFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class OutputPane {


    private Stage owner;

    private final PoolService poolService;

    private final GlobalSettings globalSettings;

    private TableView<Row> tableView;

    private PreparedDataset preparedDataset;


    @Autowired
    public OutputPane(PoolService poolService, GlobalSettings globalSettings) {

        this.poolService = poolService;
        this.globalSettings = globalSettings;
    }


    public Node buildOutputPane(Stage owner) {

        this.owner = owner;

        this.tableView = new TableView<>();
        this.tableView.setPlaceholder(new Label("No data to display"));
        this.tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.tableView.getSelectionModel().setCellSelectionEnabled(true);

        final KeyCombination keyCodeCopy = KeyCodeCombination.valueOf("CTRL+C");
        final KeyCombination keyCodeCopyLineWithHeader = KeyCodeCombination.valueOf("CTRL+SHIFT+C");
        final KeyCombination keyCodeCopyWithHeader = KeyCodeCombination.valueOf("CTRL+ALT+C");
        this.tableView.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event)) {

                copySelectionToClipboard(false, false);
                event.consume();
            } else if (keyCodeCopyLineWithHeader.match(event)) {

                copySelectionToClipboard(true, true);
                event.consume();
            } else if (keyCodeCopyWithHeader.match(event)) {

                copySelectionToClipboard(true, false);
                event.consume();
            }
        });

        Button clearButton = new Button("", new MDL2IconFont("\uE74D"));
        clearButton.setTooltip(new Tooltip("Clear overview"));
        clearButton.setOnAction(event -> clear());

        Button copyButton = new Button("", new MDL2IconFont("\uE8C8"));
        copyButton.setTooltip(new Tooltip("Copy all to clipboard"));
        copyButton.setOnAction(event -> copyAllToClipboard());

        Button countRowsButton = new Button("", new MDL2IconFont("\uF272"));
        countRowsButton.setTooltip(new Tooltip("Count rows"));
        countRowsButton.setOnAction(getDatasetCountRowsEventHandler());

        Button showSchemaButton = new Button("", new MDL2IconFont("\ue822"));
        showSchemaButton.setTooltip(new Tooltip("Show schema"));
        showSchemaButton.setOnAction(getDatasetShowSchemaEventHandler());

        VBox buttonBar = new VBox(clearButton, copyButton, countRowsButton, showSchemaButton);

        HBox hBox = new HBox(buttonBar, this.tableView);
        this.tableView.prefWidthProperty().bind(hBox.widthProperty());

        return hBox;
    }


    private EventHandler<ActionEvent> getDatasetCountRowsEventHandler() {

        return event -> {

            if (this.preparedDataset == null) return;

            DatasetCountRowsService datasetCountRowsService = new DatasetCountRowsService(poolService, this.preparedDataset.getDataset());
            datasetCountRowsService.setOnSucceeded(success ->
                AlertFactory.showInformationAlert(owner, "Number of rows : " + String.format(Locale.US,"%,d", datasetCountRowsService.getValue())));
            datasetCountRowsService.setOnFailed(fail -> AlertFactory.showFailureAlert(owner, fail, "Failed to count rows"));
            datasetCountRowsService.start();
        };
    }


    private EventHandler<ActionEvent> getDatasetShowSchemaEventHandler() {

        return event -> {

            if (this.preparedDataset == null) return;
            AlertFactory.showInformationAlert(owner, "Schema : ", this.preparedDataset.getDataset().schema().prettyJson());
        };
    }


    private void copyAllToClipboard() {

        tableView.getSelectionModel().selectAll();
        copySelectionToClipboard(true, true);
    }


    private void copySelectionToClipboard(boolean withHeader, boolean allColumns) {

        TreeSet<Integer> selectedRows = tableView.getSelectionModel().getSelectedCells()
                .stream()
                .map(TablePositionBase::getRow)
                .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

        TreeSet<Integer> selectedColumns;

        if (allColumns) {

            selectedColumns = IntStream.range(0, tableView.getColumns().size())
                    .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

        } else {

            selectedColumns = tableView.getSelectionModel().getSelectedCells()
                    .stream()
                    .map(TablePosition::getColumn)
                    .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);
        }

        String content = "";

        if (withHeader) {

            content += selectedColumns.stream()
                    .map(column -> tableView.getColumns().get(column).getText())
                    .map(data -> data == null ? "" : data)
                    .collect(Collectors.joining(";"));
            content += "\n";
        }

        content += selectedRows.stream()
                .map(rowIndex -> selectedColumns.stream()
                        .map(column -> tableView.getColumns().get(column).getCellData(rowIndex))
                        .map(cellData -> cellData == null ? "" : cellData.toString())
                        .collect(Collectors.joining(";"))
                )
                .collect(Collectors.joining("\n"));

        Utils.copyStringToClipboard(content);
    }


    public void clear() {

        this.preparedDataset = null;
        this.clearTableView();
    }


    protected void clearTableView() {

        this.tableView.getItems().clear();
        this.tableView.getColumns().clear();
    }


    public void fill(PreparedDataset preparedDataset) {

        this.preparedDataset = preparedDataset;
        List<Row> rows = preparedDataset.getOverview();

        clearTableView();

        boolean truncateAfterEnabled = globalSettings.getOverviewTruncateAfter() != 0;

        for (StructField field : preparedDataset.getDataset().schema().fields()) {

            TableColumn<Row, Object> col = new TableColumn<>(field.name());
            col.setCellValueFactory(param -> {

                String fieldValue = "";

                Object rawValue = param.getValue().getAs(field.name());
                if (rawValue != null) {
                    fieldValue = rawValue.toString();
                }
                if (truncateAfterEnabled && fieldValue.length() > globalSettings.getOverviewTruncateAfter()) {

                    fieldValue = fieldValue.substring(0,globalSettings.getOverviewTruncateAfter()).concat("...");
                }
                return new SimpleObjectProperty<>(fieldValue);
            });
            this.tableView.getColumns().add(col);
        }
        this.tableView.getItems().addAll(rows);
    }

}
