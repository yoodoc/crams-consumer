package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import com.ktcloudware.crams.consumer.util.KafkaConsumerPluginUtil;

public class CpuAvgPlugin implements CramsConsumerPlugin{
	public static final String CPU_AVG = "cpu_avg";
	
	public CpuAvgPlugin(){
	}
	
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag){
		// TODO Auto-generated method stub
		return KafkaConsumerPluginUtil.addAverageValue("cpu[0-9]+", CPU_AVG, dataMap);
	}

	@Override
	public void setProperties(String pluginProperties){
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean needProperties(){
		return false;
	}

	@Override
	public String getProperties(){
		// TODO Auto-generated method stub
		return null;
	}
}
