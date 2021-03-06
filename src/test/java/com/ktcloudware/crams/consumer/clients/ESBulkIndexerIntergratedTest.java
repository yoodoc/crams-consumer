package com.ktcloudware.crams.consumer.clients;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.ParseException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.datatype.ESConfig;
import com.ktcloudware.crams.consumer.util.FileUtil;

/**
 * integerated test so, redis, es, kafka servers are needed.
 * 
 * @author yoodoc
 * 
 */

@Ignore
public class ESBulkIndexerIntergratedTest {

    private ESConfig esConfig;

    @Before
    public void setup() {
        ESConfig esConfig = new ESConfig();
        try {
            esConfig.setESAddress("14.63.213.13:9300");
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        esConfig.clusterName = "cdp_dev_qa";
        esConfig.type = "vm";
        esConfig.routingKey = "owner";
        esConfig.indexKey = "datetime";
        try {
            esConfig.indexSettingsFileName = "indexSettings.json";
            esConfig.mappingInfoFileName = "mappingInfo.json";
            esConfig.settings = FileUtil.readJsonToString(esConfig.indexSettingsFileName);
            esConfig.mappings = FileUtil.readJsonToString(esConfig.mappingInfoFileName);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail();
        }
        this.esConfig = esConfig;
    }

    @Test
    public void testEsConfigValidator() {
        try {
            assertEquals(true, esConfig.validateConfigVals());
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail();
        }
        
    }
    
    @Test
    public void nullRoutingKey(){
        String owner = "yoodoc";

        // create ESBulkIndexer instance

        ESBulkIndexer esBulkIndexer = null;
        try {
            esBulkIndexer = new ESBulkIndexer(esConfig.esAddressList,
                    esConfig.clusterName, esConfig.type, esConfig.routingKey,
                    esConfig.settings, esConfig.mappings);
        } catch (CramsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        String index = null;
        try {
            // create index & mapping info, then indexing
            Map<String, Object> rrdJson = FileUtil
                    .readJsonToMap("singleRrdData1.json");

            // set new test owner
            rrdJson.remove("owner");
       
            // index = parseIndexField(rrdJson);
            index = "yoodoctest";
            esBulkIndexer.addRequestData(index, rrdJson, "testdata");
            assertEquals(0, esBulkIndexer.sendBulkRequest());

            Thread.sleep(1000);

            // varify indexing data
            assertTrue(isSameShard(esBulkIndexer.getClient(), index,
                    esConfig.type, owner));
            Thread.sleep(1000);

            // delete index
            esBulkIndexer.deleteIndex(index, esConfig.type);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    }

    
    
    @Test
    public void testIndexingWithRoutingKey() {
        String owner = "yoodoc";

        // create ESBulkIndexer instance

        ESBulkIndexer esBulkIndexer = null;
        try {
            esBulkIndexer = new ESBulkIndexer(esConfig.esAddressList,
                    esConfig.clusterName, esConfig.type, esConfig.routingKey,
                    esConfig.settings, esConfig.mappings);
        } catch (CramsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        String index = null;
        try {
            // create index & mapping info, then indexing
            Map<String, Object> rrdJson = FileUtil
                    .readJsonToMap("singleRrdData1.json");

            // set new test owner
            rrdJson.remove("owner");
            rrdJson.put("owner", owner);

            // index = parseIndexField(rrdJson);
            index = "yoodoctest";
            esBulkIndexer.addRequestData(index, rrdJson, "testdata");
            esBulkIndexer.sendBulkRequest();

            Thread.sleep(1000);

            // varify indexing data
            assertTrue(isSameShard(esBulkIndexer.getClient(), index,
                    esConfig.type, owner));
            Thread.sleep(1000);

            // delete index
            esBulkIndexer.deleteIndex(index, esConfig.type);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    }

    private boolean isSameShard(Client client, String index, String type,
            String owner) {
        SearchResponse response = client.prepareSearch(index).setTypes(type)
                .setQuery(QueryBuilders.termQuery("owner", owner))
                .setExplain(true).execute().actionGet();
        Pattern shardPattern = Pattern
                .compile("\"_shard\"[\\s]*:[\\s]*([0-9]+),");
        Matcher matcher = shardPattern.matcher(response.toString());
        String lastGroup = null;
        while (matcher.find()) {
            String group = matcher.group(1);
            if (lastGroup == null) {
                lastGroup = group;
            } else if (!group.equals(lastGroup)) {
                return false;
            }
        }
        return true;
    }
}
