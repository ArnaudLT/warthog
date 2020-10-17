package org.arnaudlt.projectdse.ui.pane.transform;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.extern.slf4j.Slf4j;
import org.arnaudlt.projectdse.model.dataset.NamedColumn;
import org.arnaudlt.projectdse.model.dataset.NamedDataset;
import org.arnaudlt.projectdse.model.dataset.NamedDatasetManager;
import org.arnaudlt.projectdse.model.dataset.transformation.AggregateOperator;
import org.arnaudlt.projectdse.model.dataset.transformation.BooleanOperator;
import org.arnaudlt.projectdse.model.dataset.transformation.SelectNamedColumn;
import org.arnaudlt.projectdse.model.dataset.transformation.WhereClause;

import java.util.ArrayList;

@Slf4j
public class NamedDatasetTab extends Tab  {

    private final NamedDatasetManager namedDatasetManager;

    private final NamedDataset namedDataset;


    public NamedDatasetTab(NamedDatasetManager namedDatasetManager, NamedDataset namedDataset) {

        super(namedDataset.getName());
        this.namedDatasetManager = namedDatasetManager;
        this.namedDataset = namedDataset;
        this.setId(String.valueOf(namedDataset.getId()));
    }


    public void build() {

        Tab selectTab = buildSelectTab();
        Tab whereTab = buildWhereTab();
        Tab joinTab = buildJoinTab();

        TabPane transformationTabPane = new TabPane(selectTab, whereTab, joinTab);
        transformationTabPane.setSide(Side.TOP);
        transformationTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.setContent(transformationTabPane);
    }


    private Tab buildSelectTab() {

        GridPane grid = new GridPane();
        grid.setHgap(10d);
        grid.setVgap(5d);

        grid.addRow(grid.getRowCount(),
                new Label("Column name"),
                new Label("Select"),
                new Label("Group by"),
                new Label("Aggregate"),
                new Label("Sort rank"),
                new Label("Sort type")
        );

        for (SelectNamedColumn snc : namedDataset.getTransformation().getSelectNamedColumns()) {

            // Select
            CheckBox selectCheckBox = buildSelectCheckBox(snc);

            // Group by
            CheckBox groupByCheckBox = buildGroupByCheckBox(snc);

            // Aggregate operator
            ComboBox<String> aggregateOperatorCombo = buildAggregateOperatorComboBox(snc, selectCheckBox, groupByCheckBox);

            // Sort rank + type
            TextField sortRank = buildSortRankTextField(snc, selectCheckBox);

            ComboBox<String> sortType = buildSortTypeComboBox(snc, selectCheckBox);

            grid.addRow(grid.getRowCount(),
                    new Label(snc.getName() + " - " + snc.getType()),
                    selectCheckBox,
                    groupByCheckBox,
                    aggregateOperatorCombo,
                    sortRank,
                    sortType
            );
        }


        ScrollPane scrollPane = new ScrollPane(grid);
        return new Tab("Select / Group / Sort", scrollPane);
    }


    private ComboBox<String> buildSortTypeComboBox(SelectNamedColumn snc, CheckBox selectCheckBox) {
        ComboBox<String> sortType = new ComboBox<>();
        sortType.getItems().add("");
        sortType.getItems().add("Asc");
        sortType.getItems().add("Desc");
        sortType.visibleProperty().bind(selectCheckBox.selectedProperty());
        snc.setSortType(StringBinding.stringExpression(sortType.valueProperty()));
        return sortType;
    }


    private TextField buildSortRankTextField(SelectNamedColumn snc, CheckBox selectCheckBox) {
        TextField sortRank = new TextField();
        sortRank.setPrefColumnCount(2);
        sortRank.visibleProperty().bind(selectCheckBox.selectedProperty());
        snc.setSortRank(StringBinding.stringExpression(sortRank.textProperty()));
        return sortRank;
    }


    private ComboBox<String> buildAggregateOperatorComboBox(SelectNamedColumn snc, CheckBox selectCheckBox, CheckBox groupByCheckBox) {

        ComboBox<String> groupByOperatorCombo = new ComboBox<>();
        groupByOperatorCombo.getItems().add("");
        for (AggregateOperator op : AggregateOperator.values()) {

            groupByOperatorCombo.getItems().add(op.getOperatorName());
        }
        groupByOperatorCombo.visibleProperty().bind(
                groupByCheckBox.selectedProperty().not().and(selectCheckBox.selectedProperty()));
        snc.setAggregateOperator(StringBinding.stringExpression(groupByOperatorCombo.valueProperty()));
        return groupByOperatorCombo;
    }


