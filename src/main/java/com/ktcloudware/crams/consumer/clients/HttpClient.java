package com.ktcloudware.crams.consumer.clients;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class HttpClient {
    private static Logger logger;

    public static String sendRequest(String requestUrl) {
        logger = LogManager.getLogger("HTTP");
        HttpURLConnection httpConnection = null;
        InputStream responseStream = null;
        long elapsedTime = -1;

        String response = null;
        try {
            long startTime = System.currentTimeMillis();

            URL url = new URL(requestUrl);

            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            responseStream = httpConnection.getInputStream();

            StringBuffer sb = new StringBuffer();
            byte[] b = new byte[4096];
            for (int n; (n = responseStream.read(b)) != -1;) {
                sb.append(new String(b, 0, n));
            }
            responseStream.close();

            response = sb.toString();
            logger.trace("response:" + response);
            int responseCode = httpConnection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("abnormal HTTP response code:"
                        + responseCode);
            }

            elapsedTime = System.currentTimeMillis() - startTime;

            logger.trace("single transaction took " + elapsedTime + " msec");
        } catch (IOException e) {
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
