package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.setting.ImportAzureDfsStorageSettings;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class AzureDirectoryStatisticsService extends AbstractMonitoredService<AzureDirectoryStatisticsService.DirectoryStatistics> {

    private final Connection connection;

    private final ImportAzureDfsStorageSettings importAzureDfsStorageSettings;


    public AzureDirectoryStatisticsService(PoolService poolService, Connection connection, ImportAzureDfsStorageSettings importAzureDfsStorageSettings) {

        super(poolService);
        this.connection = connection;
        this.importAzureDfsStorageSettings = importAzureDfsStorageSettings;
    }


    @Override
    protected Task<DirectoryStatistics> createTask() {

        return new Task<>() {
            @Override
            protected DirectoryStatistics call() {

                updateMessage("Gather statistics on " + importAzureDfsStorageSettings.azContainer() + "/" + importAzureDfsStorageSettings.azDirectoryPaths());
                updateProgress(-1,1);
                DirectoryStatistics statistics = AzureStorageDfsClient.getStatistics(connection,
                        importAzureDfsStorageSettings.azContainer(), importAzureDfsStorageSettings.azDirectoryPaths(), importAzureDfsStorageSettings.azPathItems());
                updateProgress(1, 1);
                return statistics;
            }
        };
    }


    @AllArgsConstructor
    @NoArgsConstructor
    public static class DirectoryStatistics {

        public long filesCount;
        public long bytes;


        public DirectoryStatistics add(DirectoryStatistics subDirectoryStatistics) {

            this.filesCount += subDirectoryStatistics.filesCount;
            this.bytes += subDirectoryStatistics.bytes;
            return this;
        }


        public static DirectoryStatistics identity() {

            return new DirectoryStatistics(0, 0);
        }
    }

}
