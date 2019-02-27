package cc.dodder.common.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Node {

	private int nid; // 父亲id
	private int pid;
	private String filename = "";
	private Long filesize;
	private int index;

	private List<Node> children;
	
	public void addChild(Node node) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(node);
	}

	public Node() {

	}

	public Node(int nid, int pid) {
		super();
		this.nid = nid;
		this.pid = pid;
	}
	
	public Node(int nid, int pid, String filename, Long filesize, int index) {
		super();
		this.nid = nid;
		this.pid = pid;
		this.filename = filename;
		this.filesize = filesize;
		this.index = index;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Node node = (Node) o;

		if (!filename.equals(node.filename)) return false;
		return filesize.equals(node.filesize);
	}

	@Override
	public int hashCode() {
		int result = filename.hashCode();
		result = 31 * result + filesize.hashCode();
		return result;
	}
}
