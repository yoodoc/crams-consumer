package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceVmAccountNamePlugin implements CramsIndexerPlugin{
	private Pattern vmAccountNamePattern;
	private static final String VM_ACCOUNT_NAME = "vm_account_name";
	private static final String VM_ACCOUNT = "vm_account";
	
	public ReplaceVmAccountNamePlugin() {
		vmAccountNamePattern = Pattern.compile("epc_(m[0-9]+)_.*", Pattern.CASE_INSENSITIVE);
	}
	
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag) {
		//Map<String, Object> newData = new HashMap<String, Object>();
		//newData.putAll(dataMap);
		
		Object originName = dataMap.get(VM_ACCOUNT_NAME);
		
		if (originName == null || !(originName instanceof String)) {
			return null;
		}
		Matcher vmAccountNameMatcher = vmAccountNamePattern.matcher((String)originName);
		if (vmAccountNameMatcher.find()) {
			String vmAccount = vmAccountNameMatcher.group(1);
			dataMap.put(VM_ACCOUNT_NAME, vmAccount);
			dataMap.put(VM_ACCOUNT, originName);
		} else {
			dataMap.put(VM_ACCOUNT, originName);
		}
		
		// TODO Auto-generated method stub
		return dataMap;
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
