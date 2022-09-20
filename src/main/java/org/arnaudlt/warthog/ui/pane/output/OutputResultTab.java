package org.arnaudlt.warthog.ui.pane.output;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import lombok.Getter;
import lombok.Setter;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.ui.util.Utils;

import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Setter
@Getter
public class OutputResultTab extends Tab {


    private TableView<Map<String,String>> tableView;

    private PreparedDataset preparedDataset;

    private boolean pin;


    public OutputResultTab() {

        super();
    }


    public void build(String name) {

        // TODO duplicated code !! (SqlTab)
        final Label label = new Label(name);
        this.setGraphic(label);
        final TextField textField = new TextField();

        label.setOnMouseClicked(evt -> {

            textField.setText(label.getText());
            this.setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        });

        textField.setOnAction(evt -> {

            label.setText(textField.getText());
        });

        textField.setOnKeyPressed(evt -> {

            if (KeyCode.ENTER.equals(evt.getCode())) {
                label.setText(textField.getText());
                this.setGraphic(label);
            }
        });

        textField.focusedProperty().addListener((obs, oldValue, newValue) -> {

            if (Boolean.FALSE.equals(newValue)) {
                label.setText(textField.getText());
                this.setGraphic(label);
            }
        });

        this.tableView = new TableView<>();
        this.tableView.setPlaceholder(new Label("No data to display"));
        this.tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.tableView.getSelectionModel().setCellSelectionEnabled(true);

        final KeyCombination keyCodeCopy = KeyCombination.valueOf("CTRL+C");
        final KeyCombination keyCodeCopyLineWithHeader = KeyCombination.valueOf("CTRL+SHIFT+C");
        final KeyCombination keyCodeCopyWithHeader = KeyCombination.valueOf("CTRL+ALT+C");
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
        this.setContent(tableView);
    }


    protected void copyAllToClipboard() {

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


    protected void clear() {

        this.setPreparedDataset(null);
        this.getTableView().getItems().clear();
        this.getTableView().getColumns().clear();
    }
}
