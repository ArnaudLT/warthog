package org.arnaudlt.warthog.model.user;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public final class SqlHistorySettings implements Serializable {

    private String directory;


    public SqlHistorySettings(SqlHistorySettings sqlHistorySettings) {

        this.directory = sqlHistorySettings.directory;
    }
}
