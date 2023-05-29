package org.arnaudlt.warthog.ui.service;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.file.datalake.DataLakeDirectoryClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.models.PathItem;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.azure.AzurePathItem;
import org.arnaudlt.warthog.model.azure.AzurePathItems;
import org.arnaudlt.warthog.model.azure.AzureStorageDfsClient;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.setting.ImportAzureDfsStorageSettings;
import org.arnaudlt.warthog.model.setting.ImportDirectorySettings;
import org.arnaudlt.warthog.model.util.FileUtil;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.model.util.PoolService;

import java.io.IOException;
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

    private final AzureDirectoryStatisticsService.DirectoryStatistics statistics;


    public NamedDatasetImportFromAzureDfsStorageService(PoolService poolService, NamedDatasetManager namedDatasetManager, Connection connection,
                                                        ImportAzureDfsStorageSettings importAzureDfsStorageSettings,
                                                        AzureDirectoryStatisticsService.DirectoryStatistics statistics) {

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

        private final AzureDirectoryStatisticsService.DirectoryStatistics statistics;

        private final long totalWork;

        private long workDone;


        private NamedDatasetImportFromAzureDfsStorageTask(NamedDatasetManager namedDatasetManager, Connection connection,
                                                          ImportAzureDfsStorageSettings importAzureDfsStorageSettings,
                                                          AzureDirectoryStatisticsService.DirectoryStatistics statistics) {
            this.namedDatasetManager = namedDatasetManager;
            this.connection = connection;
            this.importAzureDfsStorageSettings = importAzureDfsStorageSettings;
            this.statistics = statistics;
            this.totalWork = statistics.bytes + 5_000_000;
            this.workDone = 0;
        }


        @Override
        protected NamedDataset call() throws Exception {

            updateMessage("Importing " + importAzureDfsStorageSettings.azContainer() + "/" +
                    importAzureDfsStorageSettings.azDirectoryPaths());

            updateProgress(workDone, totalWork);

            final AzurePathItems azurePathItems = importAzureDfsStorageSettings.azPathItems();
            final String customBasePath = importAzureDfsStorageSettings.basePath();
            final String name = importAzureDfsStorageSettings.name();

            DataLakeFileSystemClient fileSystem = getDataLakeFileSystemClient(connection, importAzureDfsStorageSettings.azContainer());

            Path defaultBaseDirectory = Paths.get(importAzureDfsStorageSettings.localDirectoryPath(), importAzureDfsStorageSettings.azContainer());
            createDirectory(defaultBaseDirectory);

            List<Path> listOfPaths = new ArrayList<>(); // feed by side effect
            log.info("Starting to download {}/{}", importAzureDfsStorageSettings.azContainer(), importAzureDfsStorageSettings.azDirectoryPaths());
            if (azurePathItems.isEmpty()) { // selection with Azure browser

                importAllPathItems(fileSystem, importAzureDfsStorageSettings.azDirectoryPaths(), listOfPaths);
            } else { // just one or several paths (no Azure browser)

                for (AzurePathItem azurePathItem : azurePathItems) {

                    final PathItem pathItem = azurePathItem.getPathItem();
                    if (!pathItem.isDirectory()) {

                        importOnePathItem(fileSystem, pathItem, listOfPaths);
                    } else {

                        importAllPathItems(fileSystem, pathItem.getName(), listOfPaths);
                    }
                }
            }
            log.info("Download of {}/{} completed", importAzureDfsStorageSettings.azContainer(), importAzureDfsStorageSettings.azDirectoryPaths());
            updateProgress(statistics.bytes, totalWork);

            ImportDirectorySettings importDirectorySettings = getImportDirectorySettings(customBasePath, name, defaultBaseDirectory, listOfPaths);
            NamedDataset namedDataset = namedDatasetManager.createNamedDataset(importDirectorySettings);

            namedDatasetManager.registerNamedDataset(namedDataset);
            updateProgress(totalWork, totalWork);
            return namedDataset;
        }


        private void importAllPathItems(DataLakeFileSystemClient fileSystem, List<String> pathItems, List<Path> listOfPaths) throws IOException {

            for (String pi : pathItems) {

                importAllPathItems(fileSystem, pi, listOfPaths);
            }
        }


        private void importAllPathItems(DataLakeFileSystemClient fileSystem, String pathItem, List<Path> listOfPaths) throws IOException {

            DataLakeDirectoryClient subDirectory = fileSystem.getDirectoryClient(pathItem);
            PagedIterable<PathItem> subPathItems = subDirectory.listPaths(true, false, null, null);
            for (PathItem subPathItem : subPathItems) {

                if (!subPathItem.isDirectory()) {
                    importOnePathItem(fileSystem, subPathItem, listOfPaths);
                }
            }
        }


        private void importOnePathItem(DataLakeFileSystemClient fileSystem, PathItem pathItem, List<Path> listOfPaths) throws IOException {

            Path localFilePath = Paths.get(importAzureDfsStorageSettings.localDirectoryPath(), importAzureDfsStorageSettings.azContainer(), pathItem.getName());
            updateMessage("Downloading " + pathItem.getName());
            workDone += AzureStorageDfsClient.downloadOnePathItem(fileSystem, pathItem, localFilePath);
            listOfPaths.add(localFilePath);
            updateProgress(workDone, totalWork);
        }


        private static ImportDirectorySettings getImportDirectorySettings(String customBasePath, String name, Path baseDirectory, List<Path> listOfPaths) {

            Path basePath = customBasePath.isBlank() ? baseDirectory : Paths.get(customBasePath);
            Format format = FileUtil.determineFormat(listOfPaths);
            String separator = FileUtil.inferSeparator(format, listOfPaths);
            String preferredName;
            if (name.isBlank()) {
                preferredName = basePath.getFileName().toString();
            } else {
                preferredName = name;
            }

            return new ImportDirectorySettings(listOfPaths, format, preferredName, separator, basePath);
        }
    }




}
