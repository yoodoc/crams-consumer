package com.ktcloudware.crams.consumer.plugins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.clients.HttpClient;
import com.ktcloudware.crams.consumer.datatype.UcloudWatchDemension;
import com.ktcloudware.crams.consumer.datatype.UcloudWatchMetricData;

public class UcloudWatchPlugin implements CramsConsumerPlugin {
	private static final Object VM_TYPE = "vm_type";
	private static final Object VM_UUID = "vm_uuid";
	private static final Object VM_NAME = "vm_name";
	private static final int MAX_RETRY = 5;
	private static final String UCLOUD_SERVER_NAMESPACE = "ucloud/server";
	private static final String UCLOUD_RDBAAS_NAMESPACE = "ucloud/db";
	private static final String UCLOUD_VR_NAMESPACE = "ucloud/vr";

	private String properties;
	private Logger logger;
	private List<CramsConsumerPlugin> plugins;
	private String uwatchBaseUrl = "http://localhost:8080/watch";
	private String uwatchPutMetricCmdParam = "?command=putMetricData";

	public UcloudWatchPlugin() {
		logger = LogManager.getLogger("PLUGINS");
	}

	/**
	 * set DB type & ucloud watch base url. DB type can be STAGING, PRODUCT or
	 * UNITTEST
	 * 
	 * @throws CramsInitiationException
	 */
	@Override
	public void setProperties(String properties) throws CramsPluginException {
		this.properties = properties;
		String[] propertiesArray = properties.split(",");
		if (properties == null || propertiesArray.length != 1) {
			throw new CramsPluginException(
					"property is required for UcloudWatchPlugin, ucloud watch baseurl requried.");
		}

		this.uwatchBaseUrl = propertiesArray[0];

		// set required plugins
		setRequiredPlugins();
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
		return this.properties;
	}

	/**
	 * create ucloud watch metric data from xenRrd send putmetric http request
	 * to ucloud watch server
	 */
	@Override
	public Map<String, Object> excute(Map<String, Object> xenRrd, String dataTag)
			throws CramsPluginException {
	    if (xenRrd == null || xenRrd.isEmpty()) {
            return xenRrd;
        }
	    
		// make avg data from vm rrd
		xenRrd = excuteCramsPlugins(xenRrd, dataTag);

		// parse namespace
		String namespace = null;
		namespace = createUWatchNamespace(xenRrd);

		// parse ownerField
		String owner = parseOwnerField(xenRrd);
		if (owner == null) {
			return null;
		}

		// parse demension list
		List<UcloudWatchDemension> demensionList = parseUWatchDemensionList(
				namespace, xenRrd);
		if (demensionList == null || demensionList.isEmpty()) {
			return null;
		}

		// create metric data
		String ucloudWatchRequestParmaeter = createPutMatricRequest(namespace,
				owner, demensionList, xenRrd);
		if (ucloudWatchRequestParmaeter == null
				|| ucloudWatchRequestParmaeter.isEmpty()) {
			logger.error("noting to send, original data:" + xenRrd);
			return null;
		}
		// send metric data
		for (int i = 0; i < MAX_RETRY; i++) {
			String response = send(ucloudWatchRequestParmaeter);
			if (response == null) {
				System.out.println("!! failed");
				logger.error(ucloudWatchRequestParmaeter
						+ ": failed to send watch request at " + dataTag
						+ ", data map :" + xenRrd);
			} else {
				logger.trace("send ucloud watch request : "
						+ ucloudWatchRequestParmaeter + ", response : "
						+ response);
				break;
			}
		}
		return null;
	}

	private String send(String ucloudWatchRequestParmaeter) {
		return HttpClient.sendRequest(ucloudWatchRequestParmaeter);
	}

