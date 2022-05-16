package org.arnaudlt.warthog.model.util;

public enum Compression {

    SNAPPY("snappy"),
    GZIP("gzip");

    final String label;

    Compression(String label) {

        this.label = label;
    }

    public String getLabel() {
        return label;
    }

}
