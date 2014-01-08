package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.util.FileUtil;

public class ReplaceVmAccountNamePluginTest {

    @Test
    public void test() {
        Map<String, Object> testData = null;
        try {
            testData = FileUtil
                    .readJsonToMap("singleRrdData1.json");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        ReplaceVmAccountNamePlugin plugin = new ReplaceVmAccountNamePlugin();
        Map<String, Object> resultMessage = plugin.excute(testData, null);
        System.out.println(resultMessage);
        assertEquals("M000", resultMessage.get("vm_account_name"));
        assertEquals("EPC_M000_S1820", resultMessage.get("vm_account"));
    }

}
