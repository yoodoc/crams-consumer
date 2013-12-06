/**
 * @author yoodoc@gmail.com
 */

package com.ktcloudware.crams.consumer.clients;

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
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class ESBulkIndexer {
	private Client client = null;
	private BulkRequestBuilder bulkRequestBuilder;
	private long lastSendTime;
	private String typeInIndex;
	private String indexSettings;
	private String indexMappings;
	private String routingKeyName;
	private List<String> indexListToCheckExistance = null;

	private Logger logger = LogManager.getLogger("ESCLIENT");
	private String clusterName;
	private List<InetSocketTransportAddress> esAddressList;

	/**
	 * 
	 * @param esAddressList
	 * @param clusterName
	 * @param type
	 * @param routingKeyName
	 * @param indexSettings
	 * @param indexMappings
	 */
	public ESBulkIndexer(List<InetSocketTransportAddress> esAddressList,
			String clusterName, String type, String routingKeyName,
			String indexSettings, String indexMappings){
		logger = LogManager.getLogger("ESCLIENT");

		this.routingKeyName = routingKeyName;
		this.typeInIndex = type;
		this.indexSettings = indexSettings;
		this.indexMappings = indexMappings;
		this.clusterName = clusterName;
		this.esAddressList = esAddressList;
		lastSendTime = System.currentTimeMillis();
		initESClient();
	
	}

	public void initESClient(){
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("cluster.name", clusterName).build();
		TransportClient transportClient = new TransportClient(settings);
		for(InetSocketTransportAddress address : esAddressList){
			// FIXME 일부 es master 접속 불가시에 가용한 es master에만 접속하도록 하는 부분이며, 수정이
			// 필요하다.
			System.out.println("!!!!! init es");

			try{
				this.client = transportClient.addTransportAddress(address);
			}catch(Exception e){
				logger.error("ES connection error. failed to connect to "
						+ address.toString());
				e.printStackTrace();
				System.out.println("!!!!!!!!!! init es");

			}
		}

		this.indexListToCheckExistance = new ArrayList<String>();
		this.bulkRequestBuilder = client.prepareBulk();
	}

	/**
	 * 
	 * @param index
	 * @param data
	 */
	public boolean addRequestData(String index, Map<String, Object> data,
			String dataTag){
		appendBulkRequestBuilder(index, typeInIndex, data, dataTag);
		return true;
	}

	/**
	 * append data of BulkRequestBuilder
	 * 
	 * @param index
	 * @param typeName
	 * @param data
	 */
	private void appendBulkRequestBuilder(String index, String typeName,
			Map<String, Object> data, String dataTag){
		// create new index if does not exist.
		try{
			boolean result = createIndexAndMappingsIfNeeded(index,
					indexSettings, indexMappings);
			if(result == true){
				logger.info("create new index=" + index + " with mapping="
						+ indexMappings + ", dataTag=" + dataTag);
			}
		}catch(Exception e1){
			logger.error(e1.getMessage());
			e1.printStackTrace();
			return;
		}

		// set routing key for single index request, then add it to
		// bulkRequestBuilder
		IndexRequestBuilder source = null;
		try{
			Object routingKey = data.get(routingKeyName);
			if(routingKey == null){
				logger.warn("can't find routing key");
				return;
			} else if(routingKey instanceof String){
				routingKey = (String) data.get(routingKeyName);
			} else {
				routingKey = String.valueOf(data.get(routingKeyName));
			}

			if(routingKey != null && !((String) routingKey).isEmpty()){
				source = client.prepareIndex(index, typeName).setSource(data)
						.setRouting((String) routingKey);
				this.bulkRequestBuilder.add(source);
			} else {
				// routingKey에 해당하는 필드가 json 메시지에 포함되지 않는 경우에 해당 메시지는 전달되지 않는다.
				logger.warn("Missing field to generate routing key. data:"
						+ data);
			}
		}catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * send request using current bulkRequestbuilder, that has multiple index
	 * data
	 * 
	 * @return num of sended data
	 */
	public int sendBulkRequest(){
		// send bulk request using bulkRequestBulker
		int sizeOfBulkRequest = this.bulkRequestBuilder.numberOfActions();
		try{
			BulkResponse bulkResponse = this.bulkRequestBuilder.execute()
					.actionGet();
			if(bulkResponse.hasFailures()){
				throw new Exception("bulk insert fail: "
						+ bulkResponse.buildFailureMessage() + ", "
						+ bulkResponse.getItems());
			} else {
				logger.info("ES_RESULT: send " + sizeOfBulkRequest
						+ "data, it took " + bulkResponse.getTookInMillis()
						+ "msec");
			}
		}catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();

			client.close();
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e1){
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			initESClient();
			this.bulkRequestBuilder = client.prepareBulk();
			return 0;
		}

		// update last bulk request time
		lastSendTime = System.currentTimeMillis();

		// flush bulk requests list
		// TODO bulkRequestBuilder를 새로 생성할 필요가 있을까? 확인이 필요하다.
		this.bulkRequestBuilder = client.prepareBulk();

		return sizeOfBulkRequest;
	}

	/**
	 * create index, index setting & mapping info, if needed
	 * 
	 * @param index
	 * @param settings
	 * @param mappings
	 * @return
	 * @throws Exception
	 */
	private boolean createIndexAndMappingsIfNeeded(String index,
			String settings, String mappings) throws Exception {
		if(indexListToCheckExistance.contains(index)){
			return false;
		}
		boolean indexResult = false;
		try{
			indexResult = createIndexIfNeeded(index, settings);
		}catch(Exception e){
			logger.error("index creation failed");
		}

		if(indexResult == false){
			logger.error("index creation failed");
			Thread.sleep(5000);
		}

		try{
			PushMappingIfNeeded(index, mappings);
		}catch(Exception e){
			logger.error("index creation failed");
		}

		indexListToCheckExistance.add(index);
		return true;
	}

	/**
	 * first, check index existance. if needed it create index & set index info
	 * 
	 * @param index
	 * @param settings
	 * @return
	 */
	public boolean createIndexIfNeeded(String index, String settings){
		try{
			if(!doesIndexExist(client, index)){
				CreateIndexRequestBuilder cirb = client.admin().indices()
						.prepareCreate(index);
				String source = settings;
				cirb.setSettings(source);
				CreateIndexResponse createIndexResponse = cirb.execute()
						.actionGet();
				if(!createIndexResponse.isAcknowledged()){
					throw new Exception(
							"ES_REQUEST_RESULT: create index failed. index name="
									+ index);
				}
				logger.trace("ES_REQUEST_RESULT: success to create index. name="
						+ index);
				return true;
			}
		}catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * delete index
	 * 
	 * @param indexName
	 * @param type
	 */
	public void deleteIndex(String indexName, String type){
		try{
			DeleteIndexResponse deleteResponse = client.admin().indices()
					.delete(new DeleteIndexRequest(indexName)).get();
			if(!deleteResponse.isAcknowledged())
				throw new Exception("Could not create index [" + indexName
						+ "].");
		}catch(Exception e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static boolean doesIndexExist(Client client, String index)
			throws Exception {

		return client.admin().indices().prepareExists(index).execute()
				.actionGet().isExists();
	}

	public void shutdown(){
		sendBulkRequest();
		bulkRequestBuilder = null;
		client.close();
		client = null;
	}

	public Client getClient(){
		return client;
	}

	public int getSizeOfBulkRequest(){
		return this.bulkRequestBuilder.numberOfActions();
	}

	public long getLastSendTime(){
		return this.lastSendTime;
	}

	public Map<String, Object> getMapping(String index, String type){
		try{
			ClusterState clusterState = client.admin().cluster().prepareState()
					.setFilterIndices(index).execute().actionGet().getState();
			IndexMetaData indexMetadata = clusterState.getMetaData().index(
					index);

			MappingMetaData mappingMeatadata = indexMetadata
					.mapping(typeInIndex);
			if(null == mappingMeatadata){
				return null;
			}
			return mappingMeatadata.getSourceAsMap();
		}catch(Exception e){
			logger.error("GET Mapping failed " + e.getMessage(), e);
			e.printStackTrace();
		}
		return null;
	}

	public boolean PushMappingIfNeeded(String index, String mappings){
		try{
			if(null != getMapping(index, typeInIndex)){
				return false;
			}

			PutMappingRequestBuilder pmrb = client.admin().indices()
					.preparePutMapping(index).setType(typeInIndex);
			pmrb.setSource(mappings);

			// Create mapping for type
			PutMappingResponse response = pmrb.execute().actionGet();
			if(!response.isAcknowledged()){
				logger.error("Could not define mapping for type [" + index
						+ "]/[" + typeInIndex + "].");
			} else {
				logger.info("ES_REQUEST_RESULT: push " + index + " mapping :"
						+ mappings);
			}
			return true;

		}catch(Exception e){
			logger.error("push mapping failed", e);
		}
		return false;

	}
}
