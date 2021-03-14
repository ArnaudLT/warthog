package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class SqlExportToDatabaseService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final ExportDatabaseSettings exportDatabaseSettings;


    public SqlExportToDatabaseService(NamedDatasetManager namedDatasetManager, String sqlQuery, ExportDatabaseSettings exportDatabaseSettings) {

        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.exportDatabaseSettings = exportDatabaseSettings;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a DB export for {}", sqlQuery);
                namedDatasetManager.exportToDatabase(sqlQuery, exportDatabaseSettings);
                return null;
            }
        };
    }
}
