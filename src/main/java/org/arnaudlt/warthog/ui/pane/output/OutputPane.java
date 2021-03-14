package org.arnaudlt.warthog.ui.pane.output;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.MDL2IconFont;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.warthog.PoolService;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.ui.util.AlertError;
import org.arnaudlt.warthog.ui.service.DatasetCountRowsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
public class OutputPane {


    private Stage stage;

    private final PoolService poolService;

    private TableView<Row> tableView;

    private PreparedDataset preparedDataset;


    @Autowired
    public OutputPane(PoolService poolService) {

        this.poolService = poolService;
    }


    public Node buildOutputPane(Stage stage) {

        this.stage = stage;

        this.tableView = new TableView<>();
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

        VBox buttonBar = new VBox(clearButton, copyButton, countRowsButton);

        HBox hBox = new HBox(buttonBar, this.tableView);
        this.tableView.prefWidthProperty().bind(hBox.widthProperty());

        return hBox;
    }


    private EventHandler<ActionEvent> getDatasetCountRowsEventHandler() {

        return event -> {

            if (this.preparedDataset == null) return;

            DatasetCountRowsService datasetCountRowsService = new DatasetCountRowsService(this.preparedDataset.getDataset());
            datasetCountRowsService.setOnSucceeded(success -> {
                log.info("Success count rows : {}", datasetCountRowsService.getValue());
                Alert countRowsAlert = new Alert(Alert.AlertType.INFORMATION, "", ButtonType.CLOSE);
                countRowsAlert.setHeaderText("Number of rows : " + datasetCountRowsService.getValue());
                countRowsAlert.show();
            });
            datasetCountRowsService.setOnFailed(fail -> AlertError.showFailureAlert(fail, "Failed to count rows"));
            datasetCountRowsService.setExecutor(poolService.getExecutor());
            datasetCountRowsService.start();
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

        final ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(content);
        Clipboard.getSystemClipboard().setContent(clipboardContent);
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
        fillOverview(preparedDataset.getOverview());
    }


    protected void fillOverview(List<Row> rows) {

        clearTableView();
        if (rows == null || rows.isEmpty()) {

            // TODO : if rows is null or empty we don't have the columns (model) !
            log.info("No result to display");
            return;
        }

        for (StructField field : rows.get(0).schema().fields()) {

            TableColumn<Row, Object> col = new TableColumn<>(field.name());
            col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAs(field.name())));
            this.tableView.getColumns().add(col);
        }
        this.tableView.getItems().addAll(rows);
    }

}
