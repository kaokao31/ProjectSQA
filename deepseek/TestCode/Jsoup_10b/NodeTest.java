package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for org.jsoup.nodes.Node.
 * Designed to achieve high code coverage and detect faults (e.g., Defects4J bug 10).
 */
public class NodeTest {

    private Element root;
    private Element child1;
    private Element child2;
    private Element child3;
    private TextNode textNode;

    @Before
    public void setUp() {
        // Build a simple tree: root -> [child1, child2, child3], child2 -> textNode
        root = new Element("div");
        child1 = new Element("p");
        child2 = new Element("span");
        child3 = new Element("a");
        textNode = new TextNode("hello");

        root.appendChild(child1);
        root.appendChild(child2);
        root.appendChild(child3);
        child2.appendChild(textNode);
    }

    // ---------- childNodeSize() ----------
    @Test
    public void testChildNodeSize() {
        assertEquals(3, root.childNodeSize());
        assertEquals(1, child2.childNodeSize());
        assertEquals(0, child1.childNodeSize());
    }

    // ---------- childNodes() ----------
    @Test
    public void testChildNodesList() {
        assertEquals(3, root.childNodes().size());
        assertSame(child1, root.childNodes().get(0));
        assertSame(child2, root.childNodes().get(1));
        assertSame(child3, root.childNodes().get(2));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testChildNodesUnmodifiable() {
        root.childNodes().add(new Element("b"));
    }

    // ---------- parent() ----------
    @Test
    public void testParent() {
        assertSame(root, child1.parent());
        assertSame(root, child2.parent());
        assertSame(root, child3.parent());
        assertSame(child2, textNode.parent());
        assertNull(root.parent());
    }

    // ---------- nextSibling() / previousSibling() ----------
    @Test
    public void testNextSibling() {
        assertSame(child2, child1.nextSibling());
        assertSame(child3, child2.nextSibling());
        assertNull(child3.nextSibling());
        assertNull(textNode.nextSibling()); // textNode is only child of child2
    }

    @Test
    public void testPreviousSibling() {
        assertNull(child1.previousSibling());
        assertSame(child1, child2.previousSibling());
        assertSame(child2, child3.previousSibling());
        assertNull(textNode.previousSibling());
    }

    // ---------- siblingIndex() ----------
    @Test
    public void testSiblingIndex() {
        assertEquals(0, child1.siblingIndex());
        assertEquals(1, child2.siblingIndex());
        assertEquals(2, child3.siblingIndex());
        assertEquals(0, textNode.siblingIndex());
    }

    // ---------- setParentNode() ----------
    @Test
    public void testSetParentNode() {
        Element newParent = new Element("section");
        child1.setParentNode(newParent);
        assertSame(newParent, child1.parent());
        // Verify old parent's child list is not updated (setParentNode does not remove)
        assertTrue(root.childNodes().contains(child1));
    }

    // ---------- addChild() ----------
    @Test
    public void testAddChild() {
        Element newChild = new Element("b");
        root.addChild(1, newChild); // insert at index 1
        assertEquals(4, root.childNodeSize());
        assertSame(newChild, root.childNodes().get(1));
        assertSame(root, newChild.parent());
        // Check sibling indices updated
        assertEquals(0, child1.siblingIndex());
        assertEquals(1, newChild.siblingIndex());
        assertEquals(2, child2.siblingIndex());
        assertEquals(3, child3.siblingIndex());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddChildInvalidIndex() {
        root.addChild(5, new Element("x"));
    }

    // ---------- removeChild() ----------
    @Test
    public void testRemoveChild() {
        root.removeChild(child2);
        assertEquals(2, root.childNodeSize());
        assertFalse(root.childNodes().contains(child2));
        assertNull(child2.parent());
        // Sibling indices updated
        assertEquals(0, child1.siblingIndex());
        assertEquals(1, child3.siblingIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNotOwn() {
        root.removeChild(new Element("orphan"));
    }

    // ---------- replaceChild() ----------
    @Test
    public void testReplaceChild() {
        Element replacement = new Element("b");
        root.replaceChild(child2, replacement);
        assertEquals(3, root.childNodeSize());
        assertSame(replacement, root.childNodes().get(1));
        assertSame(root, replacement.parent());
        assertNull(child2.parent());
        // Sibling indices updated
        assertEquals(0, child1.siblingIndex());
        assertEquals(1, replacement.siblingIndex());
        assertEquals(2, child3.siblingIndex());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNotOwn() {
        root.replaceChild(new Element("orphan"), new Element("b"));
    }

    // ---------- clone() ----------
    @Test
    public void testCloneShallow() {
        Node clone = root.clone();
        assertNotNull(clone);
        assertNotSame(root, clone);
        assertTrue(clone instanceof Element);
        Element cloneElem = (Element) clone;
        // Shallow clone: child nodes are not cloned (depends on implementation)
        // In Jsoup, clone is deep by default? Actually Node.clone() is deep.
        // We'll test deep clone behavior.
        assertEquals(3, cloneElem.childNodeSize());
        assertNotSame(child1, cloneElem.childNodes().get(0));
        // Verify parent of cloned children is the clone
        assertSame(cloneElem, cloneElem.childNodes().get(0).parent());
    }

    // ---------- outerHtml() ----------
    @Test
    public void testOuterHtml() {
        String html = root.outerHtml();
        assertTrue(html.startsWith("<div>"));
        assertTrue(html.endsWith("</div>"));
        assertTrue(html.contains("<p></p>"));
        assertTrue(html.contains("<span>hello</span>"));
        assertTrue(html.contains("<a></a>"));
    }

    // ---------- Edge cases and fault detection (Defects4J bug 10) ----------
    // Bug 10: likely related to sibling traversal after removal or index update.
    @Test
    public void testSiblingAfterRemoval() {
        // Remove middle child and verify next/previous of neighbors
        root.removeChild(child2);
        assertSame(child3, child1.nextSibling());
        assertSame(child1, child3.previousSibling());
    }

    @Test
    public void testSiblingAfterAdd() {
        Element newChild = new Element("b");
        root.addChild(1, newChild);
        assertSame(newChild, child1.nextSibling());
        assertSame(child2, newChild.nextSibling());
        assertSame(newChild, child2.previousSibling());
    }

    @Test
    public void testSiblingIndexAfterRemoval() {
        root.removeChild(child1);
        assertEquals(0, child2.siblingIndex());
        assertEquals(1, child3.siblingIndex());
    }

    @Test
    public void testSiblingIndexAfterAdd() {
        Element newChild = new Element("b");
        root.addChild(0, newChild);
        assertEquals(0, newChild.siblingIndex());
        assertEquals(1, child1.siblingIndex());
        assertEquals(2, child2.siblingIndex());
        assertEquals(3, child3.siblingIndex());
    }

    @Test
    public void testNextSiblingOnOnlyChild() {
        // textNode is only child of child2
        assertNull(textNode.nextSibling());
        // After adding a sibling to child2
        Element newSibling = new Element("em");
        child2.appendChild(newSibling);
        assertSame(newSibling, textNode.nextSibling());
        assertNull(newSibling.nextSibling());
    }

    @Test
    public void testPreviousSiblingOnOnlyChild() {
        assertNull(textNode.previousSibling());
        Element newSibling = new Element("em");
        child2.appendChild(newSibling);
        assertSame(textNode, newSibling.previousSibling());
    }

    @Test
    public void testSetParentNodeNull() {
        child1.setParentNode(null);
        assertNull(child1.parent());
    }

    @Test
    public void testRemoveChildThenReadd() {
        root.removeChild(child2);
        root.appendChild(child2);
        assertEquals(3, root.childNodeSize());
        assertEquals(2, child2.siblingIndex()); // appended at end
        assertSame(root, child2.parent());
    }

    @Test
    public void testReplaceChildWithItself() {
        // Should be a no-op or throw? In Jsoup, replaceChild with same node might cause issues.
        // We'll test that it doesn't break structure.
        try {
            root.replaceChild(child2, child2);
            // If no exception, check state
            assertEquals(3, root.childNodeSize());
            assertSame(child2, root.childNodes().get(1));
        } catch (Exception e) {
            // Acceptable if implementation throws
        }
    }

    // ---------- Additional coverage for abstract methods (via Element) ----------
    @Test
    public void testNodeName() {
        assertEquals("div", root.nodeName());
        assertEquals("p", child1.nodeName());
        assertEquals("#text", textNode.nodeName());
    }

    @Test
    public void testAttributes() {
        Element withAttr = new Element("img");
        withAttr.attr("src", "test.png");
        assertTrue(withAttr.hasAttr("src"));
        assertEquals("test.png", withAttr.attr("src"));
    }

    @Test
    public void testBaseUri() {
        Element withBase = new Element("a", "http://example.com");
        assertEquals("http://example.com", withBase.baseUri());
    }

    // ---------- Null safety ----------
    @Test(expected = IllegalArgumentException.class)
    public void testAppendChildNull() {
        root.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddChildNull() {
        root.addChild(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveChildNull() {
        root.removeChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceChildNull() {
        root.replaceChild(null, new Element("x"));
    }
}