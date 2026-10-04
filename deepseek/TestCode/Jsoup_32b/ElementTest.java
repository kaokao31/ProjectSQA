package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for the Element class.
 * Designed to achieve high code coverage and detect faults,
 * particularly targeting bug #32 in the Defects4J Jsoup dataset.
 */
public class ElementTest {

    private Document doc;
    private Element body;

    @Before
    public void setUp() {
        doc = Jsoup.parse("<html><head></head><body></body></html>");
        body = doc.body();
    }

    // ---------- html() method tests ----------
    @Test
    public void testHtmlWithTextContent() {
        Element p = body.appendElement("p").text("Hello");
        assertEquals("Hello", p.html());
    }

    @Test
    public void testHtmlWithChildElements() {
        Element div = body.appendElement("div");
        div.appendElement("span").text("a");
        div.appendElement("span").text("b");
        assertEquals("<span>a</span><span>b</span>", div.html());
    }

    @Test
    public void testHtmlWithMixedContent() {
        Element p = body.appendElement("p");
        p.text("Hello ");
        p.appendElement("b").text("world");
        assertEquals("Hello <b>world</b>", p.html());
    }

    @Test
    public void testHtmlEmptyElement() {
        Element br = body.appendElement("br");
        assertEquals("", br.html());
    }

    @Test
    public void testHtmlWithOnlyWhitespace() {
        Element p = body.appendElement("p");
        p.text("   ");
        assertEquals("   ", p.html());
    }

    @Test
    public void testHtmlWithNoChildren() {
        Element empty = body.appendElement("div");
        assertEquals("", empty.html());
    }

