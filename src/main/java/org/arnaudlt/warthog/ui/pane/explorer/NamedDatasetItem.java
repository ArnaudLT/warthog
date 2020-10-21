package org.arnaudlt.warthog.ui.pane.explorer;

import org.arnaudlt.warthog.model.dataset.NamedDataset;

public class NamedDatasetItem {

    private final NamedDataset namedDataset;

    private final String label;


    public NamedDatasetItem(NamedDataset namedDataset, String label) {
        this.namedDataset = namedDataset;
        this.label = label;
    }


    public NamedDataset getNamedDataset() {
        return namedDataset;
    }


    public String getLabel() {
        return label;
    }


    @Override
    public String toString() {
        return label;
    }
}
