@echo off
setlocal

set RUNDIR=%~dp0
set LIBPATH=%RUNDIR%..\tnt4j-streams-ibm-b2bi-1.0.24-runnableTest.jar
set TNT4JOPTS=-Dtnt4j.config="%RUNDIR%..\config\tnt4j.properties"
set LOG4JOPTS=-Dlog4j.configuration="file:///%RUNDIR%..\config\log4j.properties"
set PARSER_CONFIG=-Dtnt4j.streams.config="%RUNDIR%..\samples\B2Bi\tnt4j-streams-ibm-b2bi.properties"
set EVENTS_DIR=-Dtnt4j.b2biSampleEvents="%RUNDIR%..\samples\B2Bi\Events\*.xml"
set STREAMSOPTS=%STREAMSOPTS% %LOG4JOPTS% %TNT4JOPTS% %EVENTS_DIR% %PARSER_CONFIG%

@echo on
java -cp "%LIBPATH%" %STREAMSOPTS% org.junit.runner.JUnitCore com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventListenerTest
pause