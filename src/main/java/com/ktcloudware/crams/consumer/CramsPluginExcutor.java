package com.ktcloudware.crams.consumer;

import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;

public class CramsPluginExcutor {
    private List<CramsConsumerPlugin> plugins;
    private Logger logger;

    public CramsPluginExcutor(List<CramsConsumerPlugin> plugins) {
        logger = LogManager.getLogger("CRAMS_CONSUMER");
        this.plugins = plugins;
        for (CramsConsumerPlugin plugin : plugins) {
            logger.info("load plugin" + plugin.getClass().getName());
        }
        logger.info("total " + plugins.size() + " plugins are loaded.");

    }

    public void excute(Map<String, Object> dataMap, String dataTag) {
        // kafka message processing
        try {
            for (CramsConsumerPlugin plugin : plugins) {
                dataMap = plugin.excute(dataMap, dataTag);
                if (dataMap != null && !dataMap.isEmpty()) {
                    logger.trace("PLUGIN_RESULT:filtering kafka message with "
                            + plugin.getClass().getName()
                            + ", filtered message:" + dataMap);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // TODO 데몬 동작 종료시 발생하는 에러에 대한 처리가 필요하다.
        }
    }
}
