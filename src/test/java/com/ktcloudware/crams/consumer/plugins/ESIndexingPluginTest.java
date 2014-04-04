package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ESIndexingPluginTest {

   // @Ignore
    @Test
    public void test() {
        ESIndexingPlugin esIndexingPlugin = null;
        try {
            esIndexingPlugin = new ESIndexingPlugin();
        } catch (CramsPluginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
        
        Map<String, Object> testData = new HashMap<String, Object>();
        testData.put("vm_name", "unittest_name");
        testData.put("vm_account_name", "account_name");
        testData.put("vm_uuid", "u-n-i-t-e-s-t");
        testData.put("vm_cpu", 1);
        testData.put("vm_type", "VR");
        testData.put("cpu0", 0.0000000005);
        testData.put("cpu1", 0.000000005);
        testData.put("datetime", "2013-01-01 11:13:11");
        testData.put("vm_pod_name", "kr-unittest-pod0");
        try {
            esIndexingPlugin.excute(testData, "unittest");
        } catch (CramsPluginException e) {
=======
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.cli.ParseException;
import org.junit.Ignore;
import org.junit.Test;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.datatype.ESConfig;
import com.ktcloudware.crams.consumer.util.FileUtil;

public class ESIndexingPluginTest {

    @Ignore
    @Test
    public void test() {
        int numOfThread = 10;
        String setting = null;
        String mapping = null;
        try {
            setting = FileUtil.readFile("indexSettings.json");
            System.out.println(setting);
            mapping = FileUtil.readFile("mappingInfo.json");
            System.out.println(mapping);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            fail();
        }
        
        ESConfig esConfig = new ESConfig();
        try {
            esConfig.setESAddress("14.63.226.175:9300");
        } catch (ParseException e) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
<<<<<<< HEAD
    }
=======
        List<ESIndexingPlugin> plugins = new ArrayList<ESIndexingPlugin>();
        try {
            plugins.add(new ESIndexingPlugin());
        } catch (CramsPluginException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
            fail();
        }
        for (ESIndexingPlugin esPlugin : plugins) {
            try {
                ESBulkIndexer esBulkIndexer = new ESBulkIndexer(
                        esConfig.esAddressList, "elasticsearch", "yoodoc",
                        "vm_pod_name", setting, mapping);
            } catch (CramsException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                fail();
            }
            try {
                esPlugin = new ESIndexingPlugin();
            } catch (CramsPluginException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                fail();
            }
        }
        ExecutorService executor = Executors.newFixedThreadPool(numOfThread);

        for (ESIndexingPlugin esPlugin : plugins) {
            MockRunableClassForKafkaConsumerPlugins worker = new MockRunableClassForKafkaConsumerPlugins(
                    plugins.get(0));
            executor.execute(worker);
        }
    }

>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
}
