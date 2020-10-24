package org.arnaudlt.warthog.model.spark;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import scala.collection.JavaConverters;
import scala.collection.Seq;


@Configuration
public class SparkInstance {


    @Bean
    public SparkSession getSpark() {

        SparkSession spark = SparkSession
                .builder()
                .appName("dataset-explorer")
                .master("local[4]")
                .config("spark.ui.enabled", false)
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
