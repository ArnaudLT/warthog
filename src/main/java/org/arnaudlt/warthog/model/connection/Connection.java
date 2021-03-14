package org.arnaudlt.warthog.model.connection;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

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


    public void clean() {

        switch (connectionType) {

            case ORACLE_DATABASE:
                this.configurationFilePath = "";
                break;
            case POSTGRESQL:
                this.configurationFilePath = "";
                break;
            case AZURE_STORAGE:
                this.host = "";
                this.port = "";
                this.database = "";
                this.databaseType = "";
                this.user = "";
                this.password = "";
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
}
