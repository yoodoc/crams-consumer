package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.util.KafkaConsumerPluginUtil;

public class VbdReadWriteAvgPlugin implements CramsIndexerPlugin {
	private ReplaceDiskFieldNamePlugin replaceName;
	private Logger logger;

	public VbdReadWriteAvgPlugin() {
		logger = LogManager.getLogger("PLUGINS");
		replaceName = new ReplaceDiskFieldNamePlugin();
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag) {
		dataMap = replaceName.excute(dataMap, dataTag);
		Map<String, Object> resultMap = KafkaConsumerPluginUtil.addAverageValue("vbd_[a-z]+_read",
				"vbd_read_avg", (KafkaConsumerPluginUtil.addAverageValue(
						"vbd_[a-z]+_write", "vbd_write_avg", dataMap)));
		if (resultMap == null) {
			logger.warn("can't get average vbd r/w values from data=" + dataMap);
			return dataMap;
		}
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
