package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.List;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testElementSibling() {
        // Test element and sibling navigation methods to cover branch conditions
        String html = "<div><p>One</p><span id='target'>Two</span><em>Three</em></div>";
        Document doc = Jsoup.parse(html);
        Element target = doc.getElementById("target");

        assertNotNull(target);
        assertEquals("span", target.tagName());

        // Previous element sibling
        Element prev = target.previousElementSibling();
        assertNotNull(prev);
        assertEquals("p", prev.tagName());

        // Next element sibling
        Element next = target.nextElementSibling();
        assertNotNull(next);
        assertEquals("em", next.tagName());

        // First and last element siblings
        Element first = target.firstElementSibling();
        assertNotNull(first);
        assertEquals("p", first.tagName());

        Element last = target.lastElementSibling();
        assertNotNull(last);
        assertEquals("em", last.tagName());

        // Test siblings without parents or at boundaries
        Element orphan = new Element("div");
        assertNull(orphan.previousElementSibling());
        assertNull(orphan.nextElementSibling());
        assertNull(orphan.firstElementSibling());
        assertNull(orphan.lastElementSibling());
    }

    @Test
    public void testChildNodeChangesAndIndentation() {
        // Test structural changes, node additions, and children modifications
        Element el = new Element("div");
        el.text("Hello");
        assertEquals(1, el.childNodeSize());
        assertEquals("Hello", el.text());

        el.append("<p>World</p>");
        assertEquals(2, el.childNodeSize());
        
        // Children list
        Elements children = el.children();
        assertEquals(1, children.size());
        assertEquals("p", children.first().tagName());

        // Insert children
        el.prepend("<p>First</p>");
        assertEquals(3, el.childNodeSize());
        assertEquals("First", el.child(0).text());

        // Test element insertion at index
        Element newChild = new Element("span").text("Middle");
        el.insertChildren(1, newChild);
        assertTrue(el.childNodeSize() >= 4);

        // Test clone
        Element clone = el.clone();
        assertNotNull(clone);
        assertEquals(el.tagName(), clone.tagName());
        assertEquals(el.childNodeSize(), clone.childNodeSize());
    }

    @Test
    public void testClassOperations() {
        Element el = new Element("div");
        
        // Class names manipulation
        el.addClass("foo bar");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertEquals("foo bar", el.className());

        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));

        el.toggleClass("baz");
        assertTrue(el.hasClass("baz"));
        
        el.toggleClass("baz");
        assertFalse(el.hasClass("baz"));
    }

    @Test
    public void testDataAndAttributes() {
        Element el = new Element("div");
        el.attr("data-id", "12345");
        assertEquals("12345", el.attr("data-id"));
        
        el.data("key", "value");
        assertEquals("value", el.data("key"));

        assertTrue(el.hasAttr("data-id"));
        el.removeAttr("data-id");
        assertFalse(el.hasAttr("data-id"));
    }

    @Test
    public void testElementTraversalAndSelect() {
        String html = "<div class='outer'><div class='inner'><p class='target'>Text</p></div></div>";
        Document doc = Jsoup.parse(html);

        Elements results = doc.select(".target");
        assertEquals(1, results.size());
        
        Element p = results.first();
        Element parent = p.parent();
        assertNotNull(parent);
        assertEquals("div", parent.tagName());

        Element ancestor = p.elementWithTag("div"); // or parent lookup
        assertNotNull(ancestor);

        List<String> data = p.dataList();
        assertNotNull(data);
    }
}