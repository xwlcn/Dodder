package cc.dodder.torrent.download.client;

import cc.dodder.common.util.ByteUtil;
import cc.dodder.common.util.bencode.BencodingUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/***
 * Peer Wire 协议客户端
 *
 * @author: Mr.Xu
 * @create: 2019-02-19 09:21
 **/
@Slf4j
public class PeerWireClient {

	private Map<String, Object> map = new HashMap<>();

	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private int protocolLen;
	private int ut_metadata;        //extended message ID
	private int metadata_size;
	private int pieces;
	private byte[] metadata;

	private byte[] readBuff = new byte[1024];
	private ByteBuf cachedBuff = Unpooled.buffer(22 * 1024);

	private int nextSize;
	private NextFunction next;

	public void downloadMetadata(InetSocketAddress address, byte[] infoHash) {
		socket = new Socket();
		try {
			socket.connect(address, Constants.CONNECT_TIMEOUT);
			socket.setSoTimeout(Constants.READ_WRITE_TIMEOUT);

			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());

			setNext(1, onProtocolLen);
			sendHandShake(infoHash);

			int len;
			while (!socket.isClosed() && (len = in.read(readBuff)) != -1) {
				cachedBuff.writeBytes(readBuff, 0, len);
				handleMessage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			destrory();
		}
	}

	private void sendHandShake(byte[] infoHash) throws Exception {

		/*proctocol*/
		out.write(Constants.BT_PROTOCOL.length() & 0xff);
		out.write(Constants.BT_PROTOCOL.getBytes());

		/*reserved bytes*/
		out.write(Constants.BT_RESERVED);

		/*info_hash*/
		out.write(infoHash);

		/*peer_id*/
		out.write(Constants.PEER_ID);

		out.flush();

	}

	private void handleMessage() throws Exception {
		while (cachedBuff.readableBytes() >= nextSize) {
			byte[] buff = new byte[nextSize];
			cachedBuff.readBytes(buff);
			cachedBuff.discardReadBytes();
			next.doNext(buff);
		}
	}

	private NextFunction onMessage = new NextFunction() {
		@Override
		public void doNext(byte[] buff) throws Exception {
			setNext(4, onMessageLength);
			if (buff[0] == Constants.BT_MSG_ID) {
				resolveExtendMessage(buff[1], Arrays.copyOfRange(buff, 2, buff.length));
			}
		}
	};

	private void resolveExtendMessage(byte b, byte[] buf) throws Exception {

		if (b == 0) {
			resolveExtendHandShake(BencodingUtils.decode(buf));
		} else {
			resolvePiece(buf);
		}

	}

	private void resolvePiece(byte[] buff) {
		String str = new String(buff, StandardCharsets.ISO_8859_1);
		int pos = str.indexOf("ee") + 2;
		Map map = BencodingUtils.decode(str.substring(0, pos).getBytes(StandardCharsets.ISO_8859_1));
		byte[] piece_metadata = Arrays.copyOfRange(buff, pos, buff.length);

		if (!map.containsKey("msg_type") || !map.containsKey("piece")) {
			destrory();
			return;
		}

		if (((BigInteger) map.get("msg_type")).intValue() != 1) {
			destrory();
			return;
		}

		int piece = ((BigInteger) map.get("piece")).intValue();
		System.arraycopy(piece_metadata, 0, this.metadata, piece * 16 * 1024, piece_metadata.length);
		pieces--;

		checkFinished();
	}

	private void checkFinished() {
		if (pieces <= 0) {
			Map map = BencodingUtils.decode(Arrays.copyOfRange(metadata, 0, metadata_size));
			destrory();
			System.out.println(map);
		}
	}

	private void resolveExtendHandShake(Map map) throws Exception {

		Map m = (Map<String, Object>) map.get("m");

		if (m == null || !m.containsKey("ut_metadata") || !map.containsKey("metadata_size")) {
			destrory();
			return;
		}
		this.ut_metadata = ((BigInteger) m.get("ut_metadata")).intValue();
		this.metadata_size = ((BigInteger) map.get("metadata_size")).intValue();

		if (this.metadata_size > Constants.MAX_METADATA_SIZE) {
			destrory();
			return;
		}

		requestPieces();
	}

	private void requestPieces() throws Exception {

		metadata = new byte[this.metadata_size];

		pieces = (int) Math.ceil(this.metadata_size / (16.0 * 1024));

		for (int piece = 0; piece < pieces; piece++) {
			requestPiece(piece);
		}
	}

	private void requestPiece(int piece) throws Exception {
		map.clear();
		map.put("msg_type", 0);
		map.put("piece", piece);

		byte[] data = BencodingUtils.encode(map);

		sendMessage(this.ut_metadata, data);

	}

	private NextFunction onMessageLength = (byte[] buff) -> {
		int length = ByteUtil.byteArrayToInt(buff);
		if (length > 0)
			setNext(length, onMessage);
	};

	private NextFunction onHandshake = (byte[] buff) -> {
		byte[] handshake = Arrays.copyOfRange(buff, protocolLen, buff.length);
		if (handshake[5] == 0x10) {
			setNext(4, onMessageLength);
			sendExtHandShake();
		}
	};

	private NextFunction onProtocolLen = (byte[] buff) -> {
		protocolLen = (int) buff[0];
		//接下来是协议名称(长度：protocolLen)和BT_RESERVED(长度：8)、info_hash(长度：20)、peer_id(长度：20)
		setNext(protocolLen + 48, onHandshake);
	};

	private void sendExtHandShake() throws Exception {
		sendMessage(Constants.EXT_HANDSHAKE_ID, Constants.EXT_HANDSHAKE_DATA);
	}

	private void sendMessage(int id, byte[] data) throws Exception {

		//length prefix bytes
		byte[] length_prefix = ByteUtil.intToByteArray(data.length + 2);
		for(int i=0; i<4; i++)
			length_prefix[i] = (byte)(length_prefix[i] & 0xff);
		out.write(length_prefix);

		//bittorrent message ID, = 20
		out.write(Constants.BT_MSG_ID);

		//extended message ID. 0 = handshake, >0 = extended message as specified by the handshake.
		out.write((byte)(id & 0xff));

		//data
		out.write(data);
		out.flush();
	}

	private interface NextFunction {
		void doNext(byte[] buff)  throws Exception;
	}

	private void setNext(int nextSize, NextFunction next) {
		this.nextSize = nextSize;
		this.next = next;
	}

	private void destrory() {
		if (socket.isConnected()) {
			try {
				socket.close();
			} catch (Exception e) {
			}
		}
		try {
			in.close();
		} catch (Exception e) {
		}
		try {
			out.close();
		} catch (Exception e) {
		}
		metadata = null;
		readBuff = null;
		cachedBuff.clear();
	}


}
