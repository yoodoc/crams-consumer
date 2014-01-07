package com.ktcloudware.crams.consumer.plugins;

import com.ktcloudware.crams.consumer.CramsException;
import com.ktcloudware.crams.consumer.clients.CacheClient;

public class MockCacheClient implements CacheClient {

    public MockCacheClient(String cacheAddress) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean shutdown() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String set(String key, String value, int expireTimeInMinutes)
            throws CramsException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String get(String key) throws CramsException {
        if (key.equalsIgnoreCase("vm_unittest_name")) {
            return "device_a,1000,500";
        }

        return "vm_unittest_name";
    }

    @Override
    public void delete(String cacheItemKey) {
        // TODO Auto-generated method stub

    }

}
