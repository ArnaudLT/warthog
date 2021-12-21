package org.arnaudlt.warthog.ui.pane.explorer;

import org.apache.spark.sql.types.DataType;
import org.arnaudlt.warthog.model.dataset.NamedDataset;

import java.util.ArrayList;
import java.util.List;

public class NamedDatasetItem {

    private final NamedDataset namedDataset;

    private String label;

    private String sqlName;

    private final DataType dataType;

    private final List<NamedDatasetItem> child;


    public NamedDatasetItem(NamedDataset namedDataset,
                            String label, String sqlName, DataType dataType) {

        this.namedDataset = namedDataset;
        this.label = label;
        this.sqlName = sqlName;
        this.dataType = dataType;
        this.child = new ArrayList<>();
    }


    public NamedDataset getNamedDataset() {
        return namedDataset;
    }


    public void setLabel(String label) {
        this.label = label;
    }


    public String getLabel() {
        return label;
    }


    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }


    public String getSqlName() {
        return sqlName;
    }


    public String getCleanedSqlName() {

        if (containsSpecialCharacter(sqlName)) {

            return "`" + sqlName + "`";
        } else {

            return sqlName;
        }
    }


    public DataType getDataType() {
        return dataType;
    }


    public List<NamedDatasetItem> getChild() {
        return child;
    }


    private boolean containsSpecialCharacter(String value) {

        return value
                .chars()
                .anyMatch(i -> !(
                        (i > 64 && i < 91) ||
                        (i > 96 && i < 123) ||
                        (i > 47 && i < 58) ||
                        (i == 95)
                ));
    }


    @Override
    public String toString() {
        return label;
    }

}
