set LIBPATH="%RUNDIR%\*"
set TNT4JOPTS=-Dtnt4j.config="%RUNDIR%..\config\tnt4j.properties"
set LOG4JOPTS=-Dlog4j.configuration="file:%RUNDIR%..\config\log4j.properties".
set EVENTS_DIR=-Dtnt4j.b2biSasmpleEvents="./samples/B2Bi/Events/*.xml"
set STREAMSOPTS=%STREAMSOPTS% %LOG4JOPTS% %TNT4JOPTS% %EVENTS_DIR%

java -cp %LIBPATH% %STREAMSOPTS% org.junit.runner.JUnitCore com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventStreamTest