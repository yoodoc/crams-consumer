package com.ktcloudware.crams.consumer.plugins;

import java.util.concurrent.LinkedBlockingQueue;


public class UWatchPutMetricRequestQueue {
  
        private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        
        public UWatchPutMetricRequestQueue() {
        }
        
        public void put(String metricReqeust) {
            try {
                queue.put(metricReqeust);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        public String take() {
            try {
                String metricRequest = queue.take();
                return metricRequest;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
}
