package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.util.KafkaConsumerPluginUtil;

public class SystemOutPlugin implements CramsConsumerPlugin {
    public SystemOutPlugin() {
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) {
        if (dataMap == null) {
            System.out.println("!![SystemOutPlugin] null dataMap");
        }
        System.out.println("!![SystemOutPlugin]" + dataMap.toString());
        return null;
    }

    @Override
    public void setProperties(String pluginProperties) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean needProperties() {
        return false;
    }

    @Override
    public String getProperties() {
        // TODO Auto-generated method stub
        return null;
    }
}
