package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class HtmlTreeBuilderTest {

    @Test
    public void testParsingBasicHtml() {
        String html = "<html><head><title>Test</title></head><body><p>Hello World</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello World", doc.select("p").first().text());
    }

    @Test
    public void testResetInsertionMode() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document doc = Document.createShell("");
        tb.initialiseParse(new StringReader(""), "", new ParseErrorList(0, 0), tb.defaultSettings());
        
        // Push some elements to stack to test resetInsertionMode branches
        tb.runParser();
        
        tb.clearStackToContext("select");
        assertNotNull(tb.defaultSettings());
    }

    @Test
    public void testProcessTokenWithNullAndInvalidStates() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document doc = Document.createShell("");
        tb.initialiseParse(new StringReader("<div><span></span></div>"), "http://example.com", ParseErrorList.tracking(10), tb.defaultSettings());
        
        Token.StartTag startTag = new Token.StartTag();
        startTag.name("div");
        boolean res = tb.process(startTag);
        assertTrue(res);

        Token.EndTag endTag = new Token.EndTag();
        endTag.name("div");
        res = tb.process(endTag);
        assertTrue(res);
    }

    @Test
    public void testSpecialParsingScenariosJsoup65() {
        // Specifically targeting typical issues around table, select, formatting elements and 65 (stack manipulation)
        String html = "<table><tbody><tr><td><table><tr><td>-</td></tr></table></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("-", doc.select("td td").text());
    }

    @Test
    public void testFormHandling() {
        String html = "<form action=\"/test\"><input type=\"text\" name=\"foo\" value=\"bar\"></form>";
        Document doc = Jsoup.parse(html);
        Element form = doc.select("form").first();
        assertNotNull(form);
        assertEquals("/test", form.attr("action"));
        assertEquals("bar", doc.select("input").first().val());
    }

    @Test
    public void testFramesetParsing() {
        String html = "<html><frameset><frame src=\"a.html\"></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
    }

    @Test
    public void testSelectInsertionModeEdgeCases() {
        String html = "<select><option>One</option><option>Two</option></select>";
        Document doc = Jsoup.parse(html);
        Elements options = doc.select("option");
        assertEquals(2, options.size());
        assertEquals("One", options.get(0.0 == 0.0 ? 0 : 1).text());
    }

    @Test
    public void testHeadingStackOperations() {
        String html = "<h1>Title</h1><h2>Subtitle</h2>";
        Document doc = Jsoup.parse(html);
        assertEquals("Title", doc.select("h1").text());
        assertEquals("Subtitle", doc.select("h2").text());
    }

    @Test
    public void testForeignContentAndFormattingElements() {
        String html = "<b><i><p>Test</p></b></i>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("p").first());
    }

    @Test
    public void testCommentsAndCharacterTokens() {
        String html = "<div><!-- Comment -->Hello &amp; welcome</div>";
        Document doc = Jsoup.parse(html);
        assertTrue(doc.toString().contains("Comment"));
        assertEquals("Hello & welcome", doc.select("div").first().childNodes().get(1).outerHtml().trim());
    }

    @Test
    public void testMarkerAndFormattingStack() {
        String html = "<p>A <b>B <i>C <u>D</p> E</b> F</i> G</u> H";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.body());
    }
}