	/**
	 * create UcloudWatchDemension list, that has demension name & demension
	 * value
	 * 
	 * rdbaas_instance use vm_uuid as "name" demension value, vr use xen vm_name
	 * as "name" demension value
	 * 
	 * @param namespace
	 * @param xenRrd
	 * @return List<UcloudWatchDemension> that contains UWatch Demension name,
	 *         UWatch Demension value pair
	 * @throws CramsPluginException
	 */
	private List<UcloudWatchDemension> parseUWatchDemensionList(
			String namespace, Map<String, Object> xenRrd)
			throws CramsPluginException {
		List<UcloudWatchDemension> demensionList = new ArrayList<UcloudWatchDemension>();
		try {
			// create "name" demension value
			
			String vmName = (String) xenRrd.get(VM_NAME);
			String vmUuid = (String) xenRrd.get(VM_UUID);
			if (null == vmName || null == vmUuid) {
				throw new CramsPluginException("failed to create demension field," + xenRrd.toString());
			}
			if (UCLOUD_SERVER_NAMESPACE.equalsIgnoreCase(namespace)) {
				vmName = "(" + vmUuid + ")";
				String vmTemplateName = (String) xenRrd.get("vm_template_name");
				if (!"".equals(vmTemplateName)) {
					demensionList.add(new UcloudWatchDemension("templatename",
							vmTemplateName));
				}
				String vmGuestOsName = (String) xenRrd.get("vm_service_offering_name");
				if (!"".equals(vmGuestOsName)) {
					demensionList.add(new UcloudWatchDemension("serviceofferingname",
							vmGuestOsName));
				}
				// create "AutoScalingGroupName" demension value
				if (vmName.startsWith("uas-")) {
					String[] nameSeries = vmName.split("-");
					String autoscalingGroupName = nameSeries[1];
					demensionList.add(new UcloudWatchDemension(
							"AutoScalingGroupName", autoscalingGroupName));
				}
			} else if (UCLOUD_RDBAAS_NAMESPACE.equalsIgnoreCase(namespace)) {
				vmName = vmUuid;
			} else if (UCLOUD_VR_NAMESPACE.equalsIgnoreCase(namespace)) {
				//nothing 
			}

			demensionList.add(new UcloudWatchDemension("name", vmName));
		} catch (Exception e) {
			logger.error("failed to create demension field,", e);
			throw new CramsPluginException("failed to create demension field,",
					e);
		}
		return demensionList;
	}

	private Map<String, Object> excuteCramsPlugins(Map<String, Object> xenRrd,
			String dataTag) throws CramsPluginException {
		for (CramsConsumerPlugin plugin : plugins) {
			try {
				xenRrd = plugin.excute(xenRrd, dataTag);
				if (xenRrd == null) {
					throw new CramsPluginException("null plugin result");
				}
			} catch (Exception e) {
				logger.error(plugin.getClass().getName() + " plugin error at "
						+ dataTag + "," + xenRrd, e);
				throw new CramsPluginException(plugin.getClass().getName()
						+ " plugin error at " + dataTag + "," + xenRrd, e);
			}
		}
		return xenRrd;
	}

	/**
	 * create http request to put ucloud watch metric it contains metric data
	 * for single xen rrd data
	 * 
	 * @param namespace
	 * @param owner
	 * @param demensionList
	 * @param xenRrd
	 * @return
	 * @throws CramsPluginException
	 */
	private String createPutMatricRequest(String namespace, String owner,
			List<UcloudWatchDemension> demensionList, Map<String, Object> xenRrd)
			throws CramsPluginException {
		// create metric data
		List<UcloudWatchMetricData> ucloudWatchMetricData = null;
		try {
			ucloudWatchMetricData = createUWatchMetricData(demensionList,
					xenRrd);
		} catch (Exception e) {
			logger.error("failed to parsing rrd data,", e);
			throw new CramsPluginException("failed to parsing rrd data,", e);
		}

		if (ucloudWatchMetricData == null || ucloudWatchMetricData.isEmpty()) {
			throw new CramsPluginException("failed to parsing matric data,"
					+ xenRrd);
		}

		// create metric data with url query format
		String requestParameter = null;
		try {
			requestParameter = uwatchBaseUrl + uwatchPutMetricCmdParam
					+ "&namespace=" + getUrlEncodedValue(namespace) + "&owner="
					+ getUrlEncodedValue(owner) + "&requesttype=vmagent";
		} catch (UnsupportedEncodingException e) {
			throw new CramsPluginException("failed to create encoded request",
					e);
		}

		for (int i = 0; i < ucloudWatchMetricData.size(); i++) {
			String metricParameter = null;
			try {
				metricParameter = ucloudWatchMetricData.get(i)
						.getRequestParameter(
								"metricData.member." + String.valueOf(i + 1)
										+ ".");
			} catch (Exception e) {
				throw new CramsPluginException(
						"failed to create putMatricRequest", e);
			}
			requestParameter = requestParameter + metricParameter;
		}
		return requestParameter;
	}

