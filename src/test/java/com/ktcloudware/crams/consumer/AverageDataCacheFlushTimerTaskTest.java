package com.ktcloudware.crams.consumer;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;
import com.ktcloudware.crams.consumer.plugins.CramsPluginException;
import com.ktcloudware.crams.consumer.plugins.SystemOutPlugin;

public class AverageDataCacheFlushTimerTaskTest {

    @Test
    public void test() {
        //create dataAggregator & flushing task for over stacked data
        AverageDataCache dataAggregator = new AverageDataCache();
        List<CramsConsumerPlugin> plugins = new ArrayList<CramsConsumerPlugin>();
        plugins.add(new SystemOutPlugin());
        CramsPluginExcutor pluginExcutor = new CramsPluginExcutor(plugins);

      AverageDataCacheFlushTimerTask flushTimerTask = new AverageDataCacheFlushTimerTask(dataAggregator, pluginExcutor);
      
        //create test data
        Map<String, Object> dataMap = new HashMap<String, Object>();
       
        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (int i = 0; i < 4; i++) {
            dataMap.put(AverageDataCache.VM_KEY, "uu_id1");
            dataMap.put(AverageDataCache.DATE_TIME_KEY, "2014-03-12 11:0" + i + ":00");
            dataMap.put("cpu0", 0.10);
            try {
                resultMap = dataAggregator.getAverage(dataMap);
                assertNull(resultMap);
            } catch (CramsPluginException e) {
                e.printStackTrace();
                fail();
            }
        }
        
        Map<String, Object> resultMap2 = new HashMap<String, Object>();
        for (int i = 0; i < 4; i++) {
            dataMap.put(AverageDataCache.VM_KEY, "uu_id2");
            dataMap.put(AverageDataCache.DATE_TIME_KEY, "2014-03-12 11:0" + i + ":00");
            dataMap.put("cpu0", 0.10);
            try {
                resultMap2 = dataAggregator.getAverage(dataMap);
                assertNull(resultMap2);
            } catch (CramsPluginException e) {
                e.printStackTrace();
                fail();
            }
        }
      
        Timer flushingTaskScheduler = new Timer();
        flushingTaskScheduler.scheduleAtFixedRate(flushTimerTask, 0, 1000);
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
      
    }

}
