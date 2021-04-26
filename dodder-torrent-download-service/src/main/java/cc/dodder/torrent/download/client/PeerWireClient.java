package cc.dodder.torrent.download.client;

import cc.dodder.common.entity.Node;
import cc.dodder.common.entity.Torrent;
import cc.dodder.common.entity.Tree;
import cc.dodder.common.util.*;
import cc.dodder.common.util.bencode.BencodingUtils;
import cc.dodder.torrent.download.TorrentDownloadServiceApplication;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/***
 * Peer Wire 协议客户端，参见协议：
 * http://www.bittorrent.org/beps/bep_0009.html
 *
 * @author: Mr.Xu
 * @create: 2019-02-19 09:21
 **/
@Slf4j
public class PeerWireClient {

	private Map<String, Object> map;

	private Socket socket;
	private InputStream in;
	private OutputStream out;
	private int protocolLen;
	private int ut_metadata;        //extended message ID
	private int metadata_size;
	private int pieces;
	private String hexHash;

	private byte[] readBuff;

	private int nextSize;
	private NextFunction next;

	private Torrent torrent;

	private static ThreadLocal<PipedStream> cachedBuff = new ThreadLocal<>();

	private byte[] metadata;

	private byte[] peerId;

	/**
	 * 下载完成监听器
	 */
	private Consumer<Torrent> onFinishedListener;

	public void setOnFinishedListener(Consumer<Torrent> listener) {
		this.onFinishedListener = listener;
	}


	public void downloadMetadata(InetSocketAddress address, byte[] peerId, byte[] infoHash) {
		this.peerId = peerId;
		hexHash = ByteUtil.byteArrayToHex(infoHash);
		socket = new Socket();
		try {
			socket.connect(address, Constants.CONNECT_TIMEOUT);
			socket.setSoTimeout(Constants.READ_WRITE_TIMEOUT);
			socket.setSoLinger(true, 0);
			socket.setTcpNoDelay(true);
			socket.setReuseAddress(true);

			in = socket.getInputStream();
			out = socket.getOutputStream();

			setNext(1, onProtocolLen);
			sendHandShake(infoHash);

			map = new HashMap<>();
			readBuff = new byte[256];
			if (cachedBuff.get() == null) {
				cachedBuff.set(new PipedStream());
			}

			int len = -1;
			while (!socket.isClosed() && (len = in.read(readBuff)) != -1) {
				cachedBuff.get().write(readBuff, 0, len);
				handleMessage();
			}
		} catch (Exception e) {
		} finally {
			destroy();
			if (onFinishedListener != null)
				onFinishedListener.accept(torrent);
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
		out.write(peerId);

		out.flush();

	}

	private void handleMessage() throws Exception {
		while (cachedBuff.get().available() >= nextSize) {
			byte[] buff = new byte[nextSize];
			cachedBuff.get().read(buff, 0, nextSize);
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
		byte[] piece_metadata = Arrays.copyOfRange(buff, pos, buff.length);

		Map map = BencodingUtils.decode(str.substring(0, pos).getBytes(StandardCharsets.ISO_8859_1));

		int piece = ((BigInteger) map.get("piece")).intValue();
		System.arraycopy(piece_metadata, 0, this.metadata, piece * 16 * 1024, piece_metadata.length);

		pieces--;

		checkFinished();
	}

	private void checkFinished() {
		if (pieces <= 0) {
			try {
				if (metadata[0] == 'd') {
					Map map = BencodingUtils.decode(metadata);
					if (map != null)
						torrent = parseTorrent(map);
				}
			} catch (Exception e) {
			}
			destroy();
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
	private Torrent parseTorrent(Map map) throws Exception {
		String encoding = null;
		Map<String, Object> info;
		if (map.containsKey("info"))
			info = (Map<String, Object>) map.get("info");
		else
			info = map;

		if (!info.containsKey("name"))
			return null;
		if (map.containsKey("encoding"))
			encoding = (String) map.get("encoding");

		Torrent torrent = new Torrent();

		if (map.containsKey("creation date"))
			torrent.setCreateDate(((BigInteger) map.get("creation date")).longValue());
		else
			torrent.setCreateDate(System.currentTimeMillis());
		byte[] temp;
		if (info.containsKey("name.utf-8")) {
			temp = (byte[]) info.get("name.utf-8");
			encoding = "UTF-8";
		}
		else {
			temp = (byte[]) info.get("name");
			if (encoding == null) {
				encoding = StringUtil.getEncoding(temp);
			}
		}

		torrent.setFileName(new String(temp, encoding));

		if (TorrentDownloadServiceApplication.filterSensitiveWords) {
			if (SensitiveWordsUtil.getInstance().containsAny(torrent.getFileName())) {
				torrent.setIsXxx(1);    //标记敏感资源
			}
		}

		//多文件
		if (info.containsKey("files")) {
			Set<String> types = new HashSet<>();

			List<Map<String, Object>> list = (List<Map<String, Object>>) info.get("files");

			long total = 0;
			int i = 0;
			List<Node> nodes = new ArrayList<>();
			int cur = 1, parent = 0;
			for (Map<String, Object> f : list) {
				long length = ((BigInteger) f.get("length")).longValue();
				total += length;

				Long filesize = null;     //null 表示为文件夹
				boolean uft8 = f.containsKey("path.utf-8");
				List<byte[]> aList = f.containsKey("path.utf-8") ? (List<byte[]>) f.get("path.utf-8") : (List<byte[]>) f.get("path");
				int j = 0;
				for (byte[] bytes : aList) {
					String sname = new String(bytes, uft8 ? "UTF-8" : StringUtil.getEncoding(bytes));
					if (sname.contains("_____padding_file_"))
						continue;
					if (j == aList.size() - 1) {
						filesize = length;
						String type = ExtensionUtil.getExtensionType(sname);
						if (type != null) {
							types.add(type);
						}
					}
					Node node = new Node(cur, j == 0 ? null : parent, sname, filesize, i);
					if (!nodes.contains(node)) {
						nodes.add(node);
						parent = cur;
					} else {
						parent = nodes.get(nodes.indexOf(node)).getNid();
					}
					cur++;
					j++;
				}
				i++;
			}
			Tree tree = new Tree(null);
			tree.createTree(nodes);
			torrent.setFileSize(total);
			torrent.setFiles(JSONUtil.toJSONString(tree));
			if (types.size() <= 0)
				types.add("其他");
			String sType = String.join(",", types);
			if (sType != null && !"".equals(sType)) {
				torrent.setFileType(sType);
			}
		} else {
			torrent.setFileSize(((BigInteger) info.get("length")).longValue());

			String type = ExtensionUtil.getExtensionType(torrent.getFileName());
			if (type != null) {
				torrent.setFileType(type);
			}
		}
		torrent.setInfoHash(hexHash);
		return torrent;
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

		int temp = pieces;
		for (int piece = 0; piece < temp; piece++) {
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
		readBuff = null;
		if (cachedBuff.get() != null) {
			try {
				cachedBuff.get().clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (map != null)
			map.clear();
		metadata = null;
	}


}
