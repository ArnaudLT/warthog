package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class SqlOverviewService extends Service<Dataset<Row>> {


    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;


    public SqlOverviewService(NamedDatasetManager namedDatasetManager, String sqlQuery) {
        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
    }


    @Override
    protected Task<Dataset<Row>> createTask() {

        return new Task<>() {
            @Override
            protected Dataset<Row> call() {

                log.info("Start generating an overview for the sql query : {}", sqlQuery.replace("\n", " "));
                return namedDatasetManager.generateRowOverview(sqlQuery);
            }
        };
    }
}
