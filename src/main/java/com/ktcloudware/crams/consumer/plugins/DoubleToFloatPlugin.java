package com.ktcloudware.crams.consumer.plugins;

import java.util.HashMap;
import java.util.Map;

public class DoubleToFloatPlugin implements CramsConsumerPlugin {
    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }
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
        //nothing to do
    }

    @Override
    public String getProperties() {
        return null;
    }

    @Override
    public boolean needProperties() {
        return false;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

}
