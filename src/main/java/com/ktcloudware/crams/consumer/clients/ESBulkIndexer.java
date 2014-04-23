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

import com.ktcloudware.crams.consumer.CramsException;

public class ESBulkIndexer {
    private Client client = null;
    private BulkRequestBuilder bulkRequestBuilder;
    private long lastSendTime;
    private String typeInIndex;
    private String indexSettings;
    private String indexMappings;
    private String routingKeyName;
    private List<String> indexListToCheckExistance = null;

    private Logger logger = LogManager.getLogger("ES_CLIENT");
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
     * @throws CramsException
     */
    public ESBulkIndexer(List<InetSocketTransportAddress> esAddressList,
            String clusterName, String type, String routingKeyName,
            String indexSettings, String indexMappings) throws CramsException {
        logger = LogManager.getLogger("ES_CLIENT");

        this.routingKeyName = routingKeyName;
        this.typeInIndex = type;
        this.indexSettings = indexSettings;
        this.indexMappings = indexMappings;
        this.clusterName = clusterName;
        this.esAddressList = esAddressList;
        lastSendTime = System.currentTimeMillis();
        initESClient();

    }

    public void initESClient() throws CramsException {
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", clusterName).build();
        TransportClient transportClient = null;
        try {
            transportClient = new TransportClient(settings);
        } catch (Exception e) {
            throw new CramsException(
                    "failed to create ES TransportClient instance", e);
        }
        for (InetSocketTransportAddress address : esAddressList) {
            // TODO 일부 es master 접속 불가시에 가용한 es master에만 접속하도록 하는 부분이며, 수정이
            // 필요하다.
            transportClient.addTransportAddress(address);
        }
        this.client = transportClient;

        this.indexListToCheckExistance = new ArrayList<String>();
        this.bulkRequestBuilder = client.prepareBulk();
    }

    /**
     * 
     * @param index
     * @param data
     */
    public boolean addRequestData(String index, Map<String, Object> data,
            String dataTag) {
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
            Map<String, Object> data, String dataTag) {
        // create new index if does not exist.
        boolean result;
        try {
            result = createIndexAndMappingsIfNeeded(index, indexSettings,
                    indexMappings);
            if (result) {
                logger.info("create new index=" + index + " with mapping="
                        + indexMappings + ", dataTag=" + dataTag);
            } 
        } catch (CramsException e) {
            logger.error("failed to create new index, index='" + index + "'");
            return;
        }

        // set routing key for single index request, then add it to
        // bulkRequestBuilder
        IndexRequestBuilder source = null;

        Object routingKey = data.get(routingKeyName);
        if (routingKey instanceof String && !((String) routingKey).isEmpty()) {
            routingKey = (String) data.get(routingKeyName);
        } else {
            logger.warn("can't find routing key," + routingKeyName +", data=" + data.toString());
        }

        source = client.prepareIndex(index, typeName).setSource(data)
                .setRouting((String) routingKey);
        this.bulkRequestBuilder.add(source);

    }

