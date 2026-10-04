package org.jsoup.parser;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * JUnit 4 test suite for TreeBuilder (Jsoup bug 44 context).
 * Tests cover line/branch coverage and fault detection for tree building logic.
 */
public class TreeBuilderTest {

    private Document doc;

    @Before
    public void setUp() {
        // Reset any shared state if needed (none here)
    }

    @After
    public void tearDown() {
        doc = null;
    }

    // ---------- Basic parsing and structure ----------

    @Test
    public void testParseEmptyString() {
        doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals("#root", doc.tagName());
        assertEquals(0, doc.children().size());
    }

    @Test
    public void testParseSimpleHtml() {
        doc = Jsoup.parse("<html><head></head><body><p>Hello</p></body></html>");
        assertNotNull(doc);
        Element html = doc.child(0);
        assertEquals("html", html.tagName());
        Element body = html.child(1);
        assertEquals("body", body.tagName());
        Element p = body.child(0);
        assertEquals("p", p.tagName());
        assertEquals("Hello", p.text());
    }

    @Test
    public void testParseWithBaseUri() {
        doc = Jsoup.parse("<a href='/test'>link</a>", "http://example.com");
        Element a = doc.select("a").first();
        assertEquals("http://example.com/test", a.absUrl("href"));
    }

    // ---------- Edge cases and malformed HTML ----------

    @Test
    public void testParseUnclosedTag() {
        doc = Jsoup.parse("<p>One<p>Two");
        Elements ps = doc.select("p");
        assertEquals(2, ps.size());
        assertEquals("One", ps.get(0).text());
        assertEquals("Two", ps.get(1).text());
    }

    @Test
    public void testParseNestedSameTag() {
        doc = Jsoup.parse("<div><div>inner</div></div>");
        Elements divs = doc.select("div");
        assertEquals(2, divs.size());
        assertEquals("inner", divs.get(1).text());
    }

    @Test
    public void testParseSelfClosingTag() {
        doc = Jsoup.parse("<br/><hr/>");
        Elements brs = doc.select("br");
        assertEquals(1, brs.size());
        Elements hrs = doc.select("hr");
        assertEquals(1, hrs.size());
    }

