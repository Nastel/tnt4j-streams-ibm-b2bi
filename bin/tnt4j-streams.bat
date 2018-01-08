@echo off
setlocal

set RUNDIR=%~dp0
set LIBPATH="%RUNDIR%..\*;%RUNDIR%..\lib\*"
set TNT4JOPTS=-Dtnt4j.config="%RUNDIR%..\config\tnt4j.properties"
set LOG4JOPTS=-Dlog4j.configuration="file:%RUNDIR%..\config\log4j.properties"
REM set LOGBACKOPTS=-Dlogback.configurationFile="file:%RUNDIR%..\config\logback.xml"
set STREAMSOPTS=%STREAMSOPTS% %LOG4JOPTS% %TNT4JOPTS%

IF ["%MAINCLASS%"] EQU [""] (
  set MAINCLASS=com.jkoolcloud.tnt4j.streams.StreamsAgent
)

@echo on
java %STREAMSOPTS% -classpath "%LIBPATH%" %MAINCLASS% %*