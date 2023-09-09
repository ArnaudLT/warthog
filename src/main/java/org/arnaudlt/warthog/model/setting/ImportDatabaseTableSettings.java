package org.arnaudlt.warthog.model.setting;

import org.arnaudlt.warthog.model.connection.Connection;

import java.util.Objects;

/**
 * Do not use a record otherwise it will not be serializable with Gson (? strange)
 */
public final class ImportDatabaseTableSettings implements ImportSettings {

    private final Connection connection;
    private final String tableName;

    public ImportDatabaseTableSettings(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    public Connection connection() {
        return connection;
    }

    public String tableName() {
        return tableName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ImportDatabaseTableSettings) obj;
        return Objects.equals(this.connection, that.connection) &&
                Objects.equals(this.tableName, that.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection, tableName);
    }

    @Override
    public String toString() {
        return "ImportDatabaseTableSettings[" +
                "connection=" + connection + ", " +
                "tableName=" + tableName + ']';
    }
}
