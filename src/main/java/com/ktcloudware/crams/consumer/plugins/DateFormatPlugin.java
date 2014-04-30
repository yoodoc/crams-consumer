package com.ktcloudware.crams.consumer.plugins;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateFormatPlugin implements CramsConsumerPlugin {
    String dateFieldName;
    private SimpleDateFormat originDateFormat;
    private SimpleDateFormat newDateFormat;
    private String properties;

    public DateFormatPlugin() {
        this.dateFieldName = "datetime";
        this.originDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.newDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ");
    }

    /*
     * public DateFormatPlugin(String dateFieldName, String originDateFormatStr,
     * String newDateFormatStr){ this.dateFieldName = "datetime";
     * this.originDateFormat = new SimpleDateFormat(originDateFormatStr);
     * this.newDateFormat = new SimpleDateFormat(newDateFormatStr); }
     */

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) throws CramsPluginException {
        if (dataMap == null || dataMap.isEmpty()) {
            return dataMap;
        }
        Map<String, Object> newDataMap = new HashMap<String, Object>(dataMap);
        try {
            String datetime = (String) newDataMap.get(this.dateFieldName);
            Date date = originDateFormat.parse(datetime);
            String newDateTime = newDateFormat.format(date);
            newDataMap.put(dateFieldName, newDateTime);
            return newDataMap;
        } catch (Exception e) {
            throw new CramsPluginException(e);
        }
    }

    @Override
    /**
     * pluginProperties : "dateFieldName,originDateFormatStr,newDateFormatStr"
     * 
     */
    public void setProperties(String pluginProperties) {
        this.properties = pluginProperties;
        String[] propertyArray = pluginProperties.split(",");
        this.dateFieldName = propertyArray[0];
        this.originDateFormat = new SimpleDateFormat(propertyArray[1]);
        this.newDateFormat = new SimpleDateFormat(propertyArray[2]);

    }

    @Override
    public boolean needProperties() {
        return true;
    }

    @Override
    public String getProperties() {
        return properties;
    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub
        
    }

}
