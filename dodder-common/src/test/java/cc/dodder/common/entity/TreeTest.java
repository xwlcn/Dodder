package cc.dodder.common.entity;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class TreeTest {

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream output = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }

    @After
    public void restoreStreams() {
        System.setOut(output);
    }

    @Test
    public void testCheckExist() {
        final Tree tree = new Tree();
        final Node node = new Node();
        node.addChild(new Node());

        assertFalse(tree.checkExist(node, null));
    }

    @Test
    public void testConstructor() {
        final Tree tree = new Tree("foo");

        assertEquals(101605, tree.getRoot().hashCode());
        assertEquals(-1, tree.getRoot().getIndex());
        assertEquals(-1, tree.getRoot().getPid());
        assertEquals(0, tree.getRoot().getNid());
        assertEquals("foo", tree.getRoot().getFilename());

        assertNull(((Node)tree.getRoot()).getChildren());
        assertNull(tree.getRoot().getFilesize());
    }

    @Test
    public void testCreateTree() {
        final Tree tree = new Tree();
        final ArrayList<Node> nodes = new ArrayList<>();
        tree.setRoot(new Node());
        nodes.add(new Node(0, 0, "foo", 0l, 0));
        nodes.add(new Node(1, 1, "bar", 0l, 1));
        tree.createTree(nodes);

        assertEquals(new Node(0, 0, "foo", 0l, 0), tree.getRoot().getChildren().get(0));
    }

    @Test
    public void testGetHtmlNoChildren() {
        final Tree tree = new Tree();
        tree.setRoot(new Node(0, 0, "a\'b\'c", 0l, 0));

        assertEquals(
                "<li><span class=\"file\">a'b'c<small>(0 B)</small></span></li>",
                tree.getHtml(new Node(0, 0, "a\'b\'c", 0l, 0)));
    }

    @Test
    public void testGetHtmlMatchingPid() {
        final Tree tree = new Tree();
        tree.setRoot(new Node(0, 0, "a\'b\'c", 0l, 0));
        final Node node = new Node(0, 0, "a/b/c", 0l, 0);
        node.setChildren(new ArrayList<Node>());

        assertEquals(
                "<ul class=\"filetree treeview\"><p><span class=\"bticon\">a\'b\'c</span></p></ul></li>",
                tree.getHtml(node));
    }

    @Test
    public void testGetHtmlNotMatchingPid() {
        final Tree tree = new Tree();
        tree.setRoot(new Node(0, 0, "a\'b\'c", 0l, 0));
        final Node node = new Node(-2_147_483_648, 0, "a/b/c", 0l, 0);
        node.setChildren(new ArrayList<Node>());

        assertEquals("<li class=\"closed\"><span class=\"folder\">a/b/c</span><ul></ul></li>",
                tree.getHtml(node));
    }

    @Test
    public void testGetLeafList1() {
        final Tree tree = new Tree();
        tree.setRoot(new Node(0, 0, "foo", 0l, 0));
        final List<Node> nodes = tree.getLeafList();

        assertEquals(1, nodes.size());
        assertEquals(0, nodes.get(0).getIndex());
        assertEquals(0, nodes.get(0).getPid());
        assertEquals(0, nodes.get(0).getNid());
        assertEquals("foo", nodes.get(0).getFilename());
    }

    @Test
    public void testGetLeafList2() {
        final Tree tree = new Tree();
        final Node node = new Node();
        node.setChildren(new ArrayList());
        tree.setRoot(node);

        assertEquals(new ArrayList<Node>(), tree.getLeafList());
    }

    @Test
    public void testMiddlePrint() {
        final Tree tree = new Tree();
        tree.setRoot(null);
        final Node node = new Node();
        final ArrayList<Node> arrayList = new ArrayList<Node>();
        arrayList.add(new Node(0, 0, "foo", 0l, 0));
        arrayList.add(new Node(0, 0, "bar", 0l, 0));
        node.setChildren(arrayList);
        tree.middlePrint(node);

        assertEquals("foo\nbar\n", outputStream.toString());
    }
}
