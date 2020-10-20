package org.arnaudlt.warthog.model.dataset;

import org.arnaudlt.warthog.model.dataset.transformation.Join;
import org.arnaudlt.warthog.model.dataset.transformation.SelectNamedColumn;
import org.arnaudlt.warthog.model.dataset.transformation.WhereClause;

import java.util.List;

public class Transformation {


    private final List<SelectNamedColumn> selectNamedColumns;

    private final List<WhereClause> whereClauses;

    private final Join join;


    public Transformation(List<SelectNamedColumn> selectNamedColumns, List<WhereClause> whereClauses, Join join) {

        this.selectNamedColumns = selectNamedColumns;
        this.whereClauses = whereClauses;
        this.join = join;
    }


    public List<SelectNamedColumn> getSelectNamedColumns() {

        return selectNamedColumns;
    }


    public List<WhereClause> getWhereClauses() {

        return whereClauses;
    }


    public Join getJoin() {

        return join;
    }
}
