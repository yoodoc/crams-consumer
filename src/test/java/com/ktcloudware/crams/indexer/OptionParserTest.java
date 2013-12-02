package com.ktcloudware.crams.indexer;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.ktcloudware.crams.indexer.plugins.CramsIndexerPlugin;
import com.ktcloudware.crams.indexer.util.IndexerOptionParser;

public class OptionParserTest {

	@Test
	public void test() {
		List<CramsIndexerPlugin> plugins = IndexerOptionParser.loadKafkaPlugins("vm_perf_rrd");
		for (CramsIndexerPlugin plugin: plugins) {
			System.out.println(plugin.getClass().toString());
			String pluginProperties = plugin.getProperties();
			if(pluginProperties == null || pluginProperties.isEmpty()) {
				System.out.println("no properties");
			} else {
				String[] properties = pluginProperties.split(","); 
				for (String property: properties) {
					System.out.println("|" + property + "|");
				}
			}
		}
		assertEquals(8, plugins.size());
	}

}
