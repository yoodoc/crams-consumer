package com.ktcloudware.crams.consumer.plugins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.clients.HttpClient;
import com.ktcloudware.crams.consumer.clients.UcloudDBSelector;
import com.ktcloudware.crams.consumer.dataType.UcloudWatchDemension;
import com.ktcloudware.crams.consumer.dataType.UcloudWatchMetricData;

public class UcloudWatchPlugin implements CramsConsumerPlugin {
	private static final Object VM_TYPE = "vm_type";
	private static final Object VM_UUID = "vm_uuid";
	private static final Object VM_NAME = "vm_name";
	
	private Logger logger;
	private List<CramsConsumerPlugin> plugins;
	private UcloudDBSelector db;
	private Map<String, String> vmNameMap;
	private boolean readyDbConnection = false;
	private String baseUrl = "http://localhost:8080/watch?command=putMetricData";
	
	public UcloudWatchPlugin() {
		logger = LogManager.getLogger("PLUGINS.WATCHAGENT");
		vmNameMap = new HashMap<String, String>();

		// set required plugins
		setRequiredPlugins();

		// init ucloud info storage connector
		db = new UcloudDBSelector();

	}

	@Override
	public void setProperties(String dbSelectorType) throws Exception {
		if (dbSelectorType == null || dbSelectorType.isEmpty()) {
			throw new Exception("UcloudWatchPlugin wrong property");
		}
		if (dbSelectorType.equalsIgnoreCase("unittest")) {
			db = new MockDBSelector();
			readyDbConnection = true;
		} else if (dbSelectorType.equalsIgnoreCase("uclouddb")) {
			db = new UcloudDBSelector();
			readyDbConnection = true;
		} else {
			throw new Exception("UcloudWatchPlugin wrong property");
		}

	}

	private void setRequiredPlugins() {
		plugins = new ArrayList<CramsConsumerPlugin>();
		DateFormatPlugin plugin = new DateFormatPlugin();
		plugin.setProperties("datetime,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd'T'HH:mm:ss.SSS");
		plugins.add(plugin);
		plugins.add(new ReplaceVmAccountNamePlugin());
		plugins.add(new ReplaceDiskFieldNamePlugin());
		plugins.add(new CpuAvgPlugin());
		plugins.add(new VbdReadWriteAvgPlugin());
		plugins.add(new VifAvgPlugin());
	}

	@Override
	public String getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Object> excute(Map<String, Object> rrdMap, String dataTag)
			throws Exception {
		if (readyDbConnection == false) {
			throw new Exception("plugin not initialized.");
		}

		// make avg data from vm rrd
		for (CramsConsumerPlugin plugin : plugins) {
			rrdMap = plugin.excute(rrdMap, dataTag);
		}

		// create metric data from dataMap
		List<UcloudWatchMetricData> ucloudWatchMetricData = null;

		// parse namespace
		String namespace = "ucloud/server";
		namespace = getNamespaceFromRrd(rrdMap);

		// parse ownerField
		String owner = getOwnerFieldFromRrd(rrdMap);
		if (owner == null)
			return null;

		// parse demension list
		// TODO get vm name from vm_uuid than make demension value
		List<UcloudWatchDemension> demensionList = new ArrayList<UcloudWatchDemension>();
		demensionList = getDemesions(db, rrdMap);
		if(demensionList == null){
			return null;
		}
		
		// create metric data
		ucloudWatchMetricData = parseDataFromRrd(namespace, demensionList,
				rrdMap);

		// send metric data
		String ucloudWatchRequestParmaeter = createPutMatricRequest(namespace,
				owner, ucloudWatchMetricData);
		send(ucloudWatchRequestParmaeter);

		return null;
	}

	private void send(String ucloudWatchRequestParmaeter) {
		HttpClient.sendRequest(ucloudWatchRequestParmaeter);
		System.out.println(ucloudWatchRequestParmaeter);

	}

	private String createPutMatricRequest(String namespace, String owner,
			List<UcloudWatchMetricData> ucloudWatchMetricData) {
		String requestParameter = baseUrl + "&namespace=" + getUrlEncodedValue(namespace)
				+ "&owner=" + getUrlEncodedValue(owner)
				+ "&requesttype=vmagent";

		for (int i = 0; i < ucloudWatchMetricData.size(); i++) {
			String metricParameter = ucloudWatchMetricData.get(i)
					.getRequestParameter(
							"metricData.member." + String.valueOf(i+1) + ".");
			requestParameter = requestParameter + metricParameter;
		}
		return requestParameter;
	}

	private List<UcloudWatchDemension> getDemesions(
			UcloudDBSelector db, Map<String, Object> rrdMap) {
		List<UcloudWatchDemension> demensions = new ArrayList<UcloudWatchDemension>();

		String vmName = getUcloudVmName(rrdMap);
		if (vmName == null || vmName.isEmpty()) {
			return null;
		}

		if (vmName.startsWith("uas-")) {
			String[] nameSeries = vmName.split("-");
			String autoscalingGroupName = nameSeries[1];
			demensions.add(new UcloudWatchDemension("AutoScalingGroupName",
					autoscalingGroupName));

		}
		demensions.add(new UcloudWatchDemension("name", vmName));

		return demensions;
	}

