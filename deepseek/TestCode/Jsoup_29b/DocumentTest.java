package org.jsoup.nodes;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for org.jsoup.nodes.Document.
 * Designed to achieve high coverage and detect faults (especially bug Jsoup-29).
 */
public class DocumentTest {

    private Document doc;

    @Before
    public void setUp() {
        // Default document with head and title for common tests
        doc = Jsoup.parse("<html><head><title>Original</title></head><body></body></html>");
    }

    // ==================== title() and title(String) ====================

    @Test
    public void testTitleNoHead() {
        // Document without head element
        Document noHeadDoc = Jsoup.parse("<html><body>test</body></html>");
        assertEquals("", noHeadDoc.title());
        // Setting title should create head and title elements
        noHeadDoc.title("New Title");
        assertEquals("New Title", noHeadDoc.title());
        assertNotNull(noHeadDoc.head());
        assertNotNull(noHeadDoc.head().selectFirst("title"));
    }

    @Test
    public void testTitleHeadNoTitle() {
        // Document with head but no title
        Document noTitleDoc = Jsoup.parse("<html><head></head><body></body></html>");
        assertEquals("", noTitleDoc.title());
        noTitleDoc.title("Added Title");
        assertEquals("Added Title", noTitleDoc.title());
        assertNotNull(noTitleDoc.head().selectFirst("title"));
    }

    @Test
    public void testTitleWithTitle() {
        // Document with one title element
        assertEquals("Original", doc.title());
        doc.title("Updated");
        assertEquals("Updated", doc.title());
        // Ensure only one title element exists
        assertEquals(1, doc.head().select("title").size());
    }

    @Test
    public void testTitleMultipleTitles() {
        // Document with multiple title elements
        Document multiTitleDoc = Jsoup.parse(
                "<html><head><title>First</title><title>Second</title></head><body></body></html>");
        assertEquals("First", multiTitleDoc.title());
        multiTitleDoc.title("Updated");
        assertEquals("Updated", multiTitleDoc.title());
        // First title should be updated, second should remain
        assertEquals("Updated", multiTitleDoc.head().select("title").first().text());
        assertEquals("Second", multiTitleDoc.head().select("title").last().text());
    }

    @Test
    public void testTitleWithEntities() {
        // Title text containing HTML entities
        Document entityDoc = Jsoup.parse(
                "<html><head><title>Hello &amp; &lt;World&gt;</title></head><body></body></html>");
        assertEquals("Hello & <World>", entityDoc.title());
        // Setting title with entities should store decoded text
        entityDoc.title("A & B");
        assertEquals("A & B", entityDoc.title());
    }

