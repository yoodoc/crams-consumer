package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

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
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }
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

}
