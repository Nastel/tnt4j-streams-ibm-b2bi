#! /bin/bash
if command -v readlink >/dev/null 2>&1; then
    SCRIPTPATH=$(dirname $(readlink -m $BASH_SOURCE))
else
    SCRIPTPATH=$(cd "$(dirname "$BASH_SOURCE")" ; pwd -P)
fi

LIBPATH="$SCRIPTPATH/../tnt4j-streams-ibm-b2bi-2.3.0-runnableTest.jar"
TNT4JOPTS="-Dtnt4j.config=$SCRIPTPATH/../config/tnt4j.properties"
LOG4JOPTS="-Dlog4j2.configurationFile=$SCRIPTPATH/../config/log4j2.xml"
PARSER_CONFIG="-Dtnt4j.streams.config=$SCRIPTPATH/../samples/B2Bi/tnt4j-streams-ibm-b2bi.properties"
EVENTS_DIR="-Dtnt4j.b2biSampleEvents=$SCRIPTPATH/../samples/B2Bi/Events/*.xml"
STREAMSOPTS=$STREAMSOPTS $LOG4JOPTS $TNT4JOPTS $EVENTS_DIR $PARSER_CONFIG "-Dtest=true"

java -cp "$LIBPATH" "$STREAMSOPTS" org.junit.runner.JUnitCore com.jkoolcloud.tnt4j.streams.custom.interceptors.castiron.B2BiSfgEventListenerTest

read -p "Press [Enter] key to exit..."
