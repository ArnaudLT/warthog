package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class SqlExportToDatabaseService extends AbstractMonitoredService<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final Connection databaseConnection;

    private final ExportDatabaseSettings exportDatabaseSettings;


    public SqlExportToDatabaseService(PoolService poolService, NamedDatasetManager namedDatasetManager, String sqlQuery,
                                      Connection databaseConnection, ExportDatabaseSettings exportDatabaseSettings) {

        super(poolService);
        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.databaseConnection = databaseConnection;
        this.exportDatabaseSettings = exportDatabaseSettings;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a DB export for {}", sqlQuery);
                updateMessage("Exporting to " + exportDatabaseSettings.getTableName() + " table");
                updateProgress(-1,1);
                namedDatasetManager.exportToDatabase(sqlQuery, databaseConnection, exportDatabaseSettings);
                updateProgress(1, 1);
                return null;
            }
        };
    }
}
