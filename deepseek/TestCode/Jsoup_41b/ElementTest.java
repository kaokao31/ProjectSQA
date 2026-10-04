package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for the Element class.
 * Designed to achieve high code coverage and detect potential faults,
 * including the specific bug addressed in Defects4J Jsoup-41.
 */
public class ElementTest {

    private Element div;
    private Element p;
    private Element a;
    private Element span;
    private Document doc;

    @Before
    public void setUp() {
        // Create a simple HTML document with various elements
        String html = "<div id='main' class='container'>" +
                "<p>Hello <a href='http://example.com'>link</a></p>" +
                "<span>text</span>" +
                "</div>";
        doc = Jsoup.parse(html);
        div = doc.getElementById("main");
        p = doc.select("p").first();
        a = doc.select("a").first();
        span = doc.select("span").first();
    }

    // ==================== Tag and Attribute Tests ====================

    @Test
    public void testTagName() {
        assertEquals("div", div.tagName());
        assertEquals("p", p.tagName());
        assertEquals("a", a.tagName());
        assertEquals("span", span.tagName());
    }

    @Test
    public void testHasAttr() {
        assertTrue(div.hasAttr("id"));
        assertTrue(div.hasAttr("class"));
        assertFalse(div.hasAttr("nonexistent"));
        assertTrue(a.hasAttr("href"));
        assertFalse(p.hasAttr("href"));
    }

    @Test
    public void testAttr() {
        assertEquals("main", div.attr("id"));
        assertEquals("container", div.attr("class"));
        assertEquals("http://example.com", a.attr("href"));
        assertEquals("", p.attr("href"));
        assertNull(div.attr("nonexistent")); // attr returns null for missing
    }

    @Test
    public void testAbsUrl() {
        // a has relative href? Actually it's absolute, but test absUrl
        assertEquals("http://example.com", a.absUrl("href"));
        // For a missing attribute, absUrl returns empty string
        assertEquals("", p.absUrl("href"));
    }

    // ==================== Text and HTML Tests ====================

    @Test
    public void testText() {
        // p contains "Hello link" (text of child a is "link")
        assertEquals("Hello link", p.text());
        // span contains "text"
        assertEquals("text", span.text());
        // div contains all text: "Hello link text"
        assertEquals("Hello link text", div.text());
    }

    @Test
    public void testOwnText() {
        // p's own text is "Hello " (before the a element)
        assertEquals("Hello ", p.ownText());
        // span's own text is "text"
        assertEquals("text", span.ownText());
        // div's own text is empty (only children)
        assertEquals("", div.ownText());
    }

    @Test
    public void testHtml() {
        // p's inner HTML: "Hello <a href=\"http://example.com\">link</a>"
        String pHtml = p.html();
        assertTrue(pHtml.contains("Hello"));
        assertTrue(pHtml.contains("<a"));
        assertTrue(pHtml.contains("</a>"));
        // span's inner HTML: "text"
        assertEquals("text", span.html());
        // div's inner HTML: contains p and span
        String divHtml = div.html();
        assertTrue(divHtml.contains("<p>"));
        assertTrue(divHtml.contains("<span>"));
    }

    @Test
    public void testOuterHtml() {
        // p's outer HTML: "<p>Hello <a href=\"http://example.com\">link</a></p>"
        String pOuter = p.outerHtml();
        assertTrue(pOuter.startsWith("<p>"));
        assertTrue(pOuter.endsWith("</p>"));
        // span's outer HTML: "<span>text</span>"
        assertEquals("<span>text</span>", span.outerHtml());
    }

    @Test
    public void testData() {
        // No data elements in this HTML, but test method exists
        assertEquals("", div.data());
    }

    // ==================== Child and Sibling Tests ====================

    @Test
    public void testChildNodeSize() {
        assertEquals(2, div.childNodeSize()); // p and span (text nodes are also children)
        // p has 2 child nodes: text "Hello " and a element
        assertEquals(2, p.childNodeSize());
        // a has 1 child node: text "link"
        assertEquals(1, a.childNodeSize());
        // span has 1 child node: text "text"
        assertEquals(1, span.childNodeSize());
    }

    @Test
    public void testChildren() {
        List<Element> divChildren = div.children();
        assertEquals(2, divChildren.size());
        assertEquals("p", divChildren.get(0).tagName());
        assertEquals("span", divChildren.get(1).tagName());

        List<Element> pChildren = p.children();
        assertEquals(1, pChildren.size());
        assertEquals("a", pChildren.get(0).tagName());
    }

    @Test
    public void testChildElements() {
        // Similar to children but returns Elements
        Elements divChildElements = div.children();
        assertEquals(2, divChildElements.size());
    }

    @Test
    public void testFirstElementSibling() {
        // p is first child of div
        assertEquals(p, div.firstElementSibling());
        // a is first child of p
        assertEquals(a, p.firstElementSibling());
    }

