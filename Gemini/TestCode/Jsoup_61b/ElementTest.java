package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.List;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testHasClassIgnoreCase() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.attr("class", "TestClass ABC");

        // Exact match
        assertTrue(el.hasClass("TestClass"));
        // Case-insensitive match (specifically targeted for Jsoup 61 bug fixes)
        assertTrue(el.hasClass("testclass"));
        assertTrue(el.hasClass("ABC"));
        assertTrue(el.hasClass("abc"));

        // Non-existent class
        assertFalse(el.hasClass("notfound"));
        
        // Edge cases with empty/null/whitespace
        assertFalse(el.hasClass(""));
        assertFalse(el.hasClass(null));
        
        Element elNoClass = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        assertFalse(elNoClass.hasClass("test"));
    }

    @Test
    public void testHasClassMultipleAndEdgeCases() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.attr("class", "  foo   BAR  ");

        assertTrue(el.hasClass("foo"));
        assertTrue(el.hasClass("FOO"));
        assertTrue(el.hasClass("bar"));
        assertTrue(el.hasClass("BAR"));
        assertFalse(el.hasClass("oo"));
    }

    @Test
    public void testGetElementsByClass() {
        Element root = Jsoup.parse("<div class='A'><p class='a b'></p><span class='B'></span></div>").body();
        
        Elements foundA = root.getElementsByClass("a");
        assertEquals(2, foundA.size()); // div.A and p.a b should both match case-insensitively

        Elements foundB = root.getElementsByClass("b");
        assertEquals(1, foundB.size());

        Elements foundNone = root.getElementsByClass("nonexistent");
        assertTrue(foundNone.isEmpty());

        // Empty/null checks
        assertTrue(root.getElementsByClass("").isEmpty());
        // Depending on implementation, passing null might throw or return empty
        try {
            root.getElementsByClass(null);
        } catch (IllegalArgumentException | NullPointerException e) {
            // expected if null is not allowed
        }
    }

    @Test
    public void testAddClass() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.addClass("test");
        assertEquals("test", el.attr("class"));
        
        // Add duplicate
        el.addClass("test");
        assertEquals("test", el.attr("class"));

        // Add another class
        el.addClass("other");
        assertTrue(el.hasClass("test"));
        assertTrue(el.hasClass("other"));
    }

    @Test
    public void testRemoveClass() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.attr("class", "foo bar baz");
        
        el.removeAttr("class");
        el.removeClass("bar");
        assertFalse(el.hasClass("bar"));
        
        el.attr("class", "foo bar foo");
        el.removeClass("foo");
        assertFalse(el.hasClass("foo"));
        assertTrue(el.hasClass("bar"));
    }

    @Test
    public void testToggleClass() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.toggleClass("foo");
        assertTrue(el.hasClass("foo"));

        el.toggleClass("foo");
        assertFalse(el.hasClass("foo"));
    }

    @Test
    public void testTagNameManipulation() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        assertEquals("div", el.tagName());
        
        el.tagName("span");
        assertEquals("span", el.tagName());
    }

    @Test
    public void testElementBasicProperties() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("p"), "http://example.com");
        assertEquals("p", el.nodeName());
        assertNotNull(el.attributes());
        assertEquals(0, el.siblingIndex());
        
        Element child = new Element(org.jsoup.parser.Tag.valueOf("span"), "http://example.com");
        el.appendChild(child);
        assertEquals(1, el.childNodeSize());
        assertEquals(child, el.child(0));
        
        List<Element> children = el.children();
        assertEquals(1, children.size());
        assertEquals(child, children.get(0));
    }

    @Test
    public void testHtmlAndTextOperations() {
        Element el = Jsoup.parse("<div><p>Hello <b>World</b></p></div>").body().child(0);
        assertEquals("Hello World", el.text());
        
        el.text("New Text");
        assertEquals("New Text", el.text());

        el.html("<span>Inner</span>");
        assertEquals("<span>Inner</span>", el.html());
    }

    @Test
    public void testAncestorsAndSiblings() {
        Element doc = Jsoup.parse("<div><p id=1></p><p id=2></p><p id=3></p></div>").body();
        Element p2 = doc.getElementById("2");
        
        assertNotNull(p2);
        assertEquals("p", p2.tagName());
        
        Elements parents = p2.parents();
        assertFalse(parents.isEmpty());
        
        Element next = p2.nextElementSibling();
        assertNotNull(next);
        assertEquals("3", next.id());

        Element prev = p2.previousElementSibling();
        assertNotNull(prev);
        assertEquals("1", prev.id());
    }

    @Test
    public void testDataAndAttr() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("a"), "");
        el.attr("href", "http://example.com");
        assertEquals("http://example.com", el.absUrl("href"));
        assertEquals("http://example.com", el.attr("href"));
        assertTrue(el.hasAttr("href"));
        
        el.removeAttr("href");
        assertFalse(el.hasAttr("href"));
    }
}