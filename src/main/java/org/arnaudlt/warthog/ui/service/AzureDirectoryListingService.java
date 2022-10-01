package org.arnaudlt.warthog.ui.service;

import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzurePathItems;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class AzureDirectoryListingService extends AbstractMonitoredService<AzurePathItems> {

    private final Connection connection;

    private final String azContainer;

    private final String azDirectoryPath;

    private DataLakeFileSystemClient fileSystem;


    public AzureDirectoryListingService(PoolService poolService, Connection connection, String azContainer, String azDirectoryPath) {

        super(poolService);
        this.connection = connection;
        this.azContainer = azContainer;
        this.azDirectoryPath = azDirectoryPath;
    }


    public Connection getConnection() {
        return connection;
    }

    public String getAzContainer() {
        return azContainer;
    }

    public String getAzDirectoryPath() {
        return azDirectoryPath;
    }

    @Override
    protected Task<AzurePathItems> createTask() {

        return new Task<>() {
            @Override
            protected AzurePathItems call() {

                updateMessage("Listing content of " + azContainer + "/" + azDirectoryPath);
                updateProgress(-1,1);

                if (fileSystem == null) {
                    fileSystem = AzureStorageDfsClient.getDataLakeFileSystemClient(connection, azContainer);
                }
                AzurePathItems azurePathItems = AzureStorageDfsClient.listDirectoryContent(fileSystem, azDirectoryPath);

                updateProgress(1, 1);
                return azurePathItems;
            }
        };
    }

}
