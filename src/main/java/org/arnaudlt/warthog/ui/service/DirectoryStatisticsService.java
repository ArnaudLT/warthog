package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.util.PoolService;

@Slf4j
public class DirectoryStatisticsService extends AbstractMonitoredService<DirectoryStatisticsService.DirectoryStatistics> {

    private final Connection connection;

    private final String container;

    private final String path;


    public DirectoryStatisticsService(PoolService poolService, Connection connection, String container, String path) {

        super(poolService);
        this.connection = connection;
        this.container = container;
        this.path = path;
    }


    @Override
    protected Task<DirectoryStatistics> createTask() {

        return new Task<>() {
            @Override
            protected DirectoryStatistics call() {

                updateMessage("Gather statistics on " + container + "/" + path);
                updateProgress(-1,1);
                DirectoryStatistics statistics = AzureStorageDfsClient.getStatistics(connection, container, path);
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
