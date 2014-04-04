package com.ktcloudware.crams.consumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import kafka.consumer.KafkaStream;
import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.clients.KafkaConsumerGroup;
import com.ktcloudware.crams.consumer.datatype.KafkaConfig;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class MainDaemon implements Daemon {

    protected KafkaConfig kafkaConfig = null;
    private List<KafkaConsumerGroup> consumerGroupList;
    private ESBulkIndexer esBulkIndexer;
    protected Map<String, ExecutorService> executorMap;

    private Logger logger = LogManager.getLogger("MAIN");
    private Timer flushingTaskScheduler;

    @Override
    public void init(DaemonContext arg0) throws CramsException {
        logger = LogManager.getLogger("MAIN");

        // read kafka client configuration
        try {
            kafkaConfig = IndexerOptionParser.parseKafkaConsumerProperties();
        } catch (Exception e) {
            logger.error("daemon initiation error", e);
            throw new CramsException("daemon initiation error", e);
        }

        // create empty thread pool for each kafka topic
        executorMap = new HashMap<String, ExecutorService>();
        for (String topic : kafkaConfig.topics) {
            ExecutorService executor = Executors.newFixedThreadPool(kafkaConfig.numOfThread);
                     
            executorMap.put(topic, executor);
        }
        
        //create scheduler for dataAggregator flushing task 
        flushingTaskScheduler = new Timer();
    }

    @Override
    public void destroy() {
        for (String topic : executorMap.keySet()) {
            executorMap.get(topic).shutdownNow();
        }

        if (flushingTaskScheduler != null) {
        	flushingTaskScheduler.cancel();
        	flushingTaskScheduler.purge();
        	flushingTaskScheduler = null;
        }
        
        logger.info("daemon shutdown");
    }

    @Override
    public void start() throws CramsException {
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
            
            CramsPluginExcutor pluginExcutor = new CramsPluginExcutor(IndexerOptionParser.loadKafkaPlugins(topic));

            //create dataAggregator & flushing task for over stacked data
            AverageDataCache dataAggregator = new AverageDataCache();
            AverageDataCacheFlushTimerTask flushTimerTask = new AverageDataCacheFlushTimerTask();
            flushTimerTask.addAverageDataCache(dataAggregator);
            flushTimerTask.addCramsPluginExcutor(pluginExcutor);
           
            try {
                for (int i = 0; i < streams.size(); i++) {
                    //create plugin runner
                    logger.info("start thread=" + i + " for topic=" + topic
                            + " " + streams.size());
                    Runnable worker = new KafkaConsumerService(streams.get(i), topic, pluginExcutor, dataAggregator);
                    executor.execute(worker);
                }
            } catch (Exception e) {
                logger.error("service init fail :" + e.getMessage(), e);
                e.printStackTrace();
                stop();
                return;
            }
            
            //run flushing task 
            
            flushingTaskScheduler.scheduleAtFixedRate(flushTimerTask, 10000, 10000);
            logger.info("run data aggregator");
        }

        logger.info("start all service threads");
    }

    @Override
    public void stop() throws CramsException {
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
