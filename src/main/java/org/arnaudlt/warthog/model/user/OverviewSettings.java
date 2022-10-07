package org.arnaudlt.warthog.model.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class OverviewSettings {

    private Integer rows;

    private Integer truncateAfter;


    public OverviewSettings(OverviewSettings overview) {

        this.rows = overview.rows;
        this.truncateAfter = overview.truncateAfter;
    }


    public OverviewSettings(GlobalSettings.SerializableOverviewSettings overview) {

        if (overview != null) {
            this.rows = overview.getRows();
            this.truncateAfter = overview.getTruncateAfter();
        }
    }
}
