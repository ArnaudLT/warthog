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

    // ########## SPARK ##########
    private Integer sparkThreads;

    private Boolean sparkUI;

    // ########## OVERVIEW ##########
    private Integer overviewRows;


    public GlobalSettings(Gson gson, Integer sparkThreads, Boolean sparkUI, Integer overviewRows) {

        this.gson = gson;
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

        log.info("Try to delete the 'settings.json'");
        new File("settings.json").delete();

        log.info("Start to write settings in 'settings.json'");
        String settingsJson = gson.toJson(this);
        Files.writeString(Paths.get("settings.json"), settingsJson, StandardOpenOption.CREATE);

        log.info("Settings written : {}", this);
    }


    public static GlobalSettings load(Gson gson) throws FileNotFoundException {

        log.info("Start to load settings from 'settings.json'");
        GlobalSettings settings = gson.fromJson(new FileReader("settings.json"), GlobalSettings.class);
        settings.setGson(gson);
        log.info("Settings read : {}", settings);
        return settings;
    }


    private void setGson(Gson gson) {
        this.gson = gson;
    }


    @Override
    public String toString() {
        return "{overviewRows=" + overviewRows +
                ", sparkThreads=" + sparkThreads +
                ", sparkUI=" + sparkUI +
                '}';
    }
}
