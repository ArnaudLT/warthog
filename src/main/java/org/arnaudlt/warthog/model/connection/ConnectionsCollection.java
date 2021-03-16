package org.arnaudlt.warthog.model.connection;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
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

        log.info("Try to delete the 'connections.ser'");
        new File("connections.ser").delete();

        log.info("Start to write connections in 'connections.ser'");
        try (FileOutputStream fos = new FileOutputStream("connections.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            SerializableConnectionsCollection serializableConnectionsCollection = this.getSerializableConnectionsCollection();
            oos.writeObject(serializableConnectionsCollection);
        }
        log.info("Connections written in 'connections.ser'");
    }


    public static ConnectionsCollection load() throws IOException, ClassNotFoundException {

        log.info("Start to load connections from 'connections.ser'");
        ConnectionsCollection connectionsCollection;
        try (FileInputStream fis = new FileInputStream("connections.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

             SerializableConnectionsCollection serializableConnectionsCollection = (SerializableConnectionsCollection) ois.readObject();
             connectionsCollection = getConnectionsCollection(serializableConnectionsCollection);
        }
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
