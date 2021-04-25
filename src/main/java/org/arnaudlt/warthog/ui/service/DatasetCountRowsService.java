package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class DatasetCountRowsService extends AbstractMonitoredService<Long> {


    private final Dataset<Row> dataset;


    public DatasetCountRowsService(PoolService poolService, Dataset<Row> dataset) {

        super(poolService);
        this.dataset = dataset;
    }


    @Override
    protected Task<Long> createTask() {

        return new Task<>() {
            @Override
            protected Long call() {

                log.info("Start counting rows");
                updateMessage("Counting rows");
                updateProgress(-1,1);
                Long count = dataset.count();
                updateProgress(1, 1);
                return count;
            }
        };
    }
}
