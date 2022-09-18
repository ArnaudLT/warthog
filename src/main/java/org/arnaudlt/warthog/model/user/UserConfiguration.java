package org.arnaudlt.warthog.model.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.history.SqlHistoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
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
    public GlobalSettings getGlobalSettings(Gson gson, DefaultSettings defaultSettings) {

        GlobalSettings settings;
        try {

            settings = GlobalSettings.load(gson, defaultSettings.user());
            defaultMissingSettings(settings, defaultSettings);
        } catch (IOException e) {

            log.warn("Unable to read settings");
            settings = new GlobalSettings(gson, defaultSettings);
            try {

                settings.persist();
            } catch (IOException ioException) {
                log.error("Unable to write settings", ioException);
            }
        }
        log.info("Settings read : {}", settings);
        return settings;
    }


    private void defaultMissingSettings(GlobalSettings settings, DefaultSettings defaultSettings) {

        if (settings.getSpark() == null) {
            settings.setSpark(new SparkSettings());
        }
        if (settings.getOverview() == null) {
            settings.setOverview(new OverviewSettings());
        }
        if (settings.getSqlHistory() == null) {
            settings.setSqlHistory(new SqlHistorySettings());
        }

        if (settings.getSqlHistory().getDirectory() == null) {
            settings.getSqlHistory().setDirectory(defaultSettings.sqlHistory().getDirectory());
        }
        if (settings.getSqlHistory().getSize() == null) {
            settings.getSqlHistory().setSize(defaultSettings.sqlHistory().getSize());
        }
        if (settings.getSpark().getThreads() == null) {
            settings.getSpark().setThreads(defaultSettings.spark().getThreads());
        }
        if (settings.getSpark().getUi() == null) {
            settings.getSpark().setUi(defaultSettings.spark().getUi());
        }
        if (settings.getOverview().getRows() == null) {
            settings.getOverview().setRows(defaultSettings.overview().getRows());
        }
        if (settings.getOverview().getTruncateAfter() == null) {
            settings.getOverview().setTruncateAfter(defaultSettings.overview().getTruncateAfter());
        }
    }


    @Bean
    @Autowired
    public ConnectionsCollection getConnectionsCollection(Gson gson, DefaultSettings defaultSettings) {

        ConnectionsCollection connectionsCollection;
        try {

            connectionsCollection = ConnectionsCollection.load(gson, defaultSettings.user().getDirectory());
        } catch (IOException e) {

            log.warn("Unable to read connections");
            connectionsCollection = new ConnectionsCollection(gson, defaultSettings.user().getDirectory());
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
    public SqlHistoryCollection getSqlHistoryCollection(Gson gson, DefaultSettings defaultSettings) {

        SqlHistoryCollection sqlHistoryCollection;
        try {

            sqlHistoryCollection = SqlHistoryCollection.load(gson, defaultSettings.sqlHistory());
        } catch (IOException e) {

            log.warn("Unable to read history");
            sqlHistoryCollection = new SqlHistoryCollection(gson, defaultSettings.sqlHistory());
        }

        return sqlHistoryCollection;
    }

}
