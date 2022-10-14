package org.arnaudlt.warthog.ui.pane.control;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCombination;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.history.SqlHistory;
import org.arnaudlt.warthog.model.history.SqlHistoryCollection;
import org.arnaudlt.warthog.ui.util.StageFactory;
import org.arnaudlt.warthog.ui.util.Utils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;

@Slf4j
@Component
public class SqlHistoryDialog {


    private Stage dialog;

    private final SqlHistoryCollection sqlHistoryCollection;

    private TableView<SqlHistory> tableView;


    @Autowired
    public SqlHistoryDialog(SqlHistoryCollection sqlHistoryCollection) {

        this.sqlHistoryCollection = sqlHistoryCollection;
    }


    public void buildBackgroundTasksDialog(Stage owner) {

        this.dialog = StageFactory.buildModalStage(owner, "SQL history", Modality.NONE, true);

        this.tableView = new TableView<>();
        this.tableView.setPlaceholder(new Label("No data to display"));
        this.tableView.setItems(sqlHistoryCollection.getSqlQueries());

        final KeyCombination keyCodeCopy = KeyCombination.valueOf("CTRL+C");
        this.tableView.setOnKeyPressed(event -> {

            if (keyCodeCopy.match(event)) {

                copySqlQueryToClipboard();
                event.consume();
            }
        });

        TableColumn<SqlHistory, ExecutionDate> timeCol = new TableColumn<>("Time");

        timeCol.setCellValueFactory(param ->
            new SimpleObjectProperty<>(new ExecutionDate(param.getValue().getTimestamp())));

        TableColumn<SqlHistory, String> sqlCol = new TableColumn<>("SQL query");
        sqlCol.setCellValueFactory(param -> {

            String sqlQuery = param.getValue().getSqlQuery();
            return new SimpleObjectProperty<>(sqlQuery);
        });

        TableColumn<SqlHistory, Double> durationCol = new TableColumn<>("Duration (s)");
        durationCol.setCellValueFactory(param -> {

            double durationMs = param.getValue().getDuration() / 1_000d;
            return new SimpleObjectProperty<>(durationMs);
        });

        timeCol.setPrefWidth(150);
        durationCol.setPrefWidth(100);

        sqlCol.prefWidthProperty().bind(
                tableView.widthProperty()
                        .subtract(timeCol.widthProperty())
                        .subtract(durationCol.widthProperty())
                        .subtract(2)
        );

        tableView.getColumns().setAll(timeCol, sqlCol, durationCol);

        Scene dialogScene = StageFactory.buildScene(tableView, 720, 400);
        tableView.prefWidthProperty().bind(dialogScene.widthProperty()); // TODO useless ?
        dialog.setScene(dialogScene);
    }


    private void copySqlQueryToClipboard() {

        SqlHistory selectedSqlHistory = tableView.getSelectionModel().getSelectedItem();
        if (selectedSqlHistory != null) {
            String sqlQuery = selectedSqlHistory.getSqlQuery();
            Utils.copyStringToClipboard(sqlQuery);
        }
    }


    public void showTasksManagerDialog() {

        this.dialog.show();
    }


    private record ExecutionDate(long timestamp) implements Comparable<ExecutionDate> {

        @Override
        public String toString() {
            return new SimpleDateFormat("dd-M-yyyy hh:mm:ss").format(Date.from(Instant.ofEpochMilli(timestamp)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExecutionDate that = (ExecutionDate) o;
            return timestamp == that.timestamp;
        }

        @Override
        public int hashCode() {
            return (int) (timestamp ^ (timestamp >>> 32));
        }

        @Override
        public int compareTo(@NotNull SqlHistoryDialog.ExecutionDate o) {
            return Long.compare(timestamp, o.timestamp);
        }
    }

}
