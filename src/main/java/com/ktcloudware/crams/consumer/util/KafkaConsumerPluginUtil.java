package com.ktcloudware.crams.consumer.util;

import java.util.Map;

public class KafkaConsumerPluginUtil {

	private KafkaConsumerPluginUtil(){
		
	}
	
	public static synchronized Map<String, Object> addAverageValue(
			String sourceKeyRegex, String avgElementName,
			Map<String, Object> dataMap){
		long totalValueLong = 0;
		float totalValueFloat = 0;
		int sourceCount = 0;
		for(String keyName : dataMap.keySet()){
			if(keyName.matches(sourceKeyRegex)){
				Object object = dataMap.get(keyName);
				try{
					if(object instanceof Integer){
						totalValueLong += (Integer) object;
					} else if(object instanceof Long){
						totalValueLong += (Long) object;
					} else if(object instanceof Float){
						totalValueFloat += (Float) object;
					} else if(object instanceof Double){
						totalValueFloat += (Double) object;
					}

				}catch(Exception e){
					e.printStackTrace();
				}
				sourceCount++;
			}
		}

		if(sourceCount < 1){
			return null;
		}
		
		Object average;
		if(sourceCount < 1){
			return dataMap;
		} else if(totalValueLong != 0 && totalValueFloat == 0){
			average = new Long((long) (totalValueLong / sourceCount));
		} else if(totalValueLong == 0 && totalValueFloat != 0){
			average = new Float(totalValueFloat / sourceCount);
		} else if(totalValueFloat != 0 && totalValueFloat != 0){
			average = new Float((totalValueFloat + totalValueLong) / sourceCount);
		} else {
			average = 0;
		}
		
		dataMap.put(avgElementName, average);
			return dataMap;
	}
}
