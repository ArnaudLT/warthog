package org.arnaudlt.warthog.model.user;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public final class OverviewSettings implements Serializable {

    private Integer rows;

    private Integer truncateAfter;

    public OverviewSettings(OverviewSettings overview) {

        this.rows = overview.rows;
        this.truncateAfter = overview.truncateAfter;
    }
}
