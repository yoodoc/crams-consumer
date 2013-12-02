package com.ktcloudware.crams.indexer.plugins;

import java.util.Map;

/**
 * accept only data that has *0 or *5 minutes value in datetime field.
 * format that include "hh:mm:ss" is allowed.
 * @author yoodoc
 *
 */
public class FiveMinutesFilterPlugin implements CramsIndexerPlugin{
	private String dateFieldName = "datetime";
	
	public FiveMinutesFilterPlugin() {
		
	}
	
	@Override
	public void setProperties(String dateFieldName) {
		this.dateFieldName = dateFieldName;	
	}
	
	
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap) {
		if (dataMap == null || dataMap.isEmpty()) {
			return dataMap;
		}
		try { 
			String dateStr = (String) dataMap.get(dateFieldName);
			String minutesStr = dateStr.split(":")[1];
			if (minutesStr.endsWith("0") || minutesStr.endsWith("5")) {
				return dataMap;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	@Override
	public String getProperties() {
		return dateFieldName;
	}

	@Override
	public boolean needProperties() {
		return true;
	}
}
