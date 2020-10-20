package org.arnaudlt.warthog.ui.pane.control;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedDatasetExportService extends Service<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedDatasetExportService.class);

    private final NamedDataset namedDataset;

    private final String filePath;


    public NamedDatasetExportService(NamedDataset namedDataset, String filePath) {
        this.namedDataset = namedDataset;
        this.filePath = filePath;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() {

                LOGGER.info("Start generating an export for {}", namedDataset.getName());
                namedDataset.export(filePath);
                return null;
            }
        };
    }
}
