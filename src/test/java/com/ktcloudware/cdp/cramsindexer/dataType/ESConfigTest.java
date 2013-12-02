package com.ktcloudware.cdp.cramsindexer.dataType;

import static org.junit.Assert.*;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import com.ktcloudware.crams.indexer.dataType.ESConfig;

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
		assertTrue(esConfig.validateConfigVals());
	}
	
	@Test
	public void testMissingClusterName() throws ParseException {
		ESConfig esConfig = new ESConfig();
		esConfig.bulkRequestSize = 0;
		//esConfig.clusterName = "testCluster";
		esConfig.setESAddress("192.168.12.11:9092");
		esConfig.type = "testType";
		assertFalse(esConfig.validateConfigVals());
	}

	@Test(expected = ParseException.class)
	public void testWrongESAddressFormat() throws ParseException {
		ESConfig esConfig = new ESConfig();
		esConfig.bulkRequestSize = 0;
		esConfig.type = "testType";
		esConfig.setESAddress("192.168.12.11");
	}
}
