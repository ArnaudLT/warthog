package org.arnaudlt.projectdse.ui.pane.output;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.arnaudlt.projectdse.model.dataset.NamedColumn;
import org.arnaudlt.projectdse.model.dataset.NamedDataset;
import org.arnaudlt.projectdse.model.dataset.transformation.SelectNamedColumn;

import java.util.ArrayList;
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
        Tab outputTab = new Tab("Overview", outputText);

        this.tableView = new TableView<>();
        Tab secret = new Tab("Secret", tableView);

        TabPane tabPane = new TabPane(outputTab, secret);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        return tabPane;
    }


    public void fillOutput(List<Row> rows) {

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


    public void fillSecret(List<Row> rows) {

        if (rows == null || rows.isEmpty()) return;
        this.tableView.getItems().clear();
        this.tableView.getColumns().clear();
        for (StructField field : rows.get(0).schema().fields()) {

            TableColumn<Row, Object> col = new TableColumn<>(field.name());
            col.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAs(field.name())));
            this.tableView.getColumns().add(col);
        }
        this.tableView.getItems().addAll(rows);
    }

}
