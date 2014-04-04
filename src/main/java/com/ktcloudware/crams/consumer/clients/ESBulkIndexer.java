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
<<<<<<< HEAD
     * @throws CramsException
=======
     * @throws CramsException 
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
     */
    public ESBulkIndexer(List<InetSocketTransportAddress> esAddressList,
            String clusterName, String type, String routingKeyName,
            String indexSettings, String indexMappings) throws CramsException {
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

<<<<<<< HEAD
    public void initESClient() throws CramsException {
=======
    public void initESClient() throws CramsException{
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        Settings settings = ImmutableSettings.settingsBuilder()
                .put("cluster.name", clusterName).build();
        TransportClient transportClient = null;
        try {
            transportClient = new TransportClient(settings);
        } catch (Exception e) {
<<<<<<< HEAD
            throw new CramsException(
                    "failed to create ES TransportClient instance", e);
        }
        for (InetSocketTransportAddress address : esAddressList) {
            // TODO 일부 es master 접속 불가시에 가용한 es master에만 접속하도록 하는 부분이며, 수정이
            // 필요하다.
            transportClient.addTransportAddress(address);
=======
            throw new CramsException("failed to create ES TransportClient instance", e);
        }
        for (InetSocketTransportAddress address : esAddressList) {
            // FIXME 일부 es master 접속 불가시에 가용한 es master에만 접속하도록 하는 부분이며, 수정이
            // 필요하다.
            System.out.println("!!!!! init es" + address.toString());

            try {
                transportClient.addTransportAddress(address);
            } catch (Exception e) {
                logger.error("ES connection error. failed to connect to "
                        + address.toString(), e);
            }
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
<<<<<<< HEAD

        boolean result = createIndexAndMappingsIfNeeded(index, indexSettings,
                indexMappings);
        if (result) {
            logger.info("create new index=" + index + " with mapping="
                    + indexMappings + ", dataTag=" + dataTag);
=======
        try {
            boolean result = createIndexAndMappingsIfNeeded(index,
                    indexSettings, indexMappings);
            if (result == true) {
                logger.info("create new index=" + index + " with mapping="
                        + indexMappings + ", dataTag=" + dataTag);
            }
        } catch (Exception e1) {
            logger.error("failed to create index ES request, " + index + ", "
                    + e1.getMessage(), e1);
            return;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        }

        // set routing key for single index request, then add it to
        // bulkRequestBuilder
        IndexRequestBuilder source = null;
<<<<<<< HEAD

        Object routingKey = data.get(routingKeyName);
        if (routingKey instanceof String && ((String) routingKey).isEmpty()) {
            routingKey = (String) data.get(routingKeyName);
        } else {
            logger.warn("can't find routing key");
        }

        source = client.prepareIndex(index, typeName).setSource(data)
                .setRouting((String) routingKey);
        this.bulkRequestBuilder.add(source);

=======
        try {
            Object routingKey = data.get(routingKeyName);
            if (routingKey == null) {
                logger.warn("can't find routing key");
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
                // routingKey에 해당하는 필드가 json 메시지에 포함되지 않는 경우에 해당 메시지는 전달되지 않는다.
                logger.warn("Missing field to generate routing key. data:"
                        + data);
            }
        } catch (Exception e) {
            logger.error("failed to append ES request, " + e.getMessage(), e);
        }
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    }

    /**
     * send request using current bulkRequestbuilder, that has multiple index
     * data
     * 
     * @return num of sended data
<<<<<<< HEAD
     * @throws CramsException
     */
    public int sendBulkRequest() {
=======
     * @throws CramsException 
     */
    public int sendBulkRequest() throws CramsException {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        // send bulk request using bulkRequestBulker
        int sizeOfBulkRequest = this.bulkRequestBuilder.numberOfActions();
        try {
            BulkResponse bulkResponse = this.bulkRequestBuilder.execute()
                    .actionGet();
            if (bulkResponse.hasFailures()) {
<<<<<<< HEAD
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
=======
                throw new Exception("bulk insert fail: "
                        + bulkResponse.buildFailureMessage() + ", "
                        + bulkResponse.getItems());
            } else {
                logger.info("ES_RESULT: send " + sizeOfBulkRequest
                        + "data, it took " + bulkResponse.getTookInMillis()
                        + "msec");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //re-init client
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            client.close();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                logger.error("sleep interrupted", e1);
            }
<<<<<<< HEAD
            try {
                initESClient();
            } catch (CramsException e1) {
                logger.error("ES client init error", e);
            }
=======
            initESClient();
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
     */
    private boolean createIndexAndMappingsIfNeeded(String index,
            String settings, String mappings) {
        if (indexListToCheckExistance.contains(index)) {
            return false;
        }
        boolean indexResult = false;
        try {
            indexResult = createIndexIfNeeded(index, settings);
        } catch (Exception e) {
            logger.error("index creation failed", e);
        }

<<<<<<< HEAD
        if (!indexResult) {
=======
        if (indexResult == false) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            logger.error("index creation failed");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                logger.error("sleep interrupted", e);
            }
        }

        try {
<<<<<<< HEAD
            pushMappingIfNeeded(index, mappings);
=======
            PushMappingIfNeeded(index, mappings);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        } catch (Exception e) {
            logger.error("index creation failed", e);
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
    public boolean createIndexIfNeeded(String index, String settings) {
        boolean isExist = false;
        try {
            isExist = doesIndexExist(client, index);
        } catch (Exception e) {
            logger.error("failed to check index existance", e);
            return false;
        }
        if (!isExist) {
            CreateIndexRequestBuilder cirb = client.admin().indices()
                    .prepareCreate(index);
            String source = settings;
            cirb.setSettings(source);
            CreateIndexResponse createIndexResponse = cirb.execute()
                    .actionGet();
            if (!createIndexResponse.isAcknowledged()) {
                logger.error("ES_REQUEST_RESULT: create index failed. index name="
                        + index);
            } else {
                logger.trace("ES_REQUEST_RESULT: success to create index. name="
                        + index);
                return true;
            }
        }
        return false;
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
<<<<<<< HEAD
            if (!deleteResponse.isAcknowledged()) {
                throw new CramsException("Could not create index [" + indexName
                        + "].");
            }
=======
            if (!deleteResponse.isAcknowledged())
                throw new Exception("Could not create index [" + indexName
                        + "].");
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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

<<<<<<< HEAD
    public boolean pushMappingIfNeeded(String index, String mappings) {
=======
    public boolean PushMappingIfNeeded(String index, String mappings) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
