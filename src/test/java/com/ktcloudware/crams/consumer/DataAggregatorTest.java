package com.ktcloudware.crams.consumer;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.DataAggregator;
import com.ktcloudware.crams.consumer.plugins.CramsPluginException;

public class DataAggregatorTest {
   
    @Test
    public void testFiveContinuesData() {
        DataAggregator plugin = new DataAggregator();
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String dataTag = null;

        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (int i = 0; i < 4; i++) {
            dataMap.put(DataAggregator.VM_KEY, "uu_id");
            dataMap.put(DataAggregator.DATE_TIME_KEY, "2014-03-12 11:0" + i + ":00");
            dataMap.put("cpu0", 0.10);
            try {
                resultMap = plugin.getAverage(dataMap);
                assertNull(resultMap);
            } catch (CramsPluginException e) {
                e.printStackTrace();
                fail();
            }
        }
        
        dataMap.put(DataAggregator.DATE_TIME_KEY, "2014-03-12 11:04:00");
        dataMap.put("cpu0", 0.60);
        try {
            resultMap = plugin.getAverage(dataMap);
         assertNotNull(resultMap);
         assertEquals(new Float(0.20), resultMap.get("cpu0"));
        } catch (CramsPluginException e) {
            e.printStackTrace();
            fail();
        }        
    }
    
    @Test
    public void testMissingOneMinuteData() {
        DataAggregator plugin = new DataAggregator();
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String dataTag = null;

        Map<String, Object> resultMap = new HashMap<String, Object>();
        dataMap.put(DataAggregator.VM_KEY, "uu_id");
        dataMap.put(DataAggregator.DATE_TIME_KEY, "2014-03-12 11:10:50");
        dataMap.put("cpu0", 0.10);
        try {
            resultMap = plugin.getAverage(dataMap);
            assertNull(resultMap);
        } catch (CramsPluginException e) {
            e.printStackTrace();
            fail();
        }
        
        dataMap.put(DataAggregator.DATE_TIME_KEY, "2014-03-12 11:11:11");
        dataMap.put("cpu0", 0.20);
        try {
            resultMap = plugin.getAverage(dataMap);
            assertNull(resultMap);
        } catch (CramsPluginException e) {
            e.printStackTrace();
            fail();
        }

        
        dataMap.put(DataAggregator.DATE_TIME_KEY, "2014-03-12 11:15:00");
        dataMap.put("cpu0", 0.30);
        try {
            resultMap = plugin.getAverage(dataMap);
         assertNotNull(resultMap);
         assertEquals(new Float(0.15), resultMap.get("cpu0"));
        } catch (CramsPluginException e) {
            e.printStackTrace();
            fail();
        }        
    }

}
