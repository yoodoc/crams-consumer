package com.ktcloudware.crams.consumer.plugins;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplaceVmAccountNamePlugin implements CramsConsumerPlugin {
    private Pattern vmAccountNamePattern;
    private static final String VM_ACCOUNT_NAME = "vm_account_name";
    private static final String VM_ACCOUNT = "vm_account";

    public ReplaceVmAccountNamePlugin() {
        vmAccountNamePattern = Pattern.compile("epc_(m[0-9]+)_.*",
                Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Map<String, Object> excute(Map<String, Object> dataMap,
            String dataTag) {
        Object originName = dataMap.get(VM_ACCOUNT_NAME);

<<<<<<< HEAD
        if (!(originName instanceof String)) {
=======
        if (originName == null || !(originName instanceof String)) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            return null;
        }
        Matcher vmAccountNameMatcher = vmAccountNamePattern
                .matcher((String) originName);
        if (vmAccountNameMatcher.find()) {
            String vmAccount = vmAccountNameMatcher.group(1);
            dataMap.put(VM_ACCOUNT_NAME, vmAccount);
            dataMap.put(VM_ACCOUNT, originName);
        } else {
            dataMap.put(VM_ACCOUNT, originName);
        }

<<<<<<< HEAD
=======
        // TODO Auto-generated method stub
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        return dataMap;
    }

    @Override
    public void setProperties(String pluginProperties) {
<<<<<<< HEAD
=======
        // TODO Auto-generated method stub

>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
    }

    @Override
    public String getProperties() {
<<<<<<< HEAD
=======
        // TODO Auto-generated method stub
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        return null;
    }

    @Override
    public boolean needProperties() {
<<<<<<< HEAD
=======
        // TODO Auto-generated method stub
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
        return false;
    }

}
