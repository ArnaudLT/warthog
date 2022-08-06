package org.arnaudlt.warthog.model.setting;


import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.time.Instant;

@SpringBootTest
class SqlHistoryCollectionTest {


    @Autowired
    private Gson gson;


    @Test
    void persist() throws IOException {

        SqlHistoryCollection sqlHistoryCollection = new SqlHistoryCollection(gson, "target");

        sqlHistoryCollection.addToSqlHistory(new SqlHistoryCollection.SqlHistory(Instant.now().toEpochMilli(), "select * from my_dummy_table;"));
        sqlHistoryCollection.addToSqlHistory(new SqlHistoryCollection.SqlHistory(Instant.now().toEpochMilli(), "select name, age, books from human;"));
        sqlHistoryCollection.addToSqlHistory(new SqlHistoryCollection.SqlHistory(Instant.now().toEpochMilli(), "select * from my_dummy_table;"));

        sqlHistoryCollection.persist();
    }
}