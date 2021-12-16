#! /bin/bash
if command -v realpath >/dev/null 2>&1; then
    SCRIPTPATH=`dirname $(realpath $0)`
else
    SCRIPTPATH=$( cd "$(dirname "$0")" ; pwd -P )
fi

LIBPATH="$SCRIPTPATH/../tnt4j-streams-ibm-b2bi-1.0.26-runnableTest.jar"
TNT4JOPTS="-Dtnt4j.config=$SCRIPTPATH/../config/tnt4j.properties"
LOG4JOPTS="-Dlog4j2.configurationFile=file:/$SCRIPTPATH/../config/log4j2.xml"
PARSER_CONFIG="-Dtnt4j.streams.config=$SCRIPTPATH/../samples/B2Bi/tnt4j-streams-ibm-b2bi.properties"
EVENTS_DIR="-Dtnt4j.b2biSampleEvents=$SCRIPTPATH/../samples/B2Bi/Events/*.xml"
STREAMSOPTS=$STREAMSOPTS $LOG4JOPTS $TNT4JOPTS $EVENTS_DIR $PARSER_CONFIG "-Dtest=true"

java -cp "$LIBPATH" "$STREAMSOPTS" org.junit.runner.JUnitCore com.jkoolcloud.tnt4j.streams.custom.inputs.B2BiSfgEventListenerTest

read -p "Press [Enter] key to exit..."
