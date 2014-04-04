package com.ktcloudware.crams.consumer.util;

import java.io.BufferedReader;
<<<<<<< HEAD
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
=======
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.map.ObjectMapper;

import com.ktcloudware.crams.consumer.CramsException;

public class FileUtil {
<<<<<<< HEAD
=======
    private static final String CONFIG_PATH = "/config/";
  
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
<<<<<<< HEAD
            throw new CramsException("," + e.getMessage(), e);
=======
            throw new CramsException("," + e.getStackTrace(), e);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        }
    }

    public static String readJsonToString(String fileName) throws CramsException {
        String jsonStr = "";
        try {
            InputStream is = FileUtil.class.getClassLoader()
                    .getResourceAsStream(fileName);
            if (is == null) {
<<<<<<< HEAD
                throw new CramsException("can't open file:" + fileName);
=======
                throw new CramsException("can't open file");
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            }
            InputStreamReader in = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(in);
          
            String str ="";
            while ((str = reader.readLine()) != null) {
                jsonStr += str;
            }
            return jsonStr;
        } catch (Exception e) {
<<<<<<< HEAD
            throw new CramsException("failed to read json file, " + e.getMessage(), e);
=======
            throw new CramsException("failed to read json file," + e.getStackTrace(), e);
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        }
    }

    public static String readFile(String fileName) throws CramsException {
        InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(
                fileName);
<<<<<<< HEAD
        if (is == null) {
           throw new CramsException("missing file," + fileName); 
        }
=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
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
<<<<<<< HEAD
=======

    public static Properties readPropertiesFromConfigPath(String fileName) throws CramsException {

        Properties systemProperty = System.getProperties();
        String dir = (String) systemProperty.get("user.dir");
        String daemonHomeDir = (String) systemProperty.get("daemon.home");
        if (daemonHomeDir != null) {
            dir = daemonHomeDir;
        }
        File file = new File(dir + CONFIG_PATH + fileName);
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            Properties property = new Properties();
            property.load(is);
            is.close();
            return property;
        } catch (FileNotFoundException e) {
            throw new CramsException("," + e.getStackTrace(), e);
        } catch (IOException e) {
            throw new CramsException("," + e.getStackTrace(), e);
        }
    }

    static List<String> splitJson(String jsonStr) {
        List<String> jsonList = new ArrayList<String>();

        int newIndex = 0;
        int openCount = 0;
        int closeCount = 0;
        int jsonStartIndex = jsonStr.indexOf('{');
        openCount++;
        while (true) {
            int newOpenIndex = jsonStr.indexOf('{', newIndex);
            if (newOpenIndex == -1) {
                return jsonList;
            }

            int newCloseIndex = jsonStr.indexOf('}', newIndex);
            if (newOpenIndex > newCloseIndex) {
                openCount++;
                newIndex = newOpenIndex + 1;
            } else {
                closeCount++;
                newIndex = newCloseIndex + 1;
            }

            if (openCount == closeCount) {
                String singleJson = jsonStr.substring(jsonStartIndex,
                        newCloseIndex + 1);
                jsonStartIndex = jsonStr.indexOf('{', newCloseIndex);
                jsonList.add(singleJson);
                openCount++;
            }
        }
    }
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
}
