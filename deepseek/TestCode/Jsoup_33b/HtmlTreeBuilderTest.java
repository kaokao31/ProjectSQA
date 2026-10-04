package org.jsoup.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for HtmlTreeBuilder.
 * Designed to achieve high line/branch coverage and detect faults
 * (including Defects4J bug 33 related to self-closing tags).
 */
public class HtmlTreeBuilderTest {

    private Document doc;

    @Before
    public void setUp() {
        // Reset document before each test (if needed)
        doc = null;
    }

    // ===== Basic parsing tests =====

    @Test
    public void testParseSimpleHtml() {
        doc = Jsoup.parse("<html><head><title>Test</title></head><body><p>Hello</p></body></html>");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParseEmptyString() {
        doc = Jsoup.parse("");
        assertNotNull(doc);
        assertTrue(doc.children().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNull() {
        Jsoup.parse(null);
    }

    @Test
    public void testParseOnlyDoctype() {
        doc = Jsoup.parse("<!DOCTYPE html>");
        assertNotNull(doc);
        assertNotNull(doc.documentType());
    }

    // ===== Self-closing tag handling (bug 33 context) =====

    @Test
    public void testParseSelfClosingBr() {
        // This should not throw NullPointerException (bug 33)
        doc = Jsoup.parse("<br>");
        assertNotNull(doc);
        Elements brs = doc.select("br");
        assertEquals(1, brs.size());
    }

    @Test
    public void testParseSelfClosingBrWithSlash() {
        doc = Jsoup.parse("<br />");
        assertNotNull(doc);
        assertEquals(1, doc.select("br").size());
    }

    @Test
    public void testParseMultipleSelfClosingTags() {
        doc = Jsoup.parse("<hr><br><img src='test.png'>");
        assertNotNull(doc);
        assertEquals(1, doc.select("hr").size());
        assertEquals(1, doc.select("br").size());
        assertEquals(1, doc.select("img").size());
    }

    @Test
    public void testParseSelfClosingTagInTable() {
        // Known tricky case: self-closing inside table
        doc = Jsoup.parse("<table><tr><td><br></td></tr></table>");
        assertNotNull(doc);
        assertEquals(1, doc.select("br").size());
    }

    // ===== Malformed HTML / error recovery =====

    @Test
    public void testParseUnclosedTags() {
        doc = Jsoup.parse("<p>One<p>Two");
        assertNotNull(doc);
        // Jsoup should close paragraphs automatically
        assertEquals(2, doc.select("p").size());
    }

    @Test
    public void testParseMisnestedTags() {
        doc = Jsoup.parse("<b><i>bold and italic</b></i>");
        assertNotNull(doc);
        // Jsoup should fix misnesting
        assertEquals("bold and italic", doc.body().text());
    }

    @Test
    public void testParseWithComments() {
        doc = Jsoup.parse("<!-- comment --><p>text</p>");
        assertNotNull(doc);
        assertEquals("text", doc.body().text());
    }

    @Test
    public void testParseWithScriptAndStyle() {
        doc = Jsoup.parse("<script>var x=1;</script><style>body{}</style><p>text</p>");
        assertNotNull(doc);
        assertEquals("text", doc.body().text());
        assertEquals(1, doc.select("script").size());
        assertEquals(1, doc.select("style").size());
    }

    // ===== Edge cases for tree builder states =====

    @Test
    public void testParseInBodyWithForm() {
        doc = Jsoup.parse("<form><input></form>");
        assertNotNull(doc);
        assertEquals(1, doc.select("input").size());
    }

    @Test
    public void testParseFosterParenting() {
        // Table foster parenting: text before table should be moved
        doc = Jsoup.parse("text<table><tr><td>cell</td></tr></table>");
        assertNotNull(doc);
        // The text "text" should appear before the table in body
        assertEquals("text", doc.body().text().substring(0, 4));
    }

    @Test
    public void testParseWithDeepNesting() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("<div>");
        }
        sb.append("deep");
        for (int i = 0; i < 100; i++) {
            sb.append("</div>");
        }
        doc = Jsoup.parse(sb.toString());
        assertNotNull(doc);
        assertEquals("deep", doc.body().text());
    }

    @Test
    public void testParseWithSpecialCharacters() {
        doc = Jsoup.parse("<p>&amp; &lt; &gt; &quot;</p>");
        assertNotNull(doc);
        assertEquals("& < > \"", doc.select("p").text());
    }

    // ===== Direct HtmlTreeBuilder method tests (if accessible) =====
    // Note: HtmlTreeBuilder is public but its constructor requires a TreeBuilder parameter.
    // We can test via Jsoup.parse which internally uses it.

    @Test
    public void testParseFragment() {
        // Use Jsoup.parseBodyFragment to test body parsing
        doc = Jsoup.parseBodyFragment("<p>fragment</p>");
        assertNotNull(doc);
        assertEquals("fragment", doc.body().text());
    }

    @Test
    public void testParseWithBaseUri() {
        doc = Jsoup.parse("<a href='/test'>link</a>", "http://example.com");
        assertNotNull(doc);
        Element link = doc.select("a").first();
        assertEquals("http://example.com/test", link.absUrl("href"));
    }

    // ===== Additional coverage for edge branches =====

    @Test
    public void testParseWithMultipleDoctypes() {
        // Only first doctype should be recognized
        doc = Jsoup.parse("<!DOCTYPE html><!DOCTYPE html>");
        assertNotNull(doc);
        // Should have only one doctype node
        assertEquals(1, doc.childNodes().size());
    }

    @Test
    public void testParseWithHtmlInComment() {
        doc = Jsoup.parse("<!-- <html> --><p>text</p>");
        assertNotNull(doc);
        assertEquals("text", doc.body().text());
    }

    @Test
    public void testParseWithCdata() {
        doc = Jsoup.parse("<![CDATA[<tag>]]>");
        assertNotNull(doc);
        // CDATA should be treated as text
        assertTrue(doc.body().text().contains("<tag>"));
    }

    @Test
    public void testParseWithXmlDeclaration() {
        doc = Jsoup.parse("<?xml version='1.0'?><html></html>");
        assertNotNull(doc);
        // XML declaration should be ignored
        assertNotNull(doc.head());
    }

    // ===== Bug-specific regression test for Defects4J bug 33 =====
    // This test reproduces the NullPointerException scenario
    @Test
    public void testBug33SelfClosingBrInSpecificContext() {
        // The bug was triggered when a <br> appeared in certain table contexts
        // This test ensures no exception is thrown
        doc = Jsoup.parse("<table><tr><td><br></td></tr></table>");
        assertNotNull(doc);
        assertEquals(1, doc.select("br").size());
        // Additional check: the <br> should be inside the <td>
        Element td = doc.select("td").first();
        assertNotNull(td);
        assertEquals(1, td.select("br").size());
    }

    @Test
    public void testBug33SelfClosingBrInList() {
        doc = Jsoup.parse("<ul><li><br></li></ul>");
        assertNotNull(doc);
        assertEquals(1, doc.select("br").size());
    }

    @Test
    public void testBug33SelfClosingBrAfterText() {
        doc = Jsoup.parse("text<br>more text");
        assertNotNull(doc);
        assertEquals("text more text", doc.body().text());
    }
}