package org.arnaudlt.warthog.model.dataset;

import org.arnaudlt.warthog.model.dataset.transformation.SelectNamedColumn;
import org.arnaudlt.warthog.model.dataset.transformation.WhereClause;

import java.util.List;

public class Transformation {


    private final List<SelectNamedColumn> selectNamedColumns;

    private final List<WhereClause> whereClauses;


    public Transformation(List<SelectNamedColumn> selectNamedColumns, List<WhereClause> whereClauses) {

        this.selectNamedColumns = selectNamedColumns;
        this.whereClauses = whereClauses;
    }


    public List<SelectNamedColumn> getSelectNamedColumns() {

        return selectNamedColumns;
    }


    public List<WhereClause> getWhereClauses() {

        return whereClauses;
    }

}
