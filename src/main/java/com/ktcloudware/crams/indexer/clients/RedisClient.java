package com.ktcloudware.crams.indexer.clients;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient implements CacheClient{
	private JedisPool pool = null;
	private static final int defaultExpireTime = 60;

	public RedisClient(String redisAddress) {
		JedisPoolConfig poolConf = new JedisPoolConfig();
		/*poolConf.setMaxActive(100);
		poolConf.setMaxWait(3000);
		poolConf.setMaxIdle(10);
		poolConf.setMinIdle(10);
		poolConf.setTestOnReturn(true);
		poolConf.setTestOnBorrow(true);*/
		this.pool = new JedisPool(poolConf, redisAddress);
	}

	public boolean shutdown() {
		if (null != pool) {
			pool.destroy();
			pool = null;
		}
		return false;
	}

	public synchronized String set(String key, String value, int expireTimeInMunites) throws Exception {
		if (expireTimeInMunites < 1) {
			expireTimeInMunites = defaultExpireTime;
		}
		Jedis jedis = pool.getResource();
		try {
			String result = jedis.set(key, value);
			if (result == null) {
				//pool.returnResource(jedis);
				throw new Exception("Inserting token is Failed.");
			}

			if (jedis.expire(key, expireTimeInMunites * 60) < 0) {
				throw new Exception("ExpireTime setting is failed.");
			}

			return key;
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			throw e;
		} finally {
			pool.returnResource(jedis);
		}
	}

	public synchronized String get(String key) throws Exception {
		Jedis jedis = pool.getResource();
		try {
			if (null == jedis) {
				throw new Exception("redis connection failed.");
			}
			if (jedis.exists(key)) {
				String value = jedis.get(key);
				return value;
			}
		} catch (Exception e) {
			pool.returnBrokenResource(jedis);
			throw e;
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	public synchronized void delete(String key) {
		Jedis jedis = pool.getResource();
		jedis.del(key);
		pool.returnResource(jedis);
	}

	public void deleteAll() {
		Jedis jedis = pool.getResource();
		jedis.flushAll();
		pool.returnResource(jedis);
		// TODO Auto-generated method stub

	}

}
