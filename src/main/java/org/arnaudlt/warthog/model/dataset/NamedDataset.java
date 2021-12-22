package org.arnaudlt.warthog.model.dataset;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Objects;


@Slf4j
public class NamedDataset {


    private final int id;

    private final String name;

    private final Dataset<Row> dataset;

    private final Catalog catalog;

    private final Decoration decoration;

    private String localTemporaryViewName;


    public NamedDataset(int id, String name, Dataset<Row> dataset, Catalog catalog, Decoration decoration) {

        this.id = id;
        this.name = name;
        this.dataset = dataset;
        this.catalog = catalog;
        this.decoration = decoration;
    }


    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public Dataset<Row> getDataset() {
        return dataset;
    }


    public Catalog getCatalog() {
        return catalog;
    }


    public Decoration getDecoration() {
        return decoration;
    }


    public void setLocalTemporaryViewName(String localTemporaryViewName) {
        this.localTemporaryViewName = localTemporaryViewName;
    }


    public String getLocalTemporaryViewName() {

        return localTemporaryViewName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedDataset that = (NamedDataset) o;
        return id == that.id;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

}
