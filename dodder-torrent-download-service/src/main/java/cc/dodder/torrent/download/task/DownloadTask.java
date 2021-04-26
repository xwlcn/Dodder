package cc.dodder.torrent.download.task;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.util.JSONUtil;
import cc.dodder.common.util.SystemClock;
import cc.dodder.torrent.download.client.Constants;
import cc.dodder.torrent.download.client.PeerWireClient;
import cc.dodder.torrent.download.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;

import java.net.InetSocketAddress;

/***
 * Torrent 下载线程
 *
 * @author Mr.Xu
 * @date 2019-02-22 10:43
 **/
public class DownloadTask implements Runnable {

	private DownloadMsgInfo msgInfo;
	private static ThreadLocal<KafkaTemplate> kafkaTemplate = new ThreadLocal<>();
	private static ThreadLocal<PeerWireClient> wireClient = new ThreadLocal<>();

	public DownloadTask(DownloadMsgInfo msgInfo) {
		this.msgInfo = msgInfo;
	}

	@Override
	public void run() {
		if (kafkaTemplate.get() == null) {
			kafkaTemplate.set((KafkaTemplate) SpringContextUtil.getBean(KafkaTemplate.class));
		}
		if (wireClient.get() == null) {
			wireClient.set(new PeerWireClient());
			//设置下载完成监听器
			wireClient.get().setOnFinishedListener((torrent) -> {
				if (torrent == null) {  //下载失败
					return;
				}
				RedisTemplate redisTemplate = (RedisTemplate) SpringContextUtil.getBean("redisTemplate");

				redisTemplate.opsForValue().set(torrent.getInfoHash(), new byte[0]);

				//丢进 kafka 消息队列进行入库及索引操作
				kafkaTemplate.get().send("torrentMessages", JSONUtil.toJSONString(torrent).getBytes());
			});
		}
		wireClient.get().downloadMetadata(new InetSocketAddress(msgInfo.getIp(), msgInfo.getPort()), msgInfo.getNodeId(), msgInfo.getInfoHash());
	}
}