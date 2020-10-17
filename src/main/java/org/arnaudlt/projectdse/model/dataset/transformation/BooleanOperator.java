package org.arnaudlt.projectdse.model.dataset.transformation;

public enum BooleanOperator {

    NONE("", 0),
    EQ("=", 2),
    NEQ("!=", 2),
    LT("<", 2),
    LEQ("<=", 2),
    GT(">", 2),
    GEQ(">=", 2),
    IS_NULL("is null", 1),
    IS_NOT_NULL("is not null", 1),
    CONTAINS("contains", 2),
    LIKE("like", 2)
    ;


    private final String operatorName;

    private final int arity;


    BooleanOperator(String operatorName, int arity) {

        this.operatorName = operatorName;
        this.arity = arity;
    }


    public String getOperatorName() {
        return operatorName;
    }


    public int getArity() {
        return arity;
    }


    public static BooleanOperator valueFromOperatorName(String operatorName) {

        BooleanOperator[] values = BooleanOperator.values();
        for (BooleanOperator value : values) {

            if (value.getOperatorName().equals(operatorName)) return value;
        }
        return null;
    }


    @Override
    public String toString() {
        return operatorName;
    }


}
