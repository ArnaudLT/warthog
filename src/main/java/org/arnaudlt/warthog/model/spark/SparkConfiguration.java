package org.arnaudlt.warthog.model.spark;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.arnaudlt.warthog.model.user.GlobalSettings;
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
                .master("local["+ globalSettings.getSpark().getThreads() +"]")
                .config("spark.ui.enabled", globalSettings.getSpark().getUi())
                .enableHiveSupport()
                .getOrCreate();

        spark.sqlContext().udf()
                .register("arrayToString", arrayToString, DataTypes.StringType);

        spark.sqlContext().udf()
                .register("mapToString", mapToString, DataTypes.StringType);

        spark.sqlContext().sql("CREATE TEMPORARY FUNCTION sum_arrays AS 'org.arnaudlt.warthog.model.hive.SumArrays';");

        return spark;
    }


    private static final UDF1<Seq<Object>, String> arrayToString = array ->
            array != null ? JavaConverters.seqAsJavaList(array).toString() : "";


    private static final UDF1<scala.collection.Map<String,?>, String> mapToString = map ->
            map != null ?JavaConverters.mapAsJavaMap(map).toString().replace("WrappedArray", "") : "";


}

