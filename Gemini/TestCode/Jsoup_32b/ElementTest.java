package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    @Test
    public void testClonePreservesAttributesAndStructure() {
        Element parent = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        parent.attr("id", "parent-id");
        
        Element child = new Element(org.jsoup.parser.Tag.valueOf("span"), "");
        child.attr("class", "child-class");
        parent.appendChild(child);

        Element clone = parent.clone();
        
        assertNotNull(clone);
        assertNotSame(parent, clone);
        assertEquals("parent-id", clone.attr("id"));
        
        assertEquals(1, clone.children().size());
        Element clonedChild = clone.child(0);
        assertNotSame(child, clonedChild);
        assertEquals("child-class", clonedChild.attr("class"));
        
        // Check structural integrity of parent pointer in clone
        assertSame(clone, clonedChild.parent());
    }

    @Test
    public void testAddClassNameAndHasClass() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        assertFalse(el.hasClass("test"));

        el.addClass("test");
        assertTrue(el.hasClass("test"));
        assertEquals("test", el.attr("class"));

        // Add duplicate class
        el.addClass("test");
        assertEquals("test", el.attr("class"));

        // Add another class
        el.addClass("second");
        assertTrue(el.hasClass("test"));
        assertTrue(el.hasClass("second"));
        assertEquals("test second", el.attr("class"));
    }

    @Test
    public void testRemoveClassName() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.addClass("one");
        el.addClass("two");
        el.addClass("three");

        assertTrue(el.hasClass("two"));
        el.removeClass("two");
        assertFalse(el.hasClass("two"));
        assertTrue(el.hasClass("one"));
        assertTrue(el.hasClass("three"));
        assertEquals("one three", el.attr("class"));

        // Remove non-existent class
        el.removeClass("nonexistent");
        assertEquals("one three", el.attr("class"));
    }

    @Test
    public void testToggleClass() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        
        el.toggleClass("toggled");
        assertTrue(el.hasClass("toggled"));

        el.toggleClass("toggled");
        assertFalse(el.hasClass("toggled"));
    }

    @Test
    public void testValGetAndSet() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("textarea"), "");
        assertEquals("", el.val());

        el.val("my-value");
        assertEquals("my-value", el.val());
        assertEquals("my-value", el.attr("value"));

        Element input = new Element(org.jsoup.parser.Tag.valueOf("input"), "");
        input.val("input-val");
        assertEquals("input-val", input.val());
    }

    @Test
    public void testDataMethods() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.attr("data-key", "value");

        assertEquals("value", el.data("key"));
        
        Map<String, String> dataset = el.dataset();
        assertEquals(1, dataset.size());
        assertEquals("value", dataset.get("key"));

        el.data("newkey", "newval");
        assertEquals("newval", el.attr("data-newkey"));
    }

    @Test
    public void testElementTraversalAndManipulation() {
        Element root = new Element(org.jsoup.parser.Tag.valueOf("root"), "");
        Element child1 = new Element(org.jsoup.parser.Tag.valueOf("child1"), "");
        Element child2 = new Element(org.jsoup.parser.Tag.valueOf("child2"), "");

        root.appendChild(child1);
        root.appendChild(child2);

        assertEquals(2, root.children().size());
        assertSame(child1, root.child(0));
        assertSame(child2, root.child(1));

        // Test prepend
        Element child0 = new Element(org.jsoup.parser.Tag.valueOf("child0"), "");
        root.prependChild(child0);
        assertSame(child0, root.child(0));
        assertEquals(3, root.children().size());

        // Test insertChildren
        Element insertTarget = new Element(org.jsoup.parser.Tag.valueOf("inserted"), "");
        root.insertChildren(1, insertTarget);
        assertSame(insertTarget, root.child(1));

        // Test remove
        child1.remove();
        assertEquals(3, root.children().size());
        assertNull(child1.parent());

        // Test empty
        root.empty();
        assertEquals(0, root.children().size());
    }

    @Test
    public void testElementSibling() {
        Element root = new Element(org.jsoup.parser.Tag.valueOf("root"), "");
        Element c1 = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        Element c2 = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        Element c3 = new Element(org.jsoup.parser.Tag.valueOf("div"), "");

        root.appendChild(c1);
        root.appendChild(c2);
        root.appendChild(c3);

        assertEquals(3, c2.siblingElements().size());
        assertNull(c1.previousElementSibling());
        assertSame(c2, c1.nextElementSibling());
        assertSame(c2, c3.previousElementSibling());
        assertNull(c3.nextElementSibling());
    }

    @Test
    public void testTextAndHtml() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.text("Hello & World");
        assertEquals("Hello & World", el.text());
        assertTrue(el.html().contains("Hello"));

        el.html("<span>Inner</span>");
        assertEquals("<span>Inner</span>", el.html().toLowerCase());
        assertEquals("Inner", el.text());
    }

    @Test
    public void testAppendAndPrependText() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.text("First");
        el.appendText(" Second");
        assertEquals("First Second", el.text());

        el.prependText("Zero ");
        assertEquals("Zero First Second", el.text());
    }

    @Test
    public void testTagNameAndId() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("p"), "");
        assertEquals("p", el.tagName());

        el.tagName("div");
        assertEquals("div", el.tagName());

        el.id("my-id");
        assertEquals("my-id", el.id());
        assertEquals("my-id", el.attr("id"));
    }

    @Test
    public void testCssSelectorQueries() {
        Element doc = Jsoup.parse("<div id='wrapper'><p class='text'>Hello</p><p class='text'>World</p></div>").body();
        
        Elements textElements = doc.select(".text");
        assertEquals(2, textElements.size());

        Element first = doc.selectFirst(".text");
        assertNotNull(first);
        assertEquals("Hello", first.text());

        assertTrue(doc.is("#wrapper"));
        assertFalse(doc.is(".text"));
    }

    @Test
    public void testAncestorsAndParents() {
        Element doc = Jsoup.parse("<div><p><span>Deep</span></p></div>").body();
        Element span = doc.selectFirst("span");
        assertNotNull(span);

        Elements parents = span.parents();
        assertEquals(3, parents.size()); // p, div, body (depending on parse tree structure)
        
        Element ownerDoc = span.ownerDocument();
        assertNotNull(ownerDoc);
    }

    @Test
    public void testDataNodeHandling() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("script"), "");
        el.data("var a = 1;");
        
        List<DataNode> dataNodes = el.dataNodes();
        assertEquals(1, dataNodes.size());
        assertEquals("var a = 1;", dataNodes.get(0).getWholeData());
    }

    @Test
    public void testBaseUriHandling() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("a"), "http://example.com/path");
        assertEquals("http://example.com/path", el.baseUri());
        
        el.attr("href", "page.html");
        assertEquals("http://example.com/page.html", el.absUrl("href"));
    }

    @Test
    public void testShallowClone() {
        Element el = new Element(org.jsoup.parser.Tag.valueOf("div"), "");
        el.attr("class", "sample");
        el.appendChild(new Element(org.jsoup.parser.Tag.valueOf("span"), ""));

        Element shallow = el.shallowClone();
        assertEquals("sample", shallow.attr("class"));
        assertEquals(0, shallow.children().size());
    }
}