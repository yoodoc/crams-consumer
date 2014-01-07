package com.ktcloudware.crams.consumer.util;

import java.util.Map;

import com.ktcloudware.crams.consumer.plugins.CramsPluginException;

public class KafkaConsumerPluginUtil {
    private KafkaConsumerPluginUtil() {
    }

    public static synchronized Map<String, Object> addAverageValue(
            String sourceKeyRegex, String avgElementName,
            Map<String, Object> dataMap) throws CramsPluginException {

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
                    throw new CramsPluginException("!!failed", e);
                }
            }
        }

        int sourceCount = numOfFloat + numOfLong;
        if (sourceCount < 1) {
            throw new CramsPluginException("no matched values");
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
}