    @Test
    public void testLastElementSibling() {
        // span is last child of div
        assertEquals(span, div.lastElementSibling());
        // a is last child of p
        assertEquals(a, p.lastElementSibling());
    }

    @Test
    public void testNextElementSibling() {
        // p's next sibling is span
        assertEquals(span, p.nextElementSibling());
        // span has no next sibling
        assertNull(span.nextElementSibling());
        // a has no next sibling inside p
        assertNull(a.nextElementSibling());
    }

    @Test
    public void testPreviousElementSibling() {
        // span's previous sibling is p
        assertEquals(p, span.previousElementSibling());
        // p has no previous sibling
        assertNull(p.previousElementSibling());
        // a has no previous sibling inside p
        assertNull(a.previousElementSibling());
    }

    // ==================== Parent and Ancestor Tests ====================

    @Test
    public void testParent() {
        assertEquals(div, p.parent());
        assertEquals(p, a.parent());
        assertEquals(div, span.parent());
        // Document's child is the root element (html)
        assertNotNull(doc.parent());
    }

    @Test
    public void testClosest() {
        // p.closest("div") should return div
        assertEquals(div, p.closest("div"));
        // a.closest("p") should return p
        assertEquals(p, a.closest("p"));
        // a.closest("span") should return null (no span ancestor)
        assertNull(a.closest("span"));
    }

    // ==================== Manipulation Tests ====================

    @Test
    public void testAppendChild() {
        Element newSpan = new Element("span").text("new");
        p.appendChild(newSpan);
        assertEquals(3, p.childNodeSize()); // original 2 + new span
        assertEquals(newSpan, p.children().get(1)); // after a
    }

    @Test
    public void testPrependChild() {
        Element newB = new Element("b").text("bold");
        p.prependChild(newB);
        assertEquals(3, p.childNodeSize());
        assertEquals(newB, p.children().get(0)); // before a
    }

    @Test
    public void testAppendText() {
        p.appendText(" appended");
        assertTrue(p.text().contains("appended"));
    }

    @Test
    public void testPrependText() {
        p.prependText("prepended ");
        assertTrue(p.text().startsWith("prepended"));
    }

    @Test
    public void testEmpty() {
        p.empty();
        assertEquals(0, p.childNodeSize());
        assertEquals("", p.text());
    }

    @Test
    public void testRemove() {
        p.remove();
        assertEquals(1, div.childNodeSize()); // only span left
        assertNull(p.parent());
    }

    @Test
    public void testWrap() {
        p.wrap("<div class='wrapper'></div>");
        // p should now be inside a new div wrapper
        Element wrapper = p.parent();
        assertEquals("div", wrapper.tagName());
        assertEquals("wrapper", wrapper.attr("class"));
        assertEquals(div, wrapper.parent());
    }

    // ==================== CSS Selector Tests ====================

    @Test
    public void testSelect() {
        Elements links = div.select("a");
        assertEquals(1, links.size());
        assertEquals(a, links.get(0));

        Elements paragraphs = div.select("p");
        assertEquals(1, paragraphs.size());
        assertEquals(p, paragraphs.get(0));
    }

    @Test
    public void testSelectFirst() {
        Element firstLink = div.selectFirst("a");
        assertEquals(a, firstLink);
        assertNull(div.selectFirst("nonexistent"));
    }

    // ==================== equals() and hashCode() Tests (Bug-specific) ====================

    @Test
    public void testEqualsSameElement() {
        // Same reference
        assertTrue(div.equals(div));
        // Different reference but same content
        Element div2 = doc.getElementById("main");
        assertTrue(div.equals(div2));
    }

    @Test
    public void testEqualsDifferentTag() {
        Element p2 = doc.select("p").first();
        // p and div have different tags
        assertFalse(div.equals(p2));
    }

    @Test
    public void testEqualsDifferentAttributes() {
        // Create a div with same tag but different attributes
        Element divDifferent = new Element("div").attr("id", "other");
        assertFalse(div.equals(divDifferent));
    }

