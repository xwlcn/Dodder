package cc.dodder.dhtserver.netty.handler;

import cc.dodder.common.entity.DownloadMsgInfo;
import cc.dodder.common.util.ByteUtil;
import cc.dodder.common.util.NodeIdUtil;
import cc.dodder.common.util.bencode.BencodingUtils;
import cc.dodder.dhtserver.netty.DHTServer;
import cc.dodder.dhtserver.netty.entity.Node;
import cc.dodder.dhtserver.netty.entity.UniqueBlockingQueue;
import cc.dodder.dhtserver.stream.MessageStreams;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/***
 * 参见 Bittorrent 协议：
 * http://www.bittorrent.org/beps/bep_0005.html
 *
 * @author Mr.Xu
 **/
@Slf4j
@Component
@ChannelHandler.Sharable
public class DHTServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

	@Autowired
	private DHTServer dhtServer;

	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private MessageStreams messageStreams;



	public static final UniqueBlockingQueue NODES_QUEUE = new UniqueBlockingQueue();

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {

		byte[] buff = new byte[packet.content().readableBytes()];
		packet.content().readBytes(buff);

		Map<String, ?> map = BencodingUtils.decode(buff);

		if (map == null || map.get("y") == null)
			return;

		String y = new String((byte[]) map.get("y"));

		if ("q".equals(y)) {            //请求 Queries
			onQuery(map, packet.sender());
		} else if ("r".equals(y)) {     //回复 Responses
			onResponse(map, packet.sender());
		}

	}

	/**
	 * 解析查询请求
	 *
	 * @param map
	 * @param sender
	 */
	private void onQuery(Map<String, ?> map, InetSocketAddress sender) {
		//transaction id 会话ID
		byte[] t = (byte[]) map.get("t");
		//query name: ping, find node, get_peers, announce_peer
		String q = new String((byte[]) map.get("q"));
		//query params
		Map<String, ?> a = (Map<String, ?>) map.get("a");
		//log.info("on query, query name is {}", q);
		switch (q) {
			case "ping"://ping Query = {"t":"aa", "y":"q", "q":"ping", "a":{"id":"发送者ID"}}
				responsePing(t, sender);
				break;
			case "find_node"://find_node Query = {"t":"aa", "y":"q", "q":"find_node", "a": {"id":"abcdefghij0123456789", "target":"mnopqrstuvwxyz123456"}}
				responseFindNode(t, sender);
				break;
			case "get_peers"://get_peers Query = {"t":"aa", "y":"q", "q":"get_peers", "a": {"id":"abcdefghij0123456789", "info_hash":"mnopqrstuvwxyz123456"}}
				responseGetPeers(t, (byte[]) a.get("info_hash"), sender);
				break;
			case "announce_peer"://announce_peers Query = {"t":"aa", "y":"q", "q":"announce_peer", "a": {"id":"abcdefghij0123456789", "implied_port": 1, "info_hash":"mnopqrstuvwxyz123456", "port": 6881, "token": "aoeusnth"}}
				responseAnnouncePeer(t, a, sender);
				break;
		}
	}

	/**
	 * 回复 ping 请求
	 * Response = {"t":"aa", "y":"r", "r": {"id":"自身节点ID"}}
	 *
	 * @param t
	 * @param sender
	 */
	private void responsePing(byte[] t, InetSocketAddress sender) {
		Map r = new HashMap<String, Object>();
		r.put("id", DHTServer.SELF_NODE_ID);
		DatagramPacket packet = createPacket(t, "r", r, sender);
		dhtServer.sendKRPC(packet);
		//log.info("response ping[{}]", sender);
	}

	/**
	 * 回复 find_node 请求, 由于是模拟的 DHT 节点，所以直接回复一个空的 node 集合即可
	 * Response = {"t":"aa", "y":"r", "r": {"id":"0123456789abcdefghij", "nodes": "def456..."}}
	 *
	 * @param t
	 * @param sender
	 */
	private void responseFindNode(byte[] t, InetSocketAddress sender) {
		HashMap<String, Object> r = new HashMap<>();
		r.put("id", DHTServer.SELF_NODE_ID);
		r.put("nodes", new byte[]{});
		DatagramPacket packet = createPacket(t, "r", r, sender);
		dhtServer.sendKRPC(packet);
		//log.info("response find_node[{}]", sender);
	}

	/**
	 * 回复 get_peers 请求，必须回复，不然收不到 announce_peer 请求
	 * Response with closest nodes = {"t":"aa", "y":"r", "r": {"id":"abcdefghij0123456789", "token":"aoeusnth", "nodes": "def456..."}}
	 *
	 * @param t
	 * @param sender
	 */
	private void responseGetPeers(byte[] t, byte[] info_hash, InetSocketAddress sender) {
		HashMap<String, Object> r = new HashMap<>();
		r.put("token", new byte[]{info_hash[0], info_hash[1]});
		r.put("nodes", new byte[]{});
		r.put("id", NodeIdUtil.getNeighbor(DHTServer.SELF_NODE_ID, info_hash));
		DatagramPacket packet = createPacket(t, "r", r, sender);
		dhtServer.sendKRPC(packet);
		//log.info("response get_peers[{}]", sender);
	}

	/**
	 * 回复 announce_peer 请求，该请求中包含了对方正在下载的 torrent 的 info_hash 以及 端口号
	 * Response = {"t":"aa", "y":"r", "r": {"id":"mnopqrstuvwxyz123456"}}
	 *
	 * @param t
	 * @param a      请求参数 a：
	 *               {
	 *               "id" : "",
	 *               "implied_port": <0 or 1>,    //为1时表示当前自身的端口就是下载端口
	 *               "info_hash" : "<20-byte infohash of target torrent>",
	 *               "port" : ,
	 *               "token" : "" //get_peer 中回复的 token，用于检测是否一致
	 *               }
	 * @param sender
	 */
	private void responseAnnouncePeer(byte[] t, Map a, InetSocketAddress sender) {

		byte[] info_hash = (byte[]) a.get("info_hash");
		byte[] token = (byte[]) a.get("token");
		int port;
		if (a.containsKey("implied_port") && ((BigInteger) a.get("implied_port")).shortValue() != 0) {
			port = sender.getPort();
		} else {
			port = ((BigInteger) a.get("port")).intValue();
		}

		HashMap<String, Object> r = new HashMap<>();
		r.put("id", NodeIdUtil.getNeighbor(DHTServer.SELF_NODE_ID, info_hash));
		DatagramPacket packet = createPacket(t, "r", r, sender);
		dhtServer.sendKRPC(packet);
		// 将 info_hash 放进消息队列
		if (token.length == 2 && info_hash[0] == token[0] && info_hash[1] == token[1]) {    //check token
			/*byte[] rightByte = new byte[10];
			System.arraycopy(info_hash, 10, rightByte,0, 10);
			byte[] key = Arrays.concatenate(sender.getHostString().getBytes(), rightByte);*/
			//使用 redis 将消息队列去重
			if (redisTemplate.hasKey(info_hash))
				return;
			/*if (redisTemplate.hasKey(info_hash) || redisTemplate.opsForValue().getAndSet(key, new byte[0]) != null) {
				return;
			}
			redisTemplate.expire(key, 5, TimeUnit.MINUTES);*/
			log.error("info_hash[AnnouncePeer] : {}:{} - {}", sender.getHostString(), port, ByteUtil.byteArrayToHex(info_hash));
			messageStreams.downloadMessageOutput()
					.send(MessageBuilder
							.withPayload(new DownloadMsgInfo(sender.getHostString(), port, info_hash))
							.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
							.build());
		}
	}

	/**
	 * 处理对方响应内容，由于我们只主动给对方发送了 find_node 请求，所以只会收到 find_node 的回复进行解析即可
	 * 解析出响应的节点列表再次给这些节点发送 find_node 请求，即可无限扩展与新的节点保持通讯（即把自己的节点加入到对方的桶里，
	 * 欺骗对方让对方给自己发送 announce_peer 请求，这样一来我们就可以抓取 DHT 网络中别人正在下载的种子文件信息）
	 *
	 * @param map
	 * @param sender
	 */
	private void onResponse(Map<String, ?> map, InetSocketAddress sender) {
		//transaction id
		byte[] t = (byte[]) map.get("t");
		//由于在我们发送查询 DHT 节点请求时，构造的查询 transaction id 为字符串 find_node（见 findNode 方法），所以根据字符串判断响应请求即可
		String type = new String(t);
		if ("find_node".equals(type)) {
			resolveNodes((Map) map.get("r"));
		} else if ("ping".equals(type)) {

		} else if ("get_peers".equals(type)) {

		} else if ("announce_peer".equals(type)) {

		}
	}

	/**
	 * 解析响应内容中的 DHT 节点信息
	 *
	 * @param r
	 */
	private void resolveNodes(Map r) {

		byte[] nodes = (byte[]) r.get("nodes");

		if (nodes == null)
			return;

		for (int i = 0; i < nodes.length; i += 26) {
			try {
				InetAddress ip = InetAddress.getByAddress(new byte[]{nodes[i + 20], nodes[i + 21], nodes[i + 22], nodes[i + 23]});
				InetSocketAddress address = new InetSocketAddress(ip, (0x0000FF00 & (nodes[i + 24] << 8)) | (0x000000FF & nodes[i + 25]));
				byte[] nid = new byte[20];
				System.arraycopy(nodes, i, nid, 0, 20);
				NODES_QUEUE.offer(new Node(nid, address));
				//log.info("get node address=[{}] ", address);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	/**
	 * 加入 DHT 网络
	 */
	public void joinDHT() {
		for (InetSocketAddress addr : DHTServer.BOOTSTRAP_NODES) {
			findNode(addr, null, DHTServer.SELF_NODE_ID);
		}
	}

	/**
	 * 发送查询 DHT 节点请求
	 *
	 * @param address 请求地址
	 * @param nid     请求节点 ID
	 * @param target  目标查询节点
	 */
	private void findNode(InetSocketAddress address, byte[] nid, byte[] target) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("target", target);
		if (nid != null)
			map.put("id", NodeIdUtil.getNeighbor(DHTServer.SELF_NODE_ID, nid));
		DatagramPacket packet = createPacket("find_node".getBytes(), "q", map, address);
		dhtServer.sendKRPC(packet);
	}

	/**
	 * 构造 KRPC 协议数据
	 *
	 * @param t
	 * @param y
	 * @param arg
	 * @return
	 */
	private DatagramPacket createPacket(byte[] t, String y, Map<String, Object> arg, InetSocketAddress address) {
		HashMap<String, Object> map = new HashMap<>();
		map.put("t", t);
		map.put("y", y);
		if (!arg.containsKey("id"))
			arg.put("id", DHTServer.SELF_NODE_ID);

		if (y.equals("q")) {
			map.put("q", t);
			map.put("a", arg);
		} else {
			map.put("r", arg);
		}
		byte[] buff = BencodingUtils.encode(map);
		DatagramPacket packet = new DatagramPacket(Unpooled.copiedBuffer(buff), address);
		return packet;
	}

	/**
	 * 查询 DHT 节点线程，用于持续获取新的 DHT 节点
	 *
	 * @date 2019/2/17
	 **/
	private Thread findNodeTask = new Thread() {

		@Override
		public void run() {
			try {
				while (!isInterrupted()) {
					try {
						Node node = NODES_QUEUE.poll();
						if (node != null) {
							findNode(node.getAddr(), node.getNodeId(), NodeIdUtil.createRandomNodeId());
						}
					} catch (Exception e) {
					}
					Thread.sleep(50);
				}
			} catch (InterruptedException e) {
			}
		}
	};

	@PostConstruct
	public void init() {
		findNodeTask.start();
	}

	@PreDestroy
	public void stop() {
		findNodeTask.interrupt();
	}
}
