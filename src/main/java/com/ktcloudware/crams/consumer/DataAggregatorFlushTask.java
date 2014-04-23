package com.ktcloudware.crams.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class DataAggregatorFlushTask extends TimerTask {
    private List<DataAggregator> dataCacheList = null;
    private CramsPluginExcutor pluginExcutor;
	private Logger logger;

    public DataAggregatorFlushTask(DataAggregator dataCache,
            CramsPluginExcutor pluginExcutor) {
        logger = LogManager.getLogger("CRAMS_CONSUMER");
        dataCacheList = new ArrayList<DataAggregator>();
        if (dataCache != null) {
            dataCacheList.add(dataCache);
        }
        if (pluginExcutor != null) {
            this.pluginExcutor = pluginExcutor;
        }
    }

    public DataAggregatorFlushTask() {
    	logger = LogManager.getLogger("CRAMS_CONSUMER");
    }

    @Override
    public void run() {
        clean();
    }

    public void addAverageDataCache(DataAggregator dataAggregator) {
        if (dataCacheList == null) {
            dataCacheList = new ArrayList<DataAggregator>();
        }
        if (dataAggregator != null) {
            dataCacheList.add(dataAggregator);
        }

    }

    public void addCramsPluginExcutor(CramsPluginExcutor pluginExcutor) {
        if (pluginExcutor != null) {
            this.pluginExcutor = pluginExcutor;
        }
    }

    public synchronized void clean() {
        for (DataAggregator dataCache : dataCacheList) {
            if (pluginExcutor == null) {
                return;
            }
            List<Map<String, Object>> dataList = dataCache
                    .cleanIfIdle(1000L);
            if (dataList != null && dataList.size() != 0) {
                logger.info("run flush task," + dataCache.getStats() +", flushed data: " + dataList.toString());
            }
            if (dataList == null || dataList.size() == 0) {
                pluginExcutor.excute(null, "aggregatedData");
            }
                
            for (Map<String, Object> dataMap : dataList) {
                pluginExcutor.excute(dataMap, "aggregatedData");
            }
        }
        
    }
}
