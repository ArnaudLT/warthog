package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;

@Slf4j
public class SqlExportToDatabaseService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final Connection databaseConnection;

    private final ExportDatabaseSettings exportDatabaseSettings;


    public SqlExportToDatabaseService(NamedDatasetManager namedDatasetManager, String sqlQuery,
                                      Connection databaseConnection, ExportDatabaseSettings exportDatabaseSettings) {

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
                namedDatasetManager.exportToDatabase(sqlQuery, databaseConnection, exportDatabaseSettings);
                return null;
            }
        };
    }
}
