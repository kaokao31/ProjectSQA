package org.jsoup.nodes;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for Element class targeting high coverage and fault detection (Jsoup bug 43).
 */
public class ElementTest {

    private Element element;
    private Element sameElement;
    private Element differentElement;
    private Element withAttributes;
    private Element emptyElement;

    @Before
    public void setUp() {
        element = new Element("div");
        element.attr("id", "test");
        element.text("Hello");

        sameElement = new Element("div");
        sameElement.attr("id", "test");
        sameElement.text("Hello");

        differentElement = new Element("span");
        differentElement.attr("class", "foo");

        withAttributes = new Element("p");
        withAttributes.attr("style", "color:red");
        withAttributes.text("World");

        emptyElement = new Element("br");
    }

    // --- equals() tests (targeting bug 43: null/non-Element handling) ---
    @Test
    public void testEqualsNull() {
        assertFalse(element.equals(null));
    }

    @Test
    public void testEqualsNonElement() {
        assertFalse(element.equals("some string"));
        assertFalse(element.equals(42));
        assertFalse(element.equals(new Object()));
    }

    @Test
    public void testEqualsSameObject() {
        assertTrue(element.equals(element));
    }

    @Test
    public void testEqualsEqualElements() {
        assertTrue(element.equals(sameElement));
        assertTrue(sameElement.equals(element)); // symmetry
    }

    @Test
    public void testEqualsDifferentTag() {
        assertFalse(element.equals(differentElement));
    }

