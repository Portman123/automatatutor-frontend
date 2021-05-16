@echo off

java -Xmx512M -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar .\sbt-launch.jar
