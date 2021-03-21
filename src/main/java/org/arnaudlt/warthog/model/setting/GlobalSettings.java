package org.arnaudlt.warthog.model.setting;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class GlobalSettings implements Serializable {

    public static final long serialVersionUID = 42L;

    private Integer sparkThreads;

    private Boolean sparkUI;

    private Integer overviewRows;


    public GlobalSettings(Integer sparkThreads, Boolean sparkUI, Integer overviewRows) {

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

        log.info("Try to delete the 'settings.ser'");
        new File("settings.ser").delete();

        log.info("Start to write settings in 'settings.ser'");
        try (FileOutputStream fos = new FileOutputStream("settings.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(this);
        }
        log.info("Settings written : {}", this);
    }


    public static GlobalSettings load() throws IOException, ClassNotFoundException {

        log.info("Start to load settings from 'settings.ser'");
        GlobalSettings settings;
        try (FileInputStream fis = new FileInputStream("settings.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            settings = (GlobalSettings) ois.readObject();
        }
        log.info("Settings read : {}", settings);
        return settings;
    }


    @Override
    public String toString() {
        return "{overviewRows=" + overviewRows +
                ", sparkThreads=" + sparkThreads +
                ", sparkUI=" + sparkUI +
                '}';
    }
}