    @Test
    public void testTitleSetThenGet() {
        // Verify that setting title then getting returns the same text
        doc.title("Test Title");
        assertEquals("Test Title", doc.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTitleNull() {
        // Setting title to null should throw IllegalArgumentException
        doc.title(null);
    }

    // ==================== outputSettings() ====================

    @Test
    public void testOutputSettings() {
        Document.OutputSettings settings = doc.outputSettings();
        assertNotNull(settings);
        // Default settings
        assertTrue(settings.prettyPrint());
        assertEquals(Document.OutputSettings.Encoder.escapeMode("base"), settings.escapeMode());
    }

    @Test
    public void testOutputSettingsModify() {
        Document.OutputSettings settings = doc.outputSettings();
        settings.prettyPrint(false);
        assertFalse(doc.outputSettings().prettyPrint());
    }

    // ==================== createElement() ====================

    @Test
    public void testCreateElement() {
        Element div = doc.createElement("div");
        assertEquals("div", div.tagName());
        assertTrue(div instanceof Element);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateElementNullTag() {
        doc.createElement(null);
    }

    // ==================== charset() ====================

    @Test
    public void testCharsetDefault() {
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testCharsetSet() {
        doc.charset(java.nio.charset.Charset.forName("ISO-8859-1"));
        assertEquals("ISO-8859-1", doc.charset().name());
    }

    // ==================== updateMetaCharsetElement() ====================

    @Test
    public void testUpdateMetaCharsetElement() {
        assertFalse(doc.updateMetaCharsetElement());
        doc.updateMetaCharsetElement(true);
        assertTrue(doc.updateMetaCharsetElement());
    }

    // ==================== clone() ====================

    @Test
    public void testClone() {
        Document clone = doc.clone();
        assertNotNull(clone);
        assertNotSame(doc, clone);
        assertEquals(doc.title(), clone.title());
        assertEquals(doc.outputSettings().prettyPrint(), clone.outputSettings().prettyPrint());
    }

    // ==================== shallowCopy() ====================

    @Test
    public void testShallowCopy() {
        // shallowCopy is protected, but we can test via clone which calls it
        Document clone = doc.clone();
        // Ensure shallow copy of child nodes is not shared
        doc.title("Changed");
        assertEquals("Original", clone.title()); // clone should be independent
    }

    // ==================== location() ====================

    @Test
    public void testLocation() {
        assertEquals("", doc.location());
        Document parsedWithBase = Jsoup.parse("<html><head><base href='http://example.com'></head><body></body></html>");
        // location is empty for parsed documents without base URI
        assertEquals("", parsedWithBase.location());
    }

    // ==================== head() ====================

    @Test
    public void testHead() {
        Element head = doc.head();
        assertNotNull(head);
        assertEquals("head", head.tagName());
    }

    @Test
    public void testHeadNoHead() {
        Document noHeadDoc = Jsoup.parse("<html><body></body></html>");
        // head() should create a head element if missing
        assertNotNull(noHeadDoc.head());
        assertEquals("head", noHeadDoc.head().tagName());
    }

    // ==================== body() ====================

    @Test
    public void testBody() {
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("body", body.tagName());
    }

    @Test
    public void testBodyNoBody() {
        Document noBodyDoc = Jsoup.parse("<html><head></head></html>");
        // body() should create a body element if missing
        assertNotNull(noBodyDoc.body());
        assertEquals("body", noBodyDoc.body().tagName());
    }

    // ==================== children() ====================

    @Test
    public void testChildrenSize() {
        // Document has two children: head and body
        assertEquals(2, doc.children().size());
    }

    // ==================== text() ====================

    @Test
    public void testText() {
        // Document text should be the combined text of all elements
        assertEquals("Original", doc.text());
    }

    // ==================== data() ====================

    @Test
    public void testData() {
        // No data nodes by default
        assertEquals("", doc.data());
    }

    // ==================== equals() ====================

    @Test
    public void testEquals() {
        Document sameDoc = Jsoup.parse("<html><head><title>Original</title></head><body></body></html>");
        // Documents with same content are not necessarily equal because of object identity
        assertFalse(doc.equals(sameDoc));
        assertTrue(doc.equals(doc));
    }

    // ==================== hashCode() ====================

    @Test
    public void testHashCode() {
        int hash = doc.hashCode();
        assertTrue(hash != 0);
    }

    // ==================== toString() ====================

    @Test
    public void testToString() {
        String str = doc.toString();
        assertTrue(str.contains("<html>"));
        assertTrue(str.contains("<head>"));
        assertTrue(str.contains("<title>Original</title>"));
    }

    // ==================== outerHtml() ====================

    @Test
    public void testOuterHtml() {
        String outer = doc.outerHtml();
        assertTrue(outer.startsWith("<html>"));
        assertTrue(outer.endsWith("</html>"));
    }

    // ==================== quirksMode() ====================

    @Test
    public void testQuirksMode() {
        assertEquals(Document.QuirksMode.noQuirks, doc.quirksMode());
        doc.quirksMode(Document.QuirksMode.quirks);
        assertEquals(Document.QuirksMode.quirks, doc.quirksMode());
    }

    // ==================== nodeName() ====================

    @Test
    public void testNodeName() {
        assertEquals("#document", doc.nodeName());
    }

    // ==================== attr() ====================

    @Test
    public void testAttr() {
        // Document has no attributes by default
        assertTrue(doc.attributes().isEmpty());
    }

    // ==================== baseUri() ====================

    @Test
    public void testBaseUri() {
        assertEquals("", doc.baseUri());
        Document docWithBase = Jsoup.parse("<html></html>", "http://example.com");
        assertEquals("http://example.com", docWithBase.baseUri());
    }

    // ==================== updateMetaCharset() ====================

    @Test
    public void testUpdateMetaCharset() {
        // This method is called internally; we can test by setting charset
        doc.charset(java.nio.charset.Charset.forName("ISO-8859-1"));
        // After setting charset, updateMetaCharsetElement should be true
        assertTrue(doc.updateMetaCharsetElement());
    }

    // ==================== parser() ====================

    @Test
    public void testParser() {
        Parser parser = doc.parser();
        assertNotNull(parser);
        // Default parser is HtmlTreeBuilder
        assertTrue(parser instanceof org.jsoup.parser.HtmlTreeBuilder);
    }

    // ==================== connection() ====================

    @Test
    public void testConnection() {
        // Connection is null for parsed documents
        assertNull(doc.connection());
    }
}