package org.arnaudlt.warthog.model.dataset.transformation;

import javafx.beans.binding.ObjectExpression;
import javafx.beans.binding.StringExpression;
import org.arnaudlt.warthog.model.dataset.NamedColumn;

public class WhereClause {


    private ObjectExpression<NamedColumn> column;

    private ObjectExpression<BooleanOperator> operator;

    private StringExpression operand;


    public NamedColumn getColumn() {
        return column.get();
    }


    public ObjectExpression<NamedColumn> columnProperty() {
        return column;
    }


    public void setColumn(ObjectExpression<NamedColumn> column) {
        this.column = column;
    }


    public BooleanOperator getOperator() {
        return operator.get();
    }


    public ObjectExpression<BooleanOperator> operatorProperty() {
        return operator;
    }


    public void setOperator(ObjectExpression<BooleanOperator> operator) {
        this.operator = operator;
    }


    public String getOperand() {
        return operand.get();
    }


    public StringExpression operandProperty() {
        return operand;
    }


    public void setOperand(StringExpression operand) {
        this.operand = operand;
    }

}
