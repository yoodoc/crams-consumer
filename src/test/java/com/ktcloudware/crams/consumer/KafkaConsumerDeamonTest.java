package com.ktcloudware.crams.consumer;

<<<<<<< HEAD
import static org.junit.Assert.*;


import org.apache.commons.daemon.DaemonContext;
import org.junit.After;
=======
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.junit.After;
import org.junit.Ignore;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
import org.junit.Test;

import com.ktcloudware.crams.consumer.MainDaemon;

public class KafkaConsumerDeamonTest {
    MainDaemon deamon;

    @After
    public void after() throws Exception {
        deamon.stop();
        deamon.destroy();
    }

<<<<<<< HEAD
   // @Ignore
    @Test
    public void test() {
       try {
        deamon = new MainDaemon();
         DaemonContext arg0 = null;
        deamon.init(arg0);
        deamon.start();
        Thread.sleep(10 * 1000);
       } catch (Exception e) {
           fail();
       }
=======
    @Ignore
    @Test
    public void test() throws DaemonInitException, Exception {
        deamon = new MainDaemon();
        DaemonContext arg0 = null;
        deamon.init(arg0);
        deamon.start();
        Thread.sleep(60 * 1000);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    }
}
