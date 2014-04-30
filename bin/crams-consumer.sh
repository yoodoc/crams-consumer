#!/bin/sh
#set environment
#JAVA_HOME=$JAVA_HOME
JAVA_HOME=/usr/lib/jvm/jre1.7.0_45/
JSVC=jsvc
DAEMON_USER=$(whoami)
#DAEMON_HOME=$(dirname $0)/..
ORIGIN_DIR=$(pwd)

uname=$(uname -a|grep -i 'ubuntu')
if [ "$uname" ];
then
    PROJECT_DIR=$(readlink -f $0|xargs dirname)/..
else
    echo this scipt need ubuntu environment
    exit
fi

PID_FILE=$PROJECT_DIR/crams-consumer.pid
CLASSPATH=\
$JAVA_HOME:\
$PROJECT_DIR:\
$PROJECT_DIR/lib:\
$PROJECT_DIR/lib/*:\
$PROJECT_DIR/conf
LOG4J_CONF=$PROJECT_DIR/conf/log4j.properties

#run daemon 
case "$1" in
    start)
        #set logging path
        sed -i s@LOGGING_PATH@$PROJECT_DIR@g $LOG4J_CONF

        #
        # Start Daemon
        #

        exec $JSVC \
        -user $DAEMON_USER \
        -debug \
        -home $JAVA_HOME \
        -outfile $PROJECT_DIR/error-jsvc.log \
        -pidfile $PROJECT_DIR/jsvc.pid \
        -errfile $PROJECT_DIR/error-jsvc.log \
        -cp $CLASSPATH \
        -Ddaemon.home=$PROJECT_DIR \
        com.ktcloudware.crams.consumer.MainDaemon
        exit $?
        ;;

    stop)
        exec $JSVC \
        -stop \
        -pidfile $PROJECT_DIR/jsvc.pid \
        com.ktcloudware.crams.consumer.MainDaemon
        exit $?
        ;;

    *)
        echo "Usage crams-consumer.sh start/stop"
        exit 1;;
esac

