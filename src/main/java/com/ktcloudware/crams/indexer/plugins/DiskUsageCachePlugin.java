package com.ktcloudware.crams.indexer.plugins;

import java.util.Map;

import com.ktcloudware.crams.indexer.clients.CacheClient;
import com.ktcloudware.crams.indexer.clients.RedisClient;

public class DiskUsageCachePlugin implements CramsIndexerPlugin {
	private CacheClient cacheClient;
	private String properties;
	public DiskUsageCachePlugin(){
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
		// calculate disk usage rate
		if(dataMap == null || dataMap.isEmpty())
			return null;
		
		String vmUuid = (String) dataMap.get("vm_uuid");
		String vdiName = (String) dataMap.get("vdi_name");
		String deviceName = (String) dataMap.get("device");
		String vdiSize = String.valueOf(dataMap.get("vdi_size"));
		String vdiUsingSize = String.valueOf(dataMap.get("vdi_using_size"));

		if(vmUuid == null || deviceName == null || vdiSize == null || vdiUsingSize == null)
			return null;
		
		try {
			cacheClient.set(vdiName, deviceName + "," + vdiSize + "," + vdiUsingSize, 130);

			// set or update vid, vm mapping infomation
			String vdiMappingInfo = cacheClient.get(vmUuid);
			if (vdiMappingInfo == null || vdiMappingInfo.isEmpty()) {
				vdiMappingInfo = vdiName;
				cacheClient.set(vmUuid, vdiMappingInfo, 130);
			} else if (!vdiMappingInfo.contains(vdiName)) {
				vdiMappingInfo = vdiMappingInfo + "," + vdiName;
				cacheClient.set(vmUuid, vdiMappingInfo, 130);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getProperties() {
		return properties;
	}

	@Override
	public boolean needProperties() {
		return true;
	}


}
