package org.arnaudlt.warthog.model.setting;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
public class GlobalSettings implements Serializable {


    private transient Gson gson;

    private transient String userDirectory;

    // ########## SPARK ##########
    private Integer sparkThreads;

    private Boolean sparkUI;

    // ########## OVERVIEW ##########
    private Integer overviewRows;


    public GlobalSettings(Gson gson, String userDirectory, Integer sparkThreads, Boolean sparkUI, Integer overviewRows) {

        this.gson = gson;
        this.userDirectory = userDirectory;
        this.sparkThreads = sparkThreads;
        this.sparkUI = sparkUI;
        this.overviewRows = overviewRows;
    }


    public Integer getOverviewRows() {
        return overviewRows;
    }


    public void setOverviewRows(Integer overviewRows) {
        this.overviewRows = overviewRows;
    }


    public Integer getSparkThreads() {
        return sparkThreads;
    }


    public void setSparkThreads(Integer sparkThreads) {
        this.sparkThreads = sparkThreads;
    }


    public Boolean getSparkUI() {
        return sparkUI;
    }


    public void setSparkUI(Boolean sparkUI) {
        this.sparkUI = sparkUI;
    }


    public void persist() throws IOException {

        log.info("Try to delete the '{}/settings.json'", userDirectory);
        Files.deleteIfExists(Paths.get(userDirectory, "settings.json"));

        log.info("Start to write settings in '{}/settings.json'", userDirectory);
        String settingsJson = gson.toJson(this);
        Files.createDirectories(Paths.get(userDirectory));
        Files.writeString(Paths.get(userDirectory,"settings.json"), settingsJson, StandardOpenOption.CREATE);

        log.info("Settings written : {}", this);
    }


    public static GlobalSettings load(Gson gson, String userDirectory) throws FileNotFoundException {

        log.info("Start to load settings from '{}/settings.json'", userDirectory);
        GlobalSettings settings = gson.fromJson(new FileReader(new File(userDirectory, "settings.json")), GlobalSettings.class);
        settings.setGson(gson);
        settings.setUserDirectory(userDirectory);
        log.info("Settings read : {}", settings);
        return settings;
    }


    private void setGson(Gson gson) {
        this.gson = gson;
    }


    private void setUserDirectory(String userDirectory) {
        this.userDirectory = userDirectory;
    }


    @Override
    public String toString() {
        return "{overviewRows=" + overviewRows +
                ", sparkThreads=" + sparkThreads +
                ", sparkUI=" + sparkUI +
                '}';
    }
}
