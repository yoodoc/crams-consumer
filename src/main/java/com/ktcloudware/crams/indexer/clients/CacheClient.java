package com.ktcloudware.crams.indexer.clients;

public interface CacheClient {

	boolean shutdown();

	String set(String key, String value, int expireTimeInMinutes) throws Exception;

	String get(String key) throws Exception;

	void delete(String cacheItemKey);

}
