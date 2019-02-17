package cc.dodder.dhtserver.netty.config;

import cc.dodder.dhtserver.netty.handler.DHTServerHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("channelInitializer")
public class DHTChannelInitializer extends ChannelInitializer<DatagramChannel> {

	private static int TIMEOUT = 1 * 60;

	@Autowired
	private DHTServerHandler dhtServerHandler;

	@Override
	protected void initChannel(DatagramChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		/*pipeline.addLast(
				new ReadTimeoutHandler(TIMEOUT),
				new WriteTimeoutHandler(TIMEOUT));*/
		pipeline.addLast("handler", dhtServerHandler);
	}
}
