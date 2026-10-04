package org.jsoup.nodes;

import org.junit.Test;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;

import static org.junit.Assert.*;

public class DocumentTest {

    @Test
    public void testDocumentCreationAndTitle() {
        Document doc = new Document("http://example.com");
        assertEquals("http://example.com", doc.location());
        
        // Test title when none set
        assertEquals("", doc.title());
        
        // Set title
        doc.title("New Title");
        assertEquals("New Title", doc.title());
        
        // Ensure head and body are created automatically
        assertNotNull(doc.head());
        assertNotNull(doc.body());
    }

    @Test
    public void testDocumentOutputSettings() {
        Document doc = new Document("http://example.com");
        assertNotNull(doc.outputSettings());
        
        // Modify output settings
        doc.outputSettings().prettyPrint(false);
        assertFalse(doc.outputSettings().prettyPrint());
        
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        assertEquals(Document.OutputSettings.Syntax.xml, doc.outputSettings().syntax());
    }

    @Test
    public void testDocumentQuirksMode() {
        Document doc = new Document("http://example.com");
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        
        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    @Test
    public void testCloneDocument() {
        Document doc = new Document("http://example.com");
        doc.title("Test Title");
        doc.append("<p>Hello</p>");

        Document clone = doc.clone();
        assertNotSame(doc, clone);
        assertEquals("Test Title", clone.title());
        assertEquals("http://example.com", clone.location());
        assertEquals(doc.html(), clone.html());
    }

    @Test
    public void testNodeNameAndOuterHtml() {
        Document doc = new Document("http://example.com");
        assertEquals("#document", doc.nodeName());
        
        // Outer HTML for empty doc
        String html = doc.outerHtml();
        assertTrue(html.contains("<html>"));
        assertTrue(html.contains("<head>"));
        assertTrue(html.contains("<body>"));
    }

    @Test
    public void testCreateShell() {
        Document doc = Document.createShell("http://example.com");
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("http://example.com", doc.location());
    }

    @Test
    public void testTextAndAppend() {
        Document doc = new Document("");
        doc.text("Hello World");
        assertEquals("Hello World", doc.text());
        assertEquals("Hello World", doc.body().text());
    }

    @Test
    public void testConnection() {
        Document doc = new Document("http://example.com");
        assertNull(doc.connection());
        
        org.jsoup.Connection conn = Jsoup.connect("http://example.com");
        doc.connection(conn);
        assertSame(conn, doc.connection());
    }
}