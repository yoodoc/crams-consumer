/**
 * 
 * @author yoodoc@gmail.com
 */
package com.ktcloudware.crams.consumer.clients;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

<<<<<<< HEAD
import com.ktcloudware.crams.consumer.CramsException;

=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumerGroup {
    ConsumerConnector kakfaConnector;
    String consumerGroupId;
    String zkAddress;
<<<<<<< HEAD
    int numOfThread = 1;
    private Map<String, Integer> topicCountMap;
=======
    int m_numOfThread = 1;
    private HashMap<String, Integer> topicCountMap;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    private Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap;

    /**
     * 
     * @param zkAddress
     * @param consumerGroupId
     * @param numOfThread
     * @param topicList
     * @param resetPolicy
<<<<<<< HEAD
     * @throws CramsException 
     */
    public KafkaConsumerGroup(String zkAddress, String consumerGroupId,
            int numOfThread, List<String> topicList, String resetPolicy) throws CramsException {
        this.consumerGroupId = consumerGroupId;
        this.zkAddress = zkAddress;
        this.numOfThread = numOfThread > 0 ? numOfThread : 1;
=======
     */
    public KafkaConsumerGroup(String zkAddress, String consumerGroupId,
            int numOfThread, List<String> topicList, String resetPolicy) {
        this.consumerGroupId = consumerGroupId;
        this.zkAddress = zkAddress;
        this.m_numOfThread = numOfThread > 0 ? numOfThread : 1;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        connect(zkAddress, consumerGroupId, topicList, resetPolicy);
    }

    /**
     * 
     * @param zk
     * @param groupId
     * @param topicList
<<<<<<< HEAD
     * @throws CramsException 
     */
    void connect(String zk, String groupId, List<String> topicList,
            String resetPolicy) throws CramsException {
=======
     */
    void connect(String zk, String groupId, List<String> topicList,
            String resetPolicy) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        ConsumerConfig consumerConfig = createConsumerConfig(zk, groupId,
                resetPolicy);
        try {
            this.kakfaConnector = kafka.consumer.Consumer
                    .createJavaConsumerConnector(consumerConfig);
        } catch (Exception e) {
<<<<<<< HEAD
            throw new CramsException("failed to create kafka consumer", e);
        }
        topicCountMap = new HashMap<String, Integer>();
        for (String topic : topicList) {
            topicCountMap.put(topic, numOfThread);
=======
            throw new RuntimeException(e);
        }
        topicCountMap = new HashMap<String, Integer>();
        for (String topic : topicList) {
            topicCountMap.put(topic, m_numOfThread);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        }
        consumerMap = kakfaConnector.createMessageStreams(topicCountMap);
    }

    public List<KafkaStream<byte[], byte[]>> getKafkaStreams(String topic) {
<<<<<<< HEAD
        List<KafkaStream<byte[], byte[]>> streams = null;
        if (consumerMap == null) {
            return streams;
        }
        streams = consumerMap.get(topic);
=======
        if (consumerMap == null)
            return null;
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        return streams;
    }

    public void shutdown() {
        kakfaConnector.shutdown();
        kakfaConnector = null;
    }

    private static ConsumerConfig createConsumerConfig(String zookeeper,
            String groupId, String resetPolicy) {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper);
        props.put("group.id", groupId);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "3000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("fetch.size", "500000");
        props.put("auto.offset.reset", resetPolicy);

        return new ConsumerConfig(props);
    }

}
