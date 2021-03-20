package org.arnaudlt.warthog.model.user;

import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class UserConfiguration {


    @Bean
    public GlobalSettings getGlobalSettings(@Value("${warthog.spark.threads}") Integer sparkThreads,
                                            @Value("${warthog.spark.ui}") Boolean sparkUI,
                                            @Value("${warthog.overview.rows}") Integer overviewRows) {

        GlobalSettings settings;
        try {

            settings = GlobalSettings.load();
        } catch (IOException | ClassNotFoundException e) {

            log.warn("Unable to read settings");
            settings = new GlobalSettings(sparkThreads, sparkUI, overviewRows);
            try {

                settings.persist();
            } catch (IOException ioException) {
                log.error("Unable to write settings", ioException);
            }
        }

        return settings;
    }


    @Bean
    public ConnectionsCollection getConnectionsCollection() {

        ConnectionsCollection connectionsCollection;
        try {

            connectionsCollection = ConnectionsCollection.load();
        } catch (IOException | ClassNotFoundException e) {

            log.warn("Unable to read connections");
            connectionsCollection = new ConnectionsCollection();
            try {

                connectionsCollection.persist();
            } catch (IOException ioException) {
                log.error("Unable to write connections", ioException);
            }
        }

        return connectionsCollection;
    }
}
