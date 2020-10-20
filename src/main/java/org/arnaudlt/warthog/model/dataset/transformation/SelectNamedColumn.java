package org.arnaudlt.warthog.model.dataset.transformation;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.StringExpression;
import org.arnaudlt.warthog.model.dataset.NamedColumn;

public class SelectNamedColumn extends NamedColumn {


    private BooleanExpression selected;

    private BooleanExpression groupBy;

    private StringExpression aggregateOperator;

    private StringExpression sortRank;

    private StringExpression sortType;



    public SelectNamedColumn(int id, String name, String type) {

        super(id, name, type);
    }


    public BooleanExpression selectedProperty() {
        return selected;
    }


    public void setSelected(BooleanExpression selected) {
        this.selected = selected;
    }


    public boolean isSelected() {
        return selected.get();
    }


    public String getAggregateOperator() {
        return aggregateOperator.get();
    }


    public StringExpression aggregateOperatorProperty() {
        return aggregateOperator;
    }


    public void setAggregateOperator(StringExpression aggregateOperator) {
        this.aggregateOperator = aggregateOperator;
    }


    public BooleanExpression groupByProperty() {
        return groupBy;
    }


    public void setGroupBy(BooleanExpression groupBy) {
        this.groupBy = groupBy;
    }


    public boolean isGroupBy() {
        return groupBy.get();
    }


    public String getSortRank() {
        return sortRank.get();
    }


    public StringExpression sortRankProperty() {
        return sortRank;
    }


    public void setSortRank(StringExpression sortRank) {
        this.sortRank = sortRank;
    }


    public String getSortType() {
        return sortType.get();
    }


    public StringExpression sortTypeProperty() {
        return sortType;
    }


    public void setSortType(StringExpression sortType) {
        this.sortType = sortType;
    }

}
