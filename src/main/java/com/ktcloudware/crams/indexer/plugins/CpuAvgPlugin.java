package com.ktcloudware.crams.indexer.plugins;

import java.util.Map;

import com.ktcloudware.crams.indexer.util.KafkaConsumerPluginUtil;

public class CpuAvgPlugin implements CramsIndexerPlugin{
	
	KafkaConsumerPluginUtil plugin;
	public CpuAvgPlugin() {
	}
	
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap) {
		// TODO Auto-generated method stub
		return KafkaConsumerPluginUtil.addAverageValue("cpu[0-9]+", "cpu_avg", dataMap);
	}

	@Override
	public void setProperties(String pluginProperties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean needProperties() {
		return false;
	}

	@Override
	public String getProperties() {
		// TODO Auto-generated method stub
		return null;
	}
}
