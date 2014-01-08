package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.FiveMinutesFilterPlugin;
import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;

public class FiveMinutesFilterPluginTest {

    @Test
    public void test() {
        CramsConsumerPlugin plugin = new FiveMinutesFilterPlugin();
        try {
            plugin.setProperties("datetime,3");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Map<String, Object> testData = new HashMap<String, Object>();
        try {
            testData.put("datetime", "2013-10-04 14:40:40");
            assertNull(plugin.excute(testData, null));
            testData.put("datetime", "2013-10-04 14:38:40");
            assertNotNull(plugin.excute(testData, null));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
