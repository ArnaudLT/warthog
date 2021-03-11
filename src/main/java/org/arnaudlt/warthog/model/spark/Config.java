package org.arnaudlt.warthog.model.spark;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.io.*;

@Slf4j
@Configuration
public class Config {


    @Bean
    public GlobalSettings getGlobalSettings(@Value("${warthog.spark.threads}") Integer sparkThreads,
                                            @Value("${warthog.spark.ui}") Boolean sparkUI,
                                            @Value("${warthog.overview.rows}") Integer overviewRows) {

        GlobalSettings settings;
        try {

            settings = GlobalSettings.deserialize();
        } catch (IOException | ClassNotFoundException e) {

            log.warn("Unable to read settings");
            settings = new GlobalSettings(sparkThreads, sparkUI, overviewRows);
            try {

                GlobalSettings.serialize(settings);
            } catch (IOException ioException) {
                log.error("Unable to write settings", ioException);
            }
        }

        return settings;
    }


    @Bean
    @Autowired
    public SparkSession getSpark(GlobalSettings globalSettings) {

        SparkSession spark = SparkSession
                .builder()
                .appName("Warthog")
                .master("local["+ globalSettings.getSparkThreads() +"]")
                .config("spark.ui.enabled", globalSettings.getSparkUI())
                .enableHiveSupport()
                .getOrCreate();

        spark.sqlContext().udf()
                .register("arrayToString", arrayToString, DataTypes.StringType);

        spark.sqlContext().udf()
                .register("mapToString", mapToString, DataTypes.StringType);

        return spark;
    }


    private static final UDF1<Seq<Object>, String> arrayToString = array ->
            array != null ? JavaConverters.seqAsJavaList(array).toString() : "";


    private static final UDF1<scala.collection.Map<String,?>, String> mapToString = map ->
            map != null ?JavaConverters.mapAsJavaMap(map).toString().replace("WrappedArray", "") : "";


}

