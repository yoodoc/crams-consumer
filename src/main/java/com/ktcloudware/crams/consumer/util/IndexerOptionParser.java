package com.ktcloudware.crams.consumer.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.datatype.ESConfig;
import com.ktcloudware.crams.consumer.datatype.KafkaConfig;
import com.ktcloudware.crams.consumer.plugins.CramsConsumerPlugin;
import com.ktcloudware.crams.consumer.plugins.CramsPluginException;

public class IndexerOptionParser {
    private static final String OPTION_ZOOKEEPER = "zookeeper";
    private static final String OPTION_GROUP = "groupId";
    private static final String OPTION_ES_ADDRESS = "esAddress";
    private static final String OPTION_ES_CLUSTER_NAME = "clusterName";
    private static final String OPTION_ES_INDEX_NAME = "indexer";
    private static final String OPTION_ES_BULK_INTERVAL_SEC = "requestIntervalMaxSec";
    private static final String OPTION_ES_BULKSIZE = "bulkRequestSize";
    private static final String OPTION_ES_ROUTINGKEY = "routingKey";
    private static final String OPTION_ES_TYPE_NAME = "type";
    private static final String OPTION_TOPIC = "topic";
    private static final String OPTION_CACHE_SERVER = "cacheServer";
    private static final String OPTION_INDEX_SETTING_FILE = "indexSettingJsonFile";
    private static final String OPTION_MAPPING_INFO_FILE = "mappingInfoJsonFile";
    private static final String OPTION_RESET_OFFSET = "resetOffset";
 
    private static final String ES_PROPERTIES_FILE = "esIndexer.properties";
    private static final String KAFKA_PROPERTIES_FILE = "kafkaConsumer.properties";
    
    static Logger logger = LogManager.getLogger("MAIN");

    private IndexerOptionParser() {
        
    }
    