    @Test
    public void testEqualsDifferentAttributes() {
        Element e1 = new Element("div");
        e1.attr("id", "one");
        Element e2 = new Element("div");
        e2.attr("id", "two");
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsDifferentText() {
        Element e1 = new Element("p");
        e1.text("A");
        Element e2 = new Element("p");
        e2.text("B");
        assertFalse(e1.equals(e2));
    }

    @Test
    public void testEqualsEmptyVsNonEmpty() {
        assertFalse(emptyElement.equals(element));
    }

    // --- hashCode() consistency ---
    @Test
    public void testHashCodeConsistency() {
        assertEquals(element.hashCode(), element.hashCode());
    }

    @Test
    public void testHashCodeEqualObjects() {
        assertEquals(element.hashCode(), sameElement.hashCode());
    }

    @Test
    public void testHashCodeDifferentObjects() {
        // Not required to be different, but should be consistent with equals
        assertFalse(element.equals(differentElement) && element.hashCode() == differentElement.hashCode());
    }

    // --- text() method ---
    @Test
    public void testText() {
        assertEquals("Hello", element.text());
    }

    @Test
    public void testTextEmpty() {
        assertEquals("", emptyElement.text());
    }

    @Test
    public void testTextWithChildren() {
        Element parent = new Element("div");
        Element child = new Element("span");
        child.text("child text");
        parent.appendChild(child);
        assertEquals("child text", parent.text());
    }

    // --- html() and outerHtml() ---
    @Test
    public void testHtml() {
        String html = element.html();
        assertTrue(html.contains("Hello"));
    }

    @Test
    public void testOuterHtml() {
        String outer = element.outerHtml();
        assertTrue(outer.startsWith("<div"));
        assertTrue(outer.endsWith("</div>"));
    }

    @Test
    public void testOuterHtmlEmpty() {
        String outer = emptyElement.outerHtml();
        assertEquals("<br></br>", outer); // or <br /> depending on settings, but Jsoup uses <br></br> by default
    }

    // --- attributes ---
    @Test
    public void testAttributes() {
        assertNotNull(element.attributes());
        assertEquals("test", element.attr("id"));
    }

    @Test
    public void testHasAttr() {
        assertTrue(element.hasAttr("id"));
        assertFalse(element.hasAttr("class"));
    }

    @Test
    public void testRemoveAttr() {
        element.removeAttr("id");
        assertFalse(element.hasAttr("id"));
    }

    // --- child management ---
    @Test
    public void testAppendChild() {
        Element child = new Element("span");
        element.appendChild(child);
        assertEquals(1, element.childrenSize());
        assertSame(child, element.child(0));
    }

    @Test
    public void testPrependChild() {
        Element child = new Element("span");
        element.prependChild(child);
        assertEquals(1, element.childrenSize());
        assertSame(child, element.child(0));
    }

    @Test
    public void testRemoveChild() {
        Element child = new Element("span");
        element.appendChild(child);
        element.removeChild(child);
        assertEquals(0, element.childrenSize());
    }

    @Test
    public void testChildren() {
        Element child1 = new Element("a");
        Element child2 = new Element("b");
        element.appendChild(child1);
        element.appendChild(child2);
        assertEquals(2, element.children().size());
    }

    // --- tag name ---
    @Test
    public void testTagName() {
        assertEquals("div", element.tagName());
    }

    @Test
    public void testTagNameChange() {
        element.tagName("section");
        assertEquals("section", element.tagName());
    }

    // --- id ---
    @Test
    public void testId() {
        element.attr("id", "myId");
        assertEquals("myId", element.id());
    }

    @Test
    public void testIdDefault() {
        assertEquals("", emptyElement.id());
    }

    // --- class names ---
    @Test
    public void testClassNames() {
        element.addClass("foo");
        element.addClass("bar");
        assertTrue(element.hasClass("foo"));
        assertTrue(element.hasClass("bar"));
        assertEquals(2, element.classNames().size());
    }

    @Test
    public void testRemoveClass() {
        element.addClass("foo");
        element.removeClass("foo");
        assertFalse(element.hasClass("foo"));
    }

    @Test
    public void testToggleClass() {
        element.addClass("foo");
        element.toggleClass("foo");
        assertFalse(element.hasClass("foo"));
        element.toggleClass("foo");
        assertTrue(element.hasClass("foo"));
    }

    // --- data attributes ---
    @Test
    public void testDataAttr() {
        element.attr("data-key", "value");
        assertEquals("value", element.dataset().get("key"));
    }

    // --- select (CSS) ---
    @Test
    public void testSelect() {
        Element child = new Element("span");
        child.attr("class", "inner");
        element.appendChild(child);
        assertEquals(1, element.select("span").size());
    }

    // --- edge cases: null/empty ---
    @Test(expected = NullPointerException.class)
    public void testAppendNullChild() {
        element.appendChild(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPrependNullChild() {
        element.prependChild(null);
    }

    @Test
    public void testTextNull() {
        element.text(null);
        assertEquals("", element.text());
    }

    @Test
    public void testAttrNullKey() {
        element.attr(null, "value");
        // should not throw, but attribute may not be set
    }

    @Test
    public void testAttrNullValue() {
        element.attr("key", null);
        assertEquals("", element.attr("key"));
    }

    // --- toString ---
    @Test
    public void testToString() {
        String str = element.toString();
        assertTrue(str.contains("div"));
    }

    // --- clone ---
    @Test
    public void testClone() {
        Element clone = element.clone();
        assertTrue(element.equals(clone));
        assertNotSame(element, clone);
    }

    // --- sibling methods ---
    @Test
    public void testSiblingElements() {
        Element parent = new Element("ul");
        Element li1 = new Element("li");
        Element li2 = new Element("li");
        parent.appendChild(li1);
        parent.appendChild(li2);
        assertEquals(1, li1.siblingElements().size());
        assertSame(li2, li1.siblingElements().get(0));
    }

    @Test
    public void testNextSibling() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(b, a.nextElementSibling());
    }

    @Test
    public void testPreviousSibling() {
        Element parent = new Element("div");
        Element a = new Element("a");
        Element b = new Element("b");
        parent.appendChild(a);
        parent.appendChild(b);
        assertSame(a, b.previousElementSibling());
    }

    // --- parent ---
    @Test
    public void testParent() {
        Element parent = new Element("div");
        parent.appendChild(element);
        assertSame(parent, element.parent());
    }

    @Test
    public void testParentNull() {
        assertNull(element.parent());
    }

    // --- root ---
    @Test
    public void testRoot() {
        assertSame(element, element.root());
        Element parent = new Element("div");
        parent.appendChild(element);
        assertSame(parent, element.root());
    }

    // --- ownText ---
    @Test
    public void testOwnText() {
        element.text("Hello");
        assertEquals("Hello", element.ownText());
    }

    @Test
    public void testOwnTextWithChildren() {
        Element parent = new Element("div");
        parent.text("Parent");
        Element child = new Element("span");
        child.text("Child");
        parent.appendChild(child);
        assertEquals("Parent", parent.ownText());
    }

    // --- wholeText ---
    @Test
    public void testWholeText() {
        element.text("Hello");
        assertEquals("Hello", element.wholeText());
    }

    // --- data ---
    @Test
    public void testData() {
        Element script = new Element("script");
        script.data("alert('hi');");
        assertEquals("alert('hi');", script.data());
    }

    // --- val ---
    @Test
    public void testVal() {
        Element input = new Element("input");
        input.attr("value", "test");
        assertEquals("test", input.val());
    }

    @Test
    public void testValTextarea() {
        Element textarea = new Element("textarea");
        textarea.text("content");
        assertEquals("content", textarea.val());
    }

    // --- html builder ---
    @Test
    public void testHtmlWithAppend() {
        element.append("<span>extra</span>");
        assertEquals(1, element.childrenSize());
        assertEquals("extra", element.child(0).text());
    }

    @Test
    public void testPrepend() {
        element.prepend("<span>first</span>");
        assertEquals(1, element.childrenSize());
        assertEquals("first", element.child(0).text());
    }

    // --- wrap ---
    @Test
    public void testWrap() {
        element.wrap("<div class='wrapper'></div>");
        assertEquals("wrapper", element.parent().attr("class"));
    }

    // --- empty ---
    @Test
    public void testEmpty() {
        element.appendChild(new Element("span"));
        element.empty();
        assertEquals(0, element.childrenSize());
    }

    // --- css selector edge cases ---
    @Test
    public void testSelectEmpty() {
        assertEquals(0, element.select("nonexistent").size());
    }

    @Test
    public void testSelectNull() {
        try {
            element.select(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- equals with different attribute order ---
    @Test
    public void testEqualsDifferentAttributeOrder() {
        Element e1 = new Element("div");
        e1.attr("a", "1");
        e1.attr("b", "2");
        Element e2 = new Element("div");
        e2.attr("b", "2");
        e2.attr("a", "1");
        assertTrue(e1.equals(e2));
    }

    // --- equals with child elements (should not consider children) ---
    @Test
    public void testEqualsIgnoresChildren() {
        Element e1 = new Element("div");
        Element e2 = new Element("div");
        e1.appendChild(new Element("span"));
        assertTrue(e1.equals(e2)); // equals should not depend on children
    }

    // --- hashCode with attributes ---
    @Test
    public void testHashCodeWithAttributes() {
        Element e1 = new Element("div");
        e1.attr("id", "x");
        Element e2 = new Element("div");
        e2.attr("id", "x");
        assertEquals(e1.hashCode(), e2.hashCode());
    }
}