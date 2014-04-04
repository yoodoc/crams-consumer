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
<<<<<<< HEAD
    private static final String UCLOUD_SERVER_NAMESPACE = "ucloud/server";
    private static final String UCLOUD_RDBAAS_NAMESPACE = "ucloud/db";
    private static final String UCLOUD_VR_NAMESPACE = "ucloud/vr";

    private String properties;
=======

>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    private Logger logger;
    private List<CramsConsumerPlugin> plugins;
    private UcloudDBSelector db;
    private Map<String, String> vmNameMap;
    private boolean readyDbConnection = false;
<<<<<<< HEAD
    private String uwatchBaseUrl = "http://localhost:8080/watch";
    private String uwatchPutMetricCmdParam = "?command=putMetricData";
=======
    private String baseUrl = "http://localhost:8080/watch?command=putMetricData";
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6

    public UcloudWatchPlugin() {
        logger = LogManager.getLogger("PLUGINS");
        vmNameMap = new HashMap<String, String>();
<<<<<<< HEAD
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
        if (properties == null || propertiesArray.length <= 0) {
            throw new CramsPluginException(
                    "property is required for UcloudWatchPlugin, it can be "
                            + "<DB type>,<DB url>");
        }

        String dbSelectorType = propertiesArray[0];
        try {
            if ("UNITTEST".equalsIgnoreCase(dbSelectorType)) {
=======

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
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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

<<<<<<< HEAD
        if (propertiesArray.length == 2) {
            this.uwatchBaseUrl = propertiesArray[1];
        }

        // set required plugins
        setRequiredPlugins();
    }

    private void setBaseUrl(String string) {
        // TODO Auto-generated method stub

=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
<<<<<<< HEAD
        return this.properties;
    }

    /**
     * create ucloud watch metric data from xenRrd & ucloud portal db info and
     * send putmetric http request to ucloud watch server
     */
    @Override
    public Map<String, Object> excute(Map<String, Object> xenRrd, String dataTag)
=======
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> rrdMap, String dataTag)
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            throws CramsPluginException {
        if (!readyDbConnection) {
            throw new CramsPluginException("plugin not initialized.");
        }

        // make avg data from vm rrd
<<<<<<< HEAD
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
=======
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

>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        if (demensionList == null || demensionList.isEmpty()) {
            return null;
        }

        // create metric data
<<<<<<< HEAD
        String ucloudWatchRequestParmaeter = createPutMatricRequest(namespace,
                owner, demensionList, xenRrd);
        if (ucloudWatchRequestParmaeter == null
                || ucloudWatchRequestParmaeter.isEmpty()) {
            logger.error("noting to send, original data:" + xenRrd);
            return null;
        }
        // send metric data
=======
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

>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        for (int i = 0; i < MAX_RETRY; i++) {
            String response = send(ucloudWatchRequestParmaeter);
            if (response == null) {
                logger.error(ucloudWatchRequestParmaeter
                        + ": failed to send watch request at " + dataTag
<<<<<<< HEAD
                        + ", data map :" + xenRrd);
=======
                        + ", data map :" + rrdMap);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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

<<<<<<< HEAD
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
            String vmName = null;
            if (UCLOUD_SERVER_NAMESPACE.equalsIgnoreCase(namespace)) {
                vmName = getUcloudVmName(namespace, xenRrd);
            } else if (UCLOUD_RDBAAS_NAMESPACE.equalsIgnoreCase(namespace)) {
                vmName = (String) xenRrd.get("vm_uuid");
            } else if (UCLOUD_VR_NAMESPACE.equalsIgnoreCase(namespace)) {
                vmName = (String) xenRrd.get("vm_name");
            }

            if (vmName == null || vmName.isEmpty()) {
                return demensionList;
            }
            demensionList.add(new UcloudWatchDemension("name", vmName));

            // create "AutoScalingGroupName" demension value
            if (vmName.startsWith("uas-")) {
                String[] nameSeries = vmName.split("-");
                String autoscalingGroupName = nameSeries[1];
                demensionList.add(new UcloudWatchDemension(
                        "AutoScalingGroupName", autoscalingGroupName));
            }
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
=======
    private String createPutMatricRequest(String namespace, String owner,
            List<UcloudWatchMetricData> ucloudWatchMetricData) throws CramsPluginException {
        String requestParameter = null;
        try {
            requestParameter = baseUrl + "&namespace="
                    + getUrlEncodedValue(namespace) + "&owner="
                    + getUrlEncodedValue(owner) + "&requesttype=vmagent";
        } catch (UnsupportedEncodingException e) {
            throw new CramsPluginException("failed to create encoded request", e);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        }

        for (int i = 0; i < ucloudWatchMetricData.size(); i++) {
            String metricParameter = null;
            try {
                metricParameter = ucloudWatchMetricData.get(i)
                        .getRequestParameter(
<<<<<<< HEAD
                                "metricData.member." + String.valueOf(i + 1)
                                        + ".");
            } catch (Exception e) {
                throw new CramsPluginException(
                        "failed to create putMatricRequest", e);
=======
                                "metricData.member." + String.valueOf(i + 1) + ".");
            } catch (Exception e) {
                throw new CramsPluginException("failed to create putMatricRequest", e);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            }
            requestParameter = requestParameter + metricParameter;
        }
        return requestParameter;
    }

<<<<<<< HEAD
    /**
     * get vm name in ucloud portal(db) for normal vm that exclude vr,
     * rdbaas_instance
     * 
     * @param xenRrd
     * @return
     * @throws CramsPluginException
     */
    private String getUcloudVmName(String namespace, Map<String, Object> xenRrd)
            throws CramsPluginException {
        String vmUuid = (String) xenRrd.get(VM_UUID);
        if (vmUuid == null) {
            throw new CramsPluginException("null vmUuid," + xenRrd);
        }

        // return vm_name as ucloud demension name for vr
        // return uuid as ucloud dememnsion name for rdbaas
        if (UCLOUD_VR_NAMESPACE.equals(namespace)) {
            return (String) xenRrd.get(VM_NAME);
        } else if (UCLOUD_RDBAAS_NAMESPACE.equals(namespace)) {
            return vmUuid;
        }

        String vmName = vmNameMap.get(vmUuid);
        if (vmName == null) {
            vmName = db.getVmNameByVmId(vmUuid);
            if (vmName == null) {
                throw new CramsPluginException("can't find vmName for vmUuid="
                        + vmUuid + "," + xenRrd);
=======
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
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            }
            vmNameMap.put(vmUuid, vmName);
        }

        return vmName;
    }

<<<<<<< HEAD
    private String parseOwnerField(Map<String, Object> xenRrd) {
        try {
            return (String) xenRrd.get("vm_account_name");
        } catch (Exception e) {
            logger.error("failed to parse vm_account_name, " + xenRrd, e);
=======
    private String getOwnerField(Map<String, Object> rrdMap) {
        try {
            return (String) rrdMap.get("vm_account_name");
        } catch (Exception e) {
            logger.error("failed to parse vm_account_name, " + rrdMap, e);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        }
        return null;
    }

<<<<<<< HEAD
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
=======
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
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    }

    @Override
    public boolean needProperties() {
        // TODO Auto-generated method stub
        return false;
    }

<<<<<<< HEAD
    private String getUrlEncodedValue(String value)
            throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    private List<UcloudWatchMetricData> createUWatchMetricData(
            List<UcloudWatchDemension> demensionList, Map<String, Object> xenRrd) {
=======
    private String getUrlEncodedValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }

    private List<UcloudWatchMetricData> createMetricDataList(List<UcloudWatchDemension> demensionList, Map<String, Object> rrdMap) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        // init data type
        List<UcloudWatchMetricData> metricData = new ArrayList<UcloudWatchMetricData>();

        // get metric data
<<<<<<< HEAD
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
=======
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
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
                || "null".equalsIgnoreCase(memomyTargetValue)
                || "null".equalsIgnoreCase(memoryInternalFreeValue)
                || "null".equalsIgnoreCase(vbdReadAvgValue)
                || "null".equalsIgnoreCase(vbdWriteAvgValue)
                || "null".equalsIgnoreCase(vifRxAvgValue)
                || "null".equalsIgnoreCase(vifTxAvgValue)) {
<<<<<<< HEAD
            return metricData;
=======
            return null;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
