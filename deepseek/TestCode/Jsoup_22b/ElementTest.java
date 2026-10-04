package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ElementTest {

    private Element div;
    private Element p;
    private Element a;
    private Element parent;

    @Before
    public void setUp() {
        parent = new Element(Tag.valueOf("div"), "");
        div = new Element(Tag.valueOf("div"), "");
        p = new Element(Tag.valueOf("p"), "");
        a = new Element(Tag.valueOf("a"), "http://example.com");
        a.attr("href", "/path");
        p.appendChild(a);
        p.appendChild(new Element(Tag.valueOf("span"), "").text("spanText"));
        div.appendChild(p);
        parent.appendChild(div);
    }

    @Test
    public void testTagName() {
        assertEquals("div", div.tagName());
        Element span = div.select("span").first();
        assertNotNull(span);
        assertEquals("span", span.tagName());
    }

    @Test
    public void testAttr() {
        div.attr("id", "main");
        assertEquals("main", div.attr("id"));
        assertEquals("", div.attr("nonexistent"));
        div.attr("class", "content");
        assertEquals("content", div.attr("class"));
    }

    @Test
    public void testHasAttr() {
        assertFalse(div.hasAttr("id"));
        div.attr("id", "main");
        assertTrue(div.hasAttr("id"));
        assertFalse(div.hasAttr("nope"));
    }

    @Test
    public void testAbsUrl() {
        assertNotNull(a);
        assertEquals("http://example.com/path", a.absUrl("href"));
        a.attr("href", "");
        assertEquals("", a.absUrl("href"));
        a.attr("href", "/other");
        assertEquals("http://example.com/other", a.absUrl("href"));
        assertNull(a.absUrl("nonexistent"));
    }

    @Test
    public void testText() {
        assertEquals("spanText", p.text());
        p.text("Updated text");
        assertEquals("Updated text", p.text());
        assertTrue(div.text().isEmpty());
        p.addText(" appended");
        assertEquals("Updated text appended", p.text());
    }

    @Test
    public void testOwnText() {
        p.addText("Own text");
        assertEquals("Own text", p.ownText());
        assertEquals("", div.ownText());
    }

    @Test
    public void testChildren() {
        List<Element> children = p.children();
        assertEquals(2, children.size());
        assertEquals("a", children.get(0).tagName());
        assertEquals("span", children.get(1).tagName());
        assertTrue(div.children().isEmpty());
    }

    @Test
    public void testSelect() {
        assertEquals(1, parent.select("div").size());
        assertEquals(1, parent.select("a").size());
        assertEquals(1, parent.select("p").size());
        assertEquals(0, parent.select("nonexistent").size());
    }

    @Test
    public void testHtmlAndOuterHtml() {
        assertEquals("<a href=\"/path\"></a><span>spanText</span>", p.html());
        assertEquals("<p><a href=\"/path\"></a><span>spanText</span></p>", p.outerHtml());
        assertEquals("<div></div>", div.outerHtml());
        assertEquals("", div.html());
    }

    @Test
    public void testId() {
        assertEquals("", div.id());
        div.id("main");
        assertEquals("main", div.id());
        assertEquals("main", div.attr("id"));
    }

    @Test
    public void testClassNameAndHasClass() {
        assertFalse(div.hasClass("content"));
        div.addClass("content");
        assertTrue(div.hasClass("content"));
        assertEquals("content", div.className());
        div.removeClass("content");
        assertFalse(div.hasClass("content"));
        assertEquals("", div.className());
        div.addClass("one two");
        assertTrue(div.hasClass("one"));
        assertTrue(div.hasClass("two"));
    }

    @Test
    public void testVal() {
        Element input = new Element(Tag.valueOf("input"), "");
        assertEquals("", input.val());
        input.val("user input");
        assertEquals("user input", input.val());
        input.attr("value", "initial");
        assertEquals("initial", input.val());
    }

    @Test
    public void testAppendChild() {
        Element child = new Element(Tag.valueOf("span"), "");
        div.appendChild(child);
        assertEquals(1, div.children().size());
        assertSame(child, div.child(0));
    }

    @Test
    public void testPrependChild() {
        Element child = new Element(Tag.valueOf("em"), "");
        div.prependChild(child);
        assertEquals(1, div.children().size());
        assertSame(child, div.child(0));
    }

    @Test
    public void testEmptyAndRemove() {
        p.empty();
        assertEquals(0, p.children().size());
        p.appendChild(new Element(Tag.valueOf("b"), "").text("bold"));
        Element b = p.select("b").first();
        assertNotNull(b);
        b.remove();
        assertEquals(0, p.children().size());
    }

    @Test
    public void testParent() {
        assertSame(parent, div.parent());
        assertSame(div, p.parent());
        assertSame(p, a.parent());
    }

    @Test
    public void testPosition() {
        assertEquals(0, div.siblingIndex());
        assertEquals(0, p.siblingIndex());
        assertEquals(0, a.siblingIndex());
        assertEquals(1, p.children().get(1).siblingIndex());
    }

    @Test
    public void testDataset() {
        div.dataset().put("key", "value");
        assertEquals("value", div.attr("data-key"));
        assertEquals(1, div.dataset().size());
    }

    @Test
    public void testIsBlock() {
        assertTrue(div.isBlock());
        assertFalse(a.isBlock());
    }

    @Test
    public void testNormaliseWhitespace() {
        Element e = new Element(Tag.valueOf("p"), "");
        e.text("  leading and trailing  ");
        assertEquals("leading and trailing", e.text());
        e.text("multiple   spaces");
        assertEquals("multiple   spaces", e.text()); // Jsoup preserves internal spaces in text()
    }

    @Test
    public void testAttributesCaseInsensitivity() {
        div.attr("DATA-X", "test");
        assertEquals("test", div.attr("data-x"));
        assertEquals("", div.attr("data-y"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Element other = new Element(Tag.valueOf("div"), "");
        assertNotEquals(div, other);
        assertNotEquals(div.hashCode(), other.hashCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTagThrows() {
        new Element(null, ""); // should throw
    }
}