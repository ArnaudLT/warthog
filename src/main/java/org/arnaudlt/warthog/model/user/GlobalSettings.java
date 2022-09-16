package org.arnaudlt.warthog.model.user;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class GlobalSettings implements Serializable {

    private static final String SETTINGS_JSON_FILENAME = "settings.json";

    private transient Gson gson;

    private transient UserSettings user;

    private SqlHistorySettings sqlHistory;

    private OverviewSettings overview;

    private SparkSettings spark;


    public GlobalSettings(Gson gson, DefaultSettings defaultSettings) {

        this.gson = gson;
        this.user = new UserSettings(defaultSettings.user());
        this.sqlHistory = new SqlHistorySettings(defaultSettings.sqlHistory());
        this.overview = new OverviewSettings(defaultSettings.overview());
        this.spark = new SparkSettings(defaultSettings.spark());
    }


    public void persist() throws IOException {

        log.info("Try to delete the '{}/{}'", user.getDirectory(), SETTINGS_JSON_FILENAME);
        Files.deleteIfExists(Paths.get(user.getDirectory(), SETTINGS_JSON_FILENAME));

        log.info("Start to write settings in '{}/{}'", user.getDirectory(), SETTINGS_JSON_FILENAME);
        String settingsJson = gson.toJson(this);
        Files.createDirectories(Paths.get(user.getDirectory()));
        Files.writeString(Paths.get(user.getDirectory(),SETTINGS_JSON_FILENAME), settingsJson, StandardOpenOption.CREATE);

        log.info("Settings written : {}", this);
    }


    public static GlobalSettings load(Gson gson, UserSettings user) throws FileNotFoundException {

        log.info("Start to load settings from '{}/{}'", user.getDirectory(), SETTINGS_JSON_FILENAME);
        GlobalSettings settings = gson.fromJson(new FileReader(new File(user.getDirectory(), SETTINGS_JSON_FILENAME)), GlobalSettings.class);
        settings.setGson(gson);
        settings.setUser(user);
        return settings;
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
}
