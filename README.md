# WARTHOG

Warthog helps you to explore, export and to cross data on parquet, json and csv files.  
You can use the visual query builder, or the Hive SQL worksheet for more complex queries.  

## Main Features

####1. Simple query builder

A visual interface to build simple queries. 
Useful to have a quick overview on the data contained in a single file.
You can easily select, aggregate, apply where clauses and sort your results.

![simple query builder](doc/img/visual_query_builder.png)

####2. Hive SQL worksheet

For more flexibility, you can build your own SQL queries with the Hive SQL worksheet.
Useful to run into more complex files like nested columns in a parquet file and to cross data between several files. 
Here you can use all the features of the SQL (Hive) language - manual is available here : https://cwiki.apache.org/confluence/display/Hive/LanguageManual

![hive join query](doc/img/hive_join_query.png)

####3. Quick result overview / Export full result as csv file

To quickly visualize the first rows of your query (_'Run -> Overview'_) 
or to export all the lines in a csv file (_'Run' -> 'Export CSV...'_)

![export_feature](doc/img/export_feature.png)


## Contributing

Contributions are welcome and can take two forms :
* A pull request to fix a bug or to propose a small enhancement.
* An issue with a description and a convenient Label.

