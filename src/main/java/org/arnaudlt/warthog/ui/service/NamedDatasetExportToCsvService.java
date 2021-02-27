package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;

@Slf4j
public class NamedDatasetExportToCsvService extends Service<Void> {


    private final NamedDataset namedDataset;

    private final String filePath;


    public NamedDatasetExportToCsvService(NamedDataset namedDataset, String filePath) {
        this.namedDataset = namedDataset;
        this.filePath = filePath;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                log.info("Start generating a CSV export for {}", namedDataset.getName());
                namedDataset.export(filePath);
                return null;
            }
        };
    }
}
