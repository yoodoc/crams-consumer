package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class SimpleLoggingPlugin implements CramsConsumerPlugin {
    private Logger logger;
    
    public SimpleLoggingPlugin() {
        logger = LogManager.getLogger("PLUGINS");
    }

    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) {
        if (dataMap == null) {
            logger.info("null dataMap");
        } else {
            logger.info("filtered data" +  dataMap.toString());
        }
        return dataMap;
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

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }
}
