package com.ktcloudware.crams.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.KafkaStream;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.indexer.clients.CacheClient;
import com.ktcloudware.crams.indexer.clients.ESBulkIndexer;
import com.ktcloudware.crams.indexer.clients.KafkaConsumerGroup;
import com.ktcloudware.crams.indexer.dataType.ESConfig;
import com.ktcloudware.crams.indexer.dataType.KafkaConfig;
import com.ktcloudware.crams.indexer.plugins.CpuAvgPlugin;
import com.ktcloudware.crams.indexer.plugins.DateFormatPlugin;
import com.ktcloudware.crams.indexer.plugins.AppendDiskUsagePlugin;
import com.ktcloudware.crams.indexer.plugins.DiskUsageCachePlugin;
import com.ktcloudware.crams.indexer.plugins.FiveMinutesFilterPlugin;
import com.ktcloudware.crams.indexer.plugins.CramsIndexerPlugin;
import com.ktcloudware.crams.indexer.plugins.MemoryUsagePlugin;
import com.ktcloudware.crams.indexer.plugins.VbdReadWriteAvgPlugin;
import com.ktcloudware.crams.indexer.plugins.VifAvgPlugin;
import com.ktcloudware.crams.indexer.util.IndexerOptionParser;

public class IndexerDaemon implements Daemon {
	private static final String ES_PROPERTIES_PATH = "elasticsearch.properties";
	private static final String KAFKA_PROPERTIES_PATH = "kafkaConsumer.properties";

	private KafkaConfig kafkaConfig = null;
	private ESConfig esConfig = null;
	private List<KafkaConsumerGroup> consumerGroupList;
	private ESBulkIndexer esBulkIndexer;
	private CacheClient cacheClient;
	private Map<String, ExecutorService> executorMap;

	static Logger logger = LogManager.getLogger("INDEXER");

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
		// read ES client configuration
		esConfig = IndexerOptionParser.parseESProperties(ES_PROPERTIES_PATH);

		// read kafka client configuration
		kafkaConfig = IndexerOptionParser
				.parseKafkaConsumerProperties(KAFKA_PROPERTIES_PATH);

		// create empty thread pool for each kafka topic
		executorMap = new HashMap<String, ExecutorService>();
		for (String topic : kafkaConfig.topics) {
			ExecutorService executor = Executors
					.newFixedThreadPool(kafkaConfig.numOfThread);
			executorMap.put(topic, executor);
		}

		// initiate cache client connection
	}

	@Override
	public void destroy() {
		cacheClient.shutdown();
		for (String topic : executorMap.keySet()) {
			executorMap.get(topic).shutdownNow();
		}

		logger.info("daemon shutdown");
	}

	@Override
	public void start() throws Exception {
		// create ESBulkIndexer instance
		esBulkIndexer = new ESBulkIndexer(esConfig);

		// create kafka consumer group & make consumer stream
		consumerGroupList = new ArrayList<KafkaConsumerGroup>();
		KafkaConsumerGroup consumerGroup = new KafkaConsumerGroup(
				kafkaConfig.zookeeper, kafkaConfig.groupId,
				kafkaConfig.numOfThread, kafkaConfig.topics);
		consumerGroupList.add(consumerGroup);

		// get kafka consuming streams for each kafka topic. and assign consumer service thread for them 
		for (String topic : kafkaConfig.topics) {
			ExecutorService executor = executorMap.get(topic);
			List<KafkaStream<byte[], byte[]>> streams = consumerGroup
					.getKafkaStreams(topic);

			try {
				for (int i = 0; i < streams.size(); i++) {
					logger.info("start thread=" + i + "for topic=" + topic
							+ streams.size());
					Runnable worker = new KafkaConsumerService(streams.get(i),
							IndexerOptionParser.loadKafkaPlugins(topic), esBulkIndexer);
					executor.execute(worker);
				}
			} catch (Exception e) {
				logger.error("service init fail :" + e.getMessage());
				stop();
				return;
			}
		}

		logger.info("start all index service threads");
	}

	@Override
	public void stop() throws Exception {
		for (KafkaConsumerGroup consumerGroup : consumerGroupList) {
			consumerGroup.shutdown();
		}
		for (String topic : executorMap.keySet()) {
			executorMap.get(topic).shutdown();
		}
		if (esBulkIndexer != null) {
			esBulkIndexer.shutdown();
		}
	}

	/*private List<CramsIndexerPlugin> getPluginsForTopic(String topic) {
		// load plugins
		// TODO Fixme
		
		List<CramsIndexerPlugin> plugins = new ArrayList<CramsIndexerPlugin>();
		if (topic.endsWith("usage")) {
			plugins.add(new DiskUsageCachePlugin());
			
		} else if (topic.endsWith("rrd")) {
			plugins.add(new FiveMinutesFilterPlugin());
			plugins.add(new DateFormatPlugin());
			plugins.add(new AppendDiskUsagePlugin());
			plugins.add(new CpuAvgPlugin());
			plugins.add(new MemoryUsagePlugin());
			plugins.add(new VbdReadWriteAvgPlugin());
			plugins.add(new VifAvgPlugin());
		}

		return plugins;
	}*/
}
