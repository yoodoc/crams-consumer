package com.ktcloudware.crams.consumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AverageDataCacheFlushTimerTask extends TimerTask {
    private List<AverageDataCache> dataAggList = null;
    private CramsPluginExcutor pluginExcutor;
	private Logger logger;

    public AverageDataCacheFlushTimerTask(AverageDataCache dataAggregator,
            CramsPluginExcutor pluginExcutor) {
        dataAggList = new ArrayList<AverageDataCache>();
        if (dataAggregator != null) {
            dataAggList.add(dataAggregator);
        }
        if (pluginExcutor != null) {
            this.pluginExcutor = pluginExcutor;
        }
    }

    public AverageDataCacheFlushTimerTask() {
    	logger = LogManager.getLogger("CRAMS_CONSUMER");
    }

    @Override
    public void run() {
        for (AverageDataCache dataAggregator : dataAggList) {
        	logger.debug("run flush task at " + (new Date()).toString() + "," + dataAggregator.getStats() );
            if (pluginExcutor == null) {
                return;
            }
            List<Map<String, Object>> dataList = dataAggregator
                    .cleanIfIdle(1000L);
            logger.debug("flushed data: " + dataList.toString());
            for (Map<String, Object> dataMap : dataList) {
                pluginExcutor.excute(dataMap, "aggregatedData");
            }
        }
    }

    public void addAverageDataCache(AverageDataCache dataAggregator) {
        if (dataAggList == null) {
            dataAggList = new ArrayList<AverageDataCache>();
        }
        if (dataAggregator != null) {
            dataAggList.add(dataAggregator);
        }

    }

    public void addCramsPluginExcutor(CramsPluginExcutor pluginExcutor) {
        if (pluginExcutor != null) {
            this.pluginExcutor = pluginExcutor;
        }
    }
}
