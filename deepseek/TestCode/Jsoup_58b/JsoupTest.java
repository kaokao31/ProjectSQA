package org.jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for the Jsoup class.
 * Designed to achieve maximum line and branch coverage and to trigger
 * potential faults (including Defects4J bug 58).
 */
public class JsoupTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String validHtml;
    private String malformedHtml;
    private String emptyHtml;
    private String nullHtml;
    private String htmlWithBodyOnly;
    private String htmlWithUnclosedTags;
    private String htmlWithSpecialChars;

    @Before
    public void setUp() {
        validHtml = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        malformedHtml = "<html><head><title>Test</title></head><body><p>Hello</body></html>";
        emptyHtml = "";
        nullHtml = null;
        htmlWithBodyOnly = "<body></body>";
        htmlWithUnclosedTags = "<div><span>text";
        htmlWithSpecialChars = "<p>&amp;&lt;&gt;&quot;</p>";
    }

    // ==================== parse(String html) ====================

    @Test
    public void testParseValidHtml() {
        Document doc = Jsoup.parse(validHtml);
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParseEmptyHtml() {
        Document doc = Jsoup.parse(emptyHtml);
        assertNotNull(doc);
        assertTrue(doc.children().isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNullHtml() {
        Jsoup.parse(nullHtml);
    }

    @Test
    public void testParseMalformedHtml() {
        Document doc = Jsoup.parse(malformedHtml);
        assertNotNull(doc);
        // Should still produce a document, possibly with corrected structure
        assertNotNull(doc.body());
    }

    @Test
    public void testParseHtmlWithBodyOnly() {
        Document doc = Jsoup.parse(htmlWithBodyOnly);
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        assertTrue(body.children().isEmpty());
    }

    @Test
    public void testParseHtmlWithUnclosedTags() {
        Document doc = Jsoup.parse(htmlWithUnclosedTags);
        assertNotNull(doc);
        // Should auto-close tags
        assertEquals("text", doc.text());
    }

    @Test
    public void testParseHtmlWithSpecialChars() {
        Document doc = Jsoup.parse(htmlWithSpecialChars);
        assertNotNull(doc);
        assertEquals("&<>\"", doc.text());
    }

    // ==================== parse(String html, String baseUri) ====================

    @Test
    public void testParseWithBaseUri() {
        String baseUri = "http://example.com";
        Document doc = Jsoup.parse(validHtml, baseUri);
        assertNotNull(doc);
        assertEquals(baseUri, doc.baseUri());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNullHtmlAndBaseUri() {
        Jsoup.parse(nullHtml, "http://example.com");
    }

    @Test
    public void testParseWithNullBaseUri() {
        Document doc = Jsoup.parse(validHtml, null);
        assertNotNull(doc);
        assertNull(doc.baseUri());
    }

    @Test
    public void testParseWithEmptyBaseUri() {
        Document doc = Jsoup.parse(validHtml, "");
        assertNotNull(doc);
        assertEquals("", doc.baseUri());
    }

    // ==================== parse(File in, String charsetName, String baseUri) ====================

    @Test
    public void testParseFile() throws IOException {
        File file = tempFolder.newFile("test.html");
        java.nio.file.Files.write(file.toPath(), validHtml.getBytes(StandardCharsets.UTF_8));
        Document doc = Jsoup.parse(file, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullFile() throws IOException {
        Jsoup.parse((File) null, "UTF-8", "http://example.com");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseFileNullCharset() throws IOException {
        File file = tempFolder.newFile("test.html");
        java.nio.file.Files.write(file.toPath(), validHtml.getBytes(StandardCharsets.UTF_8));
        Jsoup.parse(file, null, "http://example.com");
    }

    @Test(expected = IOException.class)
    public void testParseFileNonExistent() throws IOException {
        File nonExistent = new File(tempFolder.getRoot(), "nonexistent.html");
        Jsoup.parse(nonExistent, "UTF-8", "http://example.com");
    }

    // ==================== parse(URL url, int timeoutMillis) ====================

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNull() throws IOException {
        Jsoup.parse((URL) null, 3000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseUrlNegativeTimeout() throws IOException {
        Jsoup.parse(new URL("http://example.com"), -1);
    }

    // Note: Actual network test is not feasible in unit test; we rely on exception handling.

    // ==================== clean(String bodyHtml, Whitelist whitelist) ====================

    @Test
    public void testCleanValidHtml() {
        String clean = Jsoup.clean("<p>Hello</p>", Whitelist.basic());
        assertNotNull(clean);
        assertTrue(clean.contains("Hello"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCleanNullBodyHtml() {
        Jsoup.clean(nullHtml, Whitelist.basic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCleanNullWhitelist() {
        Jsoup.clean("<p>Hello</p>", null);
    }

    @Test
    public void testCleanEmptyHtml() {
        String clean = Jsoup.clean("", Whitelist.basic());
        assertEquals("", clean);
    }

    @Test
    public void testCleanWithUnsafeTags() {
        String clean = Jsoup.clean("<script>alert('xss')</script><p>safe</p>", Whitelist.basic());
        assertFalse(clean.contains("script"));
        assertTrue(clean.contains("safe"));
    }

    // ==================== isValid(String bodyHtml, Whitelist whitelist) ====================

    @Test
    public void testIsValidTrue() {
        assertTrue(Jsoup.isValid("<p>Hello</p>", Whitelist.basic()));
    }

    @Test
    public void testIsValidFalse() {
        assertFalse(Jsoup.isValid("<script>alert('xss')</script>", Whitelist.basic()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidNullBodyHtml() {
        Jsoup.isValid(nullHtml, Whitelist.basic());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsValidNullWhitelist() {
        Jsoup.isValid("<p>Hello</p>", null);
    }

    // ==================== serialize(Document doc) ====================

    @Test
    public void testSerializeDocument() {
        Document doc = Jsoup.parse(validHtml);
        String serialized = Jsoup.serialize(doc);
        assertNotNull(serialized);
        assertTrue(serialized.contains("<html>"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSerializeNullDocument() {
        Jsoup.serialize(null);
    }

    // ==================== Additional edge cases for coverage ====================

    @Test
    public void testParseHtmlWithDoctype() {
        String html = "<!DOCTYPE html><html><body><p>Test</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertNotNull(doc.documentType());
    }

    @Test
    public void testParseHtmlWithComments() {
        String html = "<html><!-- comment --><body><p>Text</p></body></html>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Text", doc.body().text());
    }

    @Test
    public void testParseHtmlWithMultipleRoots() {
        String html = "<p>First</p><p>Second</p>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals(2, doc.children().size());
    }

    @Test
    public void testParseHtmlWithWhitespaceOnly() {
        String html = "   \n\t  ";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertTrue(doc.text().isEmpty());
    }

    @Test
    public void testParseHtmlWithSelfClosingTags() {
        String html = "<br><hr><img src='test.jpg'>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals(3, doc.body().children().size());
    }

    @Test
    public void testParseHtmlWithNestedTables() {
        String html = "<table><tr><td><table><tr><td>Nested</td></tr></table></td></tr></table>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertEquals("Nested", doc.text());
    }

    // ==================== Tests targeting potential bug (Defects4J bug 58) ====================
    // Bug 58: NullPointerException when parsing HTML with <body> tag that has no content
    // and then calling body() method. This test ensures that body() does not throw NPE.

    @Test
    public void testParseBodyOnlyNoContent() {
        String html = "<body></body>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        assertTrue(body.children().isEmpty());
    }

    @Test
    public void testParseBodyWithOnlyWhitespace() {
        String html = "<body>   </body>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        assertTrue(body.text().isEmpty());
    }

    @Test
    public void testParseBodyWithNoClosingTag() {
        String html = "<body><p>Test";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        Element body = doc.body();
        assertNotNull(body);
        assertEquals("Test", body.text());
    }

    @Test
    public void testParseHtmlWithBodyAndNoHead() {
        String html = "<body><p>Content</p></body>";
        Document doc = Jsoup.parse(html);
        assertNotNull(doc);
        assertNotNull(doc.head());
        assertNotNull(doc.body());
        assertEquals("Content", doc.body().text());
    }

    // ==================== Tests for parse with baseUri and relative links ====================

    @Test
    public void testParseWithBaseUriAndRelativeLinks() {
        String html = "<a href='/page'>Link</a>";
        Document doc = Jsoup.parse(html, "http://example.com");
        assertNotNull(doc);
        Element link = doc.select("a").first();
        assertEquals("http://example.com/page", link.absUrl("href"));
    }

    // ==================== Tests for clean with different whitelists ====================

    @Test
    public void testCleanWithRelaxedWhitelist() {
        String html = "<p><b>Bold</b> and <i>italic</i></p>";
        String clean = Jsoup.clean(html, Whitelist.relaxed());
        assertTrue(clean.contains("<b>"));
        assertTrue(clean.contains("<i>"));
    }

    @Test
    public void testCleanWithNoneWhitelist() {
        String html = "<p>Text</p>";
        String clean = Jsoup.clean(html, Whitelist.none());
        assertEquals("Text", clean);
    }

    @Test
    public void testCleanWithSimpleTextWhitelist() {
        String html = "<p>Text</p>";
        String clean = Jsoup.clean(html, Whitelist.simpleText());
        assertEquals("Text", clean);
    }

    // ==================== Tests for isValid with edge cases ====================

    @Test
    public void testIsValidWithEmptyHtml() {
        assertTrue(Jsoup.isValid("", Whitelist.basic()));
    }

    @Test
    public void testIsValidWithOnlyText() {
        assertTrue(Jsoup.isValid("Just text", Whitelist.basic()));
    }

    // ==================== Tests for serialize with different documents ====================

    @Test
    public void testSerializeEmptyDocument() {
        Document doc = Jsoup.parse("");
        String serialized = Jsoup.serialize(doc);
        assertNotNull(serialized);
        // Should produce minimal HTML structure
        assertTrue(serialized.contains("<html>"));
    }

    @Test
    public void testSerializeDocumentWithDoctype() {
        String html = "<!DOCTYPE html><html><body></body></html>";
        Document doc = Jsoup.parse(html);
        String serialized = Jsoup.serialize(doc);
        assertTrue(serialized.startsWith("<!DOCTYPE html>"));
    }
}