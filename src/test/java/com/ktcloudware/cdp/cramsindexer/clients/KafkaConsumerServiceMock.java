package com.ktcloudware.cdp.cramsindexer.clients;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;

public class KafkaConsumerServiceMock implements Runnable {

	private KafkaStream<byte[], byte[]> kafkaStream;

	public KafkaConsumerServiceMock(KafkaStream<byte[], byte[]> kafkaStream) {
		this.kafkaStream = kafkaStream;
	}

	@Override
	public void run() {
		ConsumerIterator<byte[], byte[]> it = kafkaStream.iterator();
		//int count = 0;
		//long totalTime = 0 ;
		while (it.hasNext()) {
			MessageAndMetadata<byte[], byte[]> nextItem = it.next();
			System.out.println("====================");
			System.out.println("topic:" + nextItem.topic());
			System.out.println("partition:" + nextItem.partition());
			System.out.println("message:" + new String(nextItem.message()));

		}
		System.out.println();
	}

}
