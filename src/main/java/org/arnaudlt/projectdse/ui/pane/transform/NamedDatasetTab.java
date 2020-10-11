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
import org.arnaudlt.projectdse.model.dataset.transformation.WhereNamedColumn;

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

        TabPane transformationTabPane = new TabPane(selectTab, whereTab/*, joinTab*/);
        transformationTabPane.setSide(Side.TOP);
        transformationTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        this.setContent(transformationTabPane);
    }


    private Tab buildSelectTab() {

        GridPane grid = new GridPane();
        grid.setHgap(10d);
        grid.setVgap(5d);

        grid.add(new Label("Column name"), 1, 1);
        grid.add(new Label("Select"), 2, 1);
        grid.add(new Label("Group by"), 3, 1);
        grid.add(new Label("Aggregate"), 4, 1);
        grid.add(new Label("Sort rank"), 5, 1);
        grid.add(new Label("Sort type"), 6, 1);

        int i=2;
        for (SelectNamedColumn snc : namedDataset.getTransformation().getSelectNamedColumns()) {

            grid.add(new Label(snc.getName() + " - " + snc.getType()), 1, i);

            // Select
            CheckBox selectCheckBox = buildSelectCheckBox(snc);
            grid.add(selectCheckBox, 2, i);

            // Group by
            CheckBox groupByCheckBox = buildGroupByCheckBox(snc);
            grid.add(groupByCheckBox, 3, i);

            // Aggregate operator
            ComboBox<String> aggregateOperatorCombo = buildAggregateOperatorComboBox(snc, selectCheckBox, groupByCheckBox);
            grid.add(aggregateOperatorCombo, 4, i);

            // Sort rank + type
            TextField sortRank = buildSortRankTextField(snc, selectCheckBox);
            grid.add(sortRank, 5, i);

            ComboBox<String> sortType = buildSortTypeComboBox(snc, selectCheckBox);
            grid.add(sortType, 6, i);

            i++;
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

        grid.add(new Label("Column name"), 1, 1);
        grid.add(new Label("Operator"), 2, 1);
        grid.add(new Label("Operand"), 3, 1);

        int i=2;
        for (WhereNamedColumn wnc : namedDataset.getTransformation().getWhereNamedColumns()) {

            grid.add(new Label(wnc.getName() + " - " + wnc.getType()), 1, i);

            ComboBox<String> operator = new ComboBox<>();
            operator.getItems().add("");
            for (BooleanOperator op : BooleanOperator.values()) {

                operator.getItems().add(op.getOperatorName());
            }
            wnc.setOperator(StringBinding.stringExpression(operator.valueProperty()));
            grid.add(operator, 2, i);

            TextField operand = new TextField();
            wnc.setOperand(StringBinding.stringExpression(operand.textProperty()));
            grid.add(operand, 3, i);
            i++;
        }

        ScrollPane scrollPane = new ScrollPane(grid);
        return new Tab("Where", scrollPane);
    }


    private Tab buildJoinTab() {

        GridPane grid = new GridPane();
        grid.setHgap(10d);
        grid.setVgap(5d);

        grid.add(new Label("Join With"), 1, 1);
        grid.add(new Label("Type"), 2, 1);
        grid.add(new Label("Column"), 3, 1);
        grid.add(new Label(""), 4, 1);
        grid.add(new Label("Column"), 5, 1);

        // Available Dataset
        ComboBox<NamedDataset> datasetToJoin = new ComboBox<>();
        datasetToJoin.setItems(namedDatasetManager.getObservableNamedDatasets());
        namedDataset.getTransformation().getJoin().setDatasetToJoin(datasetToJoin.valueProperty());
        grid.add(datasetToJoin, 1, 2);

        // Join Type
        ComboBox<String> joinType = new ComboBox<>(
                FXCollections.observableArrayList("inner", "left", "right", "outer", "cross"));
        namedDataset.getTransformation().getJoin().setJoinType(StringBinding.stringExpression(joinType.valueProperty()));
        grid.add(joinType, 2, 2);

        // Column from current dataset
        ComboBox<NamedColumn> leftColumn = new ComboBox<>();
        for (NamedColumn nc : namedDataset.getCatalog().getColumns()) {

            leftColumn.getItems().add(nc);
        }
        namedDataset.getTransformation().getJoin().setLeftColumn(leftColumn.valueProperty());
        grid.add(leftColumn, 3, 2);

        // Operator
        grid.add(new Label("="), 4, 2);

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
        grid.add(rightColumn, 5, 2);

        Button activeJoin = new Button("Go !");

        grid.add(activeJoin, 1, 3);

        ScrollPane scrollPane = new ScrollPane(grid);
        return new Tab("Join", scrollPane);
    }



    public NamedDataset getNamedDataset() {
        return namedDataset;
    }
}
