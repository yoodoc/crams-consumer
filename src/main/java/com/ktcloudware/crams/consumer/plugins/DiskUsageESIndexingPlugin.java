package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.datatype.ESConfig;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;


/**
 * 테스트를 위한 임의의 플러그인이다. cdp 프로젝트에서 diskusage topic 유형을 별도의 ES index로 저장하는 기능을
 * 가진다.
 * 
 * @author yoodoc
 * 
 */
public class DiskUsageESIndexingPlugin implements CramsConsumerPlugin {
    private ESBulkIndexer esBulkIndexer;
    private ESConfig esConfig;

    Logger logger;

    public DiskUsageESIndexingPlugin() throws CramsPluginException,
            CramsException {
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
        esBulkIndexer = new ESBulkIndexer(esConfig.esAddressList,
                esConfig.clusterName, esConfig.type, esConfig.routingKey,
                esConfig.settings, esConfig.mappings);
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
        String index = null;
        if (dataMap != null && !dataMap.isEmpty()) {
            index = parseIndexField(dataMap);
            if (index != null && !index.isEmpty()) {
                esBulkIndexer.addRequestData(index, dataMap, dataTag);
                logger.trace("append bulk request data " + dataMap);
            } else {
                logger.error("fail to add bulk request data " + dataMap);
                return null;
            }
        }

        long currentTime = System.currentTimeMillis();
        if (esBulkIndexer.getSizeOfBulkRequest() >= esConfig.bulkRequestSize
                || ((esBulkIndexer.getSizeOfBulkRequest() != 0) && (currentTime - esBulkIndexer
                        .getLastSendTime()) > esConfig.maxRequestIntervalSec * 1000)) {
            // insert data To ElasticSearch
            int count = 0;
            try {
                count = esBulkIndexer.sendBulkRequest();
            } catch (CramsException e) {
                logger.error("failed to send request to ES," + e.getMessage(),
                        e);
                throw new CramsPluginException("failed to send request to ES,"
                        + e.getMessage(), e);
            }
            logger.info("send bulk " + index + " request : " + count);
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
        return "disk_usage";
    }
}
