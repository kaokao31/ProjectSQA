package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class NodeTest {

    @Test
    public void testNodeBasicOperations() {
        // Concrete subclass of Node for testing abstract Node class
        Node node1 = new TextNode("Hello", "http://example.com");
        Node node2 = new TextNode("World", "http://example.com");

        assertEquals("#text", node1.nodeName());
        assertEquals("http://example.com", node1.baseUri());
        
        // Test setBaseUri
        node1.setBaseUri("http://example.org");
        assertEquals("http://example.org", node1.baseUri());

        // Test child nodes operations
        assertEquals(0, node1.childNodeSize());
        assertFalse(node1.hasParent());
        assertNull(node1.parent());

        // Add child (Note: TextNode typically doesn't hold children, but let's test via a Element or mock structure if needed)
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.appendChild(node1);
        parent.appendChild(node2);

        assertEquals(2, parent.childNodeSize());
        assertTrue(node1.hasParent());
        assertEquals(parent, node1.parent());
        assertEquals(0, node1.siblingIndex());
        assertEquals(1, node2.siblingIndex());

        // Test next and previous siblings
        assertEquals(node2, node1.nextSibling());
        assertNull(node1.previousSibling());
        assertEquals(node1, node2.previousSibling());
        assertNull(node2.nextSibling());
    }

    @Test
    public void testAttrOperations() {
        Node node = new TextNode("test", "http://example.com");
        
        // Test attr set and get
        node.attr("id", "testId");
        assertEquals("testId", node.attr("id"));
        assertTrue(node.hasAttr("id"));
        assertFalse(node.hasAttr("class"));

        // Test remove attr
        node.removeAttr("id");
        assertFalse(node.hasAttr("id"));
        assertEquals("", node.attr("id"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        Node node = new TextNode("test", "http://example.com");
        node.attr(null, "val");
    }

    @Test
    public void testAbsUrl() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com/path/");
        el.attr("href", "sub/file.html");
        
        assertEquals("http://example.com/path/sub/file.html", el.absUrl("href"));
        
        // Test empty/missing attr
        assertEquals("", el.absUrl("nonexistent"));
    }

    @Test
    public void testNodeTraversalAndCloning() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        root.appendChild(child);

        Node clone = root.clone();
        assertNotNull(clone);
        assertEquals(root.nodeName(), clone.nodeName());
        assertEquals(root.childNodeSize(), clone.childNodeSize());
        assertNotSame(root, clone);
        assertNotSame(root.childNode(0), clone.childNode(0));
    }

    @Test
    public void testNodeManipulation() {
        Element root = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");
        
        root.appendChild(child1);
        child1.replaceWith(child2);

        assertEquals(1, root.childNodeSize());
        assertEquals(child2, root.childNode(0));
        assertFalse(child1.hasParent());
        assertTrue(child2.hasParent());

        child2.remove();
        assertEquals(0, root.childNodeSize());
        assertFalse(child2.hasParent());
    }

    @Test
    public void testOuterHtmlGeneration() {
        Element el = new Element(Tag.valueOf("div"), "http://example.com");
        el.attr("class", "container");
        TextNode text = new TextNode("Hello", "http://example.com");
        el.appendChild(text);

        String html = el.outerHtml();
        assertNotNull(html);
        assertTrue(html.contains("<div"));
        assertTrue(html.contains("class=\"container\""));
        assertTrue(html.contains("Hello"));
        assertTrue(html.contains("</div>"));
    }
}