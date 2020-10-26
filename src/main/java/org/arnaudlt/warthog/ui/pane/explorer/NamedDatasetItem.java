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

        if (sqlName.contains(" ")) {

            return "`" + sqlName + "`";
        } else {

            return sqlName;
        }
    }


    @Override
    public String toString() {
        return label;
    }
}
