package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

public class SystemOutPlugin implements CramsConsumerPlugin {
    public SystemOutPlugin() {
    }

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