    /**
     * esIndexer.properties 파일을 ESConfig.class 로 parsing 한다.
     * 
     * @return
     * @throws CramsException
     */
    public static ESConfig parseESProperties()
            throws CramsException {
        Properties properties = null;
        try {
            properties = FileUtil.getProperties(ES_PROPERTIES_FILE);
        } catch (Exception e) {
            throw new CramsException("failed to read ES property file", e);
        }
        PrintWriter stdoutWriter = new PrintWriter(System.out);
        stdoutWriter.append("-- es config ");
        properties.list(stdoutWriter);
        stdoutWriter.flush();

        ESConfig esConfig = new ESConfig();
        try {
            esConfig.bulkRequestSize = Integer.valueOf(properties
                    .getProperty(IndexerOptionParser.OPTION_ES_BULKSIZE));
            esConfig.maxRequestIntervalSec = Integer
                    .valueOf(properties
                            .getProperty(IndexerOptionParser.OPTION_ES_BULK_INTERVAL_SEC));
            esConfig.clusterName = properties
                    .getProperty(IndexerOptionParser.OPTION_ES_CLUSTER_NAME);
            esConfig.setESAddress(properties
                    .getProperty(IndexerOptionParser.OPTION_ES_ADDRESS));
            esConfig.indexKey = properties
                    .getProperty(IndexerOptionParser.OPTION_ES_INDEX_NAME);
            esConfig.routingKey = properties
                    .getProperty(IndexerOptionParser.OPTION_ES_ROUTINGKEY);
            esConfig.type = properties
                    .getProperty(IndexerOptionParser.OPTION_ES_TYPE_NAME);
            esConfig.indexSettingsFileName = properties
                    .getProperty(IndexerOptionParser.OPTION_INDEX_SETTING_FILE);
            esConfig.settings = FileUtil
                    .readFile(esConfig.indexSettingsFileName);
            esConfig.mappingInfoFileName = properties
                    .getProperty(IndexerOptionParser.OPTION_MAPPING_INFO_FILE);
            esConfig.mappings = FileUtil.readFile(esConfig.mappingInfoFileName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CramsException(e.getMessage(), e);
        }

        if (!esConfig.validateConfigVals()) {
            logger.error("es config validation error occurs.");
            return null;
        }
        logger.info("es bulkRequestSize" + esConfig.bulkRequestSize);
        logger.info("es bulkRequestSize" + esConfig.clusterName);
        logger.info("es bulkRequestSize" + esConfig.type);
        logger.info("es bulkRequestSize" + esConfig.mappings);
        logger.info("es bulkRequestSize" + esConfig.settings);
        return esConfig;
    }

    /**
     * kafkaConsumer.properties를 KafkaConfig.class 로 parsing 한다.
     * 
     * @param kafkaPropertiesPath
     * @return
     * @throws Exception
     */
    public static KafkaConfig parseKafkaConsumerProperties()
            throws CramsException {
        Properties properties = null;
        try {
            properties = FileUtil.getProperties(KAFKA_PROPERTIES_FILE);
        } catch (Exception e) {
            throw new CramsException("failed to read kafka property file", e);
        }

        PrintWriter stdoutWriter = new PrintWriter(System.out);
        stdoutWriter.append("-- kafka consumer config ");
        properties.list(stdoutWriter);
        stdoutWriter.flush();

        KafkaConfig kafkaConfig = new KafkaConfig();
        kafkaConfig.zookeeper = properties
                .getProperty(IndexerOptionParser.OPTION_ZOOKEEPER);
        kafkaConfig.groupId = properties
                .getProperty(IndexerOptionParser.OPTION_GROUP);
        kafkaConfig.cacheServer = properties
                .getProperty(IndexerOptionParser.OPTION_CACHE_SERVER);
        String topics = properties
                .getProperty(IndexerOptionParser.OPTION_TOPIC);
        kafkaConfig.resetPolicy = properties
                .getProperty(IndexerOptionParser.OPTION_RESET_OFFSET);
        for (String topic : topics.split(",")) {
            kafkaConfig.topics.add(topic);
        }
        kafkaConfig.numOfThread = 1;

        return kafkaConfig;

    }

    public static List<CramsConsumerPlugin> loadKafkaPlugins(String topic)
            throws CramsException {
        List<CramsConsumerPlugin> plugins = new ArrayList<CramsConsumerPlugin>();
        Properties properties = FileUtil
                .getProperties("cramsConsumerPlugins.properties");

        PrintWriter stdoutWriter = new PrintWriter(System.out);
        stdoutWriter.append("-- plugins config ");
        properties.list(stdoutWriter);
        stdoutWriter.flush();

        String pluginNames = properties.getProperty(topic);
        String[] pluginNameArray = pluginNames.split(",");
        for (String pluginName : pluginNameArray) {
            Class<?> pluginClass;
            try {
                pluginClass = Class
                        .forName("com.ktcloudware.crams.consumer.plugins."
                                + pluginName);
            } catch (ClassNotFoundException e) {
                throw new CramsException("failed to load crams plugin,"
                        + pluginName, e);
            }

            try {
                CramsConsumerPlugin plugin;
                try {
                    plugin = (CramsConsumerPlugin) pluginClass.newInstance();
                } catch (IllegalAccessException e1) {
                    logger.error(e1.getMessage(), e1);
                    throw new CramsException(e1.getMessage(), e1);
                }
                String pluginProperties = properties.getProperty(pluginName);
                if (pluginProperties != null) {
                    try {
                        plugin.setProperties(pluginProperties);
                    } catch (CramsPluginException e) {
                        logger.error("failed to set property", e);
                        throw new CramsException(e.getMessage(), e);
                    }
                }
                plugins.add(plugin);
                logger.info("load pluging : " + pluginClass.getName());
            } catch (InstantiationException e) {
                throw new CramsException("instantiation failed", e);
            }
        }
        return plugins;

    }
}
