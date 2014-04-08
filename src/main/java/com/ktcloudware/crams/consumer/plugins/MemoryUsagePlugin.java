package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

/**
 * memory 평균 사용율에 대한 {"memory_usage":<double>} 필드를 추가한다.
 * 
 * @author yoodoc
 * 
 */
public class MemoryUsagePlugin implements CramsConsumerPlugin {

    private String memoryFreeName;
    private String memoryName;
    private String memoryUsageName;

    public MemoryUsagePlugin() {
        memoryFreeName = "memory_internal_free";
        memoryName = "memory";
        memoryUsageName = "memory_usage";
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) throws CramsPluginException {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }
        
        try {
            Object freeMemObj = (float) 0.0;
            Object totalMemObj = (float) 0.0;
            double freeMem = 0;
            double totalMem = 0;
            for (String keyName : dataMap.keySet()) {
                if (keyName.matches(this.memoryFreeName)) {
                    freeMemObj = dataMap.get(keyName);
                } else if (keyName.matches(this.memoryName)) {
                    totalMemObj = dataMap.get(keyName);
                }
            }

            // cast values to Double.class
            if (freeMemObj instanceof Integer) {
                freeMem = ((Integer) freeMemObj).doubleValue();
            } else if (freeMemObj instanceof Long) {
                freeMem = ((Long) freeMemObj).doubleValue();
            } else if (freeMemObj instanceof Float) {
                freeMem = ((Float) freeMemObj).doubleValue();
            } else {
                freeMem = (Double) freeMemObj;
            }

            if (totalMemObj instanceof Integer) {
                totalMem = ((Integer) totalMemObj).doubleValue();
            } else if (totalMemObj instanceof Long) {
                totalMem = ((Long) totalMemObj).doubleValue();
            } else if (totalMemObj instanceof Float) {
                totalMem = ((Float) totalMemObj).doubleValue();
            } else {
                totalMem = (Double) totalMemObj;
            }

            // get usage value & append dataMap
            if (totalMem == (double) 0) {
                throw new CramsPluginException("totol memory is 0"
                        + ", data log = " + dataMap.toString());
            }

            double usedMemory = totalMem - (freeMem * 1024);
            if (usedMemory < 0) {
                throw new CramsPluginException(
                        "negative value for memory usage," + dataMap.toString());
            }
            dataMap.put(memoryUsageName, usedMemory / totalMem);
            return dataMap;
        } catch (Exception e) {
            throw new CramsPluginException(e.getMessage(), e);
        }
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
