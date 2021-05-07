package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.setting.ImportAzureDfsStorageSettings;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class DirectoryStatisticsService extends AbstractMonitoredService<DirectoryStatisticsService.DirectoryStatistics> {

    private final Connection connection;

    private final String azContainer;

    private final String azPath;


    public DirectoryStatisticsService(PoolService poolService, Connection connection, ImportAzureDfsStorageSettings importAzureDfsStorageSettings) {

        super(poolService);
        this.connection = connection;
        this.azContainer = importAzureDfsStorageSettings.getAzContainer();
        this.azPath = importAzureDfsStorageSettings.getAzDirectoryPath();
    }


    @Override
    protected Task<DirectoryStatistics> createTask() {

        return new Task<>() {
            @Override
            protected DirectoryStatistics call() {

                updateMessage("Gather statistics on " + azContainer + "/" + azPath);
                updateProgress(-1,1);
                DirectoryStatistics statistics = AzureStorageDfsClient.getStatistics(connection, azContainer, azPath);
                updateProgress(1, 1);
                return statistics;
            }
        };
    }


    public static class DirectoryStatistics {

        public long filesCount;
        public long bytes;

    }

}
