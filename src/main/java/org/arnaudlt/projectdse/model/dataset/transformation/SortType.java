package org.arnaudlt.projectdse.model.dataset.transformation;

public enum SortType {

    ASCENDING("Asc"),
    DESCENDING("Desc");

    private final String name;


    SortType(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }


    public static SortType valueFromSortTypeName(String sortTypeName) {

        SortType[] values = SortType.values();
        for (SortType name : values) {

            if (name.getName().equals(sortTypeName)) return name;
        }
        return null;
    }
}
