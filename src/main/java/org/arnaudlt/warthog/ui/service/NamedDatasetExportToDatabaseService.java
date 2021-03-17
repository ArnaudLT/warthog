package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;

@Slf4j
public class NamedDatasetExportToDatabaseService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final Connection databaseConnection;

    private final ExportDatabaseSettings exportDatabaseSettings;


    public NamedDatasetExportToDatabaseService(NamedDatasetManager namedDatasetManager, NamedDataset namedDataset,
                                               Connection databaseConnection, ExportDatabaseSettings exportDatabaseSettings) {
        this.namedDatasetManager = namedDatasetManager;
        this.namedDataset = namedDataset;
        this.databaseConnection = databaseConnection;
        this.exportDatabaseSettings = exportDatabaseSettings;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a CSV export for {}", namedDataset.getName());
                namedDatasetManager.exportToDatabase(namedDataset, databaseConnection, exportDatabaseSettings);
                return null;
            }
        };
    }
}
