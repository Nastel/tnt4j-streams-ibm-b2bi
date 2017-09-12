#! /bin/bash
RUNDIR=`pwd`
LIBPATH="$RUNDIR../tnt4j-streams-ibm-b2bi-1.0.11-SNAPSHOT-runnableTest.jar"
TNT4JOPTS=-Dtnt4j.config="$RUNDIR../config/tnt4j.properties"
LOG4JOPTS=-Dlog4j.configuration="file:/$RUNDIR../config/log4j.properties"
PARSER_CONFIG=-Dtnt4j.streams.config="$RUNDIR../samples/B2Bi/tnt4j-streams-ibm-b2bi.properties"
EVENTS_DIR=-Dtnt4j.b2biSampleEvents="$RUNDIR../samples/B2Bi/Events/*.xml"
STREAMSOPTS=$STREAMSOPTS $LOG4JOPTS $TNT4JOPTS $EVENTS_DIR $PARSER_CONFIG -Dtest=true

java -cp $LIBPATH $STREAMSOPTS org.junit.runner.JUnitCore com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventListenerTest

read -p "Press [Enter] key to exit..."
