package org.arnaudlt.warthog.model.dataset;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.warthog.model.connection.Connection;
import org.arnaudlt.warthog.model.dataset.transformation.SelectNamedColumn;
import org.arnaudlt.warthog.model.dataset.transformation.WhereClause;
import org.arnaudlt.warthog.model.exception.ProcessingException;
import org.arnaudlt.warthog.model.setting.ExportDatabaseSettings;
import org.arnaudlt.warthog.model.setting.ExportFileSettings;
import org.arnaudlt.warthog.model.util.FileUtil;
import org.arnaudlt.warthog.model.util.Format;
import org.arnaudlt.warthog.model.util.UniqueIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
                        .collect(Collectors.toList());
            } catch (IOException e) {

                throw new ProcessingException(String.format("Not able to scan directory %s", file.getName()), e);
            }
        } else {

            basePath = file.toPath().getParent();
            preferredName = file.getName();
            filePaths = List.of(file.toPath());
        }
        return createNamedDataset(basePath, filePaths, preferredName);
    }


    public NamedDataset createNamedDataset(Path basePath, List<Path> filePaths, String preferredName) {

        Format fileType = FileUtil.getFileType(filePaths);
        Dataset<Row> dataset;
        String[] filesToLoad = filePaths.stream().map(Path::toString).toArray(String[]::new);
        switch (fileType) {

            case CSV:
                String separator = FileUtil.inferSeparator(filePaths);
                dataset = spark.read()
                        .option("header", true)
                        .option("inferSchema", "true")
                        .option("sep", separator)
                        .option("basePath", basePath.toString())
                        .csv(filesToLoad);
                break;
            case JSON:
                dataset = spark.read()
                        .option("basePath", basePath.toString())
                        .json(filesToLoad);
                break;
            case PARQUET:
                dataset = spark.read()
                        .option("basePath", basePath.toString())
                        .parquet(filesToLoad);
                break;
            case ORC:
                dataset = spark.read()
                        .option("basePath", basePath.toString())
                        .orc(filesToLoad);
                break;
            case AVRO:
                dataset = spark.read()
                        .option("basePath", basePath.toString())
                        .format("avro")
                        .load(filesToLoad);
                break;
            default:
                throw new ProcessingException(String.format("Not able to read %s type", fileType));
        }

        Catalog catalog = buildCatalog(dataset);
        Transformation transformation = buildTransformation(catalog);
        String name = determineName(basePath, preferredName);
        Decoration decoration = buildDecoration(fileType, basePath.toString(), filePaths);

        return new NamedDataset(
                this.uniqueIdGenerator.getUniqueId(),
                name,
                dataset,
                catalog,
                transformation,
                decoration);
    }


    private Decoration buildDecoration(Format fileType, String basePath, List<Path> filePaths) {

        List<String> parts = filePaths.stream()
                .map(Path::toString)
                .filter(file -> file.toLowerCase().endsWith(fileType.name().toLowerCase()))
                .map(file -> file.startsWith(basePath) ? file.replace(basePath, "") : file)
                .collect(Collectors.toList());

        Double sizeInMegaBytes = FileUtil.getSizeInMegaBytes(filePaths);

        return new Decoration(fileType, basePath, parts, sizeInMegaBytes);
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

        Catalog catalog = buildCatalog(dataset);
        Transformation transformation = buildTransformation(catalog);

        return new NamedDataset(this.uniqueIdGenerator.getUniqueId(), tableName, dataset, catalog, transformation,
                new Decoration(null, databaseConnection.getName(), List.of(tableName), null));
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
                localTempViewName = namedDataset.getName();
                namedDataset.getDataset().createTempView(localTempViewName);
            } catch (AnalysisException e) {

                try {
                    // It failed with the name, let's go with an auto generated name.
                    localTempViewName = getAutoGeneratedTemporaryViewName(namedDataset);
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


    private String getAutoGeneratedTemporaryViewName(NamedDataset namedDataset) {

        return namedDataset.getName().trim()
                .replace(".", "_")
                .replace(" ", "_")
                .replace("-", "_")
                .replace("=", "_")
                + "_" + namedDataset.getId();
    }


    public ObservableList<NamedDataset> getObservableNamedDatasets() {
        return observableNamedDatasets;
    }


    private Catalog buildCatalog(Dataset<Row> dataset) {

        List<StructField> fields = List.of(dataset.schema().fields());
        List<NamedColumn> columns = fields.stream()
                .map(field -> new NamedColumn(this.uniqueIdGenerator.getUniqueId(), field.name(), field.dataType().typeName()))
                .collect(Collectors.toList());
        return new Catalog(columns);
    }


    private Transformation buildTransformation(Catalog catalog) {

        List<SelectNamedColumn> selectNamedColumns = catalog.getColumns().stream()
                .map(nc -> new SelectNamedColumn(nc.getId(), nc.getName(), nc.getType()))
                .collect(Collectors.toList());

        List<WhereClause> whereNamedColumns = new ArrayList<>();

        return new Transformation(selectNamedColumns, whereNamedColumns);
    }


    public Dataset<Row> prepareDataset(String sqlQuery) {

        return this.spark.sqlContext().sql(sqlQuery);
    }


    public void export(String sqlQuery, ExportFileSettings exportFileSettings) {

        Dataset<Row> output = this.spark.sqlContext().sql(sqlQuery);
        export(output, exportFileSettings);
    }


    public void export(NamedDataset namedDataset, ExportFileSettings exportFileSettings) {

        Dataset<Row> output = namedDataset.applyTransformation();
        export(output, exportFileSettings);
    }


    public void export(Dataset<Row> output, ExportFileSettings exportFileSettings) {

        DataFrameWriter<Row> dfw = output
                .coalesce(1)
                .write()
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .mode(exportFileSettings.getSaveMode());

        if (!exportFileSettings.getPartitionBy().isBlank()) {

            dfw = dfw.partitionBy(
                    Arrays.stream(exportFileSettings.getPartitionBy().split(",", -1))
                            .map(String::trim)
                            .toArray(String[]::new));
        }

        final Format format = exportFileSettings.getFormat();

        switch (format) {
            case CSV:
                dfw
                        .option("sep", exportFileSettings.getSeparator())
                        .option("header", exportFileSettings.getHeader())
                        .csv(exportFileSettings.getFilePath());
                break;
            case JSON:
                dfw
                        .json(exportFileSettings.getFilePath());
                break;
            case PARQUET:
                dfw
                        .parquet(exportFileSettings.getFilePath());
                break;
            case ORC:
                dfw
                        .orc(exportFileSettings.getFilePath());
                break;
            case AVRO:
                dfw
                        .format("avro")
                        .save(exportFileSettings.getFilePath());
                break;
        }

    }


    public void exportToDatabase(String sqlQuery, Connection databaseConnection, ExportDatabaseSettings exportDatabaseSettings) {

        Dataset<Row> output = this.spark.sqlContext().sql(sqlQuery);

        output
                .write()
                .mode(SaveMode.valueOf(exportDatabaseSettings.getSaveMode()))
                .jdbc(databaseConnection.getDatabaseUrl(), exportDatabaseSettings.getTableName(), databaseConnection.getDatabaseProperties());
    }


    public void exportToDatabase(NamedDataset namedDataset, Connection databaseConnection, ExportDatabaseSettings exportDatabaseSettings) {

        Dataset<Row> output = namedDataset.applyTransformation();

        output
                .write()
                .mode(SaveMode.valueOf(exportDatabaseSettings.getSaveMode()))
                .jdbc(databaseConnection.getDatabaseUrl(), exportDatabaseSettings.getTableName(), databaseConnection.getDatabaseProperties());
    }

}
