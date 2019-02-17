package cc.dodder.dhtserver.netty.codec;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.Map;

/***
 * DHT 协议解析
 *
 * @author: Mr.Xu
 * @create: 2019-02-15 15:10
 **/
public class DHTProtocolDecoder extends MessageToMessageDecoder<DatagramPacket> {

	private Bencode bencode = new Bencode();

	@Override
	protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
		Map<String, Object> map = bencode.decode(datagramPacket.content().array(), Type.DICTIONARY);
		list.add(map);
	}
}
