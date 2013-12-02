package com.ktcloudware.crams.consumer.clients;

public interface CacheClient {

	boolean shutdown();

	String set(String key, String value, int expireTimeInMinutes) throws Exception;

	String get(String key) throws Exception;

	void delete(String cacheItemKey);

}
