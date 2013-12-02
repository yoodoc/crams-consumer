package com.ktcloudware.crams.consumer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.dataType.ESConfig;
import com.ktcloudware.crams.consumer.dataType.KafkaConfig;
import com.ktcloudware.crams.consumer.plugins.CramsIndexerPlugin;

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
	//private static final String OPTION_FETCH_SIZE = "fetchsize";
	private static final String OPTION_TOPIC = "topic";
	private static final String OPTION_CACHE_SERVER = "cacheServer";
	private static final String OPTION_INDEX_SETTING_FILE = "indexSettingJsonFile";
	private static final String OPTION_MAPPING_INFO_FILE = "mappingInfoJsonFile";
	private static final String OPTION_RESET_OFFSET = "resetOffset";

	static Logger logger = LogManager.getLogger("INDEXER");
	
	/**
	 * elasticsearch.properties를 ESConfig.class 로 parsing 한다. 
	 * @param esPropertiesPath
	 * @return
	 */
	public static ESConfig parseESProperties(String esPropertiesPath) {
		Properties properties = FileUtil.readPropertiesFromConfigPath(esPropertiesPath);
		ESConfig esConfig = new ESConfig();

		try {
			esConfig.bulkRequestSize = Integer.valueOf(properties
					.getProperty(IndexerOptionParser.OPTION_ES_BULKSIZE));
			esConfig.maxRequestIntervalSec = Integer.valueOf(properties
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
					.readJsonFromConfigPath(esConfig.indexSettingsFileName);
			esConfig.mappingInfoFileName = properties
					.getProperty(IndexerOptionParser.OPTION_MAPPING_INFO_FILE);
			esConfig.mappings = FileUtil.readJsonFromConfigPath(esConfig.mappingInfoFileName);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
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
	 *  kafkaConsumer.properties를 KafkaConfig.class 로 parsing 한다. 
	 * @param kafkaPropertiesPath
	 * @return
	 */
	public static KafkaConfig parseKafkaConsumerProperties(String kafkaPropertiesPath) {
		Properties properties = FileUtil
				.readPropertiesFromConfigPath(kafkaPropertiesPath);
		KafkaConfig kafkaConfig = new KafkaConfig();
		kafkaConfig.zookeeper = properties
				.getProperty(IndexerOptionParser.OPTION_ZOOKEEPER);
		kafkaConfig.groupId = properties.getProperty(IndexerOptionParser.OPTION_GROUP);
		kafkaConfig.cacheServer = properties.getProperty(IndexerOptionParser.OPTION_CACHE_SERVER);
		String topics = properties.getProperty(IndexerOptionParser.OPTION_TOPIC);
		kafkaConfig.resetPolicy =  properties.getProperty(IndexerOptionParser.OPTION_RESET_OFFSET);
		for (String topic : topics.split(",")) {
			kafkaConfig.topics.add(topic);
		}
		kafkaConfig.numOfThread = 1;

		return kafkaConfig;
		
	}
	
	public static List<CramsIndexerPlugin> loadKafkaPlugins(String topic) {
		List<CramsIndexerPlugin> plugins = new ArrayList<CramsIndexerPlugin>();
		Properties properties = FileUtil
				.readPropertiesFromConfigPath("cramsIndexerPlugins.properties");
		String pluginNames = properties.getProperty(topic);
		for (String pluginName: pluginNames.split(",")){
			try {
				Class<?> pluginClass = Class.forName("com.ktcloudware.crams.consumer.plugins." + pluginName);
				try {
					CramsIndexerPlugin plugin = (CramsIndexerPlugin) pluginClass.newInstance();
					String pluginProperties = properties.getProperty(pluginName);
					if (pluginProperties != null) {
						plugin.setProperties(pluginProperties);
					}
					plugins.add(plugin);
					logger.info("load pluging : " + pluginClass.getName());
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return plugins;
		
	}
	
	public static Map<String, String> parseCmdOptions(String[] args)
			throws ParseException {
		Options options = IndexerOptionParser.buildOptions();
		CommandLineParser parser = new GnuParser();
		CommandLine cmd = parser.parse(options, args);

		if (!cmd.hasOption(IndexerOptionParser.OPTION_ZOOKEEPER)
				|| !cmd.hasOption(IndexerOptionParser.OPTION_GROUP)
				|| !cmd.hasOption(IndexerOptionParser.OPTION_TOPIC)) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("kafka2es [OPTION]...", options);
			return null;
		}
		Map<String, String> map = new HashMap<String, String>();
		String zkAddress = cmd.getOptionValue(IndexerOptionParser.OPTION_ZOOKEEPER);
		String groupId = cmd.getOptionValue(IndexerOptionParser.OPTION_GROUP);
		String topic = cmd.getOptionValue(IndexerOptionParser.OPTION_TOPIC);
		String esAddress = cmd.getOptionValue(IndexerOptionParser.OPTION_ES_ADDRESS);
		String esClusterName = cmd
				.getOptionValue(IndexerOptionParser.OPTION_ES_CLUSTER_NAME);
		String esIndexName = cmd
				.getOptionValue(IndexerOptionParser.OPTION_ES_INDEX_NAME);
		String esTypeName = cmd
				.getOptionValue(IndexerOptionParser.OPTION_ES_TYPE_NAME);
		String esRoutingKeyName = cmd
				.getOptionValue(IndexerOptionParser.OPTION_ES_ROUTINGKEY);
		String esBulkSize = cmd.getOptionValue(IndexerOptionParser.OPTION_ES_BULKSIZE);
		String esBulkMaxIntervalSec = cmd
				.getOptionValue(IndexerOptionParser.OPTION_ES_BULK_INTERVAL_SEC);

		/* not implemented */
	//	String fetchSize = cmd.getOptionValue("fetchSize");

		map.put(IndexerOptionParser.OPTION_ZOOKEEPER, zkAddress);
		map.put(IndexerOptionParser.OPTION_GROUP, groupId);
		map.put(IndexerOptionParser.OPTION_TOPIC, topic);
		map.put(IndexerOptionParser.OPTION_ES_ADDRESS, esAddress);
		map.put(IndexerOptionParser.OPTION_ES_CLUSTER_NAME, esClusterName);
		map.put(IndexerOptionParser.OPTION_ES_INDEX_NAME, esIndexName);
		map.put(IndexerOptionParser.OPTION_ES_TYPE_NAME, esTypeName);
		map.put(IndexerOptionParser.OPTION_ES_ROUTINGKEY, esRoutingKeyName);
		map.put(IndexerOptionParser.OPTION_ES_BULKSIZE, esBulkSize);
		map.put(IndexerOptionParser.OPTION_ES_BULK_INTERVAL_SEC, esBulkMaxIntervalSec);

		if (esAddress != null && esClusterName != null) {
			map.put(IndexerOptionParser.OPTION_ES_ADDRESS, esAddress);
			map.put(IndexerOptionParser.OPTION_ES_CLUSTER_NAME, esClusterName);
		} else {
			HelpFormatter formatter = new HelpFormatter();
			formatter
					.printHelp(
							"kafka2es --enable --elasticsearch=<urls> -- clustername=<name> ...",
							options);
			return null;
		} 

		System.out.println("paring cli options");
		for (Entry<String, String> entry : map.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		return map;
	}

	/**
	 * 
	 * @return
	 */
	public static Options buildOptions() {
		@SuppressWarnings("static-access")
		Option listener = OptionBuilder
				.withArgName("urls")
				.hasArg()
				.withDescription(
						"REQUIRED:  The connection string for the zookeeper connection in the form host:port. Multiple URLS can be given to allow fail-over.")
				.create(IndexerOptionParser.OPTION_ZOOKEEPER);
		@SuppressWarnings("static-access")
		Option group = OptionBuilder.withArgName("group name").hasArg()
				.withDescription("REQUIRED: The group id to consume on.")
				.create(IndexerOptionParser.OPTION_GROUP);
		@SuppressWarnings("static-access")
		Option topic = OptionBuilder.withArgName("topic name").hasArg()
				.withDescription("REQUIRED: The topic id to consume on.")
				.create(IndexerOptionParser.OPTION_TOPIC);
		@SuppressWarnings("static-access")
		Option esAddress = OptionBuilder
				.withArgName("required action with data")
				.hasArg()
				.withArgName("urls")
				.withDescription(
						"The connection string for the elasticsearch connection in the form host:port. Multiple URLS can be given to allow fail-over.")
				.create(IndexerOptionParser.OPTION_ES_ADDRESS);
		@SuppressWarnings("static-access")
		Option esClusterName = OptionBuilder.withArgName("ES cluster name")
				.hasArg()
				.withDescription("enable inserting data to elasticsearch")
				.create(IndexerOptionParser.OPTION_ES_CLUSTER_NAME);
		@SuppressWarnings("static-access")
		Option esIndex = OptionBuilder.withArgName("index of es data").hasArg()
				.withDescription("index of es data")
				.create(IndexerOptionParser.OPTION_ES_INDEX_NAME);
		@SuppressWarnings("static-access")
		Option esType = OptionBuilder.withArgName("type of es data").hasArg()
				.withDescription("type of es data")
				.create(IndexerOptionParser.OPTION_ES_TYPE_NAME);
		@SuppressWarnings("static-access")
		Option esRoutingKey = OptionBuilder
				.withArgName("routing key for es data").hasArg()
				.withDescription("routing key for es data")
				.create(IndexerOptionParser.OPTION_ES_ROUTINGKEY);
		@SuppressWarnings("static-access")
		Option esBulkSize = OptionBuilder.withArgName("size").hasArg()
				.withDescription("es bulk insert size ")
				.create(IndexerOptionParser.OPTION_ES_BULKSIZE);
		@SuppressWarnings("static-access")
		Option esBulkMaxIntervalSec = OptionBuilder.withArgName("seconds")
				.hasArg()
				.withDescription("es bulk insert max interval in seconds")
				.create(IndexerOptionParser.OPTION_ES_BULK_INTERVAL_SEC);
		@SuppressWarnings("static-access")
		Option fetchSize = OptionBuilder
				.withArgName("urls")
				.hasArg()
				.withDescription(
						"The amount of data to fetch in a single request. (default: 1048576)")
				.create(IndexerOptionParser.OPTION_ES_INDEX_NAME);

		Options options = new Options();
		options.addOption(listener);
		options.addOption(fetchSize);
		options.addOption(group);
		options.addOption(topic);
		options.addOption(esAddress);
		options.addOption(esClusterName);
		options.addOption(esIndex);
		options.addOption(esType);
		options.addOption(esRoutingKey);
		options.addOption(esBulkSize);
		options.addOption(esBulkMaxIntervalSec);

		return options;
	}

}
