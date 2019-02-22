package cc.dodder.dhtserver.netty;


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
 * @since 2019-02-15 14:44
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

	/**
	 * 本机 DHT 节点 ID
	 */
	public static final byte[] SELF_NODE_ID = NodeIdUtil.createRandomNodeId();

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
	}

	/**
	 * 发送 KRPC 协议数据报文
	 *
	 * @param packet
	 */
	public void sendKRPC(DatagramPacket packet) {
		serverChannelFuture.channel().writeAndFlush(packet);
	}
}
