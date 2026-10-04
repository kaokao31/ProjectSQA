package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Tag;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ElementTest {

    private Element div;
    private Element p;
    private Document doc;

    @Before
    public void setUp() {
        doc = Jsoup.parse("<div id='main' class='container content'><p class='para'>Text</p></div>");
        div = doc.getElementById("main");
        p = doc.select("p").first();
    }

    // Basic constructor and tag tests
    @Test
    public void testConstructorAndTag() {
        Element e = new Element(Tag.valueOf("span"), "");
        assertEquals("span", e.tagName());
        assertEquals("", e.baseUri());
        assertNotNull(e.attributes());
    }

    @Test
    public void testConstructorWithAttributes() {
        Attributes attrs = new Attributes();
        attrs.put("class", "foo");
        Element e = new Element(Tag.valueOf("div"), "", attrs);
        assertEquals("foo", e.className());
    }

    // id, className, classNames tests
    @Test
    public void testId() {
        assertEquals("main", div.id());
        assertNull(p.id()); // no id set
    }

    @Test
    public void testClassName() {
        assertEquals("container content", div.className());
        assertEquals("para", p.className());
    }

    @Test
    public void testClassNames() {
        Set<String> expectedDiv = new LinkedHashSet<>();
        expectedDiv.add("container");
        expectedDiv.add("content");
        assertEquals(expectedDiv, div.classNames());

        Set<String> expectedP = new LinkedHashSet<>();
        expectedP.add("para");
        assertEquals(expectedP, p.classNames());
    }

    @Test
    public void testClassNamesEmpty() {
        Element e = new Element(Tag.valueOf("div"), "");
        assertTrue(e.classNames().isEmpty());
    }

    // hasClass tests - critical for bug 61
    @Test
    public void testHasClassSingle() {
        assertTrue(p.hasClass("para"));
        assertFalse(p.hasClass("container"));
    }

    @Test
    public void testHasClassMultiple() {
        assertTrue(div.hasClass("container"));
        assertTrue(div.hasClass("content"));
        assertFalse(div.hasClass("main")); // main is id, not class
    }

    @Test
    public void testHasClassWhitespace() {
        // Element with extra whitespace in class attribute
        Element e = Jsoup.parse("<div class='  foo  bar '></div>").select("div").first();
        assertTrue(e.hasClass("foo"));
        assertTrue(e.hasClass("bar"));
        assertFalse(e.hasClass("foobar"));
    }

    @Test
    public void testHasClassSubstring() {
        // Ensure that hasClass does not match substrings
        Element e = Jsoup.parse("<div class='foobar'></div>").select("div").first();
        assertFalse(e.hasClass("foo"));
        assertFalse(e.hasClass("bar"));
        assertTrue(e.hasClass("foobar"));
    }

    @Test
    public void testHasClassNull() {
        Element e = new Element(Tag.valueOf("div"), "");
        assertFalse(e.hasClass(null));
    }

    @Test
    public void testHasClassEmpty() {
        Element e = new Element(Tag.valueOf("div"), "");
        assertFalse(e.hasClass(""));
    }

    @Test
    public void testHasClassNoClassAttr() {
        Element e = new Element(Tag.valueOf("div"), "");
        assertFalse(e.hasClass("anything"));
    }

    // text content tests
    @Test
    public void testText() {
        assertEquals("Text", p.text());
        // div text includes child text
        assertEquals("Text", div.text()); // only one text node
    }

    @Test
    public void testOwnText() {
        assertEquals("", div.ownText()); // div has no direct text
        assertEquals("Text", p.ownText());
    }

    @Test
    public void testWholeText() {
        assertEquals("Text", p.wholeText());
    }

    // html tests
    @Test
    public void testHtml() {
        assertEquals("<p class=\"para\">Text</p>", div.html());
        assertEquals("Text", p.html());
    }

    @Test
    public void testOuterHtml() {
        String divOuter = "<div id=\"main\" class=\"container content\">\n <p class=\"para\">Text</p>\n</div>";
        // Note: Jsoup may format differently; we check contains key parts
        assertTrue(div.outerHtml().contains("id=\"main\""));
        assertTrue(div.outerHtml().contains("class=\"container content\""));
        assertTrue(div.outerHtml().contains("<p class=\"para\">Text</p>"));
    }

    // data tests (for data elements like script, style)
    @Test
    public void testData() {
        Document doc2 = Jsoup.parse("<script>var x=1;</script>");
        Element script = doc2.select("script").first();
        assertEquals("var x=1;", script.data());
    }

    // val tests (for form elements)
    @Test
    public void testVal() {
        Document doc2 = Jsoup.parse("<input type='text' value='hello'>");
        Element input = doc2.select("input").first();
        assertEquals("hello", input.val());
    }

    @Test
    public void testValTextarea() {
        Document doc2 = Jsoup.parse("<textarea>some text</textarea>");
        Element textarea = doc2.select("textarea").first();
        assertEquals("some text", textarea.val());
    }

    // child node manipulation tests
    @Test
    public void testAppendChild() {
        Element child = new Element(Tag.valueOf("span"), "");
        child.text("new");
        div.appendChild(child);
        assertEquals(2, div.childrenSize()); // p and span
        assertEquals("new", div.child(1).text());
    }

    @Test
    public void testPrependChild() {
        Element child = new Element(Tag.valueOf("span"), "");
        child.text("first");
        div.prependChild(child);
        assertEquals(2, div.childrenSize());
        assertEquals("first", div.child(0).text());
    }

    @Test
    public void testInsertChildren() {
        Element child = new Element(Tag.valueOf("span"), "");
        child.text("inserted");
        div.insertChildren(0, child);
        assertEquals(2, div.childrenSize());
        assertEquals("inserted", div.child(0).text());
    }

    @Test
    public void testRemoveChild() {
        div.removeChild(p);
        assertEquals(0, div.childrenSize());
    }

    @Test
    public void testEmpty() {
        div.empty();
        assertEquals(0, div.childrenSize());
        assertEquals("", div.html());
    }

    // parent, children, sibling tests
    @Test
    public void testParent() {
        assertEquals(div, p.parent());
    }

    @Test
    public void testChildren() {
        Elements children = div.children();
        assertEquals(1, children.size());
        assertEquals(p, children.get(0));
    }

    @Test
    public void testChildNodes() {
        assertEquals(1, div.childNodes().size());
    }

    @Test
    public void testSiblingElements() {
        // p has no siblings in this doc
        assertTrue(p.siblingElements().isEmpty());
    }

    @Test
    public void testNextSibling() {
        assertNull(p.nextSibling());
    }

    @Test
    public void testPreviousSibling() {
        assertNull(p.previousSibling());
    }

    // traversal methods
    @Test
    public void testFirstElementChild() {
        assertEquals(p, div.firstElementChild());
    }

    @Test
    public void testLastElementChild() {
        assertEquals(p, div.lastElementChild());
    }

    @Test
    public void testElementSiblingIndex() {
        assertEquals(0, p.elementSiblingIndex());
    }

    // CSS selector methods (basic)
    @Test
    public void testGetElementById() {
        assertEquals(div, doc.getElementById("main"));
        assertNull(doc.getElementById("nonexistent"));
    }

    @Test
    public void testGetElementsByTag() {
        Elements tags = doc.getElementsByTag("p");
        assertEquals(1, tags.size());
        assertEquals(p, tags.get(0));
    }

    @Test
    public void testGetElementsByClass() {
        Elements classes = doc.getElementsByClass("para");
        assertEquals(1, classes.size());
        assertEquals(p, classes.get(0));
    }

    @Test
    public void testGetElementsByAttribute() {
        Elements attrs = doc.getElementsByAttribute("id");
        assertEquals(1, attrs.size());
        assertEquals(div, attrs.get(0));
    }

    // equals and hashCode
    @Test
    public void testEquals() {
        Element e1 = new Element(Tag.valueOf("div"), "");
        Element e2 = new Element(Tag.valueOf("div"), "");
        // Elements are not equal by default because they have different identities
        assertNotEquals(e1, e2);
    }

    @Test
    public void testHashCode() {
        Element e = new Element(Tag.valueOf("div"), "");
        assertNotNull(e.hashCode());
    }

    // toString
    @Test
    public void testToString() {
        assertTrue(p.toString().contains("<p class=\"para\">Text</p>"));
    }

    // clone
    @Test
    public void testClone() {
        Element clone = p.clone();
        assertEquals(p.tagName(), clone.tagName());
        assertEquals(p.className(), clone.className());
        assertEquals(p.text(), clone.text());
        assertNotSame(p, clone);
    }

    // Edge case: element with no attributes
    @Test
    public void testNoAttributes() {
        Element e = new Element(Tag.valueOf("br"), "");
        assertTrue(e.attributes().isEmpty());
        assertEquals("", e.className());
        assertFalse(e.hasClass("any"));
    }

    // Edge case: element with only id
    @Test
    public void testOnlyId() {
        Element e = Jsoup.parse("<div id='only'></div>").select("div").first();
        assertEquals("only", e.id());
        assertEquals("", e.className());
        assertTrue(e.classNames().isEmpty());
    }

    // Edge case: multiple classes with leading/trailing spaces
    @Test
    public void testClassNamesWithSpaces() {
        Element e = Jsoup.parse("<div class='  a  b  c  '></div>").select("div").first();
        Set<String> expected = new LinkedHashSet<>();
        expected.add("a");
        expected.add("b");
        expected.add("c");
        assertEquals(expected, e.classNames());
    }

    // Edge case: class attribute with newlines or tabs
    @Test
    public void testClassNamesWithWhitespaceVariants() {
        Element e = Jsoup.parse("<div class='a\tb\nc'></div>").select("div").first();
        assertTrue(e.hasClass("a"));
        assertTrue(e.hasClass("b"));
        assertTrue(e.hasClass("c"));
    }

    // Test for bug 61: hasClass should not match partial class names
    @Test
    public void testHasClassPartialMatchBug() {
        Element e = Jsoup.parse("<div class='abc def'></div>").select("div").first();
        assertFalse(e.hasClass("ab"));
        assertFalse(e.hasClass("bc"));
        assertFalse(e.hasClass("de"));
        assertFalse(e.hasClass("ef"));
        assertTrue(e.hasClass("abc"));
        assertTrue(e.hasClass("def"));
    }

    // Test for bug 61: multiple classes with same prefix
    @Test
    public void testHasClassMultipleSimilar() {
        Element e = Jsoup.parse("<div class='foo foobar'></div>").select("div").first();
        assertTrue(e.hasClass("foo"));
        assertTrue(e.hasClass("foobar"));
        assertFalse(e.hasClass("foob"));
    }

    // Test for bug 61: class attribute with only one class but extra spaces
    @Test
    public void testHasClassSingleWithSpaces() {
        Element e = Jsoup.parse("<div class='  single  '></div>").select("div").first();
        assertTrue(e.hasClass("single"));
        assertFalse(e.hasClass("single "));
        assertFalse(e.hasClass(" single"));
    }

    // Test for bug 61: empty class attribute
    @Test
    public void testHasClassEmptyAttr() {
        Element e = Jsoup.parse("<div class=''></div>").select("div").first();
        assertFalse(e.hasClass(""));
        assertFalse(e.hasClass("anything"));
    }

    // Test for bug 61: class attribute with only whitespace
    @Test
    public void testHasClassWhitespaceOnly() {
        Element e = Jsoup.parse("<div class='   '></div>").select("div").first();
        assertFalse(e.hasClass(""));
        assertFalse(e.hasClass(" "));
        assertFalse(e.hasClass("anything"));
    }
}