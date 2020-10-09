package org.arnaudlt.projectdse.model.dataset;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.projectdse.model.dataset.transformation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.spark.sql.functions.col;

public class NamedDataset {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedDataset.class);

    private final int id;

    private final String name;

    private final Dataset<Row> dataset;

    private final Catalog catalog;

    private final Transformation transformation;

    private final Decoration decoration;


    public NamedDataset(int id, String name, Dataset<Row> dataset, Catalog catalog, Transformation transformation, Decoration decoration) {

        this.id = id;
        this.name = name;
        this.dataset = dataset;
        this.catalog = catalog;
        this.transformation = transformation;
        this.decoration = decoration;
    }


    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public Dataset<Row> getDataset() {
        return dataset;
    }


    public Transformation getTransformation() {
        return transformation;
    }


    public Catalog getCatalog() {
        return catalog;
    }


    public Decoration getDecoration() {
        return decoration;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedDataset that = (NamedDataset) o;
        return id == that.id;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return name + " (" + id + ")";
    }

    private Dataset<Row> applySelect(Dataset<Row> in) {

        return in.select(this.transformation.getSelectNamedColumns().stream()
                .filter(snc -> snc.isSelected() || snc.isGroupBy())
                .map(SelectNamedColumn::getName)
                .map(functions::col)
                .toArray(Column[]::new));
    }


    private Dataset<Row> applyGroupBy(Dataset<Row> in) {

        long columnGroupByCount = this.getTransformation().getSelectNamedColumns().stream()
                .filter(SelectNamedColumn::isGroupBy)
                .count();

        if (columnGroupByCount == 0) {
            return in;
        }

        RelationalGroupedDataset afterGroupBy = in.groupBy(this.transformation.getSelectNamedColumns().stream()
                .filter(SelectNamedColumn::isGroupBy)
                .map(SelectNamedColumn::getName)
                .map(functions::col)
                .toArray(Column[]::new));

        Column[] aggColumns = this.transformation.getSelectNamedColumns().stream()
                .filter(snc -> snc.isSelected() && !snc.isGroupBy())
                .map(snc -> applyOneAggregateOperator(col(snc.getName()), snc))
                .toArray(Column[]::new);

        if (aggColumns.length == 0) {
            return afterGroupBy.df();
        }

        return afterGroupBy
                .agg(aggColumns[0], Arrays.copyOfRange(aggColumns, 1, aggColumns.length));
    }


    private Column applyOneAggregateOperator(Column column, SelectNamedColumn snc) {

        AggregateOperator aggregateOperator = AggregateOperator.valueFromOperatorName(snc.getAggregateOperator());

        if (aggregateOperator == null) {

            LOGGER.info("No valid aggregate operator ({}) on column {}", snc.getAggregateOperator(), snc.getName());
            return column;
        }

        Column columnResult = column;
        switch (aggregateOperator) {

            case COUNT:
                columnResult = functions.count(column);
                break;
            case COUNT_DISTINCT:
                columnResult = functions.countDistinct(column);
                break;
            case SUM:
                columnResult = functions.sum(column);
                break;
            case SUM_DISTINCT:
                columnResult = functions.sumDistinct(column);
                break;
            case MIN:
                columnResult = functions.min(column);
                break;
            case MAX:
                columnResult = functions.max(column);
                break;
            case MEAN:
                columnResult = functions.mean(column);
                break;
            default:
                LOGGER.error("Aggregate operator {} is not yet implemented", aggregateOperator);
        }
        snc.setAlias(columnResult.toString() + "#agg_" + snc.getId());
        return columnResult.alias(snc.getAlias());
    }


    private Dataset<Row> applyWhere(Dataset<Row> in) {

        Dataset<Row> out = in;
        for (WhereNamedColumn wnc : this.transformation.getWhereNamedColumns()) {

            out = applyOneWhereNamedClause(out, wnc);
        }
        return out;
    }


    private Dataset<Row> applyOneWhereNamedClause(Dataset<Row> in, WhereNamedColumn wnc) {

        String columnNameString = wnc.getName();
        String operatorString = wnc.getOperator();
        String operandString = wnc.getOperand();

        Dataset<Row> out = in;

        // START VALIDITY CHECK
        if (!isAValidWhereClause(columnNameString, operatorString, operandString)) {
            return out;
        }

        BooleanOperator booleanOperator = BooleanOperator.valueFromOperatorName(operatorString);
        switch (booleanOperator) {

            // TODO consider column typ and cast operand ?
            case EQ:
                out = out.where(col(columnNameString).equalTo(operandString));
                break;
            case NEQ:
                out = out.where(col(columnNameString).notEqual(operandString));
                break;
            case LT:
                out = out.where(col(columnNameString).lt(operandString));
                break;
            case LEQ:
                out = out.where(col(columnNameString).leq(operandString));
                break;
            case GT:
                out = out.where(col(columnNameString).gt(operandString));
                break;
            case GEQ:
                out = out.where(col(columnNameString).geq(operandString));
                break;
            case IS_NULL:
                out = out.where(col(columnNameString).isNull());
                break;
            case IS_NOT_NULL:
                out = out.where(col(columnNameString).isNotNull());
                break;
            case CONTAINS:
                out = out.where(col(columnNameString).contains(operandString));
                break;
            case LIKE:
                out = out.where(col(columnNameString).like(operandString));
                break;
            default:
                LOGGER.error("Operator {} is not yet implemented", booleanOperator);
        }

        return out;
    }


    private boolean isAValidWhereClause(String columnNameString, String operatorString, String operandString) {

        if (columnNameString == null || columnNameString.isBlank() || operatorString == null || operatorString.isBlank()) {

            LOGGER.info("c1a");
            return false;
        }

        BooleanOperator booleanOperator = BooleanOperator.valueFromOperatorName(operatorString);
        if (booleanOperator == null) {

            LOGGER.warn("Unknown operator {} used on column {}", operatorString, columnNameString);
            return false;
        }

        if (booleanOperator.getArity() == 2 && (operandString == null || operandString.isBlank())) {

            LOGGER.warn("Missing operand in restriction {} {} ?", columnNameString, booleanOperator);
            return false;
        }

        return true;
    }


    // Remove the alias given to the agg columns
    private Dataset<Row> removeInternalAlias(Dataset<Row> output) {

        for (SelectNamedColumn snc : this.getTransformation().getSelectNamedColumns()) {

            if (snc.getAlias() == null || snc.getAlias().isEmpty()) continue;

            // TODO not correct and risky (image a world where #agg_12 is a part of the column name !! :-) )
            String withoutInternalAlias = snc.getAlias().replace("#agg_" + snc.getId(), "");
            output = output.withColumnRenamed(snc.getAlias(), withoutInternalAlias);
        }
        return output;
    }


    private Dataset<Row> applySort(Dataset<Row> output) {

        List<SelectNamedColumn> sortedColumns = this.getTransformation().getSelectNamedColumns().stream()
                .filter(snc -> snc.isSelected() && iAValidSort(snc.getSortType(), snc.getSortRank()))
                .sorted(Comparator.comparingInt(snc -> Integer.parseInt(snc.getSortRank().trim())))
                .collect(Collectors.toList());

        if (!sortedColumns.isEmpty()) {

            Column[] sort = sortedColumns.stream()
                    .map(this::applyOneSort)
                    .toArray(Column[]::new);
            output = output.sort(sort);
        }

        return output;
    }


    private Column applyOneSort(SelectNamedColumn snc) {

        Column sortColumn;
        if (snc.getAlias() != null && !snc.getAlias().isEmpty()) {

            sortColumn = functions.col(snc.getAlias());
        } else {

            sortColumn = functions.col(snc.getName());
        }

        SortType sortType = SortType.valueFromSortTypeName(snc.getSortType());
        if (sortType == SortType.DESCENDING) {

            sortColumn = sortColumn.desc();
        }
        return sortColumn;
    }


    private boolean iAValidSort(String sortTypeString, String sortRankString) {

        if (SortType.valueFromSortTypeName(sortTypeString) == null) {
            return false;
        }
        try {

            Integer.parseInt(sortRankString.trim());
        } catch (Exception e) {

            return false;
        }
        return true;
    }


    protected Dataset<Row> applyTransformation() {

        Dataset<Row> output = applyWhere(this.dataset);
        output = applySelect(output);
        output = applyGroupBy(output);
        output = applySort(output);
        output = removeInternalAlias(output);
        output = dropUnselected(output); // drop unselected columns but present in the group by.

        return output;
    }


    // Handle all the columns part of the group by, but not selected by the user.
    private Dataset<Row> dropUnselected(Dataset<Row> output) {

        String[] toRemove = this.transformation.getSelectNamedColumns().stream()
                .filter(snc -> !snc.isSelected())
                .map(NamedColumn::getName)
                .toArray(String[]::new);

        return output.drop(toRemove);
    }


    public List<Row> generateRowOverview() {

        Dataset<Row> output = applyTransformation();
        return output.takeAsList(100);
    }


    public List<String> generateOverview() {

        Dataset<Row> output = applyTransformation();

        String separator = this.getDecoration().getSeparator();

        List<String> strings = output
                .map((MapFunction<Row, String>) row -> row.mkString(separator), Encoders.STRING())
                .takeAsList(100);

        String header = Arrays.stream(output.schema().fields())
                .map(StructField::name)
                .collect(Collectors.joining(separator));

        ArrayList<String> overview = new ArrayList<>();
        overview.add(header);
        overview.addAll(strings);

        return overview;
    }


    public void export(String filePath) {

        Dataset<Row> output = applyTransformation();

        output
                .coalesce(1)
                .write()
                .option("sep", this.getDecoration().getSeparator())
                .option("header", true)
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .csv(filePath);
    }

}
