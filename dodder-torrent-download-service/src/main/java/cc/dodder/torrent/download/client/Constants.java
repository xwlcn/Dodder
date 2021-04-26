package cc.dodder.torrent.download.client;

import cc.dodder.common.util.NodeIdUtil;

public class Constants {

	public static final String BT_PROTOCOL = "BitTorrent protocol";
	public static final byte[] BT_RESERVED = new byte[] {
			(byte) (0x00 & 0xff), (byte) (0x00 & 0xff), (byte) (0x00 & 0xff), (byte) (0x00 & 0xff),
			(byte) (0x00 & 0xff), (byte) (0x10 & 0xff), (byte) (0x00 & 0xff), (byte) (0x01 & 0xff),
	};

	public static final byte[] EXT_HANDSHAKE_DATA = "d1:md11:ut_metadatai1eee".getBytes();

	public static final byte BT_MSG_ID = 20 & 0xff;
	public static final int EXT_HANDSHAKE_ID = 0;
	public static final int CONNECT_TIMEOUT = 5000;
	public static final int READ_WRITE_TIMEOUT = 5000;
	public static final int MAX_METADATA_SIZE = 1024 * 1024 * 10;        //最大 10M

	public static final byte[] PEER_ID = NodeIdUtil.createRandomNodeId();

	public static final long MAX_LOSS_TIME = 10 * 60 * 1000;

}
