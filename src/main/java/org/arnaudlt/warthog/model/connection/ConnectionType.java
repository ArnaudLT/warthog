package org.arnaudlt.warthog.model.connection;

public enum ConnectionType {

    ORACLE_DATABASE("Oracle Database"),
    POSTGRESQL("PostgreSQL"),
    AZURE_STORAGE("Azure DFS Storage");

    final String label;


    ConnectionType(String label) {
        this.label = label;
    }


    @Override
    public String toString() {
        return label;
    }
}
