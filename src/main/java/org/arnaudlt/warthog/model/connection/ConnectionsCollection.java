package org.arnaudlt.warthog.model.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
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


    private ObservableList<Connection> connections;


    public ConnectionsCollection() {
        this.connections = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(new ArrayList<>()));
    }


    public ConnectionsCollection(List<Connection> connections) {
        this.connections = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(connections));
    }


    public void persist() throws IOException {

        log.info("Try to delete the 'connections.json'");
        new File("connections.json").delete();

        log.info("Start to write connections in 'connections.json'");

        SerializableConnectionsCollection serializableConnectionsCollection = this.getSerializableConnectionsCollection();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String connectionsJson = gson.toJson(serializableConnectionsCollection);
        Files.writeString(Paths.get("connections.json"), connectionsJson, StandardOpenOption.CREATE);

        log.info("Connections written in 'connections.json'");
    }


    public static ConnectionsCollection load() throws IOException {

        log.info("Start to load connections from 'connections.json'");
        Gson gson = new GsonBuilder().create();

        SerializableConnectionsCollection serializableConnectionsCollection =
                gson.fromJson(new FileReader("connections.json"), SerializableConnectionsCollection.class);
        ConnectionsCollection connectionsCollection = getConnectionsCollection(serializableConnectionsCollection);
        log.info("{} connections loaded", connectionsCollection.getConnections().size());

        return connectionsCollection;
    }


    private SerializableConnectionsCollection getSerializableConnectionsCollection() {

        return new SerializableConnectionsCollection(new ArrayList<>(this.connections));
    }


    private static ConnectionsCollection getConnectionsCollection(SerializableConnectionsCollection serializableConnectionsCollection) {

        return new ConnectionsCollection(serializableConnectionsCollection.connections);
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
