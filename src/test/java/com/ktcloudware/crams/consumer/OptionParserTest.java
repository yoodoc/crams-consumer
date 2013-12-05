package com.ktcloudware.crams.consumer;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class OptionParserTest {

	@Test
	public void test(){
		List<CramsConsumerPlugin> plugins = null;
		try {
			plugins = IndexerOptionParser.loadKafkaPlugins("vm_perf_rrd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(CramsConsumerPlugin plugin: plugins){
			System.out.println(plugin.getClass().toString());
			String pluginProperties = plugin.getProperties();
			if(pluginProperties == null || pluginProperties.isEmpty()){
				System.out.println("no properties");
			} else {
				String[] properties = pluginProperties.split(","); 
				for(String property: properties){
					System.out.println("|" + property + "|");
				}
			}
		}
		assertEquals(10, plugins.size());
	}

}
