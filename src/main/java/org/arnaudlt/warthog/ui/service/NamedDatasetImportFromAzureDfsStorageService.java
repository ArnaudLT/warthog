package org.arnaudlt.warthog.ui.service;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.models.PathItem;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ImportAzureDfsStorageSettings;
import org.arnaudlt.warthog.model.util.PoolService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.arnaudlt.warthog.model.azure.AzureStorageDfsClient.*;

@Slf4j
public class NamedDatasetImportFromAzureDfsStorageService extends AbstractMonitoredService<NamedDataset> {

    private final NamedDatasetManager namedDatasetManager;

    private final Connection connection;

    private final ImportAzureDfsStorageSettings importAzureDfsStorageSettings;

    private final DirectoryStatisticsService.DirectoryStatistics statistics;


    public NamedDatasetImportFromAzureDfsStorageService(PoolService poolService, NamedDatasetManager namedDatasetManager, Connection connection,
                                                        ImportAzureDfsStorageSettings importAzureDfsStorageSettings,
                                                        DirectoryStatisticsService.DirectoryStatistics statistics) {

        super(poolService);
        this.namedDatasetManager = namedDatasetManager;
        this.connection = connection;
        this.importAzureDfsStorageSettings = importAzureDfsStorageSettings;
        this.statistics = statistics;
    }


    @Override
    protected Task<NamedDataset> createTask() {

        return new NamedDatasetImportFromAzureDfsStorageTask(namedDatasetManager, connection, importAzureDfsStorageSettings, statistics);
    }



    private static class NamedDatasetImportFromAzureDfsStorageTask extends Task<NamedDataset> {

        private final NamedDatasetManager namedDatasetManager;

        private final Connection connection;

        private final ImportAzureDfsStorageSettings importAzureDfsStorageSettings;

        private final DirectoryStatisticsService.DirectoryStatistics statistics;


        private NamedDatasetImportFromAzureDfsStorageTask(NamedDatasetManager namedDatasetManager, Connection connection,
                                                          ImportAzureDfsStorageSettings importAzureDfsStorageSettings,
                                                          DirectoryStatisticsService.DirectoryStatistics statistics) {
            this.namedDatasetManager = namedDatasetManager;
            this.connection = connection;
            this.importAzureDfsStorageSettings = importAzureDfsStorageSettings;
            this.statistics = statistics;
        }


        @Override
        protected NamedDataset call() throws Exception {

            updateMessage("Importing " + importAzureDfsStorageSettings.getAzContainer() + "/" +
                    importAzureDfsStorageSettings.getAzDirectoryPath());
            long totalWork = statistics.bytes + 5_000_000; // 5_000_000 is an arbitrary amount for the creation and the registration of the dataset
            long workDone = 0;
            updateProgress(workDone, totalWork);

            final String container = importAzureDfsStorageSettings.getAzContainer();
            final String azDirectoryPath = importAzureDfsStorageSettings.getAzDirectoryPath();
            final String localDirectoryPath = importAzureDfsStorageSettings.getLocalDirectoryPath();
            final String customBasePath = importAzureDfsStorageSettings.getBasePath();

            DataLakeFileSystemClient fileSystem = getDataLakeFileSystemClient(connection, container);
            DataLakeDirectoryClient directoryClient = fileSystem.getDirectoryClient(azDirectoryPath);

            Path baseDirectory = Paths.get(localDirectoryPath, container, azDirectoryPath);
            createDirectory(baseDirectory);

            List<Path> listOfPaths = new ArrayList<>();
            PagedIterable<PathItem> pathItems = directoryClient.listPaths(true, false, null, null);
            log.info("Starting to download {}/{}", container, azDirectoryPath);
            for (PathItem pathItem : pathItems) {

                Path localFilePath = Paths.get(localDirectoryPath, container, pathItem.getName());
                workDone += downloadOnePathItem(fileSystem, pathItem, localFilePath);
                listOfPaths.add(localFilePath);
                updateProgress(workDone, totalWork);
            }
            log.info("Download of {}/{} completed", container, azDirectoryPath);
            updateProgress(statistics.bytes, totalWork);

            Path basePath = customBasePath.isBlank() ? baseDirectory : Paths.get(customBasePath);
            String preferredName = basePath.getFileName().toString();
            NamedDataset namedDataset = namedDatasetManager.createNamedDataset(basePath, listOfPaths, preferredName);

            namedDatasetManager.registerNamedDataset(namedDataset);
            updateProgress(totalWork, totalWork);
            return namedDataset;
        }
    }


}
