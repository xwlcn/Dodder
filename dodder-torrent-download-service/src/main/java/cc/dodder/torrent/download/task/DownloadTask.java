package cc.dodder.torrent.download.task;

import cc.dodder.api.StoreFeignClient;
import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.entity.Result;
import cc.dodder.common.util.ByteUtil;
import cc.dodder.common.util.SystemClock;
import cc.dodder.torrent.download.client.Constants;
import cc.dodder.torrent.download.client.PeerWireClient;
import cc.dodder.torrent.download.stream.MessageStreams;
import cc.dodder.torrent.download.util.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageHeaders;
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
		/*byte[] rightByte = new byte[10];
		System.arraycopy(msgInfo.getInfoHash(), 10, rightByte,0, 10);
		byte[] key = Arrays.concatenate(msgInfo.getIp().getBytes(), rightByte);*/
		//由于下载线程消费的速度总是比 dht server 生产的速度慢，所以要做一下时间限制，否则程序越跑越慢
		if (SystemClock.now() - msgInfo.getTimestamp() >= Constants.MAX_LOSS_TIME) {
			return;
		}

		StoreFeignClient storeFeignClient = (StoreFeignClient) SpringContextUtil.getBean(StoreFeignClient.class);
		Result result = storeFeignClient.existHash(ByteUtil.byteArrayToHex(msgInfo.getInfoHash()));
		if (result.getStatus() == HttpStatus.NO_CONTENT.value()) {
			RedisTemplate redisTemplate = (RedisTemplate) SpringContextUtil.getBean("redisTemplate");
			redisTemplate.opsForValue().set(msgInfo.getInfoHash(), new byte[0]);
			return;
		}

		PeerWireClient wireClient = new PeerWireClient();
		//设置下载完成监听器
		wireClient.setOnFinishedListener((torrent) -> {
			if (torrent == null) {  //下载失败
				return;
			}
			RedisTemplate redisTemplate = (RedisTemplate) SpringContextUtil.getBean("redisTemplate");
			torrent.setCreateDate(msgInfo.getTimestamp());
			redisTemplate.opsForValue().set(msgInfo.getInfoHash(), new byte[0]);
			messageStreams = (MessageStreams) SpringContextUtil.getBean(MessageStreams.class);
			//丢进 kafka 消息队列进行入库操作
			messageStreams.torrentMessageOutput()
					.send(MessageBuilder.withPayload(torrent)
							.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
							.build());
			//丢进 kafka 消息队列进行索引操作
			messageStreams.indexMessageOutput()
					.send(MessageBuilder.withPayload(torrent)
							.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
							.build());
			log.info("[{}:{}] Download torrent success, info hash is {}",
					msgInfo.getIp(),
					msgInfo.getPort(),
					torrent.getInfoHash());
		});
		wireClient.downloadMetadata(new InetSocketAddress(msgInfo.getIp(), msgInfo.getPort()), msgInfo.getNodeId(), msgInfo.getInfoHash());
	}
}
