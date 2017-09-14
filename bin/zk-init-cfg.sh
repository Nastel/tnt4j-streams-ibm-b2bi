#! /bin/bash
if command -v realpath >/dev/null 2>&1; then
    SCRIPTPATH=`dirname $(realpath $0)`
else
    SCRIPTPATH=$( cd "$(dirname "$0")" ; pwd -P )
fi

MAINCLASS="com.jkoolcloud.tnt4j.streams.configure.zookeeper.ZKConfigInit"
tnt4j-streams.sh -c -f:$SCRIPTPATH/../config/zk-init-cfg.properties