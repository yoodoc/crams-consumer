/**
 * 
 * @author yoodoc@gmail.com
 */
package com.ktcloudware.crams.consumer.clients;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.ktcloudware.crams.consumer.CramsException;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumerGroup {
    ConsumerConnector kakfaConnector;
    String consumerGroupId;
    String zkAddress;
    int numOfThread = 1;
    private Map<String, Integer> topicCountMap;
    private Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap;

    /**
     * 
     * @param zkAddress
     * @param consumerGroupId
     * @param numOfThread
     * @param topicList
     * @param resetPolicy
     * @throws CramsException 
     */
    public KafkaConsumerGroup(String zkAddress, String consumerGroupId,
            int numOfThread, List<String> topicList, String resetPolicy) throws CramsException {
        this.consumerGroupId = consumerGroupId;
        this.zkAddress = zkAddress;
        this.numOfThread = numOfThread > 0 ? numOfThread : 1;
        connect(zkAddress, consumerGroupId, topicList, resetPolicy);
    }

    /**
     * 
     * @param zk
     * @param groupId
     * @param topicList
     * @throws CramsException 
     */
    void connect(String zk, String groupId, List<String> topicList,
            String resetPolicy) throws CramsException {
        ConsumerConfig consumerConfig = createConsumerConfig(zk, groupId,
                resetPolicy);
        try {
            this.kakfaConnector = kafka.consumer.Consumer
                    .createJavaConsumerConnector(consumerConfig);
        } catch (Exception e) {
            throw new CramsException("failed to create kafka consumer", e);
        }
        topicCountMap = new HashMap<String, Integer>();
        for (String topic : topicList) {
            topicCountMap.put(topic, numOfThread);
        }
        consumerMap = kakfaConnector.createMessageStreams(topicCountMap);
    }

    public List<KafkaStream<byte[], byte[]>> getKafkaStreams(String topic) {
        List<KafkaStream<byte[], byte[]>> streams = null;
        if (consumerMap == null) {
            return streams;
        }
        streams = consumerMap.get(topic);
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
