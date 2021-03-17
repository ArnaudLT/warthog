package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.NamedDataset;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;

@Slf4j
public class NamedDatasetImportFromDatabaseService extends Service<NamedDataset> {


    private final NamedDatasetManager namedDatasetManager;

    private final Connection connection;

    private final String tableName;


    public NamedDatasetImportFromDatabaseService(NamedDatasetManager namedDatasetManager, Connection connection, String tableName) {

        this.namedDatasetManager = namedDatasetManager;
        this.connection = connection;
        this.tableName = tableName;
    }


    @Override
    protected Task<NamedDataset> createTask() {

        return new Task<>() {
            @Override
            protected NamedDataset call() {

                log.info("Start importing a named dataset from {} table on {}", tableName, connection.getName() );
                NamedDataset namedDataset = namedDatasetManager.createNamedDataset(connection, tableName);
                namedDatasetManager.registerNamedDataset(namedDataset);
                return namedDataset;
            }
        };
    }
}
