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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filename == null) ? 0 : filename.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		return true;
	}
}
