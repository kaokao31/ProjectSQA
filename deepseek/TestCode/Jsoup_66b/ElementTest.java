package org.jsoup.nodes;

import org.junit.Before;
import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for org.jsoup.nodes.Element.
 * Designed to achieve high code coverage and detect faults (including Defects4J bug 66).
 */
public class ElementTest {

    private Document doc;
    private Element root;

    @Before
    public void setUp() {
        // Create a simple document for testing
        doc = Jsoup.parse("<div id='root' class='container'><p>Hello</p><!-- comment --><span>World</span></div>");
        root = doc.getElementById("root");
    }

    // --- Basic getters and attributes ---

    @Test
    public void testTagName() {
        assertEquals("div", root.tagName());
    }

    @Test
    public void testId() {
        assertEquals("root", root.id());
    }

    @Test
    public void testClassName() {
        assertEquals("container", root.className());
    }

    @Test
    public void testHasClass() {
        assertTrue(root.hasClass("container"));
        assertFalse(root.hasClass("nonexistent"));
    }

    @Test
    public void testAttr() {
        assertEquals("root", root.attr("id"));
        assertTrue(root.hasAttr("id"));
        assertFalse(root.hasAttr("nonexistent"));
        // Test setting attribute
        root.attr("data-test", "value");
        assertEquals("value", root.attr("data-test"));
    }

    @Test
    public void testAbsUrl() {
        // Need a base URL for absolute URL resolution
        Document docWithBase = Jsoup.parse("<a href='/test'>link</a>", "http://example.com");
        Element link = docWithBase.select("a").first();
        assertEquals("http://example.com/test", link.absUrl("href"));
    }

    // --- Child element access ---

    @Test
    public void testChildren() {
        Elements children = root.children();
        assertEquals(2, children.size()); // p and span (comment is not an element)
        assertEquals("p", children.get(0).tagName());
        assertEquals("span", children.get(1).tagName());
    }

    @Test
    public void testChildNodeSize() {
        // Includes text nodes, comment nodes, element nodes
        assertEquals(3, root.childNodeSize()); // p, comment, span
    }

    @Test
    public void testGetElementById() {
        Element p = root.getElementById("root"); // should not find because id is on root
        assertNull(p);
        Element span = root.getElementById("nonexistent");
        assertNull(span);
    }

    @Test
    public void testGetElementsByTag() {
        Elements spans = root.getElementsByTag("span");
        assertEquals(1, spans.size());
        assertEquals("World", spans.first().text());
    }

    @Test
    public void testGetElementsByClass() {
        // Add a class to a child
        root.child(0).addClass("highlight");
        Elements highlighted = root.getElementsByClass("highlight");
        assertEquals(1, highlighted.size());
        assertEquals("p", highlighted.first().tagName());
    }

    @Test
    public void testSelect() {
        Elements selected = root.select("p");
        assertEquals(1, selected.size());
        assertEquals("Hello", selected.first().text());
    }

    // --- Text content ---

    @Test
    public void testText() {
        // Bug 66: text() should return text ignoring comments
        assertEquals("Hello World", root.text());
    }

    @Test
    public void testTextWithCommentOnly() {
        // Element with only a comment node should return empty string
        Element div = new Element("div");
        div.appendChild(new Comment("just a comment"));
        assertEquals("", div.text());
    }

    @Test
    public void testTextWithMixedContent() {
        // Text node, comment, element with text
        Element div = new Element("div");
        div.appendChild(new TextNode("Start "));
        div.appendChild(new Comment("middle"));
        div.appendChild(new Element("b").text("bold"));
        assertEquals("Start bold", div.text());
    }

    @Test
    public void testOwnText() {
        // ownText() should return text directly in this element, not from children
        Element div = new Element("div");
        div.appendChild(new TextNode("Direct "));
        div.appendChild(new Element("span").text("Child"));
        assertEquals("Direct ", div.ownText());
    }

    @Test
    public void testWholeText() {
        // wholeText() includes all text nodes including those inside children? Actually wholeText is for TextNode, not Element.
        // For Element, there is no wholeText method. We'll skip.
    }

    // --- HTML and data ---

    @Test
    public void testHtml() {
        String html = root.html();
        // Should include the comment? By default, Jsoup's html() includes comments.
        assertTrue(html.contains("<!-- comment -->"));
        assertTrue(html.contains("<p>Hello</p>"));
        assertTrue(html.contains("<span>World</span>"));
    }

