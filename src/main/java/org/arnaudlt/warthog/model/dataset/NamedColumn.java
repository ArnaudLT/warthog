package org.arnaudlt.warthog.model.dataset;

import java.util.Objects;

public class NamedColumn {

    private final String name;

    private final String type;


    public NamedColumn(String name, String type) {

        Objects.requireNonNull(name, "A column name cannot be null");
        Objects.requireNonNull(type, "A column type cannot be null");
        this.name = name;
        this.type = type;
    }


    public String getName() {
        return name;
    }


    public String getType() {
        return type;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedColumn that = (NamedColumn) o;
        return name.equals(that.name) && type.equals(that.type);
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }


    @Override
    public String toString() {
        return name + " - " + type;
    }
}
