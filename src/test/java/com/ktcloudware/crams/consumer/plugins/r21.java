package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

public class r21 {

    @Test
    public void test() {
        int count;
        final String value = "value";
         Cache<String, Object> cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.SECONDS).build();
   
        try {
            cache.put("ttt", value);
            System.out.println(cache.asMap().toString());
            Thread.sleep(5000);
            
            cache.put("ttt2","ttt2");
            System.out.println(cache.asMap().toString());
            
            Thread.sleep(5000);
            System.out.println(cache.asMap().toString());
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
}
