package com.ktcloudware.cdp.cramsindexer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.indexer.plugins.DateFormatPlugin;

public class DateFormatPluginTest {

	@Test
	public void test() {
		DateFormatPlugin plugin = new DateFormatPlugin();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("datetime", "2013-01-01 11:11:11");
		map = plugin.excute(map);
		
		assertEquals("2013-01-01T11:11:11+0900", map.get("datetime"));	
	}

}
