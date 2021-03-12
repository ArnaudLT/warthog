package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;

@Slf4j
public class SqlExportToFileService extends Service<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final ExportFileSettings exportFileSettings;


    public SqlExportToFileService(NamedDatasetManager namedDatasetManager, String sqlQuery, ExportFileSettings exportFileSettings) {

        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.exportFileSettings = exportFileSettings;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating an export for {}", sqlQuery);
                namedDatasetManager.export(sqlQuery, exportFileSettings);
                return null;
            }
        };
    }
}
