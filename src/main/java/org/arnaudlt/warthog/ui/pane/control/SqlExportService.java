package org.arnaudlt.warthog.ui.pane.control;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class SqlExportService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final String filePath;


    public SqlExportService(NamedDatasetManager namedDatasetManager, String sqlQuery, String filePath) {

        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.filePath = filePath;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating an export for {}", sqlQuery);
                namedDatasetManager.export(sqlQuery, filePath);
                return null;
            }
        };
    }
}
