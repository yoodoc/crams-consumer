package com.ktcloudware.crams.consumer.clients;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.ktcloudware.crams.consumer.plugins.UWatchPutMetricRequestQueue;

public class HttpClient implements Runnable{
    private static Logger logger;
    private UWatchPutMetricRequestQueue requestQueue;
    HttpURLConnection httpConnection = null;
    public HttpClient(UWatchPutMetricRequestQueue requestQueue) {
        this.requestQueue = requestQueue;
    }
    
    @Override
    public void run() {
        while (true) {
            String request = requestQueue.take();
            String response = sendRequest(request);
            if (response == null) {
                logger.error(request + ": failed to send watch request");
            } else {
                logger.trace("send ucloud watch request : " + request
                        + ", response : " + response);
            }
        }
    }
    
    public String sendRequest(String requestUrl) {
        logger = LogManager.getLogger("CRAMS_CONSUMER");
        InputStream responseStream = null;
       
        long elapsedTime = -1;

        String response = null;
        try {
            long startTime = System.currentTimeMillis();
            URL url = new URL(requestUrl);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setConnectTimeout(10000);
            responseStream = httpConnection.getInputStream();
            StringBuffer sb = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = responseStream.read(b)) != -1;) {
                sb.append(new String(b, 0, n));
            }
            responseStream.close();
            response = sb.toString();
          //  logger.trace("response:" + response);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("abnormal HTTP response code:"
                        + responseCode);
            }
           elapsedTime = System.currentTimeMillis() - startTime;
           logger.trace("single transaction took " + elapsedTime + " msec");
        } catch (IOException e) {
        	e.printStackTrace();
            if (httpConnection != null) {
                try {
                    InputStream in = httpConnection.getErrorStream();
                    in.close();
                } catch (IOException e1) {
                    logger.error(e1.getMessage(), e1);
                }
                httpConnection.disconnect();
            }
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            if (httpConnection != null) {
                //httpConnection.disconnect();
            }
            if (responseStream != null) {
                try {
                    responseStream.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return response;
    }

   
}
