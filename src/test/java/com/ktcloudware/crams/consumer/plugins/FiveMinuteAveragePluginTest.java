package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class FiveMinuteAveragePluginTest {

    @Test
    public void test() {
        FiveMinuteAveragePlugin plugin = new FiveMinuteAveragePlugin();
        Map<String, Object> dataMap = new HashMap<String, Object>();
        String dataTag = null;

        Map<String, Object> resultMap = new HashMap<String, Object>();
        dataMap.put(FiveMinuteAveragePlugin.VM_KEY, "uu_id");
        dataMap.put(FiveMinuteAveragePlugin.DATE_TIME_KEY, "2014-03-12 11:10:50");
        dataMap.put("cpu0", 0.10);
        try {
            resultMap = plugin.excute(dataMap, dataTag);
            assertNull(resultMap);
        } catch (CramsPluginException e) {
            e.printStackTrace();
            fail();
        }
        
        dataMap.put(FiveMinuteAveragePlugin.DATE_TIME_KEY, "2014-03-12 11:11:11");
        dataMap.put("cpu0", 0.20);
        try {
            resultMap = plugin.excute(dataMap, dataTag);
            assertNull(resultMap);
        } catch (CramsPluginException e) {
            e.printStackTrace();
            fail();
        }

        
        dataMap.put(FiveMinuteAveragePlugin.DATE_TIME_KEY, "2014-03-12 11:15:00");
        dataMap.put("cpu0", 0.30);
        try {
            resultMap = plugin.excute(dataMap, dataTag);
         assertNotNull(resultMap);
         assertEquals(new Float(0.15), resultMap.get("cpu0"));
        } catch (CramsPluginException e) {
            e.printStackTrace();
            fail();
        }        
    }

}
