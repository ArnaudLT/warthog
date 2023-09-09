package org.arnaudlt.warthog.model.history;

import lombok.Data;

import java.io.Serializable;

@Data
public class SqlHistory implements Serializable {

    private String sqlQuery;

    private long timestamp;

    private long duration;

    private String fileName;


    public SqlHistory(String sqlQuery, long timestamp, long duration) {
        this.sqlQuery = sqlQuery;
        this.timestamp = timestamp;
        this.duration = duration;
    }
}
