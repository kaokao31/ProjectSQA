package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test suite for HtmlTreeBuilder, focusing on self-closing tag handling (Defects4J bug 63).
 */
public class HtmlTreeBuilderTest {

    private Document doc;

    @Before
    public void setUp() {
        // Reset any static state if needed (Jsoup is generally stateless)
    }

    // --- Self-closing tag tests (bug 63 context) ---

    @Test
    public void testSelfClosingBr() {
        doc = Jsoup.parse("<br/><br/>");
        Elements brs = doc.select("br");
        assertEquals("Should have two <br> elements", 2, brs.size());
        // Ensure they are siblings, not nested
        assertEquals("First br should be direct child of body", "body", brs.get(0).parent().tagName());
        assertEquals("Second br should be direct child of body", "body", brs.get(1).parent().tagName());
    }

    @Test
    public void testSelfClosingImg() {
        doc = Jsoup.parse("<img src='a'/><img src='b'/>");
        Elements imgs = doc.select("img");
        assertEquals("Should have two <img> elements", 2, imgs.size());
        assertEquals("src attribute of first img", "a", imgs.get(0).attr("src"));
        assertEquals("src attribute of second img", "b", imgs.get(1).attr("src"));
    }

    @Test
    public void testSelfClosingInput() {
        doc = Jsoup.parse("<input type='text'/><input type='checkbox'/>");
        Elements inputs = doc.select("input");
        assertEquals("Should have two <input> elements", 2, inputs.size());
    }

    @Test
    public void testSelfClosingTagWithAttributes() {
        doc = Jsoup.parse("<br class='clear'/>");
        Element br = doc.selectFirst("br");
        assertNotNull("br element should exist", br);
        assertEquals("class attribute", "clear", br.attr("class"));
    }

    @Test
    public void testSelfClosingTagFollowedByText() {
        doc = Jsoup.parse("<br/>some text");
        assertEquals("Body should have two children: br and text node", 2, doc.body().childNodeSize());
        assertEquals("Text should be after br", "some text", doc.body().text());
    }

    @Test
    public void testSelfClosingTagInsideDiv() {
        doc = Jsoup.parse("<div><br/><br/></div>");
        Elements brs = doc.select("div br");
        assertEquals("Div should contain two br elements", 2, brs.size());
    }

    @Test
    public void testNonVoidSelfClosingTag() {
        // Custom non-void tag with self-closing syntax should be treated as open tag
        doc = Jsoup.parse("<custom/><custom/>");
        Elements customs = doc.select("custom");
        // In HTML5, custom tags are not void, so they should be treated as open tags
        // This test checks that the parser does not incorrectly self-close them
        // The expected behavior may vary; we assert that there is exactly one <custom> element
        // because the second <custom/> would be nested inside the first.
        assertEquals("Custom non-void tags should not self-close", 1, customs.size());
    }

    // --- Edge cases and boundary tests ---

    @Test
    public void testEmptyInput() {
        doc = Jsoup.parse("");
        assertNotNull("Document should not be null", doc);
        assertEquals("Body should be empty", 0, doc.body().childNodeSize());
    }

    @Test
    public void testNullInput() {
        // Jsoup.parse(null) should throw IllegalArgumentException
        try {
            Jsoup.parse((String) null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testOnlyWhitespace() {
        doc = Jsoup.parse("   \n\t");
        assertEquals("Body should have no children", 0, doc.body().childNodeSize());
    }

    @Test
    public void testMalformedSelfClosingTag() {
        // Missing slash: <br> should still be self-closing (void element)
        doc = Jsoup.parse("<br><br>");
        Elements brs = doc.select("br");
        assertEquals("Two br elements expected", 2, brs.size());
    }

    @Test
    public void testSelfClosingTagWithNoSpaceBeforeSlash() {
        doc = Jsoup.parse("<br/>");
        assertNotNull("br element should exist", doc.selectFirst("br"));
    }

    @Test
    public void testMultipleSelfClosingTagsInSequence() {
        doc = Jsoup.parse("<br/><hr/><img src='x'/>");
        assertEquals("Body should have three children", 3, doc.body().childNodeSize());
        assertEquals("First child should be br", "br", doc.body().child(0).tagName());
        assertEquals("Second child should be hr", "hr", doc.body().child(1).tagName());
        assertEquals("Third child should be img", "img", doc.body().child(2).tagName());
    }

    @Test
    public void testSelfClosingTagInsideTable() {
        doc = Jsoup.parse("<table><tr><td><br/></td></tr></table>");
        Element td = doc.selectFirst("td");
        assertNotNull("td should exist", td);
        assertEquals("td should contain one br", 1, td.childrenSize());
    }

    @Test
    public void testSelfClosingTagWithUnclosedParent() {
        // <div><br/> should close the div implicitly? Actually <br/> is self-closing, so div remains open.
        doc = Jsoup.parse("<div><br/>");
        Element div = doc.selectFirst("div");
        assertNotNull("div should exist", div);
        assertEquals("div should contain one br", 1, div.childrenSize());
    }

    // --- Additional coverage: normal tags, formatting, etc. ---

    @Test
    public void testNormalTagParsing() {
        doc = Jsoup.parse("<p>Hello</p>");
        assertEquals("p element should contain text", "Hello", doc.selectFirst("p").text());
    }

    @Test
    public void testNestedTags() {
        doc = Jsoup.parse("<div><span>text</span></div>");
        assertEquals("span should be inside div", "span", doc.selectFirst("div span").tagName());
    }

    @Test
    public void testVoidElementWithoutSlash() {
        // <br> is a void element, should be self-closing even without slash
        doc = Jsoup.parse("<br>");
        assertNotNull("br should exist", doc.selectFirst("br"));
    }

    @Test
    public void testScriptTagNotSelfClosing() {
        // <script> is not void, self-closing syntax should be ignored
        doc = Jsoup.parse("<script/>");
        // The parser should treat <script/> as an open script tag, and the rest as content
        // Since there is no closing tag, the script tag may be left open
        Element script = doc.selectFirst("script");
        assertNotNull("script element should exist", script);
        // The body should contain the script element
        assertTrue("Body should contain script", doc.body().children().contains(script));
    }

    @Test
    public void testDoctypeHandling() {
        doc = Jsoup.parse("<!DOCTYPE html><html><body></body></html>");
        assertNotNull("Document should have a doctype", doc.documentType());
    }

    @Test
    public void testCommentHandling() {
        doc = Jsoup.parse("<!-- comment --><p>text</p>");
        assertEquals("Body should have one child (p)", 1, doc.body().childNodeSize());
    }

    @Test
    public void testDeepNesting() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("<div>");
        }
        sb.append("content");
        for (int i = 0; i < 100; i++) {
            sb.append("</div>");
        }
        doc = Jsoup.parse(sb.toString());
        assertNotNull("Document should parse deep nesting", doc);
        assertEquals("Deep nesting should produce correct structure", "content", doc.text());
    }
}