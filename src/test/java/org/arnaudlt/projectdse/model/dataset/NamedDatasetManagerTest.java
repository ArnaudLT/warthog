package org.arnaudlt.projectdse.model.dataset;

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


    @Test
    void nestedColumnTest() {


    }


    @Test
    void createNamedDataset() {

        File file = new File("src/test/resources/covid19-sample.csv");
        NamedDataset namedDataset = this.namedDatasetManager.createNamedDataset(file);

        assertEquals("covid19-sample", namedDataset.getName());

        Catalog catalog = namedDataset.getCatalog();
        assertEquals(7, catalog.getColumns().size());
    }


    @Test
    void registerNamedDataset() {


    }

    @Test
    void deregisterNamedDataset() {


    }
}