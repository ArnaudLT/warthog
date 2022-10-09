package org.arnaudlt.warthog.ui.service;

import com.azure.storage.file.datalake.models.PathItem;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzurePathItem;
import org.arnaudlt.warthog.model.azure.AzurePathItems;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.util.PoolService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class MockDirectoryListingService extends AzureDirectoryListingService {


    public MockDirectoryListingService(PoolService poolService, Connection connection, String azContainer, String azDirectoryPath) {

        super(poolService, connection, azContainer, azDirectoryPath);
    }


    @Override
    protected Task<AzurePathItems> createTask() {

        return new Task<>() {
            @Override
            protected AzurePathItems call() throws IOException {

                updateMessage("Listing content of " + getAzContainer() + "/" + getAzDirectoryPath());
                updateProgress(-1,1);

                final List<AzurePathItem> tmp;
                try (Stream<Path> paths = Files.walk(Paths.get(getAzDirectoryPath()), 1, FileVisitOption.FOLLOW_LINKS)) {
                    tmp = paths
                            .skip(1)
                            .map(path -> {
                                File file = path.toFile();
                                return new AzurePathItem(new PathItem(
                                        "-",
                                        OffsetDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()),
                                        file.length(),
                                        "",
                                        file.isDirectory(),
                                        path.toString(),
                                        "",
                                        ""));
                            })
                            .toList();
                }
                AzurePathItems azurePathItems = new AzurePathItems(tmp);

                updateProgress(1, 1);
                return azurePathItems;
            }
        };
    }

}
