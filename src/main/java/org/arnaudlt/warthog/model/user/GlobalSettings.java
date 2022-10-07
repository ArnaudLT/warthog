package org.arnaudlt.warthog.model.user;

import com.google.gson.Gson;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Getter
@Setter
public class GlobalSettings {

    private static final String SETTINGS_JSON_FILENAME = "settings.json";

    private final Gson gson;

    private UserSettings user;

    private SqlHistorySettings sqlHistory;

    private OverviewSettings overview;

    private SparkSettings spark;


    public GlobalSettings(Gson gson) {

        this.gson = gson;
        this.user = new UserSettings(DefaultSettings.INSTANCE.user);
        this.sqlHistory = new SqlHistorySettings(DefaultSettings.INSTANCE.sqlHistory);
        this.overview = new OverviewSettings(DefaultSettings.INSTANCE.overview);
        this.spark = new SparkSettings(DefaultSettings.INSTANCE.spark);
    }


    public void persist() throws IOException {

        log.info("Try to delete the '{}/{}'", user.getDirectory(), SETTINGS_JSON_FILENAME);
        Files.deleteIfExists(Paths.get(user.getDirectory(), SETTINGS_JSON_FILENAME));

        log.info("Start to write settings in '{}/{}'", user.getDirectory(), SETTINGS_JSON_FILENAME);

        SerializableGlobalSettings serializableGlobalSettings = new SerializableGlobalSettings(this);
        String settingsJson = gson.toJson(serializableGlobalSettings);

        Files.createDirectories(Paths.get(user.getDirectory()));
        Files.writeString(Paths.get(user.getDirectory(),SETTINGS_JSON_FILENAME), settingsJson, StandardOpenOption.CREATE);

        log.info("Settings written : {}", this);
    }


    public static GlobalSettings load(Gson gson) throws FileNotFoundException {

        log.info("Start to load settings from '{}/{}'", DefaultSettings.INSTANCE.user.getDirectory(), SETTINGS_JSON_FILENAME);
        SerializableGlobalSettings serializableGlobalSettings = gson.fromJson(
                new FileReader(new File(DefaultSettings.INSTANCE.user.getDirectory(), SETTINGS_JSON_FILENAME)), SerializableGlobalSettings.class);

        GlobalSettings globalSettings = getGlobalSettings(gson, serializableGlobalSettings);
        log.info("Settings loaded {}", globalSettings);

        return globalSettings;
    }


    private static GlobalSettings getGlobalSettings(Gson gson, SerializableGlobalSettings serializableGlobalSettings) {

        GlobalSettings globalSettings = new GlobalSettings(gson);
        globalSettings.setUser(new UserSettings(serializableGlobalSettings.user));
        globalSettings.setSqlHistory(new SqlHistorySettings(serializableGlobalSettings.sqlHistory));
        globalSettings.setOverview(new OverviewSettings(serializableGlobalSettings.overview));
        globalSettings.setSpark(new SparkSettings(serializableGlobalSettings.spark));

        return globalSettings;
    }


    @Override
    public String toString() {
        return "GlobalSettings{" +
                "user=" + user +
                ", sqlHistory=" + sqlHistory +
                ", overview=" + overview +
                ", spark=" + spark +
                '}';
    }

    @Data
    protected static class SerializableGlobalSettings implements Serializable {

        private SerializableUserSettings user;

        private SerializableSqlHistorySettings sqlHistory;

        private SerializableOverviewSettings overview;

        private SerializableSparkSettings spark;


        public SerializableGlobalSettings(GlobalSettings globalSettings) {

            this.user = new SerializableUserSettings(globalSettings.user);
            this.sqlHistory = new SerializableSqlHistorySettings(globalSettings.sqlHistory);
            this.overview = new SerializableOverviewSettings(globalSettings.overview);
            this.spark = new SerializableSparkSettings(globalSettings.spark);
        }
    }

    @Data
    protected static class SerializableUserSettings implements Serializable {

        private String preferredDownloadDirectory;

        public SerializableUserSettings(UserSettings userSettings) {
            this.preferredDownloadDirectory = userSettings.getPreferredDownloadDirectory();
        }
    }

    @Data
    protected static class SerializableSqlHistorySettings implements Serializable {

        private String directory;
        private Integer size;

        public SerializableSqlHistorySettings(SqlHistorySettings sqlHistorySettings) {
            this.directory = sqlHistorySettings.getDirectory();
            this.size = sqlHistorySettings.getSize();
        }
    }

    @Data
    protected static class SerializableOverviewSettings implements Serializable {

        private Integer rows;
        private Integer truncateAfter;

        public SerializableOverviewSettings(OverviewSettings overviewSettings) {
            this.rows = overviewSettings.getRows();
            this.truncateAfter = overviewSettings.getTruncateAfter();
        }
    }

    @Data
    protected static class SerializableSparkSettings implements Serializable {

        private Integer threads;
        private Boolean ui;

        public SerializableSparkSettings(SparkSettings spark) {
            this.threads = spark.getThreads();
            this.ui = spark.getUi();
        }
    }


}
