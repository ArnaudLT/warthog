package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ImportAzureDfsStorageSettings;

import java.io.File;
import java.io.IOException;

@Slf4j
public class NamedDatasetImportFromAzureDfsStorageService extends Service<NamedDataset> {

    private final NamedDatasetManager namedDatasetManager;

    private final Connection connection;

    private final ImportAzureDfsStorageSettings importAzureDfsStorageSettings;


    public NamedDatasetImportFromAzureDfsStorageService(NamedDatasetManager namedDatasetManager, Connection connection,
                                                        ImportAzureDfsStorageSettings importAzureDfsStorageSettings) {

        this.namedDatasetManager = namedDatasetManager;
        this.connection = connection;
        this.importAzureDfsStorageSettings = importAzureDfsStorageSettings;
    }


    @Override
    protected Task<NamedDataset> createTask() {

        return new Task<>() {
            @Override
            protected NamedDataset call() throws IOException {

                File dl = AzureStorageDfsClient.download(connection, importAzureDfsStorageSettings.getContainer(),
                        importAzureDfsStorageSettings.getAzDirectoryPath(), importAzureDfsStorageSettings.getLocalDirectoryPath());

                NamedDataset namedDataset = namedDatasetManager.createNamedDataset(dl);
                namedDatasetManager.registerNamedDataset(namedDataset);
                return namedDataset;
            }
        };
    }

}
