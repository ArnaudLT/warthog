package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.util.PoolService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class SaveToSqlFileService extends AbstractMonitoredService<Void> {


    private final String content;

    private final File sqlFile;


    public SaveToSqlFileService(PoolService poolService, String content, File sqlFile) {

        super(poolService);
        this.content = content;
        this.sqlFile = sqlFile;
    }


    @Override
    protected Task<Void> createTask() {

        return new Task<>() {
            @Override
            protected Void call() throws IOException {

                log.info("Saving to SQL file '{}'", sqlFile.getAbsolutePath());
                updateMessage("Saving SQL sheet to " + sqlFile.getName());
                updateProgress(-1,1);
                try (FileWriter writer = new FileWriter(sqlFile)) {

                    writer.write(content);
                }
                updateProgress(1, 1);
                return null;
            }
        };
    }
}
