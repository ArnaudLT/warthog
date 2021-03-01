package org.arnaudlt.warthog.ui.pane.explorer;

import org.arnaudlt.warthog.model.dataset.NamedDataset;

public class NamedDatasetItem {

    private final NamedDataset namedDataset;

    private final String label;

    private final String sqlName;


    public NamedDatasetItem(NamedDataset namedDataset, String label, String sqlName) {
        this.namedDataset = namedDataset;
        this.label = label;
        this.sqlName = sqlName;
    }


    public NamedDataset getNamedDataset() {
        return namedDataset;
    }


    public String getLabel() {
        return label;
    }


    public String getSqlName() {

        if (containsSpecialCharacter(sqlName)) {

            return "`" + sqlName + "`";
        } else {

            return sqlName;
        }
    }


    private boolean containsSpecialCharacter(String value) {

        return value
                .chars()
                .anyMatch(i -> !(
                        (i > 64 && i < 91) ||
                        (i > 96 && i < 123) ||
                        (i > 47 && i < 58) ||
                        (i == 45) ||
                        (i == 95)
                ));
    }


    @Override
    public String toString() {
        return label;
    }
}
