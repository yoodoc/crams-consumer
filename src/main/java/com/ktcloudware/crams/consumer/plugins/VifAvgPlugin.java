package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.util.KafkaConsumerPluginUtil;

public class VifAvgPlugin implements CramsConsumerPlugin {
    Logger logger = LogManager.getLogger("PLUGINS");
    public static final String VIF_TX_AVG = "vif_tx_avg";
    public static final String VIF_RX_AVG = "vif_rx_avg";

    public VifAvgPlugin() {
        logger = LogManager.getLogger("PLUGINS");
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) throws CramsPluginException {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }

        try {
            dataMap = KafkaConsumerPluginUtil.addAverageValue("vif_[0-9]+_rx",
                    VIF_RX_AVG, dataMap);
        } catch (CramsException e) {
            dataMap = putZeorResult(VIF_RX_AVG, dataMap);
            logger.warn("failed to get average for" + VIF_RX_AVG, e);
        }

        try {
            dataMap = KafkaConsumerPluginUtil.addAverageValue("vif_[0-9]+_tx",
                    VIF_TX_AVG, dataMap);
        } catch (CramsException e) {
            dataMap = putZeorResult(VIF_TX_AVG, dataMap);
            logger.warn("failed to get average for" + VIF_TX_AVG, e);
        }
        return dataMap;
    }

    @Override
    public void setProperties(String pluginProperties) {
        }

    @Override
    public String getProperties() {
        return null;
    }

    @Override
    public boolean needProperties() {
        return false;
    }

    public Map<String, Object> putZeorResult(String key,
            Map<String, Object> dataMap) {
        dataMap.put(key, (long) 0.0);
        return dataMap;
    }

    @Override
    public void stop() {
     
    }
}
