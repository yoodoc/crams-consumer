package com.ktcloudware.cdp.cramsindexer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.indexer.plugins.FiveMinutesFilterPlugin;
import com.ktcloudware.crams.indexer.plugins.CramsIndexerPlugin;

public class FiveMinutesFilterPluginTest {

	@Test
	public void test() {
		CramsIndexerPlugin plugin = new FiveMinutesFilterPlugin();
		plugin.setProperties("datetime");
		Map<String, Object> testData = new HashMap<String, Object>();
		testData.put("datetime", "2013-10-04 14:38:40");
		assertNull(plugin.excute(testData)); 
		testData.put("datetime", "2013-10-04 14:40:40");
		assertNotNull(plugin.excute(testData)); 
	}

}
