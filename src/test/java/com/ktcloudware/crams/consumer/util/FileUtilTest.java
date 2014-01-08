package com.ktcloudware.crams.consumer.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ktcloudware.crams.consumer.CramsException;

public class FileUtilTest {

    @Test
    public void testReadFileWithWrongFileName() {
        try {
            FileUtil.readFile("test");
            fail();
        } catch (CramsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testJsonToMapWithWrongFileName() {
        try {
            FileUtil.readJsonToMap("test");
            fail();
        } catch (CramsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testJsonToStringWithWrongFileName() {
        try {
            FileUtil.readJsonToString("test");
            fail();
        } catch (CramsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
