#!/bin/sh
<<<<<<< HEAD
#set environment
JAVA_HOME=$JAVA_HOME/bin
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

=======
#JAVA_HOME=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin
#JAVA_HOME=/usr/lib/jvm/jre1.7.0_45
JAVA_HOME=/usr/lib/jvm/java-7-oracle/jre
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
#
# To get a verbose JVM
#-verbose \
# To get a debug of jsvc.
#-debug \
exit $?
;;

stop)
#
# Stop PostMan
#
$JSVC \
-stop \
-pidfile $DAEMON_HOME/jsvc.pid \
com.ktcloudware.crams.consumer.IndexerDaemon
exit $?
;;
#
*)
echo "Usage DaemonTest.sh start/stop"
exit 1;;
esac
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
