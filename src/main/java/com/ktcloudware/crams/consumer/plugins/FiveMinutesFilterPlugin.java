package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

/**
 * accept only data that has *3 or *8 minutes value in datetime field. format
 * that include "hh:mm:ss" is allowed.
 * 
 * @author yoodoc
 * 
 */
public class FiveMinutesFilterPlugin implements CramsConsumerPlugin {
    private String dateFieldName = "datetime";
    private int baseMinute = 3;

    public FiveMinutesFilterPlugin() {

    }

    @Override
    public void setProperties(String properties) {
        String[] propertiesArray = properties.split(",");
        this.dateFieldName = propertiesArray[0];
        this.baseMinute = new Integer((String) propertiesArray[1]);
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) throws CramsPluginException {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }
        try {
            String dateStr = (String) dataMap.get(dateFieldName);
            String minutesStr = dateStr.split(":")[1];
            if (minutesStr.endsWith(String.valueOf(baseMinute))
                    || minutesStr.endsWith(String.valueOf(baseMinute + 5))) {
                return dataMap;
            }
        } catch (Exception e) {
            throw new CramsPluginException(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getProperties() {
        return dateFieldName;
    }

    @Override
    public boolean needProperties() {
        return true;
    }
}