    @Test
    public void testHtmlAfterAppendChild() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("test");
        assertEquals("<p>test</p>", div.html());
    }

    @Test
    public void testHtmlAfterPrependChild() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("original");
        div.prependChild(new Element("span").text("prepended"));
        assertEquals("<span>prepended</span><p>original</p>", div.html());
    }

    // ---------- outerHtml() method tests ----------
    @Test
    public void testOuterHtmlWithTextContent() {
        Element p = body.appendElement("p").text("Hello");
        assertEquals("<p>Hello</p>", p.outerHtml());
    }

    @Test
    public void testOuterHtmlWithChildElements() {
        Element div = body.appendElement("div");
        div.appendElement("span").text("a");
        assertEquals("<div><span>a</span></div>", div.outerHtml());
    }

    @Test
    public void testOuterHtmlSelfClosingTag() {
        Element br = body.appendElement("br");
        assertEquals("<br>", br.outerHtml()); // Jsoup outputs <br> not <br/>
    }

    // ---------- text() method tests ----------
    @Test
    public void testTextWithTextContent() {
        Element p = body.appendElement("p").text("Hello");
        assertEquals("Hello", p.text());
    }

    @Test
    public void testTextWithChildElements() {
        Element div = body.appendElement("div");
        div.appendElement("span").text("a");
        div.appendElement("span").text("b");
        assertEquals("a b", div.text());
    }

    @Test
    public void testTextWithMixedContent() {
        Element p = body.appendElement("p");
        p.text("Hello ");
        p.appendElement("b").text("world");
        assertEquals("Hello world", p.text());
    }

    @Test
    public void testTextEmptyElement() {
        Element br = body.appendElement("br");
        assertEquals("", br.text());
    }

    // ---------- attribute tests ----------
    @Test
    public void testAttrSetAndGet() {
        Element div = body.appendElement("div").attr("id", "test");
        assertEquals("test", div.attr("id"));
    }

    @Test
    public void testAttrDefaultValue() {
        Element div = body.appendElement("div");
        assertEquals("", div.attr("class"));
    }

    @Test
    public void testHasAttrTrue() {
        Element div = body.appendElement("div").attr("data-x", "val");
        assertTrue(div.hasAttr("data-x"));
    }

    @Test
    public void testHasAttrFalse() {
        Element div = body.appendElement("div");
        assertFalse(div.hasAttr("nonexistent"));
    }

    @Test
    public void testRemoveAttr() {
        Element div = body.appendElement("div").attr("id", "test");
        div.removeAttr("id");
        assertFalse(div.hasAttr("id"));
    }

    // ---------- class manipulation tests ----------
    @Test
    public void testClassName() {
        Element div = body.appendElement("div").addClass("foo");
        assertEquals("foo", div.className());
    }

    @Test
    public void testClassNames() {
        Element div = body.appendElement("div").addClass("foo bar");
        Set<String> classes = div.classNames();
        assertTrue(classes.contains("foo"));
        assertTrue(classes.contains("bar"));
        assertEquals(2, classes.size());
    }

    @Test
    public void testHasClassTrue() {
        Element div = body.appendElement("div").addClass("active");
        assertTrue(div.hasClass("active"));
    }

    @Test
    public void testHasClassFalse() {
        Element div = body.appendElement("div");
        assertFalse(div.hasClass("nonexistent"));
    }

    @Test
    public void testAddClass() {
        Element div = body.appendElement("div");
        div.addClass("new-class");
        assertTrue(div.hasClass("new-class"));
    }

    @Test
    public void testRemoveClass() {
        Element div = body.appendElement("div").addClass("temp");
        div.removeClass("temp");
        assertFalse(div.hasClass("temp"));
    }

    @Test
    public void testToggleClassAdd() {
        Element div = body.appendElement("div");
        div.toggleClass("toggle-me");
        assertTrue(div.hasClass("toggle-me"));
    }

    @Test
    public void testToggleClassRemove() {
        Element div = body.appendElement("div").addClass("toggle-me");
        div.toggleClass("toggle-me");
        assertFalse(div.hasClass("toggle-me"));
    }

    // ---------- children and traversal tests ----------
    @Test
    public void testChildren() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("1");
        div.appendElement("p").text("2");
        Elements children = div.children();
        assertEquals(2, children.size());
        assertEquals("1", children.get(0).text());
        assertEquals("2", children.get(1).text());
    }

    @Test
    public void testChildNodeSize() {
        Element div = body.appendElement("div");
        assertEquals(0, div.childNodeSize());
        div.appendChild(new TextNode("text"));
        assertEquals(1, div.childNodeSize());
    }

    @Test
    public void testParent() {
        Element p = body.appendElement("p");
        assertSame(body, p.parent());
    }

    @Test
    public void testParents() {
        Element p = body.appendElement("p");
        Elements parents = p.parents();
        assertTrue(parents.contains(body));
        assertTrue(parents.contains(doc));
    }

    @Test
    public void testFirstElementSibling() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("first");
        div.appendElement("p").text("second");
        Element first = div.children().first();
        Element second = div.children().last();
        assertSame(first, second.previousElementSibling());
        assertNull(first.previousElementSibling());
    }

    @Test
    public void testLastElementSibling() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("first");
        div.appendElement("p").text("second");
        Element first = div.children().first();
        Element second = div.children().last();
        assertSame(second, first.nextElementSibling());
        assertNull(second.nextElementSibling());
    }

    @Test
    public void testElementSiblingIndex() {
        Element div = body.appendElement("div");
        Element p1 = div.appendElement("p");
        Element p2 = div.appendElement("p");
        assertEquals(0, p1.elementSiblingIndex());
        assertEquals(1, p2.elementSiblingIndex());
    }

    @Test
    public void testElementSiblings() {
        Element div = body.appendElement("div");
        Element p1 = div.appendElement("p");
        Element p2 = div.appendElement("p");
        Elements siblings = p1.elementSiblings();
        assertTrue(siblings.contains(p2));
        assertFalse(siblings.contains(p1));
    }

    // ---------- selection tests ----------
    @Test
    public void testSelectByTag() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("a");
        div.appendElement("p").text("b");
        Elements selected = div.select("p");
        assertEquals(2, selected.size());
    }

    @Test
    public void testSelectByClass() {
        Element div = body.appendElement("div");
        div.appendElement("p").addClass("highlight").text("a");
        div.appendElement("p").text("b");
        Elements selected = div.select(".highlight");
        assertEquals(1, selected.size());
    }

    @Test
    public void testGetElementById() {
        Element div = body.appendElement("div").attr("id", "unique");
        assertSame(div, body.getElementById("unique"));
    }

    @Test
    public void testGetElementsByTag() {
        Element div = body.appendElement("div");
        div.appendElement("span").text("a");
        div.appendElement("span").text("b");
        Elements spans = div.getElementsByTag("span");
        assertEquals(2, spans.size());
    }

    @Test
    public void testGetElementsByClass() {
        Element div = body.appendElement("div");
        div.appendElement("p").addClass("foo").text("a");
        div.appendElement("p").addClass("bar").text("b");
        Elements foos = div.getElementsByClass("foo");
        assertEquals(1, foos.size());
    }

    @Test
    public void testGetElementsByAttribute() {
        Element div = body.appendElement("div");
        div.appendElement("p").attr("data-x", "v1");
        div.appendElement("p").attr("data-y", "v2");
        Elements withDataX = div.getElementsByAttribute("data-x");
        assertEquals(1, withDataX.size());
    }

    @Test
    public void testGetElementsByAttributeValue() {
        Element div = body.appendElement("div");
        div.appendElement("p").attr("data-x", "v1");
        div.appendElement("p").attr("data-x", "v2");
        Elements withValue = div.getElementsByAttributeValue("data-x", "v1");
        assertEquals(1, withValue.size());
    }

    // ---------- val() and data() tests ----------
    @Test
    public void testValOnInput() {
        Element input = body.appendElement("input").attr("value", "test");
        assertEquals("test", input.val());
    }

    @Test
    public void testValOnTextarea() {
        Element textarea = body.appendElement("textarea").text("content");
        assertEquals("content", textarea.val());
    }

    @Test
    public void testData() {
        Element script = body.appendElement("script").text("alert(1);");
        assertEquals("alert(1);", script.data());
    }

    // ---------- dataset() tests ----------
    @Test
    public void testDataset() {
        Element div = body.appendElement("div").attr("data-name", "value");
        assertEquals("value", div.dataset().get("name"));
    }

    // ---------- equals and hashCode tests ----------
    @Test
    public void testEqualsSameObject() {
        Element p = body.appendElement("p");
        assertEquals(p, p);
    }

    @Test
    public void testEqualsDifferentElements() {
        Element p1 = body.appendElement("p").text("a");
        Element p2 = body.appendElement("p").text("a");
        // Elements are not equal by default (identity-based)
        assertNotEquals(p1, p2);
    }

    @Test
    public void testHashCodeConsistency() {
        Element p = body.appendElement("p");
        int hash = p.hashCode();
        assertEquals(hash, p.hashCode());
    }

    // ---------- clone() tests ----------
    @Test
    public void testClone() {
        Element original = body.appendElement("div").attr("id", "orig");
        original.appendElement("span").text("child");
        Element cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.outerHtml(), cloned.outerHtml());
        assertEquals(original.html(), cloned.html());
    }

    @Test
    public void testCloneDeepCopy() {
        Element original = body.appendElement("div");
        original.appendElement("p").text("text");
        Element cloned = original.clone();
        cloned.child(0).text("modified");
        assertNotEquals(original.html(), cloned.html());
    }

    // ---------- cssSelector() tests ----------
    @Test
    public void testCssSelector() {
        Element div = body.appendElement("div").attr("id", "myid");
        assertEquals("#myid", div.cssSelector());
    }

    @Test
    public void testCssSelectorWithClass() {
        Element div = body.appendElement("div").addClass("myclass");
        assertEquals("div.myclass", div.cssSelector());
    }

    // ---------- edge cases and bug-specific tests ----------
    @Test
    public void testHtmlWithOnlyTextNodeBug32() {
        // This test targets the suspected bug: html() returning empty for text-only content
        Element p = body.appendElement("p").text("Hello World");
        assertEquals("Hello World", p.html());
    }

    @Test
    public void testHtmlWithMultipleTextNodes() {
        Element p = body.appendElement("p");
        p.appendChild(new TextNode("First "));
        p.appendChild(new TextNode("Second"));
        assertEquals("First Second", p.html());
    }

    @Test
    public void testHtmlWithEmptyTextNode() {
        Element p = body.appendElement("p");
        p.appendChild(new TextNode(""));
        assertEquals("", p.html());
    }

    @Test
    public void testHtmlWithCommentNode() {
        Element div = body.appendElement("div");
        div.appendChild(new Comment("comment"));
        assertEquals("<!--comment-->", div.html());
    }

    @Test
    public void testHtmlWithDataNode() {
        Element script = body.appendElement("script");
        script.appendChild(new DataNode("var x=1;"));
        assertEquals("var x=1;", script.html());
    }

    @Test
    public void testHtmlWithOnlyChildElement() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("child");
        assertEquals("<p>child</p>", div.html());
    }

    @Test
    public void testHtmlWithNoChildNodes() {
        Element div = body.appendElement("div");
        assertEquals("", div.html());
    }

    @Test
    public void testHtmlAfterRemovingChild() {
        Element div = body.appendElement("div");
        Element p = div.appendElement("p").text("remove me");
        p.remove();
        assertEquals("", div.html());
    }

    @Test
    public void testHtmlAfterReplacingChild() {
        Element div = body.appendElement("div");
        Element old = div.appendElement("p").text("old");
        Element newChild = new Element("span").text("new");
        div.replaceChild(old, newChild);
        assertEquals("<span>new</span>", div.html());
    }

    // ---------- append/prepend element tests ----------
    @Test
    public void testAppendElement() {
        Element div = body.appendElement("div");
        Element p = div.appendElement("p").text("appended");
        assertSame(p, div.children().last());
    }

    @Test
    public void testPrependElement() {
        Element div = body.appendElement("div");
        Element p = div.prependElement("p").text("prepended");
        assertSame(p, div.children().first());
    }

    // ---------- append/prepend text tests ----------
    @Test
    public void testAppendText() {
        Element div = body.appendElement("div");
        div.appendText("some text");
        assertEquals("some text", div.text());
    }

    @Test
    public void testPrependText() {
        Element div = body.appendElement("div");
        div.appendText("original");
        div.prependText("prepended ");
        assertEquals("prepended original", div.text());
    }

    // ---------- wrap() tests ----------
    @Test
    public void testWrap() {
        Element p = body.appendElement("p").text("content");
        p.wrap("<div></div>");
        assertEquals("<div><p>content</p></div>", body.html());
    }

    // ---------- empty() tests ----------
    @Test
    public void testEmpty() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("child");
        div.empty();
        assertEquals(0, div.children().size());
        assertEquals("", div.html());
    }

    // ---------- indexOf() tests ----------
    @Test
    public void testIndexOf() {
        Element div = body.appendElement("div");
        Element p1 = div.appendElement("p");
        Element p2 = div.appendElement("p");
        assertEquals(0, div.indexOf(p1));
        assertEquals(1, div.indexOf(p2));
        assertEquals(-1, div.indexOf(new Element("span")));
    }

    // ---------- sibling methods ----------
    @Test
    public void testSiblingElements() {
        Element div = body.appendElement("div");
        Element p1 = div.appendElement("p");
        Element p2 = div.appendElement("p");
        Elements siblings = p1.siblingElements();
        assertTrue(siblings.contains(p2));
        assertFalse(siblings.contains(p1));
    }

    // ---------- textNodes() tests ----------
    @Test
    public void testTextNodes() {
        Element p = body.appendElement("p");
        p.appendChild(new TextNode("text1"));
        p.appendChild(new TextNode("text2"));
        List<TextNode> textNodes = p.textNodes();
        assertEquals(2, textNodes.size());
        assertEquals("text1", textNodes.get(0).text());
    }

    // ---------- dataNodes() tests ----------
    @Test
    public void testDataNodes() {
        Element script = body.appendElement("script");
        script.appendChild(new DataNode("data1"));
        script.appendChild(new DataNode("data2"));
        List<DataNode> dataNodes = script.dataNodes();
        assertEquals(2, dataNodes.size());
    }

    // ---------- wholeText() tests ----------
    @Test
    public void testWholeText() {
        Element p = body.appendElement("p");
        p.appendChild(new TextNode("Hello"));
        p.appendChild(new TextNode(" World"));
        assertEquals("Hello World", p.wholeText());
    }

    // ---------- ownText() tests ----------
    @Test
    public void testOwnText() {
        Element p = body.appendElement("p");
        p.text("Parent text");
        p.appendElement("span").text("Child text");
        assertEquals("Parent text", p.ownText());
    }

    // ---------- isBlock() tests ----------
    @Test
    public void testIsBlock() {
        Element div = body.appendElement("div");
        assertTrue(div.isBlock());
        Element span = body.appendElement("span");
        assertFalse(span.isBlock());
    }

    // ---------- tagName() tests ----------
    @Test
    public void testTagName() {
        Element p = body.appendElement("p");
        assertEquals("p", p.tagName());
    }

    @Test
    public void testTagNameChange() {
        Element p = body.appendElement("p");
        p.tagName("div");
        assertEquals("div", p.tagName());
    }

    // ---------- id() tests ----------
    @Test
    public void testId() {
        Element div = body.appendElement("div").attr("id", "myid");
        assertEquals("myid", div.id());
    }

    @Test
    public void testIdEmpty() {
        Element div = body.appendElement("div");
        assertEquals("", div.id());
    }

    // ---------- nodeName() tests ----------
    @Test
    public void testNodeName() {
        Element p = body.appendElement("p");
        assertEquals("p", p.nodeName());
    }

    // ---------- baseUri() tests ----------
    @Test
    public void testBaseUri() {
        Document docWithBase = Jsoup.parse("<html></html>", "http://example.com");
        Element body = docWithBase.body();
        assertEquals("http://example.com", body.baseUri());
    }

    // ---------- childNode(int) tests ----------
    @Test
    public void testChildNode() {
        Element div = body.appendElement("div");
        TextNode tn = new TextNode("text");
        div.appendChild(tn);
        assertSame(tn, div.childNode(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testChildNodeOutOfBounds() {
        Element div = body.appendElement("div");
        div.childNode(0);
    }

    // ---------- childNodes() tests ----------
    @Test
    public void testChildNodes() {
        Element div = body.appendElement("div");
        div.appendChild(new TextNode("a"));
        div.appendChild(new TextNode("b"));
        assertEquals(2, div.childNodes().size());
    }

    // ---------- childNodesCopy() tests ----------
    @Test
    public void testChildNodesCopy() {
        Element div = body.appendElement("div");
        div.appendChild(new TextNode("original"));
        List<Node> copy = div.childNodesCopy();
        assertEquals(1, copy.size());
        // Modifying copy should not affect original
        copy.clear();
        assertEquals(1, div.childNodes().size());
    }

    // ---------- shallowClone() tests ----------
    @Test
    public void testShallowClone() {
        Element original = body.appendElement("div").attr("id", "orig");
        original.appendElement("p").text("child");
        Element shallow = original.shallowClone();
        assertNotSame(original, shallow);
        assertEquals(original.attributes(), shallow.attributes());
        // Shallow clone should have no children
        assertEquals(0, shallow.children().size());
    }

    // ---------- equals() with null ----------
    @Test
    public void testEqualsNull() {
        Element p = body.appendElement("p");
        assertFalse(p.equals(null));
    }

    // ---------- toString() tests ----------
    @Test
    public void testToString() {
        Element p = body.appendElement("p").text("test");
        assertEquals("<p>test</p>", p.toString());
    }

    // ---------- outerHtmlHead/outerHtmlTail indirectly tested via outerHtml() ----------
    // Already covered.

    // ---------- html(StringBuilder) tests ----------
    @Test
    public void testHtmlStringBuilder() {
        Element p = body.appendElement("p").text("Hello");
        StringBuilder sb = new StringBuilder();
        p.html(sb);
        assertEquals("Hello", sb.toString());
    }

    // ---------- data() with key-value ----------
    @Test
    public void testDataKeyValue() {
        Element div = body.appendElement("div").attr("data-key", "value");
        assertEquals("value", div.data("key"));
    }

    // ---------- dataset() modification ----------
    @Test
    public void testDatasetModification() {
        Element div = body.appendElement("div");
        div.dataset().put("name", "value");
        assertEquals("value", div.attr("data-name"));
    }

    // ---------- hasText() tests ----------
    @Test
    public void testHasTextTrue() {
        Element p = body.appendElement("p").text("text");
        assertTrue(p.hasText());
    }

    @Test
    public void testHasTextFalse() {
        Element br = body.appendElement("br");
        assertFalse(br.hasText());
    }

    // ---------- isDataNode() tests ----------
    @Test
    public void testIsDataNode() {
        Element script = body.appendElement("script");
        assertTrue(script.isDataNode());
        Element p = body.appendElement("p");
        assertFalse(p.isDataNode());
    }

    // ---------- appendChild with null ----------
    @Test(expected = IllegalArgumentException.class)
    public void testAppendChildNull() {
        Element div = body.appendElement("div");
        div.appendChild(null);
    }

    // ---------- prependChild with null ----------
    @Test(expected = IllegalArgumentException.class)
    public void testPrependChildNull() {
        Element div = body.appendElement("div");
        div.prependChild(null);
    }

    // ---------- insertChildren with null ----------
    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenNull() {
        Element div = body.appendElement("div");
        div.insertChildren(0, (Node[]) null);
    }

    // ---------- removeChild tests ----------
    @Test
    public void testRemoveChild() {
        Element div = body.appendElement("div");
        Element p = div.appendElement("p");
        div.removeChild(p);
        assertEquals(0, div.children().size());
    }

    // ---------- replaceChildren tests ----------
    @Test
    public void testReplaceChildren() {
        Element div = body.appendElement("div");
        div.appendElement("p").text("old");
        Element newChild = new Element("span").text("new");
        div.replaceChildren(newChild);
        assertEquals(1, div.children().size());
        assertEquals("new", div.text());
    }

    // ---------- ensureChildNodes() tests ----------
    // This is internal but we can trigger by adding children.

    // ---------- nodelistChanged() tests ----------
    // Covered by modifications.

    // ---------- doSetBaseUri() tests ----------
    @Test
    public void testSetBaseUri() {
        Element p = body.appendElement("p");
        p.setBaseUri("http://newbase.com");
        assertEquals("http://newbase.com", p.baseUri());
    }

    // ---------- absUrl() tests ----------
    @Test
    public void testAbsUrl() {
        Element a = body.appendElement("a").attr("href", "/page");
        a.setBaseUri("http://example.com");
        assertEquals("http://example.com/page", a.absUrl("href"));
    }

    @Test
    public void testAbsUrlNoBase() {
        Element a = body.appendElement("a").attr("href", "/page");
        assertEquals("", a.absUrl("href"));
    }

    // ---------- cssSelector() with no id/class ----------
    @Test
    public void testCssSelectorNoIdOrClass() {
        Element div = body.appendElement("div");
        // Should return something like "html > body > div:nth-child(1)"
        assertNotNull(div.cssSelector());
        assertTrue(div.cssSelector().contains("div"));
    }

    // ---------- equals() with different type ----------
    @Test
    public void testEqualsDifferentType() {
        Element p = body.appendElement("p");
        assertFalse(p.equals("string"));
    }

    // ---------- hashCode() consistency with equals ----------
    // Not required but good to test.

    // ---------- serialization tests ----------
    // Not required for unit tests.

    // ---------- additional edge cases ----------
    @Test
    public void testHtmlWithMultipleChildElementsAndText() {
        Element div = body.appendElement("div");
        div.appendText("before");
        div.appendElement("span").text("middle");
        div.appendText("after");
        assertEquals("before<span>middle</span>after", div.html());
    }

    @Test
    public void testHtmlWithOnlyComment() {
        Element div = body.appendElement("div");
        div.appendChild(new Comment("comment"));
        assertEquals("<!--comment-->", div.html());
    }

    @Test
    public void testHtmlWithOnlyDataNode() {
        Element script = body.appendElement("script");
        script.appendChild(new DataNode("data"));
        assertEquals("data", script.html());
    }

    @Test
    public void testHtmlWithMixedNodes() {
        Element div = body.appendElement("div");
        div.appendChild(new TextNode("text"));
        div.appendChild(new Comment("comment"));
        div.appendChild(new DataNode("data"));
        assertEquals("text<!--comment-->data", div.html());
    }

    // ---------- test for bug #32: html() with single text child ----------
    @Test
    public void testHtmlWithSingleTextChildBug32() {
        // This is a direct test for the suspected bug
        Element p = body.appendElement("p");
        p.appendChild(new TextNode("Hello"));
        assertEquals("Hello", p.html());
    }

    @Test
    public void testHtmlWithSingleTextChildAndNoOtherNodesBug32() {
        Element p = body.appendElement("p");
        p.text("Hello");
        assertEquals("Hello", p.html());
    }

    // ---------- test for bug #32: outerHtml() with single text child ----------
    @Test
    public void testOuterHtmlWithSingleTextChildBug32() {
        Element p = body.appendElement("p");
        p.text("Hello");
        assertEquals("<p>Hello</p>", p.outerHtml());
    }

    // ---------- test for bug #32: html() after cloning ----------
    @Test
    public void testHtmlAfterCloneBug32() {
        Element original = body.appendElement("p").text("Hello");
        Element cloned = original.clone();
        assertEquals("Hello", cloned.html());
    }
}