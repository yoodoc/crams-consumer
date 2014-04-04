package com.ktcloudware.crams.consumer.clients;

import com.ktcloudware.crams.consumer.CramsException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisClient implements CacheClient {
    private JedisPool pool = null;
<<<<<<< HEAD
    private static final int DEFAULT_EXPIRE_TIME = 60;

    public RedisClient(String redisAddress) {
        JedisPoolConfig poolConf = new JedisPoolConfig();
=======
    private static final int defaultExpireTime = 60;

    public RedisClient(String redisAddress) {
        JedisPoolConfig poolConf = new JedisPoolConfig();
        /*
         * poolConf.setMaxActive(100); poolConf.setMaxWait(3000);
         * poolConf.setMaxIdle(10); poolConf.setMinIdle(10);
         * poolConf.setTestOnReturn(true); poolConf.setTestOnBorrow(true);
         */
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        this.pool = new JedisPool(poolConf, redisAddress);
    }

    public boolean shutdown() {
        if (null != pool) {
            pool.destroy();
            pool = null;
        }
        return false;
    }

<<<<<<< HEAD
    /**
     * set key,value with DEFAULT_EXPIRTE_TIME
     */
    public synchronized String set(String key, String value,
            int expireTimeInMunites) throws CramsException {
        if (expireTimeInMunites < 1) {
            expireTimeInMunites = DEFAULT_EXPIRE_TIME;
=======
    public synchronized String set(String key, String value,
            int expireTimeInMunites) throws CramsException {
        if (expireTimeInMunites < 1) {
            expireTimeInMunites = defaultExpireTime;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        }
        Jedis jedis = pool.getResource();
        try {
            String result = jedis.set(key, value);
            if (result == null) {
<<<<<<< HEAD
                throw new CramsException("Inserting token is Failed.");
            }

            if (jedis.expire(key, expireTimeInMunites * 60) < 0) {
                throw new CramsException("ExpireTime setting is failed.");
=======
                throw new Exception("Inserting token is Failed.");
            }

            if (jedis.expire(key, expireTimeInMunites * 60) < 0) {
                throw new Exception("ExpireTime setting is failed.");
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            }

            return key;
        } catch (Exception e) {
            pool.returnBrokenResource(jedis);
            throw new CramsException("failed to set data to redis", e);
        } finally {
            pool.returnResource(jedis);
        }
    }

<<<<<<< HEAD
    /**
     * return value for key if exist, or return null
     */
=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    public synchronized String get(String key) throws CramsException {
        Jedis jedis = pool.getResource();
        try {
            if (null == jedis) {
<<<<<<< HEAD
                throw new CramsException("redis connection failed.");
            }
            if (jedis.exists(key)) {
                return jedis.get(key);
=======
                throw new Exception("redis connection failed.");
            }
            if (jedis.exists(key)) {
                String value = jedis.get(key);
                return value;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            }
        } catch (Exception e) {
            pool.returnBrokenResource(jedis);
            throw new CramsException("failed to get data from redis", e);
        } finally {
            pool.returnResource(jedis);
        }
        return null;
    }

<<<<<<< HEAD
    /**
     * delete key,value in cache
     */
=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    public synchronized void delete(String key) {
        Jedis jedis = pool.getResource();
        jedis.del(key);
        pool.returnResource(jedis);
    }

<<<<<<< HEAD
    /**
     * delete all cache data
     */
=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    public void deleteAll() {
        Jedis jedis = pool.getResource();
        jedis.flushAll();
        pool.returnResource(jedis);
    }

}
