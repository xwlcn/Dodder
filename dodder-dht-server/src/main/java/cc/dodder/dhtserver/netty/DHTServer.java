package cc.dodder.dhtserver.netty;


import cc.dodder.common.util.BloomFilter;
import cc.dodder.common.util.NodeIdUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/***
 * 模拟 DHT 节点服务器
 *
 * @author Mr.Xu
 * @date 2019-02-15 14:44
 **/
@Slf4j
@Component
public class DHTServer {

	@Autowired
	@Qualifier("serverBootstrap")
	private Bootstrap b;

	@Autowired
	@Qualifier("udpSocketAddress")
	private InetSocketAddress udpPort;

	private ChannelFuture serverChannelFuture;

	public BloomFilter bloomFilter;
	private String filterSavePath;

	/**
	 * 本机 DHT 节点 ID （根据 IP 生成）
	 */
	public static final byte[] SELF_NODE_ID = NodeIdUtil.randSelfNodeId();

	public static final int SECRET = 888;

	/**
	 * 启动节点列表
	 */
	public static final List<InetSocketAddress> BOOTSTRAP_NODES = new ArrayList<>(Arrays.asList(
			new InetSocketAddress("router.bittorrent.com", 6881),
			new InetSocketAddress("dht.transmissionbt.com", 6881),
			new InetSocketAddress("router.utorrent.com", 6881),
			new InetSocketAddress("dht.aelitis.com", 6881)));

	/**
	 * 随 SpringBoot 启动 DHT 服务器
	 *
	 * @throws Exception
	 */
	@PostConstruct
	public void start() throws Exception {
		log.info("Starting dht server at " + udpPort);
		serverChannelFuture = b.bind(udpPort).sync();
		serverChannelFuture.channel().closeFuture();

		//init bloom filter
		/*bloomFilter = new BloomFilter(10000000);
		String path = System.getProperty("java.class.path");
		int firstIndex = path.lastIndexOf(System.getProperty("path.separator")) + 1;
		int lastIndex = path.lastIndexOf(File.separator) + 1;
		filterSavePath = path.substring(firstIndex, lastIndex) + "filter.data";
		File file = new File(filterSavePath);
		if (file.exists())
			BloomFilter.readFilterFromFile(filterSavePath);*/
	}

	/*@PreDestroy
	public void saveBloomFilter() {
		bloomFilter.saveFilterToFile(filterSavePath);
	}*/

	/**
	 * 发送 KRPC 协议数据报文
	 *
	 * @param packet
	 */
	public void sendKRPC(DatagramPacket packet) {
		serverChannelFuture.channel().writeAndFlush(packet);
	}
}
