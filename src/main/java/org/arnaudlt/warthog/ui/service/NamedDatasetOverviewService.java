package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDataset;

@Slf4j
public class NamedDatasetOverviewService extends Service<Dataset<Row>> {


    private final NamedDataset namedDataset;


    public NamedDatasetOverviewService(NamedDataset namedDataset) {
        this.namedDataset = namedDataset;
    }


    @Override
    protected Task<Dataset<Row>> createTask() {

        return new Task<>() {
            @Override
            protected Dataset<Row> call() {

                log.info("Start generating an overview for {}", namedDataset.getName());
                return namedDataset.applyTransformation();
            }
        };
    }
}
