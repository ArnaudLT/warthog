package org.arnaudlt.warthog.model.user;

import lombok.*;

@Getter
@Setter
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


    @Override
    public String toString() {
        return "SqlHistorySettings{" +
                "directory='" + directory + '\'' +
                ", size=" + size +
                '}';
    }
}
