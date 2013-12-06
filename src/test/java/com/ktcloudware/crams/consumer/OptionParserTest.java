package com.ktcloudware.crams.consumer;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.ktcloudware.crams.consumer.dataType.KafkaConfig;
import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class OptionParserTest {
  
	@Test
	public void test(){
		KafkaConfig kafkaConfig = null;
		try {
			kafkaConfig = IndexerOptionParser.parseKafkaConsumerProperties();
			assertEquals(1,kafkaConfig.numOfThread);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
