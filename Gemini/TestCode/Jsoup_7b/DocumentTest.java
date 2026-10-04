package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

import static org.junit.Assert.*;

public class DocumentTest {

    @Test
    public void testDocumentCreationAndShell() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc);
        assertEquals("http://example.com", doc.location());
        
        // Test basic structure created by shell
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("#document", doc.nodeName());
        
        // Test title functionality
        assertEquals("", doc.title());
        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
        assertEquals("Test Title", doc.head().getElementsByTag("title").text());

        // Setting title when title tag already exists
        doc.title("New Title");
        assertEquals("New Title", doc.title());
        assertEquals(1, doc.head().getElementsByTag("title").size());
    }

    @Test
    public void testDocumentOutputSettings() {
        Document doc = new Document("http://example.com");
        assertNotNull(doc.outputSettings());
        
        Document.OutputSettings settings = new Document.OutputSettings();
        doc.outputSettings(settings);
        assertSame(settings, doc.outputSettings());
        
        assertEquals(Document.OutputSettings.Syntax.html, doc.syntax());
        doc.syntax(Document.OutputSettings.Syntax.xml);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.syntax());
    }

    @Test
    public void testDocumentQuirksMode() {
        Document doc = new Document("http://example.com");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        
        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
        
        doc.quirksMode(Document.QuirksMode.limitedQuirks);
        assertEquals(Document.QuirksMode.limitedQuirks, doc.quirksMode());
    }

    @Test
    public void testNodeNameAndOuterHtml() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
        
        // Empty document outerHtml should contain basic structure or be empty depending on implementation, 
        // but let's test with elements.
        doc.body().append("<p>Hello</p>");
        String html = doc.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head></head>"));
        assertTrue(html.contains("<body>"));
        assertTrue(html.contains("<p>Hello</p>"));
        assertTrue(html.contains("</html>"));
    }

    @Test
    public void testNormalise() {
        Document doc = Jsoup.parse("<div><span></span></div>");
        // Document.normalise() ensures structural integrity (html, head, body)
        Document normalized = doc.normalise();
        assertSame(doc, normalized);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testClone() {
        Document doc = new Document("http://example.com");
        doc.title("Original");
        doc.body().append("<p>Text</p>");

        Document clone = doc.clone();
        assertNotSame(doc, clone);
        assertEquals(doc.location(), clone.location());
        assertEquals(doc.title(), clone.title());
        assertEquals(doc.html(), clone.html());
        
        // Modify clone and verify original is unaffected
        clone.title("Cloned");
        assertEquals("Original", doc.title());
        assertEquals("Cloned", clone.title());
    }

    @Test
    public void testConnection() {
        Document doc = new Document("http://example.com");
        assertNull(doc.connection());
        
        // Connection can be set if Connection object is available, 
        // but we can at least verify getter/setter if exposed or test null safety.
        // Jsoup Document has connection(Connection) method.
        org.jsoup.Connection conn = Jsoup.connect("http://example.com");
        doc.connection(conn);
        assertSame(conn, doc.connection());
    }
}