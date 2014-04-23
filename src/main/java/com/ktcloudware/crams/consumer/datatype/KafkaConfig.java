package com.ktcloudware.crams.consumer.datatype;

import java.util.ArrayList;
import java.util.List;

public class KafkaConfig {
    public String zookeeper;
    public String groupId;
    public List<String> topics;
    public int numOfThread;
    public String cacheServer;
    public String resetPolicy;
    public String disableAggregation;

    public KafkaConfig() {
        topics = new ArrayList<String>();
    }
}
