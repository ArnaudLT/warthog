package org.arnaudlt.warthog.model.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
                                            @Value("${warthog.spark.threads}") Integer sparkThreads,
                                            @Value("${warthog.spark.ui}") Boolean sparkUI,
                                            @Value("${warthog.overview.rows}") Integer overviewRows) {

        GlobalSettings settings;
        try {

            settings = GlobalSettings.load(gson);
        } catch (IOException e) {

            log.warn("Unable to read settings");
            settings = new GlobalSettings(gson, sparkThreads, sparkUI, overviewRows);
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
    public ConnectionsCollection getConnectionsCollection(Gson gson) {

        ConnectionsCollection connectionsCollection;
        try {

            connectionsCollection = ConnectionsCollection.load(gson);
        } catch (IOException e) {

            log.warn("Unable to read connections");
            connectionsCollection = new ConnectionsCollection(gson);
            try {

                connectionsCollection.persist();
            } catch (IOException ioException) {
                log.error("Unable to write connections", ioException);
            }
        }

        return connectionsCollection;
    }
}
