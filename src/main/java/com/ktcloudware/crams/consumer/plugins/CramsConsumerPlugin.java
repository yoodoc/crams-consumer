package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

public interface CramsConsumerPlugin {
    /**
     * pluginProperties : comma seperatied properties for each
     * kafkaConsumerPlugin implements
     * 
     * @param pluginProperties
<<<<<<< HEAD
      * @throws CramsPluginException 
=======
     * @throws CramsPluginException
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
}
