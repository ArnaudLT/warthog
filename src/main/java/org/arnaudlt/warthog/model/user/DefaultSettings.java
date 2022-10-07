package org.arnaudlt.warthog.model.user;


import javafx.beans.property.SimpleStringProperty;

public enum DefaultSettings {

    INSTANCE;

    public final UserSettings user = new UserSettings("user", new SimpleStringProperty());

    public final SqlHistorySettings sqlHistory = new SqlHistorySettings("history", 20);

    public final OverviewSettings overview = new OverviewSettings(50, 0);

    public final SparkSettings spark = new SparkSettings(4, false);

}
