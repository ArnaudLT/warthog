package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;

@Slf4j
public class DirectoryStatisticsService extends Service<DirectoryStatisticsService.DirectoryStatistics> {

    private final Connection connection;

    private final String container;

    private final String path;


    public DirectoryStatisticsService(Connection connection, String container, String path) {

        this.connection = connection;
        this.container = container;
        this.path = path;
    }


    @Override
    protected Task<DirectoryStatistics> createTask() {

        return new Task<>() {
            @Override
            protected DirectoryStatistics call() {

                return AzureStorageDfsClient.getStatistics(connection, container, path);
            }
        };
    }


    public static class DirectoryStatistics {

        public long filesCount;
        public long bytes;

    }

}
