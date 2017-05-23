#! /bin/bash
if command -v realpath >/dev/null 2>&1; then
    SCRIPTPATH=`dirname $(realpath $0)`
else
    SCRIPTPATH=$( cd "$(dirname "$0")" ; pwd -P )
fi

RUNDIR=`pwd`
LIBPATH="$SCRIPTPATH/../*:$SCRIPTPATH/../lib/*"
TNT4JOPTS="-Dtnt4j.config=$SCRIPTPATH/../config/tnt4j.properties"
LOG4JOPTS="-Dlog4j.configuration=file:$SCRIPTPATH/../config/log4j.properties"
#LOGBACKOPTS="-Dlogback.configurationFile=file:$SCRIPTPATH/../config/logback.xml"
STREAMSOPTS="$STREAMSOPTS $LOG4JOPTS $TNT4JOPTS"

if [ "$MAINCLASS" == "" ]; then
	MAINCLASS="com.jkoolcloud.tnt4j.streams.StreamsAgent"
fi

java $STREAMSOPTS -classpath "$LIBPATH" $MAINCLASS $*