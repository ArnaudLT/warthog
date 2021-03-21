package org.arnaudlt.warthog.model.connection;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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

    public static final long serialVersionUID = 7372913086816112179L;

    private String userDirectory;

    private Gson gson;

    private ObservableList<Connection> connections;


    public ConnectionsCollection(Gson gson, String userDirectory) {
        this.gson = gson;
        this.userDirectory = userDirectory;
        this.connections = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(new ArrayList<>()));
    }


    public ConnectionsCollection(Gson gson, String userDirectory, List<Connection> connections) {
        this.gson = gson;
        this.userDirectory = userDirectory;
        this.connections = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(connections));
    }


    public void persist() throws IOException {

        log.info("Try to delete the '{}/connections.json'", userDirectory);
        new File(userDirectory, "connections.json").delete();

        log.info("Start to write connections in '{}/connections.json'", userDirectory);

        SerializableConnectionsCollection serializableConnectionsCollection = this.getSerializableConnectionsCollection();
        String connectionsJson = gson.toJson(serializableConnectionsCollection);

        Files.createDirectories(Paths.get(userDirectory));
        Files.writeString(Paths.get(userDirectory, "connections.json"), connectionsJson, StandardOpenOption.CREATE);

        log.info("Connections written in '{}/connections.json'", userDirectory);
    }


    public static ConnectionsCollection load(Gson gson, String userDirectory) throws IOException {

        log.info("Start to load connections from '{}/connections.json'", userDirectory);

        SerializableConnectionsCollection serializableConnectionsCollection =
                gson.fromJson(new FileReader(new File(userDirectory, "connections.json")), SerializableConnectionsCollection.class);
        ConnectionsCollection connectionsCollection = getConnectionsCollection(gson, userDirectory, serializableConnectionsCollection);
        log.info("{} connections loaded", connectionsCollection.getConnections().size());

        return connectionsCollection;
    }


    private SerializableConnectionsCollection getSerializableConnectionsCollection() {

        return new SerializableConnectionsCollection(new ArrayList<>(this.connections));
    }


    private static ConnectionsCollection getConnectionsCollection(Gson gson, String userDirectory, SerializableConnectionsCollection serializableConnectionsCollection) {

        return new ConnectionsCollection(gson, userDirectory, serializableConnectionsCollection.connections);
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