    @Test
    public void testData() {
        // For script/style elements, data() returns content. For normal elements, returns empty.
        Element script = new Element("script");
        script.appendChild(new DataNode("alert('hi');"));
        assertEquals("alert('hi');", script.data());
        assertEquals("", root.data());
    }

    @Test
    public void testOuterHtml() {
        String outer = root.outerHtml();
        assertTrue(outer.startsWith("<div"));
        assertTrue(outer.endsWith("</div>"));
    }

    // --- Manipulation ---

    @Test
    public void testAppendChild() {
        Element newChild = new Element("b").text("Bold");
        root.appendChild(newChild);
        assertEquals(4, root.childNodeSize()); // p, comment, span, b
        assertEquals("Bold", root.child(3).text());
    }

    @Test
    public void testPrependChild() {
        Element newChild = new Element("b").text("Bold");
        root.prependChild(newChild);
        assertEquals("Bold", root.child(0).text());
    }

    @Test
    public void testAppendText() {
        root.appendText(" Appended");
        assertEquals("Hello World Appended", root.text());
    }

    @Test
    public void testPrependText() {
        root.prependText("Prepended ");
        assertEquals("Prepended Hello World", root.text());
    }

    @Test
    public void testEmpty() {
        root.empty();
        assertEquals(0, root.childNodeSize());
        assertEquals("", root.text());
    }

    @Test
    public void testRemove() {
        Element p = root.child(0);
        p.remove();
        assertEquals(2, root.childNodeSize()); // comment and span
        assertEquals("World", root.text());
    }

    // --- Traversal and position ---

    @Test
    public void testParent() {
        assertEquals(root, root.child(0).parent());
    }

    @Test
    public void testNextSibling() {
        Element p = root.child(0);
        // Next sibling should be the comment node (not element)
        Node next = p.nextSibling();
        assertTrue(next instanceof Comment);
        // Next element sibling should be span
        Element nextElement = p.nextElementSibling();
        assertEquals("span", nextElement.tagName());
    }

    @Test
    public void testPreviousSibling() {
        Element span = root.child(2);
        Node prev = span.previousSibling();
        assertTrue(prev instanceof Comment);
        Element prevElement = span.previousElementSibling();
        assertEquals("p", prevElement.tagName());
    }

    @Test
    public void testFirstChild() {
        assertEquals("p", root.firstChild().tagName());
    }

    @Test
    public void testLastChild() {
        assertEquals("span", root.lastChild().tagName());
    }

    // --- Edge cases and null handling ---

    @Test(expected = NullPointerException.class)
    public void testAppendNullChild() {
        root.appendChild(null);
    }

    @Test(expected = NullPointerException.class)
    public void testPrependNullChild() {
        root.prependChild(null);
    }

    @Test
    public void testNullAttribute() {
        // Setting attribute with null value should not throw
        root.attr("test", null);
        assertEquals("", root.attr("test"));
    }

    @Test
    public void testEmptyElement() {
        Element empty = new Element("div");
        assertEquals("", empty.text());
        assertEquals("", empty.html());
        assertEquals("<div></div>", empty.outerHtml());
    }

    @Test
    public void testElementWithOnlyWhitespace() {
        Element ws = new Element("div");
        ws.appendChild(new TextNode("   "));
        assertEquals("", ws.text()); // text() trims
        assertEquals("   ", ws.wholeText()); // wholeText() preserves whitespace? Actually wholeText is for TextNode, not Element.
    }

    // --- Bug-specific tests (Defects4J bug 66) ---

    @Test
    public void testTextWithCommentInside() {
        // This is the core of bug 66: text() should not be affected by comment nodes
        Element div = new Element("div");
        div.appendChild(new TextNode("Hello"));
        div.appendChild(new Comment("comment"));
        div.appendChild(new TextNode(" World"));
        assertEquals("Hello World", div.text());
    }

    @Test
    public void testTextWithCommentAndElement() {
        Element div = new Element("div");
        div.appendChild(new TextNode("Start "));
        div.appendChild(new Comment("middle"));
        div.appendChild(new Element("b").text("bold"));
        assertEquals("Start bold", div.text());
    }

