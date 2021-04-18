package org.arnaudlt.warthog.model.connection;

public enum ConnectionType {

    ORACLE_DATABASE("Oracle Database"),
    POSTGRESQL("PostgreSQL"),
    AZURE_STORAGE("Azure DFS Storage");

    final String label;


    ConnectionType(String label) {
        this.label = label;
    }


    static ConnectionType valueFromLabel(String label) {

        for (ConnectionType f : values()) {
            if (f.label.equals(label)) {
                return f;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }
}
