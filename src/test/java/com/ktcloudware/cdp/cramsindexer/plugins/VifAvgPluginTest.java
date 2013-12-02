package com.ktcloudware.cdp.cramsindexer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.indexer.plugins.VifAvgPlugin;
import com.ktcloudware.crams.indexer.util.FileUtil;

public class VifAvgPluginTest {

	@Test
	public void test() {
		Map<String, Object> testData = FileUtil.readJsonToMap("singleRrdData1.json");
		VifAvgPlugin plugin = new VifAvgPlugin();
		Map<String, Object> resultMessage = plugin.excute(testData);
		System.out.println(resultMessage);
		assertEquals(203703075L, resultMessage.get("vif_tx_avg"));
		assertEquals(465025100L, resultMessage.get("vif_rx_avg"));
	}

}
