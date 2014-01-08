package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.datatype.ESConfig;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class ESIndexingPlugin implements CramsConsumerPlugin {
    private static final String DATETIME = "datetime";

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

        logger.info("load es indexer properites : " + esConfig.toString());

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
            throw new CramsPluginException("null data input," + dataMap
                    + dataTag);
        }
        
        String index = parseIndexField(dataMap);
        if (index == null || index.isEmpty()) {
            logger.error("fail to add bulk request data, " + dataMap);
            throw new CramsPluginException("fail to add bulk request data, "
                    + dataMap);
        }
       
        esBulkIndexer.addRequestData(index, dataMap, dataTag);
        logger.trace("append bulk request data " + dataMap);
       
        long currentTime = System.currentTimeMillis();
        if (esBulkIndexer.getSizeOfBulkRequest() >= esConfig.bulkRequestSize
                || ((esBulkIndexer.getSizeOfBulkRequest() != 0) && (currentTime - esBulkIndexer
                        .getLastSendTime()) > esConfig.maxRequestIntervalSec * 1000)) {
            // insert data To ElasticSearch
            int count = 0;
            count = esBulkIndexer.sendBulkRequest();
            logger.info("send bulk " + index + " request : " + count);

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
        long endTime = System.currentTimeMillis();
        logger.trace("plugin excution time : " + (endTime - startTime) + "msec");
        return null;
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
    private String parseIndexField(Map<String, Object> dataMap) {
        if (dataMap == null || dataMap.isEmpty()) {
            return null;
        }
        String date = (String) dataMap.get(DATETIME);
        if (date == null) {
            return null;
        }
        // TODO fix parsing index name from datetime field
        return "cdp-" + date.split("T")[0];
    }
}
