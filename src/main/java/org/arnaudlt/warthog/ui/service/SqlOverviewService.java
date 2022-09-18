package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.warthog.model.dataset.NamedDatasetManager;
import org.arnaudlt.warthog.model.dataset.PreparedDataset;
import org.arnaudlt.warthog.model.history.SqlHistory;
import org.arnaudlt.warthog.model.history.SqlHistoryCollection;
import org.arnaudlt.warthog.model.user.GlobalSettings;
import org.arnaudlt.warthog.model.util.PoolService;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SqlOverviewService extends AbstractMonitoredService<PreparedDataset> {


    private final NamedDatasetManager namedDatasetManager;

    private final String sqlQuery;

    private final GlobalSettings globalSettings;

    private final SqlHistoryCollection sqlHistoryCollection;


    public SqlOverviewService(PoolService poolService, NamedDatasetManager namedDatasetManager, String sqlQuery,
                              GlobalSettings globalSettings, SqlHistoryCollection sqlHistoryCollection) {

        super(poolService);
        this.namedDatasetManager = namedDatasetManager;
        this.sqlQuery = sqlQuery;
        this.globalSettings = globalSettings;
        this.sqlHistoryCollection = sqlHistoryCollection;
    }


    @Override
    protected Task<PreparedDataset> createTask() {

        return new Task<>() {
            @Override
            protected PreparedDataset call() {

                log.info("Start generating an overview for the sql query : \"{}\"", sqlQuery.replace("\n", " "));
                updateMessage("Generating overview");
                updateProgress(-1,1);

                long startTimestamp = Instant.now().toEpochMilli();

                Dataset<Row> ds = namedDatasetManager.prepareDataset(sqlQuery);
                List<Map<String, String>> rows = ds.takeAsList(globalSettings.getOverview().getRows())
                        .stream()
                        .map(r -> {
                            Map<String, String> map = new HashMap<>();
                            for (StructField field : ds.schema().fields()) {

                                String valueAsString;
                                Object value = r.getAs(field.name());
                                if (value != null) {
                                    valueAsString = value.toString();
                                } else {
                                    valueAsString = "";
                                }
                                map.put(field.name(), valueAsString);
                            }
                            return map;
                        })
                        .toList();
                PreparedDataset preparedDataset = new PreparedDataset(sqlQuery, ds, rows);

                long endTimestamp = Instant.now().toEpochMilli();
                long durationTimestamp = endTimestamp - startTimestamp;

                SqlHistory sqlHistory = new SqlHistory(sqlQuery, startTimestamp, durationTimestamp);
                sqlHistoryCollection.persistOne(sqlHistory);

                updateProgress(1, 1);
                return preparedDataset;
            }
        };
    }
}
