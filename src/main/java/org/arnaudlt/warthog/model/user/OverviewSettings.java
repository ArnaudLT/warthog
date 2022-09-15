package org.arnaudlt.warthog.model.user;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public final class OverviewSettings {

    private Integer rows;

    private Integer truncateAfter;

    public OverviewSettings(OverviewSettings overview) {

        this.rows = overview.rows;
        this.truncateAfter = overview.truncateAfter;
    }
}
