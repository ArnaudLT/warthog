package org.arnaudlt.warthog.model.dataset;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.decoration.DatabaseDecoration;
import org.arnaudlt.warthog.model.dataset.decoration.LocalDecoration;
import org.arnaudlt.warthog.model.exception.ProcessingException;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.setting.ImportDirectorySettings;
import org.arnaudlt.warthog.model.util.FileUtil;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.model.util.UniqueIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
public class NamedDatasetManager {

    private final SparkSession spark;

    private final UniqueIdGenerator uniqueIdGenerator;

    private final ObservableList<NamedDataset> observableNamedDatasets;


    @Autowired
    public NamedDatasetManager(SparkSession spark, UniqueIdGenerator uniqueIdGenerator) {

        this.spark = spark;
        this.uniqueIdGenerator = uniqueIdGenerator;
        this.observableNamedDatasets = FXCollections.synchronizedObservableList(
                FXCollections.observableArrayList(new ArrayList<>()));
    }


    public NamedDataset createNamedDataset(File file) {

        Path basePath;
        String preferredName;
        List<Path> filePaths;
        if (file.isDirectory()) {

            basePath = file.toPath();
            preferredName = file.getName();
            try (Stream<Path> walk = Files.walk(file.toPath(), FileVisitOption.FOLLOW_LINKS)) {

                filePaths = walk
                        .filter(path -> !path.toFile().isDirectory())
                        .filter(path -> !path.toFile().isHidden())
                        .toList();
            } catch (IOException e) {

                throw new ProcessingException(String.format("Not able to scan directory %s", file.getName()), e);
            }
        } else {

            basePath = file.toPath().getParent();
            preferredName = file.getName();
            filePaths = List.of(file.toPath());
        }

        Format fileType = FileUtil.determineFormat(filePaths);
        String separator = FileUtil.inferSeparator(fileType, filePaths);

        ImportDirectorySettings importDirectorySettings = new ImportDirectorySettings(
            filePaths, fileType, preferredName, separator, basePath);

        return createNamedDataset(importDirectorySettings);
    }



    public NamedDataset createNamedDataset(ImportDirectorySettings importDirectorySettings) {

        Dataset<Row> dataset = switch (importDirectorySettings.getFormat()) {
            case CSV -> spark.read()
                    .option("header", importDirectorySettings.getHeader())
                    .option("inferSchema", "true")
                    .option("sep", importDirectorySettings.getSeparator())
                    .option("basePath", importDirectorySettings.getBasePath())
                    .csv(importDirectorySettings.getFilePath());
            case JSON -> spark.read()
                    .option("basePath", importDirectorySettings.getBasePath())
                    .option("multiline", importDirectorySettings.getMultiLine())
                    .json(importDirectorySettings.getFilePath());
            case PARQUET -> spark.read()
                    .option("basePath", importDirectorySettings.getBasePath())
                    .parquet(importDirectorySettings.getFilePath());
            case ORC -> spark.read()
                    .option("basePath", importDirectorySettings.getBasePath())
                    .orc(importDirectorySettings.getFilePath());
            case AVRO -> spark.read()
                    .option("basePath", importDirectorySettings.getBasePath())
                    .format("avro")
                    .load(importDirectorySettings.getFilePath());
        };

        String name = determineName(Paths.get(importDirectorySettings.getBasePath()), importDirectorySettings.getName());
        LocalDecoration decoration = buildDecoration(importDirectorySettings, dataset);

        return new NamedDataset(
                this.uniqueIdGenerator.getUniqueId(),
                name,
                dataset,
                decoration);
    }


    private LocalDecoration buildDecoration(ImportDirectorySettings importDirectorySettings, Dataset<Row> dataset) {

        List<Path> partPaths = Arrays.stream(dataset.inputFiles())
                .map(file -> Paths.get(URI.create(file)))
                .toList();

        Double sizeInMegaBytes = FileUtil.getSizeInMegaBytes(partPaths);

        List<String> parts = partPaths.stream()
                .map(path -> path.getFileName().toString())
                .toList();

        return new LocalDecoration(
                importDirectorySettings.getBasePath(),
                parts,
                importDirectorySettings.getFormat(),
                sizeInMegaBytes
                );
    }


    private String determineName(Path basePath, String preferredName) {

        if (preferredName != null && !preferredName.isBlank()) {
            return preferredName;
        } else {
            return basePath.getFileName().toString();
        }
    }


    public NamedDataset createNamedDataset(Connection databaseConnection, String tableName) {

        Dataset<Row> dataset = this.spark
                .read()
                .jdbc(databaseConnection.getDatabaseUrl(), tableName, databaseConnection.getDatabaseProperties());

        return new NamedDataset(this.uniqueIdGenerator.getUniqueId(), tableName, dataset,
                new DatabaseDecoration(databaseConnection.getName(), tableName));
    }


