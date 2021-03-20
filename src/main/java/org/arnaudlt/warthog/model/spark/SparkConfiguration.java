package org.arnaudlt.warthog.model.spark;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.arnaudlt.warthog.model.setting.GlobalSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scala.collection.JavaConverters;
import scala.collection.Seq;

@Slf4j
@Configuration
public class SparkConfiguration {


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

