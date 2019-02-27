package cc.dodder.common.entity;

import cc.dodder.common.util.ExtensionUtil;
import cc.dodder.common.util.StringUtil;
import com.alibaba.fastjson.JSON;
import org.eclipse.ecf.protocol.bittorrent.internal.encode.BEncodedDictionary;
import org.eclipse.ecf.protocol.bittorrent.internal.encode.Decode;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/***
 * Torrent 种子文件信息，改自 eclipse ecf torrent 协议 TorrentFile 类
 *
 * @author Mr.Xu
 * @since 2019-02-24 20:23
 **/
public class TorrentFile {

	static MessageDigest shaDigest;
	private final String[] filenames;
	private final long[] lengths;
	private final byte[] torrentData;

	private final BEncodedDictionary dictionary;
	private final String tracker;
	private final String infoHash;
	private File file;
	private String name;
	private long total;
	private Long createDate;
	private String fileType;
	private Tree tree;

	static {
		try {
			shaDigest = MessageDigest.getInstance("SHA-1"); //$NON-NLS-1$
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a new <code>Torrent</code> to analyze the provided torrent
	 * file.
	 *
	 * @param metadata
	 *            the torrent metadata
	 * @throws IllegalArgumentException
	 *             If <code>file</code> is <code>null</code> or a directory
	 * @throws IOException
	 *             If an I/O error occurs whilst analyzing the torrent file
	 */
	public TorrentFile(String metadata, String infoHash) throws IllegalArgumentException, IOException {
		this.infoHash = infoHash;
		dictionary = Decode.bDecode(metadata);
		torrentData = dictionary.toString().getBytes("ISO-8859-1"); //$NON-NLS-1$
		tracker = (String) dictionary.get("announce"); //$NON-NLS-1$

		createDate = (Long) dictionary.get("creation date");
		createDate = createDate == null ? new Date().getTime() : createDate;

		BEncodedDictionary info = (BEncodedDictionary) dictionary.get("info"); //$NON-NLS-1$
		if (info == null)
			info = dictionary;
		final List list = (List) info.get("files"); //$NON-NLS-1$
		String encoding = (String) dictionary.get("encoding");
		byte[] bytes = ((String) info.get("name")).getBytes("ISO-8859-1");
		encoding = encoding == null ? StringUtil.getEncoding(bytes) : encoding;
		name = new String(bytes, encoding);
		Set<String> types = new HashSet<>();
		if (list != null) {
			filenames = new String[list.size()];
			lengths = new long[filenames.length];
			total = 0;
			for (int i = 0; i < filenames.length; i++) {
				final BEncodedDictionary aDictionary = (BEncodedDictionary) list.get(i);
				lengths[i] = ((Long) aDictionary.get("length")).longValue(); //$NON-NLS-1$
				total += lengths[i];
				final List aList = (List) aDictionary.get("path"); //$NON-NLS-1$

				final StringBuffer buffer = new StringBuffer();
				synchronized (buffer) {
					for (int j = 0; j < aList.size(); j++) {
						String sname = new String(((String)aList.get(j)).getBytes("ISO-8859-1"),encoding);
						buffer.append(sname).append(File.separator);
					}
				}
				filenames[i] = buffer.toString();
				String type = ExtensionUtil.getExtensionType(filenames[i]);
				if (type != null) {
					types.add(type);
				}
			}
		} else {
			lengths = new long[] {((Long) info.get("length")).longValue()}; //$NON-NLS-1$
			total = lengths[0];
			filenames = new String[] {name}; //$NON-NLS-1$
			String type = ExtensionUtil.getExtensionType(filenames[0]);
			if (type != null) {
				types.add(type);
			}
		}
		if (types.size() <= 0)
			types.add("其他");
		String sType = String.join(",", types);
		if (sType != null && !"".equals(sType)) {
			this.fileType = sType;
		} else {
			this.fileType = "其他";
		}

		//将文件列表转为树形结构
		List<Long> sizes = new ArrayList<>();
		List<String> names = new ArrayList<>();
		List<Integer> indexes = new ArrayList<>();
		//过滤无效文件
		for (int i = 0; i < filenames.length; i++) {
			if (!filenames[i].contains("_____padding_file_")) {
				names.add(filenames[i]);
				sizes.add(lengths[i]);
				indexes.add(i);
			}
		}
		if (names.size() >0 && names.size() == sizes.size()) {
			List<Node> nodes = new ArrayList<>();
			int cur = 1, parent = 0;
			for (int j = 0; j < names.size(); j++) {
				String[] arr = names.get(j).split("\\\\");
				if (arr.length == 1)
					arr = names.get(j).split("/");
				for (int i = 0; i < arr.length; i++) {
					String filename = arr[i];
					Long filesize = null;     //null 表示为文件夹
					if (i == arr.length - 1) {      //叶子节点，即文件，包含文件大小
						filesize = sizes.get(j);
					}
					Node node = new Node(cur, i == 0 ? 0 : parent, filename, filesize, j);
					if (!nodes.contains(node)) {
						nodes.add(node);
						parent = cur;
					} else {
						parent = nodes.get(nodes.indexOf(node)).getNid();
					}
					cur++;
				}
			}
			tree = new Tree(name);
			tree.createTree(nodes);
		}
	}

	public Torrent toEntity() {
		if (tree == null)
			return null;
		Torrent torrent = Torrent.builder()
				.infoHash(infoHash)
				.fileType(fileType)
				.filesize(total)
				.createDate(createDate)
				.files(JSON.toJSONString(tree))
				.build();
		return torrent;
	}
}
