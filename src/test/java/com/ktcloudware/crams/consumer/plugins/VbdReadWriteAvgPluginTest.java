package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.VbdReadWriteAvgPlugin;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class VbdReadWriteAvgPluginTest {

    @Test
    public void test() {
        Map<String, Object> testData = null;
        try {
            testData = FileUtil
                    .readJsonToMap("singleRrdData1.json");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        VbdReadWriteAvgPlugin plugin = new VbdReadWriteAvgPlugin();
        Map<String, Object> resultMessage = null;
        try {
            resultMessage = plugin.excute(testData, null);
        } catch (CramsPluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        System.out.println(resultMessage);
        assertEquals(879123556L, resultMessage.get("vbd_write_avg"));
    }

    @Test
    public void testForZeroValue() {
        Map<String, Object> testData = null;
        try {
            testData = FileUtil
                    .readJsonToMap("singleRrdData2.json");
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail();
        }
        VbdReadWriteAvgPlugin plugin = new VbdReadWriteAvgPlugin();
        Map<String, Object> resultMessage = null;
        try {
            resultMessage = plugin.excute(testData, null);
        } catch (CramsPluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        System.out.println(resultMessage);
        assertEquals(0L, resultMessage.get("vbd_read_avg"));
    }

}
