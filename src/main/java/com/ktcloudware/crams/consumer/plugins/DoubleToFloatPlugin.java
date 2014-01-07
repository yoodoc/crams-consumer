package com.ktcloudware.crams.consumer.plugins;

import java.util.HashMap;
import java.util.Map;

public class DoubleToFloatPlugin implements CramsConsumerPlugin {
    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) {
        Map<String, Object> newData = new HashMap<String, Object>();
        for (String dataKey : dataMap.keySet()) {
            Object value = dataMap.get(dataKey);
            if (value instanceof Double) {
                value = (Float) value;
            }
            newData.put(dataKey, value);
        }
        return newData;
    }

    @Override
    public void setProperties(String pluginProperties) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean needProperties() {
        // TODO Auto-generated method stub
        return false;
    }

}
