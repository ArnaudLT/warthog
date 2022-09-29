package org.arnaudlt.warthog.ui.service;

import com.azure.storage.file.datalake.models.PathItem;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzurePathItem;
import org.arnaudlt.warthog.model.azure.AzurePathItems;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.util.PoolService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AzureDirectoryListingService extends AbstractMonitoredService<AzurePathItems> {

    private final Connection connection;

    private final String azContainer;

    private final String azDirectoryPath;


    public AzureDirectoryListingService(PoolService poolService, Connection connection, String azContainer, String azDirectoryPath) {

        super(poolService);
        this.connection = connection;
        this.azContainer = azContainer;
        this.azDirectoryPath = azDirectoryPath;
    }


    @Override
    protected Task<AzurePathItems> createTask() {

        return new Task<>() {
            @Override
            protected AzurePathItems call() throws InterruptedException {

                updateMessage("Listing content of " + azContainer + "/" + azDirectoryPath);
                updateProgress(-1,1);

                AzurePathItems azurePathItems = AzureStorageDfsClient.listDirectoryContent(connection, azContainer, azDirectoryPath);
                /*
                TimeUnit.SECONDS.sleep(10);
                List<AzurePathItem> azurePathItems = List.of(
                        new AzurePathItem(new PathItem("eTag", OffsetDateTime.now(), 8_192, "admin", false, "toto.json", "arnaud", "rwx-rw-r")),
                        new AzurePathItem(new PathItem("eTag", OffsetDateTime.now(), 0, "power_users", true, "data", "camille", "rwx-rw-r")),
                        new AzurePathItem(new PathItem("eTag", OffsetDateTime.now(), 23_496, "power_users", false, "titi.json", "camille", "rwx-rw-r")),
                        new AzurePathItem(new PathItem("eTag", OffsetDateTime.now(), 1_024, "admin", false, "tete.json", "arnaud", "rwx-rw-r")),
                        new AzurePathItem(new PathItem("eTag", OffsetDateTime.now(), 16_384, "user", false, "tata.json", "virginie", "rwx-rw-r"))
                );
                AzurePathItems azurePathItems = new AzurePathItems(azurePathItems);
                */
                updateProgress(1, 1);
                return azurePathItems;
            }
        };
    }

}
