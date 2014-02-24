#!/bin/sh
#set environment
JAVA_HOME=$JAVA_HOME/bin
JSVC=jsvc
DAEMON_USER=$(whoami)
#DAEMON_HOME=$(dirname $0)/..
ORIGIN_DIR=$(pwd)

uname=$(uname -a|grep -i 'ubuntu')
if [ "$uname" ];
then
    $PROJECT_DIR=$(readlink -f $0|xargs dirname)/..
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

#run daemon 
case "$1" in
    start)
        #
        # Start Daemon
        #

        exec $PROJECT_DIR/$JSVC \
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
        exec $PROJECT_DIR/$JSVC \
        -stop \
        -pidfile $PROJECT_DIR/jsvc.pid \
        com.ktcloudware.crams.consumer.MainDaemon
        exit $?
        ;;

    *)
        echo "Usage crams-consumer.sh start/stop"
        exit 1;;
esac