	private String getUcloudVmName(Map<String, Object> rrdMap) {
		String vmType = (String)rrdMap.get(VM_TYPE);
		if(vmType != null && vmType.equalsIgnoreCase("DomainRouter")){
			return (String)rrdMap.get(VM_NAME);
		}
		
		String vmUuid = (String) rrdMap.get(VM_UUID);
		String vmName = vmNameMap.get(vmUuid);
		if (vmName == null || !vmName.isEmpty()) {
			vmName = db.getVmNameByVmId(vmUuid);
			vmNameMap.put(vmUuid, vmName);
		}

		return vmName;
	}

	private String getOwnerFieldFromRrd(Map<String, Object> rrdMap) {
		try {
			return (String) rrdMap.get("vm_account_name");
		} catch (Exception e) {
			logger.error("failed to parse vm_account_name, " + rrdMap, e);
		}
		return null;
	}

	private String getNamespaceFromRrd(Map<String, Object> dataMap) {
		try{
			String displayName = (String) dataMap.get("vm_display_name");
			String vmType = (String) dataMap.get(VM_TYPE);
			if(displayName != null
					&& displayName.equalsIgnoreCase("rdbaas_instance")) {
				return "ucloud/db";
			}else if(vmType != null && vmType.equalsIgnoreCase("DomainRouter")){
				return "ucloud/vr";
			}

		}catch(Exception e){
			logger.error("failed to parse vm_display_name, "
					+ dataMap.toString());
		}
		return "ucloud/server";
	}

	@Override
	public boolean needProperties() {
		// TODO Auto-generated method stub
		return false;
	}

	private List<UcloudWatchMetricData> parseDataFromRrd(String namespace,
			List<UcloudWatchDemension> demensionList,
			Map<String, Object> dataMap) {
		// init data type
		List<UcloudWatchMetricData> metricData = new ArrayList<UcloudWatchMetricData>();

		// get timestamp
		String timestamp = (String) dataMap.get("datetime");

		UcloudWatchMetricData cpuUtilization = new UcloudWatchMetricData();
		cpuUtilization.metricName = "CPUUtillization";
		cpuUtilization.unit = "Percent";
		cpuUtilization.value = String
				.valueOf(dataMap.get(CpuAvgPlugin.CPU_AVG));
		cpuUtilization.timestamp = timestamp;
		cpuUtilization.demension.addAll(demensionList);
		metricData.add(cpuUtilization);

		UcloudWatchMetricData memoryTarget = new UcloudWatchMetricData();
		memoryTarget.metricName = "MemoryTarget";
		memoryTarget.unit = "Bytes";
		memoryTarget.value = String.valueOf(dataMap.get("memory_target"));
		memoryTarget.timestamp = timestamp;
		memoryTarget.demension.addAll(demensionList);
		metricData.add(memoryTarget);

		UcloudWatchMetricData memoryInternalFree = new UcloudWatchMetricData();
		memoryInternalFree.metricName = "MemoryInternalFree";
		memoryInternalFree.unit = "Bytes";
		memoryInternalFree.value = String.valueOf(dataMap
				.get("memory_internal_free"));
		memoryInternalFree.timestamp = timestamp;
		memoryInternalFree.demension.addAll(demensionList);
		metricData.add(memoryInternalFree);

		UcloudWatchMetricData vbdReadAvg = new UcloudWatchMetricData();
		vbdReadAvg.metricName = "DiskReadBytes";
		vbdReadAvg.unit = "Bytes";
		vbdReadAvg.value = String.valueOf(dataMap
				.get(VbdReadWriteAvgPlugin.VBD_READ_AVG));
		vbdReadAvg.timestamp = timestamp;
		vbdReadAvg.demension.addAll(demensionList);
		metricData.add(vbdReadAvg);

		UcloudWatchMetricData vbdWriteAvg = new UcloudWatchMetricData();
		vbdWriteAvg.metricName = "DiskWriteBytes";
		vbdWriteAvg.unit = "Bytes";
		vbdWriteAvg.value = String.valueOf(dataMap
				.get(VbdReadWriteAvgPlugin.VBD_WRITE_AVG));
		vbdWriteAvg.timestamp = timestamp;
		vbdWriteAvg.demension.addAll(demensionList);
		metricData.add(vbdWriteAvg);

		UcloudWatchMetricData vifRxAvg = new UcloudWatchMetricData();
		vifRxAvg.metricName = "NetworkIn";
		vifRxAvg.unit = "Bytes";
		vifRxAvg.value = String.valueOf(dataMap.get(VifAvgPlugin.VIF_RX_AVG));
		vifRxAvg.timestamp = timestamp;
		vifRxAvg.demension.addAll(demensionList);
		metricData.add(vifRxAvg);

		UcloudWatchMetricData vifTxAvg = new UcloudWatchMetricData();
		vifTxAvg.metricName = "NetworkOut";
		vifTxAvg.unit = "Bytes";
		vifTxAvg.value = String.valueOf(dataMap.get(VifAvgPlugin.VIF_TX_AVG));
		vifTxAvg.timestamp = timestamp;
		vifTxAvg.demension.addAll(demensionList);
		metricData.add(vifTxAvg);

		return metricData;
	}

	private String getUrlEncodedValue(String value) {
		String encodedValue = "";
		try {
			encodedValue = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encodedValue;
	}
}
