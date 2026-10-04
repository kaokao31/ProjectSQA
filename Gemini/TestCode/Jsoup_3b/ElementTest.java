package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testElementConstructorAndBasicGetters() {
        Element el = new Element("div");
        assertEquals("div", el.tagName());
        assertNotNull(el.attributes());
        assertNull(el.parent());
    }

    @Test
    public void testTagNameMutation() {
        Element el = new Element("p");
        el.tagName("span");
        assertEquals("span", el.tagName());
    }

    @Test
    public void testAttrGetSet() {
        Element el = new Element("a");
        el.attr("href", "http://example.com");
        assertEquals("http://example.com", el.attr("href"));
        
        // Test non-existent attribute
        assertEquals("", el.attr("class"));
    }

    @Test
    public void testId() {
        Element el = new Element("div");
        el.attr("id", "main-id");
        assertEquals("main-id", el.id());
    }

    @Test
    public void testChildManipulation() {
        Element parent = new Element("div");
        Element child1 = new Element("span");
        Element child2 = new Element("a");

        parent.appendChild(child1);
        parent.appendChild(child2);

        assertEquals(2, parent.childNodeSize());
        assertSame(child1, parent.child(0));
        assertSame(child2, parent.child(1));
        assertSame(parent, child1.parent());
        assertSame(parent, child2.parent());
    }

    @Test
    public void testTextGetSet() {
        Element el = new Element("div");
        el.text("Hello World");
        assertEquals("Hello World", el.text());
        assertEquals("<div>Hello World</div>", el.html());
    }

    @Test
    public void testHtmlGetSet() {
        Element el = new Element("div");
        el.html("<span>Inner</span>");
        assertEquals("<span>Inner</span>", el.html());
        assertEquals("Inner", el.text());
    }

    @Test
    public void testData() {
        Element el = new Element("script");
        el.text("var x = 1;");
        assertEquals("var x = 1;", el.data());
    }

    @Test
    public void testClassNameHandling() {
        Element el = new Element("div");
        el.addClass("foo");
        el.addClass("bar");
        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertEquals("foo bar", el.className());

        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
        assertEquals("bar", el.className());
    }

    @Test
    public void testElementSibling() {
        Element parent = new Element("div");
        Element child1 = new Element("span");
        Element child2 = new Element("span");
        Element child3 = new Element("span");

        parent.appendChild(child1);
        parent.appendChild(child2);
        parent.appendChild(child3);

        assertSame(child2, child1.nextElementSibling());
        assertSame(child1, child2.previousElementSibling());
        assertNull(child1.previousElementSibling());
        assertNull(child3.nextElementSibling());
    }

    @Test
    public void testParents() {
        Element grandparent = new Element("div");
        Element parent = new Element("p");
        Element child = new Element("span");

        grandparent.appendChild(parent);
        parent.appendChild(child);

        Elements parents = child.parents();
        assertEquals(2, parents.size());
        assertSame(parent, parents.get(0));
        assertSame(grandparent, parents.get(1));
    }

    @Test
    public void testClone() {
        Element el = new Element("div");
        el.attr("class", "test");
        Element child = new Element("span");
        el.appendChild(child);

        Element clone = el.clone();
        assertEquals("div", clone.tagName());
        assertEquals("test", clone.attr("class"));
        assertEquals(1, clone.childNodeSize());
        assertNotSame(el, clone);
        assertNotSame(child, clone.child(0));
    }

    @Test
    public void testEmptyElement() {
        Element el = new Element("div");
        el.appendChild(new Element("span"));
        assertEquals(1, el.childNodeSize());
        el.empty();
        assertEquals(0, el.childNodeSize());
    }

    @Test
    public void testAppendPrepend() {
        Element el = new Element("div");
        el.text("Middle");
        el.prepend("First ");
        el.append(" Last");
        assertEquals("First Middle Last", el.text());
    }

    @Test
    public void testWrap() {
        Element el = new Element("span");
        el.text("Hello");
        Element parent = new Element("div");
        parent.appendChild(el);

        el.wrap("<div class='wrapper'></div>");
        assertEquals("<div class=\"wrapper\"><span>Hello</span></div>", parent.html());
    }
}