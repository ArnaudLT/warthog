package org.arnaudlt.warthog.model.database;

import java.util.Properties;


public class DatabaseSettings {


    private final String url;

    private final String user;

    private final String password;

    private final String driver;

    private final String saveMode;

    private final String table;


    public DatabaseSettings(String url, String user, String password, String driver, String saveMode, String table) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.driver = driver;
        this.saveMode = saveMode;
        this.table = table;
    }


    public String getUrl() {
        return url;
    }


    public String getSaveMode() {
        return saveMode;
    }


    public String getTable() {
        return table;
    }


    public Properties getProperties() {

        final Properties dbProperties = new Properties();
        dbProperties.put("user", user);
        dbProperties.put("password", password);
        dbProperties.put("driver", driver);
        return dbProperties;
    }
}
