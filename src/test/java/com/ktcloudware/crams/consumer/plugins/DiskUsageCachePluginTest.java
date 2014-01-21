package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.ktcloudware.crams.consumer.clients.RedisClient;
import com.ktcloudware.crams.consumer.plugins.DiskUsageCachePlugin;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class DiskUsageCachePluginTest {
    // @Ignore
    @Test
    public void testRedisCache() throws Exception {
        RedisClient redis = new RedisClient("localhost");
        DiskUsageCachePlugin plugin = new DiskUsageCachePlugin();
        plugin.setProperties("localhost,600");

        // insert First data
        Map<String, Object> testData = FileUtil.readJsonToMap("diskData1.json");
        plugin.excute(testData, null);
        assertEquals("xvda,2147483648,234881024",
                redis.get((String) testData.get("vdi_name")));

        // insert Second data
        testData = FileUtil.readJsonToMap("diskData2.json");
        plugin.excute(testData, null);
        assertEquals("xvdb,2147483648,47483648",
                redis.get((String) testData.get("vdi_name")));
        assertEquals("ROOT-36597,DATA-46597",
                redis.get(String.valueOf(testData.get("vm_uuid"))));

        // insert update data
        testData = FileUtil.readJsonToMap("diskData1.json");
        testData.put("vdi_using_size", 1);
        plugin.excute(testData, null);
        assertEquals("xvda,2147483648,1",
                redis.get((String) testData.get("vdi_name")));
        assertEquals("ROOT-36597,DATA-46597",
                redis.get(String.valueOf(testData.get("vm_uuid"))));
    }
}
