package com.ktcloudware.crams.consumer.util;

import java.util.Map;

import com.ktcloudware.crams.consumer.CramsException;

public class KafkaConsumerPluginUtil {
    private KafkaConsumerPluginUtil() {
    }

    /**
     * return Long type or Float type
     * @param sourceKeyRegex
     * @param avgElementName
     * @param dataMap
     * @return
     * @throws CramsException
     */
    public static synchronized Map<String, Object> addAverageValue(
            String sourceKeyRegex, String avgElementName,
            Map<String, Object> dataMap) throws CramsException {

        long totalValueLong = 0L;
        float totalValueFloat = (float) 0;
        int numOfFloat = 0;
        int numOfLong = 0;
        for (String keyName : dataMap.keySet()) {
            if (keyName.matches(sourceKeyRegex)) {
                Object object = dataMap.get(keyName);
                try {
                    if (object instanceof Integer || object instanceof Long) {
                        totalValueLong += Long
                                .parseLong(String.valueOf(object));
                        numOfLong++;
                    } else if (object instanceof Float
                            || object instanceof Double) {
                        totalValueFloat += Float.parseFloat(String
                                .valueOf(object));
                        ;
                        numOfFloat++;
                    }

                } catch (Exception e) {
                    throw new CramsException("!!failed", e);
                }
            }
        }

        int sourceCount = numOfFloat + numOfLong;
        if (sourceCount < 1) {
            throw new CramsException("no matched values");
        }

        Object average;

        if (sourceCount < 1) {
            return dataMap;
        } else if (totalValueLong != 0 && totalValueFloat == 0) {
            average = new Long((long) (totalValueLong / sourceCount));
        } else if (totalValueLong == 0 && totalValueFloat != 0) {
            average = new Float(totalValueFloat / sourceCount);
        } else if (totalValueFloat != 0 && totalValueFloat != 0) {
            average = new Float((totalValueFloat + totalValueLong)
                    / sourceCount);
        } else if (numOfFloat > 0) {
            average = (float) 0;
        } else {
            average = (long) 0;
        }

        dataMap.put(avgElementName, average);
        return dataMap;
    }
    
    public static synchronized Map<String, Object> addFloatTypeAverageValue(
            String sourceKeyRegex, String avgElementName,
            Map<String, Object> dataMap) throws CramsException {

        float totalValue = (float) 0;
        int count = 0;
        for (String keyName : dataMap.keySet()) {
            if (keyName.matches(sourceKeyRegex)) {
                Object object = dataMap.get(keyName);
                try {
                    if (object instanceof Integer || object instanceof Long) {
                        totalValue += Float.parseFloat(String.valueOf(object));
                    } else if (object instanceof Float
                            || object instanceof Double) {
                        totalValue += Float.parseFloat(String.valueOf(object));
                    } else {
                        continue;
                    }
                    count++;
                } catch (Exception e) {
                    throw new CramsException("!!failed", e);
                }
            }
        }

        if (count < 1) {
            throw new CramsException("no matched values");
        }

        Float average = new Float(totalValue / count);     
        dataMap.put(avgElementName, average);
        return dataMap;
    }
    
    public static synchronized Map<String, Object> addLongTypeAverageValue(
            String sourceKeyRegex, String avgElementName,
            Map<String, Object> dataMap) throws CramsException {

        long totalValue = 0L;
        int count = 0;
        for (String keyName : dataMap.keySet()) {
            if (keyName.matches(sourceKeyRegex)) {
                Object object = dataMap.get(keyName);
                try {
                if (object instanceof Integer || object instanceof Long) {
                    totalValue += Long
                            .parseLong(String.valueOf(object));
                } else if (object instanceof Float) {
                    totalValue = ((Float) object).longValue();
                } else if (object instanceof Double) {
                    totalValue = ((Double) object).longValue();
                } else {
                    continue;
                }
                count++;
                } catch (Exception e) {
                    throw new CramsException("failed with " + e.getMessage() , e);
                }
            }
        }

        if (count < 1) {
            throw new CramsException("no matched values");
        }

        long average = new Long(totalValue / count);     
        dataMap.put(avgElementName, average);
        return dataMap;
    }
}
