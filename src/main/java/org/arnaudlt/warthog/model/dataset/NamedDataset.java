package org.arnaudlt.warthog.model.dataset;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructField;
import org.arnaudlt.warthog.model.dataset.transformation.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
public class NamedDataset {


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


    protected Dataset<Row> applyTransformation() {

        Dataset<Row> output = applyJoin(this.dataset);
        output = applyWhere(output);
        output = applySelect(output);
        output = applyGroupBy(output);
        output = applySort(output);
        output = removeInternalAlias(output);
        output = dropUnselected(output); // drop unselected columns but present in the group by.

        return output;
    }


    private Dataset<Row> applyJoin(Dataset<Row> in) {

        Join join = this.getTransformation().getJoin();

        if (join.getDatasetToJoin() == null ||
            join.getJoinType() == null || join.getJoinType().isEmpty() ||
            join.getLeftColumn() == null ||
            join.getRightColumn() == null) {

            log.info("No valid join for dataset {}", this.name);
            return in;
        }

        return in.join(join.getDatasetToJoin().getDataset(),
                dataset.col(join.getLeftColumn().getName()).equalTo(join.getDatasetToJoin().dataset.col(join.getRightColumn().getName())),
                join.getJoinType());
    }


    private Dataset<Row> applyWhere(Dataset<Row> in) {

        Dataset<Row> out = in;
        for (WhereClause wc : this.transformation.getWhereClauses()) {

            out = applyOneWhereNamedClause(out, wc);
        }
        return out;
    }


    private Dataset<Row> applySelect(Dataset<Row> in) {

        return in.select(this.transformation.getSelectNamedColumns().stream()
                .filter(snc -> snc.isSelected() || snc.isGroupBy())
                .map(SelectNamedColumn::getName)
                .map(dataset::col)
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
                .map(in::col)
                .toArray(Column[]::new));

        Column[] aggColumns = this.transformation.getSelectNamedColumns().stream()
                .filter(snc -> snc.isSelected() && !snc.isGroupBy())
                .map(snc -> applyOneAggregateOperator(in.col(snc.getName()), snc))
                .toArray(Column[]::new);

        if (aggColumns.length == 0) {
            return afterGroupBy.df();
        }

        return afterGroupBy
                .agg(aggColumns[0], Arrays.copyOfRange(aggColumns, 1, aggColumns.length));
    }


    private Dataset<Row> applySort(Dataset<Row> output) {

        Column[] sortColumns = this.getTransformation().getSelectNamedColumns().stream()
                .filter(snc -> snc.isSelected() && iAValidSort(snc.getSortType(), snc.getSortRank()))
                .sorted(Comparator.comparingInt(snc -> Integer.parseInt(snc.getSortRank().trim())))
                .collect(Collectors.toList())
                .stream()
                .map(snc -> applyOneSortType(getColumn(output, snc), snc))
                .toArray(Column[]::new);

        Dataset<Row> output2 = output;
        if (sortColumns.length > 0) {

           output2 = output.sort(sortColumns);
        }

        return output2;
    }


    private Dataset<Row> removeInternalAlias(Dataset<Row> output) {

        for (SelectNamedColumn snc : this.getTransformation().getSelectNamedColumns()) {

            if (snc.getAlias() == null || snc.getAlias().isEmpty()) continue;

            String withoutInternalAlias = snc.getAlias().replaceAll("#agg_" + snc.getId() + "$", "");
            output = output.withColumnRenamed(snc.getAlias(), withoutInternalAlias);
        }
        return output;
    }


    private Dataset<Row> dropUnselected(Dataset<Row> output) {

        String[] toRemove = this.transformation.getSelectNamedColumns().stream()
                .filter(snc -> !snc.isSelected())
                .map(NamedColumn::getName)
                .toArray(String[]::new);

        return output.drop(toRemove);
    }



