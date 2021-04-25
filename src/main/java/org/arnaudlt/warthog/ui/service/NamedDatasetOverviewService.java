package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class NamedDatasetOverviewService extends AbstractMonitoredService<PreparedDataset> {


    private final NamedDataset namedDataset;

    private final Integer overviewRows;


    public NamedDatasetOverviewService(PoolService poolService, NamedDataset namedDataset, Integer overviewRows) {

        super(poolService);
        this.namedDataset = namedDataset;
        this.overviewRows = overviewRows;
    }


    @Override
    protected Task<PreparedDataset> createTask() {

        return new Task<>() {
            @Override
            protected PreparedDataset call() {

                log.info("Start generating an overview for {}", namedDataset.getName());
                updateMessage("Generating overview");
                updateProgress(-1,1);
                Dataset<Row> output = namedDataset.applyTransformation();
                PreparedDataset preparedDataset = new PreparedDataset(output, output.takeAsList(overviewRows));
                updateProgress(1, 1);
                return preparedDataset;
            }
        };
    }
}
