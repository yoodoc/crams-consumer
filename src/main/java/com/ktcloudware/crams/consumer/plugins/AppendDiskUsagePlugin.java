package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.clients.CacheClient;
import com.ktcloudware.crams.consumer.clients.RedisClient;

public class AppendDiskUsagePlugin implements CramsConsumerPlugin {
	private CacheClient cacheClient;
	private String properties;
	private static final String DISK_AVG_USAGE = "vbd_usage";

	Logger logger = LogManager.getLogger("PLUGINS");

	public AppendDiskUsagePlugin(){
		initCacheClient("localhost");
	}

	@Override
	public void setProperties(String pluginProperties){
		this.properties = pluginProperties;
		initCacheClient(pluginProperties);

	}

	private void initCacheClient(String cacheAddress){
		this.cacheClient = new RedisClient(cacheAddress);
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag) {
		long totalDisk = 0;
		long totalUsingDisk = 0;
		String vmUuid = null;

		// get diskMapping
		try{
			vmUuid = String.valueOf(dataMap.get("vm_uuid"));
			if(vmUuid == null || vmUuid.isEmpty()){
				logger.trace("can't find vm_uuid field");
				dataMap = createEmptyDiskUsageData(dataMap);
				throw new CramsPluginException("can't find vm_uuid field");
			}

			String vdi = cacheClient.get(vmUuid);
			if(vdi == null || vdi.isEmpty()){
				logger.trace("can't find vdiset for vmUUID= " + vmUuid);
				dataMap = createEmptyDiskUsageData(dataMap);
				throw new CramsPluginException("can't find vdiset for vmUUID= " + vmUuid);
			}

			String[] vdiSet = vdi.split(",");

			for(String vdiName : vdiSet){
				String[] vdiNames = cacheClient.get(vdiName).split(",");

				if(vdiNames.length != 3){
					throw new Exception(
							"wrong formmated vdi usage values in cache object. :"
									+ vdi);
				}
				totalDisk += Long.valueOf(vdiNames[1]);
				totalUsingDisk += Long.valueOf(vdiNames[2]);

				// append device info to dataMap
				String newDeviceName = "vbd_"
						+ vdiNames[0].charAt(vdiNames[0].length() - 1);
				dataMap.put(newDeviceName + "_size", Long.valueOf(vdiNames[1]));
				dataMap.put(newDeviceName + "_using_size",
						Long.valueOf(vdiNames[2]));
			}
		}catch(Exception e1){
			logger.warn("failed to get disk usage information for" + vmUuid
					+ ":" + e1.getMessage());
			
			//set default values to dataMap
			dataMap.put(DISK_AVG_USAGE, 0.0);
			return dataMap;
		}

		// calculate disk usage rate
		float diskUsageRate = 0;
		if(totalDisk != 0){
			diskUsageRate = (float) ((double) totalUsingDisk / totalDisk);
		}

		dataMap.put(DISK_AVG_USAGE, diskUsageRate);
		return dataMap;
	}

	@Override
	public String getProperties(){
		return properties;
	}

	@Override
	public boolean needProperties(){
		// TODO Auto-generated method stub
		return true;
	}

	private Map<String, Object> createEmptyDiskUsageData(
			Map<String, Object> data){
		data.put("vbd_a_size", 0);
		data.put("vbd_a_using_size",
				0);
		data.put(DISK_AVG_USAGE, 0);
		return data;
	}
}
