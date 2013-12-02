#!/bin/bash

mvn clean

mvn install:install-file -Dfile=$PWD/src/main/lib/kafka_2.8.0-0.8.0-beta1.jar -DgroupId=com.ktcloudware.kafka -DartifactId=kafka_2.8.0 -Dversion=0.8.0-beta1 -Dpackaging=jar

mvn install:install-file -Dfile=src/main/lib/kafka-assembly-0.8.0-beta1-deps.jar -DgroupId=com.ktcloudware.kafka -DartifactId=kafka_assembly -Dversion=0.8.0-beta1 -Dpackaging=jar

mvn assembly:assembly

