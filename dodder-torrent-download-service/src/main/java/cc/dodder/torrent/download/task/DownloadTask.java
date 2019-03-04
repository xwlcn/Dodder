package cc.dodder.torrent.download.task;

import cc.dodder.api.StoreFeignClient;
import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.entity.Result;
import cc.dodder.common.util.ByteUtil;
import cc.dodder.torrent.download.client.PeerWireClient;
import cc.dodder.torrent.download.stream.MessageStreams;
import cc.dodder.torrent.download.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.net.InetSocketAddress;

/***
 * Torrent 下载线程
 *
 * @author Mr.Xu
 * @date 2019-02-22 10:43
 **/
@Slf4j
public class DownloadTask implements Runnable {

	private DownloadMsgInfo msgInfo;
	private MessageStreams messageStreams;

	public DownloadTask(DownloadMsgInfo msgInfo) {
		this.msgInfo = msgInfo;
	}

	@Override
	public void run() {
		StoreFeignClient storeFeignClient = (StoreFeignClient) SpringContextUtil.getBean(StoreFeignClient.class);
		Result result = storeFeignClient.existHash(ByteUtil.byteArrayToHex(msgInfo.getInfoHash()));
		if (result.getStatus() == HttpStatus.NO_CONTENT.value()) {
			return;
		}

		PeerWireClient wireClient = new PeerWireClient();
		//设置下载完成监听器
		wireClient.setOnFinishedListener((torrent) -> {
			RedisTemplate redisTemplate = (RedisTemplate) SpringContextUtil.getBean("redisTemplate");
			if (torrent == null) {  //下载失败
				redisTemplate.delete(msgInfo.getInfoHash());
				return;
			}
			redisTemplate.persist(msgInfo.getInfoHash());
			messageStreams = (MessageStreams) SpringContextUtil.getBean(MessageStreams.class);
			//丢进 kafka 消息队列进行入库操作
			messageStreams.torrentMessageOutput()
					.send(MessageBuilder.withPayload(torrent)
							.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
							.build());
			log.info("[{}:{}] Download torrent success, info hash is {}",
					msgInfo.getIp(),
					msgInfo.getPort(),
					torrent.getInfoHash());
		});
		wireClient.downloadMetadata(new InetSocketAddress(msgInfo.getIp(), msgInfo.getPort()), msgInfo.getInfoHash());
	}
}
