package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ImportDirectorySettings;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class NamedDatasetImportFromLocalService extends AbstractMonitoredService<NamedDataset> {


    private final NamedDatasetManager namedDatasetManager;

    private final ImportDirectorySettings importDirectorySettings;


    public NamedDatasetImportFromLocalService(PoolService poolService, NamedDatasetManager namedDatasetManager,
                                              ImportDirectorySettings importDirectorySettings) {

        super(poolService);
        this.namedDatasetManager = namedDatasetManager;
        this.importDirectorySettings = importDirectorySettings;
    }


    @Override
    protected Task<NamedDataset> createTask() {

        return new Task<>() {
            @Override
            protected NamedDataset call() {

                log.info("Start importing a named dataset from {}", importDirectorySettings.getFilePath());
                updateMessage("Importing " + importDirectorySettings.getName());
                updateProgress(-1,1);
                NamedDataset namedDataset = namedDatasetManager.createNamedDataset(importDirectorySettings);
                namedDatasetManager.registerNamedDataset(namedDataset);
                updateProgress(1, 1);
                return namedDataset;
            }
        };
    }
}
