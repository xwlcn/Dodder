package cc.dodder.common.util;


import org.apache.commons.codec.digest.PureJavaCrc32C;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

/***
 * NodeId Util
 *
 * @author Mr.Xu
 * @simce 2019-02-15 17:36
 **/
public class NodeIdUtil {

	private static MessageDigest messageDigest;
	private static Random random;
	static
	{
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		random = new Random(new Date().getTime());
	}

	/**
	 * See https://libtorrent.org/dht_sec.html
	 *
	 * @return byte[] node id
	 */
	public static byte[] randSelfNodeId() {
		long ip = NetworkUtil.getIp();
		return getNodeIdByIp(ip);
	}

	/**
	 * 根据安全扩展协议生成 node_id，规则：保持前三个与最后一个字节不变，中间的从其他节点 ID 中复制
	 * 用于确保 find_node 与其他回复中的 id 保持一致，并且对外每个节点回复不同的 ID
	 *
	 * @param selfId		//第一次初始时的 node_id
	 * @param nodeId		//其他节点的 id
	 * @return byte[] node id
	 */
	public static byte[] makeSelfId(byte[] selfId, byte[] nodeId) {
		byte[] bytes = new byte[20];
		bytes[0] = selfId[0];
		bytes[1] = selfId[1];
		bytes[2] = selfId[2];
		System.arraycopy(nodeId, 3, bytes, 3, 16);
		bytes[19] = selfId[19];
		return bytes;
	}

	/**
	 * generate random node_id
	 *
	 * @return byte[] node id
	 */
	public static byte[] createRandomNodeId() {
		byte[] bytes = new byte[20];
		for (int i = 0; i < bytes.length; i++)
		{
			bytes[i] = (byte)random.nextInt(256);
		}
		messageDigest.update(bytes);
		return messageDigest.digest();
	}

	/**
	 * distance: node1 XOR node2
	 *
	 * @param node1
	 * @param node2
	 * @return node id which closeness to node2
	 */
	public static byte[] getNeighbor(byte[] node1, byte[] node2) {
		byte[] bytes = new byte[20];
		System.arraycopy(node2, 0, bytes, 0, 18);
		System.arraycopy(node1, 18, bytes, 18, 2);
		return bytes;
	}

	private static byte[] getNodeIdByIp(long ip) {
		int rand = new Random().nextInt(256);
		int r = rand & 0x7;

		PureJavaCrc32C crc32C = new PureJavaCrc32C();
		crc32C.update(ByteUtil.intToByteArray((int)(ip & 0x030f3fff) | (r << 29)), 0, 4);
		long crc = crc32C.getValue();

		byte[] node_id = new byte[20];
		node_id[0] = (byte)((crc >> 24) & 0xff);
		node_id[1] = (byte)((crc >> 16) & 0xff);
		node_id[2] = (byte)(((crc >> 8) & 0xf8) | (rand & 0x7));
		for (int i = 3; i < 19; ++i)
			node_id[i] = (byte)random.nextInt(256);
		node_id[19] = (byte)rand;

		return node_id;
	}

}
