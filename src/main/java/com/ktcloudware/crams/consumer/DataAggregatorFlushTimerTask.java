package com.ktcloudware.crams.consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class DataAggregatorFlushTimerTask extends TimerTask {
    private List<DataAggregator> dataAggList = null;
    private CramsPluginExcutor pluginExcutor;

    public DataAggregatorFlushTimerTask(DataAggregator dataAggregator,
            CramsPluginExcutor pluginExcutor) {
        dataAggList = new ArrayList<DataAggregator>();
        if (dataAggregator != null) {
            dataAggList.add(dataAggregator);
        }
        if (pluginExcutor != null) {
            this.pluginExcutor = pluginExcutor;
        }
    }

    public DataAggregatorFlushTimerTask() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void run() {
        for (DataAggregator dataAggregator : dataAggList) {
            if (pluginExcutor == null) {
                return;
            }
            List<Map<String, Object>> dataList = dataAggregator
                    .cleanIfIdle(1000L);
            System.out.println("!!DataAggregatorFlushTimerTask result data " + dataList.toString());
            for (Map<String, Object> dataMap : dataList) {
                pluginExcutor.excute(dataMap, "aggregatedData");
            }
        }
    }

    public void addDataAggregator(DataAggregator dataAggregator) {
        if (dataAggList == null) {
            dataAggList = new ArrayList<DataAggregator>();
        }
        if (dataAggregator != null) {
            dataAggList.add(dataAggregator);
        }

    }

    public void addPluginExcutor(CramsPluginExcutor pluginExcutor) {
        if (pluginExcutor != null) {
            this.pluginExcutor = pluginExcutor;
        }
    }
}
