package org.arnaudlt.warthog.model.dataset;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.warthog.model.database.DatabaseSettings;
import org.arnaudlt.warthog.model.dataset.transformation.SelectNamedColumn;
import org.arnaudlt.warthog.model.dataset.transformation.WhereClause;
import org.arnaudlt.warthog.model.exception.ProcessingException;
import org.arnaudlt.warthog.model.util.FileUtil;
import org.arnaudlt.warthog.model.util.UniqueIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NamedDatasetManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedDatasetManager.class);

    private final SparkSession spark;

    private final DatabaseSettings databaseSettings;

    private final UniqueIdGenerator uniqueIdGenerator;

    private final ObservableList<NamedDataset> observableNamedDatasets;


    @Autowired
    public NamedDatasetManager(SparkSession spark, DatabaseSettings databaseSettings, UniqueIdGenerator uniqueIdGenerator) {

        this.spark = spark;
        this.databaseSettings = databaseSettings;
        this.uniqueIdGenerator = uniqueIdGenerator;
        this.observableNamedDatasets = FXCollections.synchronizedObservableList(FXCollections.observableArrayList(new ArrayList<>()));
    }


    public NamedDataset createNamedDataset(File file) {

        try {

            String fileType = FileUtil.getFileType(file);
            String separator = ";";
            double sizeInMegaBytes = FileUtil.getSizeInMegaBytes(file);

            Dataset<Row> dataset;
            switch (fileType) {
                case "csv":
                    separator = FileUtil.inferSeparator(file);
                    dataset = this.spark
                            .read()
                            .option("header", true)
                            .option("inferSchema", "true")
                            .option("sep", separator)
                            .csv(file.getAbsolutePath());
                    break;
                case "json":
                    dataset = this.spark
                            .read()
                            .json(file.getAbsolutePath());
                    break;
                case "parquet":
                    dataset = this.spark
                            .read()
                            .parquet(file.getAbsolutePath());
                    break;
                case "orc":
                    dataset = this.spark
                            .read()
                            .orc(file.getAbsolutePath());
                    break;
                default:
                    throw new ProcessingException(String.format("Not able to read %s file type", fileType));
            }
            Catalog catalog = buildCatalog(dataset);

            List<SelectNamedColumn> selectNamedColumns = catalog.getColumns().stream()
                    .map(nc -> new SelectNamedColumn(nc.getId(), nc.getName(), nc.getType()))
                    .collect(Collectors.toList());

            List<WhereClause> whereNamedColumns = new ArrayList<>();

            Transformation transformation = new Transformation(selectNamedColumns, whereNamedColumns);

            return new NamedDataset(this.uniqueIdGenerator.getUniqueId(),
                    file.getName(), dataset, catalog, transformation, new Decoration(file.toPath(), sizeInMegaBytes, separator));

        } catch (Exception e) {

            throw new ProcessingException(String.format("Not able to create the named dataset from %s", file),e);
        }
    }


    public void registerNamedDataset(NamedDataset namedDataset) {

        if (namedDataset == null) {

            throw new ProcessingException("Cannot register a null named dataset");
        } else if (this.observableNamedDatasets.contains(namedDataset)) {

            throw new ProcessingException(String.format("A named dataset has already been registered with the same name; unable to add %s",
                    namedDataset.getName()));
        } else {

            this.observableNamedDatasets.add(namedDataset);
            try {
                namedDataset.getDataset().createTempView(namedDataset.getViewName());
            } catch (AnalysisException e) {

                throw new ProcessingException(String.format("Unable to create temporary view, the name %s is invalid or already exists", namedDataset.getName()),
                        e);
            }
            LOGGER.info("Named dataset {} registered (view : `{}`)", namedDataset.getName(), namedDataset.getViewName());
        }
    }


    public void deregisterNamedDataset(NamedDataset namedDataset) {

        if (namedDataset == null) {

            throw new ProcessingException("Cannot deregister a null named dataset");
        } else if (this.observableNamedDatasets.contains(namedDataset)) {

            this.observableNamedDatasets.remove(namedDataset);
            this.spark.catalog().dropTempView(namedDataset.getViewName());
            LOGGER.info("Deregister the named dataset {}", namedDataset.getName());
        } else {

            LOGGER.error("The named dataset {} is not register - cannot be deregistered", namedDataset.getName());
            throw new ProcessingException(String.format("The named dataset %s is not register - cannot be deregistered", namedDataset.getName()));
        }
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


    public Dataset<Row> generateRowOverview(String sqlQuery) {

        return this.spark.sqlContext().sql(sqlQuery);
    }


    public void exportToCsv(NamedDataset namedDataset, String filePath) {

        Dataset<Row> output = namedDataset.applyTransformation();
        output
                .coalesce(1)
                .write()
                .option("sep", namedDataset.getDecoration().getSeparator())
                .option("header", true)
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .csv(filePath);
    }


    public void exportToCsv(String sqlQuery, String filePath) {

        Dataset<Row> output = this.spark.sqlContext().sql(sqlQuery);
        output
                .coalesce(1)
                .write()
                .option("sep", ";")
                .option("header", true)
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .csv(filePath);
    }


    public void exportToDatabase(String sqlQuery) {

        Dataset<Row> output = this.spark.sqlContext().sql(sqlQuery);

        output
                .write()
                .mode(SaveMode.valueOf(databaseSettings.getSaveMode()))
                .jdbc(databaseSettings.getUrl(), databaseSettings.getTable(), databaseSettings.getProperties());
    }


    public void exportToDatabase(NamedDataset namedDataset) {

        Dataset<Row> output = namedDataset.applyTransformation();

        output
                .write()
                .mode(SaveMode.valueOf(databaseSettings.getSaveMode()))
                .jdbc(databaseSettings.getUrl(), databaseSettings.getTable(), databaseSettings.getProperties());
    }

}
