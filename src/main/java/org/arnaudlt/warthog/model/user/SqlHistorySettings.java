package org.arnaudlt.warthog.model.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SqlHistorySettings {

    private String directory;

    private Integer size;


    public SqlHistorySettings(SqlHistorySettings sqlHistorySettings) {

        this.directory = sqlHistorySettings.directory;
        this.size = sqlHistorySettings.size;
    }


    public SqlHistorySettings(GlobalSettings.SerializableSqlHistorySettings sqlHistory) {

        if (sqlHistory != null) {
            this.directory = sqlHistory.getDirectory();
            this.size = sqlHistory.getSize();
        }
    }
}
