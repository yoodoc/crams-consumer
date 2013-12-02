package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.clients.CacheClient;
import com.ktcloudware.crams.consumer.clients.RedisClient;

public class DiskUsageCachePlugin implements CramsIndexerPlugin {
	Logger logger = LogManager.getLogger("PLUGINS");
	private CacheClient cacheClient;
	private String properties;
	private int expireTimeInMinutes = 3000;

	public DiskUsageCachePlugin() {
	}

	@Override
	public void setProperties(String pluginProperties) {
		try {
			this.properties = pluginProperties;
			String[] addressAndExpireTime = properties.split(",");
			initCacheClient(addressAndExpireTime[0]);
			expireTimeInMinutes = Integer.valueOf(addressAndExpireTime[1]);
		} catch (Exception e) {
			logger.error("plugin init error" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void initCacheClient(String cacheAddress) throws Exception {
		try {
		this.cacheClient = new RedisClient(cacheAddress);
		logger.info("success to connect");
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag) {
		// calculate disk usage rate
		if (dataMap == null || dataMap.isEmpty())
			return null;

		// parsing disk_usage data
		String vmUuid = null;
		String vdiName = null;
		String vdiUsingSize = null;
		String vdiSize = null;
		String deviceName = null;
		try {
			vmUuid = (String) dataMap.get("vm_uuid");
			vdiName = (String) dataMap.get("vdi_name");
			deviceName = (String) dataMap.get("device");
			vdiSize = String.valueOf(dataMap.get("vdi_size"));
			vdiUsingSize = String.valueOf(dataMap.get("vdi_using_size"));
			if (vmUuid == null || deviceName == null || vdiSize == null
					|| vdiUsingSize == null)
				throw new Exception("null data");
		} catch (Exception e) {
			logger.warn("disk usage parsing error, " + e.getMessage() + ":"
					+ dataMap);
			return null;
		}

		try {
			cacheClient.set(vdiName, deviceName + "," + vdiSize + ","
					+ vdiUsingSize, expireTimeInMinutes);
			logger.trace("set cache data for vdi_name, vm_uuid=" + vmUuid);
			// set or update vdi, vm mapping infomation
			String vdiMappingInfo = cacheClient.get(vmUuid);
			if (vdiMappingInfo == null || vdiMappingInfo.isEmpty()) {
				vdiMappingInfo = vdiName;
			} else if (!vdiMappingInfo.contains(vdiName)) {
				vdiMappingInfo = vdiMappingInfo + "," + vdiName;
			} 
			cacheClient.set(vmUuid, vdiMappingInfo, expireTimeInMinutes);
			logger.trace("set cache data for vm_uuid=" + vmUuid
					+ " , vdi_name=" + vdiName + " expire time:"
					+ expireTimeInMinutes);
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
