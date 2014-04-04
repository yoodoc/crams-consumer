package com.ktcloudware.crams.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.KafkaStream;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
<<<<<<< HEAD
=======
import org.apache.commons.daemon.DaemonInitException;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.clients.KafkaConsumerGroup;
import com.ktcloudware.crams.consumer.datatype.KafkaConfig;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class MainDaemon implements Daemon {

<<<<<<< HEAD
    protected KafkaConfig kafkaConfig = null;
    private List<KafkaConsumerGroup> consumerGroupList;
    private ESBulkIndexer esBulkIndexer;
    protected Map<String, ExecutorService> executorMap;
=======
    private KafkaConfig kafkaConfig = null;
    private List<KafkaConsumerGroup> consumerGroupList;
    private ESBulkIndexer esBulkIndexer;
    // private CacheClient cacheClient;
    private Map<String, ExecutorService> executorMap;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6

    Logger logger = LogManager.getLogger("MAIN");

    @Override
<<<<<<< HEAD
    public void init(DaemonContext arg0) throws CramsException {
        logger = LogManager.getLogger("MAIN");

        // read kafka client configuration
        try {
            kafkaConfig = IndexerOptionParser.parseKafkaConsumerProperties();
        } catch (Exception e) {
            logger.error("daemon initiation error", e);
            throw new CramsException("daemon initiation error", e);
        }
=======
    public void init(DaemonContext arg0) throws DaemonInitException, Exception {
        logger = LogManager.getLogger("MAIN");

        // read kafka client configuration
        kafkaConfig = IndexerOptionParser.parseKafkaConsumerProperties();
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6

        // create empty thread pool for each kafka topic
        executorMap = new HashMap<String, ExecutorService>();
        for (String topic : kafkaConfig.topics) {
            ExecutorService executor = Executors
                    .newFixedThreadPool(kafkaConfig.numOfThread);
            executorMap.put(topic, executor);
        }
    }

    @Override
    public void destroy() {
        for (String topic : executorMap.keySet()) {
            executorMap.get(topic).shutdownNow();
        }

        logger.info("daemon shutdown");
    }

    @Override
<<<<<<< HEAD
    public void start() throws CramsException {
=======
    public void start() throws Exception {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        // create kafka consumer group & make consumer stream
        consumerGroupList = new ArrayList<KafkaConsumerGroup>();
        KafkaConsumerGroup consumerGroup = new KafkaConsumerGroup(
                kafkaConfig.zookeeper, kafkaConfig.groupId,
                kafkaConfig.numOfThread, kafkaConfig.topics,
                kafkaConfig.resetPolicy);
        consumerGroupList.add(consumerGroup);

        // get kafka consuming streams for each kafka topic. and assign consumer
        // service thread for them
        for (String topic : kafkaConfig.topics) {
            ExecutorService executor = executorMap.get(topic);
            List<KafkaStream<byte[], byte[]>> streams = consumerGroup
                    .getKafkaStreams(topic);

            try {
                for (int i = 0; i < streams.size(); i++) {
                    logger.info("start thread=" + i + " for topic=" + topic
                            + " " + streams.size());
<<<<<<< HEAD
=======
                    System.out.println("start thread=" + i + " for topic="
                            + topic + " " + streams.size());
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
                    Runnable worker = new KafkaConsumerService(streams.get(i),
                            IndexerOptionParser.loadKafkaPlugins(topic), topic);
                    executor.execute(worker);
                }
            } catch (Exception e) {
                logger.error("service init fail :" + e.getMessage(), e);
                e.printStackTrace();
                stop();
                return;
            }
        }

        logger.info("start all service threads");
    }

    @Override
<<<<<<< HEAD
    public void stop() throws CramsException {
=======
    public void stop() throws Exception {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        for (KafkaConsumerGroup consumerGroup : consumerGroupList) {
            consumerGroup.shutdown();
        }
        for (String topic : executorMap.keySet()) {
            executorMap.get(topic).shutdown();
        }
        if (esBulkIndexer != null) {
            esBulkIndexer.shutdown();
        }
    }
}
