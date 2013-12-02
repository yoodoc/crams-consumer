/**
 * @author yoodoc@gmail.com
 */

package com.ktcloudware.crams.indexer.clients;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.ktcloudware.crams.indexer.dataType.ESConfig;

public class ESBulkIndexer {
	private Client client = null;
	private BulkRequestBuilder bulkRequestBuilder;
	private long lastSendTime;
	private List<String> indexListToCheckExistance = null;
	private String typeInIndex;
	public int maxSizeOfbulkRequest;
	private int maxIntervalInSecondsForBulkRequest;
	private String indexSettings;
	private String indexMappings;
	private String routingKeyName;

	private static Logger logger = LogManager.getLogger("INDEXER");

	/**
	 * 
	 * @param esConfig
	 */
	public ESBulkIndexer(ESConfig esConfig) {
		routingKeyName = esConfig.routingKey;
		indexListToCheckExistance = new ArrayList<String>();
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", esConfig.clusterName).build();
		TransportClient transportClient = new TransportClient(settings);
		for (InetSocketTransportAddress address : esConfig.esAddressList) {
			// FIXME 일부 es master 접속 불가시에 가용한 es master에만 접속하도록 하는 부분이며, 수정이
			// 필요하다.
			try {
				this.client = transportClient.addTransportAddress(address);
			} catch (Exception e) {
				logger.error("ES connection error. failed to connect to "
						+ address.toString());
				logger.error(e.getMessage());
			}
		}
		this.bulkRequestBuilder = client.prepareBulk();
		this.typeInIndex = esConfig.type;
		this.maxSizeOfbulkRequest = esConfig.bulkRequestSize;
		this.maxIntervalInSecondsForBulkRequest = esConfig.maxRequestIntervalSec;
		this.indexSettings = esConfig.settings;
		this.indexMappings = esConfig.mappings;
		lastSendTime = System.currentTimeMillis();
		/*
		 * this.setRoutingKeyPattern(Pattern.compile("\"" + esConfig.routingKey
		 * + "\"[\\s]*:[\\s]*\"([^\"]+)\""));
		 */
	}

	/**
	 * 
	 * @param index
	 * @param data
	 * @return
	 */
	public synchronized boolean excute(String index, Map<String, Object> data) {
		appendBulkRequestBuilder(index, typeInIndex, data);
		bulkRequestBuilder.numberOfActions();
		long currentTime = System.currentTimeMillis();
		logger.trace(bulkRequestBuilder.numberOfActions() + " index requests are wait.");
		
		if (bulkRequestBuilder.numberOfActions() >= maxSizeOfbulkRequest
				|| ((bulkRequestBuilder.numberOfActions() != 0) && (currentTime - lastSendTime) > maxIntervalInSecondsForBulkRequest * 1000)) {
			// insert data To ElasticSearch
			int numOfSendData = sendBulkRequest();
			
			// flush bulk requests list
			//TODO bulkRequestBuilder를 새로 생성할 필요가 있을까? 확인이 필요하다.
			this.bulkRequestBuilder = client.prepareBulk();
			logger.trace("bulk index request success. size="
					+ numOfSendData);
		}
		return true;
	}

