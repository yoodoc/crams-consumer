/**
 * KafkaStream 인스턴스로 부터 Data을 읽고, KafkaDataJob 객체를 이용해 data 처리한다.  
 * 
 * @author yoodoc@gmail.com
 */

package com.ktcloudware.crams.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;

/**
 * date format is hardcoded
 * 
 * @author yoodoc
 * 
 */
public class KafkaConsumerService implements Runnable {
    // public List<String> result;
    private KafkaStream<byte[], byte[]> stream;
    private List<CramsConsumerPlugin> plugins;
    private ObjectMapper mapper;
    private String topicName;
    private Logger logger;
    private Logger logger2;

    public KafkaConsumerService(KafkaStream<byte[], byte[]> kafkaStream,
            List<CramsConsumerPlugin> plugins, String topicName) {
        this.topicName = topicName;
        logger = LogManager.getLogger("CONSUMER.MAIN");
        logger2 = LogManager.getLogger("CONSUMER.KAFKADATA");
        stream = kafkaStream;
        this.plugins = plugins;
        for (CramsConsumerPlugin plugin : plugins) {
            logger.info("load plugin" + plugin.getClass().getName());
        }
        logger.info("total " + plugins.size() + " plugins are loaded.");
        mapper = new ObjectMapper();
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

                // kafka message processing
                try {
                    for (CramsConsumerPlugin plugin : plugins) {
                        userData = plugin.excute(userData, dataTag);
                        logger.trace("PLUGIN_RESULT:filtering kafka message with "
                                + plugin.getClass().getName()
                                + ", filtered message:" + userData);
                        if (userData == null) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    // TODO 데몬 동작을 종료해야하는 에러에 대한 처리가 필요하다.
                }

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