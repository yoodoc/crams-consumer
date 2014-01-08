package com.ktcloudware.crams.consumer.clients;

import com.ktcloudware.crams.consumer.CramsException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient implements CacheClient {
    private JedisPool pool = null;
    private static final int DEFAULT_EXPIRE_TIME = 60;

    public RedisClient(String redisAddress) {
        JedisPoolConfig poolConf = new JedisPoolConfig();
        this.pool = new JedisPool(poolConf, redisAddress);
    }

    public boolean shutdown() {
        if (null != pool) {
            pool.destroy();
            pool = null;
        }
        return false;
    }

    /**
     * set key,value with DEFAULT_EXPIRTE_TIME
     */
    public synchronized String set(String key, String value,
            int expireTimeInMunites) throws CramsException {
        if (expireTimeInMunites < 1) {
            expireTimeInMunites = DEFAULT_EXPIRE_TIME;
        }
        Jedis jedis = pool.getResource();
        try {
            String result = jedis.set(key, value);
            if (result == null) {
                throw new CramsException("Inserting token is Failed.");
            }

            if (jedis.expire(key, expireTimeInMunites * 60) < 0) {
                throw new CramsException("ExpireTime setting is failed.");
            }

            return key;
        } catch (Exception e) {
            pool.returnBrokenResource(jedis);
            throw new CramsException("failed to set data to redis", e);
        } finally {
            pool.returnResource(jedis);
        }
    }

    /**
     * return value for key if exist, or return null
     */
    public synchronized String get(String key) throws CramsException {
        Jedis jedis = pool.getResource();
        try {
            if (null == jedis) {
                throw new CramsException("redis connection failed.");
            }
            if (jedis.exists(key)) {
                return jedis.get(key);
            }
        } catch (Exception e) {
            pool.returnBrokenResource(jedis);
            throw new CramsException("failed to get data from redis", e);
        } finally {
            pool.returnResource(jedis);
        }
        return null;
    }

    /**
     * delete key,value in cache
     */
    public synchronized void delete(String key) {
        Jedis jedis = pool.getResource();
        jedis.del(key);
        pool.returnResource(jedis);
    }

    /**
     * delete all cache data
     */
    public void deleteAll() {
        Jedis jedis = pool.getResource();
        jedis.flushAll();
        pool.returnResource(jedis);
    }

}
