package com.ktcloudware.crams.consumer.plugins;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.HttpClient;
import com.ktcloudware.crams.consumer.clients.UcloudDBSelector;
import com.ktcloudware.crams.consumer.datatype.UcloudWatchDemension;
import com.ktcloudware.crams.consumer.datatype.UcloudWatchMetricData;

public class UcloudWatchPlugin implements CramsConsumerPlugin {
    private static final Object VM_TYPE = "vm_type";
    private static final Object VM_UUID = "vm_uuid";
    private static final Object VM_NAME = "vm_name";
    private static final int MAX_RETRY = 5;

    private Logger logger;
    private List<CramsConsumerPlugin> plugins;
    private UcloudDBSelector db;
    private Map<String, String> vmNameMap;
    private boolean readyDbConnection = false;
    private String baseUrl = "http://localhost:8080/watch?command=putMetricData";

    public UcloudWatchPlugin() {
        logger = LogManager.getLogger("PLUGINS");
        vmNameMap = new HashMap<String, String>();

        // set required plugins
        setRequiredPlugins();

     }

    @Override
    public void setProperties(String dbSelectorType)
            throws CramsPluginException {
        if (dbSelectorType == null || dbSelectorType.isEmpty()) {
            throw new CramsPluginException("UcloudWatchPlugin wrong property");
        }
        try {
            if (dbSelectorType.equalsIgnoreCase("UNITTEST")) {
                db = new MockDBSelectorForUcloudWatchPlugin("");
                readyDbConnection = true;
            } else {
                db = new UcloudDBSelector(dbSelectorType);
                readyDbConnection = true;
            }
        } catch (CramsException e) {
            throw new CramsPluginException("UcloudWatchPlugin wrong property",
                    e);
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
            throws CramsPluginException {
        if (!readyDbConnection) {
            throw new CramsPluginException("plugin not initialized.");
        }

        // make avg data from vm rrd
        for (CramsConsumerPlugin plugin : plugins) {
            try {
                rrdMap = plugin.excute(rrdMap, dataTag);
                if (rrdMap == null) {
                    throw new CramsPluginException("null plugin result");
                }
            } catch (Exception e) {
                logger.error(plugin.getClass().getName() + " plugin error at "
                        + dataTag + "," + rrdMap, e);
                throw new CramsPluginException(plugin.getClass().getName()
                        + " plugin error at " + dataTag + "," + rrdMap, e);
            }
        }

        // create metric data from dataMap
        List<UcloudWatchMetricData> ucloudWatchMetricData = null;

        // parse namespace
        String namespace = "ucloud/server";
        namespace = getNamespace(rrdMap);

        // parse ownerField
        String owner = getOwnerField(rrdMap);
        if (owner == null) {
            return null;
        }
        
        // parse demension list
        // TODO get vm name from vm_uuid than make demension value
        List<UcloudWatchDemension> demensionList = new ArrayList<UcloudWatchDemension>();
        try {
            demensionList = getDemesions(rrdMap);
        } catch (Exception e) {
            logger.error("failed to create demension field,", e);
            throw new CramsPluginException("failed to create demension field,",
                    e);
        }

        if (demensionList == null || demensionList.isEmpty()) {
            return null;
        }

        // create metric data
        try {
            ucloudWatchMetricData = createMetricDataList(demensionList, rrdMap);
        } catch (Exception e) {
            logger.error("failed to parsing rrd data,", e);
            throw new CramsPluginException("failed to parsing rrd data,", e);
        }

        if (ucloudWatchMetricData == null || ucloudWatchMetricData.isEmpty()) {
            return null;
        }

        // send metric data
        String ucloudWatchRequestParmaeter = createPutMatricRequest(namespace,
                owner, ucloudWatchMetricData);

        for (int i = 0; i < MAX_RETRY; i++) {
            String response = send(ucloudWatchRequestParmaeter);
            if (response == null) {
                logger.error(ucloudWatchRequestParmaeter
                        + ": failed to send watch request at " + dataTag
                        + ", data map :" + rrdMap);
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

    private String createPutMatricRequest(String namespace, String owner,
            List<UcloudWatchMetricData> ucloudWatchMetricData) throws CramsPluginException {
        String requestParameter = null;
        try {
            requestParameter = baseUrl + "&namespace="
                    + getUrlEncodedValue(namespace) + "&owner="
                    + getUrlEncodedValue(owner) + "&requesttype=vmagent";
        } catch (UnsupportedEncodingException e) {
            throw new CramsPluginException("failed to create encoded request", e);
        }

        for (int i = 0; i < ucloudWatchMetricData.size(); i++) {
            String metricParameter = null;
            try {
                metricParameter = ucloudWatchMetricData.get(i)
                        .getRequestParameter(
                                "metricData.member." + String.valueOf(i + 1) + ".");
            } catch (Exception e) {
                throw new CramsPluginException("failed to create putMatricRequest", e);
            }
            requestParameter = requestParameter + metricParameter;
        }
        return requestParameter;
    }

    private List<UcloudWatchDemension> getDemesions(Map<String, Object> rrdMap) throws CramsPluginException {
        List<UcloudWatchDemension> demensions = new ArrayList<UcloudWatchDemension>();

        String vmName = getUcloudVmName(rrdMap);
        if (vmName == null || vmName.isEmpty()) {
            return demensions;
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

    private String getUcloudVmName(Map<String, Object> rrdMap)
            throws CramsPluginException {
        String vmType = (String) rrdMap.get(VM_TYPE);
        if ("DomainRouter".equalsIgnoreCase(vmType)) {
            return (String) rrdMap.get(VM_NAME);
        }

        String vmUuid = (String) rrdMap.get(VM_UUID);
        if (vmUuid == null || vmUuid.isEmpty()) {
            throw new CramsPluginException("null vmUuid," + rrdMap);
        }

        String vmName = vmNameMap.get(vmUuid);
        if (vmName == null || vmName.isEmpty()) {
            vmName = db.getVmNameByVmId(vmUuid);
            if (vmName == null || vmName.isEmpty()) {
                throw new CramsPluginException("can't find null vmUuid,"
                        + rrdMap);
            }
            vmNameMap.put(vmUuid, vmName);
        }

        return vmName;
    }

    private String getOwnerField(Map<String, Object> rrdMap) {
        try {
            return (String) rrdMap.get("vm_account_name");
        } catch (Exception e) {
            logger.error("failed to parse vm_account_name, " + rrdMap, e);
        }
        return null;
    }

    private String getNamespace(Map<String, Object> dataMap) {
        try {
            String displayName = (String) dataMap.get("vm_display_name");
            String vmType = (String) dataMap.get(VM_TYPE);
            if ("rdbaas-instance".equalsIgnoreCase(displayName) || "rdbaas_instance".equalsIgnoreCase(displayName)) {
                return "ucloud/db";
            } else if ("DomainRouter".equalsIgnoreCase(vmType)) {
                return "ucloud/vr";
            }

        } catch (Exception e) {
            logger.error("failed to parse vm_display_name, "
                    + dataMap.toString(), e);
        }
        return "ucloud/server";
    }

    @Override
    public boolean needProperties() {
        // TODO Auto-generated method stub
        return false;
    }

    private String getUrlEncodedValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    private List<UcloudWatchMetricData> createMetricDataList(List<UcloudWatchDemension> demensionList, Map<String, Object> rrdMap) {
        // init data type
        List<UcloudWatchMetricData> metricData = new ArrayList<UcloudWatchMetricData>();

        // get metric data
        String timestamp = (String) rrdMap.get("datetime");
        String cpuUtilizationValue = String.valueOf(rrdMap
                .get(CpuAvgPlugin.CPU_AVG));
        String memomyTargetValue = String.valueOf(rrdMap.get("memory_target"));
        String memoryInternalFreeValue = String.valueOf(rrdMap
                .get("memory_internal_free"));
        String vbdReadAvgValue = String.valueOf(rrdMap
                .get(VbdReadWriteAvgPlugin.VBD_READ_AVG));
        String vbdWriteAvgValue = String.valueOf(rrdMap
                .get(VbdReadWriteAvgPlugin.VBD_WRITE_AVG));
        String vifRxAvgValue = String.valueOf(rrdMap
                .get(VifAvgPlugin.VIF_RX_AVG));
        String vifTxAvgValue = String.valueOf(rrdMap
                .get(VifAvgPlugin.VIF_TX_AVG));
        if (timestamp == null || timestamp.isEmpty()
                || "null".equalsIgnoreCase(cpuUtilizationValue)
                || "null".equalsIgnoreCase(memomyTargetValue)
                || "null".equalsIgnoreCase(memoryInternalFreeValue)
                || "null".equalsIgnoreCase(vbdReadAvgValue)
                || "null".equalsIgnoreCase(vbdWriteAvgValue)
                || "null".equalsIgnoreCase(vifRxAvgValue)
                || "null".equalsIgnoreCase(vifTxAvgValue)) {
            return null;
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
