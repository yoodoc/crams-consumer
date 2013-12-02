package com.ktcloudware.crams.indexer.plugins;

import java.util.Map;

public interface CramsIndexerPlugin {
	/**
	 * pluginProperties : comma seperatied properties for each kafkaConsumerPlugin implements
	 * @param pluginProperties
	 */
	public void setProperties(String pluginProperties);

	public String getProperties();
	/**
	 * excute plugin 
	 * @param dataMap
	 * @return
	 */
	public Map<String, Object> excute(Map<String, Object> dataMap);
	
	
	public boolean needProperties();
}
