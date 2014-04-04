package com.ktcloudware.crams.consumer.util;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

<<<<<<< HEAD
import com.ktcloudware.crams.consumer.CramsException;
=======
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
import com.ktcloudware.crams.consumer.plugins.CramsPluginException;

public class KafkaConsumerPluginUtilTest {

    @Test
    public void test() {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("unit_0", 0);
        dataMap.put("unit_1", 1);
        dataMap.put("unit_2", 2);
        dataMap.put("unit_3", 3);
        dataMap.put("unit_4", 4);
        try {
            dataMap = KafkaConsumerPluginUtil.addAverageValue("unit_[0-9]+",
                    "unit_result", dataMap);
<<<<<<< HEAD
        } catch (CramsException e) {
=======
        } catch (CramsPluginException e) {
>>>>>>> e78ac19f5440d48ea70e632fa092a3a030f29ee6
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(dataMap);
        assertEquals(2L, dataMap.get("unit_result"));
    }

}
