package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.VifAvgPlugin;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class VifAvgPluginTest {

    @Test
    public void test() {
        Map<String, Object> testData = null;
        try {
            testData = FileUtil
                    .readJsonToMap("singleRrdData1.json");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail();
        }
        VifAvgPlugin plugin = new VifAvgPlugin();
        Map<String, Object> resultMessage = null;
        try {
            resultMessage = plugin.excute(testData, null);
        } catch (CramsPluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        System.out.println(resultMessage);
        assertEquals(203703075L, resultMessage.get("vif_tx_avg"));
        assertEquals(465025100L, resultMessage.get("vif_rx_avg"));
    }

    @Test
    public void testMissingTargetValue() {
        Map<String, Object> testData = null;
        try {
            testData = FileUtil
                    .readJsonToMap("singleRrdData1.json");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail();
        }
        testData.remove("vif_0_rx");
        VifAvgPlugin plugin = new VifAvgPlugin();
        Map<String, Object> resultMessage = null;
        try {
            resultMessage = plugin.excute(testData, null);
        } catch (CramsPluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        System.out.println(resultMessage);
        assertEquals(203703075L, resultMessage.get("vif_tx_avg"));
        assertEquals(0L, resultMessage.get("vif_rx_avg"));
    }
}
