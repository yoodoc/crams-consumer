package com.ktcloudware.cdp.cramsindexer;

import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import com.ktcloudware.crams.indexer.IndexerDaemon;

public class KafkaConsumerDeamonTest {
	IndexerDaemon deamon;
	@After
	public void after() throws Exception {
		deamon.stop();
		deamon.destroy();
	}
	
	@Ignore
	@Test
	public void test() throws DaemonInitException, Exception {
		deamon = new IndexerDaemon();
		DaemonContext arg0 = null;
		deamon.init(arg0);
		deamon.start();
		Thread.sleep(60*1000);
	}
}
