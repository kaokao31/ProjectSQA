package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class DocumentTest {

    @Test
    public void testCreateShell() {
        Document doc = Document.createShell("http://example.com/");
        assertNotNull(doc);
        assertEquals("http://example.com/", doc.baseUri());
        assertEquals("http://example.com/", doc.location());
        
        Element html = doc.child(0);
        assertEquals("html", html.tagName());
        assertEquals(2, html.children().size());
        
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
        
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    @Test
    public void testConstructorAndLocation() {
        Document doc = new Document("http://test.org");
        assertEquals("http://test.org", doc.location());
        assertEquals("#document", doc.nodeName());
    }

    @Test
    public void testTitleGetAndSet() {
        Document doc = Document.createShell("");
        assertEquals("", doc.title());

        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertEquals("Test Title", doc.head().getElementsByTag("title").first().text());

        doc.title("Updated Title");
        assertEquals("Updated Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("title").size());

        // Test title when no title element exists initially
        Document doc2 = new Document("");
        Element html = doc2.appendElement("html");
        html.appendElement("body");
        doc2.title("New Title in Headless Doc");
        assertEquals("New Title in Headless Doc", doc2.title());
        assertNotNull(doc2.head());
    }

    @Test
    public void testCreateElement() {
        Document doc = new Document("http://example.com/");
        Element div = doc.createElement("div");
        assertNotNull(div);
        assertEquals("div", div.tagName());
        assertEquals("http://example.com/", div.baseUri());
    }

    @Test
    public void testNormaliseStructure() {
        Document doc = new Document("http://example.com/");
        doc.appendElement("p").text("Hello");
        assertEquals(1, doc.children().size());
        assertEquals("p", doc.child(0).tagName());

        doc.normalise();

        assertEquals(1, doc.children().size());
        Element html = doc.child(0);
        assertEquals("html", html.tagName());
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testNormaliseWithExistingStructure() {
        Document doc = new Document("");
        Element html = doc.appendElement("html");
        html.appendElement("head");
        html.appendElement("body").appendElement("p").text("Content");

        doc.normalise();
        assertEquals("Content", doc.body().text());
    }

    @Test
    public void testNormaliseWithTextNodesAndDuplicates() {
        Document doc = new Document("");
        doc.appendText("Text node at root");
        doc.appendElement("html").appendElement("body").text("Body text");
        doc.appendElement("html");

        doc.normalise();
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertTrue(doc.body().text().contains("Body text"));
        assertTrue(doc.body().text().contains("Text node at root"));
    }

    @Test
    public void testCharsetAndOutputSettings() {
        Document doc = new Document("http://example.com/");
        assertEquals(StandardCharsets.UTF_8, doc.charset());

        doc.charset(StandardCharsets.ISO_8859_1);
        assertEquals(StandardCharsets.ISO_8859_1, doc.charset());
        assertEquals(StandardCharsets.ISO_8859_1, doc.outputSettings().charset());

        doc.charset(Charset.forName("US-ASCII"));
        assertEquals(Charset.forName("US-ASCII"), doc.charset());
    }

    @Test
    public void testUpdateMetaCharsetElement() {
        Document doc = Document.createShell("");
        assertFalse(doc.updateMetaCharsetElement());

        doc.updateMetaCharsetElement(true);
        assertTrue(doc.updateMetaCharsetElement());

        doc.charset(StandardCharsets.UTF_8);
        Element meta = doc.selectFirst("meta[charset]");
        assertNotNull(meta);
        assertEquals("UTF-8", meta.attr("charset"));

        doc.charset(StandardCharsets.ISO_8859_1);
        assertEquals("ISO-8859-1", doc.selectFirst("meta[charset]").attr("charset"));
    }

    @Test
    public void testUpdateMetaCharsetXmlSyntax() {
        Document doc = Document.createShell("");
        doc.outputSettings().syntax(Syntax.xml);
        doc.updateMetaCharsetElement(true);
        doc.charset(StandardCharsets.UTF_8);

        Element meta = doc.selectFirst("meta[charset]");
        assertNotNull(meta);
        assertEquals("UTF-8", meta.attr("charset"));
    }

    @Test
    public void testClone() {
        Document doc = Document.createShell("http://example.com/");
        doc.title("Original");
        doc.outputSettings().indentAmount(4).prettyPrint(false).syntax(Syntax.xml);
        doc.quirksMode(Document.QuirksMode.quirks);

        Document clone = doc.clone();
        assertNotSame(doc, clone);
        assertEquals(doc.title(), clone.title());
        assertEquals(doc.baseUri(), clone.baseUri());
        assertEquals(doc.quirksMode(), clone.quirksMode());
        assertEquals(doc.outputSettings().indentAmount(), clone.outputSettings().indentAmount());
        assertEquals(doc.outputSettings().prettyPrint(), clone.outputSettings().prettyPrint());
        assertEquals(doc.outputSettings().syntax(), clone.outputSettings().syntax());

        clone.title("Modified");
        assertEquals("Original", doc.title());
        assertEquals("Modified", clone.title());
    }

    @Test
    public void testShallowClone() {
        Document doc = Document.createShell("http://example.com/");
        Document shallow = doc.shallowClone();
        assertNotSame(doc, shallow);
        assertEquals(0, shallow.children().size());
        assertEquals("http://example.com/", shallow.baseUri());
    }

    @Test
    public void testQuirksMode() {
        Document doc = new Document("");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());

        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());
    }

    @Test
    public void testParserGetterAndSetter() {
        Document doc = new Document("");
        assertNotNull(doc.parser());

        Parser htmlParser = Parser.htmlParser();
        doc.parser(htmlParser);
        assertSame(htmlParser, doc.parser());

        Parser xmlParser = Parser.xmlParser();
        doc.parser(xmlParser);
        assertSame(xmlParser, doc.parser());
    }

    @Test
    public void testOuterHtml() {
        Document doc = Document.createShell("");
        doc.title("Hello");
        doc.body().appendElement("p").text("World");

        String html = doc.outerHtml();
        assertTrue(html.contains("<title>Hello</title>"));
        assertTrue(html.contains("<p>World</p>"));
    }

    @Test
    public void testTextSetter() {
        Document doc = Document.createShell("");
        doc.text("Replacement Text");
        assertEquals("Replacement Text", doc.body().text());
    }

    @Test
    public void testOutputSettings() {
        OutputSettings settings = new OutputSettings();
        
        assertSame(settings, settings.charset(StandardCharsets.UTF_8));
        assertEquals(StandardCharsets.UTF_8, settings.charset());

        assertSame(settings, settings.charset("US-ASCII"));
        assertEquals(StandardCharsets.US_ASCII, settings.charset());

        assertSame(settings, settings.escapeMode(EscapeMode.extended));
        assertEquals(EscapeMode.extended, settings.escapeMode());

        assertSame(settings, settings.syntax(Syntax.xml));
        assertEquals(Syntax.xml, settings.syntax());

        assertSame(settings, settings.prettyPrint(false));
        assertFalse(settings.prettyPrint());

        assertSame(settings, settings.outline(true));
        assertTrue(settings.outline());

        assertSame(settings, settings.indentAmount(8));
        assertEquals(8, settings.indentAmount());

        try {
            settings.indentAmount(-1);
            fail("Should throw IllegalArgumentException on negative indent");
        } catch (IllegalArgumentException expected) {
            // Success
        }

        assertSame(settings, settings.maxPaddingWidth(40));
        assertEquals(40, settings.maxPaddingWidth());

        try {
            settings.maxPaddingWidth(-2);
            fail("Should throw IllegalArgumentException on negative max padding");
        } catch (IllegalArgumentException expected) {
            // Success
        }

        OutputSettings clone = settings.clone();
        assertNotSame(settings, clone);
        assertEquals(settings.charset(), clone.charset());
        assertEquals(settings.escapeMode(), clone.escapeMode());
        assertEquals(settings.syntax(), clone.syntax());
        assertEquals(settings.prettyPrint(), clone.prettyPrint());
        assertEquals(settings.outline(), clone.outline());
        assertEquals(settings.indentAmount(), clone.indentAmount());
        assertEquals(settings.maxPaddingWidth(), clone.maxPaddingWidth());
    }

    @Test
    public void testHtmlOutputFormatting() {
        Document doc = Jsoup.parse("<div><p>Hello   \n  <span>world</span></p></div>");
        doc.outputSettings().prettyPrint(true);
        String formatted = doc.body().html();
        assertTrue(formatted.contains("<div>"));
        assertTrue(formatted.contains("<p>Hello <span>world</span></p>"));
    }

    @Test
    public void testXmlOutputFormatting() {
        Document doc = Document.createShell("");
        doc.outputSettings().syntax(Syntax.xml);
        doc.body().appendElement("img").attr("src", "image.png");

        String xml = doc.body().html();
        assertTrue(xml.contains("<img src=\"image.png\" />") || xml.contains("<img src=\"image.png\"/>"));
    }
}