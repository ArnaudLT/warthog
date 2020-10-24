package org.arnaudlt.warthog.ui.pane.transform;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

import java.util.List;

@Slf4j
public class NamedDatasetSqlService extends Service<List<Row>> {


    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;


    public NamedDatasetSqlService(NamedDatasetManager namedDatasetManager, String sqlQuery) {
        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
    }


    @Override
    protected Task<List<Row>> createTask() {

        return new Task<>() {
            @Override
            protected List<Row> call() {

                String cleanedSqlQuery = sqlQuery.trim();
                log.info("Start generating an overview for the sql query : {}", cleanedSqlQuery);
                return namedDatasetManager.generateRowOverview(cleanedSqlQuery);
            }
        };
    }
}