    /**
     * send request using current bulkRequestbuilder, that has multiple index
     * data
     * 
     * @return num of sended data
     * @throws CramsException
     */
    public int sendBulkRequest() {
        // send bulk request using bulkRequestBulker
        int sizeOfBulkRequest = this.bulkRequestBuilder.numberOfActions();
        try {
            BulkResponse bulkResponse = this.bulkRequestBuilder.execute()
                    .actionGet();
            if (bulkResponse.hasFailures()) {
                throw new CramsException("bulk insert fail: "
                        + bulkResponse.buildFailureMessage() + ", "
                        + bulkResponse.getItems());
            } 
            
            logger.info("ES_RESULT: send " + sizeOfBulkRequest
                        + "data, it took " + bulkResponse.getTookInMillis()
                        + "msec");
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // re-init client
            client.close();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                logger.error("sleep interrupted", e1);
            }
            try {
                initESClient();
            } catch (CramsException e1) {
                logger.error("ES client init error", e);
            }
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
     * @throws CramsException 
     */
    private boolean createIndexAndMappingsIfNeeded(String index,
            String settings, String mappings) throws CramsException {
        if (indexListToCheckExistance.contains(index)) {
            return false;
        }
        boolean indexResult = false;
        try {
            indexResult = createIndexIfNeeded(index, settings);
        } catch (Exception e) {
            logger.error("index creation failed", e);
            throw new CramsException("index creation failed", e);
        }

        if (!indexResult) {
            logger.info("index '" + index + "' already exist");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.warn("sleep interrupted", e);
            }
        } 

        try {
            pushMappingIfNeeded(index, mappings);
        } catch (Exception e) {
            logger.warn("index creation failed", e);
            return false;
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
     * @throws CramsException 
     */
    public boolean createIndexIfNeeded(String index, String settings) throws CramsException {
        boolean isExist = false;
        try {
            isExist = doesIndexExist(client, index);
            if(isExist) {
                return false;
            }
        } catch (Exception e) {
            String errmsg = "failed to check index existance";
            logger.error(errmsg, e);
            throw new CramsException(errmsg, e);
        }
       
        CreateIndexRequestBuilder cirb = client.admin().indices()
                .prepareCreate(index);
        String source = settings;
        cirb.setSettings(source);
        CreateIndexResponse createIndexResponse = cirb.execute()
                .actionGet();
        if (!createIndexResponse.isAcknowledged()) {
            String errmsg = "ES_REQUEST_RESULT: create index failed. index name="
                    + index;
            logger.error(errmsg);
            throw new CramsException(errmsg);
        } 
        
            logger.info("ES_REQUEST_RESULT: success to create index. name="
                    + index);
            return true;
    }       


    /**
     * delete index
     * 
     * @param indexName
     * @param type
     */
    public void deleteIndex(String indexName, String type) {
        try {
            DeleteIndexResponse deleteResponse = client.admin().indices()
                    .delete(new DeleteIndexRequest(indexName)).get();
            if (!deleteResponse.isAcknowledged()) {
                throw new CramsException("Could not create index [" + indexName
                        + "].");
            }
        } catch (Exception e) {
            logger.error(
                    "failed to delete index, " + indexName + ", "
                            + e.getMessage(), e);
        }
    }

    public static boolean doesIndexExist(Client client, String index) {
        return client.admin().indices().prepareExists(index).execute()
                .actionGet().isExists();
    }

    public void shutdown() throws CramsException {
        sendBulkRequest();
        bulkRequestBuilder = null;
        client.close();
        client = null;
    }

    public Client getClient() {
        return client;
    }

    public int getSizeOfBulkRequest() {
        return this.bulkRequestBuilder.numberOfActions();
    }

    public long getLastSendTime() {
        return this.lastSendTime;
    }

    public Map<String, Object> getMapping(String index, String type) {
        try {
            ClusterState clusterState = client.admin().cluster().prepareState()
                    .setFilterIndices(index).execute().actionGet().getState();
            IndexMetaData indexMetadata = clusterState.getMetaData().index(
                    index);

            MappingMetaData mappingMeatadata = indexMetadata
                    .mapping(typeInIndex);
            if (null == mappingMeatadata) {
                return null;
            }
            return mappingMeatadata.getSourceAsMap();
        } catch (Exception e) {
            logger.error("GET Mapping failed " + e.getMessage(), e);
        }
        return null;
    }

    public boolean pushMappingIfNeeded(String index, String mappings) {
        try {
            if (null != getMapping(index, typeInIndex)) {
                return false;
            }

            PutMappingRequestBuilder pmrb = client.admin().indices()
                    .preparePutMapping(index).setType(typeInIndex);
            pmrb.setSource(mappings);

            // Create mapping for type
            PutMappingResponse response = pmrb.execute().actionGet();
            if (!response.isAcknowledged()) {
                logger.error("Could not define mapping for type [" + index
                        + "]/[" + typeInIndex + "].");
            } else {
                logger.info("ES_REQUEST_RESULT: push " + index + " mapping :"
                        + mappings);
            }
            return true;

        } catch (Exception e) {
            logger.error("push mapping failed", e);
        }
        return false;

    }
}