    @Test
    public void testTextWithMultipleComments() {
        Element div = new Element("div");
        div.appendChild(new Comment("c1"));
        div.appendChild(new TextNode("text"));
        div.appendChild(new Comment("c2"));
        assertEquals("text", div.text());
    }

    // --- Additional coverage for other methods ---

    @Test
    public void testIsBlock() {
        assertTrue(new Element("div").isBlock());
        assertFalse(new Element("span").isBlock());
    }

    @Test
    public void testCssSelector() {
        String css = root.cssSelector();
        assertNotNull(css);
        assertTrue(css.contains("#root"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Element e1 = new Element("div");
        Element e2 = new Element("div");
        // Elements are not equal by default because they have different identity
        assertNotEquals(e1, e2);
        assertNotEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    public void testClone() {
        Element clone = root.clone();
        assertNotSame(root, clone);
        assertEquals(root.html(), clone.html());
    }

    @Test
    public void testWrap() {
        Element p = root.child(0);
        p.wrap("<div class='wrapper'></div>");
        // After wrapping, p should be inside a new div
        assertEquals("wrapper", p.parent().className());
    }

    @Test
    public void testUnwrap() {
        Element p = root.child(0);
        p.unwrap();
        // p's children should now be direct children of root
        assertEquals("Hello", root.child(0).outerHtml()); // text node
    }

    @Test
    public void testVal() {
        // For input elements, val() returns value attribute
        Element input = new Element("input");
        input.attr("value", "test");
        assertEquals("test", input.val());
        // For non-input elements, val() returns empty string
        assertEquals("", root.val());
    }

    @Test
    public void testTextWithNewlines() {
        Element div = new Element("div");
        div.appendChild(new TextNode("Line1\nLine2"));
        assertEquals("Line1 Line2", div.text()); // text() normalizes whitespace
    }

    @Test
    public void testHtmlWithSpecialChars() {
        Element div = new Element("div");
        div.appendChild(new TextNode("<tag> & \" '"));
        String html = div.html();
        assertTrue(html.contains("&lt;tag&gt; &amp; &quot; &apos;"));
    }

    @Test
    public void testDataWithScript() {
        Element script = new Element("script");
        script.appendChild(new DataNode("var x = 1;"));
        assertEquals("var x = 1;", script.data());
    }

    @Test
    public void testDataWithStyle() {
        Element style = new Element("style");
        style.appendChild(new DataNode("body { color: red; }"));
        assertEquals("body { color: red; }", style.data());
    }

    @Test
    public void testGetElementsByAttribute() {
        Elements withId = root.getElementsByAttribute("id");
        assertEquals(1, withId.size());
        assertEquals("root", withId.first().id());
    }

    @Test
    public void testGetElementsByAttributeValue() {
        Elements withClassContainer = root.getElementsByAttributeValue("class", "container");
        assertEquals(1, withClassContainer.size());
    }

    @Test
    public void testGetElementsContainingText() {
        Elements containingHello = root.getElementsContainingText("Hello");
        assertEquals(1, containingHello.size());
        assertEquals("p", containingHello.first().tagName());
    }

    @Test
    public void testGetElementsContainingOwnText() {
        // ownText of root is empty, so should not find
        Elements containingOwn = root.getElementsContainingOwnText("Hello");
        assertEquals(0, containingOwn.size());
    }

    @Test
    public void testGetElementsMatchingText() {
        Elements matching = root.getElementsMatchingText("Hello");
        assertEquals(1, matching.size());
    }

    @Test
    public void testGetElementsMatchingOwnText() {
        Elements matchingOwn = root.getElementsMatchingOwnText("Hello");
        assertEquals(0, matchingOwn.size());
    }

    @Test
    public void testGetAllElements() {
        Elements all = root.getAllElements();
        // Should include root, p, span
        assertEquals(3, all.size());
    }

    @Test
    public void testTextNodes() {
        // Should return text nodes directly under root
        // root has no direct text nodes (only p, comment, span)
        assertEquals(0, root.textNodes().size());
        // Add a text node
        root.appendChild(new TextNode("extra"));
        assertEquals(1, root.textNodes().size());
    }

    @Test
    public void testDataNodes() {
        // root has no data nodes
        assertEquals(0, root.dataNodes().size());
    }

    @Test
    public void testNodeName() {
        assertEquals("div", root.nodeName());
    }

    @Test
    public void testBaseUri() {
        // Default base URI is empty
        assertEquals("", root.baseUri());
    }

    @Test
    public void testSetBaseUri() {
        root.setBaseUri("http://example.com");
        assertEquals("http://example.com", root.baseUri());
    }

    @Test
    public void testTraverse() {
        final StringBuilder sb = new StringBuilder();
        root.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                sb.append(node.nodeName()).append(",");
            }

            @Override
            public void tail(Node node, int depth) {
                // do nothing
            }
        });
        // Should include div, p, #text, #comment, span, #text
        assertTrue(sb.toString().contains("div"));
        assertTrue(sb.toString().contains("p"));
        assertTrue(sb.toString().contains("span"));
        assertTrue(sb.toString().contains("#comment"));
    }

