package org.arnaudlt.projectdse.ui.pane.output;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.MDL2IconFont;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OutputPane {


    private final Stage stage;

    private TextArea outputText;

    private TableView<Row> tableView;


    public OutputPane(Stage stage) {
        this.stage = stage;
    }


    public Node buildOutputPane() {

        this.outputText = new TextArea();
        Tab outputTab = new Tab("Raw Overview", outputText);

        this.tableView = new TableView<>();
        Tab secret = new Tab("Table Overview", tableView);

        TabPane tabPane = new TabPane(outputTab, secret);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setMaxWidth(Double.MAX_VALUE);


        Button settingButton = new Button("", new MDL2IconFont("\uE7FC"));
        // number of lines in the output. => Dataset<Row> or NamedDataset
        Button clearButton = new Button("", new MDL2IconFont("\uE74D"));
        clearButton.setOnAction(event -> clear());

        VBox buttonBar = new VBox(settingButton, clearButton);

        HBox hBox = new HBox(buttonBar, tabPane);
        tabPane.prefWidthProperty().bind(hBox.widthProperty());

        return hBox;
    }


    public void clear() {

        this.clearOutput();
        this.clearSecret();
    }


    protected void clearOutput() {

        this.outputText.setText("");
    }


    protected void clearSecret() {

        this.tableView.getItems().clear();
        this.tableView.getColumns().clear();
    }


    public void fill(List<Row> rows) {

        fillOutput(rows);
        fillSecret(rows);
    }


    protected void fillOutput(List<Row> rows) {

        clearOutput();
        if (rows == null || rows.isEmpty()) {

            this.outputText.setText("No result !");
        } else {

            String header = Arrays.stream(rows.get(0).schema().fields())
                    .map(StructField::name)
                    .collect(Collectors.joining(";"));

            String toReturn = rows.stream()
                    .map(row -> row.mkString(";"))
                    .collect(Collectors.joining("\n"));

            this.outputText.setText(header + '\n' + toReturn);
        }
    }


    protected void fillSecret(List<Row> rows) {

        clearSecret();
        if (rows == null || rows.isEmpty()) return;

        for (StructField field : rows.get(0).schema().fields()) {

            TableColumn<Row, Object> col = new TableColumn<>(field.name());
            col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAs(field.name())));
            this.tableView.getColumns().add(col);
        }
        this.tableView.getItems().addAll(rows);
    }

}
