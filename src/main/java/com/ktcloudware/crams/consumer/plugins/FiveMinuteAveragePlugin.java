package com.ktcloudware.crams.consumer.plugins;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import com.google.common.cache.*;

public class FiveMinuteAveragePlugin implements CramsConsumerPlugin{
    public static final String CPU_AVG = "cpu_avg";
    private Logger logger;
    private Map<String, Map<String, Object>> vmPerfMap = new HashMap<String, Map<String, Object>>();
    private Map<String, List<Long>> vmTimestampsInMinutes = new HashMap<String, List<Long>>();
    private Cache<String, Map<String, Object>> cache = null;
    
    private SimpleDateFormat originDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String KEY_DELETEMETER = "::Delimeter::";
    public static final String DATE_TIME_KEY = "date_time";
    public static final String VM_KEY = "vm_uuid";
    
    public FiveMinuteAveragePlugin() {
        logger = LogManager.getLogger("PLUGINS");
        cache  = CacheBuilder.newBuilder()
                .concurrencyLevel(4)
                .weakKeys()
                .maximumSize(10000)
                .expireAfterWrite(30, TimeUnit.MINUTES).build();
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) throws CramsPluginException {
        Map<String, Object> avgDataMap = new HashMap<String, Object>();
        String vmId = (String) dataMap.get(VM_KEY);
        if (vmId == null || vmId.isEmpty()) {
            return null;
        }
        String timestamp = (String) dataMap.get(DATE_TIME_KEY);
        if (timestamp == null) {
            return null;
        }
        try { 
            Date date = originDateFormat.parse(timestamp);
             avgDataMap = getAvgData(vmId,  TimeUnit.MILLISECONDS.toMinutes(date.getTime()), dataMap);
        } catch (ParseException e) {
            logger.warn("failed to parse date_time" + dataMap, e);
            throw new CramsPluginException(e);
        }
        
        cache.put(vmId , dataMap);
        
         return avgDataMap;
    }

    /**
     * addPerfData and get avgPerfData if available
     * @param vmId
     * @param timestampInMinutes
     * @param dataMap
     * @return dataMap has fiveMinutes avg values. return null it needs more data to calculate avg values
     */
    private synchronized Map<String, Object> getAvgData(String vmId, long timestampInMinutes, Map<String, Object> dataMap) {
        List<Long> timestamps = vmTimestampsInMinutes.get(vmId);
        if(timestamps == null) {
            timestamps = new ArrayList<Long>();
        }
        timestamps.add(timestampInMinutes);
        Map<String, Object> receivedData = new HashMap<String, Object>(dataMap);
        vmPerfMap.put(vmId+KEY_DELETEMETER+timestampInMinutes, receivedData);
        long firstMinutes = timestamps.get(0);
        long lastMinutes = timestamps.get(timestamps.size() - 1);
        int dataSizeInSingleFiveMinutes = 0;
        //정상적으로 5분 데이터가 수집된 경우 
        if (4 == (firstMinutes - firstMinutes)) {
            dataSizeInSingleFiveMinutes = 5;
            timestamps = new ArrayList<Long>();
        } 
        //5분 이상 데이터가 쌓이는 순간 이전 성능 값을 이용해 평균값을 계산한다. 
        else if (4 < (lastMinutes - firstMinutes)) {
            System.out.println("!!size " + timestamps.size());
            dataSizeInSingleFiveMinutes = timestamps.size() - 1;
            timestamps = timestamps.subList(timestamps.size() - 1, timestamps.size());
        }
        vmTimestampsInMinutes.put(vmId, timestamps);
     
        //calculate avg values
        VMPerfData perfData = new VMPerfData();
        for (int i = 0; i < dataSizeInSingleFiveMinutes; i++) {
            String key = vmId+KEY_DELETEMETER+(firstMinutes+i); 
            perfData.addPerfData(vmPerfMap.remove(key));
        }
        return perfData.getAvgValuesData();
    }



    @Override
    public void setProperties(String pluginProperties) {
    }

    @Override
    public boolean needProperties() {
        return false;
    }

    @Override
    public String getProperties() {
         return null;
    }
}