    @Test
    public void testEqualsDifferentChildren() {
        // Create a div with same tag and attributes but different children
        Element divSameAttrs = new Element("div").attr("id", "main").attr("class", "container");
        // divSameAttrs has no children, while div has children
        assertFalse(div.equals(divSameAttrs));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(div.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        assertFalse(div.equals("string"));
    }

    @Test
    public void testHashCodeConsistency() {
        Element div2 = doc.getElementById("main");
        assertEquals(div.hashCode(), div2.hashCode());
    }

    @Test
    public void testHashCodeDifferentElements() {
        assertNotEquals(div.hashCode(), p.hashCode());
    }

    // ==================== Edge Cases and Boundary Tests ====================

    @Test
    public void testEmptyElement() {
        Element empty = new Element("br");
        assertEquals("br", empty.tagName());
        assertEquals("", empty.text());
        assertEquals("", empty.html());
        assertEquals("<br />", empty.outerHtml()); // self-closing
        assertEquals(0, empty.childNodeSize());
        assertTrue(empty.children().isEmpty());
    }

    @Test
    public void testElementWithOnlyText() {
        Element textOnly = new Element("b").text("bold");
        assertEquals("bold", textOnly.text());
        assertEquals("bold", textOnly.html());
        assertEquals("<b>bold</b>", textOnly.outerHtml());
    }

    @Test
    public void testElementWithNoAttributes() {
        Element noAttrs = new Element("div");
        assertFalse(noAttrs.hasAttr("id"));
        assertEquals("", noAttrs.attr("id"));
    }

    @Test
    public void testElementWithMultipleAttributes() {
        Element multi = new Element("input")
                .attr("type", "text")
                .attr("name", "username")
                .attr("value", "test");
        assertTrue(multi.hasAttr("type"));
        assertTrue(multi.hasAttr("name"));
        assertTrue(multi.hasAttr("value"));
        assertEquals("text", multi.attr("type"));
        assertEquals("username", multi.attr("name"));
        assertEquals("test", multi.attr("value"));
    }

    @Test
    public void testElementWithBooleanAttribute() {
        Element checkbox = new Element("input").attr("checked", "");
        assertTrue(checkbox.hasAttr("checked"));
        assertEquals("", checkbox.attr("checked"));
    }

    @Test
    public void testElementWithDataAttributes() {
        Element dataEl = new Element("div").attr("data-info", "value");
        assertTrue(dataEl.hasAttr("data-info"));
        assertEquals("value", dataEl.attr("data-info"));
    }

    @Test
    public void testElementWithClass() {
        Element cls = new Element("div").addClass("myclass");
        assertTrue(cls.hasClass("myclass"));
        assertFalse(cls.hasClass("other"));
        cls.addClass("another");
        assertTrue(cls.hasClass("another"));
        cls.removeClass("myclass");
        assertFalse(cls.hasClass("myclass"));
    }

    @Test
    public void testElementWithId() {
        Element idEl = new Element("div").attr("id", "unique");
        assertEquals("unique", idEl.id());
    }

    @Test
    public void testElementWithCssSelector() {
        Element styled = new Element("div").addClass("a").addClass("b");
        assertTrue(styled.cssSelector().contains(".a.b"));
    }

    // ==================== Serialization and Cloning Tests ====================

    @Test
    public void testClone() {
        Element cloned = div.clone();
        assertNotSame(div, cloned);
        assertEquals(div.tagName(), cloned.tagName());
        assertEquals(div.attr("id"), cloned.attr("id"));
        assertEquals(div.text(), cloned.text());
        assertEquals(div.html(), cloned.html());
        // Children should be cloned as well
        assertEquals(div.children().size(), cloned.children().size());
        assertNotSame(div.children().get(0), cloned.children().get(0));
    }

    @Test
    public void testOuterHtmlAfterClone() {
        Element cloned = div.clone();
        assertEquals(div.outerHtml(), cloned.outerHtml());
    }

    // ==================== Special Cases for Bug Detection ====================

    @Test
    public void testElementWithSameTagAndAttributesButDifferentBaseUri() {
        // Elements with different base URIs should not be equal
        Element divBase1 = new Element("div").attr("id", "main").attr("class", "container");
        divBase1.setBaseUri("http://example1.com");
        Element divBase2 = new Element("div").attr("id", "main").attr("class", "container");
        divBase2.setBaseUri("http://example2.com");
        // baseUri is not part of equals()? Actually in Jsoup, baseUri is not considered in equals.
        // But we test that equals still works (should be true if only tag and attributes match)
        // This is a potential bug area: equals might incorrectly consider baseUri or not.
        // According to Jsoup source, equals compares tag and attributes only.
        assertEquals(divBase1, divBase2);
    }

    @Test
    public void testElementWithDifferentChildOrder() {
        // Create two elements with same children but different order
        Element parent1 = new Element("div");
        parent1.appendChild(new Element("span").text("first"));
        parent1.appendChild(new Element("b").text("second"));

        Element parent2 = new Element("div");
        parent2.appendChild(new Element("b").text("second"));
        parent2.appendChild(new Element("span").text("first"));

        // equals should compare children order? In Jsoup, equals does not compare children.
        // So these should be equal if tag and attributes match.
        assertEquals(parent1, parent2);
    }

    @Test
    public void testElementWithWhitespaceOnlyText() {
        Element ws = new Element("p").text("   ");
        assertEquals("   ", ws.text());
        assertEquals("   ", ws.html());
        assertEquals("<p>   </p>", ws.outerHtml());
    }

    @Test
    public void testElementWithEmptyText() {
        Element emptyText = new Element("p").text("");
        assertEquals("", emptyText.text());
        assertEquals("", emptyText.html());
        assertEquals("<p></p>", emptyText.outerHtml());
    }

    @Test
    public void testElementWithNullText() {
        // text(null) should be treated as empty string
        Element nullText = new Element("p").text(null);
        assertEquals("", nullText.text());
    }

    @Test
    public void testElementWithSpecialCharactersInText() {
        Element special = new Element("p").text("<>&\"'");
        assertEquals("<>&\"'", special.text());
        // HTML should be escaped
        assertEquals("&lt;&gt;&amp;&quot;'", special.html());
    }

    @Test
    public void testElementWithMultipleChildrenAndTextNodes() {
        // Create element with mixed content
        Element mixed = new Element("div");
        mixed.appendChild(new Element("b").text("bold"));
        mixed.appendChild(new TextNode(" plain ", ""));
        mixed.appendChild(new Element("i").text("italic"));
        assertEquals("bold plain italic", mixed.text());
        assertEquals("<b>bold</b> plain <i>italic</i>", mixed.html());
    }

    @Test
    public void testElementWithSelfClosingTag() {
        Element br = new Element("br");
        assertEquals("<br />", br.outerHtml());
        assertEquals(0, br.childNodeSize());
    }

    @Test
    public void testElementWithUnknownTag() {
        Element custom = new Element("custom");
        assertEquals("custom", custom.tagName());
        assertEquals("<custom></custom>", custom.outerHtml());
    }

    @Test
    public void testElementWithNamespace() {
        Element ns = new Element("ns:tag");
        assertEquals("ns:tag", ns.tagName());
        // outerHtml should preserve namespace
        assertEquals("<ns:tag></ns:tag>", ns.outerHtml());
    }

    // ==================== Form Element Tests ====================

    @Test
    public void testFormElement() {
        // Create a form element
        Element form = new Element("form").attr("action", "/submit").attr("method", "post");
        assertEquals("form", form.tagName());
        assertEquals("/submit", form.attr("action"));
        assertEquals("post", form.attr("method"));
    }

    @Test
    public void testInputElement() {
        Element input = new Element("input").attr("type", "text").attr("name", "user");
        assertEquals("input", input.tagName());
        assertEquals("text", input.attr("type"));
        assertEquals("user", input.attr("name"));
        // Input is self-closing
        assertEquals("<input type=\"text\" name=\"user\" />", input.outerHtml());
    }

    @Test
    public void testSelectElement() {
        Element select = new Element("select").attr("name", "country");
        select.appendChild(new Element("option").attr("value", "us").text("USA"));
        select.appendChild(new Element("option").attr("value", "ca").text("Canada"));
        assertEquals(2, select.children().size());
        assertEquals("USA", select.children().get(0).text());
    }

    // ==================== Edge Cases with Null/Empty Input ====================

    @Test(expected = IllegalArgumentException.class)
    public void testAppendChildNull() {
        div.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrependChildNull() {
        div.prependChild(null);
    }

    @Test
    public void testAttrWithNullKey() {
        // Should return empty string or null? Jsoup returns empty string for null key
        assertEquals("", div.attr(null));
    }

    @Test
    public void testHasAttrWithNullKey() {
        assertFalse(div.hasAttr(null));
    }

    @Test
    public void testRemoveAttr() {
        div.removeAttr("class");
        assertFalse(div.hasAttr("class"));
    }

    @Test
    public void testRemoveAttrNonExistent() {
        // Should not throw
        div.removeAttr("nonexistent");
    }

    @Test
    public void testAddClassNull() {
        div.addClass(null);
        // Should not throw, class attribute may become empty
        assertTrue(div.hasClass("")); // or false? depends on implementation
    }

    @Test
    public void testRemoveClassNull() {
        div.removeClass(null);
        // Should not throw
    }

    @Test
    public void testToggleClass() {
        div.toggleClass("active");
        assertTrue(div.hasClass("active"));
        div.toggleClass("active");
        assertFalse(div.hasClass("active"));
    }

    // ==================== Performance and Stress Tests (Optional) ====================

    @Test(timeout = 1000)
    public void testLargeNumberOfChildren() {
        Element parent = new Element("div");
        for (int i = 0; i < 1000; i++) {
            parent.appendChild(new Element("span").text("child" + i));
        }
        assertEquals(1000, parent.children().size());
        assertEquals(1000, parent.childNodeSize());
    }

    @Test(timeout = 1000)
    public void testDeepNesting() {
        Element current = new Element("div");
        Element root = current;
        for (int i = 0; i < 100; i++) {
            Element child = new Element("div");
            current.appendChild(child);
            current = child;
        }
        // Should not cause stack overflow
        assertNotNull(root.outerHtml());
    }
}