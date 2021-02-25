package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

import java.io.File;

@Slf4j
public class NamedDatasetImportService extends Service<NamedDataset> {


    private final NamedDatasetManager namedDatasetManager;

    private final File file;


    public NamedDatasetImportService(NamedDatasetManager namedDatasetManager, File file) {

        this.namedDatasetManager = namedDatasetManager;
        this.file = file;
    }


    @Override
    protected Task<NamedDataset> createTask() {

        return new Task<>() {
            @Override
            protected NamedDataset call() {

                log.info("Start importing a named dataset from {}", file.getAbsolutePath());
                NamedDataset namedDataset = namedDatasetManager.createNamedDataset(file);
                namedDatasetManager.registerNamedDataset(namedDataset);
                return namedDataset;
            }
        };
    }
}
