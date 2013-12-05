package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import scala.Array;

public class UcloudWatchPluginTest {

	@Test
	public void test() {
		UcloudWatchPlugin uwp = new UcloudWatchPlugin();
		try {
			uwp.setProperties("unittest");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//make test data
		List<Map<String, Object>> dataList = new ArrayList<Map<String, Object>>();
		
		int testDataSize = 50000;
		long starttime = System.currentTimeMillis();
		for(int i = 0; i < testDataSize; i++){
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("memory_target", 100000000000L);
			data.put("memory_internal_free", 100000000);
			data.put("cpu0", 0.38738647169767515);
			data.put("cpu1", 0.635808734557892);
			data.put("cpu2", 0.9888381871688897);
			data.put("cpu3", 0.08545827027426656);
			data.put("cpu4", 0.7659032390437487);
			data.put("vm_uuit", "unit_test");
			data.put("vm_account_name", "EPC_M1111112_S1111");
			int hour = i%60;
			int minute = i/60;
			data.put("datetime", "2013-12-01 " + hour + ":" + String.valueOf(minute) + ":40");
			data.put("vif_0_rx", 465025100);
			data.put("vif_0_tx", 203703075);
			data.put("vbd_hda_read", 47568770);
			data.put("vbd_hda_write", 879123556);
			
			dataList.add(data);
		}
		
		for(Map<String,Object> data: dataList){
			try {
				uwp.excute(data, "unittest");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		long endtime = System.currentTimeMillis();
		System.out.println("total " + (endtime - starttime) + "time for " + testDataSize + " data");
	}

}
