package cc.dodder.dhtserver.netty.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * 去重的节点阻塞队列
 *
 * @author: Mr.Xu
 * @create: 2019-02-17 18:44
 **/
public class UniqueBlockingQueue {

	private Set<String> ips = new HashSet<>();
	private BlockingQueue<Node> nodes = new LinkedBlockingQueue<>(1024);

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public boolean offer(Node node) {
		if (ips.add(node.getAddr().getHostString()))
			return nodes.offer(node);
		return false;
	}

	public Node poll() {
		Node node = nodes.poll();
		if (node != null)
			ips.remove(node.getAddr().getHostString());
		return node;
	}
}
