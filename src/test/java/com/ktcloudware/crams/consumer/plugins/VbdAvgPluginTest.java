package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.VbdReadWriteAvgPlugin;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class VbdAvgPluginTest {

	@Test
	public void test(){
		Map<String, Object> testData = FileUtil.readJsonToMap("singleRrdData1.json");
		VbdReadWriteAvgPlugin plugin = new VbdReadWriteAvgPlugin();
		Map<String, Object> resultMessage = plugin.excute(testData, null);
		System.out.println(resultMessage);
		assertEquals(879123556L, resultMessage.get("vbd_write_avg"));
	}


	@Test
	public void testForZeroValue(){
		Map<String, Object> testData = FileUtil.readJsonToMap("singleRrdData2.json");
		VbdReadWriteAvgPlugin plugin = new VbdReadWriteAvgPlugin();
		Map<String, Object> resultMessage = plugin.excute(testData, null);
		System.out.println(resultMessage);
		assertEquals(0, resultMessage.get("vbd_read_avg"));
	}

}
