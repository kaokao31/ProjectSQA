package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;

import java.io.StringReader;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    @Test
    public void testParsingSimpleHtml() {
        String html = "<html><head><title>Test</title></head><body><p>Hello World</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello World", doc.select("p").first().text());
    }

    @Test
    public void testFormElementRegistrationAndTagHandling() {
        // This targets potential bugs in HtmlTreeBuilder State transitions and form element handling (Defects4J #45 context)
        String html = "<html><body><form action=\"/submit\"><input type=\"text\" name=\"user\" value=\"jsoup\"><button>Submit</button></form></body></html>";
        Document doc = Jsoup.parse(html);
        
        List<FormElement> forms = doc.forms();
        assertNotNull(forms);
        assertEquals(1, forms.size());
        
        FormElement form = forms.get(0);
        assertEquals("/submit", form.absUrl("action"));
        
        // Ensure elements inside the form are correctly associated
        Element input = form.select("input").first();
        assertNotNull(input);
        assertEquals("user", input.attr("name"));
        assertEquals("jsoup", input.attr("value"));
    }

    @Test
    public void testResetInsertionMode() {
        // Test various nesting tags that trigger resetInsertionMode in HtmlTreeBuilder
        String html = "<table>" +
                "<caption>Caption</caption>" +
                "<colgroup><col></colgroup>" +
                "<thead><tr><th>Header</th></tr></thead>" +
                "<tbody><tr><td>Data</td></tr></tbody>" +
                "<tfoot><tr><td>Footer</td></tr></tfoot>" +
                "</table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Caption", doc.select("caption").text());
        assertEquals("Header", doc.select("th").text());
        assertEquals("Data", doc.select("td").text());
        assertEquals("Footer", doc.select("tfoot td").text());
    }

    @Test
    public void testSelectScopeAndFormattingElements() {
        // Testing formatting elements stack and scope handling
        String html = "<p><b><i>Formatted text</p></b></i>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        // Jsoup should correct and close tags properly
        assertTrue(doc.select("p").text().contains("Formatted text"));
    }

    @Test
    public void testFramesetAndBodyHandling() {
        String html = "<html><frameset><frame src=\"a.html\"><frame src=\"b.html\"></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals(2, doc.select("frame").size());
    }

    @Test
    public void testSelectElementInScope() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document doc = tb.parse(new StringReader("<div><p><span>Text</span></p></div>"), "http://example.com");
        assertNotNull(doc);
        
        // Directly exercise some methods if accessible or via integration through parser
        Element div = doc.select("div").first();
        assertNotNull(div);
    }

    @Test
    public void testBogusCommentsAndProcessingInstructions() {
        String html = "<?xml version=\"1.0\"?><!-- Comment --><root>Data</root>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Data", doc.select("root").text());
    }

    @Test
    public void testForeignContentAndSelect() {
        String html = "<select><option>One</option><option>Two</option></select>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals(2, doc.select("option").size());
        assertEquals("One", doc.select("option").first().text());
    }

    @Test
    public void testButtonInButtonEdgeCase() {
        // HTML5 specs state a button cannot contain another button. TreeBuilder handles this.
        String html = "<button>Outer <button>Inner</button></button>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertTrue(doc.select("button").size() >= 1);
    }

    @Test
    public void testEmptyHtml() {
        Document doc = Jsoup.parse("");
        assertNotNull(doc);
        assertEquals("", doc.text());
    }

    @Test
    public void testUnclosedTags() {
        String html = "<p>Unclosed paragraph <b>Bold text";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Unclosed paragraph Bold text", doc.body().text());
    }
}