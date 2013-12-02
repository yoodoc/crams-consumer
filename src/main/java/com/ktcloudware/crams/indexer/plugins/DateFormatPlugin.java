package com.ktcloudware.crams.indexer.plugins;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateFormatPlugin implements CramsIndexerPlugin {
	String dateFieldName;
	private SimpleDateFormat originDateFormat;
	private SimpleDateFormat newDateFormat;
	private String properties;
	
	public DateFormatPlugin() {
		this.dateFieldName = "datetime";
		this.originDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.newDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
	}
	
/*	public DateFormatPlugin(String dateFieldName, String originDateFormatStr, String newDateFormatStr) {
		this.dateFieldName = "datetime";
		this.originDateFormat = new SimpleDateFormat(originDateFormatStr);
		this.newDateFormat = new SimpleDateFormat(newDateFormatStr);
	}*/
	
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap) {
		Map<String, Object> newDataMap = new HashMap<String, Object>();
		newDataMap.putAll(dataMap);
		try {
			String datetime = (String) newDataMap.get(this.dateFieldName);
			Date date = originDateFormat.parse(datetime);
			String newDateTime = newDateFormat.format(date);
			newDataMap.put(dateFieldName, newDateTime);
			return newDataMap;
		} catch (Exception e) {
				e.printStackTrace();
			}
		return null;
	}

@Override
/**
 * pluginProperties : "dateFieldName,originDateFormatStr,newDateFormatStr"
 * 
 */
public void setProperties(String pluginProperties) {
	this.properties = pluginProperties;
	String[] properties = pluginProperties.split(",");
	this.dateFieldName = properties[0];
	this.originDateFormat = new SimpleDateFormat(properties[1]);
	this.newDateFormat = new SimpleDateFormat(properties[2]);
	
}

@Override
public boolean needProperties() {
	return true;
}

@Override
public String getProperties() {
	return properties;
}

}
