package com.ktcloudware.cdp.cramsindexer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.indexer.plugins.CpuAvgPlugin;
import com.ktcloudware.crams.indexer.util.FileUtil;

public class CpuAvgPluginTest {

	@Test
	public void test() {
		Map<String, Object> testData = FileUtil.readJsonToMap("singleRrdData1.json");
		CpuAvgPlugin plugin = new CpuAvgPlugin();
		assertEquals((float)0.48285455, plugin.excute(testData).get("cpu_avg"));
	}
}
