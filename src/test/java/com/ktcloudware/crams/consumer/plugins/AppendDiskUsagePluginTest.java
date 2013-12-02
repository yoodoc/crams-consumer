package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.AppendDiskUsagePlugin;
import com.ktcloudware.crams.consumer.plugins.DiskUsageCachePlugin;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class AppendDiskUsagePluginTest {
	//@Ignore
	@Test
	public void testDiskAvgUsagePlugin() {
		DiskUsageCachePlugin cachePlugin = new DiskUsageCachePlugin();
		cachePlugin.setProperties("localhost,60");
		AppendDiskUsagePlugin avgUsagePlugin = new AppendDiskUsagePlugin();
		avgUsagePlugin.setProperties("localhost");
		
		//Map diskData = JsonFileToMap.read("singleDiskData.json");
		//Map cacheItemKey = cachePlugin.excute(diskData);
		Map<String, Object> diskData = FileUtil.readJsonToMap("diskData1.json");
		diskData.put("vm_uuid", "unittest_uuid");
		Map<String, Object> rrdData = FileUtil.readJsonToMap("singleRrdData1.json");
		rrdData.put("vm_uuid", "unittest_uuid");
		cachePlugin.excute(diskData, null);
		rrdData = avgUsagePlugin.excute(rrdData, null);
		
		System.out.println(rrdData);
		assertEquals((float)0.109375, rrdData.get("vbd_usage"));	
		assertEquals(2147483648L, rrdData.get("vbd_a_size"));
		//cacheClient.delete(cacheItemKey);
	}
}
