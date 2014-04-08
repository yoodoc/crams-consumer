package com.ktcloudware.crams.consumer.util;

import java.util.HashMap;
import java.util.Map;

public class VMPerfDataUtil {
    int count = 0;
    Map<String, Object> avgDataMap = null;
    private static final String DATETIME_IN_MILLIS = "datetime_in_millis";
    
    Long memory_internal_free;
   
    Long vbd_a_read;
    Long vbd_a_write;
    Long vbd_b_read;
    Long vbd_b_write;
    Long vbd_c_read;
    Long vbd_c_write;
    Long vbd_d_read;
    Long vbd_d_write;
    Long vbd_e_read;
    Long vbd_e_write;
    Long vbd_f_read;
    Long vbd_f_write;
    Long vbd_g_read;
    Long vbd_g_write;
    Long vbd_h_read;
    Long vbd_h_write;
    
    Long vif_0_rx;
    Long vif_0_tx;
    Long vif_1_rx;
    Long vif_1_tx;
    Long vif_2_rx;
    Long vif_2_tx;
    Long vif_3_rx;
    Long vif_3_tx;


    Float cpu0 = null;
    Float cpu1 = null;
    Float cpu2 = null;
    Float cpu3 = null;
    Float cpu4 = null;
    Float cpu5 = null;
    Float cpu6 = null;
    Float cpu7 = null;
    Float cpu8 = null;
    Float cpu9 = null;
    Float cpu10 = null;
    Float cpu11 = null;
    Float cpu12 = null;
    Float cpu13 = null;
    Float cpu14 = null;
    Float cpu15 = null;
     
    /**
     * 
     * @param dataMap
     * @param datetimeInMinutes
     */
    public void addPerfData(Map<String, Object> dataMap){
        if (dataMap == null || dataMap.isEmpty()) {
            return;
        }
     
        count++;
        if(avgDataMap == null || avgDataMap.isEmpty()) {
            avgDataMap = new HashMap<String, Object>(dataMap);
            return;
        }
       
        for (String keyName: dataMap.keySet()) { 
            if (keyName.matches("cpu[0-9]+")) {
                Float cpuValue =  null;
                if (dataMap.get(keyName) instanceof Double) {
                    cpuValue = ((Double) dataMap.get(keyName)).floatValue();
                } else {
                    cpuValue = (Float) dataMap.get(keyName);                    
                }
                
                Float cpuValueTotal =  null;
                if (avgDataMap.get(keyName) instanceof Double) {
                    cpuValueTotal = ((Double) avgDataMap.get(keyName)).floatValue();
                } else {
                    cpuValueTotal = (Float) avgDataMap.get(keyName);                    
                }
                if (cpuValueTotal == null) { 
                    continue;
                }
                cpuValueTotal = cpuValueTotal * (count-1);
                cpuValueTotal += cpuValue;
               this.avgDataMap.put(keyName, new Float(cpuValueTotal/count));
            } else if (keyName.equals(DATETIME_IN_MILLIS) || keyName.matches("vbd_[a-z]_[read|write]") || keyName.matches("vif_[0-9]_[rx|tx]") || keyName.equals("memory_internal_free")) {
                Long value;
                if (dataMap.get(keyName) instanceof Integer) {
                    value = ((Integer) dataMap.get(keyName)).longValue();
                }
                if (dataMap.get(keyName) instanceof Long) {
                    value = (Long) dataMap.get(keyName);
                } else {
                    continue;
                }
                Long valueTotal = (Long) this.avgDataMap.get(keyName);
                if (valueTotal == null) { 
                    continue;
                }
                valueTotal = valueTotal * (count-1);
                valueTotal += value;
                this.avgDataMap.put(keyName, valueTotal/count);
            }
         }
        
       /* vbd_a_read =(Long)dataMap.get("vbd_a_read");
          vbd_b_read =(Long)dataMap.get("vbd_b_read");
          vbd_c_read =(Long)dataMap.get("vbd_c_read");
          vbd_d_read =(Long)dataMap.get("vbd_d_read");
          vbd_e_read =(Long)dataMap.get("vbd_e_read");
          vbd_f_read =(Long)dataMap.get("vbd_f_read");
          vbd_g_read =(Long)dataMap.get("vbd_g_read");
          vbd_h_read =(Long)dataMap.get("vbd_h_read");

          vbd_a_write =(Long)dataMap.get("vbd_a_write");
          vbd_b_write =(Long)dataMap.get("vbd_b_write");
          vbd_c_write =(Long)dataMap.get("vbd_c_write");
          vbd_d_write =(Long)dataMap.get("vbd_d_write");
          vbd_e_write =(Long)dataMap.get("vbd_e_write");
          vbd_f_write =(Long)dataMap.get("vbd_f_write");
          vbd_g_write =(Long)dataMap.get("vbd_g_write");
          vbd_h_write =(Long)dataMap.get("vbd_h_write");
        
          vif_0_rx =(Long)dataMap.get("vif_0_rx");
          vif_1_rx =(Long)dataMap.get("vif_1_rx");
          vif_2_rx =(Long)dataMap.get("vif_2_rx");
          vif_3_rx =(Long)dataMap.get("vif_3_rx");
          
          vif_0_tx =(Long)dataMap.get("vif_0_tx");
          vif_1_tx =(Long)dataMap.get("vif_1_tx");
          vif_2_tx =(Long)dataMap.get("vif_2_tx");
          vif_3_tx =(Long)dataMap.get("vif_3_tx");
        
        cpu0 =+ (Float)dataMap.get("cpu0");
        cpu1 =+ (Float)dataMap.get("cpu1");
        cpu2 =+ (Float)dataMap.get("cpu2");
        cpu3 =+ (Float)dataMap.get("cpu3");
        cpu4 =+ (Float)dataMap.get("cpu4");
        cpu5 =+ (Float)dataMap.get("cpu5");
        cpu6 =+ (Float)dataMap.get("cpu6");
        cpu7 =+ (Float)dataMap.get("cpu7");
        cpu8 =+ (Float)dataMap.get("cpu8");
        cpu9 =+ (Float)dataMap.get("cpu9");
        cpu10 =+ (Float)dataMap.get("cpu10");
        cpu11 =+ (Float)dataMap.get("cpu11");
        cpu12 =+ (Float)dataMap.get("cpu12");
        cpu13 =+ (Float)dataMap.get("cpu13");
        cpu14 =+ (Float)dataMap.get("cpu14");
        cpu15 =+ (Float)dataMap.get("cpu15");*/
    }
    
    public Map<String, Object> getAvgValuesData(){
        
        if (avgDataMap == null || avgDataMap.isEmpty()) {
            return null;
        }
        Map<String, Object> avgFloatValuesData = new HashMap<String, Object>();
        for(String key: avgDataMap.keySet()) {
            Object value = avgDataMap.get(key);
            if (value instanceof Double) {
                avgFloatValuesData.put(key, ((Double)value).floatValue());
            } else {
                avgFloatValuesData.put(key, value);
            }
        }
        avgDataMap = null;
        return avgFloatValuesData;
    }
}
