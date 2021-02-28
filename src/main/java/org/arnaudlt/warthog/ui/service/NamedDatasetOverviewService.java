package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;

@Slf4j
public class NamedDatasetOverviewService extends Service<PreparedDataset> {


    private final NamedDataset namedDataset;


    public NamedDatasetOverviewService(NamedDataset namedDataset) {
        this.namedDataset = namedDataset;
    }


    @Override
    protected Task<PreparedDataset> createTask() {

        return new Task<>() {
            @Override
            protected PreparedDataset call() {

                log.info("Start generating an overview for {}", namedDataset.getName());
                Dataset<Row> output = namedDataset.applyTransformation();
                return new PreparedDataset(output, output.takeAsList(50));
            }
        };
    }
}
