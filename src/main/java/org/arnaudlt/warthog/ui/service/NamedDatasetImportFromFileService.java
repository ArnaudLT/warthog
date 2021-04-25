package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.util.PoolService;

import java.io.File;

@Slf4j
public class NamedDatasetImportFromFileService extends AbstractMonitoredService<NamedDataset> {


    private final NamedDatasetManager namedDatasetManager;

    private final File file;


    public NamedDatasetImportFromFileService(PoolService poolService, NamedDatasetManager namedDatasetManager, File file) {

        super(poolService);
        this.namedDatasetManager = namedDatasetManager;
        this.file = file;
    }


    @Override
    protected Task<NamedDataset> createTask() {

        return new Task<>() {
            @Override
            protected NamedDataset call() throws InterruptedException {

                log.info("Start importing a named dataset from {}", file.getAbsolutePath());
                updateMessage("Importing " + file.getName());
                updateProgress(-1,1);
                NamedDataset namedDataset = namedDatasetManager.createNamedDataset(file);
                namedDatasetManager.registerNamedDataset(namedDataset);
                updateProgress(1, 1);
                return namedDataset;
            }
        };
    }
}
