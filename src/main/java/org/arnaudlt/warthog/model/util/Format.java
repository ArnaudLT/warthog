package org.arnaudlt.warthog.model.util;

public enum Format {

    CSV("csv"),
    JSON("json"),
    PARQUET("parquet"),
    ORC("orc");

    final String label;

    Format(String label) {

        this.label = label;
    }


    static Format valueFromLabel(String label) {

        for (Format f : values()) {
            if (f.label.equals(label)) {
                return f;
            }
        }
        return null;
    }

}
