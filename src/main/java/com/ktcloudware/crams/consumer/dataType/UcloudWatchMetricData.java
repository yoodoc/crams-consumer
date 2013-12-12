package com.ktcloudware.crams.consumer.dataType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ktcloudware.crams.consumer.plugins.CpuAvgPlugin;
import com.ktcloudware.crams.consumer.plugins.UcloudWatchPlugin;
import com.ktcloudware.crams.consumer.plugins.VbdReadWriteAvgPlugin;
import com.ktcloudware.crams.consumer.plugins.VifAvgPlugin;

public class UcloudWatchMetricData {
	// metric data
	public String metricName;
	public List<UcloudWatchDemension> demension;
	public String unit;
	public String value;
	public String timestamp;

	public UcloudWatchMetricData() {
		demension = new ArrayList<UcloudWatchDemension>();
	}

	public String getMetricData() {
		return null;
	}

	public Map<String, String> getMetricDataMap() {
		Map<String, String> metricData = new HashMap<String, String>();

		return metricData;
	}

	public String getRequestParameter(String prefix) {
		String requestParameter = null;
		String metricName = "&" + prefix + "MetricName=" + getUrlEncodedValue(this.metricName);
		String unit = "&" + prefix + "Unit=" + getUrlEncodedValue(this.unit);
		String dimension = getDemesionParameter(prefix);
		String value = "&" + prefix + "Value=" + getUrlEncodedValue(this.value);
		String timestamp = "&" + prefix + "Timestamp=" + getUrlEncodedValue(this.timestamp);

		requestParameter = metricName + unit + dimension + value + timestamp;
		return requestParameter;
	}

	private String getDemesionParameter(String prefix) {
		String demensionParameter = "";
		for (int i = 0; i < this.demension.size(); i++) {
			String demensionNameParameter = "&" + prefix + "Dimensions.member."
					+ String.valueOf(i+1) + ".Name=" + getUrlEncodedValue(demension.get(i).name);
			String demensionValueParameter = "&" + prefix
					+ "Dimensions.member." + String.valueOf(i+1) + ".Value="
					+ getUrlEncodedValue(demension.get(i).value);
			demensionParameter = demensionParameter + demensionNameParameter
					+ demensionValueParameter;
		}
		// TODO Auto-generated method stub
		return demensionParameter;
	}
	
	private String getUrlEncodedValue(String value){
		String encodedValue = "";
		try {
			encodedValue = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return encodedValue;
	}
}
