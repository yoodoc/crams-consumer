#!/bin/sh
JAVA_HOME=$JAVA_HOME/bin
JSVC=jsvc
DAEMON_USER=$(whoami)
DAEMON_HOME=$(dirname $0)/..
PID_FILE=$DAEMON_HOME/crams-consumer.pid

CLASSPATH=\
$JAVA_HOME:\
$DAEMON_HOME:\
$DAEMON_HOME/lib:\
$DAEMON_HOME/lib/*:\
$DAEMON_HOME/conf

case "$1" in
    start)
        #
        # Start Daemon
        #

        exec $JSVC \
        -user $DAEMON_USER \
        -debug \
        -home $JAVA_HOME \
        -outfile $DAEMON_HOME/error-jsvc.log \
        -pidfile $DAEMON_HOME/jsvc.pid \
        -errfile $DAEMON_HOME/error-jsvc.log \
        -cp $CLASSPATH \
        -Ddaemon.home=$DAEMON_HOME \
        com.ktcloudware.crams.consumer.MainDaemon
        exit $?
        ;;

    stop)
        $JSVC \
        -stop \
        -pidfile $DAEMON_HOME/jsvc.pid \
        com.ktcloudware.crams.consumer.MainDaemon
        exit $?
        ;;

    *)
        echo "Usage crams-consumer.sh start/stop"
        exit 1;;
esac

