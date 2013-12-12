package com.ktcloudware.crams.consumer.plugins;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.UcloudDBSelector;

public class MockDBSelectorForUcloudWatchPlugin extends UcloudDBSelector {
	public MockDBSelectorForUcloudWatchPlugin()
			throws CramsException {
	}
	
	public MockDBSelectorForUcloudWatchPlugin(String dbTarget)
			throws CramsException {
	}

	public String getVmNameByVmId(String vmUuid){
		return "unit-test-vm";
	}
}
