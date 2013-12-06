package com.ktcloudware.crams.consumer;

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

import com.ktcloudware.crams.consumer.clients.ESBulkIndexer;
import com.ktcloudware.crams.consumer.clients.KafkaConsumerGroup;
import com.ktcloudware.crams.consumer.dataType.KafkaConfig;
import com.ktcloudware.crams.consumer.util.IndexerOptionParser;

public class MainDaemon implements Daemon {
	
	private KafkaConfig kafkaConfig = null;
	private List<KafkaConsumerGroup> consumerGroupList;
	private ESBulkIndexer esBulkIndexer;
	//private CacheClient cacheClient;
	private Map<String, ExecutorService> executorMap;

	Logger logger = LogManager.getLogger("INDEXER");

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception {
		logger = LogManager.getLogger("INDEXER");
		
		// read kafka client configuration
		kafkaConfig = IndexerOptionParser
				.parseKafkaConsumerProperties();

		// create empty thread pool for each kafka topic
		executorMap = new HashMap<String, ExecutorService>();
		for(String topic : kafkaConfig.topics){
			ExecutorService executor = Executors
					.newFixedThreadPool(kafkaConfig.numOfThread);
			executorMap.put(topic, executor);
		}
	}

	@Override
	public void destroy(){
		for(String topic : executorMap.keySet()){
			executorMap.get(topic).shutdownNow();
		}

		logger.info("daemon shutdown");
	}

	@Override
	public void start() throws Exception {
		// create kafka consumer group & make consumer stream
		consumerGroupList = new ArrayList<KafkaConsumerGroup>();
		KafkaConsumerGroup consumerGroup = new KafkaConsumerGroup(
				kafkaConfig.zookeeper, kafkaConfig.groupId,
				kafkaConfig.numOfThread, kafkaConfig.topics, kafkaConfig.resetPolicy);
		consumerGroupList.add(consumerGroup);

		// get kafka consuming streams for each kafka topic. and assign consumer service thread for them 
		for(String topic : kafkaConfig.topics){
			ExecutorService executor = executorMap.get(topic);
			List<KafkaStream<byte[], byte[]>> streams = consumerGroup
					.getKafkaStreams(topic);

			try{
				for(int i = 0; i < streams.size(); i++){
					logger.info("start thread=" + i + " for topic=" + topic + " "
							+ streams.size());
					System.out.println("start thread=" + i + " for topic=" + topic + " "
							+ streams.size());
					Runnable worker = new KafkaConsumerService(streams.get(i),
							IndexerOptionParser.loadKafkaPlugins(topic), topic);
					executor.execute(worker);
				}
			}catch(Exception e){
				logger.error("service init fail :" + e.getMessage(), e);
				e.printStackTrace();
				stop();
				return;
			}
		}

		logger.info("start all index service threads");
	}

	@Override
	public void stop() throws Exception {
		for(KafkaConsumerGroup consumerGroup : consumerGroupList){
			consumerGroup.shutdown();
		}
		for(String topic : executorMap.keySet()){
			executorMap.get(topic).shutdown();
		}
		if(esBulkIndexer != null){
			esBulkIndexer.shutdown();
		}
	}

	/*private List<CramsIndexerPlugin> getPluginsForTopic(String topic){
		// load plugins
		// TODO Fixme
		
		List<CramsIndexerPlugin> plugins = new ArrayList<CramsIndexerPlugin>();
		if(topic.endsWith("usage")){
			plugins.add(new DiskUsageCachePlugin());
			
		} else if(topic.endsWith("rrd")){
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
