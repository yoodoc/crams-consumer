package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.dataType.ESConfig;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class ESIndexingPlugin implements CramsConsumerPlugin{
	private ESBulkIndexer esBulkIndexer;
	private ESConfig esConfig;
	
	private static final String ES_PROPERTIES_PATH = "esIndexer.properties";
	Logger logger;

	public ESIndexingPlugin(){
		logger = LogManager.getLogger("PLUGINS");
		
		// read ES client configuration
		esConfig = IndexerOptionParser.parseESProperties(ES_PROPERTIES_PATH);
		
		logger.info("load es indexer properites : " + esConfig.toString());
				
		// create ESBulkIndexer instance
		esBulkIndexer = new ESBulkIndexer(esConfig.esAddressList,
				esConfig.clusterName, esConfig.type, esConfig.routingKey,
				esConfig.settings, esConfig.mappings);
	}

	public ESIndexingPlugin(ESBulkIndexer esBulkIndexer){
		this.esBulkIndexer = esBulkIndexer;
	}
	
	@Override
	public void setProperties(String pluginProperties){
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProperties(){
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * basically, add single data to bulk request.
	 * but if it has enough number of data, send bulkRequest to ES  
	 * enough number of data determined by argument of ESBulkIndexer constructor 
	 * @param index
	 * @param data
	 * @return
	 */
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap, String dataTag){
		long startTime = System.currentTimeMillis();
		String index = null;
		if(dataMap != null && !dataMap.isEmpty()){
			index = parseIndexField(dataMap);
			if(index != null && !index.isEmpty()){
				esBulkIndexer.addRequestData(index, dataMap, dataTag);
				logger.trace("append bulk request data " + dataMap);
			} else {
				logger.error("fail to add bulk request data " + dataMap);
				return null;
			}
		}
	
		long currentTime = System.currentTimeMillis();
		if(esBulkIndexer.getSizeOfBulkRequest() >= esConfig.bulkRequestSize
				|| ((esBulkIndexer.getSizeOfBulkRequest() != 0) && (currentTime - esBulkIndexer
						.getLastSendTime()) > esConfig.maxRequestIntervalSec * 1000)){
			// insert data To ElasticSearch
			int count = esBulkIndexer.sendBulkRequest();
			logger.info("send bulk " + index + " request : " + count);
			
			if(count == 0){
				esBulkIndexer.initESClient();
			}
		}

		long endTime = System.currentTimeMillis();
		logger.trace("plugin excution time : " + (endTime - startTime) + "msec");
		
		return null;
	}

	@Override
	public boolean needProperties(){
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * "cdp-<분단위 까지의 시간>" 형식의 index name을 생성한다. 
	 * @param dataMap
	 * @return
	 */
	private String parseIndexField(Map<String, Object> dataMap){
		String DATETIME = "datetime";

		if(dataMap == null || dataMap.isEmpty()){
			return null;
		}
		String date = (String) dataMap.get(DATETIME);
		if(date == null)
			return null;
		// TODO fix parsing index name from datetime field
		return "cdp-" + date.split("T")[0];
	}
}
