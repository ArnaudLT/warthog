package org.arnaudlt.warthog.model.setting;

public class ExportDatabaseSettings {

    private final String tableName;

    private final String saveMode;


    public ExportDatabaseSettings(String tableName, String saveMode) {
        this.tableName = tableName;
        this.saveMode = saveMode;
    }


    public String getTableName() {
        return tableName;
    }


    public String getSaveMode() {
        return saveMode;
    }

}
