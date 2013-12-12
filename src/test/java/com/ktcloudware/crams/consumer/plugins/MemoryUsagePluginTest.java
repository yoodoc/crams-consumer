package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.MemoryUsagePlugin;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class MemoryUsagePluginTest {
	@Test
	public void test() {
		Map<String, Object> testData = FileUtil
				.readJsonToMap("singleRrdData1.json");
		MemoryUsagePlugin plugin = new MemoryUsagePlugin();
		Map<String, Object> resultMessage = null;
		try {
			resultMessage = plugin.excute(testData, null);
		} catch (CramsPluginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		System.out.println(resultMessage);
		assertEquals(0.9990233054662256, resultMessage.get("memory_usage"));
	}
}