    // WHERE APPLY
    private Dataset<Row> applyOneWhereNamedClause(Dataset<Row> in, WhereClause wc) {

        Dataset<Row> out = in;

        if (!isAValidWhereClause(wc)) {
            return out;
        }

        final String columnName = wc.getColumn().getName();

        switch (wc.getOperator()) {

            case EQ:
                out = out.where(dataset.col(columnName).equalTo(wc.getOperand()));
                break;
            case NEQ:
                out = out.where(dataset.col(columnName).notEqual(wc.getOperand()));
                break;
            case LT:
                out = out.where(dataset.col(columnName).lt(wc.getOperand()));
                break;
            case LEQ:
                out = out.where(dataset.col(columnName).leq(wc.getOperand()));
                break;
            case GT:
                out = out.where(dataset.col(columnName).gt(wc.getOperand()));
                break;
            case GEQ:
                out = out.where(dataset.col(columnName).geq(wc.getOperand()));
                break;
            case IS_NULL:
                out = out.where(dataset.col(columnName).isNull());
                break;
            case IS_NOT_NULL:
                out = out.where(dataset.col(columnName).isNotNull());
                break;
            case CONTAINS:
                out = out.where(dataset.col(columnName).contains(wc.getOperand()));
                break;
            case LIKE:
                out = out.where(dataset.col(columnName).like(wc.getOperand()));
                break;
            default:
                log.error("Operator {} is not implemented", wc.getOperator());
        }

        return out;
    }


    // WHERE CHECK
    private boolean isAValidWhereClause(WhereClause wc) {

        if (wc.getColumn() == null || wc.getOperator() == null || wc.getOperator() == BooleanOperator.NONE) {

            return false;
        }

        if (wc.getOperator().getArity() == 2 && wc.getOperand() == null) {

            return false;
        }

        log.info("Where clause : {} {} {}", wc.getColumn(), wc.getOperator(), wc.getOperand());
        return true;
    }


    // AGG APPLY
    private Column applyOneAggregateOperator(Column column, SelectNamedColumn snc) {

        AggregateOperator aggregateOperator = AggregateOperator.valueFromOperatorName(snc.getAggregateOperator());

        if (aggregateOperator == null) {

            log.info("No valid aggregate operator ({}) on column {}", snc.getAggregateOperator(), snc.getName());
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
                log.error("Aggregate operator {} is not yet implemented", aggregateOperator);
        }
        snc.setAlias(columnResult.toString() + "#agg_" + snc.getId());
        return columnResult.alias(snc.getAlias());
    }


    // SORT APPLY
    private Column applyOneSortType(Column column, SelectNamedColumn snc) {

        SortType sortType = SortType.valueFromSortTypeName(snc.getSortType());
        if (sortType == SortType.DESCENDING) {

            column = column.desc();
        }
        return column;
    }


    // SORT CHECK
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


    private Column getColumn(Dataset<Row> dataset, SelectNamedColumn snc) {

        Column column;
        if (snc.getAlias() != null && !snc.getAlias().isEmpty()) {

            column = dataset.col(snc.getAlias());
        } else {

            column = dataset.col(snc.getName());
        }
        return column;
    }

    // ###################################################################
    // ######################### OUTPUT ##################################
    // ###################################################################


    public List<Row> generateRowOverview() {

        Dataset<Row> output = applyTransformation();
        output = stringify(output);
        return output.takeAsList(100);
    }


    public void export(String filePath) {

        Dataset<Row> output = applyTransformation();
        output = stringify(output);
        output
                .coalesce(1)
                .write()
                .option("sep", this.getDecoration().getSeparator())
                .option("header", true)
                .option("mapreduce.fileoutputcommitter.marksuccessfuljobs", false)
                .csv(filePath);
    }


    //TODO this is a very low cost map/array stringify function :-)
    private Dataset<Row> stringify(Dataset<Row> dataset) {

        Dataset<Row> output = dataset;

        StructField[] fields = dataset.schema().fields();
        for (StructField field : fields) {

            if ("map".equals(field.dataType().typeName())) {

                output = output.withColumn(field.name(), functions.callUDF("mapToString", output.col(field.name())));
            } else if ("array".equals(field.dataType().typeName())) {

                output = output.withColumn(field.name(), functions.callUDF("arrayToString", output.col(field.name())));
            }
        }
        return output;
    }

}
