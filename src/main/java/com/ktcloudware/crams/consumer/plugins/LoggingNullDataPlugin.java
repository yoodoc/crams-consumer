package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class LoggingNullDataPlugin implements CramsIndexerPlugin {
	private String properties;
	Logger logger;
	
	public LoggingNullDataPlugin() {
		properties = "vm_uuid";
		logger = LogManager.getLogger("PLUGINS.NULLPLUGIN");
	}
	
	@Override
	public void setProperties(String pluginProperties) {
		properties = pluginProperties;
	}

	@Override
	public String getProperties() {
		return properties;
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag) {
		String[] keyNamesForCheckNullValue = properties.split(",");
		for (String keyName : keyNamesForCheckNullValue) {
			Object value = dataMap.get(keyName);
			if (value == null) {
				logger.info("NULL_VMUUID:recieved data has null " + keyName + " value:" + dataMap);
				return null;
			}
		}
	
		return dataMap;
	}

	@Override
	public boolean needProperties() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
