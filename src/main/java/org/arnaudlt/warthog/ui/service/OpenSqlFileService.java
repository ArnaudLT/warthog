package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Slf4j
public class OpenSqlFileService extends AbstractMonitoredService<String> {


    private final File sqlFile;


    public OpenSqlFileService(PoolService poolService, File sqlFile) {

        super(poolService);
        this.sqlFile = sqlFile;
    }


    @Override
    protected Task<String> createTask() {

        return new Task<>() {
            @Override
            protected String call() throws IOException {

                log.info("Opening Sql file '{}'", sqlFile.getAbsolutePath());
                updateMessage("Importing " + sqlFile.getName());
                updateProgress(-1,1);

                StringBuilder fileContent = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new FileReader(sqlFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        fileContent.append(line);
                        fileContent.append("\n");
                    }
                }

                updateProgress(1, 1);
                return fileContent.toString();
            }
        };
    }
}
