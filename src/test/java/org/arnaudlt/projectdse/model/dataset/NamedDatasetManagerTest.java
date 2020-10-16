package org.arnaudlt.projectdse.model.dataset;

import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

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

        Catalog catalog = namedDataset.getCatalog();
        assertEquals(7, catalog.getColumns().size());
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
        assertEquals("cov", tableNames[0]);
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

}