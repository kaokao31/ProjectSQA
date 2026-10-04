package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

/**
 * Comprehensive JUnit 4 test suite for org.jsoup.nodes.Node.
 * Designed to achieve high code coverage and detect potential faults,
 * including the known bug in Defects4J Jsoup-13 (outerHtml() on child nodes).
 */
public class NodeTest {

    private Element root;
    private Element div;
    private TextNode textNode;
    private Element span;

    @Before
    public void setUp() {
        // Parse a simple HTML document to create a tree of nodes
        String html = "<html><body><div id='content'>Hello <span>world</span></div></body></html>";
        root = Jsoup.parse(html).body(); // <body> element as root for testing
        div = root.select("div").first();
        textNode = (TextNode) div.childNode(0); // "Hello " text node
        span = div.select("span").first();
    }

    // --- outerHtml() tests (targeting Jsoup-13 bug) ---

    @Test
    public void testOuterHtmlOnRootNode() {
        // Root node (body) should produce its outer HTML
        String outer = root.outerHtml();
        assertNotNull(outer);
        assertTrue(outer.contains("<div"));
        assertTrue(outer.contains("</div>"));
    }

    @Test
    public void testOuterHtmlOnChildElement() {
        // Child element (div) should produce its own outer HTML
        String outer = div.outerHtml();
        assertNotNull(outer);
        assertTrue(outer.startsWith("<div"));
        assertTrue(outer.endsWith("</div>"));
        assertTrue(outer.contains("Hello"));
        assertTrue(outer.contains("<span>world</span>"));
    }

    @Test
    public void testOuterHtmlOnTextNode() {
        // Text node should produce its text content
        String outer = textNode.outerHtml();
        assertEquals("Hello ", outer);
    }

    @Test
    public void testOuterHtmlOnDeepChild() {
        // Deep child (span) should produce its own outer HTML
        String outer = span.outerHtml();
        assertEquals("<span>world</span>", outer);
    }

    @Test
    public void testOuterHtmlOnDetachedNode() {
        // A node not attached to any parent should still produce its outer HTML
        Element detached = new Element(Tag.valueOf("p"), "");
        detached.text("detached");
        String outer = detached.outerHtml();
        assertEquals("<p>detached</p>", outer);
    }

    // --- toString() tests (delegates to outerHtml()) ---

    @Test
    public void testToString() {
        assertEquals(div.outerHtml(), div.toString());
        assertEquals(textNode.outerHtml(), textNode.toString());
    }

    // --- attr() tests ---

