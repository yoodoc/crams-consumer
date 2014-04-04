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
    	logger = LogManager.getLogger("MAIN");
    }

    @Override
    public void run() {
    	logger.debug("run flush task at " + (new Date()).toString());
        for (AverageDataCache dataAggregator : dataAggList) {
            if (pluginExcutor == null) {
                return;
            }
            System.out.println("!!DataAggregatorFlushTimerTask cache stats " + dataAggregator.getStats());
            List<Map<String, Object>> dataList = dataAggregator
                    .cleanIfIdle(1000L);
            System.out.println("!!DataAggregatorFlushTimerTask result data " + dataList.toString());
            System.out.println("!!DataAggregatorFlushTimerTask cache stats " + dataAggregator.getStats());
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
