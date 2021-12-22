package org.arnaudlt.warthog.model.dataset.decoration;

public class DatabaseDecoration implements Decoration {

    private final String source;

    private final String tableName;


    public DatabaseDecoration(String source, String tableName) {
        this.source = source;
        this.tableName = tableName;
    }


    public String getSource() {
        return source;
    }


    public String getTableName() {
        return tableName;
    }
}