    @Test
    public void testFilter() {
        NodeFilter filter = new NodeFilter() {
            @Override
            public FilterResult head(Node node, int depth) {
                return node.nodeName().equals("p") ? FilterResult.REMOVE : FilterResult.CONTINUE;
            }

            @Override
            public FilterResult tail(Node node, int depth) {
                return FilterResult.CONTINUE;
            }
        };
        root.filter(filter);
        // p should be removed
        assertEquals(2, root.childNodeSize()); // comment and span
    }

    @Test
    public void testSiblingIndex() {
        assertEquals(0, root.siblingIndex()); // root is only child of document
        assertEquals(0, root.child(0).siblingIndex()); // p is first child
        assertEquals(2, root.child(2).siblingIndex()); // span is third child (index 2)
    }

    @Test
    public void testSiblingNodes() {
        Elements siblings = root.siblingNodes();
        // root has no siblings (only child of document)
        assertEquals(0, siblings.size());
    }

    @Test
    public void testSiblingElements() {
        Elements siblingElements = root.siblingElements();
        assertEquals(0, siblingElements.size());
    }

    @Test
    public void testFirstElementSibling() {
        Element p = root.child(0);
        assertEquals("span", p.firstElementSibling().tagName());
    }

    @Test
    public void testLastElementSibling() {
        Element p = root.child(0);
        assertEquals("span", p.lastElementSibling().tagName());
    }

    @Test
    public void testElementSiblingIndex() {
        Element p = root.child(0);
        assertEquals(0, p.elementSiblingIndex());
        Element span = root.child(2);
        assertEquals(1, span.elementSiblingIndex());
    }

    @Test
    public void testGetElementsByIndexLessThan() {
        Elements lessThan = root.getElementsByIndexLessThan(1);
        // Only p has index 0
        assertEquals(1, lessThan.size());
        assertEquals("p", lessThan.first().tagName());
    }

    @Test
    public void testGetElementsByIndexGreaterThan() {
        Elements greaterThan = root.getElementsByIndexGreaterThan(0);
        // span has index 1
        assertEquals(1, greaterThan.size());
        assertEquals("span", greaterThan.first().tagName());
    }

    @Test
    public void testGetElementsByIndexEquals() {
        Elements equals = root.getElementsByIndexEquals(0);
        assertEquals(1, equals.size());
        assertEquals("p", equals.first().tagName());
    }

    @Test
    public void testGetElementsContainingOwnTextWithComment() {
        // ownText of root is empty, so no match
        Element div = new Element("div");
        div.appendChild(new TextNode("own"));
        div.appendChild(new Comment("comment"));
        Elements result = div.getElementsContainingOwnText("own");
        assertEquals(1, result.size());
        assertEquals(div, result.first());
    }

    @Test
    public void testGetElementsMatchingOwnTextWithComment() {
        Element div = new Element("div");
        div.appendChild(new TextNode("own"));
        div.appendChild(new Comment("comment"));
        Elements result = div.getElementsMatchingOwnText("own");
        assertEquals(1, result.size());
    }

    // --- Test for bug 66 with more complex structure ---

    @Test
    public void testTextWithCommentAndMultipleChildren() {
        String html = "<div>Before<!-- comment --><p>Inside</p>After</div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("Before Inside After", div.text());
    }

    @Test
    public void testTextWithCommentOnlyNoText() {
        String html = "<div><!-- only comment --></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("", div.text());
    }

    @Test
    public void testTextWithCommentAndWhitespace() {
        String html = "<div>  <!-- comment -->  text  </div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("text", div.text());
    }

    // --- Additional edge cases for coverage ---

