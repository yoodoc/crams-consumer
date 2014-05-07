package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class ESIndexingPluginTest {

    @Ignore
    @Test
    public void test() {
        ESIndexingPlugin esIndexingPlugin = null;
        try {
            esIndexingPlugin = new ESIndexingPlugin();
        } catch (CramsPluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        
        Map<String, Object> testData = new HashMap<String, Object>();
        testData.put("vm_name", "unittest_name");
        testData.put("vm_account_name", "account_name");
        testData.put("vm_uuid", "u-n-i-t-e-s-t");
        testData.put("vm_cpu", 1);
        testData.put("vm_type", "VR");
        testData.put("cpu0", 0.0000000005);
        testData.put("cpu1", 0.000000005);
        testData.put("datetime", "2013-01-01 11:13:11");
        testData.put("vm_pod_name", "kr-unittest-pod0");
        try {
            esIndexingPlugin.excute(testData, "unittest");
        } catch (CramsPluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    }
}
