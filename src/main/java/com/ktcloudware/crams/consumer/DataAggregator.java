package com.ktcloudware.crams.consumer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.plugins.CramsPluginException;
import com.ktcloudware.crams.consumer.util.VMPerfDataUtil;

public class DataAggregator {
    private Logger logger;
    private Map<String, Map<String, Object>> vmPerfMap = new ConcurrentHashMap<String, Map<String, Object>>();
    private Map<String, List<Long>> vmTimestampsInMinutes = new ConcurrentHashMap<String, List<Long>>();
    private SimpleDateFormat originDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private int processedData = 0;
    private String tag = "";
    private long lastUpdateTime;

    public static final String CPU_AVG = "cpu_avg";
    private static final String KEY_DELETEMETER = "::Delimeter::";
    public static final String DATE_TIME_KEY = "datetime";
    public static final String VM_KEY = "vm_uuid";
    private static final String DATE_TIME_LONG = "datetime_in_millis";

    public DataAggregator() {
        logger = LogManager.getLogger("CRAMS_CONSUMER");
    }

    /**
     * 
     * @param dataMap
     * @return
     * @throws CramsPluginException
     */
    public synchronized Map<String, Object> add(Map<String, Object> dataMap)
            throws CramsPluginException {
        if (null == dataMap || dataMap.isEmpty()) {
            return null;
        }
        lastUpdateTime = System.currentTimeMillis();

        Map<String, Object> avgDataMap = new HashMap<String, Object>();
        String vmId = (String) dataMap.get(VM_KEY);
        if (vmId == null || vmId.isEmpty()) {
        	throw new CramsPluginException("failed to parse vm_uuid, " + dataMap.toString());
        }
        String timestamp = (String) dataMap.get(DATE_TIME_KEY);
        if (timestamp == null) {
        	throw new CramsPluginException("failed to parse " + DATE_TIME_KEY + ", " + dataMap.toString());
        }
        try {
            Date date = originDateFormat.parse(timestamp);
            addData(vmId, TimeUnit.MILLISECONDS.toMinutes(date.getTime()),
                    dataMap);
            avgDataMap = getFiveMinutesAvgData(vmId, false);
        } catch (ParseException e) {
            logger.warn("failed to parse datetime, " + dataMap, e);
            throw new CramsPluginException(e);
        }

        return avgDataMap;
    }

    private void addData(String vmId, long timestamp,
            Map<String, Object> dataMap) {
       // store new dataMap
        if (null == dataMap || dataMap.isEmpty()) {
            return;
        }
        
        Map<String, Object> receivedData = new HashMap<String, Object>(dataMap);
        receivedData.put(DATE_TIME_LONG, TimeUnit.MINUTES.toMillis(timestamp));
        vmPerfMap.put(vmId + KEY_DELETEMETER + timestamp, receivedData);
        List<Long> timestamps = vmTimestampsInMinutes.get(vmId);
        if (timestamps == null) {
            timestamps = new ArrayList<Long>();
        }
        timestamps.add(timestamp);
        vmTimestampsInMinutes.put(vmId, timestamps);
    }

    /**
     * get avgPerfData if available
     * 
     * @param vmId
     * @param force
     *            if true, returns get Avg for five minutes at that time. if
     *            false, return avg when five minute data is stored.
     * @return dataMap has fiveMinutes avg values. return null it needs more
     *         data to calculate avg values
     */
    private synchronized Map<String, Object> getFiveMinutesAvgData(String vmId,
            boolean force) {
        // get avgDataMap
        List<Long> timestamps = vmTimestampsInMinutes.get(vmId);
        if (timestamps == null || timestamps.isEmpty()) {
            return null;
        }
        long firstTimestamp = timestamps.get(0);
        long lastTimestamp = timestamps.get(timestamps.size() - 1);
        int dataSizeInSingleFiveMinutes = 0;
        
        //쌓여있는 모든 데이터를 제거하는 경우 
        processedData++;
     
        if (true == force) {
            for (long timestamp : timestamps) {
                if (0 > timestamp - firstTimestamp) {
                    dataSizeInSingleFiveMinutes = 1;
                    break;
                }else if (5 > timestamp - firstTimestamp) {
                    dataSizeInSingleFiveMinutes++;
                } else {
                    break;
                }
            }
        }
        // 정상적으로 5분 데이터가 수집된 경우
        else if (4 == lastTimestamp - firstTimestamp) {
            dataSizeInSingleFiveMinutes = timestamps.size();
        } 
        // 5분 이상 데이터가 쌓이는 경우, 역순의 데이터가 쌓이는 경우 이전 성능 값을 이용해 평균값을 계산한다.
        else if ((4 < lastTimestamp - firstTimestamp)  || (0 > lastTimestamp - firstTimestamp)){
             dataSizeInSingleFiveMinutes = timestamps.size() - 1;
        } else {
            processedData--;
        }

        if (0 == dataSizeInSingleFiveMinutes) {
            return null;
        } 
        
        //update timestamps in datacache
        timestamps = timestamps.subList(dataSizeInSingleFiveMinutes,
                timestamps.size());
        if (timestamps.isEmpty()) {
           vmTimestampsInMinutes.remove(vmId); 
        } else {
            vmTimestampsInMinutes.put(vmId, timestamps);
        }
        
        // calculate avg values
        VMPerfDataUtil perfData = new VMPerfDataUtil();
        
        for (int i = 0; i < 5; i++) {
            String key = vmId + KEY_DELETEMETER + (firstTimestamp + i);
            perfData.addPerfData(vmPerfMap.remove(key));
        }
        Map<String, Object> data = perfData.getAvgValuesData();
        if (null == data || data.isEmpty()) {
            return null;
        }
         Long datetimeInMills = (Long) data.remove(DATE_TIME_LONG);
        String datetime = originDateFormat.format(new Date(datetimeInMills));  
        data.put(DATE_TIME_KEY, datetime);
        //data.remove(DATE_TIME_IN_MINUTES);
        return data;
    }

    /**
     * clean cache and return averages for all vm
     * @param avgs
     * @return
     */
    public List<Map<String, Object>> clean(List<Map<String, Object>> avgs) {
        for (String vm : vmTimestampsInMinutes.keySet()) {
            Map<String, Object> avgData = getFiveMinutesAvgData(vm, true);
            avgs.add(avgData);
        }
        if (vmTimestampsInMinutes.size() != 0) {
            avgs = clean(avgs);
        }
        return avgs;
    }

    /**
     * if datacache is idle for timeDuration, clean cache and return averages for all vm
     * @param timeDuration millis
     * @return
     */
    public List<Map<String, Object>> cleanIfIdle(long timeDuration) {
        List<Map<String, Object>> avgs = new ArrayList<Map<String, Object>>();
        if ((lastUpdateTime + timeDuration) < System.currentTimeMillis()) {
            avgs = clean(avgs);
        }
        return avgs;
    }
    
    public String getStats() {
        return tag +" processed avg data count=" + processedData + ",remained vm count=" + vmTimestampsInMinutes.size() + ",remained perfData count=" + vmPerfMap.size() ;
    }

    public void setTag(String string) {
        tag  = string; 
    }
    
}
