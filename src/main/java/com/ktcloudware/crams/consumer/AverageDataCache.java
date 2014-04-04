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

import com.google.common.cache.*;
import com.ktcloudware.crams.consumer.plugins.CramsPluginException;
import com.ktcloudware.crams.consumer.util.VMPerfDataUtil;

public class AverageDataCache {
    public static final String CPU_AVG = "cpu_avg";
    private Logger logger;
    private Map<String, Map<String, Object>> vmPerfMap = new ConcurrentHashMap<String, Map<String, Object>>();
    private Map<String, List<Long>> vmTimestampsInMinutes = new ConcurrentHashMap<String, List<Long>>();
    private Cache<String, Map<String, Object>> cache = null;

    private SimpleDateFormat originDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    private static final String KEY_DELETEMETER = "::Delimeter::";
    public static final String DATE_TIME_KEY = "datetime";
    public static final String VM_KEY = "vm_uuid";
    private long lastUpdateTime;
    private int processedData = 0;

    public AverageDataCache() {
        logger = LogManager.getLogger("CRAMS_CONSUMER");
        cache = CacheBuilder.newBuilder().concurrencyLevel(4).weakKeys()
                .maximumSize(10000).expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
    }

    public synchronized Map<String, Object> getAverage(Map<String, Object> dataMap)
            throws CramsPluginException {
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
            avgDataMap = getFiveMinuteAvgData(vmId, false);
        } catch (ParseException e) {
            logger.warn("failed to parse datetime, " + dataMap, e);
            throw new CramsPluginException(e);
        }

        cache.put(vmId, dataMap);
        return avgDataMap;
    }

    private void addData(String vmId, long timestampInMinutes,
            Map<String, Object> dataMap) {
       // store new dataMap
        if (null == dataMap || dataMap.isEmpty()) {
            return;
        }
        Map<String, Object> receivedData = new HashMap<String, Object>(dataMap);
        vmPerfMap.put(vmId + KEY_DELETEMETER + timestampInMinutes, receivedData);
        List<Long> timestamps = vmTimestampsInMinutes.get(vmId);
        if (timestamps == null) {
            timestamps = new ArrayList<Long>();
        }
        timestamps.add(timestampInMinutes);
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
    private synchronized Map<String, Object> getFiveMinuteAvgData(String vmId,
            boolean force) {
        // get avgDataMap
        List<Long> timestamps = vmTimestampsInMinutes.get(vmId);
        if (timestamps == null || timestamps.isEmpty()) {
            return null;
        }
        long firstMinutes = timestamps.get(0);
        long lastMinutes = timestamps.get(timestamps.size() - 1);
        int dataSizeInSingleFiveMinutes = 0;
      
        //쌓여있는 모든 데이터를 제거하는 경우 
        processedData++;
        if (true == force) {
            for (long timestamp : timestamps) {
                if (5 > (timestamp - firstMinutes)) {
                    dataSizeInSingleFiveMinutes++;
                } else {
                    break;
                }
            }
        }
        // 정상적으로 5분 데이터가 수집된 경우
        else if (4 == (lastMinutes - firstMinutes)) {
            dataSizeInSingleFiveMinutes = timestamps.size();
        }
        // 5분 이상 데이터가 쌓이는 순간 이전 성능 값을 이용해 평균값을 계산한다.
        else if (4 < (lastMinutes - firstMinutes)) {
            System.out.println("!!size " + timestamps.size());
            dataSizeInSingleFiveMinutes = timestamps.size() - 1;
        } else {
            processedData--;
        }

        if (0 == dataSizeInSingleFiveMinutes) {
            return null;
        }
        timestamps = timestamps.subList(dataSizeInSingleFiveMinutes,
                dataSizeInSingleFiveMinutes);
        if (timestamps.isEmpty()) {
           vmTimestampsInMinutes.remove(vmId); 
        } else {
            vmTimestampsInMinutes.put(vmId, timestamps);
        }
        
        // calculate avg values
        VMPerfDataUtil perfData = new VMPerfDataUtil();
        for (int i = 0; i < 5; i++) {
            String key = vmId + KEY_DELETEMETER + (firstMinutes + i);
            Map<String, Object> dataMap = vmPerfMap.remove(key);
            perfData.addPerfData(dataMap);
        }
        
        return perfData.getAvgValuesData();
    }

    /**
     * clean cache and return averages for all vm
     * @param avgs
     * @return
     */
    public List<Map<String, Object>> clean(List<Map<String, Object>> avgs) {
        for (String vm : vmTimestampsInMinutes.keySet()) {
            Map<String, Object> avgData = getFiveMinuteAvgData(vm, true);
            avgs.add(avgData);
        }
        if (vmTimestampsInMinutes.size() != 0) {
            avgs = clean(avgs);
        }
        return avgs;
    }

    /**
     * if datacache is idle for timeDuration, clean cache and return averages for all vm
     * @param timeDuration
     * @return
     */
    public List<Map<String, Object>> cleanIfIdle(long timeDuration) {
    	System.out.println("!!cleanIfIdle " + lastUpdateTime + "," + timeDuration + "," + System.currentTimeMillis());
        List<Map<String, Object>> avgs = new ArrayList<Map<String, Object>>();
        if ((lastUpdateTime + timeDuration) < System.currentTimeMillis()) {
            avgs = clean(avgs);
        }
        return avgs;
    }
    
    public String getStats() {
        return "processed avg data count=" + processedData + ",remained vm count=" + vmTimestampsInMinutes.size() + ",remained perfData count=" + vmPerfMap.size() ;
    }
    
}
