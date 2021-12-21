package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class NamedDatasetRenameViewService extends AbstractMonitoredService<Boolean> {

    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final String newName;


    public NamedDatasetRenameViewService(PoolService poolService, NamedDatasetManager namedDatasetManager,
                                         NamedDataset namedDataset, String newName) {

        super(poolService);
        this.namedDatasetManager = namedDatasetManager;
        this.namedDataset = namedDataset;
        this.newName = newName;
    }


    @Override
    protected Task<Boolean> createTask() {

        return new Task<>() {
            @Override
            protected Boolean call() throws AnalysisException {

                log.info("Start renaming the view {}", namedDataset.getLocalTemporaryViewName());
                updateMessage("Renaming dataset " + namedDataset.getLocalTemporaryViewName() + " to " + newName);
                updateProgress(-1,1);
                namedDatasetManager.tryRenameTempView(namedDataset, newName);
                updateProgress(1, 1);
                return true;
            }
        };
    }
}
