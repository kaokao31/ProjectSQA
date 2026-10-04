package org.jsoup.parser;

import org.junit.Test;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

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
    public void testReset() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        CharacterReader reader = new CharacterReader("<div>test</div>");
        Parser parser = Parser.htmlParser();
        tb.initialiseParse(reader, "http://example.com", parser);
        
        // Exercise reset/re-initialization logic
        tb.reset();
        assertNotNull(tb.state());
    }

    @Test
    public void testFormHandling() {
        String html = "<form action=\"/submit\"><input type=\"text\" name=\"user\" value=\"john\"></form>";
        Document doc = Jsoup.parse(html);
        List<FormElement> forms = doc.forms();
        assertEquals(1, forms.size());
        FormElement form = forms.get(0);
        assertEquals("/submit", form.absUrl("action"));
        assertEquals(1, form.elements().size());
    }

    @Test
    public void testSpecialTagsAndFormatting() {
        String html = "<b><i>Test</i></b>";
        Document doc = Jsoup.parse(html);
        assertEquals("Test", doc.select("b > i").text());
    }

    @Test
    public void testTableInsertionMode() {
        String html = "<table><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertEquals("Cell", doc.select("td").text());
    }

    @Test
    public void testInSelectInsertionMode() {
        String html = "<select><option value=\"1\">One</option><option value=\"2\" selected>Two</option></select>";
        Document doc = Jsoup.parse(html);
        Elements options = doc.select("option");
        assertEquals(2, options.size());
        assertTrue(options.get(1).hasAttr("selected"));
    }

    @Test
    public void testFrameset() {
        String html = "<html><frameset cols=\"25%,75%\"><frame src=\"left.htm\"><frame src=\"right.htm\"></frameset></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc.select("frameset").first());
        assertEquals(2, doc.select("frame").size());
    }

    @Test
    public void testCommentsAndDoctype() {
        String html = "<!DOCTYPE html><html><!-- comment --><body><p>Text</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Text", doc.select("p").text());
    }

    @Test
    public void testMarkerAndFormattingElements() {
        // Specifically targets formatting element cleanups and insertion modes (relevant for Jsoup-33 scope)
        String html = "<div><p>A <b>B <i>C</p>D</b>E</i></div>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }

    @Test
    public void testFosterParenting() {
        // Table foster parenting scenario
        String html = "<table><span>Foster me</span><tr><td>Cell</td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Foster me", doc.select("table").first().previousElementSibling().text());
    }

    @Test
    public void testPendingTableCharacters() {
        String html = "<table>a<td></td></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
    }
}