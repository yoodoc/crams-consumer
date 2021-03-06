package com.ktcloudware.crams.consumer;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.util.VMPerfDataUtil;

public class VMPerfDataTest {

    @Test
    public void test() {
        VMPerfDataUtil perfData = new VMPerfDataUtil();
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("cpu0", 1.0);
        dataMap.put("datetime-in-minutes", 1L);
        perfData.addPerfData(dataMap);
        dataMap.put("datetime-in-minutes", 2L);
        dataMap.put("cpu0", 2.0);
        dataMap.put("cpu1", 2.0);
        perfData.addPerfData(dataMap);
        dataMap = perfData.getAvgValuesData();
        assertEquals(new Float(1.5), dataMap.get("cpu0"));
        assertEquals(null, dataMap.get("cpu1"));
        assertEquals(1L, dataMap.get("datetime-in-minutes"));
    }

}
