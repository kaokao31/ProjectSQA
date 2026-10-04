package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.parser.Tag;
import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void testNodeConstructorAndBaseUri() {
        Node node = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("http://example.com", node.baseUri());
    }

    @Test
    public void testAttrGetAndSet() {
        Node node = new Element(Tag.valueOf("a"), "http://example.com");
        
        // Test non-existent attribute
        assertEquals("", node.attr("href"));

        // Set and get attribute
        Node returnedNode = node.attr("href", "http://jsoup.org");
        assertSame(node, returnedNode); // Check fluent interface
        assertEquals("http://jsoup.org", node.attr("href"));
        
        // Test absolute URL retrieval
        assertEquals("http://jsoup.org", node.absUrl("href"));
    }

    @Test
    public void testAbsUrlWithRelative() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com/path/");
        el.attr("href", "about.html");
        assertEquals("http://example.com/path/about.html", el.absUrl("href"));
    }

    @Test
    public void testAbsUrlEmptyAndMissing() {
        Element el = new Element(Tag.valueOf("a"), "http://example.com");
        assertEquals("", el.absUrl("href"));
        
        el.attr("href", "");
        assertEquals("", el.absUrl("href"));
    }

    @Test
    public void testAttributesManipulation() {
        Node node = new Element(Tag.valueOf("div"), "http://example.com");
        assertNotNull(node.attributes());
        
        node.attr("class", "container");
        assertTrue(node.hasAttr("class"));
        assertFalse(node.hasAttr("id"));

        node.removeAttr("class");
        assertFalse(node.hasAttr("class"));
    }

    @Test
    public void testNodeChildManagement() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals(0, parent.childNodeSize());
        assertTrue(parent.childNodes().isEmpty());

        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");

        parent.appendChild(child1);
        assertEquals(1, parent.childNodeSize());
        assertSame(parent, child1.parent());

        parent.appendChild(child2);
        assertEquals(2, parent.childNodeSize());
        
        // Test child replacement
        Element child3 = new Element(Tag.valueOf("div"), "http://example.com");
        child1.replaceWith(child3);
        
        assertEquals(2, parent.childNodeSize());
        assertSame(parent, child3.parent());
        assertNull(child1.parent());
    }

    @Test
    public void testNodeRemoval() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child = new Element(Tag.valueOf("p"), "http://example.com");
        parent.appendChild(child);

        assertEquals(1, parent.childNodeSize());
        child.remove();
        assertEquals(0, parent.childNodeSize());
        assertNull(child.parent());
    }

    @Test
    public void testNodeSiblings() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        Element child1 = new Element(Tag.valueOf("p"), "http://example.com");
        Element child2 = new Element(Tag.valueOf("span"), "http://example.com");
        Element child3 = new Element(Tag.valueOf("a"), "http://example.com");

        parent.appendChild(child1);
        parent.appendChild(child2);
        parent.appendChild(child3);

        // child1 siblings
        assertNull(child1.previousSibling());
        assertSame(child2, child1.nextSibling());

        // child2 siblings
        assertSame(child1, child2.previousSibling());
        assertSame(child3, child2.nextSibling());

        // child3 siblings
        assertSame(child2, child3.previousSibling());
        assertNull(child3.nextSibling());
        
        // Orphan node siblings
        Node orphan = new Element(Tag.valueOf("div"), "http://example.com");
        assertNull(orphan.previousSibling());
        assertNull(orphan.nextSibling());
    }

    @Test
    public void testNodeTraversalAndOuterHtml() {
        Element parent = new Element(Tag.valueOf("div"), "http://example.com");
        parent.attr("id", "test");
        Element child = new Element(Tag.valueOf("span"), "http://example.com");
        child.text("Hello");
        parent.appendChild(child);

        String outerHtml = parent.outerHtml();
        assertNotNull(outerHtml);
        assertTrue(outerHtml.contains("<div"));
        assertTrue(outerHtml.contains("<span>"));
        assertTrue(outerHtml.contains("Hello"));
    }
}