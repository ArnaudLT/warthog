package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class SqlExportToFileService extends AbstractMonitoredService<Void> {

    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final ExportFileSettings exportFileSettings;


    public SqlExportToFileService(PoolService poolService, NamedDatasetManager namedDatasetManager, String sqlQuery, ExportFileSettings exportFileSettings) {

        super(poolService);
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
                updateMessage("Exporting to " + exportFileSettings.filePath());
                updateProgress(-1,1);
                namedDatasetManager.export(sqlQuery, exportFileSettings);
                updateProgress(1, 1);
                return null;
            }
        };
    }
}
