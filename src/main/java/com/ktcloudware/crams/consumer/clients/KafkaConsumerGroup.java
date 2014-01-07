/**
 * 
 * @author yoodoc@gmail.com
 */
package com.ktcloudware.crams.consumer.clients;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class KafkaConsumerGroup {
    ConsumerConnector kakfaConnector;
    String consumerGroupId;
    String zkAddress;
    int m_numOfThread = 1;
    private HashMap<String, Integer> topicCountMap;
    private Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap;

    /**
     * 
     * @param zkAddress
     * @param consumerGroupId
     * @param numOfThread
     * @param topicList
     * @param resetPolicy
     */
    public KafkaConsumerGroup(String zkAddress, String consumerGroupId,
            int numOfThread, List<String> topicList, String resetPolicy) {
        this.consumerGroupId = consumerGroupId;
        this.zkAddress = zkAddress;
        this.m_numOfThread = numOfThread > 0 ? numOfThread : 1;
        connect(zkAddress, consumerGroupId, topicList, resetPolicy);
    }

    /**
     * 
     * @param zk
     * @param groupId
     * @param topicList
     */
    void connect(String zk, String groupId, List<String> topicList,
            String resetPolicy) {
        ConsumerConfig consumerConfig = createConsumerConfig(zk, groupId,
                resetPolicy);
        try {
            this.kakfaConnector = kafka.consumer.Consumer
                    .createJavaConsumerConnector(consumerConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        topicCountMap = new HashMap<String, Integer>();
        for (String topic : topicList) {
            topicCountMap.put(topic, m_numOfThread);
        }
        consumerMap = kakfaConnector.createMessageStreams(topicCountMap);
    }

    public List<KafkaStream<byte[], byte[]>> getKafkaStreams(String topic) {
        if (consumerMap == null)
            return null;
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
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
