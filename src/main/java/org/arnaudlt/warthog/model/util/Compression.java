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

    static Compression valueFromLabel(String label) {

        Compression[] values = values();
        for (Compression c : values) {
            if (c.label.equals(label)) {
                return c;
            }
        }
        return null;
    }
}
