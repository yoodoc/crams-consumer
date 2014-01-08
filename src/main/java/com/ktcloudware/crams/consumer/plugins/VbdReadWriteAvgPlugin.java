package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.util.KafkaConsumerPluginUtil;

public class VbdReadWriteAvgPlugin implements CramsConsumerPlugin {
    private ReplaceDiskFieldNamePlugin replaceName;
    public static final String VBD_READ_AVG = "vbd_read_avg";
    public static final String VBD_WRITE_AVG = "vbd_write_avg";
    private Logger logger;

    public VbdReadWriteAvgPlugin() {
        logger = LogManager.getLogger("PLUGINS");
        replaceName = new ReplaceDiskFieldNamePlugin();
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) throws CramsPluginException {
        if (dataMap == null || dataMap.isEmpty()) {
            throw new CramsPluginException("null input dataMap");
        }

        dataMap = replaceName.excute(dataMap, dataTag);

        try {
            dataMap = KafkaConsumerPluginUtil.addAverageValue(
                    "vbd_[a-z]+_read", VBD_READ_AVG, dataMap);
        } catch (CramsException e) {
            logger.warn("can't get average vbd read values from data="
                    + dataMap, e);
            putZeroFloatResult(VBD_READ_AVG, dataMap);
        }

        try {
            logger.warn("can't get average vbd write values from data="
                    + dataMap);
            dataMap = KafkaConsumerPluginUtil.addAverageValue(
                    "vbd_[a-z]+_write", VBD_WRITE_AVG, dataMap);
        } catch (CramsException e) {
            logger.error(e.getMessage(), e);
            putZeroFloatResult(VBD_WRITE_AVG, dataMap);
        }

        return dataMap;
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

    public Map<String, Object> putZeroFloatResult(String key,
            Map<String, Object> dataMap) {
        dataMap.put(key, (float) 0.0);
        return dataMap;
    }
}
