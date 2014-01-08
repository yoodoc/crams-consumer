package com.ktcloudware.crams.consumer.clients;

import com.ktcloudware.crams.consumer.CramsException;

public interface CacheClient {

    boolean shutdown();

    String set(String key, String value, int expireTimeInMinutes)
            throws CramsException;

    String get(String key) throws CramsException;

    void delete(String cacheItemKey);

}
