package org.arnaudlt.warthog.model.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
class ConnectionsCollectionTest {

    @Test
    void persist() throws IOException {


        Connection fakeAZConnection1 = new Connection("Azure storage sample", ConnectionType.AZURE_STORAGE);
        fakeAZConnection1.setConfigurationFilePath("C:\\Users\\Arnaud\\Downloads\\samples\\config.snp");

        Connection fakeORAConnection2 = new Connection("Oracle database sample", ConnectionType.ORACLE_DATABASE);

        Connection fakePGConnection3 = new Connection("PostgreSQL sample", ConnectionType.POSTGRESQL);

        ConnectionsCollection connectionsCollection = new ConnectionsCollection();
        connectionsCollection.getConnections().addAll(Arrays.asList(fakeAZConnection1, fakeORAConnection2, fakePGConnection3));

        //connectionsCollection.persist();
    }

    @Test
    void load() throws IOException, ClassNotFoundException {

        ConnectionsCollection connectionsCollection = ConnectionsCollection.load();
        log.info("Loading connections...");
        for (Connection connection : connectionsCollection) {

            log.info("Connection : {}", connection.toExtraString());
        }

        //Assert.assertEquals(3, connectionsCollection.getConnections().size());
    }

}