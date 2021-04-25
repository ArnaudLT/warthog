package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class SqlOverviewService extends AbstractMonitoredService<PreparedDataset> {


    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final Integer overviewRows;


    public SqlOverviewService(PoolService poolService, NamedDatasetManager namedDatasetManager, String sqlQuery, Integer overviewRows) {

        super(poolService);
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
                updateMessage("Generating overview");
                updateProgress(-1,1);
                Dataset<Row> row = namedDatasetManager.prepareDataset(sqlQuery);
                PreparedDataset preparedDataset = new PreparedDataset(row, row.takeAsList(overviewRows));
                updateProgress(1, 1);
                return preparedDataset;
            }
        };
    }
}
