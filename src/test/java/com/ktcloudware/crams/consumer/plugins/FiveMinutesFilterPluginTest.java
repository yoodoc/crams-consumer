package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.FiveMinutesFilterPlugin;
import com.ktcloudware.crams.consumer.plugins.CramsIndexerPlugin;

public class FiveMinutesFilterPluginTest {

	@Test
	public void test() {
		CramsIndexerPlugin plugin = new FiveMinutesFilterPlugin();
		plugin.setProperties("datetime,3");
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("datetime", "2013-10-04 14:40:40");
		assertNull(plugin.excute(testData, null)); 
		testData.put("datetime", "2013-10-04 14:38:40");
		assertNotNull(plugin.excute(testData, null)); 
	}

}
