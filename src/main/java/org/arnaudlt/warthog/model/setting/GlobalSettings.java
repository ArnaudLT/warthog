package org.arnaudlt.warthog.model.setting;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;

@Slf4j
public class GlobalSettings implements Serializable {

    public static final long serialVersionUID = 42L;

    private Integer sparkThreads;

    private Boolean sparkUI;

    private Integer overviewRows;


    @Autowired
    public GlobalSettings(
            @Value("${warthog.spark.threads}") Integer sparkThreads,
            @Value("${warthog.spark.ui}") Boolean sparkUI,
            @Value("${warthog.overview.rows}") Integer overviewRows) {

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


    public static void serialize(GlobalSettings settings) throws IOException {

        log.info("Try to delete the 'settings.ser'");
        new File("settings.ser").delete();

        log.info("Start to write settings in 'settings.ser'");
        try (FileOutputStream fos = new FileOutputStream("settings.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(settings);
        }
        log.info("Settings written : {}", settings);
    }


    public static GlobalSettings deserialize() throws IOException, ClassNotFoundException {

        log.info("Start to read settings from 'settings.ser'");
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
