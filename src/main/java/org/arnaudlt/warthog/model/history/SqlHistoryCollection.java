package org.arnaudlt.warthog.model.history;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.warthog.model.user.DefaultSettings;
import org.arnaudlt.warthog.model.user.SqlHistorySettings;
import org.arnaudlt.warthog.model.util.FileUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Getter
@Setter
public class SqlHistoryCollection implements Iterable<SqlHistory> {


    private static final String SQL_HISTORY_FILE_EXTENSION = "json";

    private SqlHistorySettings sqlHistorySettings;

    private Gson gson;

    private ObservableList<SqlHistory> sqlQueries;


    public SqlHistoryCollection(Gson gson, SqlHistorySettings sqlHistory) {

        this.sqlHistorySettings = sqlHistory;
        this.gson = gson;
        this.sqlQueries = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(new ArrayList<>()));
    }


    public SqlHistoryCollection(Gson gson, SqlHistorySettings sqlHistory, List<SqlHistory> sqlQueries) {

        this.sqlHistorySettings = sqlHistory;
        this.gson = gson;
        this.sqlQueries = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(sqlQueries));
    }


    public static SqlHistoryCollection load(Gson gson, SqlHistorySettings sqlHistorySettings) throws IOException {

        log.info("Start to load sql history from '{}'", sqlHistorySettings.getDirectory());

        SerializableSqlHistoryCollection serializableSqlHistoryCollection = new SerializableSqlHistoryCollection(new ArrayList<>());

        try (Stream<Path> walk = Files.walk(Paths.get(sqlHistorySettings.getDirectory()), FileVisitOption.FOLLOW_LINKS)) {

            List<SqlHistory> sqlHistoryLoaded = walk
                    .filter(path -> !path.toFile().isDirectory())
                    .filter(path -> !path.toFile().isHidden())
                    .filter(path -> SQL_HISTORY_FILE_EXTENSION.equals(FileUtil.getLowerCaseExtension(path.getFileName().toString())))
                    .map(path -> {
                        SqlHistory sqlHistory = null;
                        try {
                            sqlHistory = gson.fromJson(new FileReader(path.toFile()), SqlHistory.class);
                        } catch (FileNotFoundException e) {
                            log.error("Unable to load sql query history from '"+ path +"'", e);
                        }
                        return sqlHistory;
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparingLong(SqlHistory::getTimestamp).reversed())
                    .limit(sqlHistorySettings.getSize())
                    .toList();

            serializableSqlHistoryCollection.setSqlQueries(sqlHistoryLoaded);
        }

        SqlHistoryCollection sqlHistoryCollection = getSqlHistoryCollection(gson, sqlHistorySettings, serializableSqlHistoryCollection);
        log.info("{} sql queries loaded in history", sqlHistoryCollection.sqlQueries.size());

        return sqlHistoryCollection;
    }


    private static SqlHistoryCollection getSqlHistoryCollection(Gson gson, SqlHistorySettings sqlHistorySettings, SerializableSqlHistoryCollection serializableSqlHistoryCollection) {

        return new SqlHistoryCollection(gson, sqlHistorySettings, serializableSqlHistoryCollection.sqlQueries);
    }


    public void persistOne(SqlHistory sqlHistory) {

        this.sqlQueries.add(0, sqlHistory);

        final String queryFileName = UUID.randomUUID() + "." + SQL_HISTORY_FILE_EXTENSION;
        sqlHistory.setFileName(queryFileName);
        String sqlQueryHistoryJson = gson.toJson(sqlHistory);
        try {
            Files.createDirectories(Paths.get(sqlHistorySettings.getDirectory()));
            Files.writeString(Paths.get(sqlHistorySettings.getDirectory(), queryFileName), sqlQueryHistoryJson, StandardOpenOption.CREATE);
            if (this.sqlQueries.size() > sqlHistorySettings.getSize()) {
                SqlHistory toBeDeleted = this.sqlQueries.remove(this.sqlQueries.size() - 1);
                Files.deleteIfExists(Paths.get(sqlHistorySettings.getDirectory(), toBeDeleted.getFileName()));
            }
        } catch (IOException e) {
            log.warn("Unable to save history", e);
        }
    }


    public void initializeHistoryDirectory() throws IOException {

        log.info("Initializing sql queries directory in '{}'", sqlHistorySettings.getDirectory());
        Files.createDirectories(Paths.get(sqlHistorySettings.getDirectory()));
    }


    @Override
    public Iterator<SqlHistory> iterator() {

        return this.sqlQueries.iterator();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SerializableSqlHistoryCollection implements Serializable {

        private List<SqlHistory> sqlQueries;
    }
}
