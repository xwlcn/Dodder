package cc.dodder.torrent.download.client;

import cc.dodder.common.entity.Info;
import cc.dodder.common.entity.SubFile;
import cc.dodder.common.entity.Torrent;
import cc.dodder.common.util.ByteUtil;
import cc.dodder.common.util.ExtensionUtil;
import cc.dodder.common.util.bencode.BencodingUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

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

	private Optional<Torrent> torrent;

	/**
	 * 下载完成监听器
	 */
	private Consumer<Torrent> onFinishedListener;

	public void setOnFinishedListener(Consumer<Torrent> listener) {
		this.onFinishedListener = listener;
	}

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
			//e.printStackTrace();
		} finally {
			destroy();
			torrent.ifPresent(onFinishedListener);
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
		if (b == 0)
			resolveExtendHandShake(BencodingUtils.decode(buf));
		else
			resolvePiece(buf);
	}

	private void resolvePiece(byte[] buff) {
		String str = new String(buff, StandardCharsets.ISO_8859_1);
		int pos = str.indexOf("ee") + 2;
		Map map = BencodingUtils.decode(str.substring(0, pos).getBytes(StandardCharsets.ISO_8859_1));
		byte[] piece_metadata = Arrays.copyOfRange(buff, pos, buff.length);

		if (!map.containsKey("msg_type") || !map.containsKey("piece")) {
			destroy();
			return;
		}

		if (((BigInteger) map.get("msg_type")).intValue() != 1) {
			destroy();
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
			destroy();
			if (map == null)
				return;
			this.torrent = parseTorrent(map);
		}
	}

	/**
	* 解析 Torrent 文件信息，封装成对象
	 *
	 * 多文件Torrent的结构的树形图为：
	 *
	 * Multi-file Torrent
	 * ├─announce
	 * ├─announce-list
	 * ├─comment
	 * ├─comment.utf-8
	 * ├─creation date
	 * ├─encoding
	 * ├─info
	 * │ ├─files
	 * │ │ ├─length
	 * │ │ ├─path
	 * │ │ └─path.utf-8
	 * │ ├─name
	 * │ ├─name.utf-8
	 * │ ├─piece length
	 * │ ├─pieces
	 * │ ├─publisher
	 * │ ├─publisher-url
	 * │ ├─publisher-url.utf-8
	 * │ └─publisher.utf-8
	 * └─nodes
	 *
	 * 单文件Torrent的结构的树形图为：
	 *
	 * Single-File Torrent
	 * ├─announce
	 * ├─announce-list
	 * ├─comment
	 * ├─comment.utf-8
	 * ├─creation date
	 * ├─encoding
	 * ├─info
	 * │ ├─length
	 * │ ├─name
	 * │ ├─name.utf-8
	 * │ ├─piece length
	 * │ ├─pieces
	 * │ ├─publisher
	 * │ ├─publisher-url
	 * │ ├─publisher-url.utf-8
	 * │ └─publisher.utf-8
	 * └─nodes
	*
	* @param map
	* @return java.util.Optional<cc.dodder.common.entity.Torrent>
	*/
	private Optional<Torrent> parseTorrent(Map map) {
		String encoding = "UTF-8";
		Map<String, Object> info;
		Torrent torrent;
		if (map.containsKey("info"))
			info = (Map<String, Object>) map.get("info");
		else
			info = map;

		if (!info.containsKey("name"))
			return Optional.empty();
		if (map.containsKey("encoding"))
			encoding = (String) map.get("encoding");
		/*
		if (map.containsKey("announce")) {
			torrent.setAnnounce(decode_utf8(encoding, map, "announce"));
		}

		if (map.containsKey("comment")) {
			torrent.setComment(decode_utf8(encoding, map, "comment").toString().substring(0, 300));
		}

		if (map.containsKey("created by")) {
			torrent.setCreatedBy(decode_utf8(encoding, map, "created by"));
		}*/
		torrent = new Torrent();
		Info torrentInfo = new Info();

		if (map.containsKey("creation date"))
			torrent.setCreationDate(new Date(((BigInteger) map.get("creation date")).longValue()));
		else
			torrent.setCreationDate(new Date());


		String name = decode_utf8(encoding, map, "name");
		torrentInfo.setName(name.length() > 200 ? name.substring(0, 200) : name);

		Set<String> types = new HashSet<>();
		//多文件
		if (info.containsKey("files")) {
			List<Map<String, Object>> list = (List<Map<String, Object>>) info.get("files");
			List<SubFile> subFiles = new ArrayList<>();
			Long countLength = new Long(0);
			for (Map<String, Object> f : list) {
				Long length = ((BigInteger) f.get("length")).longValue();
				countLength += length;

				String path = decode_utf8_2(encoding, f, "path");
				if (path != null)
					path = path.length() > 200 ? path.substring(0, 200) : path;
				SubFile subFile = new SubFile(length, path);

				String type = ExtensionUtil.getExtensionType(subFile.getPath());
				if (type != null) {
					types.add(type);
				}

				if (subFile.getPath().indexOf("如果您看到此文件，请升级到BitComet(比特彗星)") == -1)
					subFiles.add(subFile);
			}
			torrentInfo.setFiles(subFiles);
			torrentInfo.setLength(countLength);

			String temp = org.apache.commons.lang.StringUtils.join(types.toArray(new String[]{}), ",");
			if (temp != null && !"".equals(temp)) {
				torrent.setType(temp);
			} else {
				torrent.setType("其他");
			}
		} else {
			torrentInfo.setLength(((BigInteger) info.get("length")).longValue());

			String type = ExtensionUtil.getExtensionType(name);
			torrent.setType(type == null ? "其他" :type);
		}
		torrent.setInfo(torrentInfo);
		return Optional.of(torrent);
	}

	private String decode_utf8(String encoding, Map<String, Object> map, String key) {
		if (map.containsKey(key + ".utf-8")) {
			return new String((byte[]) map.get(key + ".utf-8"), StandardCharsets.UTF_8);
		}
		return StringUtils.newStringUtf8((byte[]) map.get(key));
	}

	private String decode_utf8_2(String encoding, Map<String, Object> map, String key) {
		if (map.containsKey(key + ".utf-8")) {
			return new String(((List<byte[]>) map.get(key + ".utf-8")).get(0), StandardCharsets.UTF_8);
		}
		return StringUtils.newStringUtf8(((List<byte[]>) map.get(key )).get(0));
	}

	private void resolveExtendHandShake(Map map) throws Exception {

		Map m = (Map<String, Object>) map.get("m");

		if (m == null || !m.containsKey("ut_metadata") || !map.containsKey("metadata_size")) {
			destroy();
			return;
		}
		this.ut_metadata = ((BigInteger) m.get("ut_metadata")).intValue();
		this.metadata_size = ((BigInteger) map.get("metadata_size")).intValue();

		if (this.metadata_size > Constants.MAX_METADATA_SIZE) {
			destroy();
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

	private void destroy() {
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