    @Test
    public void testAttrGetSet() {
        // Set and get attribute
        div.attr("data-test", "value");
        assertEquals("value", div.attr("data-test"));
        // Get non-existent attribute
        assertEquals("", div.attr("nonexistent"));
        // Set empty value
        div.attr("data-empty", "");
        assertEquals("", div.attr("data-empty"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrNullKey() {
        div.attr(null, "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrEmptyKey() {
        div.attr("", "value");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrGetNullKey() {
        div.attr(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAttrGetEmptyKey() {
        div.attr("");
    }

    // --- absUrl() tests ---

    @Test
    public void testAbsUrlWithBase() {
        // Set base URI on root
        root.setBaseUri("http://example.com/");
        // Add a relative href to div
        div.attr("href", "/path");
        String abs = div.absUrl("href");
        assertEquals("http://example.com/path", abs);
    }

    @Test
    public void testAbsUrlWithAbsoluteUrl() {
        root.setBaseUri("http://example.com/");
        div.attr("href", "http://other.com/page");
        String abs = div.absUrl("href");
        assertEquals("http://other.com/page", abs);
    }

    @Test
    public void testAbsUrlNoBase() {
        // No base URI set
        div.attr("href", "/path");
        String abs = div.absUrl("href");
        assertEquals("", abs); // Should return empty string
    }

    @Test
    public void testAbsUrlNonExistentAttribute() {
        root.setBaseUri("http://example.com/");
        String abs = div.absUrl("nonexistent");
        assertEquals("", abs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlNullKey() {
        div.absUrl(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbsUrlEmptyKey() {
        div.absUrl("");
    }

    // --- baseUri() tests ---

    @Test
    public void testBaseUriGetSet() {
        root.setBaseUri("http://test.com/");
        assertEquals("http://test.com/", root.baseUri());
        // Child should inherit base URI? Actually baseUri is per node, not inherited.
        // But typically set on root. Test child's baseUri after setting root.
        // In Jsoup, baseUri is stored per node, not inherited.
        // So child's baseUri may be empty unless set.
        assertEquals("", div.baseUri());
        div.setBaseUri("http://child.com/");
        assertEquals("http://child.com/", div.baseUri());
    }

    // --- childNodeSize() and childNodes() tests ---

    @Test
    public void testChildNodeSize() {
        // div has two children: text node and span
        assertEquals(2, div.childNodeSize());
        // text node has no children
        assertEquals(0, textNode.childNodeSize());
        // root (body) has one child (div)
        assertEquals(1, root.childNodeSize());
    }

    @Test
    public void testChildNodesList() {
        assertEquals(2, div.childNodes().size());
        assertTrue(div.childNodes().get(0) instanceof TextNode);
        assertTrue(div.childNodes().get(1) instanceof Element);
    }

    // --- parent() tests ---

    @Test
    public void testParent() {
        assertSame(root, div.parent());
        assertSame(div, span.parent());
        assertSame(div, textNode.parent());
        // Root's parent is null (body is root in this test)
        assertNull(root.parent());
    }

    // --- remove() tests ---

    @Test
    public void testRemoveChild() {
        span.remove();
        assertEquals(1, div.childNodeSize()); // only text node left
        assertNull(span.parent());
    }

    @Test
    public void testRemoveRoot() {
        // Removing root should detach it from document
        root.remove();
        assertNull(root.parent());
        // The document's body should now be empty? Actually body is root, so after removal body is detached.
        // But we can't easily check document structure here.
    }

    // --- replaceWith() tests ---

    @Test
    public void testReplaceWith() {
        Element newSpan = new Element(Tag.valueOf("span"), "");
        newSpan.text("new");
        span.replaceWith(newSpan);
        assertEquals(2, div.childNodeSize());
        assertSame(newSpan, div.childNode(1));
        assertNull(span.parent());
        assertSame(div, newSpan.parent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceWithNull() {
        span.replaceWith(null);
    }

    // --- unwrap() tests ---

    @Test
    public void testUnwrapElement() {
        // Unwrap span: its children (text node "world") become children of div
        span.unwrap();
        assertEquals(3, div.childNodeSize()); // text "Hello ", text "world", and span is removed
        // The text "world" should now be a direct child of div
        assertTrue(div.childNode(1) instanceof TextNode);
        assertEquals("world", ((TextNode) div.childNode(1)).text());
        assertNull(span.parent());
    }

    @Test
    public void testUnwrapTextNode() {
        // Unwrap text node should do nothing? Actually text node has no children, so unwrap is a no-op.
        textNode.unwrap();
        assertEquals(2, div.childNodeSize()); // unchanged
        assertSame(div, textNode.parent());
    }

    // --- clone() tests ---

    @Test
    public void testClone() {
        Node clone = div.clone();
        assertNotNull(clone);
        assertNotSame(div, clone);
        assertTrue(clone instanceof Element);
        assertEquals(div.outerHtml(), clone.outerHtml());
        // Cloned node should have no parent
        assertNull(clone.parent());
        // Cloned node's children should also be cloned and have no parent
        for (Node child : clone.childNodes()) {
            assertNull(child.parent());
        }
    }

    // --- siblingIndex() and siblingNodes() tests ---

    @Test
    public void testSiblingIndex() {
        assertEquals(0, div.siblingIndex()); // div is first child of body
        assertEquals(0, textNode.siblingIndex()); // text node is first child of div
        assertEquals(1, span.siblingIndex()); // span is second child of div
    }

    @Test
    public void testSiblingNodes() {
        assertEquals(1, root.siblingNodes().size()); // body has no siblings? Actually body is only child of html? In our parse, body is root, so siblings empty.
        // For div's children:
        assertEquals(2, div.siblingNodes().size());
        assertTrue(div.siblingNodes().contains(textNode));
        assertTrue(div.siblingNodes().contains(span));
    }

    // --- nodeName() tests ---

    @Test
    public void testNodeName() {
        assertEquals("body", root.nodeName());
        assertEquals("div", div.nodeName());
        assertEquals("span", span.nodeName());
        assertEquals("#text", textNode.nodeName());
    }

    // --- attributes() tests ---

    @Test
    public void testAttributes() {
        assertNotNull(div.attributes());
        // Initially div has id attribute
        assertEquals("content", div.attributes().get("id"));
        // Text node has no attributes
        assertTrue(textNode.attributes().isEmpty());
    }

    // --- equals() and hashCode() tests (if applicable) ---

    @Test
    public void testEqualsAndHashCode() {
        // Nodes are not equal by default (identity)
        assertFalse(div.equals(span));
        assertFalse(div.hashCode() == span.hashCode());
        // Clone should not be equal to original
        Node clone = div.clone();
        assertFalse(div.equals(clone));
    }

    // --- Edge case: empty document ---

    @Test
    public void testEmptyDocumentNode() {
        Element emptyBody = new Element(Tag.valueOf("body"), "");
        assertEquals(0, emptyBody.childNodeSize());
        assertEquals("", emptyBody.outerHtml());
        assertNull(emptyBody.parent());
    }

    // --- Edge case: node with only whitespace text ---

    @Test
    public void testWhitespaceTextNode() {
        TextNode ws = new TextNode("   ", "");
        assertEquals("   ", ws.outerHtml());
        assertEquals(0, ws.childNodeSize());
    }

    // --- Edge case: attribute with special characters ---

    @Test
    public void testAttrWithSpecialChars() {
        div.attr("data-test", "a\"b'c<d>e&f");
        assertEquals("a\"b'c<d>e&f", div.attr("data-test"));
    }

    // --- Edge case: multiple attributes ---

    @Test
    public void testMultipleAttributes() {
        div.attr("class", "main");
        div.attr("style", "color:red");
        assertEquals("main", div.attr("class"));
        assertEquals("color:red", div.attr("style"));
        assertEquals("content", div.attr("id")); // original id still present
    }

    // --- Edge case: remove attribute ---

    @Test
    public void testRemoveAttr() {
        div.attr("data-temp", "temp");
        assertEquals("temp", div.attr("data-temp"));
        div.removeAttr("data-temp");
        assertEquals("", div.attr("data-temp"));
    }

    // --- Edge case: set baseUri to null ---

    @Test
    public void testSetBaseUriNull() {
        root.setBaseUri(null);
        assertNull(root.baseUri());
        // absUrl should return empty for relative
        div.attr("href", "/test");
        assertEquals("", div.absUrl("href"));
    }

    // --- Edge case: replaceWith self? ---

    @Test
    public void testReplaceWithSelf() {
        // Replacing a node with itself should be a no-op? Actually it might cause issues.
        // We'll test that it doesn't throw.
        span.replaceWith(span);
        assertSame(div, span.parent());
        assertEquals(2, div.childNodeSize());
    }

    // --- Edge case: unwrap on root? ---

    @Test
    public void testUnwrapRoot() {
        // Unwrapping root (body) should move its children to parent (html) but body has no parent in our test.
        // In Jsoup, unwrap on root does nothing? Actually it throws? Let's test.
        // We'll create a scenario where root has a parent.
        Element html = new Element(Tag.valueOf("html"), "");
        Element body = new Element(Tag.valueOf("body"), "");
        html.appendChild(body);
        body.appendChild(new TextNode("text", ""));
        body.unwrap();
        // After unwrap, body's children become children of html
        assertEquals(1, html.childNodeSize());
        assertTrue(html.childNode(0) instanceof TextNode);
        assertNull(body.parent());
    }

    // --- Edge case: clone with deep hierarchy ---

    @Test
    public void testCloneDeep() {
        Node clone = root.clone();
        assertNotNull(clone);
        assertEquals(root.outerHtml(), clone.outerHtml());
        // Ensure all descendants have no parent
        for (Node child : clone.childNodes()) {
            assertNull(child.parent());
            if (child instanceof Element) {
                for (Node grandchild : child.childNodes()) {
                    assertNull(grandchild.parent());
                }
            }
        }
    }

    // --- Edge case: siblingIndex after removal ---

    @Test
    public void testSiblingIndexAfterRemoval() {
        span.remove();
        // After removal, the remaining text node's sibling index should be 0 (only child)
        assertEquals(0, textNode.siblingIndex());
    }

    // --- Edge case: childNodes() unmodifiable? ---

    @Test(expected = UnsupportedOperationException.class)
    public void testChildNodesUnmodifiable() {
        div.childNodes().add(new TextNode("extra", ""));
    }
}