    public void registerNamedDataset(NamedDataset namedDataset) {

        if (namedDataset == null) {

            throw new ProcessingException("Cannot register a null named dataset");
        } else if (this.observableNamedDatasets.contains(namedDataset)) {

            throw new ProcessingException(String.format("A named dataset has already been registered with the same id - unable to add %s",
                    namedDataset.getName()));
        } else {

            this.observableNamedDatasets.add(namedDataset);
            String localTempViewName;

            try {
                // Try to create the view with the dataset's name...
                localTempViewName = replaceForbiddenCharacters(namedDataset.getName());
                namedDataset.getDataset().createTempView(localTempViewName);
            } catch (AnalysisException e) {

                try {
                    // It failed with the name, let's go with an auto generated name.
                    localTempViewName = generatedTemporaryViewName(namedDataset);
                    namedDataset.getDataset().createTempView(localTempViewName);
                } catch (AnalysisException e2) {

                    throw new ProcessingException(
                            String.format("Unable to create temporary view, the name %s is invalid or already exists", namedDataset.getLocalTemporaryViewName()), e2);
                }
            }
            namedDataset.setLocalTemporaryViewName(localTempViewName);
            log.info("Named dataset {} registered (view : `{}`)", namedDataset.getName(), namedDataset.getLocalTemporaryViewName());
        }
    }


    public void tryRenameTempView(NamedDataset namedDataset, String name) throws AnalysisException {

        namedDataset.getDataset().createTempView(name);
        this.spark.catalog().dropTempView(namedDataset.getLocalTemporaryViewName());
        namedDataset.setLocalTemporaryViewName(name);
    }


    public void deregisterNamedDataset(NamedDataset namedDataset) {

        if (namedDataset == null) {

            throw new ProcessingException("Cannot deregister a null named dataset");
        } else if (this.observableNamedDatasets.contains(namedDataset)) {

            this.observableNamedDatasets.remove(namedDataset);
            this.spark.catalog().dropTempView(namedDataset.getLocalTemporaryViewName());
            log.info("Deregister the named dataset {}", namedDataset.getName());
        } else {

            log.error("The named dataset {} is not register - cannot be deregistered", namedDataset.getName());
            throw new ProcessingException(String.format("The named dataset %s is not register - cannot be deregistered", namedDataset.getName()));
        }
    }


    private String generatedTemporaryViewName(NamedDataset namedDataset) {

        return replaceForbiddenCharacters(namedDataset.getName()) + "_" + namedDataset.getId();
    }


    private String replaceForbiddenCharacters(String datasetName) {

        return datasetName.trim()
                .replace(".", "_")
                .replace(" ", "_")
                .replace("-", "_")
                .replace("=", "_");
    }


    public ObservableList<NamedDataset> getObservableNamedDatasets() {
        return observableNamedDatasets;
    }


    public Dataset<Row> prepareDataset(String sqlQuery) {

        return this.spark.sqlContext().sql(sqlQuery);
    }


    public void export(String sqlQuery, ExportFileSettings exportFileSettings) {

        Dataset<Row> output = this.spark.sqlContext().sql(sqlQuery);
        export(output, exportFileSettings);
    }


    public void export(Dataset<Row> output, ExportFileSettings exportFileSettings) {

        DataFrameWriter<Row> dfw = output
                .repartition(exportFileSettings.repartition())
                .write()
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .mode(exportFileSettings.saveMode());

        if (!exportFileSettings.partitionBy().isBlank()) {

            dfw = dfw.partitionBy(
                    Arrays.stream(exportFileSettings.partitionBy().split(",", -1))
                            .map(String::trim)
                            .toArray(String[]::new));
        }

        final Format format = exportFileSettings.format();

        switch (format) {
            case CSV -> dfw
                    .option("sep", exportFileSettings.separator())
                    .option("header", exportFileSettings.header())
                    .csv(exportFileSettings.filePath());
            case JSON -> dfw
                    .json(exportFileSettings.filePath());
            case PARQUET -> dfw
                    .option("compression", exportFileSettings.compression().getLabel())
                    .parquet(exportFileSettings.filePath());
            case ORC -> dfw
                    .orc(exportFileSettings.filePath());
            case AVRO -> dfw
                    .format("avro")
                    .save(exportFileSettings.filePath());
        }
    }


    public void exportToDatabase(String sqlQuery, Connection databaseConnection, ExportDatabaseSettings exportDatabaseSettings) {

        Dataset<Row> output = this.spark.sqlContext().sql(sqlQuery);

        output
                .write()
                .mode(SaveMode.valueOf(exportDatabaseSettings.saveMode()))
                .jdbc(databaseConnection.getDatabaseUrl(), exportDatabaseSettings.tableName(), databaseConnection.getDatabaseProperties());
    }

}
