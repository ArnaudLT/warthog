package org.arnaudlt.warthog.model.connection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

@EqualsAndHashCode
@Getter
@Setter
public class Connection implements Serializable {

    public static final long serialVersionUID = -2304815625109476983L;

    private String name;

    private ConnectionType connectionType;

    // AZ Storage
    private String configurationFilePath;

    // Database
    protected static final List<String> knownDatabaseType = List.of("SID", "Service name");

    private String host;

    private String port;

    private String database;

    // Oracle specific
    private String databaseType;

    private String user;

    private String password;


    public Connection(String name, ConnectionType connectionType) {
        this.name = name;
        this.connectionType = connectionType;
    }


    public Connection(Connection value) {
        this.name = value.name;
        this.connectionType = value.connectionType;
        this.configurationFilePath = value.configurationFilePath;
        this.host = value.host;
        this.port = value.port;
        this.database = value.database;
        this.databaseType = value.databaseType;
        this.user = value.user;
        this.password = value.password;
    }


    public void cleanUselessAttributs() {

        switch (connectionType) {

            case ORACLE_DATABASE:
            case POSTGRESQL:
                this.configurationFilePath = null;
                break;
            case AZURE_STORAGE:
                this.host = null;
                this.port = null;
                this.database = null;
                this.databaseType = null;
                this.user = null;
                this.password = null;
                break;
        }
    }


    @Override
    public String toString() {
        return name;
    }


    public String toExtraString() {
        return "{name='" + name + '\'' +
                ", connectionType=" + connectionType +
                ", configurationFilePath='" + configurationFilePath + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", database='" + database + '\'' +
                ", databaseType='" + databaseType + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                '}';
    }


    public Properties getDatabaseProperties() {

        final Properties dbProperties = new Properties();
        dbProperties.put("user", user);
        dbProperties.put("password", password);

        if (ConnectionType.ORACLE_DATABASE.equals(connectionType)) {

            dbProperties.put("driver", "oracle.jdbc.driver.OracleDriver");
        } else if (ConnectionType.POSTGRESQL.equals(connectionType)) {

            dbProperties.put("driver", "org.postgresql.Driver");
        }
        return dbProperties;
    }


    public String getDatabaseUrl() {

        String url = null;
        if (ConnectionType.ORACLE_DATABASE.equals(connectionType) && "SID".equals(databaseType)) {

            // jdbc:oracle:thin:@localhost:49161:xe
            url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
        } else if (ConnectionType.ORACLE_DATABASE.equals(connectionType) && "Service name".equals(databaseType)) {

            // jdbc:oracle:thin:@//localhost:49161/xe
            url = "jdbc:oracle:thin:@//" + host + ":" + port + "/" + database;
        } else if (ConnectionType.POSTGRESQL.equals(connectionType)) {

            // jdbc:postgresql://localhost:5432/postgres
            url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        }
        return url;
    }


    public static List<String> getKnownDatabaseType() {
        return knownDatabaseType;
    }
}
