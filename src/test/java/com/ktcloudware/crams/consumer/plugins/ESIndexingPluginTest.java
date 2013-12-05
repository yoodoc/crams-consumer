package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.ParseException;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Test;

import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.dataType.ESConfig;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class ESIndexingPluginTest {

	@Test
	public void test(){
		int numOfThread = 10;
		String setting = FileUtil.readJsonFromConfigPath("indexSettings.json");
		String mapping = FileUtil.readJsonFromConfigPath("mappingInfo.json");
		ESConfig esConfig = new ESConfig();
		try{
			esConfig.setESAddress("localhost:9300");
		}catch(ParseException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<ESIndexingPlugin> plugins = new ArrayList<ESIndexingPlugin>();
		
		for(ESIndexingPlugin esPlugin: plugins){
			ESBulkIndexer esBulkIndexer = new ESBulkIndexer(esConfig.esAddressList,
					"elasticsearch", "yoodoc", "vm_pod_name",
					setting, mapping);
			esPlugin = new ESIndexingPlugin();
		}
		ExecutorService executor = Executors
				.newFixedThreadPool(numOfThread);
		
		MockRunableClassForKafkaConsumerPlugins worker = new MockRunableClassForKafkaConsumerPlugins(plugins.get(0));
		executor.execute(worker);
	}

}
