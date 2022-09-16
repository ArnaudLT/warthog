package org.arnaudlt.warthog.model.user;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public final class SparkSettings implements Serializable {

    private Integer threads;

    private Boolean ui;

    public SparkSettings(SparkSettings spark) {

        this.threads = spark.threads;
        this.ui = spark.ui;
    }
}
