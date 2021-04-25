package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class NamedDatasetExportToDatabaseService extends AbstractMonitoredService<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final Connection databaseConnection;

    private final ExportDatabaseSettings exportDatabaseSettings;


    public NamedDatasetExportToDatabaseService(PoolService poolService, NamedDatasetManager namedDatasetManager, NamedDataset namedDataset,
                                               Connection databaseConnection, ExportDatabaseSettings exportDatabaseSettings) {

        super(poolService);
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

                log.info("Starting database export for {}", namedDataset.getName());
                updateMessage("Exporting to table " + exportDatabaseSettings.getTableName());
                updateProgress(-1,1);
                namedDatasetManager.exportToDatabase(namedDataset, databaseConnection, exportDatabaseSettings);
                updateProgress(1, 1);
                return null;
            }
        };
    }
}
