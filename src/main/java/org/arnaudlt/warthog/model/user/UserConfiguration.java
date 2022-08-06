package org.arnaudlt.warthog.model.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.arnaudlt.warthog.model.setting.SqlHistoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
@Configuration
public class UserConfiguration {

    @Bean
    public Gson getGson() {

        return new GsonBuilder().setPrettyPrinting().create();
    }


    @Bean
    @Autowired
    public GlobalSettings getGlobalSettings(Gson gson,
                                            @Value("${warthog.user.directory}") String userDirectory,
                                            @Value("${warthog.spark.threads}") Integer sparkThreads,
                                            @Value("${warthog.spark.ui}") Boolean sparkUI,
                                            @Value("${warthog.overview.rows}") Integer overviewRows,
                                            @Value("${warthog.overview.truncate-after}") Integer overviewTruncateAfter) {

        GlobalSettings settings;
        try {

            settings = GlobalSettings.load(gson, userDirectory);
            // TODO if new properties have been added, we should default them.
        } catch (IOException e) {

            log.warn("Unable to read settings");
            settings = new GlobalSettings(gson, userDirectory, sparkThreads, sparkUI, overviewRows, overviewTruncateAfter);
            try {

                settings.persist();
            } catch (IOException ioException) {
                log.error("Unable to write settings", ioException);
            }
        }

        return settings;
    }


    @Bean
    @Autowired
    public ConnectionsCollection getConnectionsCollection(Gson gson,
                                                          @Value("${warthog.user.directory}") String userDirectory) {

        ConnectionsCollection connectionsCollection;
        try {

            connectionsCollection = ConnectionsCollection.load(gson, userDirectory);
        } catch (IOException e) {

            log.warn("Unable to read connections");
            connectionsCollection = new ConnectionsCollection(gson, userDirectory);
            try {

                connectionsCollection.persist();
            } catch (IOException ioException) {
                log.error("Unable to write connections", ioException);
            }
        }

        return connectionsCollection;
    }


    @Bean
    @Autowired
    public SqlHistoryCollection getSqlHistoryCollection(Gson gson,
                                                        @Value("${warthog.user.directory}") String userDirectory) {

        SqlHistoryCollection sqlHistoryCollection;
        try {

            sqlHistoryCollection = SqlHistoryCollection.load(gson, userDirectory);
        } catch (IOException e) {

            log.warn("Unable to read Sql history");
            sqlHistoryCollection = new SqlHistoryCollection(gson, userDirectory);
            try {

                sqlHistoryCollection.persist();
            } catch (IOException ioException) {
                log.error("Unable to write Sql history", ioException);
            }
        }

        return sqlHistoryCollection;
    }
}
