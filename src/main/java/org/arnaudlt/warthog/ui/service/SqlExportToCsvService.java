package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class SqlExportToCsvService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final String filePath;


    public SqlExportToCsvService(NamedDatasetManager namedDatasetManager, String sqlQuery, String filePath) {

        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.filePath = filePath;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a CSV export for {}", sqlQuery);
                namedDatasetManager.exportToCsv(sqlQuery, filePath);
                return null;
            }
        };
    }
}
