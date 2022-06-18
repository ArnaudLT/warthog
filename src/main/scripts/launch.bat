set HADOOP_HOME=%cd%/winutils/
set JAVA_HOME=%cd%/jre/
set PATH=%JAVA_HOME%/bin/;%HADOOP_HOME%/bin/;%PATH%
javaw --add-exports java.base/sun.nio.ch=ALL-UNNAMED -jar warthog-2.0-SNAPSHOT.jar
