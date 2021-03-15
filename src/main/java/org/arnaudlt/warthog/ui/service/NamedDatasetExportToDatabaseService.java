package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class NamedDatasetExportToDatabaseService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final Connection databaseConnection;

    private final String table;

    private final String saveMode;


    public NamedDatasetExportToDatabaseService(NamedDatasetManager namedDatasetManager, NamedDataset namedDataset,
                                               Connection databaseConnection, String table, String saveMode) {
        this.namedDatasetManager = namedDatasetManager;
        this.namedDataset = namedDataset;
        this.databaseConnection = databaseConnection;
        this.table = table;
        this.saveMode = saveMode;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a CSV export for {}", namedDataset.getName());
                namedDatasetManager.exportToDatabase(namedDataset, databaseConnection, table, saveMode);
                return null;
            }
        };
    }
}
