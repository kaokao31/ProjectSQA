package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testNormalizingAttributeName() {
        Element el = new Element("div");
        el.attr("Data-Test", "value");
        // Verify attribute is set
        assertEquals("value", el.attr("Data-Test"));
        
        // Test tagName normalization
        assertEquals("div", el.tagName());
        
        el.tagName("SPAN");
        assertEquals("span", el.tagName());
    }

    @Test
    public void testDataAttributes() {
        Element el = new Element("div");
        el.attr("data-id", "123");
        assertEquals("123", el.dataset().get("id"));
        
        el.dataset().put("name", "jsoup");
        assertEquals("jsoup", el.attr("data-name"));
    }

    @Test
    public void testAppendChildAndElementSibling() {
        Element parent = new Element("div");
        Element child1 = new Element("p");
        Element child2 = new Element("span");

        parent.appendChild(child1);
        parent.appendChild(child2);

        assertEquals(2, parent.childNodeSize());
        assertEquals(child1, parent.child(0));
        assertEquals(child2, parent.child(1));

        assertEquals(child2, child1.nextElementSibling());
        assertEquals(child1, child2.previousElementSibling());
        assertNull(child1.previousElementSibling());
        assertNull(child2.nextElementSibling());
    }

    @Test
    public void testClassNames() {
        Element el = new Element("div");
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
    public void testHtmlAndTextManipulation() {
        Element el = new Element("div");
        el.text("Hello <b>World</b>");
        // text() should escape the tags
        assertEquals("Hello <b>World</b>", el.text());
        
        el.html("<span>Child</span>");
        assertEquals("<span>Child</span>", el.html());
        assertEquals("Child", el.text());
    }

    @Test
    public void testChildElementSelector() {
        Element parent = new Element("div");
        Element child1 = new Element("p").id("p1").classNames(java.util.Collections.singleton("c1"));
        Element child2 = new Element("span").id("p2").classNames(java.util.Collections.singleton("c2"));

        parent.appendChild(child1);
        parent.appendChild(child2);

        Elements children = parent.children();
        assertEquals(2, children.size());
        assertEquals(child1, children.get(0));

        Elements pElements = parent.getElementsByTag("p");
        assertEquals(1, pElements.size());
        assertEquals(child1, pElements.get(0));

        Elements c1Elements = parent.getElementsByClass("c1");
        assertEquals(1, c1Elements.size());
        assertEquals(child1, c1Elements.get(0));

        Element foundById = parent.getElementById("p2");
        assertEquals(child2, foundById);
    }

    @Test
    public void testTagNameWithNamespace() {
        Element el = new Element("fb:name");
        assertEquals("fb:name", el.tagName());
        
        Element clone = el.clone();
        assertEquals("fb:name", clone.tagName());
    }

    @Test
    public void testOuterHtmlSelfClosing() {
        Element el = new Element("br");
        String html = el.outerHtml();
        assertTrue(html.contains("br"));
    }
}