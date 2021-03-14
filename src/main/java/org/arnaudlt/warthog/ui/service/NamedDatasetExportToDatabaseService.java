package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class NamedDatasetExportToDatabaseService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final ExportDatabaseSettings exportDatabaseSettings;


    public NamedDatasetExportToDatabaseService(NamedDatasetManager namedDatasetManager, NamedDataset namedDataset, ExportDatabaseSettings exportDatabaseSettings) {
        this.namedDatasetManager = namedDatasetManager;
        this.namedDataset = namedDataset;
        this.exportDatabaseSettings = exportDatabaseSettings;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a CSV export for {}", namedDataset.getName());
                namedDatasetManager.exportToDatabase(namedDataset, exportDatabaseSettings);
                return null;
            }
        };
    }
}
