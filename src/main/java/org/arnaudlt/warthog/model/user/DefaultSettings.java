package org.arnaudlt.warthog.model.user;


public enum DefaultSettings {

    INSTANCE;

    public final UserSettings user = new UserSettings("user", "", "");

    public final SqlHistorySettings sqlHistory = new SqlHistorySettings("history", 20);

    public final OverviewSettings overview = new OverviewSettings(50, 0);

    public final SparkSettings spark = new SparkSettings(4, false);

    public final WorkspaceSettings workspaceSettings = new WorkspaceSettings("workspace");

}
