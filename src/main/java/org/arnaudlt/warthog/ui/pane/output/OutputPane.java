package org.arnaudlt.warthog.ui.pane.output;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.MDL2IconFont;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class OutputPane {


    private final Stage stage;

    private TableView<Row> tableView;


    public OutputPane(Stage stage) {
        this.stage = stage;
    }


    public Node buildOutputPane() {

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

        
        // number of lines in the output. => Dataset<Row> or NamedDataset
        Button clearButton = new Button("", new MDL2IconFont("\uE74D"));
        clearButton.setTooltip(new Tooltip("Clear overview"));
        clearButton.setOnAction(event -> clear());

        Button copyButton = new Button("", new MDL2IconFont("\uE8C8"));
        copyButton.setTooltip(new Tooltip("Copy all to clipboard"));
        copyButton.setOnAction(event -> copyAllToClipboard());

        VBox buttonBar = new VBox(clearButton, copyButton);

        HBox hBox = new HBox(buttonBar, this.tableView);
        this.tableView.prefWidthProperty().bind(hBox.widthProperty());

        return hBox;
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

        this.clearOverview();
    }


    protected void clearOverview() {

        this.tableView.getItems().clear();
        this.tableView.getColumns().clear();
    }


    public void fill(List<Row> rows) {

        fillOverview(rows);
    }


    protected void fillOverview(List<Row> rows) {

        clearOverview();
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
