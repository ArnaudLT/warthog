package org.arnaudlt.warthog.model.azure;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.file.datalake.*;
import com.azure.storage.file.datalake.models.PathItem;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.exception.ProcessingException;
import org.arnaudlt.warthog.ui.service.DirectoryStatisticsService;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.OffsetDateTime;

@Slf4j
public class AzureStorageDfsClient {



    private AzureStorageDfsClient() {}


    public static DirectoryStatisticsService.DirectoryStatistics getStatistics(Connection connection, String container, String path) {

        DataLakeFileSystemClient fileSystem = getDataLakeFileSystemClient(connection, container);
        DataLakeDirectoryClient directoryClient = fileSystem.getDirectoryClient(path);

        DirectoryStatisticsService.DirectoryStatistics directoryStatistics = new DirectoryStatisticsService.DirectoryStatistics();

        PagedIterable<PathItem> pathItems = directoryClient.listPaths(true, false, null, null);
        for (PathItem pathItem : pathItems) {

            if (!pathItem.isDirectory()) {

                DataLakeFileClient fileClient = fileSystem.getFileClient(pathItem.getName());

                directoryStatistics.filesCount++;
                directoryStatistics.bytes += fileClient.getProperties().getFileSize();
            }
        }
        return directoryStatistics;
    }


    public static long downloadOnePathItem(DataLakeFileSystemClient fileSystem, PathItem pathItem, Path localFilePath) throws IOException {

        final long fileSize;
        if (!pathItem.isDirectory()) {

            log.info("Downloading file : {}", localFilePath);
            createDirectory(localFilePath.getParent());
            fileSize = downloadOneFile(fileSystem, pathItem, localFilePath);
        } else {
            // Allow to keep empty directories
            createDirectory(localFilePath);
            fileSize = 0;
        }
        return fileSize;
    }


    private static long downloadOneFile(DataLakeFileSystemClient fileSystem, PathItem pathItem, Path localFilePath) {

        DataLakeDirectoryClient dc = fileSystem.getDirectoryClient(Paths.get(pathItem.getName()).getParent().toString());
        String fileName = Paths.get(pathItem.getName()).getFileName().toString();
        DataLakeFileClient fileClient = dc.getFileClient(fileName);

        fileClient.readToFile(localFilePath.toString());

        return fileClient.getProperties().getFileSize();
    }


    public static DataLakeFileSystemClient getDataLakeFileSystemClient(Connection connection, String container) {

        DataLakeServiceClient datalakeServiceClient = getDataLakeServiceClient(connection);
        return datalakeServiceClient.getFileSystemClient(container);
    }


    public static void createDirectory(Path targetDirectoryPath) throws IOException {

        if (!Files.exists(targetDirectoryPath)) {
            log.info("Creating directory : {}", targetDirectoryPath);
            Files.createDirectories(targetDirectoryPath);
        }
    }


    private static DataLakeServiceClient getDataLakeServiceClient(Connection connection) {

        HttpClient azureHttpClient = new NettyAsyncHttpClientBuilder()
                .proxy(new ProxyOptions(ProxyOptions.Type.SOCKS5,
                        new InetSocketAddress(connection.getProxyUrl(), connection.getProxyPort())))
                .build();

        return new DataLakeServiceClientBuilder()
                .credential(new AzureTokenCredential(connection))
                .endpoint("https://" + connection.getStorageAccount() + ".dfs.core.windows.net")
                .httpClient(azureHttpClient)
                .buildClient();
    }


    private record AzureTokenCredential(Connection connection) implements TokenCredential {


        @Override
        public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext) {

            AccessToken accessToken;
            try {

                ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
                        connection.getClientId(),
                        connection.getTenantId(),
                        connection.getClientKey(),
                        AzureEnvironment.AZURE
                );

                applicationTokenCredentials.withProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(
                        connection.getProxyUrl(), connection.getProxyPort()
                )));

                String token = applicationTokenCredentials.getToken("https://storage.azure.com");
                accessToken = new AccessToken(token, OffsetDateTime.now().plus(Duration.ofHours(2)));

                return Mono.just(accessToken);
            } catch (IOException e) {

                throw new ProcessingException("Unable to get access token from Azure storage", e);
            }
        }
    }
}
