package com.ktcloudware.crams.consumer;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.VMPerfData;

public class VMPerfDataTest {

    @Test
    public void test() {
        VMPerfData perfData = new VMPerfData();
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("cpu0", 1.0);
        perfData.addPerfData(dataMap);
        dataMap.put("cpu0", 2.0);
        dataMap.put("cpu1", 2.0);
        perfData.addPerfData(dataMap);
        dataMap = perfData.getAvgValuesData();
        assertEquals(new Float(1.5), dataMap.get("cpu0"));
        assertEquals(null, dataMap.get("cpu1"));
    }

}