    private CheckBox buildGroupByCheckBox(SelectNamedColumn snc) {

        CheckBox groupByCheckBox = new CheckBox();
        groupByCheckBox.setSelected(false);
        snc.setGroupBy(BooleanBinding.booleanExpression(groupByCheckBox.selectedProperty()));
        return groupByCheckBox;
    }


    private CheckBox buildSelectCheckBox(SelectNamedColumn snc) {

        CheckBox selectCheckBox = new CheckBox();
        selectCheckBox.setId(String.valueOf(snc.getId()));
        selectCheckBox.setSelected(true);
        snc.setSelected(BooleanBinding.booleanExpression(selectCheckBox.selectedProperty()));
        return selectCheckBox;
    }


    private Tab buildWhereTab() {

        GridPane grid = new GridPane();
        grid.setHgap(10d);
        grid.setVgap(5d);

        grid.addRow(grid.getRowCount(),
                new Label("Column"),
                new Label("Operator"),
                new Label("Operand")
        );

        for (WhereClause wc : namedDataset.getTransformation().getWhereClauses()) {

            // TODO allow dynamic number of where clauses
            ComboBox<NamedColumn> column = new ComboBox<>();
            column.getItems().add(null);
            column.getItems().addAll(namedDataset.getCatalog().getColumns());
            wc.setColumn(column.valueProperty());

            ComboBox<BooleanOperator> operator = new ComboBox<>();
            operator.getItems().addAll(BooleanOperator.values());
            operator.visibleProperty().bind(column.valueProperty().isNotNull());
            wc.setOperator(operator.valueProperty());

            TextField operand = new TextField();
            operand.visibleProperty().bind(Bindings.createObjectBinding(() -> {
                    if (operator.getValue() != null) {
                        return operator.getValue().getArity() > 1 && operator.isVisible();
                    }
                    return false;
                }, operator.valueProperty(), operator.visibleProperty()));
            wc.setOperand(StringBinding.stringExpression(operand.textProperty()));


            grid.addRow(grid.getRowCount(),
                    column,
                    operator,
                    operand);

        }

        ScrollPane scrollPane = new ScrollPane(grid);
        return new Tab("Where", scrollPane);
    }


    private Tab buildJoinTab() {

        GridPane grid = new GridPane();
        grid.setHgap(10d);
        grid.setVgap(5d);

        grid.addRow(grid.getRowCount(),
                new Label("Join With"),
                new Label("Type"),
                new Label("Column"),
                new Label(""),
                new Label("Column")
        );

        // Available Dataset
        ComboBox<NamedDataset> datasetToJoin = new ComboBox<>();
        datasetToJoin.setItems(namedDatasetManager.getObservableNamedDatasets());
        namedDataset.getTransformation().getJoin().setDatasetToJoin(datasetToJoin.valueProperty());

        // Join Type
        ComboBox<String> joinType = new ComboBox<>(
                FXCollections.observableArrayList("inner", "left", "right", "outer", "cross"));
        namedDataset.getTransformation().getJoin().setJoinType(StringBinding.stringExpression(joinType.valueProperty()));

        // Column from current dataset
        ComboBox<NamedColumn> leftColumn = new ComboBox<>();
        for (NamedColumn nc : namedDataset.getCatalog().getColumns()) {

            leftColumn.getItems().add(nc);
        }
        namedDataset.getTransformation().getJoin().setLeftColumn(leftColumn.valueProperty());

        // Column from selected available dataset
        ComboBox<NamedColumn> rightColumn = new ComboBox<>();
        rightColumn.itemsProperty().bind(Bindings.createObjectBinding(() -> {

                    NamedDataset selectedNamedDataset = datasetToJoin.getValue();
                    ObservableList<NamedColumn> toReturn;
                    if (selectedNamedDataset == null) {
                        toReturn = FXCollections.emptyObservableList();
                    } else {
                        toReturn = FXCollections.observableArrayList(new ArrayList<>(datasetToJoin.getValue().getCatalog().getColumns()));
                    }
                    return toReturn;
            }, datasetToJoin.valueProperty()
        ));
        namedDataset.getTransformation().getJoin().setRightColumn(rightColumn.valueProperty());

        grid.addRow(grid.getRowCount(),
                datasetToJoin,
                joinType,
                leftColumn,
                new Label("="),
                rightColumn
        );

        ScrollPane scrollPane = new ScrollPane(grid);
        return new Tab("Join", scrollPane);
    }


    public NamedDataset getNamedDataset() {
        return namedDataset;
    }
}
