package com.ktcloudware.crams.consumer.datatype;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.ParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.ktcloudware.crams.consumer.CramsException;

public class ESConfig {
    public String clusterName = "";
    public String indexKey = "";
    public String type = "";
    public String routingKey = "";
    public int bulkRequestSize = -1;
    public int maxRequestIntervalSec = 10;
    public List<InetSocketTransportAddress> esAddressList = new ArrayList<InetSocketTransportAddress>();
    public String settings;
    public String mappings;
    public String indexSettingsFileName;
    public String mappingInfoFileName;

    public ESConfig() {
        esAddressList = new ArrayList<InetSocketTransportAddress>();
    }

    public boolean validateConfigVals() throws CramsException {
        if (esAddressList.isEmpty()) {
            return false;
        } else if (clusterName.isEmpty()) {
            return false;
        } else if (type.isEmpty()) {
            return false;
        } else if (indexSettingsFileName.isEmpty()) {
            return false;
        } else if (mappingInfoFileName.isEmpty()) {
            return false;
        } else if (settings.isEmpty()) {
            return false;
        } else if ( mappings.isEmpty()) {
            return false;
        }
        ObjectMapper mapper = new ObjectMapper();
        InputStream settingsIs = null;
        InputStream mappingsIs = null;
        try {
            settingsIs = new ByteArrayInputStream(settings.getBytes("UTF-8"));
            mappingsIs = new ByteArrayInputStream(mappings.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            throw new CramsException("ES config validation failed", e1);
        }

        try {
            mapper.readValue(settingsIs, Map.class);
            mapper.readValue(mappingsIs, Map.class);
        } catch (Exception e) {
            throw new CramsException("failed to read es configuration file", e);
        }
        return true;
    }

    public List<InetSocketTransportAddress> setESAddress(String esAddress)
            throws ParseException {
        String[] urls = esAddress.split(",");
        int port;

        for (String address : urls) {
            String[] ipAndPort = address.split(":");
            if (ipAndPort.length == 2) {
                port = Integer.valueOf(ipAndPort[1]);
                esAddressList.add(new InetSocketTransportAddress(ipAndPort[0],
                        port));
            } else {
                throw new ParseException("wrong elasticsearch address format");
            }
        }
        return esAddressList;
    }
}
