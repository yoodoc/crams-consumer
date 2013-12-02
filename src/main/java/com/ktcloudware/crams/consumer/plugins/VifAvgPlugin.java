package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.util.KafkaConsumerPluginUtil;

public class VifAvgPlugin implements CramsIndexerPlugin {
	Logger logger = LogManager.getLogger("PLUGINS");
	
	public VifAvgPlugin() {
		logger = LogManager.getLogger("PLUGINS");
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag) {
		Map<String, Object> resultMap = KafkaConsumerPluginUtil
				.addAverageValue("vif_[0-9]+_rx", "vif_rx_avg", dataMap);
		if (resultMap == null) {
			logger.warn("can't get average vif values from data=" + dataMap);
			return dataMap;
		}
		resultMap = KafkaConsumerPluginUtil.addAverageValue("vif_[0-9]+_tx",
				"vif_tx_avg", resultMap);
		return resultMap;
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
	public boolean needProperties() {
		// TODO Auto-generated method stub
		return false;
	}
}
