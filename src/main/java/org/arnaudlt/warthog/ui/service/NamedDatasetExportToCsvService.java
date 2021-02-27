package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class NamedDatasetExportToCsvService extends Service<Void> {


    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;

    private final String filePath;


    public NamedDatasetExportToCsvService(NamedDatasetManager namedDatasetManager, NamedDataset namedDataset, String filePath) {
        this.namedDatasetManager = namedDatasetManager;
        this.namedDataset = namedDataset;
        this.filePath = filePath;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a CSV export for {}", namedDataset.getName());
                namedDatasetManager.exportToCsv(namedDataset, filePath);
                return null;
            }
        };
    }
}
