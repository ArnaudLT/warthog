package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class SqlExportToDatabaseService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;


    public SqlExportToDatabaseService(NamedDatasetManager namedDatasetManager, String sqlQuery) {

        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a DB export for {}", sqlQuery);
                namedDatasetManager.exportToDatabase(sqlQuery);
                return null;
            }
        };
    }
}
