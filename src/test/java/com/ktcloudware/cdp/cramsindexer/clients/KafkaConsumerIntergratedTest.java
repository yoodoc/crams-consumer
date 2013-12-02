package com.ktcloudware.cdp.cramsindexer.clients;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kafka.consumer.KafkaStream;

import org.junit.Ignore;
import org.junit.Test;

import com.ktcloudware.crams.indexer.clients.KafkaConsumerGroup;
import com.ktcloudware.crams.indexer.dataType.KafkaConfig;

public class KafkaConsumerIntergratedTest {
	private KafkaConfig kafkaConfig = null;
	private ExecutorService executor;
	private KafkaConsumerGroup consumerGroup;

	@Ignore
	@Test
	public void test() throws Exception {
		init();
		Thread.sleep(2 * 1000);
		start();
		Thread.sleep(120 * 1000);
		stop();
	}

	public void init() throws Exception {
		kafkaConfig = new KafkaConfig();
		kafkaConfig.zookeeper = "14.63.226.175";
		kafkaConfig.groupId = "ydTest";
		String topics = "yd1031";
		kafkaConfig.topics.add(topics);
		kafkaConfig.numOfThread = 1;

	}

	public void start() throws Exception {

		// create kafka consumer group & make consumer stream
		consumerGroup = new KafkaConsumerGroup(kafkaConfig.zookeeper,
				kafkaConfig.groupId, kafkaConfig.numOfThread,
				kafkaConfig.topics);

		// create kafka client for consuming messages
		String topic = kafkaConfig.topics.get(0);
		List<KafkaStream<byte[], byte[]>> streams = consumerGroup
				.getKafkaStreams(topic);
		executor = Executors.newFixedThreadPool(streams.size());
		for (int i = 0; i < streams.size(); i++) {
				System.out.println("start thread=" + i + "for topic=" + topic
						+ streams.size());
				Runnable worker = new KafkaConsumerServiceMock(streams.get(i));
				executor.execute(worker);
			}
	}

	public void stop() throws Exception {
		System.out.println("shutdown");
		consumerGroup.shutdown();
		executor.shutdown();
	}
}
