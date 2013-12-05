package com.ktcloudware.crams.consumer.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceDiskFieldNamePlugin implements CramsConsumerPlugin{
	private Pattern hdxPattern;
	private Pattern xvdxPattern;
	
	public ReplaceDiskFieldNamePlugin(){
		hdxPattern = Pattern.compile("vbd_hd([a-z]+_[a-z]+)");
		xvdxPattern = Pattern.compile("vbd_xvd([a-z]+_[a-z]+)");
	}
	
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag){
		Map<String, Object> newData = new HashMap<String, Object>();
		for(String keyName: dataMap.keySet()){
			Matcher hdxMatcher = hdxPattern.matcher(keyName);
			Matcher xvdxMatcher = xvdxPattern.matcher(keyName);
			
			if(hdxMatcher.find()){
				Object value = dataMap.get(keyName);
				String newKeyName = "vbd_" + hdxMatcher.group(1);
				newData.put(newKeyName, value);
			} else if(xvdxMatcher.find()){
				Object value = dataMap.get(keyName);
				String newKeyName = "vbd_" + xvdxMatcher.group(1);
				newData.put(newKeyName, value);
			} else {
				Object value = dataMap.get(keyName);
				newData.put(keyName, value);
			}
		}
		// TODO Auto-generated method stub
		return newData;
	}

	@Override
	public void setProperties(String pluginProperties){
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProperties(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean needProperties(){
		// TODO Auto-generated method stub
		return false;
	}

}
