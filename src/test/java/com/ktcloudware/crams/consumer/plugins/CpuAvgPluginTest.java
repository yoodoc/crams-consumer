package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.CpuAvgPlugin;

public class CpuAvgPluginTest {

    @Test
    public void test() {
        Map<String, Object> testData = new HashMap<String, Object>();
        testData.put("vm_name", "unittest_name");
        testData.put("vm_account_name", "account_name");
        testData.put("vm_uuid", "u-n-i-t-e-s-t");
        testData.put("vm_cpu", 1);
        testData.put("vm_type", "VR");
        testData.put("cpu0", 0.5);
        testData.put("cpu1", 0);
        CpuAvgPlugin plugin = new CpuAvgPlugin();
        assertEquals((float) 0.25,
                plugin.excute(testData, null).get("cpu_avg"));
    }

    @Test
    public void testNoCpuInfo() {
        Map<String, Object> testData = new HashMap<String, Object>();
        testData.put("vm_name", "unittest_name");
        testData.put("vm_account_name", "account_name");
        testData.put("vm_uuid", "u-n-i-t-e-s-t");
        testData.put("vm_cpu", 0);
        testData.put("vm_type", "VR");
        CpuAvgPlugin plugin = new CpuAvgPlugin();
        assertEquals((float) 0.0, plugin.excute(testData, null).get("cpu_avg"));
    }
}
