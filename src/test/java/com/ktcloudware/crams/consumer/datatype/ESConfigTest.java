package com.ktcloudware.crams.consumer.datatype;

import static org.junit.Assert.*;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import com.ktcloudware.crams.consumer.datatype.ESConfig;

public class ESConfigTest {

    @Test
    public void testValidateConfigValue() throws ParseException {
        ESConfig esConfig = new ESConfig();
        esConfig.clusterName = "testCluster";
        esConfig.setESAddress("192.168.12.11:9092");
        esConfig.type = "testType";
        esConfig.indexSettingsFileName = "indexSettings.json";
        esConfig.mappingInfoFileName = "mappingInfo.json";
        esConfig.settings = "{}";
        esConfig.mappings = "{}";
        try {
            assertTrue(esConfig.validateConfigVals());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testMissingClusterName() throws ParseException {
        ESConfig esConfig = new ESConfig();
        esConfig.bulkRequestSize = 0;
        // esConfig.clusterName = "testCluster";
        esConfig.setESAddress("192.168.12.11:9092");
        esConfig.type = "testType";
        try {
            assertFalse(esConfig.validateConfigVals());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = ParseException.class)
    public void testWrongESAddressFormat() throws ParseException {
        ESConfig esConfig = new ESConfig();
        esConfig.bulkRequestSize = 0;
        esConfig.type = "testType";
        esConfig.setESAddress("192.168.12.11");
    }
}
