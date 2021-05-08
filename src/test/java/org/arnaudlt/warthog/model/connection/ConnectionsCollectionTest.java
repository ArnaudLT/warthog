package org.arnaudlt.warthog.model.connection;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
class ConnectionsCollectionTest {


    @Autowired
    private Gson gson;


    void persist() {


        Connection fakeAZConnection1 = new Connection("Azure storage sample", ConnectionType.AZURE_STORAGE);

        Connection fakeORAConnection2 = new Connection("Oracle database sample", ConnectionType.ORACLE_DATABASE);

        Connection fakePGConnection3 = new Connection("PostgreSQL sample", ConnectionType.POSTGRESQL);

        ConnectionsCollection connectionsCollection = new ConnectionsCollection(gson, "target");
        connectionsCollection.getConnections().addAll(Arrays.asList(fakeAZConnection1, fakeORAConnection2, fakePGConnection3));

        //connectionsCollection.persist();
    }


    void load() throws IOException {

        ConnectionsCollection connectionsCollection = ConnectionsCollection.load(gson, "target");
        log.info("Loading connections...");
        for (Connection connection : connectionsCollection) {

            log.info("Connection : {}", connection.getName());
        }

        //Assert.assertEquals(3, connectionsCollection.getConnections().size());
    }

}