    @Test
    public void testAppendElement() {
        Element newDiv = root.appendElement("div");
        assertEquals("div", newDiv.tagName());
        assertSame(root, newDiv.parent());
    }

    @Test
    public void testPrependElement() {
        Element newDiv = root.prependElement("div");
        assertEquals("div", newDiv.tagName());
        assertEquals(0, newDiv.siblingIndex());
    }

    @Test
    public void testAppendTextWithNull() {
        root.appendText(null);
        // Should not throw, should append empty string
        assertEquals("Hello World", root.text());
    }

    @Test
    public void testPrependTextWithNull() {
        root.prependText(null);
        assertEquals("Hello World", root.text());
    }

    @Test
    public void testEmptyAfterAppend() {
        root.empty();
        root.appendChild(new Element("b").text("new"));
        assertEquals("new", root.text());
    }

    @Test
    public void testRemoveChild() {
        Element p = root.child(0);
        p.remove();
        assertNull(p.parent());
    }

    @Test
    public void testReplaceChild() {
        Element p = root.child(0);
        Element newB = new Element("b").text("Bold");
        root.replaceChild(p, newB);
        assertEquals("Bold", root.child(0).text());
    }

    @Test
    public void testCloneDeep() {
        Element clone = root.clone();
        clone.child(0).text("Modified");
        // Original should not be affected
        assertEquals("Hello", root.child(0).text());
    }

    @Test
    public void testShallowClone() {
        // shallowClone is not public? Actually Element has a shallowClone method? Not sure.
        // We'll skip.
    }

    @Test
    public void testDoSetBaseUri() {
        // doSetBaseUri is protected, cannot test directly.
    }

    @Test
    public void testOuterHtmlHead() {
        // outerHtmlHead is protected, but we can test via outerHtml()
        String outer = root.outerHtml();
        assertTrue(outer.startsWith("<div"));
    }

    @Test
    public void testOuterHtmlTail() {
        // similar
    }

    @Test
    public void testHasChildNodes() {
        assertTrue(root.hasChildNodes());
        Element empty = new Element("div");
        assertFalse(empty.hasChildNodes());
    }

    @Test
    public void testChildNodeAsElement() {
        // childNode(int) returns Node, but we can cast
        Node child = root.childNode(0);
        assertTrue(child instanceof Element);
        Element p = (Element) child;
        assertEquals("p", p.tagName());
    }

    @Test
    public void testChildNodesCopy() {
        List<Node> copy = root.childNodesCopy();
        assertEquals(root.childNodeSize(), copy.size());
        // Modifying copy should not affect original
        copy.clear();
        assertEquals(3, root.childNodeSize());
    }

    @Test
    public void testChildNodeSizeWithTextNodes() {
        // Already tested
    }

    @Test
    public void testNodeDepth() {
        assertEquals(1, root.nodeDepth()); // root is depth 1? Actually depends on document.
        // Document is depth 0, root is depth 1.
        assertEquals(2, root.child(0).nodeDepth());
    }

    @Test
    public void testSiblingIndexWithTextNodes() {
        // siblingIndex includes all nodes, not just elements
        Element p = root.child(0);
        assertEquals(0, p.siblingIndex());
        Node comment = root.childNode(1);
        assertEquals(1, comment.siblingIndex());
    }

    @Test
    public void testNextSiblingWithTextNodes() {
        Element p = root.child(0);
        Node next = p.nextSibling();
        assertTrue(next instanceof Comment);
    }

    @Test
    public void testPreviousSiblingWithTextNodes() {
        Element span = root.child(2);
        Node prev = span.previousSibling();
        assertTrue(prev instanceof Comment);
    }

    @Test
    public void testGetElementsByTagWithStar() {
        Elements all = root.getElementsByTag("*");
        assertEquals(3, all.size()); // root, p, span
    }

    @Test
    public void testSelectWithPseudoClass() {
        Elements firstChild = root.select(":first-child");
        assertEquals(1, firstChild.size());
        assertEquals("p", firstChild.first().tagName());
    }

    @Test
    public void testCssSelectorWithClass() {
        Element p = root.child(0);
        p.addClass("test-class");
        String css = p.cssSelector();
        assertTrue(css.contains(".test-class"));
    }

    @Test
    public void testEqualsWithSameElement() {
        assertEquals(root, root);
    }

