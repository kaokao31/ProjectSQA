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
        assertEquals("Hello World", doc.select("p").text());
    }

    @Test
    public void testSpecificTreeBuilderMethods() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        assertNotNull(tb.defaultSettings());
        
        // Test parsing with an existing parser/treebuilder instance
        Document doc = tb.parse(new StringReader("<div><span>Text</span></div>"), "http://example.com");
        assertNotNull(doc);
        assertEquals("Text", doc.select("span").text());
    }

    @Test
    public void testStackManipulation() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Document doc = Document.createShell("http://example.com");
        tb.initialiseParse(new StringReader("<div><p>Paragraph</p></div>"), "http://example.com", new ParseErrorList(10), ParseSettings.preserveCase);
        
        // Test basic stack queries
        Element el = tb.currentElement();
        assertNotNull(el);
        
        boolean inStack = tb.onStack(doc.body());
        // Initially body might not be explicitly pushed or might be through default state
        assertNotNull(tb.getFromStack("body"));
    }

    @Test
    public void testResetInsertionMode() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader("<table><tr><td>Cell</td></tr></table>"), "http://example.com", ParseErrorList.tracking(10), ParseSettings.defaultSettings);
        
        // Trigger internal methods via parsing complex nested structures
        Document doc = tb.parse(new StringReader("<!DOCTYPE html><html><body><table><tr><td>Data</td></tr></table></body></html>"), "");
        assertNotNull(doc);
        assertEquals("Data", doc.select("td").text());
    }

    @Test
    public void testErrorTracking() {
        ParseErrorList errorList = ParseErrorList.tracking(10);
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        tb.initialiseParse(new StringReader("<invalid></invalid>"), "http://example.com", errorList, ParseSettings.defaultSettings);
        
        // Process tokens or run parser
        Document doc = tb.parse(new StringReader("<html><head></head><body><unclosed></body></html>"), "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testReparentNodesAndFormattingElements() {
        // Specifically targeting complex foster parenting / formatting element stacks (common in Jsoup 67 bugs)
        String html = "<div><b><i>Test</b></i></div>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Test", doc.select("div").text());
    }

    @Test
    public void testMarkerAndFormattingStack() {
        String html = "<p><b>1<i>2<b>3</i>4</b>5</i>6</p>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertTrue(doc.html().contains("<b>"));
    }

    @Test
    public void testCloneInstance() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        HtmlTreeBuilder clone = tb.newInstance();
        assertNotNull(clone);
        assertNotSame(tb, clone);
    }

    @Test
    public void testRunParserDirectly() {
        HtmlTreeBuilder tb = new HtmlTreeBuilder();
        Tokeniser tokeniser = new Tokeniser(new CharacterReader("<div>Test</div>"), ParseErrorList.noTracking());
        tb.initialiseParse(new StringReader("<div>Test</div>"), "http://example.com", ParseErrorList.noTracking(), ParseSettings.defaultSettings);
        Document doc = tb.parse(new StringReader("<div>Test</div>"), "http://example.com");
        assertNotNull(doc);
    }
}