package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.NodeVisitor;
import org.jsoup.select.Elements;

import java.util.List;

import static org.junit.Assert.*;

public class NodeTest {

    @Test
    public void testNodeBasicOperations() {
        // Create concrete nodes using Element (subclass of Node)
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "");
        Element child2 = new Element(Tag.valueOf("span"), "");

        assertEquals(0, parent.childNodeSize());
        
        parent.appendChild(child1);
        assertEquals(1, parent.childNodeSize());
        assertEquals(child1, parent.childNode(0));
        assertEquals(parent, child1.parent());

        parent.appendChild(child2);
        assertEquals(2, parent.childNodeSize());
        assertEquals(child2, parent.childNode(1));

        // Test indexInOwner
        assertEquals(0, child1.siblingIndex);
        assertEquals(1, child2.siblingIndex);

        // Test reparenting / adding an already existing child or replacing
        Element child3 = new Element(Tag.valueOf("a"), "");
        child1.replaceWith(child3);
        assertEquals(child3, parent.childNode(0));
        assertNull(child1.parent());
        assertEquals(parent, child3.parent());
        assertEquals(0, child3.siblingIndex);
    }

    @Test
    public void testNodeAttributesAndBaseUri() {
        Element node = new Element(Tag.valueOf("div"), "http://example.com");
        assertEquals("http://example.com", node.baseUri());

        node.attr("id", "test-id");
        assertEquals("test-id", node.attr("id"));
        assertTrue(node.hasAttr("id"));

        node.removeAttr("id");
        assertFalse(node.hasAttr("id"));

        node.setBaseUri("http://example.org");
        assertEquals("http://example.org", node.baseUri());
    }

    @Test
    public void testNodeTraversal() {
        Element root = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        TextNode text = new TextNode("hello", "");
        root.appendChild(child);
        child.appendChild(text);

        StringBuilder visited = new StringBuilder();
        root.traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                visited.append("H:").append(node.nodeName()).append(" ");
            }

            public void tail(Node node, int depth) {
                visited.append("T:").append(node.nodeName()).append(" ");
            }
        });

        assertTrue(visited.toString().contains("H:div"));
        assertTrue(visited.toString().contains("H:p"));
        assertTrue(visited.toString().contains("H:#text"));
    }

    @Test
    public void testRemoveAndClear() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);

        assertEquals(1, parent.childNodeSize());
        child.remove();
        assertEquals(0, parent.childNodeSize());
        assertNull(child.parent());
    }

    @Test
    public void testChildNodesCopy() {
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child = new Element(Tag.valueOf("p"), "");
        parent.appendChild(child);

        List<Node> children = parent.childNodesCopy();
        assertEquals(1, children.size());
        assertNotSame(child, children.get(0));
        assertEquals(child.nodeName(), children.get(0).nodeName());
    }

    @Test
    public void testAddChildrenFaultScenarioJsoup49() {
        // Specifically targeting Jsoup 49 bug (re-indexing / adding children array handling)
        Element parent = new Element(Tag.valueOf("div"), "");
        Element child1 = new Element(Tag.valueOf("p"), "http://base1");
        Element child2 = new Element(Tag.valueOf("span"), "http://base2");

        parent.addChildren(child1, child2);

        assertEquals(2, parent.childNodeSize());
        assertEquals(0, child1.siblingIndex);
        assertEquals(1, child2.siblingIndex);
        assertEquals(parent, child1.parent());
        assertEquals(parent, child2.parent());

        // Test inserting children at a specific index
        Element child0 = new Element(Tag.valueOf("div"), "http://base0");
        parent.addChildren(0, child0);

        assertEquals(3, parent.childNodeSize());
        assertEquals(child0, parent.childNode(0));
        assertEquals(child1, parent.childNode(1));
        assertEquals(child2, parent.childNode(2));

        assertEquals(0, child0.siblingIndex);
        assertEquals(1, child1.siblingIndex);
        assertEquals(2, child2.siblingIndex);
    }

    @Test
    public void testWrapAndOuterHtml() {
        Element el = new Element(Tag.valueOf("span"), "");
        el.text("Hello");
        String html = el.outerHtml();
        assertTrue(html.contains("span"));
        assertTrue(html.contains("Hello"));

        Element parent = new Element(Tag.valueOf("div"), "");
        parent.appendChild(el);
        
        String wrapResult = el.wrap("<div></div>");
        assertNotNull(wrapResult);
    }
}