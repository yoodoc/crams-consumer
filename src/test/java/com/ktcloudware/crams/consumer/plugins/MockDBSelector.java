package com.ktcloudware.crams.consumer.plugins;

import com.ktcloudware.crams.consumer.clients.UcloudDBSelector;

public class MockDBSelector extends UcloudDBSelector {
	public String getVmNameByVmId(String vmUuid){
		return "unit-test-vm";
	}
}
