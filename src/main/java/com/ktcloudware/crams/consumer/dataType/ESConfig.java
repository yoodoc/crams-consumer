package com.ktcloudware.crams.consumer.dataType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ESConfig {
	// public String esAddress = "";
	public String clusterName = "";
	public String indexKey = "";
	public String type = "";
	public String routingKey = "";
	public int bulkRequestSize = -1;
	public int maxRequestIntervalSec = -1;
	public List<InetSocketTransportAddress> esAddressList = new ArrayList<InetSocketTransportAddress>();
	public String settings;
	public String mappings;
	public String indexSettingsFileName;
	public String mappingInfoFileName;

	public ESConfig(){
		esAddressList = new ArrayList<InetSocketTransportAddress>();
	}

	public boolean validateConfigVals(){
		if(esAddressList.size() < 1 || clusterName.isEmpty() || type.isEmpty()
				|| indexSettingsFileName.isEmpty()
				|| mappingInfoFileName.isEmpty() || settings.isEmpty()
				|| mappings.isEmpty()){
			return false;
		}
		ObjectMapper mapper = new ObjectMapper();
		InputStream settingsIs = null;
		InputStream mappingsIs = null;
		try{
			settingsIs = new ByteArrayInputStream(settings.getBytes("UTF-8"));
			 mappingsIs = new ByteArrayInputStream(mappings.getBytes("UTF-8"));
		}catch(UnsupportedEncodingException e1){
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}
		
		try{
			mapper.readValue(settingsIs, Map.class);
			mapper.readValue(mappingsIs, Map.class);
		}catch(Exception e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
		return true;
	}

	public List<InetSocketTransportAddress> setESAddress(String esAddress)
			throws ParseException {
		String[] urls = esAddress.split(",");
		int port;

		for(String address : urls){
			String[] ipAndPort = address.split(":");
			if(ipAndPort.length == 2){
				port = Integer.valueOf(ipAndPort[1]);
				esAddressList.add(new InetSocketTransportAddress(ipAndPort[0],
						port));
			} else {
				throw new ParseException("wrong elasticsearch address format");
			}
		}
		return esAddressList;
	}
}
