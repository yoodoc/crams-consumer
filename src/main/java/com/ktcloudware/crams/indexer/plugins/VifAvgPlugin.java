package com.ktcloudware.crams.indexer.plugins;

import java.util.Map;

import com.ktcloudware.crams.indexer.util.KafkaConsumerPluginUtil;

public class VifAvgPlugin implements CramsIndexerPlugin {
	public VifAvgPlugin() {
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap) {
		Map<String, Object> resultMap = KafkaConsumerPluginUtil
				.addAverageValue("vif_[0-9]+_rx", "vif_rx_avg", dataMap);
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
