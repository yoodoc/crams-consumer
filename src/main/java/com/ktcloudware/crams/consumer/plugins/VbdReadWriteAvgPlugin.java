package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

<<<<<<< HEAD
import com.ktcloudware.crams.consumer.CramsException;
=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
<<<<<<< HEAD
        } catch (CramsException e) {
=======
        } catch (CramsPluginException e) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            logger.warn("can't get average vbd read values from data="
                    + dataMap, e);
            putZeroFloatResult(VBD_READ_AVG, dataMap);
        }

        try {
<<<<<<< HEAD
            dataMap = KafkaConsumerPluginUtil.addAverageValue(
                    "vbd_[a-z]+_write", VBD_WRITE_AVG, dataMap);
        } catch (CramsException e) {
            logger.warn("can't get average vbd write values from data="
                    + dataMap, e);
=======
            logger.warn("can't get average vbd write values from data="
                    + dataMap);
            dataMap = KafkaConsumerPluginUtil.addAverageValue(
                    "vbd_[a-z]+_write", VBD_WRITE_AVG, dataMap);
        } catch (CramsPluginException e) {
            logger.error(e.getMessage(), e);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            putZeroFloatResult(VBD_WRITE_AVG, dataMap);
        }

        return dataMap;
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

    public Map<String, Object> putZeroFloatResult(String key,
            Map<String, Object> dataMap) {
        dataMap.put(key, (float) 0.0);
        return dataMap;
    }
}
