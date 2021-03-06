package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

public interface CramsConsumerPlugin {
    /**
     * pluginProperties : comma seperatied properties for each
     * kafkaConsumerPlugin implements
     * 
     * @param pluginProperties
      * @throws CramsPluginException 
     */
    public void setProperties(String pluginProperties)
            throws CramsPluginException;

    public String getProperties();

    /**
     * excute plugin
     * 
     * @param dataMap
     * @return
     */

    public boolean needProperties();

    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) throws CramsPluginException;

    public void stop();
}
