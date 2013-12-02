package com.ktcloudware.crams.indexer.plugins;

import java.util.Map;

import com.ktcloudware.crams.indexer.util.KafkaConsumerPluginUtil;

public class VbdReadWriteAvgPlugin implements CramsIndexerPlugin {
	private ReplaceDiskFieldNamePlugin replaceName;

	public VbdReadWriteAvgPlugin() {
		replaceName = new ReplaceDiskFieldNamePlugin();
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap) {
		dataMap = replaceName.excute(dataMap);
		return KafkaConsumerPluginUtil.addAverageValue("vbd_[a-z]+_read",
				"vbd_read_avg", (KafkaConsumerPluginUtil.addAverageValue(
						"vbd_[a-z]+_write", "vbd_write_avg", dataMap)));
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
