package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.AppendDiskUsagePlugin;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class AppendDiskUsagePluginTest {
	@Test
	public void testAppendDiskUsagePlugin() {
		//create AppendDiskUsagePlugin for unittest
		AppendDiskUsagePlugin diskUsagePlugin = new AppendDiskUsagePlugin() {
			void initCacheClient(String cacheAddress){
				this.cacheClient = new MockCacheClient(cacheAddress) {
					@Override
					public String get(String key) throws Exception {
						if (key.equalsIgnoreCase("unittest_name")) {
							return "device_a,1000,500";
						}
						
						if (key.equalsIgnoreCase("unittest_uuid")) {
							return "unittest_name";
						}
						return null;
					}
				};
			}
		};
		diskUsagePlugin.setProperties("localhost");
		
		Map<String, Object> rrdData = FileUtil
				.readJsonToMap("singleRrdData1.json");
		rrdData.put("vm_uuid", "unittest_uuid");
	
		try {
			rrdData = diskUsagePlugin.excute(rrdData, null);
		} catch (CramsPluginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}

		System.out.println(rrdData);
		assertEquals((float) 0.5, rrdData.get("vbd_usage"));
		assertEquals(1000L, rrdData.get("vbd_a_size"));
	}
	
	@Test
	public void testCanNotFindVMInfoFromCache() {
		//create AppendDiskUsagePlugin for unittest
		AppendDiskUsagePlugin diskUsagePlugin = new AppendDiskUsagePlugin() {
			void initCacheClient(String cacheAddress){
				this.cacheClient = new MockCacheClient(cacheAddress) {
					@Override
					public String get(String key) throws Exception {
						if (key.equalsIgnoreCase("unittest_name")) {
							return "device_a,1000,500";
						}
						
						if (key.equalsIgnoreCase("unittest_uuid")) {
							return "unittest_name";
						}
						return null;
					}
				};
			}
		};
		diskUsagePlugin.setProperties("localhost");
		
		Map<String, Object> rrdData = FileUtil
				.readJsonToMap("singleRrdData1.json");
		rrdData.put("vm_uuid", "");
	
		try {
			rrdData = diskUsagePlugin.excute(rrdData, null);
		} catch (CramsPluginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}

		System.out.println(rrdData);
		assertEquals((float) 0.0, rrdData.get("vbd_usage"));
		assertEquals(0L, rrdData.get("vbd_a_size"));
	}
}
