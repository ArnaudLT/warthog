package org.arnaudlt.warthog.model.connection;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.user.DefaultSettings;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Getter
@Setter
public class ConnectionsCollection implements Iterable<Connection> {

    public static final String CONNECTIONS_JSON_FILENAME = "connections.json";

    private Gson gson;

    private ObservableList<Connection> connections;


    public ConnectionsCollection(Gson gson) {

        this.gson = gson;
        this.connections = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(new ArrayList<>()));
    }


    public ConnectionsCollection(Gson gson, List<Connection> connections) {

        this.gson = gson;
        this.connections = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(connections));
    }


    public void persist() throws IOException {

        log.info("Try to delete the '{}/{}'", DefaultSettings.INSTANCE.user.getDirectory(), CONNECTIONS_JSON_FILENAME);
        Files.deleteIfExists(Paths.get(DefaultSettings.INSTANCE.user.getDirectory(), CONNECTIONS_JSON_FILENAME));

        log.info("Start to write connections in '{}/{}'", DefaultSettings.INSTANCE.user.getDirectory(), CONNECTIONS_JSON_FILENAME);

        SerializableConnectionsCollection serializableConnectionsCollection = this.getSerializableConnectionsCollection();
        String connectionsJson = gson.toJson(serializableConnectionsCollection);

        Files.createDirectories(Paths.get(DefaultSettings.INSTANCE.user.getDirectory()));
        Files.writeString(Paths.get(DefaultSettings.INSTANCE.user.getDirectory(), CONNECTIONS_JSON_FILENAME), connectionsJson, StandardOpenOption.CREATE);

        log.info("Connections written in '{}/{}'", DefaultSettings.INSTANCE.user.getDirectory(), CONNECTIONS_JSON_FILENAME);
    }


    public static ConnectionsCollection load(Gson gson) throws IOException {

        log.info("Start to load connections from '{}/{}'", DefaultSettings.INSTANCE.user.getDirectory(), CONNECTIONS_JSON_FILENAME);

        SerializableConnectionsCollection serializableConnectionsCollection =
                gson.fromJson(new FileReader(new File(DefaultSettings.INSTANCE.user.getDirectory(), CONNECTIONS_JSON_FILENAME)), SerializableConnectionsCollection.class);
        ConnectionsCollection connectionsCollection = getConnectionsCollection(gson, serializableConnectionsCollection);
        log.info("{} connections loaded", connectionsCollection.connections.size());

        return connectionsCollection;
    }


    private SerializableConnectionsCollection getSerializableConnectionsCollection() {

        return new SerializableConnectionsCollection(new ArrayList<>(this.connections));
    }


    private static ConnectionsCollection getConnectionsCollection(Gson gson, SerializableConnectionsCollection serializableConnectionsCollection) {

        return new ConnectionsCollection(gson, serializableConnectionsCollection.connections);
    }


    @Override
    public Iterator<Connection> iterator() {

        return this.connections.iterator();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SerializableConnectionsCollection implements Serializable {

        private List<Connection> connections;
    }
}
