package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;

@Slf4j
public class NamedDatasetExportToFileService extends Service<Void> {


    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final ExportFileSettings exportFileSettings;


    public NamedDatasetExportToFileService(NamedDatasetManager namedDatasetManager, NamedDataset namedDataset,
                                           ExportFileSettings exportFileSettings) {
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
                namedDatasetManager.export(namedDataset, exportFileSettings);
                return null;
            }
        };
    }
}
