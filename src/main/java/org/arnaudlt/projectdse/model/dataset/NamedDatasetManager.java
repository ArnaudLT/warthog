package org.arnaudlt.projectdse.model.dataset;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.projectdse.model.dataset.transformation.Join;
import org.arnaudlt.projectdse.model.dataset.transformation.SelectNamedColumn;
import org.arnaudlt.projectdse.model.dataset.transformation.WhereNamedColumn;
import org.arnaudlt.projectdse.model.exception.ProcessingException;
import org.arnaudlt.projectdse.model.util.FileUtil;
import org.arnaudlt.projectdse.model.util.UniqueIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Component
public class NamedDatasetManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedDatasetManager.class);

    private final SparkSession spark;

    private final UniqueIdGenerator uniqueIdGenerator;

    private final ConcurrentMap<Integer, NamedDataset> namedDatasets;

    // TODO can we find a better way ? (tracking with an observable the content of the namedDatasets map)
    private final ObservableList<NamedDataset> observableNamedDatasets;


    @Autowired
    public NamedDatasetManager(SparkSession spark, UniqueIdGenerator uniqueIdGenerator) {

        this.spark = spark;
        this.uniqueIdGenerator = uniqueIdGenerator;
        this.namedDatasets = new ConcurrentHashMap<>();
        this.observableNamedDatasets = FXCollections.observableArrayList();
    }


    public NamedDataset createNamedDataset(File file) {

        try {

            String fileType = FileUtil.getFileType(file.getName());
            String name = FileUtil.determineName(file.getName());
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
                default:
                    throw new ProcessingException(String.format("Not able to read %s file type", fileType));
            }
            Catalog catalog = buildCatalog(dataset);

            List<SelectNamedColumn> selectNamedColumns = catalog.getColumns().stream()
                    .map(nc -> new SelectNamedColumn(nc.getId(), nc.getName(), nc.getType()))
                    .collect(Collectors.toList());

            List<WhereNamedColumn> whereNamedColumns = catalog.getColumns().stream()
                    .map(nc -> new WhereNamedColumn(nc.getId(), nc.getName(), nc.getType()))
                    .collect(Collectors.toList());

            Join join = new Join();

            Transformation transformation = new Transformation(selectNamedColumns, whereNamedColumns, join);

            return new NamedDataset(this.uniqueIdGenerator.getUniqueId(),
                    name, dataset, catalog, transformation, new Decoration(sizeInMegaBytes, separator));

        } catch (Exception e) {

            throw new ProcessingException(String.format("Not able to create the named dataset from %s", file),e);
        }
    }


    public void registerNamedDataset(NamedDataset namedDataset) {

        if (namedDataset == null) {

            throw new ProcessingException("Cannot register a null named dataset");
        } else if (this.namedDatasets.containsKey(namedDataset.getId())) {

            throw new ProcessingException(String.format("A named dataset has already been registered with the same name; unable to add %s",
                    namedDataset.getName()));
        } else {

            this.namedDatasets.put(namedDataset.getId(), namedDataset);
            this.observableNamedDatasets.add(namedDataset);
            LOGGER.info("Named dataset {} registered", namedDataset.getName());
        }
    }


    public void deregisterNamedDataset(NamedDataset namedDataset) {

        if (namedDataset == null) {

            throw new ProcessingException("Cannot deregister a null named dataset");
        } else if (this.namedDatasets.containsKey(namedDataset.getId())) {

            this.namedDatasets.remove(namedDataset.getId());
            this.observableNamedDatasets.remove(namedDataset);
            LOGGER.info("Deregister the named dataset {}", namedDataset.getName());
        } else {

            LOGGER.error("The named dataset {} is not register - cannot be deregistered", namedDataset.getName());
            throw new ProcessingException(String.format("The named dataset %s is not register - cannot be deregistered", namedDataset.getName()));
        }
    }


    public ConcurrentMap<Integer, NamedDataset> getNamedDatasets() {

        return namedDatasets;
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


}
