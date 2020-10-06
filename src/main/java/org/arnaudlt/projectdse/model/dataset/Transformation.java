package org.arnaudlt.projectdse.model.dataset;

import org.arnaudlt.projectdse.model.dataset.transformation.SelectNamedColumn;
import org.arnaudlt.projectdse.model.dataset.transformation.WhereNamedColumn;

import java.util.List;

public class Transformation {


    private final List<SelectNamedColumn> selectNamedColumns;

    private final List<WhereNamedColumn> whereNamedColumns;


    public Transformation(List<SelectNamedColumn> selectNamedColumns, List<WhereNamedColumn> whereNamedColumns) {

        this.selectNamedColumns = selectNamedColumns;
        this.whereNamedColumns = whereNamedColumns;
    }


    public List<SelectNamedColumn> getSelectNamedColumns() {

        return selectNamedColumns;
    }


    public List<WhereNamedColumn> getWhereNamedColumns() {

        return whereNamedColumns;
    }
}
