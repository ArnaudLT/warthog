package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class SqlExportToDatabaseService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final Connection databaseConnection;

    private final String table;

    private final String saveMode;


    public SqlExportToDatabaseService(NamedDatasetManager namedDatasetManager, String sqlQuery,
                                      Connection databaseConnection, String table, String saveMode) {

        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.databaseConnection = databaseConnection;
        this.table = table;
        this.saveMode = saveMode;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a DB export for {}", sqlQuery);
                namedDatasetManager.exportToDatabase(sqlQuery, databaseConnection, table, saveMode);
                return null;
            }
        };
    }
}
