package com.ktcloudware.crams.consumer.datatype;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.ktcloudware.crams.consumer.CramsException;

public class UcloudWatchMetricData {
    // metric data
    public String metricName;
    public List<UcloudWatchDemension> demension;
    public String unit;
    public String value;
    public String timestamp;
    public String sampleCount;
    
    public UcloudWatchMetricData() {
        demension = new ArrayList<UcloudWatchDemension>();
    }

    /**
     * return &<prefix>key=<prefix>value&<prefix>key2=<prefix>value2... 
     * @param prefix
     * @return
     * @throws CramsException
     */
    public String getRequestParameter(String prefix) throws CramsException {
        String requestParameter = null;
        String metricName = null;
        String unit = null;
        String dimension = null;
        String value = null;
        String timestamp = null;
        String sampleCount = null;
        try {
            metricName = "&" + prefix + "MetricName="
                    + getUrlEncodedValue(this.metricName);
            unit = "&" + prefix + "Unit=" + getUrlEncodedValue(this.unit);
            dimension = getDemesionParameter(prefix);
            value = "&" + prefix + "Value=" + getUrlEncodedValue(this.value);
            timestamp = "&" + prefix + "Timestamp="
                    + getUrlEncodedValue(this.timestamp);
            sampleCount = "&" + prefix + "SampleCount=" + getUrlEncodedValue(this.sampleCount);
        } catch (UnsupportedEncodingException e) {
             throw new CramsException("failed to create request parameter, unsupportedEncodeing:", e);
        }

        requestParameter = dimension + metricName + unit + value + timestamp;
        return requestParameter;
    }

    private String getDemesionParameter(String prefix) throws UnsupportedEncodingException {
    	String demensionParameter = "";
        for (int i = 0; i < this.demension.size(); i++) {
            String demensionNameParameter = "&" + prefix + "Dimensions.member."
                        + String.valueOf(i + 1) + ".Name="
                        + getUrlEncodedValue(demension.get(i).name);
            String demensionValueParameter = "&" + prefix
                        + "Dimensions.member." + String.valueOf(i + 1) + ".Value="
                        + getUrlEncodedValue(demension.get(i).value);
          
            demensionParameter = demensionParameter + demensionNameParameter
                    + demensionValueParameter;
        }
        return demensionParameter;
    }

    private String getUrlEncodedValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, "UTF-8");
    }
}
