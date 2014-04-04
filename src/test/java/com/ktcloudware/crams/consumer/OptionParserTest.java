package com.ktcloudware.crams.consumer;

import static org.junit.Assert.*;

<<<<<<< HEAD
=======
import java.util.List;

import org.junit.Ignore;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
import org.junit.Test;

import com.ktcloudware.crams.consumer.datatype.ESConfig;
import com.ktcloudware.crams.consumer.datatype.KafkaConfig;
<<<<<<< HEAD
=======
import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class OptionParserTest {

    @Test
    public void test() {
        KafkaConfig kafkaConfig = null;
        try {
            kafkaConfig = IndexerOptionParser.parseKafkaConsumerProperties();
            assertEquals(1, kafkaConfig.numOfThread);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
        
        
        ESConfig esConfig = null;
        try {
            esConfig = IndexerOptionParser.parseESProperties();
            assertEquals("crams", esConfig.clusterName);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
