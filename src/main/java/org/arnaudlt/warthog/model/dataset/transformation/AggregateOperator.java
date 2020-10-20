package org.arnaudlt.warthog.model.dataset.transformation;

public enum AggregateOperator {

    COUNT("count"),
    COUNT_DISTINCT("count distinct"),
    SUM("sum"),
    SUM_DISTINCT("sum distinct"),
    MIN("min"),
    MAX("max"),
    MEAN("mean");


    private final String operatorName;


    AggregateOperator(String operatorName) {
        this.operatorName = operatorName;
    }


    public String getOperatorName() {
        return operatorName;
    }


    public static AggregateOperator valueFromOperatorName(String operatorName) {

        AggregateOperator[] values = AggregateOperator.values();
        for (AggregateOperator value : values) {

            if (value.getOperatorName().equals(operatorName)) return value;
        }
        return null;
    }


    @Override
    public String toString() {
        return operatorName;
    }
}
