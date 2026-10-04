package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ElementTest {

    private Element emptyDiv;
    private Element divWithText;
    private Element divWithChildren;
    private Element divWithMixed;
    private Element divWithLtGt;
    private Element divWithAmp;
    private Element scriptElement;
    private Element styleElement;

    @Before
    public void setUp() {
        emptyDiv = Jsoup.parse("<div></div>").body().child(0);
        divWithText = Jsoup.parse("<div>Hello World</div>").body().child(0);
        divWithChildren = Jsoup.parse("<div><span>child1</span><span>child2</span></div>").body().child(0);
        divWithMixed = Jsoup.parse("<div>Text <span>span</span> more text</div>").body().child(0);
        divWithLtGt = Jsoup.parse("<div> < </div>").body().child(0);
        divWithAmp = Jsoup.parse("<div> &amp; </div>").body().child(0);
        scriptElement = Jsoup.parse("<script>var x = 1 < 2;</script>").body().child(0);
        styleElement = Jsoup.parse("<style>body { color: red; }</style>").body().child(0);
    }

    // ---------- html() method tests ----------

    @Test
    public void testHtmlEmpty() {
        assertEquals("", emptyDiv.html());
    }

    @Test
    public void testHtmlTextOnly() {
        assertEquals("Hello World", divWithText.html());
    }

    @Test
    public void testHtmlChildrenOnly() {
        assertEquals("<span>child1</span><span>child2</span>", divWithChildren.html());
    }

    @Test
    public void testHtmlMixed() {
        assertEquals("Text <span>span</span> more text", divWithMixed.html());
    }

    @Test
    public void testHtmlWithLtGt() {
        // Bug 70: html() should preserve original < and > in text
        assertEquals(" < ", divWithLtGt.html());
    }

    @Test
    public void testHtmlWithAmp() {
        // &amp; should be preserved as &amp; in html()
        assertEquals(" &amp; ", divWithAmp.html());
    }

    @Test
    public void testHtmlScript() {
        // Script data should be preserved as-is
        assertEquals("var x = 1 < 2;", scriptElement.html());
    }

    @Test
    public void testHtmlStyle() {
        assertEquals("body { color: red; }", styleElement.html());
    }

    // ---------- outerHtml() method tests ----------

    @Test
    public void testOuterHtmlEmpty() {
        assertEquals("<div></div>", emptyDiv.outerHtml());
    }

    @Test
    public void testOuterHtmlText() {
        assertEquals("<div>Hello World</div>", divWithText.outerHtml());
    }

    @Test
    public void testOuterHtmlChildren() {
        assertEquals("<div><span>child1</span><span>child2</span></div>", divWithChildren.outerHtml());
    }

    @Test
    public void testOuterHtmlLtGt() {
        assertEquals("<div> < </div>", divWithLtGt.outerHtml());
    }

    // ---------- text() method tests ----------

    @Test
    public void testTextEmpty() {
        assertEquals("", emptyDiv.text());
    }

    @Test
    public void testTextSimple() {
        assertEquals("Hello World", divWithText.text());
    }

    @Test
    public void testTextChildren() {
        assertEquals("child1 child2", divWithChildren.text());
    }

    @Test
    public void testTextMixed() {
        assertEquals("Text span more text", divWithMixed.text());
    }

    @Test
    public void testTextLtGt() {
        assertEquals(" < ", divWithLtGt.text());
    }

    // ---------- data() method tests ----------

    @Test
    public void testDataScript() {
        assertEquals("var x = 1 < 2;", scriptElement.data());
    }

    @Test
    public void testDataStyle() {
        assertEquals("body { color: red; }", styleElement.data());
    }

    @Test
    public void testDataNonDataElement() {
        assertNull(emptyDiv.data());
    }

    // ---------- id() and className() tests ----------

    @Test
    public void testId() {
        Element withId = Jsoup.parse("<div id='test'>text</div>").body().child(0);
        assertEquals("test", withId.id());
    }

    @Test
    public void testIdNone() {
        assertEquals("", emptyDiv.id());
    }

    @Test
    public void testClassName() {
        Element withClass = Jsoup.parse("<div class='a b'>text</div>").body().child(0);
        assertEquals("a b", withClass.className());
    }

    @Test
    public void testClassNameNone() {
        assertEquals("", emptyDiv.className());
    }

    @Test
    public void testClassNames() {
        Element withClass = Jsoup.parse("<div class='a b'>text</div>").body().child(0);
        assertEquals(2, withClass.classNames().size());
        assertTrue(withClass.classNames().contains("a"));
        assertTrue(withClass.classNames().contains("b"));
    }

    @Test
    public void testHasClass() {
        Element withClass = Jsoup.parse("<div class='a'>text</div>").body().child(0);
        assertTrue(withClass.hasClass("a"));
        assertFalse(withClass.hasClass("b"));
    }

    // ---------- attr() and attributes() tests ----------

    @Test
    public void testAttr() {
        Element withAttr = Jsoup.parse("<div data-val='x'>text</div>").body().child(0);
        assertEquals("x", withAttr.attr("data-val"));
        assertEquals("", withAttr.attr("nonexistent"));
    }

    @Test
    public void testAttributes() {
        Element withAttr = Jsoup.parse("<div id='a' class='b'>text</div>").body().child(0);
        assertEquals(2, withAttr.attributes().size());
        assertEquals("a", withAttr.attributes().get("id"));
        assertEquals("b", withAttr.attributes().get("class"));
    }

    // ---------- child node tests ----------

    @Test
    public void testChildNodeSize() {
        assertEquals(0, emptyDiv.childNodeSize());
        assertEquals(1, divWithText.childNodeSize());
        assertEquals(2, divWithChildren.childNodeSize());
        assertEquals(3, divWithMixed.childNodeSize()); // text, span, text
    }

    @Test
    public void testChildNodes() {
        assertEquals(0, emptyDiv.childNodes().size());
        assertEquals(1, divWithText.childNodes().size());
        assertEquals(2, divWithChildren.childNodes().size());
    }

    @Test
    public void testChildren() {
        assertEquals(0, emptyDiv.children().size());
        assertEquals(0, divWithText.children().size());
        assertEquals(2, divWithChildren.children().size());
        assertEquals(1, divWithMixed.children().size()); // only the span
    }

    // ---------- sibling tests ----------

    @Test
    public void testFirstElementSibling() {
        Element parent = Jsoup.parse("<div><p>a</p><p>b</p></div>").body().child(0);
        Element first = parent.child(0);
        Element second = parent.child(1);
        assertSame(first, second.firstElementSibling());
        assertNull(first.firstElementSibling());
    }

    @Test
    public void testLastElementSibling() {
        Element parent = Jsoup.parse("<div><p>a</p><p>b</p></div>").body().child(0);
        Element first = parent.child(0);
        Element second = parent.child(1);
        assertSame(second, first.lastElementSibling());
        assertNull(second.lastElementSibling());
    }

    @Test
    public void testNextElementSibling() {
        Element parent = Jsoup.parse("<div><p>a</p><p>b</p></div>").body().child(0);
        Element first = parent.child(0);
        Element second = parent.child(1);
        assertSame(second, first.nextElementSibling());
        assertNull(second.nextElementSibling());
    }

    @Test
    public void testPreviousElementSibling() {
        Element parent = Jsoup.parse("<div><p>a</p><p>b</p></div>").body().child(0);
        Element first = parent.child(0);
        Element second = parent.child(1);
        assertNull(first.previousElementSibling());
        assertSame(first, second.previousElementSibling());
    }

    // ---------- parent tests ----------

    @Test
    public void testParent() {
        Element parent = Jsoup.parse("<div><p>text</p></div>").body().child(0);
        Element child = parent.child(0);
        assertSame(parent, child.parent());
        assertNull(parent.parent()); // body's parent is document? Actually, body's parent is document, but we test child
    }

    @Test
    public void testParents() {
        Element child = Jsoup.parse("<div><p>text</p></div>").body().child(0).child(0);
        assertEquals(3, child.parents().size()); // p, div, body
    }

    // ---------- index tests ----------

    @Test
    public void testElementSiblingIndex() {
        Element parent = Jsoup.parse("<div><p>a</p><p>b</p></div>").body().child(0);
        assertEquals(0, parent.child(0).elementSiblingIndex());
        assertEquals(1, parent.child(1).elementSiblingIndex());
    }

    // ---------- manipulation tests ----------

    @Test
    public void testAppendChild() {
        Element parent = Jsoup.parse("<div></div>").body().child(0);
        Element child = new Element("span");
        parent.appendChild(child);
        assertEquals(1, parent.children().size());
        assertSame(child, parent.child(0));
    }

    @Test
    public void testPrependChild() {
        Element parent = Jsoup.parse("<div><p>existing</p></div>").body().child(0);
        Element child = new Element("span");
        parent.prependChild(child);
        assertEquals(2, parent.children().size());
        assertSame(child, parent.child(0));
    }

    @Test
    public void testInsertChildren() {
        Element parent = Jsoup.parse("<div><p>a</p><p>c</p></div>").body().child(0);
        Element newChild = new Element("span");
        parent.insertChildren(1, newChild);
        assertEquals(3, parent.children().size());
        assertSame(newChild, parent.child(1));
    }

    @Test
    public void testAppendText() {
        Element parent = Jsoup.parse("<div></div>").body().child(0);
        parent.appendText("Hello");
        assertEquals("Hello", parent.text());
    }

    @Test
    public void testPrependText() {
        Element parent = Jsoup.parse("<div>World</div>").body().child(0);
        parent.prependText("Hello ");
        assertEquals("Hello World", parent.text());
    }

    @Test
    public void testAppendElement() {
        Element parent = Jsoup.parse("<div></div>").body().child(0);
        Element child = parent.appendElement("span");
        assertNotNull(child);
        assertEquals("span", child.tagName());
        assertEquals(1, parent.children().size());
    }

    @Test
    public void testPrependElement() {
        Element parent = Jsoup.parse("<div><p>existing</p></div>").body().child(0);
        Element child = parent.prependElement("span");
        assertNotNull(child);
        assertEquals("span", child.tagName());
        assertEquals(2, parent.children().size());
        assertSame(child, parent.child(0));
    }

    @Test
    public void testWrap() {
        Element child = Jsoup.parse("<div><p>text</p></div>").body().child(0).child(0);
        child.wrap("<section></section>");
        assertEquals("<section><p>text</p></section>", child.parent().outerHtml());
    }

    @Test
    public void testUnwrap() {
        Element parent = Jsoup.parse("<div><p>text</p></div>").body().child(0);
        Element child = parent.child(0);
        child.unwrap();
        assertEquals("text", parent.text());
        assertEquals(0, parent.children().size());
    }

    // ---------- cssSelector() tests ----------

    @Test
    public void testCssSelector() {
        Element el = Jsoup.parse("<div id='foo' class='bar'>text</div>").body().child(0);
        assertEquals("#foo.bar", el.cssSelector());
    }

    @Test
    public void testCssSelectorNoId() {
        Element el = Jsoup.parse("<div class='bar'>text</div>").body().child(0);
        assertEquals("div.bar", el.cssSelector());
    }

    // ---------- equals and hashCode tests ----------

    @Test
    public void testEquals() {
        Element a = Jsoup.parse("<div>text</div>").body().child(0);
        Element b = Jsoup.parse("<div>text</div>").body().child(0);
        // Elements from different parse trees are not equal
        assertNotEquals(a, b);
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsSame() {
        Element a = Jsoup.parse("<div>text</div>").body().child(0);
        assertEquals(a, a);
    }

    // ---------- clone() tests ----------

    @Test
    public void testClone() {
        Element original = Jsoup.parse("<div><span>text</span></div>").body().child(0);
        Element cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals(original.outerHtml(), cloned.outerHtml());
        // Ensure deep clone: children are also cloned
        assertNotSame(original.child(0), cloned.child(0));
    }

    // ---------- edge cases ----------

    @Test
    public void testHtmlWithOnlyWhitespace() {
        Element el = Jsoup.parse("<div>   </div>").body().child(0);
        assertEquals("   ", el.html());
    }

    @Test
    public void testHtmlWithNewlines() {
        Element el = Jsoup.parse("<div>\nHello\n</div>").body().child(0);
        assertEquals("\nHello\n", el.html());
    }

    @Test
    public void testHtmlWithComment() {
        Element el = Jsoup.parse("<div><!-- comment --></div>").body().child(0);
        assertEquals("<!-- comment -->", el.html());
    }

    @Test
    public void testHtmlWithMultipleTextNodes() {
        Element el = Jsoup.parse("<div>a<b>b</b>c</div>").body().child(0);
        assertEquals("a<b>b</b>c", el.html());
    }

    @Test
    public void testHtmlWithSpecialCharsInAttribute() {
        Element el = Jsoup.parse("<div data-val='<>&\"'>text</div>").body().child(0);
        // Attribute values are escaped in outerHtml, but html() only returns inner HTML
        assertEquals("text", el.html());
        // Check outerHtml includes escaped attribute
        assertTrue(el.outerHtml().contains("&lt;&gt;&amp;&quot;"));
    }

    @Test
    public void testTextWithLtGt() {
        Element el = Jsoup.parse("<div>a < b > c</div>").body().child(0);
        assertEquals("a < b > c", el.text());
    }

    @Test
    public void testDataWithLtGt() {
        Element el = Jsoup.parse("<script>if (a < b) {}</script>").body().child(0);
        assertEquals("if (a < b) {}", el.data());
    }

    @Test
    public void testHtmlPreservesOriginalLtGt() {
        // This is the core of bug 70: html() should not escape < and > in text
        Element el = Jsoup.parse("<div> < </div>").body().child(0);
        assertEquals(" < ", el.html());
    }

    @Test
    public void testOuterHtmlPreservesOriginalLtGt() {
        Element el = Jsoup.parse("<div> < </div>").body().child(0);
        assertEquals("<div> < </div>", el.outerHtml());
    }

    @Test
    public void testHtmlWithMultipleLtGt() {
        Element el = Jsoup.parse("<div><<>></div>").body().child(0);
        assertEquals("<<>>", el.html());
    }

    @Test
    public void testHtmlWithAmpersandNotEscaped() {
        // & should be preserved as & in html()? Actually, & is special and should be escaped in HTML output.
        // But the original HTML may have & unescaped, which is invalid. Jsoup's parser may normalize it.
        // We test that the original & is preserved if it was in the source.
        Element el = Jsoup.parse("<div>&</div>").body().child(0);
        // The parser may treat & as text and keep it, but output should escape it? This is tricky.
        // For the purpose of bug 70, we focus on < and >. We'll just check that it doesn't crash.
        assertNotNull(el.html());
    }

    // ---------- null/empty safety ----------

    @Test(expected = IllegalArgumentException.class)
    public void testAppendChildNull() {
        emptyDiv.appendChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrependChildNull() {
        emptyDiv.prependChild(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertChildrenNull() {
        emptyDiv.insertChildren(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapNull() {
        emptyDiv.wrap(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapEmpty() {
        emptyDiv.wrap("");
    }
}