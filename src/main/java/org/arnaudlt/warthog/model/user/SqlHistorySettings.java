package org.arnaudlt.warthog.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class SqlHistorySettings {

    private String directory;


    public SqlHistorySettings(SqlHistorySettings sqlHistorySettings) {

        this.directory = sqlHistorySettings.directory;
    }
}