    @Test
    public void testParseNullInput() {
        // Jsoup.parse(null) should throw NullPointerException or return empty doc?
        // According to Jsoup API, it throws NullPointerException.
        try {
            Jsoup.parse(null);
            fail("Expected NullPointerException for null input");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ---------- Bug 44 specific: <select> inside <form> ----------

    @Test
    public void testSelectInsideForm() {
        // Known bug: parser incorrectly closes form before select
        String html = "<form><select><option>Test</option></select></form>";
        doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull("Form should be present", form);
        Element select = form.select("select").first();
        assertNotNull("Select should be inside form", select);
        Element option = select.select("option").first();
        assertNotNull("Option should be inside select", option);
        assertEquals("Test", option.text());
        // Ensure form is not closed prematurely
        assertEquals("form", form.tagName());
        assertEquals(1, form.children().size()); // only select
    }

    @Test
    public void testMultipleSelectsInsideForm() {
        String html = "<form><select id='s1'><option>A</option></select><select id='s2'><option>B</option></select></form>";
        doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals(2, form.select("select").size());
    }

    @Test
    public void testSelectInsideFormWithOtherElements() {
        String html = "<form><input type='text' name='a'/><select><option>X</option></select><input type='submit'/></form>";
        doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals(3, form.children().size());
        assertEquals("input", form.child(0).tagName());
        assertEquals("select", form.child(1).tagName());
        assertEquals("input", form.child(2).tagName());
    }

    // ---------- Deep nesting and complex structures ----------

    @Test
    public void testDeepNesting() {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (int i = 0; i < 100; i++) {
            sb.append("<span>");
        }
        sb.append("deep");
        for (int i = 0; i < 100; i++) {
            sb.append("</span>");
        }
        sb.append("</div>");
        doc = Jsoup.parse(sb.toString());
        Element div = doc.select("div").first();
        assertNotNull(div);
        Element deepest = div;
        for (int i = 0; i < 100; i++) {
            deepest = deepest.child(0);
        }
        assertEquals("deep", deepest.text());
    }

    @Test
    public void testParseWithDoctype() {
        doc = Jsoup.parse("<!DOCTYPE html><html><body>Hello</body></html>");
        assertNotNull(doc);
        assertEquals("#document", doc.tagName());
        // Doctype node is not a child of document? Actually it's a child node.
        assertEquals(2, doc.children().size()); // doctype and html
    }

    @Test
    public void testParseWithComments() {
        doc = Jsoup.parse("<!-- comment --><p>text</p>");
        assertEquals("text", doc.select("p").text());
        // Comment should be removed from output? Jsoup preserves comments in body.
        // But we can check that the p is still there.
    }

    // ---------- Fault detection: null/empty attributes, unusual characters ----------

    @Test
    public void testParseWithNullAttributeValue() {
        // Some parsers might crash on null attribute values
        doc = Jsoup.parse("<div class='test' id=''>Empty id</div>");
        Element div = doc.select("div").first();
        assertEquals("", div.id());
    }

    @Test
    public void testParseWithSpecialChars() {
        doc = Jsoup.parse("<p>&amp;&lt;&gt;&quot;</p>");
        assertEquals("&<>\"", doc.select("p").text());
    }

    @Test
    public void testParseWithInvalidTagName() {
        doc = Jsoup.parse("<123>text</123>");
        // Invalid tag names are treated as text? Jsoup might ignore or create an element.
        // At least no exception should be thrown.
        assertNotNull(doc);
    }

    // ---------- TreeBuilder specific: process tokens ----------

    // Since TreeBuilder is abstract, we test through HtmlTreeBuilder via Jsoup.parse.
    // Additional coverage can be achieved by testing with different settings.

    @Test
    public void testParseWithXmlTreeBuilder() {
        // Use Jsoup.parse with XML parser
        doc = Jsoup.parse("<root><child attr='val'>text</child></root>", "", org.jsoup.parser.Parser.xmlParser());
        assertNotNull(doc);
        Element root = doc.child(0);
        assertEquals("root", root.tagName());
        Element child = root.child(0);
        assertEquals("child", child.tagName());
        assertEquals("val", child.attr("attr"));
        assertEquals("text", child.text());
    }

    @Test
    public void testParseWithHtmlTreeBuilderAndBaseUri() {
        doc = Jsoup.parse("<img src='/image.png'>", "http://example.com");
        Element img = doc.select("img").first();
        assertEquals("http://example.com/image.png", img.absUrl("src"));
    }

    // ---------- Edge cases for tree building logic ----------

    @Test
    public void testParseTableInsideForm() {
        // Complex structure that might trigger tree building bugs
        String html = "<form><table><tr><td>cell</td></tr></table></form>";
        doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        Element table = form.select("table").first();
        assertNotNull(table);
        assertEquals("cell", table.select("td").text());
    }

    @Test
    public void testParseMultipleForms() {
        String html = "<form id='f1'><input name='a'/></form><form id='f2'><input name='b'/></form>";
        doc = Jsoup.parse(html);
        assertEquals(2, doc.select("form").size());
    }

    @Test
    public void testParseFormWithFieldset() {
        String html = "<form><fieldset><legend>Info</legend><input name='x'/></fieldset></form>";
        doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        Element fieldset = form.select("fieldset").first();
        assertNotNull(fieldset);
        assertEquals(1, fieldset.select("input").size());
    }

    // ---------- Null/empty base URI ----------

    @Test
    public void testParseWithEmptyBaseUri() {
        doc = Jsoup.parse("<a href='test'>link</a>", "");
        Element a = doc.select("a").first();
        // Relative URL with empty base should remain relative
        assertEquals("test", a.attr("href"));
    }

    @Test
    public void testParseWithNullBaseUri() {
        // Jsoup.parse with null base URI should throw NullPointerException
        try {
            Jsoup.parse("<p>test</p>", (String) null);
            fail("Expected NullPointerException for null base URI");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ---------- Large input to test stack/buffer handling ----------

    @Test
    public void testParseLargeInput() {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        for (int i = 0; i < 1000; i++) {
            sb.append("<p>Paragraph ").append(i).append("</p>");
        }
        sb.append("</body></html>");
        doc = Jsoup.parse(sb.toString());
        assertEquals(1000, doc.select("p").size());
    }

    // ---------- Fault detection: incorrect tree structure ----------

    @Test
    public void testParseMisnestedTags() {
        // <b><i>text</b></i> should be corrected to <b><i>text</i></b>
        doc = Jsoup.parse("<b><i>text</b></i>");
        Element b = doc.select("b").first();
        assertNotNull(b);
        Element i = b.select("i").first();
        assertNotNull(i);
        assertEquals("text", i.text());
    }

    @Test
    public void testParseDuplicateAttributes() {
        doc = Jsoup.parse("<div class='a' class='b'>text</div>");
        Element div = doc.select("div").first();
        // Jsoup keeps the first attribute value
        assertEquals("a", div.className());
    }
}