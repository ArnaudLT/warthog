package org.arnaudlt.projectdse.ui.pane.explorer;

import org.arnaudlt.projectdse.model.dataset.NamedDataset;

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


    @Override
    public String toString() {
        return label;
    }
}
