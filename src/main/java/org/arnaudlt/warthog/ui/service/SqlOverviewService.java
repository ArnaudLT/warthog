package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;

@Slf4j
public class SqlOverviewService extends Service<PreparedDataset> {


    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final Integer overviewRows;


    public SqlOverviewService(NamedDatasetManager namedDatasetManager, String sqlQuery, Integer overviewRows) {
        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.overviewRows = overviewRows;
    }


    @Override
    protected Task<PreparedDataset> createTask() {

        return new Task<>() {
            @Override
            protected PreparedDataset call() {

                log.info("Start generating an overview for the sql query : \"{}\"", sqlQuery.replace("\n", " "));
                Dataset<Row> row = namedDatasetManager.prepareDataset(sqlQuery);
                return new PreparedDataset(row, row.takeAsList(overviewRows));
            }
        };
    }
}
