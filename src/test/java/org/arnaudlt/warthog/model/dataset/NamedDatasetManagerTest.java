package org.arnaudlt.warthog.model.dataset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class NamedDatasetManagerTest {


    @Autowired
    private NamedDatasetManager namedDatasetManager;

    @Autowired
    private SparkSession sparkSession;


    @Test
    void createNamedDataset() {

        File file = new File("src/test/resources/covid19-sample.csv");
        NamedDataset namedDataset = this.namedDatasetManager.createNamedDataset(file);

        assertEquals("covid19-sample.csv", namedDataset.getName());

        assertEquals(7, namedDataset.getDataset().columns().length);
    }


    @Test
    void testIt() {

        String[] fl = new String[]{
                "src/test/resources/map_test.parquet/part-00002-0c02924e-645c-4efc-a600-249510188a39-c000.gz.parquet"
                ,"src/test/resources/map_test.parquet/part-00003-0c02924e-645c-4efc-a600-249510188a39-c000.gz.parquet"
        };
        Dataset<Row> parquet = this.sparkSession
                .read()
                .option("basePath", Paths.get("src/test/resources/map_test.parquet/").toString())
                .parquet(fl);

        parquet.show(100, false);
        assertEquals(3, parquet.count());
    }


    @Test
    void avroTest() {

        Dataset<Row> avroDs = this.sparkSession
                .read()
                .format("avro")
                .load("src/test/resources/userdata2.avro");

        avroDs.show(10);
        avroDs.printSchema();
    }


    @Test
    void exportMap() {

        File file = new File("src/test/resources/map_test.parquet");
        NamedDataset namedDataset = this.namedDatasetManager.createNamedDataset(file);

        Dataset<Row> dataset = namedDataset.getDataset();
        dataset.printSchema(10);

        dataset
                .withColumn("itemsLists", functions.callUDF("mapToString", dataset.col("itemsLists")))
                .coalesce(1)
                .write()
                .option("sep", ";")
                .option("header", true)
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .mode(SaveMode.Overwrite)
                .csv("target/with_map_and_array");
    }


    @Test
    void exportArrayAndMap() {

        Dataset<Row> dataset = datasetWithOneArray();

        dataset
                .withColumn("items", functions.callUDF("arrayToString", dataset.col("items")))
                .coalesce(1)
                .write()
                .option("sep", ";")
                .option("header", true)
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .mode(SaveMode.Overwrite)
                .csv("target/with_array");
    }


    @Test
    void exportMapWithKeyObject() {

        Dataset<Row> dataset = datasetWithMapKeyObject();

        dataset
                .write()
                .parquet("target/with_key_object");
    }


    @Test
    void hiveSupport() throws AnalysisException {

        File file = new File("src/test/resources/covid19-sample.csv");
        NamedDataset namedDataset = this.namedDatasetManager.createNamedDataset(file);
        namedDataset.getDataset().createTempView("cov");
        Dataset<Row> sqlResult = namedDataset.getDataset().sqlContext()
                .sql("SELECT `Nom Officiel Région` FROM cov WHERE `Indicateur (couleur)` = 'vert'");
        assertEquals(60, sqlResult.count());

        String[] tableNames = sparkSession.sqlContext().tableNames();
        assertEquals(1, tableNames.length);
        assertTrue(List.of(tableNames).contains("cov"));

        namedDataset.getDataset().createTempView("renamed_cov");
        sparkSession.catalog().dropTempView("cov");

        tableNames = sparkSession.sqlContext().tableNames();
        assertFalse(List.of(tableNames).contains("cov"));
        assertTrue(List.of(tableNames).contains("renamed_cov"));
    }


    @Test
    void registerNamedDataset() {

        File file = new File("src/test/resources/covid19-sample.csv");
        NamedDataset namedDataset = this.namedDatasetManager.createNamedDataset(file);

        this.namedDatasetManager.registerNamedDataset(namedDataset);
        assertTrue(this.namedDatasetManager.getObservableNamedDatasets().contains(namedDataset));

        this.namedDatasetManager.deregisterNamedDataset(namedDataset);
        assertFalse(this.namedDatasetManager.getObservableNamedDatasets().contains(namedDataset));
    }


    Dataset<Row> datasetWithOneArray() {

        ArrayList<ObjectWithOneArray> items = new ArrayList<>();
        items.add(new ObjectWithOneArray("number_1", List.of("n1_item1", "n1_item2", "n1_item3", "n1_item4", "n1_item5")));
        items.add(new ObjectWithOneArray("number_2", List.of("n2_item1", "n2_item2")));
        items.add(new ObjectWithOneArray("number_3", List.of("n3_item1", "n3_item2", "n3_item3")));
        items.add(new ObjectWithOneArray("number_4", List.of("n4_item1", "n4_item2", "n4_item3", "n4_item4")));

        return this.sparkSession.createDataset(items, Encoders.bean(ObjectWithOneArray.class)).select("name", "items");
    }


    Dataset<Row> datasetWithMapKeyObject() {

        ArrayList<MapWithKeyObject> items = new ArrayList<>();

        items.add(new MapWithKeyObject(Map.of(
                new ObjectWithOneArray("number_1", List.of("n1_item1", "n1_item2", "n1_item3", "n1_item4", "n1_item5")), "value_1",
                new ObjectWithOneArray("number_1", List.of("n2_item1", "n2_item2", "n2_item3", "n2_item4", "n2_item5")), "value_2",
                new ObjectWithOneArray("number_1", List.of("n3_item1", "n3_item2", "n3_item3", "n3_item4", "n3_item5")), "value_3"
                )));

        return this.sparkSession.createDataset(items, Encoders.bean(MapWithKeyObject.class)).select("data");
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class ObjectWithOneArray implements Serializable {

        private String name;

        private List<String> items;
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class MapWithKeyObject implements Serializable {

        private Map<ObjectWithOneArray, String> data;
    }


}