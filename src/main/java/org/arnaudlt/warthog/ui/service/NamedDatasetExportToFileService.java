package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class NamedDatasetExportToFileService extends AbstractMonitoredService<Void> {


    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final ExportFileSettings exportFileSettings;


    public NamedDatasetExportToFileService(PoolService poolService, NamedDatasetManager namedDatasetManager, NamedDataset namedDataset,
                                           ExportFileSettings exportFileSettings) {

        super(poolService);
        this.namedDatasetManager = namedDatasetManager;
        this.namedDataset = namedDataset;
        this.exportFileSettings = exportFileSettings;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating an export for {}", namedDataset.getName());
                updateMessage("Exporting to " + exportFileSettings.getFilePath());
                updateProgress(-1,1);
                namedDatasetManager.export(namedDataset, exportFileSettings);
                updateProgress(1, 1);
                return null;
            }
        };
    }
}
