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
<<<<<<< HEAD
        //nothing to do
=======
        // TODO Auto-generated method stub

>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    }

    @Override
    public String getProperties() {
<<<<<<< HEAD
=======
        // TODO Auto-generated method stub
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        return null;
    }

    @Override
    public boolean needProperties() {
<<<<<<< HEAD
=======
        // TODO Auto-generated method stub
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        return false;
    }

}
