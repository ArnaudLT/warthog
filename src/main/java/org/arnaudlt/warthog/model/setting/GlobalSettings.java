package org.arnaudlt.warthog.model.setting;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@NoArgsConstructor
public class GlobalSettings implements Serializable {

    private static final String SETTINGS_JSON_FILENAME = "settings.json";

    private transient Gson gson;

    private transient String userDirectory;

    // ########## SPARK ##########
    private Integer sparkThreads;

    private Boolean sparkUI;

    // ########## OVERVIEW ##########
    private Integer overviewRows;

    private Integer overviewTruncateAfter;


    public void persist() throws IOException {

        log.info("Try to delete the '{}/{}'", userDirectory, SETTINGS_JSON_FILENAME);
        Files.deleteIfExists(Paths.get(userDirectory, SETTINGS_JSON_FILENAME));

        log.info("Start to write settings in '{}/{}'", userDirectory, SETTINGS_JSON_FILENAME);
        String settingsJson = gson.toJson(this);
        Files.createDirectories(Paths.get(userDirectory));
        Files.writeString(Paths.get(userDirectory,SETTINGS_JSON_FILENAME), settingsJson, StandardOpenOption.CREATE);

        log.info("Settings written : {}", this);
    }


    public static GlobalSettings load(Gson gson, String userDirectory) throws FileNotFoundException {

        log.info("Start to load settings from '{}/{}'", userDirectory, SETTINGS_JSON_FILENAME);
        GlobalSettings settings = gson.fromJson(new FileReader(new File(userDirectory, SETTINGS_JSON_FILENAME)), GlobalSettings.class);
        settings.setGson(gson);
        settings.setUserDirectory(userDirectory);
        log.info("Settings read : {}", settings);
        return settings;
    }


    @Override
    public String toString() {
        return "GlobalSettings{" +
                "sparkThreads=" + sparkThreads +
                ", sparkUI=" + sparkUI +
                ", overviewRows=" + overviewRows +
                ", overviewTruncateAfter=" + overviewTruncateAfter +
                '}';
    }
}
