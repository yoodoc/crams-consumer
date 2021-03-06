package com.ktcloudware.crams.consumer.plugins;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.common.mvel2.ast.AssertNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

//@Ignore
public class UcloudWatchPluginTest {

    UcloudWatchPlugin uwp;

    @Before
    public void before() {

        uwp = new UcloudWatchPlugin();
        try {
            uwp.setProperties("http://14.63.214.195:9010/watch");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        long starttime = System.currentTimeMillis();

        // make default input data
        int testDataSize = 1;
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < testDataSize; i++) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("memory_target", 100000000000L);
            data.put("memory_target", 100000000000L);
            data.put("memory_internal_free", 100000000);
            data.put("cpu0", 0.5);
            data.put("cpu1", 0.5);
            data.put("cpu2", 0.5);
            data.put("cpu3", 0);
            data.put("cpu4", 1);
            data.put("vm_uuid", "uu_id");
            data.put("vm_display_name", null);
            data.put("vm_account_name", "EPC_M1111112_S1111");
            data.put("vm_template_name", "template");
            data.put("vm_service_offering_name", "serviceoffering");
            
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String dayAndMonth = String.valueOf(year) + "-"
                    + String.valueOf(month + 1) + "-" + String.valueOf(day - 5);
            int hour = i % 60;
            int minute = i / 60;
            data.put("datetime",
                    dayAndMonth + " " + hour + ":" + String.valueOf(minute)
                            + ":40");
            data.put("vif_0_rx", 465025100);
            data.put("vif_0_tx", 203703075);
            data.put("vbd_hda_read", 47568770);
            data.put("vbd_hda_write", 879123556);
            dataList.add(data);
        }

        for (Map<String, Object> data : dataList) {
            try {
                uwp.excute(data, "unittest");
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        }

        long endtime = System.currentTimeMillis();
        System.out.println("total " + (endtime - starttime) + " msec for "
                + dataList.size() + " data");
    }

