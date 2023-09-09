package org.arnaudlt.warthog.model.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.connection.ConnectionsCollection;
import org.arnaudlt.warthog.model.history.SqlHistoryCollection;
import org.arnaudlt.warthog.model.history.WorkspaceHistory;
import org.arnaudlt.warthog.model.setting.ImportSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Configuration
public class UserConfiguration {


    @Bean
    public Gson getGson() {

        return new GsonBuilder()
                .registerTypeAdapter(ImportSettings.class, new ImportSettings.ImportSettingsDeserializerAdapter())
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
    }


    @Bean
    @Autowired
    public GlobalSettings getGlobalSettings(Gson gson) {

        GlobalSettings settings;
        try {

            settings = GlobalSettings.load(gson);
            defaultMissingSettings(settings);
        } catch (IOException e) {

            log.warn("Unable to read settings");
            settings = new GlobalSettings(gson);
            try {

                settings.persist();
            } catch (IOException ioException) {
                log.error("Unable to write settings", ioException);
            }
        }
        log.info("Settings loaded : {}", settings);
        return settings;
    }


    private void defaultMissingSettings(GlobalSettings settings) {

        if (settings.getUser() == null) {
            settings.setUser(new UserSettings());
        }
        settings.getUser().setDirectory(DefaultSettings.INSTANCE.user.getDirectory()); // forced

        if (settings.getSpark() == null) {
            settings.setSpark(new SparkSettings());
        }
        if (settings.getOverview() == null) {
            settings.setOverview(new OverviewSettings());
        }
        if (settings.getSqlHistory() == null) {
            settings.setSqlHistory(new SqlHistorySettings());
        }

        if (settings.getUser().getPreferredDownloadDirectory() == null) {
            settings.getUser().setPreferredDownloadDirectory(DefaultSettings.INSTANCE.user.getPreferredDownloadDirectory());
        }
        if (settings.getUser().getPreferredExportDirectory() == null) {
            settings.getUser().setPreferredExportDirectory(DefaultSettings.INSTANCE.user.getPreferredExportDirectory());
        }
        if (settings.getSqlHistory().getDirectory() == null) {
            settings.getSqlHistory().setDirectory(DefaultSettings.INSTANCE.sqlHistory.getDirectory());
        }
        if (settings.getSqlHistory().getSize() == null) {
            settings.getSqlHistory().setSize(DefaultSettings.INSTANCE.sqlHistory.getSize());
        }
        if (settings.getSpark().getThreads() == null) {
            settings.getSpark().setThreads(DefaultSettings.INSTANCE.spark.getThreads());
        }
        if (settings.getSpark().getUi() == null) {
            settings.getSpark().setUi(DefaultSettings.INSTANCE.spark.getUi());
        }
        if (settings.getOverview().getRows() == null) {
            settings.getOverview().setRows(DefaultSettings.INSTANCE.overview.getRows());
        }
        if (settings.getOverview().getTruncateAfter() == null) {
            settings.getOverview().setTruncateAfter(DefaultSettings.INSTANCE.overview.getTruncateAfter());
        }
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


    @Bean
    @Autowired
    public SqlHistoryCollection getSqlHistoryCollection(Gson gson, GlobalSettings globalSettings) {

        SqlHistoryCollection sqlHistoryCollection;
        try {

            sqlHistoryCollection = SqlHistoryCollection.load(gson, globalSettings.getSqlHistory());
        } catch (IOException e) {

            log.warn("Unable to read history");
            sqlHistoryCollection = new SqlHistoryCollection(gson, globalSettings.getSqlHistory());
            try {

                sqlHistoryCollection.initializeHistoryDirectory();
            } catch (IOException ioException) {
                log.error("Unable to write history");
            }
        }
        return sqlHistoryCollection;
    }


    @Bean
    @Autowired
    public WorkspaceHistory getWorkspaceHistory(Gson gson, GlobalSettings globalSettings) {

        WorkspaceHistory workspaceHistory;
        try {
            workspaceHistory = WorkspaceHistory.load(gson, globalSettings.getWorkspace());
        } catch (Exception e) {

            log.warn("Unable to read workspace");
            workspaceHistory = new WorkspaceHistory(gson, globalSettings.getWorkspace(), new ArrayList<>());
        }
        return workspaceHistory;
    }

}
