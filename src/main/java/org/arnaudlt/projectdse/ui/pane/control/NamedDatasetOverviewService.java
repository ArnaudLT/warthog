package org.arnaudlt.projectdse.ui.pane.control;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.arnaudlt.projectdse.model.dataset.NamedDataset;

import java.util.List;

@Slf4j
public class NamedDatasetOverviewService extends Service<List<Row>> {


    private final NamedDataset namedDataset;


    public NamedDatasetOverviewService(NamedDataset namedDataset) {
        this.namedDataset = namedDataset;
    }


    @Override
    protected Task<List<Row>> createTask() {

        return new Task<>() {
            @Override
            protected List<Row> call() {

                log.info("Start generating an overview for {}", namedDataset.getName());
                return namedDataset.generateRowOverview();
            }
        };
    }
}
