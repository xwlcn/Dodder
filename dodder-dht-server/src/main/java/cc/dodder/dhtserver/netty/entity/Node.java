package cc.dodder.dhtserver.netty.entity;

import lombok.Data;

import java.net.InetSocketAddress;

/***
 * DHT 节点
 *
 * @author: Mr.Xu
 * @create: 2019-02-17 14:26
 **/
@Data
public class Node {

	private byte[] nodeId;
	private InetSocketAddress addr;

	public Node(byte[] nodeId, InetSocketAddress addr) {
		this.nodeId = nodeId;
		this.addr = addr;
	}
}
