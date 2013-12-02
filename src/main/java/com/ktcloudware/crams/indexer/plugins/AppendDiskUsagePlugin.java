package com.ktcloudware.crams.indexer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.indexer.clients.CacheClient;
import com.ktcloudware.crams.indexer.clients.RedisClient;

public class AppendDiskUsagePlugin implements CramsIndexerPlugin{
	private CacheClient cacheClient;
	private String properties;
	private static final String DISK_AVG_USAGE = "vbd_usage";
	
	static Logger logger = LogManager.getLogger("CRAMS_INDEXER_PLUGIN");
	
	public AppendDiskUsagePlugin() {
		initCacheClient("localhost");
	}
	
	@Override
	public void setProperties(String pluginProperties) {
		this.properties = pluginProperties;
		initCacheClient(pluginProperties);
		
	}
	
	private void initCacheClient(String cacheAddress) {
		this.cacheClient = new RedisClient(cacheAddress);
	}
	
	
	
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap) {
		long totalDisk = 0;
		long totalUsingDisk = 0;
		String vmUuid = null;
		//get diskMapping
		try {
			vmUuid = String.valueOf(dataMap.get("vm_uuid"));
			String[] vdiSet = cacheClient.get(vmUuid).split(",");
			
			for (String vdiName: vdiSet) {
				String[] vdiNames= cacheClient.get(vdiName).split(",");
				
				if (vdiNames.length != 3) {
					throw new Exception("wrong formmated vdi usage values in cache object.");
				}
				totalDisk += Long.valueOf(vdiNames[1]);
				totalUsingDisk += Long.valueOf(vdiNames[2]);
				
				//append device info to dataMap
				String newDeviceName = "vbd_" + vdiNames[0].charAt(vdiNames[0].length()-1);
				dataMap.put(newDeviceName + "_size", Long.valueOf(vdiNames[1]));
				dataMap.put(newDeviceName + "_using_size", Long.valueOf(vdiNames[2]));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			logger.error("failed to get disk usage information for " + vmUuid + " : " + e1.getMessage());
			return dataMap;
		}
		
		float diskUsageRate = 0;
		//calculated disk usage rate 
		if (totalDisk != 0) {
			diskUsageRate = (float) ((double)totalUsingDisk / totalDisk);
		} 
		
		dataMap.put(DISK_AVG_USAGE,  diskUsageRate);
		return dataMap;
	}

	@Override
	public String getProperties() {
		return properties;
	}

	@Override
	public boolean needProperties() {
		// TODO Auto-generated method stub
		return true;
	}
}