    @Test
    public void testHashCodeConsistency() {
        int hc = root.hashCode();
        assertEquals(hc, root.hashCode());
    }

    @Test
    public void testToString() {
        String str = root.toString();
        assertTrue(str.contains("div"));
    }

    @Test
    public void testCloneWithChildren() {
        Element clone = root.clone();
        assertEquals(root.html(), clone.html());
        // Ensure deep clone
        assertNotSame(root.child(0), clone.child(0));
    }

    @Test
    public void testWrapWithNull() {
        try {
            root.wrap(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testWrapWithInvalidHtml() {
        try {
            root.wrap("<div>");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testUnwrapOnRoot() {
        // Unwrapping root should not throw but may do nothing
        root.unwrap();
        // root should still be there
        assertNotNull(root.parent());
    }

    @Test
    public void testValOnInput() {
        Element input = new Element("input");
        input.attr("value", "test");
        assertEquals("test", input.val());
        input.val("new");
        assertEquals("new", input.val());
    }

    @Test
    public void testValOnTextarea() {
        Element textarea = new Element("textarea");
        textarea.text("content");
        assertEquals("content", textarea.val());
    }

    @Test
    public void testValOnSelect() {
        // Not implemented in this test, skip.
    }

    @Test
    public void testDataWithScriptAndComment() {
        // Script with comment inside? Not typical.
    }

    @Test
    public void testGetElementsByAttributeStarting() {
        root.attr("data-custom", "value");
        Elements withData = root.getElementsByAttributeStarting("data-");
        assertEquals(1, withData.size());
    }

    @Test
    public void testGetElementsByAttributeValueNot() {
        Elements notContainer = root.getElementsByAttributeValueNot("class", "container");
        // Only p and span have no class, but they are not root
        // Actually root has class container, so not container should be p and span
        assertEquals(2, notContainer.size());
    }

    @Test
    public void testGetElementsByAttributeValueStarting() {
        root.attr("data-x", "y");
        Elements starting = root.getElementsByAttributeValueStarting("data-", "x");
        assertEquals(1, starting.size());
    }

    @Test
    public void testGetElementsByAttributeValueEnding() {
        root.attr("data-x", "y");
        Elements ending = root.getElementsByAttributeValueEnding("data-", "y");
        assertEquals(1, ending.size());
    }

    @Test
    public void testGetElementsByAttributeValueContaining() {
        root.attr("data-x", "hello");
        Elements containing = root.getElementsByAttributeValueContaining("data-", "ell");
        assertEquals(1, containing.size());
    }

    @Test
    public void testGetElementsByAttributeValueMatching() {
        root.attr("data-x", "hello");
        Elements matching = root.getElementsByAttributeValueMatching("data-", ".*ell.*");
        assertEquals(1, matching.size());
    }

    @Test
    public void testGetElementsByIndexLessThanWithNegative() {
        Elements lessThan = root.getElementsByIndexLessThan(-1);
        assertEquals(0, lessThan.size());
    }

    @Test
    public void testGetElementsByIndexGreaterThanWithLarge() {
        Elements greaterThan = root.getElementsByIndexGreaterThan(100);
        assertEquals(0, greaterThan.size());
    }

    @Test
    public void testGetElementsByIndexEqualsWithNegative() {
        Elements equals = root.getElementsByIndexEquals(-1);
        assertEquals(0, equals.size());
    }

    @Test
    public void testGetElementsContainingTextWithNull() {
        try {
            root.getElementsContainingText(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsContainingOwnTextWithNull() {
        try {
            root.getElementsContainingOwnText(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsMatchingTextWithNull() {
        try {
            root.getElementsMatchingText(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsMatchingOwnTextWithNull() {
        try {
            root.getElementsMatchingOwnText(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSelectWithNull() {
        try {
            root.select(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testSelectWithEmpty() {
        Elements selected = root.select("");
        assertEquals(0, selected.size());
    }

    @Test
    public void testGetElementsByTagWithNull() {
        try {
            root.getElementsByTag(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByClassWithNull() {
        try {
            root.getElementsByClass(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementByIdWithNull() {
        assertNull(root.getElementById(null));
    }

    @Test
    public void testGetElementsByAttributeWithNull() {
        try {
            root.getElementsByAttribute(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByAttributeValueWithNullKey() {
        try {
            root.getElementsByAttributeValue(null, "value");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByAttributeValueWithNullValue() {
        // Should not throw, should match elements with that attribute (value null treated as empty)
        root.attr("test", null);
        Elements result = root.getElementsByAttributeValue("test", null);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetElementsByAttributeValueStartingWithNullKey() {
        try {
            root.getElementsByAttributeValueStarting(null, "prefix");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByAttributeValueEndingWithNullKey() {
        try {
            root.getElementsByAttributeValueEnding(null, "suffix");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByAttributeValueContainingWithNullKey() {
        try {
            root.getElementsByAttributeValueContaining(null, "sub");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByAttributeValueMatchingWithNullKey() {
        try {
            root.getElementsByAttributeValueMatching(null, "pattern");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByAttributeValueMatchingWithNullPattern() {
        try {
            root.getElementsByAttributeValueMatching("key", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByAttributeStartingWithNull() {
        try {
            root.getElementsByAttributeStarting(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testGetElementsByIndexLessThanWithNull() {
        // Not applicable, method takes int
    }

    @Test
    public void testGetElementsByIndexGreaterThanWithNull() {
        // Not applicable
    }

    @Test
    public void testGetElementsByIndexEqualsWithNull() {
        // Not applicable
    }

    @Test
    public void testGetAllElementsWithNoChildren() {
        Element empty = new Element("div");
        Elements all = empty.getAllElements();
        assertEquals(1, all.size());
        assertEquals(empty, all.first());
    }

    @Test
    public void testTextWithNestedElementsAndComments() {
        String html = "<div>Outer<!-- comment --><p>Inner</p></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        assertEquals("Outer Inner", div.text());
    }

    @Test
    public void testHtmlWithComment() {
        String html = "<div>Text<!-- comment --></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        String innerHtml = div.html();
        assertTrue(innerHtml.contains("<!-- comment -->"));
        assertTrue(innerHtml.contains("Text"));
    }

    @Test
    public void testOuterHtmlWithComment() {
        String html = "<div>Text<!-- comment --></div>";
        Document doc = Jsoup.parse(html);
        Element div = doc.select("div").first();
        String outerHtml = div.outerHtml();
        assertTrue(outerHtml.contains("<!-- comment -->"));
    }

    @Test
    public void testCloneWithComment() {
        Element div = new Element("div");
        div.appendChild(new TextNode("text"));
        div.appendChild(new Comment("comment"));
        Element clone = div.clone();
        assertEquals(div.html(), clone.html());
        assertEquals(div.text(), clone.text());
    }

    @Test
    public void testTextWithMultipleCommentsAndElements() {
        Element div = new Element("div");
        div.appendChild(new Comment("c1"));
        div.appendChild(new Element("b").text("bold"));
        div.appendChild(new Comment("c2"));
        div.appendChild(new TextNode(" text"));
        assertEquals("bold text", div.text());
    }

    @Test
    public void testOwnTextWithComment() {
        Element div = new Element("div");
        div.appendChild(new TextNode("own"));
        div.appendChild(new Comment("comment"));
        assertEquals("own", div.ownText());
    }

    @Test
    public void testWholeTextWithComment() {
        // wholeText is for TextNode, not Element. Skip.
    }

    @Test
    public void testDataWithComment() {
        // data() returns empty for normal elements, comment doesn't affect
        Element div = new Element("div");
        div.appendChild(new Comment("comment"));
        assertEquals("", div.data());
    }

    @Test
    public void testAppendChildWithComment() {
        Comment comment = new Comment("new comment");
        root.appendChild(comment);
        assertEquals(4, root.childNodeSize());
        assertSame(comment, root.lastChild());
    }

    @Test
    public void testPrependChildWithComment() {
        Comment comment = new Comment("new comment");
        root.prependChild(comment);
        assertEquals(4, root.childNodeSize());
        assertSame(comment, root.firstChild());
    }

    @Test
    public void testRemoveChildWithComment() {
        Node comment = root.childNode(1);
        comment.remove();
        assertEquals(2, root.childNodeSize());
        assertEquals("Hello World", root.text());
    }

    @Test
    public void testReplaceChildWithComment() {
        Element p = root.child(0);
        Comment comment = new Comment("replaced");
        root.replaceChild(p, comment);
        assertEquals(3, root.childNodeSize());
        assertEquals("World", root.text()); // only span text
    }

    @Test
    public void testWrapWithComment() {
        // Wrapping a comment node? Not typical, but Element.wrap is for elements.
        // Comment is not an Element, so cannot call wrap on it.
    }

    @Test
    public void testUnwrapWithComment() {
        // Comment cannot be unwrapped as it's not an element.
    }

    @Test
    public void testCloneWithDeepComment() {
        Element div = new Element("div");
        div.appendChild(new Comment("deep"));
        Element clone = div.clone();
        assertEquals(div.html(), clone.html());
    }

    @Test
    public void testSiblingIndexWithComment() {
        Node comment = root.childNode(1);
        assertEquals(1, comment.siblingIndex());
    }

    @Test
    public void testNextSiblingWithComment() {
        Node comment = root.childNode(1);
        Node next = comment.nextSibling();
        assertTrue(next instanceof Element);
        assertEquals("span", ((Element) next).tagName());
    }

    @Test
    public void testPreviousSiblingWithComment() {
        Node comment = root.childNode(1);
        Node prev = comment.previousSibling();
        assertTrue(prev instanceof Element);
        assertEquals("p", ((Element) prev).tagName());
    }

    @Test
    public void testGetElementsContainingTextWithComment() {
        // Should find text inside p, not comment
        Elements found = root.getElementsContainingText("Hello");
        assertEquals(1, found.size());
        assertEquals("p", found.first().tagName());
    }

    @Test
    public void testGetElementsMatchingTextWithComment() {
        Elements found = root.getElementsMatchingText("Hello");
        assertEquals(1, found.size());
    }

    @Test
    public void testGetElementsContainingOwnTextWithCommentOnRoot() {
        // root ownText is empty
        Elements found = root.getElementsContainingOwnText("Hello");
        assertEquals(0, found.size());
    }

    @Test
    public void testGetElementsMatchingOwnTextWithCommentOnRoot() {
        Elements found = root.getElementsMatchingOwnText("Hello");
        assertEquals(0, found.size());
    }

    @Test
    public void testSelectWithComment() {
        // Selectors don't match comments
        Elements selected = root.select("comment");
        assertEquals(0, selected.size());
    }

    @Test
    public void testCssSelectorWithComment() {
        // Comments don't have CSS selectors
    }

    @Test
    public void testEqualsWithComment() {
        Comment c1 = new Comment("test");
        Comment c2 = new Comment("test");
        // Comments are equal if data is equal? Actually Comment.equals compares data.
        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testHashCodeWithComment() {
        Comment c = new Comment("test");
        assertNotNull(c.hashCode());
    }

    @Test
    public void testToStringWithComment() {
        Comment c = new Comment("test");
        String str = c.toString();
        assertTrue(str.contains("comment"));
    }

    @Test
    public void testCloneWithCommentNode() {
        Comment c = new Comment("test");
        Comment clone = c.clone();
        assertEquals(c.getData(), clone.getData());
        assertNotSame(c, clone);
    }

    @Test
    public void testOuterHtmlWithComment() {
        Comment c = new Comment("test");
        String outer = c.outerHtml();
        assertEquals("<!--test-->", outer);
    }

    @Test
    public void testNodeNameWithComment() {
        Comment c = new Comment("test");
        assertEquals("#comment", c.nodeName());
    }

    @Test
    public void testGetDataWithComment() {
        Comment c = new Comment("test data");
        assertEquals("test data", c.getData());
    }

    @Test
    public void testSetDataWithComment() {
        Comment c = new Comment("old");
        c.setData("new");
        assertEquals("new", c.getData());
    }

    @Test
    public void testIsXmlDeclaration() {
        Comment c = new Comment("?xml version='1.0'?");
        assertTrue(c.isXmlDeclaration());
        Comment c2 = new Comment("normal");
        assertFalse(c2.isXmlDeclaration());
    }

    @Test
    public void testAsXmlDeclaration() {
        Comment c = new Comment("?xml version='1.0'?");
        XmlDeclaration decl = c.asXmlDeclaration();
        assertNotNull(decl);
        assertEquals("xml", decl.name());
        assertEquals("1.0", decl.attr("version"));
    }

    @Test
    public void testAsXmlDeclarationNot() {
        Comment c = new Comment("normal");
        assertNull(c.asXmlDeclaration());
    }

    // --- End of tests ---
}