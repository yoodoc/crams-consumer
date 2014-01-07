package com.ktcloudware.crams.consumer.plugins;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import com.ktcloudware.crams.consumer.plugins.DateFormatPlugin;

public class DateFormatPluginTest {

    @Test
    public void test() {
        DateFormatPlugin plugin = new DateFormatPlugin();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("datetime", "2013-01-01 11:13:11");
        try {
            map = plugin.excute(map, null);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail();
        }

        assertEquals("2013-01-01T11:13:11+0900", map.get("datetime"));
    }

}