	/**
	 * get vm name in ucloud portal(db) for normal vm that exclude vr,
	 * rdbaas_instance
	 * 
	 * @param xenRrd
	 * @return
	 * @throws CramsPluginException
	 */
	/*
	 * private String getUcloudVmName(String namespace, Map<String, Object>
	 * xenRrd) throws CramsPluginException { String vmUuid = (String)
	 * xenRrd.get(VM_UUID); if (vmUuid == null) { throw new
	 * CramsPluginException("null vmUuid," + xenRrd); }
	 * 
	 * // return vm_name as ucloud demension name for vr // return uuid as
	 * ucloud dememnsion name for rdbaas if
	 * (UCLOUD_VR_NAMESPACE.equals(namespace)) { return (String)
	 * xenRrd.get(VM_NAME); } else if
	 * (UCLOUD_RDBAAS_NAMESPACE.equals(namespace)) { return vmUuid; }
	 * 
	 * String vmName = vmNameMap.get(vmUuid); if (vmName == null) { vmName =
	 * db.getVmNameByVmId(vmUuid); if (vmName == null) { throw new
	 * CramsPluginException("can't find vmName for vmUuid=" + vmUuid + "," +
	 * xenRrd); } vmNameMap.put(vmUuid, vmName); }
	 * 
	 * return vmName; }
	 */

	private String parseOwnerField(Map<String, Object> xenRrd) {
		try {
			return (String) xenRrd.get("vm_account_name");
		} catch (Exception e) {
			logger.error("failed to parse vm_account_name, " + xenRrd, e);
		}
		return null;
	}

	/**
	 * get Namespace for ucloud watch metric data if depends on vm_display_namm
	 * & vmType fields in xenRrd
	 * 
	 * @param dataMap
	 * @return ucloud/server, ucloud/db or ucloud/vr
	 */
	private String createUWatchNamespace(Map<String, Object> dataMap) {
		try {
			String displayName = (String) dataMap.get("vm_display_name");
			String vmType = (String) dataMap.get(VM_TYPE);
			if ("rdbaas-instance".equalsIgnoreCase(displayName)
					|| "rdbaas_instance".equalsIgnoreCase(displayName)) {
				return UCLOUD_RDBAAS_NAMESPACE;
			} else if ("DomainRouter".equalsIgnoreCase(vmType)) {
				return UCLOUD_VR_NAMESPACE;
			}
		} catch (Exception e) {
			logger.error(
					"failed to parse vm_display_name, " + dataMap.toString(), e);
		}
		return UCLOUD_SERVER_NAMESPACE;
	}

	@Override
	public boolean needProperties() {
		// TODO Auto-generated method stub
		return false;
	}

	private String getUrlEncodedValue(String value)
			throws UnsupportedEncodingException {
		return URLEncoder.encode(value, "UTF-8");
	}

