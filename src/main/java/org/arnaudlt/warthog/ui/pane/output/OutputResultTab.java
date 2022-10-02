package org.arnaudlt.warthog.ui.pane.output;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.ui.util.LabelFactory;
import org.arnaudlt.warthog.ui.util.Utils;

import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Setter
@Getter
@EqualsAndHashCode
@Slf4j
public class OutputResultTab extends Tab {


    private TableView<Map<String,String>> tableView;

    private PreparedDataset preparedDataset;

    private boolean pin;


    public OutputResultTab() {

        super();
    }


    public void build(String name) {

        final int hBoxSpace = 7;

        final Label pinLabel = LabelFactory.buildSegoeLabel("\uE718");
        final Label unpinLabel = LabelFactory.buildSegoeLabel("\uE77A");

        final Label label = new Label(name);
        HBox hBoxContainer = new HBox(hBoxSpace, unpinLabel, label);
        hBoxContainer.setAlignment(Pos.BASELINE_LEFT);
        this.setGraphic(hBoxContainer);
        final TextField textField = new TextField();

        label.setOnMouseClicked(evt -> {

            textField.setText(label.getText());
            HBox hBox = new HBox(hBoxSpace, pin ? pinLabel : unpinLabel, textField);
            hBox.setAlignment(Pos.BASELINE_LEFT);
            setGraphic(hBox);
            textField.selectAll();
            textField.requestFocus();
        });

        textField.setOnAction(evt -> {

            label.setText(textField.getText());
        });

        textField.setOnKeyPressed(evt -> {

            if (KeyCode.ENTER.equals(evt.getCode())) {
                label.setText(textField.getText());
                HBox hBox = new HBox(hBoxSpace, pin ? pinLabel : unpinLabel, label);
                hBox.setAlignment(Pos.BASELINE_LEFT);
                setGraphic(hBox);
            }
        });

        textField.focusedProperty().addListener((obs, oldValue, newValue) -> {

            if (Boolean.FALSE.equals(newValue)) {
                label.setText(textField.getText());
                HBox hBox = new HBox(hBoxSpace, pin ? pinLabel : unpinLabel, label);
                hBox.setAlignment(Pos.BASELINE_LEFT);
                setGraphic(hBox);
            }
        });

        pinLabel.setOnMouseClicked(evt -> {

            if (pin) {
                HBox hBox = new HBox(hBoxSpace, unpinLabel, label);
                hBox.setAlignment(Pos.BASELINE_LEFT);
                setGraphic(hBox);
                pin = false;
            }
        });

        unpinLabel.setOnMouseClicked(evt -> {

            if (!pin) {
                HBox hBox = new HBox(hBoxSpace, pinLabel, label);
                hBox.setAlignment(Pos.BASELINE_LEFT);
                setGraphic(hBox);
                pin = true;
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
