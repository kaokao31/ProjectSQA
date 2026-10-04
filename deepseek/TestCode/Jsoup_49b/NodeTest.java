package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class NodeTest {

    private TestNode parent;
    private TestNode child1;
    private TestNode child2;
    private TestNode child3;

    // Concrete implementation of Node for testing
    private static class TestNode extends Node {
        private String name;
        private String head;
        private String tail;

        TestNode(String name, String head, String tail) {
            super();
            this.name = name;
            this.head = head;
            this.tail = tail;
        }

        @Override
        public String nodeName() {
            return name;
        }

        @Override
        protected void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append(head);
        }

        @Override
        protected void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
            accum.append(tail);
        }

        // Expose protected methods for testing
        @Override
        public Node doClone(Node parent) {
            return super.doClone(parent);
        }

        @Override
        public List<Node> childNodesCopy() {
            return super.childNodesCopy();
        }
    }

    @Before
    public void setUp() {
        parent = new TestNode("parent", "<p>", "</p>");
        child1 = new TestNode("child1", "<a>", "</a>");
        child2 = new TestNode("child2", "<b>", "</b>");
        child3 = new TestNode("child3", "<c>", "</c>");
    }

    // --- childNodeSize() ---
    @Test
    public void testChildNodeSizeEmpty() {
        assertEquals(0, parent.childNodeSize());
    }

    @Test
    public void testChildNodeSizeAfterAdd() {
        parent.addChildren(child1);
        assertEquals(1, parent.childNodeSize());
        parent.addChildren(child2, child3);
        assertEquals(3, parent.childNodeSize());
    }

    // --- childNode(int) ---
    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNodeNegativeIndex() {
        parent.addChildren(child1);
        parent.childNode(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNodeIndexTooHigh() {
        parent.addChildren(child1);
        parent.childNode(1);
    }

    @Test
    public void testChildNodeValid() {
        parent.addChildren(child1, child2);
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
    }

    // --- addChildren(Node...) ---
    @Test(expected = IllegalArgumentException.class)
    public void testAddChildrenNull() {
        parent.addChildren((Node) null);
    }

    @Test
    public void testAddChildrenMultiple() {
        parent.addChildren(child1, child2);
        assertEquals(2, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
    }

    // --- addChildren(int, Node...) ---
    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddChildrenAtIndexNegative() {
        parent.addChildren(-1, child1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddChildrenAtIndexTooHigh() {
        parent.addChildren(1, child1);
    }

    @Test
    public void testAddChildrenAtIndexBeginning() {
        parent.addChildren(child2);
        parent.addChildren(0, child1);
        assertEquals(2, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
    }

    @Test
    public void testAddChildrenAtIndexMiddle() {
        parent.addChildren(child1, child3);
        parent.addChildren(1, child2);
        assertEquals(3, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
        assertSame(child3, parent.childNode(2));
    }

    @Test
    public void testAddChildrenAtIndexEnd() {
        parent.addChildren(child1);
        parent.addChildren(1, child2);
        assertEquals(2, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
    }

    // --- removeChild(Node) ---
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNull() {
        parent.removeChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNotPresent() {
        parent.removeChild(child1);
    }

    @Test
    public void testRemoveChildSingle() {
        parent.addChildren(child1);
        parent.removeChild(child1);
        assertEquals(0, parent.childNodeSize());
        assertNull(child1.parentNode());
    }

    @Test
    public void testRemoveChildFirst() {
        parent.addChildren(child1, child2, child3);
        parent.removeChild(child1);
        assertEquals(2, parent.childNodeSize());
        assertSame(child2, parent.childNode(0));
        assertSame(child3, parent.childNode(1));
    }

    @Test
    public void testRemoveChildMiddle() {
        parent.addChildren(child1, child2, child3);
        parent.removeChild(child2);
        assertEquals(2, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
        assertSame(child3, parent.childNode(1));
    }

    @Test
    public void testRemoveChildLast() {
        parent.addChildren(child1, child2, child3);
        parent.removeChild(child3);
        assertEquals(2, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
        assertSame(child2, parent.childNode(1));
    }

    // --- replaceChild(Node, Node) ---
    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNullOld() {
        parent.replaceChild(null, child1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNullNew() {
        parent.addChildren(child1);
        parent.replaceChild(child1, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNotPresent() {
        parent.replaceChild(child1, child2);
    }

    @Test
    public void testReplaceChild() {
        parent.addChildren(child1);
        parent.replaceChild(child1, child2);
        assertEquals(1, parent.childNodeSize());
        assertSame(child2, parent.childNode(0));
        assertNull(child1.parentNode());
        assertSame(parent, child2.parentNode());
    }

    // --- parentNode() / setParentNode() ---
    @Test
    public void testParentNodeInitiallyNull() {
        assertNull(child1.parentNode());
    }

    @Test
    public void testSetParentNode() {
        child1.setParentNode(parent);
        assertSame(parent, child1.parentNode());
    }

    @Test
    public void testParentNodeAfterAdd() {
        parent.addChildren(child1);
        assertSame(parent, child1.parentNode());
    }

    // --- outerHtml() ---
    @Test
    public void testOuterHtmlNoChildren() {
        assertEquals("<p></p>", parent.outerHtml());
    }

    @Test
    public void testOuterHtmlOneChild() {
        parent.addChildren(child1);
        assertEquals("<p><a></a></p>", parent.outerHtml());
    }

    @Test
    public void testOuterHtmlMultipleChildren() {
        parent.addChildren(child1, child2);
        assertEquals("<p><a></a><b></b></p>", parent.outerHtml());
    }

    @Test
    public void testOuterHtmlNestedChildren() {
        TestNode inner = new TestNode("inner", "<span>", "</span>");
        child1.addChildren(inner);
        parent.addChildren(child1);
        assertEquals("<p><a><span></span></a></p>", parent.outerHtml());
    }

    @Test
    public void testOuterHtmlChildWithEmptyHeadTail() {
        TestNode emptyChild = new TestNode("empty", "", "");
        parent.addChildren(emptyChild);
        assertEquals("<p></p>", parent.outerHtml());
    }

    @Test
    public void testOuterHtmlChildWithNullHeadTail() {
        // head and tail are never null in our implementation, but we can test with empty strings
        TestNode nullChild = new TestNode("null", "", "");
        parent.addChildren(nullChild);
        assertEquals("<p></p>", parent.outerHtml());
    }

    // --- toString() ---
    @Test
    public void testToString() {
        parent.addChildren(child1);
        assertEquals(parent.outerHtml(), parent.toString());
    }

    // --- equals() and hashCode() ---
    @Test
    public void testEqualsSameObject() {
        assertTrue(parent.equals(parent));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(parent.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(parent.equals("string"));
    }

    @Test
    public void testEqualsDifferentNode() {
        TestNode other = new TestNode("parent", "<p>", "</p>");
        assertFalse(parent.equals(other));
    }

    @Test
    public void testHashCodeConsistent() {
        int hash1 = parent.hashCode();
        int hash2 = parent.hashCode();
        assertEquals(hash1, hash2);
    }

    // --- clone() ---
    @Test
    public void testClone() {
        parent.addChildren(child1);
        Node clone = parent.clone();
        assertNotSame(parent, clone);
        assertEquals(parent.outerHtml(), clone.outerHtml());
        // Children should be cloned as well
        assertNotSame(parent.childNode(0), clone.childNode(0));
    }

    // --- doClone() ---
    @Test
    public void testDoCloneWithParent() {
        parent.addChildren(child1);
        Node clonedChild = child1.doClone(parent);
        assertNotSame(child1, clonedChild);
        assertSame(parent, clonedChild.parentNode());
    }

    // --- siblingIndex() / setSiblingIndex() ---
    @Test
    public void testSiblingIndexDefault() {
        assertEquals(0, child1.siblingIndex());
    }

    @Test
    public void testSiblingIndexAfterAdd() {
        parent.addChildren(child1, child2);
        assertEquals(0, child1.siblingIndex());
        assertEquals(1, child2.siblingIndex());
    }

    @Test
    public void testSetSiblingIndex() {
        child1.setSiblingIndex(5);
        assertEquals(5, child1.siblingIndex());
    }

    // --- childNodesCopy() ---
    @Test
    public void testChildNodesCopyEmpty() {
        List<Node> copy = parent.childNodesCopy();
        assertTrue(copy.isEmpty());
    }

    @Test
    public void testChildNodesCopyWithChildren() {
        parent.addChildren(child1, child2);
        List<Node> copy = parent.childNodesCopy();
        assertEquals(2, copy.size());
        assertNotSame(child1, copy.get(0));
        assertNotSame(child2, copy.get(1));
        assertEquals(child1.outerHtml(), copy.get(0).outerHtml());
        assertEquals(child2.outerHtml(), copy.get(1).outerHtml());
    }

    // --- Edge cases for outerHtml with multiple children and empty strings ---
    @Test
    public void testOuterHtmlWithEmptyChildren() {
        TestNode empty1 = new TestNode("e1", "", "");
        TestNode empty2 = new TestNode("e2", "", "");
        parent.addChildren(empty1, empty2);
        assertEquals("<p></p>", parent.outerHtml());
    }

    @Test
    public void testOuterHtmlWithMixedChildren() {
        TestNode empty = new TestNode("empty", "", "");
        parent.addChildren(empty, child1);
        assertEquals("<p><a></a></p>", parent.outerHtml());
    }

    // --- Test that outerHtml does not throw when children have no parent ---
    @Test
    public void testOuterHtmlChildWithoutParent() {
        // child1 has no parent, but we add it to parent
        parent.addChildren(child1);
        // Should work fine
        assertEquals("<p><a></a></p>", parent.outerHtml());
    }

    // --- Test addChildren with array of nodes (varargs) ---
    @Test
    public void testAddChildrenVarargs() {
        parent.addChildren(child1, child2, child3);
        assertEquals(3, parent.childNodeSize());
    }

    // --- Test removeChild when child is not a direct child (should throw) ---
    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNotDirectChild() {
        parent.addChildren(child1);
        parent.removeChild(child2); // child2 not added
    }

    // --- Test replaceChild with same node (should work) ---
    @Test
    public void testReplaceChildSameNode() {
        parent.addChildren(child1);
        parent.replaceChild(child1, child1);
        assertEquals(1, parent.childNodeSize());
        assertSame(child1, parent.childNode(0));
    }

    // --- Test that outerHtmlTail is called after children ---
    @Test
    public void testOuterHtmlTailAfterChildren() {
        TestNode tailNode = new TestNode("tail", "<x>", "</x>");
        parent.addChildren(tailNode);
        String html = parent.outerHtml();
        assertTrue(html.contains("</x></p>"));
    }
}