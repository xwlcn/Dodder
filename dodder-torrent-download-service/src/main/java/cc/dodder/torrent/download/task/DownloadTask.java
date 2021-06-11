package cc.dodder.torrent.download.task;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.torrent.download.client.PeerWireClient;

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

	@Override
	public void run() {
		try {
			if (wireClient.get() == null) {
				wireClient.set(new PeerWireClient());
			}
			wireClient.get().downloadMetadata(new InetSocketAddress(msgInfo.getIp(), msgInfo.getPort()), msgInfo.getInfoHash(), msgInfo.getCrc64());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			msgInfo = null;
		}
	}


}