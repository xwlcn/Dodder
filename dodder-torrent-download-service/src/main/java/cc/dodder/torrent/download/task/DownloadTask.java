package cc.dodder.torrent.download.task;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.util.JSONUtil;
import cc.dodder.torrent.download.client.PeerWireClient;
import cc.dodder.torrent.download.util.SpringContextUtil;
import lombok.SneakyThrows;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.net.InetSocketAddress;

/***
 * Torrent 下载线程
 *
 * @author Mr.Xu
 * @date 2019-02-22 10:43
 **/
public class DownloadTask implements Runnable {

	private DownloadMsgInfo msgInfo;
	private static ThreadLocal<PeerWireClient> wireClient = new ThreadLocal<>();

	public DownloadTask(DownloadMsgInfo msgInfo) {
		this.msgInfo = msgInfo;
	}

	@SneakyThrows
	@Override
	public void run() {

		if (wireClient.get() == null) {
			wireClient.set(new PeerWireClient());
			//设置下载完成监听器
			wireClient.get().setOnFinishedListener((torrent) -> {
				if (torrent == null) {  //下载失败
					return;
				}
				StringRedisTemplate redisTemplate = (StringRedisTemplate) SpringContextUtil.getBean(StringRedisTemplate.class);
				if (redisTemplate.hasKey(torrent.getInfoHash()))
					return;
				//丢进 kafka 消息队列进行入库及索引操作
				StreamBridge streamBridge = (StreamBridge) SpringContextUtil.getBean(StreamBridge.class);
				streamBridge.send("download-out-0", JSONUtil.toJSONString(torrent).getBytes());
				redisTemplate.opsForValue().set(torrent.getInfoHash(), "");
			});
		}
		wireClient.get().downloadMetadata(new InetSocketAddress(msgInfo.getIp(), msgInfo.getPort()), msgInfo.getNodeId(), msgInfo.getInfoHash());
	}
}