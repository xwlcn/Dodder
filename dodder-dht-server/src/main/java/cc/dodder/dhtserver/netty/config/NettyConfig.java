package cc.dodder.dhtserver.netty.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/***
 * Netty 服务器配置
 *
 * @author: Mr.Xu
 * @create: 2019-02-15 14:50
 **/
@Configuration
@ConfigurationProperties(prefix = "netty")
public class NettyConfig {

	@Value("${netty.group.thread.count}")
	private int groupCount;
	@Value("${netty.tcp.port}")
	private int udpPort;
	@Value("${netty.so.keepalive}")
	private boolean keepAlive;
	@Value("${netty.so.backlog}")
	private int backlog;
	@Value("${netty.so.rcvbuf}")
	private int rcvbuf;
	@Value("${netty.so.sndbuf}")
	private int sndbuf;


	@Autowired
	@Qualifier("channelInitializer")
	private ChannelInitializer channelInitializer;

	@Bean(name = "serverBootstrap")
	public Bootstrap bootstrap() {
		Bootstrap b = new Bootstrap();
		b.group(group())
				.channel(NioDatagramChannel.class)
				.handler(channelInitializer);
		Map<ChannelOption<?>, Object> tcpChannelOptions = udpChannelOptions();
		Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
		for (@SuppressWarnings("rawtypes")
				ChannelOption option : keySet) {
			b.option(option, tcpChannelOptions.get(option));
		}
		return b;
	}

	@Bean(name = "group", destroyMethod = "shutdownGracefully")
	public EventLoopGroup group() {
		return new NioEventLoopGroup(groupCount);
	}

	@Bean(name = "udpSocketAddress")
	public InetSocketAddress udpPort() {
		return new InetSocketAddress(udpPort);
	}

	@Bean(name = "udpChannelOptions")
	public Map<ChannelOption<?>, Object> udpChannelOptions() {
		Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
		//options.put(ChannelOption.SO_BACKLOG, backlog);
		options.put(ChannelOption.SO_RCVBUF, rcvbuf);
		options.put(ChannelOption.SO_SNDBUF, sndbuf);
		return options;
	}
}
