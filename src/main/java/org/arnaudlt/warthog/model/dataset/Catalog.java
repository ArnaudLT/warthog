package org.arnaudlt.warthog.model.dataset;


import java.util.List;
import java.util.Objects;

public class Catalog {


    private final List<NamedColumn> columns;


    public Catalog(List<NamedColumn> columns) {

        Objects.requireNonNull(columns);
        this.columns = columns;
    }


    public List<NamedColumn> getColumns() {
        return columns;
    }


}
