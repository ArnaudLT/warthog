package org.arnaudlt.projectdse.model.dataset.transformation;

import javafx.beans.binding.StringExpression;
import org.apache.spark.sql.Column;
import org.arnaudlt.projectdse.model.dataset.NamedColumn;

public class WhereNamedColumn extends NamedColumn {


    private StringExpression operator;

    private StringExpression operand;


    public WhereNamedColumn(int id, String name, String type, Column column) {

        super(id, name, type, column);
    }


    public String getOperator() {
        return operator.get();
    }


    public StringExpression operatorProperty() {
        return operator;
    }


    public void setOperator(StringExpression operator) {
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

