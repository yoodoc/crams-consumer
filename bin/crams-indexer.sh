#!/bin/sh
#JAVA_HOME=/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin
JAVA_HOME=/usr/lib/jvm/jre1.7.0_45
#JAVA_HOME=/usr/lib/jvm/java-7-oracle/jre
JSVC=jsvc
DAEMON_USER=cdp
DAEMON_HOME=$(dirname $0)/..
PID_FILE=$DAEMON_HOME/crams-indexer.pid
CLASSPATH=\
$JAVA_HOME:\
$DAEMON_HOME:\
$DAEMON_HOME/lib:\
$DAEMON_HOME/lib/*:\
$DAEMON_HOME/config
case "$1" in

start)
#
# Start Daemon
#

exec $JSVC \
-user $DAEMON_USER \
-debug \
-home $JAVA_HOME \
-outfile $DAEMON_HOME/jsvc-error.log \
-pidfile $DAEMON_HOME/crams-indexer.pid \
-errfile $DAEMON_HOME/jsvc-error.log \
-cp $CLASSPATH \
com.ktcloudware.crams.indexer.IndexerDaemon
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
-pidfile $DAEMON_HOME/crams-indexer.pid \
com.ktcloudware.crams.indexer.IndexerDaemon
exit $?
;;
#
*)
echo "Usage DaemonTest.sh start/stop"
exit 1;;
esac
