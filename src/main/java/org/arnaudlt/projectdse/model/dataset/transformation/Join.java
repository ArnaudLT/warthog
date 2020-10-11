package org.arnaudlt.projectdse.model.dataset.transformation;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringExpression;
import org.arnaudlt.projectdse.model.dataset.NamedColumn;
import org.arnaudlt.projectdse.model.dataset.NamedDataset;

public class Join {


    private ObjectExpression<NamedDataset> datasetToJoin;

    private StringExpression joinType;

    private ObjectExpression<NamedColumn> leftColumn;

    private ObjectExpression<NamedColumn> rightColumn;


    public void setDatasetToJoin(ObjectExpression<NamedDataset> datasetToJoin) {
        this.datasetToJoin = datasetToJoin;
    }


    public void setJoinType(StringExpression joinType) {
        this.joinType = joinType;
    }


    public void setLeftColumn(ObjectExpression<NamedColumn> leftColumn) {
        this.leftColumn = leftColumn;
    }


    public void setRightColumn(ObjectExpression<NamedColumn> rightColumn) {
        this.rightColumn = rightColumn;
    }


    public NamedDataset getDatasetToJoin() {
        return datasetToJoin.get();
    }


    public ObjectExpression<NamedDataset> datasetToJoinProperty() {
        return datasetToJoin;
    }


    public String getJoinType() {
        return joinType.get();
    }


    public StringExpression joinTypeProperty() {
        return joinType;
    }


    public NamedColumn getLeftColumn() {
        return leftColumn.get();
    }


    public ObjectExpression<NamedColumn> leftColumnProperty() {
        return leftColumn;
    }


    public NamedColumn getRightColumn() {
        return rightColumn.get();
    }


    public ObjectExpression<NamedColumn> rightColumnProperty() {
        return rightColumn;
    }
}
