package com.ktcloudware.cdp.cramsindexer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.indexer.plugins.MemoryUsagePlugin;
import com.ktcloudware.crams.indexer.util.FileUtil;

public class MemoryUsagePluginTest {
	@Test
	public void test() {
		Map<String, Object> testData = FileUtil.readJsonToMap("singleRrdData1.json");
		MemoryUsagePlugin plugin = new MemoryUsagePlugin();
		Map<String, Object> resultMessage = plugin.excute(testData);
		System.out.println(resultMessage);
		assertEquals(0.9999990461967444, resultMessage.get("memory_usage"));
	}
}
