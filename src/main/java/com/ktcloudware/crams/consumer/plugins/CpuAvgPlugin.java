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
		try {
			dataMap = KafkaConsumerPluginUtil.addAverageValue("cpu[0-9]+", CPU_AVG, dataMap);
		} catch (CramsPluginException e) {
			dataMap = putZeroFloatResult(dataMap);
		}
		return dataMap;
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
	
	public Map<String,Object> putZeroFloatResult(Map<String, Object> dataMap) {
		dataMap.put(CPU_AVG, (float) 0.0);
		return dataMap;
	}
}
