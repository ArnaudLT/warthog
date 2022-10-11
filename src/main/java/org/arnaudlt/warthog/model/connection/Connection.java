package org.arnaudlt.warthog.model.connection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jasypt.util.StrongTextEncryptor;

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
    private String tenantId;

    private String clientId;

    private String clientKey;

    private String proxyUrl;

    private Integer proxyPort;

    private String storageAccount;

    // Optional
    private String preferredContainer;

    // Optional
    private String preferredAzureDirectory;

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
        this.tenantId = value.tenantId;
        this.clientId = value.clientId;
        this.clientKey = value.clientKey;
        this.proxyUrl = value.proxyUrl;
        this.proxyPort = value.proxyPort;
        this.storageAccount = value.storageAccount;
        this.preferredContainer = value.preferredContainer;
        this.preferredAzureDirectory = value.preferredAzureDirectory;
        this.host = value.host;
        this.port = value.port;
        this.database = value.database;
        this.databaseType = value.databaseType;
        this.user = value.user;
        this.password = value.password;
    }


    public void cleanUselessAttributes() {

        if (connectionType == ConnectionType.ORACLE_DATABASE || connectionType == ConnectionType.POSTGRESQL) {
            this.tenantId = null;
            this.clientId = null;
            this.clientKey = null;
            this.proxyUrl = null;
            this.proxyPort = null;
            this.storageAccount = null;
            this.preferredContainer = null;
            this.preferredAzureDirectory = null;
        } else if (connectionType == ConnectionType.AZURE_STORAGE) {
            this.host = null;
            this.port = null;
            this.database = null;
            this.databaseType = null;
            this.user = null;
            this.password = null;
        }
    }


    @Override
    public String toString() {
        return name;
    }


    public Properties getDatabaseProperties(StrongTextEncryptor encryptor) {

        final Properties dbProperties = new Properties();
        dbProperties.put("user", user);
        dbProperties.put("password", encryptor.decrypt(password));

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
