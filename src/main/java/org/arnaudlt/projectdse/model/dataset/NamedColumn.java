package org.arnaudlt.projectdse.model.dataset;

import java.util.Objects;

public class NamedColumn {

    private final int id;

    private final String name;

    private final String type;

    private String alias;


    public NamedColumn(int id, String name, String type) {

        Objects.requireNonNull(name, "A column name cannot be null");
        Objects.requireNonNull(type, "A column type cannot be null");
        this.name = name;
        this.type = type;
        this.id = id;
    }


    public int getId() {
        return id;
    }


    public String getName() {
        return name;
    }


    public String getType() {
        return type;
    }


    public String getAlias() {
        return alias;
    }


    public void setAlias(String alias) {
        this.alias = alias;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NamedColumn that = (NamedColumn) o;
        return name.equals(that.name) &&
                type.equals(that.type);
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
