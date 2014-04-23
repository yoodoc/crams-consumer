/**
 * KafkaStream 인스턴스로 부터 Data을 읽고, KafkaDataJob 객체를 이용해 data 처리한다.  
 * 
 * @author yoodoc@gmail.com
 */

package com.ktcloudware.crams.consumer;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;


/**
 * date format is hardcoded
 * 
 * @author yoodoc
 * 
 */
public class KafkaConsumerService implements Runnable {
    // public List<String> result;
    private KafkaStream<byte[], byte[]> stream;
    private ObjectMapper mapper;
    private String topicName;
    private Logger logger;
    private Logger logger2;
    private CramsPluginExcutor runner;
    private DataAggregator dataStorage;
    private boolean disableAggregation = false;

    /**
     * 
     * @param kafkaStream
     * @param topicName
     * @param runner
     * @param dataStorage
     * @throws CramsException 
     */
    public KafkaConsumerService(KafkaStream<byte[], byte[]> kafkaStream,
             String topicName, CramsPluginExcutor runner, DataAggregator dataStorage, boolean disableAggregation) throws CramsException {
        this.topicName = topicName;
        logger = LogManager.getLogger("CRAMS_CONSUMER");
        logger2 = LogManager.getLogger("KAFKADATA");
        stream = kafkaStream;
        mapper = new ObjectMapper();
        this.runner = runner;
        this.dataStorage = dataStorage;
        this.disableAggregation = disableAggregation;
        
        if (this.disableAggregation == false && this.dataStorage == null) {
            throw new CramsException("failed to init");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            Map<Integer, Long> lastOffsetForPartition = new HashMap<Integer, Long>();
            ConsumerIterator<byte[], byte[]> it = stream.iterator();
            int kafkaCount = 0;
            while (it.hasNext()) {
                // read a single kafka message
                Map<String, Object> userData = null;
                byte[] message = null;
                String dataTag = null;
                try {
                    MessageAndMetadata<byte[], byte[]> kafkaData = it.next();
                    message = kafkaData.message();
                    int partition = kafkaData.partition();
                    long offset = kafkaData.offset();
                    dataTag = topicName + "[" + String.valueOf(partition) + "]"
                            + String.valueOf(offset);
                    lastOffsetForPartition.put(partition, offset);
                    logger2.trace("KAFKA_MESSAGE:recieve new kafka message ["
                            + partition + "]" + offset + " : "
                            + new String(message));
                    userData = mapper.readValue(message, Map.class);
                    kafkaCount++;
                } catch (Exception e) {
                    // json format exception
                    logger2.warn("json parsing error : " + e.getMessage(), e);
                    continue;
                }

                //make average 
                if (disableAggregation == false) {
                    userData = dataStorage.add(userData);
                    if (null != userData) {
                        logger.trace("produced avg data = " + userData);
                    }
                }
                runner.excute(userData,dataTag);
                
                logger.trace("total processing data : " + kafkaCount);
                for (Integer partition : lastOffsetForPartition.keySet()) {
                    logger.trace("last [" + partition + "] offset is "
                            + lastOffsetForPartition.get(partition));
                }
            }
            logger.info("kafka broker session closed.");
            return;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return;
    }
}