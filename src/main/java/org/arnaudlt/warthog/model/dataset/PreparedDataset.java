package org.arnaudlt.warthog.model.dataset;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;
import java.util.Map;

public record PreparedDataset(String sqlQuery, Dataset<Row> dataset, List<Map<String,String>> overview) {}
