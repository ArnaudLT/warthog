package org.arnaudlt.projectdse.model.dataset;

import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
class NamedDatasetManagerTest {


    @Autowired
    private NamedDatasetManager namedDatasetManager;

    @Autowired
    private SparkSession sparkSession;


    @Test
    void nestedColumnTest() {


    }


    @Test
    void createNamedDataset() {

        File file = new File("src/test/resources/covid19-sample.csv");
        NamedDataset namedDataset = this.namedDatasetManager.createNamedDataset(file);

        assertEquals("covid19-sample.csv", namedDataset.getName());

        Catalog catalog = namedDataset.getCatalog();
        assertEquals(7, catalog.getColumns().size());
    }


    void hiveSupport() throws AnalysisException {

        File file = new File("src/test/resources/covid19-sample.csv");
        NamedDataset namedDataset = this.namedDatasetManager.createNamedDataset(file);
        namedDataset.getDataset().createTempView("cov");
        namedDataset.getDataset().sqlContext()
                .sql("SELECT `Nom Officiel Région` FROM cov WHERE `Indicateur (couleur)` = 'vert'")
                .limit(10)
                .show(false);

        String[] tableNames = sparkSession.sqlContext().tableNames();
        assertEquals(1, tableNames.length);
        assertEquals("cov", tableNames[0]);
    }


    @Test
    void registerNamedDataset() {


    }

    @Test
    void deregisterNamedDataset() {


    }
}