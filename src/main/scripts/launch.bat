set HADOOP_HOME=%cd%/winutils/
set JAVA_HOME=%cd%/jre/
set PATH=%JAVA_HOME%/bin/;%HADOOP_HOME%/bin/;%PATH%
javaw --add-exports java.base/sun.nio.ch=ALL-UNNAMED ^
--add-opens=java.base/java.lang=ALL-UNNAMED ^
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED ^
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED ^
--add-opens=java.base/java.io=ALL-UNNAMED ^
--add-opens=java.base/java.net=ALL-UNNAMED ^
--add-opens=java.base/java.nio=ALL-UNNAMED ^
--add-opens=java.base/java.util=ALL-UNNAMED ^
--add-opens=java.base/java.util.concurrent=ALL-UNNAMED ^
--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED ^
--add-opens=java.base/sun.nio.ch=ALL-UNNAMED ^
--add-opens=java.base/sun.nio.cs=ALL-UNNAMED ^
--add-opens=java.base/sun.security.action=ALL-UNNAMED ^
--add-opens=java.base/sun.util.calendar=ALL-UNNAMED -jar warthog-2.2.2-SNAPSHOT.jar