    @Test
    public void testNullvmUuid() {
        // make default input data
        int testDataSize = 1;
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < testDataSize; i++) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("memory_target", 100000000000L);
            data.put("memory_target", 100000000000L);
            data.put("memory_internal_free", 100000000);
            data.put("cpu0", 0.38738647169767515);
            data.put("cpu1", 0.635808734557892);
            data.put("cpu2", 0.9888381871688897);
            data.put("cpu3", 0.08545827027426656);
            data.put("cpu4", 0.7659032390437487);
            data.put("vm_uuid", null);
            data.put("vm_name", "test_vm");
            data.put("vm_account_name", "EPC_M1111112_S1111");
            data.put("vm_template_name", "template");
            data.put("vm_service_offering_name", "serviceoffering");
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String dayAndMonth = String.valueOf(year) + "-"
                    + String.valueOf(month + 1) + "-" + String.valueOf(day - 5);
            int hour = i % 60;
            int minute = i / 60;
            data.put("datetime",
                    dayAndMonth + " " + hour + ":" + String.valueOf(minute)
                            + ":40");
            data.put("vif_0_rx", 465025100);
            data.put("vif_0_tx", 203703075);
            data.put("vbd_hda_read", 47568770);
            data.put("vbd_hda_write", 879123556);
            dataList.add(data);
        }

        for (Map<String, Object> data : dataList) {
            try {
                data = uwp.excute(data, "unittest");
                Assert.assertNull(data);
//                Assert.fail("expect exception");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testNullMemory() {
        // make default input data
        int testDataSize = 1;
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < testDataSize; i++) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("memory_target", null);
            data.put("memory_internal_free", 100000000);
            data.put("cpu0", 0.38738647169767515);
            data.put("cpu1", 0.635808734557892);
            data.put("cpu2", 0.9888381871688897);
            data.put("cpu3", 0.08545827027426656);
            data.put("cpu4", 0.7659032390437487);
            data.put("vm_uuid", "testid");
            data.put("vm_account_name", "EPC_M1111112_S1111");
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String dayAndMonth = String.valueOf(year) + "-"
                    + String.valueOf(month + 1) + "-" + String.valueOf(day - 5);
            int hour = i % 60;
            int minute = i / 60;
            data.put("datetime",
                    dayAndMonth + " " + hour + ":" + String.valueOf(minute)
                            + ":40");
            data.put("vif_0_rx", 465025100);
            data.put("vif_0_tx", 203703075);
            data.put("vbd_hda_read", 47568770);
            data.put("vbd_hda_write", 879123556);
            dataList.add(data);
        }

        for (Map<String, Object> data : dataList) {
            try {
                uwp.excute(data, "unittest");
                System.out.println("!! hi");
                Assert.fail("expect exception");
                ;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testWrongDateFormat() {
        // make default input data
        int testDataSize = 1;
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < testDataSize; i++) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("memory_target", 100000000000L);
            data.put("memory_internal_free", 100000000);
            data.put("cpu0", 0.38738647169767515);
            data.put("cpu1", 0.635808734557892);
            data.put("cpu2", 0.9888381871688897);
            data.put("cpu3", 0.08545827027426656);
            data.put("cpu4", 0.7659032390437487);
            data.put("vm_uuid", "testid");
            data.put("vm_account_name", "EPC_M1111112_S1111");
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String dayAndMonth = String.valueOf(year) + "-"
                    + String.valueOf(month + 1) + "-" + String.valueOf(day - 5);
            int hour = i % 60;
            int minute = i / 60;
            data.put("datetime",
                    dayAndMonth + "T" + hour + ":" + String.valueOf(minute)
                            + ":40");
            data.put("vif_0_rx", 465025100);
            data.put("vif_0_tx", 203703075);
            data.put("vbd_hda_read", 47568770);
            data.put("vbd_hda_write", 879123556);
            dataList.add(data);
        }

        for (Map<String, Object> data : dataList) {
            try {
                data = uwp.excute(data, "unittest");
                Assert.assertNull(data);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @Test
    public void testRdbaasInstanceData() {
    
    }
    
    @Test
    public void testVrData() {
    
    }
    
    @Test
    public void testAutoscalingInstanceData() {
        long starttime = System.currentTimeMillis();

        // make default input data
        int testDataSize = 1;
        List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < testDataSize; i++) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("memory_target", 100000000000L);
            data.put("memory_target", 100000000000L);
            data.put("memory_internal_free", 100000000);
            data.put("cpu0", 0.5);
            data.put("cpu1", 0.5);
            data.put("cpu2", 0.5);
            data.put("cpu3", 0);
            data.put("cpu4", 1);
            data.put("vm_uuid", "uu_id");
            data.put("vm_display_name", "uas-as-test");
            data.put("vm_account_name", "EPC_M1111112_S1111");
            data.put("vm_template_name", "template");
            data.put("vm_service_offering_name", "serviceoffering");
            
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            String dayAndMonth = String.valueOf(year) + "-"
                    + String.valueOf(month + 1) + "-" + String.valueOf(day - 5);
            int hour = i % 60;
            int minute = i / 60;
            data.put("datetime",
                    dayAndMonth + " " + hour + ":" + String.valueOf(minute)
                            + ":40");
            data.put("vif_0_rx", 465025100);
            data.put("vif_0_tx", 203703075);
            data.put("vbd_hda_read", 47568770);
            data.put("vbd_hda_write", 879123556);
            dataList.add(data);
        }

        for (Map<String, Object> data : dataList) {
            try {
                uwp.excute(data, "unittest");
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail();
            }
        }

        long endtime = System.currentTimeMillis();
        System.out.println("total " + (endtime - starttime) + " msec for "
                + dataList.size() + " data");
    }
    
    @Test
    public void testTemp() {
        String testStr = "uas-AS-as-yd2-11wed12d";
        System.out.println(testStr.substring(testStr.indexOf('-')+1, testStr.lastIndexOf('-')));
    }
    
}
