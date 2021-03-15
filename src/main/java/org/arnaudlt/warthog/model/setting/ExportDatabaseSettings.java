package org.arnaudlt.warthog.model.setting;

import java.util.List;
import java.util.Properties;

// TODO Temporary ! Must be removed once connections manager is ready.
public class ExportDatabaseSettings {

    private static final List<String> knownConnectionTypes = List.of("Oracle", "PostgreSQL");

    private static final List<String> knownDatabaseType = List.of("SID", "Service name");

    private final String connectionType;

    private final String host;

    private final String port;

    private final String database;

    private final String databaseType;

    private final String user;

    private final String password;

    private final String saveMode;

    private final String table;


    public ExportDatabaseSettings(String connectionType, String host, String port, String database, String databaseType,
                                  String user, String password, String saveMode, String table) {
        this.connectionType = connectionType;
        this.host = host;
        this.port = port;
        this.database = database;
        this.databaseType = databaseType;
        this.user = user;
        this.password = password;
        this.saveMode = saveMode;
        this.table = table;
    }


    public Properties getProperties() {

        final Properties dbProperties = new Properties();
        dbProperties.put("user", user);
        dbProperties.put("password", password);

        if ("Oracle".equals(connectionType)) {

            dbProperties.put("driver", "oracle.jdbc.driver.OracleDriver");
        } else if ("PostgreSQL".equals(connectionType)) {

            dbProperties.put("driver", "org.postgresql.Driver");
        }
        return dbProperties;
    }


    public String getUrl() {

        String url = null;
        if ("Oracle".equals(connectionType) && "SID".equals(databaseType)) {

            // jdbc:oracle:thin:@localhost:49161:xe
            url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
        } else if ("Oracle".equals(connectionType) && "Service name".equals(databaseType)) {

            // jdbc:oracle:thin:@//localhost:49161/xe
            url = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + database;
        } else if ("PostgreSQL".equals(connectionType)) {

            // jdbc:postgresql://localhost:5432/postgres
            url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        }
        return url;
    }


    public String getSaveMode() {
        return saveMode;
    }


    public String getTable() {
        return table;
    }


    public static List<String> getKnownConnectionTypes() {
        return knownConnectionTypes;
    }


    public static List<String> getKnownDatabaseType() {
        return knownDatabaseType;
    }
}