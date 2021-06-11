package cc.dodder.common.entity;

import cc.dodder.common.util.SystemClock;
import lombok.Data;

import java.io.Serializable;

/***
 * Peer Wire 下载消息信息
 *
 * @author Mr.Xu
 * @date 2019-02-20 15:53
 **/
@Data
public class DownloadMsgInfo implements Serializable {

	private String ip;
	private int port;
	private byte[] infoHash;
	private long timestamp;
	private String crc64;

	public DownloadMsgInfo() {

	}

	public DownloadMsgInfo(String ip, int port, byte[] infoHash, String crc64) {
		this.ip = ip;
		this.port = port;
		this.infoHash = infoHash;
		timestamp = SystemClock.now();
		this.crc64 = crc64;
	}
}
