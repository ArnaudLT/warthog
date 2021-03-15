package org.arnaudlt.warthog.model.connection;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Getter
@Setter
public class ConnectionsCollection implements Serializable, Iterable<Connection> {

    public static final long serialVersionUID = 7372913086816112179L;


    private List<Connection> connections;


    public ConnectionsCollection() {
        this.connections = new ArrayList<>();
    }

    public void persist() throws IOException {

        log.info("Try to delete the 'connections.ser'");
        new File("connections.ser").delete();

        log.info("Start to write connections in 'connections.ser'");
        try (FileOutputStream fos = new FileOutputStream("connections.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(this);
        }
        log.info("Connections written in 'connections.ser'");
    }


    public static ConnectionsCollection load() throws IOException, ClassNotFoundException {

        log.info("Start to load connections from 'connections.ser'");
        ConnectionsCollection connectionsCollection;
        try (FileInputStream fis = new FileInputStream("connections.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            connectionsCollection = (ConnectionsCollection) ois.readObject();
        }
        return connectionsCollection;
    }


    @Override
    public Iterator<Connection> iterator() {

        return this.connections.iterator();
    }
}
