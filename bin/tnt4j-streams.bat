@echo off
setlocal

set RUNDIR=%~p0
set LIBPATH="%RUNDIR%..\*;%RUNDIR%..\lib\*"
set TNT4JOPTS=-Dtnt4j.config="%RUNDIR%..\config\tnt4j.properties"
set LOG4JOPTS=-Dlog4j.configuration="file:%RUNDIR%..\config\log4j.properties"
REM set LOGBACKOPTS=-Dlogback.configurationFile="file:%RUNDIR%..\config\logback.xml"
set STREAMSOPTS=%STREAMSOPTS% %LOG4JOPTS% %TNT4JOPTS%

if "%MAINCLASS%" == "" goto set_default_main
goto run_stream

:set_default_main
set MAINCLASS=com.jkoolcloud.tnt4j.streams.StreamsAgent

:run_stream
@echo on
java %STREAMSOPTS% -classpath "%LIBPATH%" %MAINCLASS% %*