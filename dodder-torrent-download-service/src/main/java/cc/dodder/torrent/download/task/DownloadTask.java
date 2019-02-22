package cc.dodder.torrent.download.task;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.util.ByteUtil;
import cc.dodder.torrent.download.client.PeerWireClient;
import cc.dodder.torrent.download.client.StoreFeignClient;
import cc.dodder.torrent.download.stream.MessageStreams;
import cc.dodder.torrent.download.util.SpringContextUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import java.net.InetSocketAddress;

/***
 * Torrent 下载线程
 *
 * @author Mr.Xu
 * @since 2019-02-22 10:43
 **/
public class DownloadTask implements Runnable {

	private DownloadMsgInfo msgInfo;
	private MessageStreams messageStreams;
	private StoreFeignClient storeFeignClient;

	public DownloadTask(DownloadMsgInfo msgInfo) {
		this.msgInfo = msgInfo;
	}

	@Override
	public void run() {
		storeFeignClient = (StoreFeignClient) SpringContextUtil.getBean(StoreFeignClient.class);
		ResponseEntity response = storeFeignClient.existHash(ByteUtil.byteArrayToHex(msgInfo.getInfoHash()));
		if (response.getStatusCode() == HttpStatus.NO_CONTENT)
			return;
		PeerWireClient wireClient = new PeerWireClient();
		//设置下载完成监听器
		wireClient.setFinishedListener((torrent) -> {
			messageStreams = (MessageStreams) SpringContextUtil.getBean(MessageStreams.class);
			//丢进 kafka 消息队列进行入库操作
			messageStreams.torrentMessageOutput()
					.send(MessageBuilder.withPayload(torrent)
							.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
							.build());
		});
		wireClient.downloadMetadata(new InetSocketAddress(msgInfo.getIp(), msgInfo.getPort()), msgInfo.getInfoHash());
	}
}
