package org.arnaudlt.warthog.model.setting;

import com.google.gson.Gson;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Slf4j
@Setter
public class SqlHistoryCollection implements Iterable<SqlHistoryCollection.SqlHistory> {

    private static final String SQL_HISTORY_JSON_FILENAME = "sql-history.json";

    private Gson gson;

    private String userDirectory;

    private Deque<SqlHistory> sqlHistory;


    public SqlHistoryCollection(Gson gson, String userDirectory) {

        this.gson = gson;
        this.userDirectory = userDirectory;
        this.sqlHistory = new ArrayDeque<>();
    }


    public SqlHistoryCollection(Gson gson, String userDirectory, List<SqlHistory> sqlHistory) {

        this.gson = gson;
        this.userDirectory = userDirectory;
        this.sqlHistory = new ArrayDeque<>(sqlHistory);
    }


    public void addToSqlHistory(SqlHistory sqlHistory) {

        if (this.sqlHistory.size() >= 10) {

            this.sqlHistory.removeLast();
        }
        this.sqlHistory.addFirst(sqlHistory);
    }


    public void persist() throws IOException {

        log.info("Try to delete the '{}/{}'", userDirectory, SQL_HISTORY_JSON_FILENAME);
        Files.deleteIfExists(Paths.get(userDirectory, SQL_HISTORY_JSON_FILENAME));

        log.info("Start to write Sql history in '{}/{}'", userDirectory, SQL_HISTORY_JSON_FILENAME);

        SerializableSqlHistoryCollection serializableSqlHistoryCollection = this.getSerializableSqlHistoryCollection();
        String sqlHistoryJson = gson.toJson(serializableSqlHistoryCollection);

        Files.createDirectories(Paths.get(userDirectory));
        Files.writeString(Paths.get(userDirectory, SQL_HISTORY_JSON_FILENAME), sqlHistoryJson, StandardOpenOption.CREATE);

        log.info("Sql history written : {}", this);
    }


    public static SqlHistoryCollection load(Gson gson, String userDirectory) throws FileNotFoundException {

        log.info("Start to load Sql history from '{}/{}'", userDirectory, SQL_HISTORY_JSON_FILENAME);

        SerializableSqlHistoryCollection serializableSqlHistoryCollection =
                gson.fromJson(new FileReader(new File(userDirectory, SQL_HISTORY_JSON_FILENAME)), SerializableSqlHistoryCollection.class);
        SqlHistoryCollection sqlHistoryCollection = getSqlHistoryCollection(gson, userDirectory, serializableSqlHistoryCollection);

        log.info("{} Sql queries in history read", sqlHistoryCollection.sqlHistory.size());
        return sqlHistoryCollection;
    }


    @Override
    public Iterator<SqlHistory> iterator() {

        return this.sqlHistory.iterator();
    }


    private SerializableSqlHistoryCollection getSerializableSqlHistoryCollection() {

        return new SerializableSqlHistoryCollection(new ArrayList<>(this.sqlHistory));
    }


    private static SqlHistoryCollection getSqlHistoryCollection(Gson gson, String userDirectory, SerializableSqlHistoryCollection serializableSqlHistoryCollection) {

        return new SqlHistoryCollection(gson, userDirectory, serializableSqlHistoryCollection.sqlHistory);
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SerializableSqlHistoryCollection implements Serializable {

        private List<SqlHistory> sqlHistory;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SqlHistory implements Serializable {

        private long timestamp;

        private String sqlQuery;

    }

}
