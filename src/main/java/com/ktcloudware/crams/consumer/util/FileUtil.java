package com.ktcloudware.crams.consumer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.map.ObjectMapper;

import com.ktcloudware.crams.consumer.CramsException;

public class FileUtil {
    private FileUtil() {
        
    }
    
    public static Map<String, Object> readJsonToMap(String fileName) throws CramsException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            InputStream is = FileUtil.class.getClassLoader()
                    .getResourceAsStream(fileName);
            if (is == null) {
               throw new CramsException("can't open file");
            }
            InputStreamReader in = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(in);
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = mapper.readValue(reader, Map.class);
            return dataMap;
        } catch (Exception e) {
            throw new CramsException("," + e.getMessage(), e);
        }
    }

    public static String readJsonToString(String fileName) throws CramsException {
        String jsonStr = "";
        try {
            InputStream is = FileUtil.class.getClassLoader()
                    .getResourceAsStream(fileName);
            if (is == null) {
                throw new CramsException("can't open file:" + fileName);
            }
            InputStreamReader in = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(in);
          
            String str ="";
            while ((str = reader.readLine()) != null) {
                jsonStr += str;
            }
            return jsonStr;
        } catch (Exception e) {
            throw new CramsException("failed to read json file, " + e.getMessage(), e);
        }
    }

    public static String readFile(String fileName) throws CramsException {
        InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(
                fileName);
        if (is == null) {
           throw new CramsException("missing file," + fileName); 
        }
        StringBuffer sb = new StringBuffer();
        byte[] b = new byte[4096];
        try {
            for (int n; (n = is.read(b)) != -1;) {
                sb.append(new String(b, 0, n));
            }
        } catch (IOException e) {
            throw new CramsException("," + e.getStackTrace(), e);
        }
        return sb.toString();
    }

    public static Properties getProperties(String propertyName) throws CramsException {
        Properties properties = new Properties();

        InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(
                propertyName);
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new CramsException("," + e.getStackTrace(), e);
        }
        return properties;

    }
}
