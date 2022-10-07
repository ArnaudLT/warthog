package org.arnaudlt.warthog.model.user;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class SparkSettings {

    private Integer threads;

    private Boolean ui;


    public SparkSettings(SparkSettings spark) {

        this.threads = spark.threads;
        this.ui = spark.ui;
    }


    public SparkSettings(GlobalSettings.SerializableSparkSettings spark) {

        if (spark != null) {
            this.threads = spark.getThreads();
            this.ui = spark.getUi();
        }
    }
}
