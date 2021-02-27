package org.arnaudlt.warthog.model.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class DatabaseConnection {


    private final String url;

    private final String user;

    private final String password;

    private final String driver;

    private final String saveMode;


    public DatabaseConnection(@Value("${warthog.datasource.url}") String url,
                              @Value("${warthog.datasource.username}") String user,
                              @Value("${warthog.datasource.password}") String password,
                              @Value("${warthog.datasource.driverClassName}") String driver,
                              @Value("${warthog.datasource.saveMode}")String saveMode) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.driver = driver;
        this.saveMode = saveMode;
    }


    public String getUrl() {
        return url;
    }


    public String getSaveMode() {
        return saveMode;
    }


    public Properties getProperties() {

        final Properties dbProperties = new Properties();
        dbProperties.put("user", user);
        dbProperties.put("password", password);
        dbProperties.put("driver", driver);
        return dbProperties;
    }
}
