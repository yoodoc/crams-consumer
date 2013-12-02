package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UcloudWatchPlugin implements CramsIndexerPlugin {
	private Logger logger;

	public UcloudWatchPlugin() {
		logger = LogManager.getLogger("PLUGINS");
	}

	@Override
	public void setProperties(String pluginProperties) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean needProperties() {
		// TODO Auto-generated method stub
		return false;
	}
}
