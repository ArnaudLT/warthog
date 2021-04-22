package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

import java.io.File;
import java.io.IOException;

@Slf4j
public class NamedDatasetImportFromAzureDfsStorageService extends Service<NamedDataset> {

    private final NamedDatasetManager namedDatasetManager;

    private final Connection connection;

    private final String container;

    private final String path;

    private final String targetDirectory;


    public NamedDatasetImportFromAzureDfsStorageService(NamedDatasetManager namedDatasetManager, Connection connection,
                                                        String container, String path, String targetDirectory) {

        this.namedDatasetManager = namedDatasetManager;
        this.connection = connection;
        this.container = container;
        this.path = path;
        this.targetDirectory = targetDirectory;
    }


    @Override
    protected Task<NamedDataset> createTask() {

        return new Task<>() {
            @Override
            protected NamedDataset call() throws IOException {

                File dl = AzureStorageDfsClient.download(connection, container, path, targetDirectory);
                NamedDataset namedDataset = namedDatasetManager.createNamedDataset(dl);
                namedDatasetManager.registerNamedDataset(namedDataset);
                return namedDataset;
            }
        };
    }

}
