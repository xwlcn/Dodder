package cc.dodder.common.entity;


import cc.dodder.common.util.FileTypeUtil;
import cc.dodder.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Tree {

	private Node root;

	private List<Node> leaves;
	
	public Tree() {
		super();
	}

	public Tree(String text) {
		root = new Node(0, -1, text, null, -1);
	}
	
	public void createTree(List<Node> nodes) {
		for (Node node : nodes) {
			//父亲是根节点，直接添加到根节点下面
			if (node.getPid() == root.getNid()) {
				root.addChild(node);
			} else {	//父亲是其他节点
				Node parent = findParent(root, node.getPid());
				if (parent != null) {
					parent.addChild(node);
				}
			}
		}
	}
	
	private Node findParent(Node node, int pid) {
		Node result = null;
		for (Node n : node.getChildren()) {
			if (n.getNid() == pid) {
				return n;
			} else {
				//递归搜索
				if (n.getChildren() != null)
					result =  findParent(n, pid);
			}
		}
		return result;
	}
	
	public void middlePrint(Node tnode) {
		if (tnode.getChildren() == null) {
			return;
		}
		for (Node node : tnode.getChildren()) {
			System.out.println(node.getFilename());
			middlePrint(node);
		}
	}

	/**
	 * 构建叶子节点数组，实际上就是构建子文件列表
	 * @return
	 */
	public List<Node> getLeafList() {
		leaves = new ArrayList<>();
		deep(root);
		return leaves;
	}

	private void deep(Node tnode) {
		if (tnode.getChildren() == null) {      //叶子节点
			if (leaves.size() < 3)
				leaves.add(tnode);
			return;
		}
		if (leaves.size() >= 3)
			return;
		for (Node node : tnode.getChildren()) {
			deep(node);
		}
	}
	
	public String getHtml(Node tnode) {
		
		if (tnode.getChildren() == null) {	//叶子节点
			return "<li><span class=\"" + FileTypeUtil.getFileType(tnode.getFilename()) + "\">" + tnode.getFilename()
					+ "<small>(" + StringUtil.formatSize(tnode.getFilesize()) + ")" + "</small>"
					+ "</span></li>";
		}
		
		String str = "";
		if (tnode.getNid() == root.getNid()) {	//根节点
			str += "<ul class=\"filetree treeview\"><p><span class=\"bticon\">" + root.getFilename() + "</span></p>";
		} else {				//子节点
			str += "<li class=\"closed\"><span class=\"folder\">" + tnode.getFilename() + "</span><ul>";
		}
		
		for (Node node : tnode.getChildren()) {
			str += getHtml(node);
		}
		
		if (tnode == root) {	//根节点
			return str += "</ul>";
		} else {				//子节点
			return str += "</ul></li>";
		}
	}
	
	public boolean checkExist(Node tnode, String text) {
		boolean exist = false;
		if (tnode.getChildren() == null) {
			return false;
		}
		for (Node node : tnode.getChildren()) {
			exist |= checkExist(node, text);
		}
		return exist;
	}

	public Node getRoot() {
		return root;
	}

	public void setRoot(Node root) {
		this.root = root;
	}
	
	
}
