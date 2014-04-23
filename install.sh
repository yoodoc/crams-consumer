#!/bin/bash

ORIGIN_DIR=$(pwd)
uname=$(uname -a|grep -i 'ubuntu')
if [ "$uname" ];
then
    PROJECT_DIR=$(readlink -f $0|xargs dirname)
else
    echo this scipt need ubuntu environment
    exit
fi

#set config for each server environment
case "$1" in
    develop)
        #set configuration for develop environment
        for i in `ls $PROJECT_DIR/conf|grep dev`
        do
            #name=$(echo $i|awk -F . '/.dev/{print $1"."$2}')
            name=$(echo $i|sed 's/properties.dev/properties/')
            cp $PROJECT_DIR/conf/$i $PROJECT_DIR/conf/$name
        done
        for i in `ls $PROJECT_DIR/bin|grep dev`
        do
            name=$(echo $i|sed 's/sh.dev/sh/')
            cp $PROJECT_DIR/bin/$i $PROJECT_DIR/bin/$name
        done
        ;;
    staging)
        ;;
    product)
        ;;
    *)
        ;;
esac

cd $PROJECT_DIR
mvn clean
mvn install:install-file -Dfile=$PROJECT_DIR/src/main/lib/kafka_2.8.0-0.8.0.jar -DgroupId=com.ktcloudware.kafka -DartifactId=kafka_2.8.0 -Dversion=0.8.0 -Dpackaging=jar
#mvn install:install-file -Dfile=$PWD/src/main/lib/kafka_2.8.0-0.8.0-beta1.jar -DgroupId=com.ktcloudware.kafka -DartifactId=kafka_2.8.0 -Dversion=0.8.0-beta1 -Dpackaging=jar
#mvn install:install-file -Dfile=src/main/lib/kafka-assembly-0.8.0-beta1-deps.jar -DgroupId=com.ktcloudware.kafka -DartifactId=kafka_assembly -Dversion=0.8.0-beta1 -Dpackaging=jar
mvn install:install-file -Dfile=$PROJECT_DIR/src/main/lib/kafka_2.8.0-0.8.0.jar -DgroupId=com.ktcloudware.kafka -DartifactId=kafka_2.8.0 -Dversion=0.8.0 -Dpackaging=jar
mvn assembly:assembly -DskipTests
cd $ORIGIN_DIR

