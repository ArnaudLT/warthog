package org.arnaudlt.warthog.model.dataset;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;

public class PreparedDataset {


    private final Dataset<Row> dataset;

    private final List<Row> overview;


    public PreparedDataset(Dataset<Row> dataset, List<Row> overview) {
        this.dataset = dataset;
        this.overview = overview;
    }


    public Dataset<Row> getDataset() {
        return dataset;
    }

    public List<Row> getOverview() {
        return overview;
    }

}
