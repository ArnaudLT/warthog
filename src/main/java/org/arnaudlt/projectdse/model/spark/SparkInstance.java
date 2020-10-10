package org.arnaudlt.projectdse.model.spark;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SparkInstance {


    @Bean
    public SparkSession getSpark() {

        return SparkSession
                .builder()
                .appName("project-blt")
                .master("local[4]")
                .config("spark.ui.enabled", false)
                .enableHiveSupport()
                .getOrCreate();
    }
}
