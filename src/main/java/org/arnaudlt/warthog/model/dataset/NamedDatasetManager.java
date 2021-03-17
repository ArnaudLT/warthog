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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        this.observableNamedDatasets = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(new ArrayList<>()));
    }


    public NamedDataset createNamedDataset(File file) {

        try {

            Format fileType = FileUtil.getFileType(file);
            String separator = ";";
            double sizeInMegaBytes = FileUtil.getSizeInMegaBytes(file);

            Dataset<Row> dataset;
            switch (fileType) {
                case CSV:
                    separator = FileUtil.inferSeparator(file);
                    dataset = this.spark
                            .read()
                            .option("header", true)
                            .option("inferSchema", "true")
                            .option("sep", separator)
                            .csv(file.getAbsolutePath());
                    break;
                case JSON:
                    dataset = this.spark
                            .read()
                            .json(file.getAbsolutePath());
                    break;
                case PARQUET:
                    dataset = this.spark
                            .read()
                            .parquet(file.getAbsolutePath());
                    break;
                case ORC:
                    dataset = this.spark
                            .read()
                            .orc(file.getAbsolutePath());
                    break;
                default:
                    throw new ProcessingException(String.format("Not able to read %s file type", fileType));
            }
            Catalog catalog = buildCatalog(dataset);
            Transformation transformation = buildTransformation(catalog);

            return new NamedDataset(this.uniqueIdGenerator.getUniqueId(),
                    file.getName(), dataset, catalog, transformation, new Decoration(file.toPath(), sizeInMegaBytes, separator));

        } catch (Exception e) {

            throw new ProcessingException(String.format("Not able to create the named dataset from %s", file),e);
        }
    }


    public NamedDataset createNamedDataset(Connection databaseConnection, String tableName) {

        Dataset<Row> dataset = this.spark
                .read()
                .jdbc(databaseConnection.getDatabaseUrl(), tableName, databaseConnection.getDatabaseProperties());

        Catalog catalog = buildCatalog(dataset);
        Transformation transformation = buildTransformation(catalog);

        return new NamedDataset(this.uniqueIdGenerator.getUniqueId(), tableName, dataset, catalog, transformation,
                new Decoration(null, 0, ""));
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
