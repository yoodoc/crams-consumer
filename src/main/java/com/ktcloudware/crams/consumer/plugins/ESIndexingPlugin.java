package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.datatype.ESConfig;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class ESIndexingPlugin implements CramsConsumerPlugin {
	private ESBulkIndexer esBulkIndexer;
	private ESConfig esConfig;
	Logger logger;

	public ESIndexingPlugin() throws CramsPluginException {
		logger = LogManager.getLogger("PLUGINS");

		// read ES client configuration
		try {
			esConfig = IndexerOptionParser.parseESProperties();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new CramsPluginException(e.getMessage(), e);
		}

		// create ESBulkIndexer instance
		try {
			esBulkIndexer = new ESBulkIndexer(esConfig.esAddressList,
					esConfig.clusterName, esConfig.type, esConfig.routingKey,
					esConfig.settings, esConfig.mappings);
		} catch (CramsException e) {
			logger.error("failed to initiate ESBulkIndexer," + e.getMessage(),
					e);
			throw new CramsPluginException("failed to initiate ESBulkIndexer,"
					+ e.getMessage(), e);
		}
	}

	public ESIndexingPlugin(ESBulkIndexer esBulkIndexer) {
		this.esBulkIndexer = esBulkIndexer;
	}

	@Override
	public void setProperties(String pluginProperties) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * basically, add single data to bulk request. but if it has enough number
	 * of data, send bulkRequest to ES enough number of data determined by
	 * argument of ESBulkIndexer constructor
	 * 
	 * @param index
	 * @param data
	 * @return
	 * @throws CramsPluginException
	 */
	@Override
	public Map<String, Object> excute(Map<String, Object> dataMap,
			String dataTag) throws CramsPluginException {
		long startTime = System.currentTimeMillis();
		if (dataMap == null || dataMap.isEmpty()) {
		    sendBulkRequest(startTime);
		    return null;
			//throw new CramsPluginException("null data input," + dataMap + dataTag);
		}
		
		String index = parseIndexField(dataMap, esConfig.indexKey);
		
		if (index == null || index.isEmpty()) {
			logger.error("missing indexKey'" + esConfig.indexKey + "', " + dataMap.toString());
			throw new CramsPluginException("fail to add bulk request data, "
					+ dataMap);
		}

		esBulkIndexer.addRequestData(index, dataMap, dataTag);
		//logger.trace("append bulk request data " + dataMap);
		long currentTime = System.currentTimeMillis();
		sendBulkRequest(currentTime);
		long endTime = System.currentTimeMillis();
		//logger.trace("plugin excution time : " + (endTime - startTime) + "msec");
		return null;
	}

    private void sendBulkRequest(long currentTime)
            throws CramsPluginException {
        if (esBulkIndexer.getSizeOfBulkRequest() >= esConfig.bulkRequestSize
				|| ((esBulkIndexer.getSizeOfBulkRequest() != 0) && (currentTime - esBulkIndexer
						.getLastSendTime()) > esConfig.maxRequestIntervalSec * 1000)) {
			// insert data To ElasticSearch
			int count = 0;
			int bufferedRequestSize = esBulkIndexer.getSizeOfBulkRequest();
			count = esBulkIndexer.sendBulkRequest();
			logger.info("send bulk request : " + count + ",buffered request count=" + bufferedRequestSize + "--> " + esBulkIndexer.getSizeOfBulkRequest());
			if (count == 0) {
				try {
					esBulkIndexer.initESClient();
				} catch (CramsException e) {
					logger.error("failed to init ES Client," + e.getMessage(),
							e);
					throw new CramsPluginException("failed to init ES Client,"
							+ e.getMessage(), e);
				}
			}
		}
    }

	@Override
	public boolean needProperties() {
		return false;
	}

	/**
	 * "cdp-<분단위 까지의 시간>" 형식의 index name을 생성한다.
	 * 
	 * @param dataMap
	 * @return
	 */
	private String parseIndexField(Map<String, Object> dataMap, String indexKey) {
		if (dataMap == null || dataMap.isEmpty()) {
			return null;
		}
		if ("datetime".equalsIgnoreCase(indexKey)) {
			String date = (String) dataMap.get(indexKey);
			if (date == null) {
				return null;
			}
			return "cdp-" + date.split("T")[0].split(" ")[0];
		} else if ("test".equalsIgnoreCase(indexKey)) {
			String date = (String) dataMap.get("datetime");
			if (date == null) {
				return null;
			}
			return "test-" + date.split("T")[0].split(" ")[0];
		} else if ("yd".equalsIgnoreCase(indexKey)) {
			String date = (String) dataMap.get("datetime");
			if (date == null) {
				return null;
			}
			return "yd-" + date.split("T")[0].split(" ")[0];
		} else {
			//unsupported key
			return null;
		}
	}
}
