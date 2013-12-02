/**
 * KafkaStream 인스턴스로 부터 Data을 읽고, KafkaDataJob 객체를 이용해 data 처리한다.  
 * 
 * @author yoodoc@gmail.com
 */

package com.ktcloudware.crams.indexer;

import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import com.ktcloudware.crams.indexer.clients.ESBulkIndexer;
import com.ktcloudware.crams.indexer.plugins.CramsIndexerPlugin;

import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;

/**
 * date format is hardcoded
 * 
 * @author yoodoc
 * 
 */
public class KafkaConsumerService implements Runnable {
	private static final String DATETIME = "datetime";

	private KafkaStream<byte[], byte[]> stream;
	private List<CramsIndexerPlugin> plugins;
	private ESBulkIndexer esBulkIndexer;

	public List<String> result;
	private ObjectMapper mapper;

	private Logger logger;

	public KafkaConsumerService(KafkaStream<byte[], byte[]> kafkaStream,
			List<CramsIndexerPlugin> plugins, ESBulkIndexer esBulkIndexer) {
		stream = kafkaStream;
		this.plugins = plugins;
		this.esBulkIndexer = esBulkIndexer;
		mapper = new ObjectMapper();
		logger = LogManager.getLogger("INDEXER");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			ConsumerIterator<byte[], byte[]> it = stream.iterator();
			int kafkaCount = 0;
			int indexCount = 0;
			long totalTime = 0;
			while (it.hasNext()) {
				long startTime = System.currentTimeMillis();

				// read a single kafka message
				Map<String, Object> userData = null;
				byte[] message = null;
				try {
					message = it.next().message();
					userData = mapper.readValue(message, Map.class);
					logger.debug("recieve new kafka messagemap: " + userData);
					kafkaCount++;
				} catch (Exception e) {
					// json format exception
					logger.error("json parsing error : " + new String(message));
					logger.error(e.getMessage());
					System.out.println("json parsing error " + new String(message));
					e.printStackTrace();
				}

				// modify a kafka message
				long startPluginsTime = System.currentTimeMillis();
				logger.debug("kafka read time " + (startPluginsTime - startTime));
				
				for (CramsIndexerPlugin plugin : plugins) {
					long startPluginTime = System.currentTimeMillis();
					userData = plugin.excute(userData);
					logger.trace("filtering kafka message with "
							+ plugin.getClass() + ", filtered message:"
							+ userData + " : " + (System.currentTimeMillis() - startPluginTime));
					if (userData == null) {
						logger.trace("filtering kafka message with "
								+ plugin.getClass() + ", filtered message:"
								+ userData);

						break;
					}
				}
				long endPluginTime = System.currentTimeMillis();
				logger.debug("plugin time " + (endPluginTime - startPluginsTime));
				
				if (userData != null && !userData.isEmpty()) {
					String index = parseIndexField(userData);
					if (index != null && !index.isEmpty()) {
						esBulkIndexer.excute(index, userData);
						indexCount++;
					}
				}

				// logging performance
				long endTime = System.currentTimeMillis();
				long time = endTime - startTime;
				totalTime += time;

				logger.trace("read count=" + kafkaCount
						+ ", write buffer size=" + indexCount);

				if (indexCount >= esBulkIndexer.maxSizeOfbulkRequest) {
					logger.info("INDEXER_STATS:read " + kafkaCount
							+ ", index " + indexCount
							+ "document to ES, and it took "
							+ (totalTime / 1000) + "Sec");
					totalTime = 0;
					kafkaCount = 0;
					indexCount = 0;
				}
			}
			logger.info("kafka broker session closed.");
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

	/**
	 * "cdp-<분단위 까지의 시간>" 형식의 index name을 생성한다. 
	 * @param dataMap
	 * @return
	 */
	private String parseIndexField(Map<String, Object> dataMap) {
		if (dataMap == null || dataMap.isEmpty()) {
			return null;
		}
		String date = (String) dataMap.get(DATETIME);
		if (date == null)
			return null;
		// TODO fix parsing index name from datetime field
		return "cdp-" + date.split("T")[0];
	}
}