	/**
	 * append data of BulkRequestBuilder
	 * @param index
	 * @param typeName
	 * @param data
	 */
	private void appendBulkRequestBuilder(String index, String typeName,
			Map<String, Object> data) {
		if (!indexListToCheckExistance.contains(index)) {
			indexListToCheckExistance.add(index);
		}

		IndexRequestBuilder source = null;
		
		try {
			Object routingKey = data.get(routingKeyName);
			if (routingKey == null) {
				logger.error("can't find routing key");
				return;
			} else if (routingKey instanceof String) {
				routingKey = (String) data.get(routingKeyName);
			} else {
				routingKey = String.valueOf(data.get(routingKeyName));
			}

			if (routingKey != null && !((String) routingKey).isEmpty()) {
				source = client.prepareIndex(index, typeName).setSource(data)
						.setRouting((String) routingKey);
				this.bulkRequestBuilder.add(source);
			} else {
				// TODO routingKey에 해당하는 필드가 json 메시지에 포함되지 않는 경우에 대한 에러 처리를
				// 해야한다.
				logger.error("Missing field to generate routing key.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * send request using current bulkRequestbuilder, that has multiple index data
	 * @return
	 */
	private int sendBulkRequest() {
		try {
			for (String index : indexListToCheckExistance) {
				createIndexAndMappingsIfNeeded(index, indexSettings,
						indexMappings);
			}
			indexListToCheckExistance = new ArrayList<String>();
		} catch (Exception e1) {
			logger.error(e1.getMessage());
			return 0;
		}

		int sizeOfBulkRequest = this.bulkRequestBuilder.numberOfActions();
		try {
			BulkResponse bulkResponse = this.bulkRequestBuilder.execute()
					.actionGet();
			if (bulkResponse.hasFailures()) {
				throw new Exception("bulk insert fail: "
						+ bulkResponse.buildFailureMessage());
			} else {
				logger.trace("ES_REQUEST_RESULT: send " + sizeOfBulkRequest
						+ "data, it took " + bulkResponse.getTookInMillis()
						+ "msec");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			return 0;
		}
		lastSendTime = System.currentTimeMillis();
		return sizeOfBulkRequest;
	}

	/**
	 * create index, index setting & mapping info, if needed
	 * @param index
	 * @param settings
	 * @param mappings
	 * @return
	 * @throws Exception
	 */
	public boolean createIndexAndMappingsIfNeeded(String index,
			String settings, String mappings) throws Exception {
		if (createIndexIfNeeded(index, settings)) {
			return pushMapping(index, mappings);
		}
		return false;
	}

	/**
	 * first, check index existance.
	 * if needed it create index & set index info
	 * @param index
	 * @param settings
	 * @return
	 */
	public boolean createIndexIfNeeded(String index, String settings) {
		try {
			if (!isIndexExist(client, index)) {
				CreateIndexRequestBuilder cirb = client.admin().indices()
						.prepareCreate(index);
				String source = settings;
				cirb.setSettings(source);
				CreateIndexResponse createIndexResponse = cirb.execute()
						.actionGet();
				if (!createIndexResponse.isAcknowledged()) {
					throw new Exception("create index failed. index name="
							+ index);
				}
				logger.trace("success to create index. name=" + index);
				return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}

	/**
	 * delete index
	 * @param indexName
	 * @param type
	 */
	public void deleteIndex(String indexName, String type) {
		try {
			DeleteIndexResponse deleteResponse = client.admin().indices()
					.delete(new DeleteIndexRequest(indexName)).get();
			if (!deleteResponse.isAcknowledged())
				throw new Exception("Could not create index [" + indexName
						+ "].");
		} catch (Exception e) {
			logger.error(e);// TODO Auto-generated catch block
		}
	}

	public static boolean isIndexExist(Client client, String index)
			throws Exception {

		return client.admin().indices().prepareExists(index).execute()
				.actionGet().isExists();
	}

	/**
	 * create index mapping info
	 * 
	 * @param index
	 * @param mappings
	 * @throws Exception
	 */
	public boolean pushMapping(String index, String mappings) throws Exception {
		PutMappingRequestBuilder pmrb = client.admin().indices()
				.preparePutMapping(index).setType(typeInIndex);
		pmrb.setSource(mappings);

		// Create type and mapping
		PutMappingResponse response = pmrb.execute().actionGet();
		if (!response.isAcknowledged()) {
			logger.error("Could not define mapping for type [" + index + "]/["
					+ typeInIndex + "].");
			return false;
		}
		logger.trace("ES_REQUEST_RESULT: create new Index, " + index);
		return true;
	}

	public void shutdown() {
		sendBulkRequest();
		bulkRequestBuilder = null;
		client.close();
		client = null;
	}

	public Client getClient() {
		return client;
	}
}
