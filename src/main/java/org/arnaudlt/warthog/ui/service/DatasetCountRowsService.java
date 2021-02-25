package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDataset;

@Slf4j
public class DatasetCountRowsService extends Service<Long> {


    private final Dataset<Row> dataset;


    public DatasetCountRowsService(Dataset<Row> dataset) {

        this.dataset = dataset;
    }


    @Override
    protected Task<Long> createTask() {

        return new Task<>() {
            @Override
            protected Long call() {

                log.info("Start counting rows");
                return dataset.count();
            }
        };
    }
}
