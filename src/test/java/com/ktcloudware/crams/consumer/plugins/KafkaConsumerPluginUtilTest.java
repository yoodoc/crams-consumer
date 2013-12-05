package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.util.KafkaConsumerPluginUtil;

public class KafkaConsumerPluginUtilTest {
	
	@Test
	public void test(){
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("cpu1", new Double(100));
		message.put("cpu2", new Float(50));
	
		KafkaConsumerPluginUtil.addAverageValue("cpu[0-9]+", "cpu_avg", message);
		assertEquals((float)75.0, message.get("cpu_avg"));
	}
	
	@Test
	public void testVbd(){
		Map<String, Object> message = new HashMap<String, Object>();
		message.put("vbd_hda_write", 100);
		message.put("vbd_hdb_write", 50);
		
		message = KafkaConsumerPluginUtil.addAverageValue("vbd_hd[a-z]_write", "vbd_write_avg", message);
		assertEquals(75L, message.get("vbd_write_avg"));
	}
}
