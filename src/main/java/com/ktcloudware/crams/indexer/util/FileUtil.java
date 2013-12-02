package com.ktcloudware.crams.indexer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.map.ObjectMapper;

public class FileUtil {
	private static final String CONFIG_PATH = "/config/";

	public static Map<String, Object> readJsonToMap(String fileName) {
		ObjectMapper mapper = new ObjectMapper();
		try {
				InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
			if(is == null){
				System.out.println("Can't read file");
				return null;
			}
			InputStreamReader in = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(in);
			@SuppressWarnings("unchecked")
			Map<String, Object> dataMap = mapper.readValue(reader, Map.class);
			return dataMap;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String readJsonToString(String fileName) {
		String jsonStr = null;
		try {
			InputStream is = FileUtil.class.getClassLoader().getResourceAsStream(fileName);
			if(is == null){
				System.out.println("Can't read file");
				return null;
			}
			InputStreamReader in = new InputStreamReader(is);
			BufferedReader reader = new BufferedReader(in);
			jsonStr = new String();
			String str;
			while ((str = reader.readLine()) != null) {
				jsonStr += str;
			}
			return jsonStr;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		}
		
	public static String readJsonFromConfigPath(String fileName) {
		String jsonStr = null;
		try {
			Properties systemProperty = System.getProperties();
			String dir = (String) systemProperty.get("user.dir");
			File file = new File(dir + CONFIG_PATH + fileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader reader = new BufferedReader(fileReader);
			jsonStr = new String();
			String str;
			while ((str = reader.readLine()) != null) {
				jsonStr += str;
			}
			reader.close();
			fileReader.close();
			return jsonStr;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		}
	
		public static Properties readPropertiesFromConfigPath(String fileName) {
			Properties systemProperty = System.getProperties();
			String dir = (String) systemProperty.get("user.dir");
			File file = new File(dir + CONFIG_PATH + fileName);
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				Properties property = new Properties();
				property.load(is);
				is.close();
				return property;
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
	
	static List<String> splitJson(String jsonStr){
		List<String> jsonList = new ArrayList<String>();
		
		int newIndex = 0; 
		int openCount = 0;
		int closeCount = 0;
		int jsonStartIndex = jsonStr.indexOf("{");
		openCount++;
		while (true) {
			int newOpenIndex = jsonStr.indexOf("{",newIndex);
			if (newOpenIndex == -1) {
				return jsonList;
			}
			
			int newCloseIndex = jsonStr.indexOf("}", newIndex);
			if(newOpenIndex > newCloseIndex) {
				openCount++;
				newIndex = newOpenIndex + 1;
			} else {
				closeCount++;
				newIndex = newCloseIndex + 1;
			}
			
			if(openCount == closeCount) {
				String singleJson = jsonStr.substring(jsonStartIndex, newCloseIndex+1);
				jsonStartIndex = jsonStr.indexOf("{", newCloseIndex);
				jsonList.add(singleJson);
				openCount++;
				//return jsonList;
			}
		}
	}	
}