	private List<UcloudWatchMetricData> createUWatchMetricData(
			List<UcloudWatchDemension> demensionList, Map<String, Object> xenRrd) {
		// init data type
		List<UcloudWatchMetricData> metricData = new ArrayList<UcloudWatchMetricData>();

		// get metric data
		String timestamp = (String) xenRrd.get("datetime");
		String cpuUtilizationValue = String.valueOf(xenRrd
				.get(CpuAvgPlugin.CPU_AVG));
		String memomyTargetValue = String.valueOf(xenRrd.get("memory_target"));
		String memoryInternalFreeValue = String.valueOf(xenRrd
				.get("memory_internal_free"));
		String vbdReadAvgValue = String.valueOf(xenRrd
				.get(VbdReadWriteAvgPlugin.VBD_READ_AVG));
		String vbdWriteAvgValue = String.valueOf(xenRrd
				.get(VbdReadWriteAvgPlugin.VBD_WRITE_AVG));
		String vifRxAvgValue = String.valueOf(xenRrd
				.get(VifAvgPlugin.VIF_RX_AVG));
		String vifTxAvgValue = String.valueOf(xenRrd
				.get(VifAvgPlugin.VIF_TX_AVG));
		if (timestamp == null || "null".equalsIgnoreCase(cpuUtilizationValue)
				|| "null".equalsIgnoreCase(memomyTargetValue)
				|| "null".equalsIgnoreCase(memoryInternalFreeValue)
				|| "null".equalsIgnoreCase(vbdReadAvgValue)
				|| "null".equalsIgnoreCase(vbdWriteAvgValue)
				|| "null".equalsIgnoreCase(vifRxAvgValue)
				|| "null".equalsIgnoreCase(vifTxAvgValue)) {
			return metricData;
		}

		UcloudWatchMetricData cpuUtilization = new UcloudWatchMetricData();
		cpuUtilization.metricName = "CPUUtillization";
		cpuUtilization.unit = "Percent";
		cpuUtilization.value = cpuUtilizationValue;
		cpuUtilization.timestamp = timestamp;
		cpuUtilization.demension.addAll(demensionList);
		metricData.add(cpuUtilization);

		UcloudWatchMetricData memoryTarget = new UcloudWatchMetricData();
		memoryTarget.metricName = "MemoryTarget";
		memoryTarget.unit = "Bytes";
		memoryTarget.value = memomyTargetValue;
		memoryTarget.timestamp = timestamp;
		memoryTarget.demension.addAll(demensionList);
		metricData.add(memoryTarget);

		UcloudWatchMetricData memoryInternalFree = new UcloudWatchMetricData();
		memoryInternalFree.metricName = "MemoryInternalFree";
		memoryInternalFree.unit = "Bytes";
		memoryInternalFree.value = memoryInternalFreeValue;
		memoryInternalFree.timestamp = timestamp;
		memoryInternalFree.demension.addAll(demensionList);
		metricData.add(memoryInternalFree);

		UcloudWatchMetricData vbdReadAvg = new UcloudWatchMetricData();
		vbdReadAvg.metricName = "DiskReadBytes";
		vbdReadAvg.unit = "Bytes";
		vbdReadAvg.value = vbdReadAvgValue;
		vbdReadAvg.timestamp = timestamp;
		vbdReadAvg.demension.addAll(demensionList);
		metricData.add(vbdReadAvg);

		UcloudWatchMetricData vbdWriteAvg = new UcloudWatchMetricData();
		vbdWriteAvg.metricName = "DiskWriteBytes";
		vbdWriteAvg.unit = "Bytes";
		vbdWriteAvg.value = vbdWriteAvgValue;
		vbdWriteAvg.timestamp = timestamp;
		vbdWriteAvg.demension.addAll(demensionList);
		metricData.add(vbdWriteAvg);

		UcloudWatchMetricData vifRxAvg = new UcloudWatchMetricData();
		vifRxAvg.metricName = "NetworkIn";
		vifRxAvg.unit = "Bytes";
		vifRxAvg.value = vifRxAvgValue;
		vifRxAvg.timestamp = timestamp;
		vifRxAvg.demension.addAll(demensionList);
		metricData.add(vifRxAvg);

		UcloudWatchMetricData vifTxAvg = new UcloudWatchMetricData();
		vifTxAvg.metricName = "NetworkOut";
		vifTxAvg.unit = "Bytes";
		vifTxAvg.value = vifTxAvgValue;
		vifTxAvg.timestamp = timestamp;
		vifTxAvg.demension.addAll(demensionList);
		metricData.add(vifTxAvg);

		return metricData;
	}
}
