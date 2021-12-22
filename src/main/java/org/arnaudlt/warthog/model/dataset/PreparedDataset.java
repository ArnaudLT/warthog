package org.arnaudlt.warthog.model.dataset;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public class PreparedDataset {

    private final String sqlQuery;

    private final Dataset<Row> dataset;

    private final List<Row> overview;


    public PreparedDataset(String sqlQuery, Dataset<Row> dataset, List<Row> overview) {
        this.sqlQuery = sqlQuery;
        this.dataset = dataset;
        this.overview = overview;
    }


    public String getSqlQuery() {
        return sqlQuery;
    }


    public Dataset<Row> getDataset() {
        return dataset;
    }


    public List<Row> getOverview() {
        return overview;
    }

}
