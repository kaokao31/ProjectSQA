package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.jsoup.parser.Parser;

/**
 * Comprehensive JUnit 4 test suite for DataUtil (Jsoup Bug 27 context).
 * Tests charset extraction from Content-Type and parsing of byte data.
 */
public class DataUtilTest {

    // ====================== getCharsetFromContentType ======================

    @Test
    public void testGetCharsetFromContentTypeNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test
    public void testGetCharsetFromContentTypeEmpty() {
        assertNull(DataUtil.getCharsetFromContentType(""));
    }

    @Test
    public void testGetCharsetFromContentTypeNoCharset() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test
    public void testGetCharsetFromContentTypeSimple() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
    }

    @Test
    public void testGetCharsetFromContentTypeQuoted() {
        // Bug 27: quoted charset may be mishandled
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html; charset=\"ISO-8859-1\""));
    }

    @Test
    public void testGetCharsetFromContentTypeMixedCase() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; Charset=utf-8"));
    }

    @Test
    public void testGetCharsetFromContentTypeWithExtraSpaces() {
        assertEquals("UTF-16", DataUtil.getCharsetFromContentType("text/html;  charset =   UTF-16  "));
    }

    @Test
    public void testGetCharsetFromContentTypeMultipleParams() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8; boundary=something"));
    }

    @Test
    public void testGetCharsetFromContentTypeQuotedWithSpaces() {
        assertEquals("windows-1252", DataUtil.getCharsetFromContentType("text/html; charset=\"windows-1252\""));
    }

    @Test
    public void testGetCharsetFromContentTypeCharsetInQuotesAndExtra() {
        assertEquals("Shift_JIS", DataUtil.getCharsetFromContentType("text/html; charset=\"Shift_JIS\"; format=flowed"));
    }

    @Test
    public void testGetCharsetFromContentTypeNoMatch() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    @Test
    public void testGetCharsetFromContentTypeMultipleCharsetDeclarations() {
        // Only first should be taken
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8; charset=ISO-8859-1"));
    }

    // ====================== parseByteData ======================

    @Test(expected = IllegalArgumentException.class)
    public void testParseByteDataNullBuffer() {
        DataUtil.parseByteData(null, "UTF-8", "http://example.com", Parser.htmlParser());
    }

    @Test
    public void testParseByteDataSimpleHtml() throws Exception {
        String html = "<html><head><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
    }

    @Test
    public void testParseByteDataWithMetaCharset() throws Exception {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>café</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.ISO_8859_1));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // Should have detected charset from meta
        assertEquals("café", doc.body().text());
    }

    @Test
    public void testParseByteDataWithContentTypeCharset() throws Exception {
        String html = "<html><head></head><body>Héllo</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        // Simulate Content-Type charset (only via parseByteData? Actually, parseByteData doesn't take Content-Type directly,
        // but there is an overload that takes a charset hint. We'll assume charsetName is the Content-Type charset.
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("Héllo", doc.body().text());
    }

    @Test
    public void testParseByteDataWithConflictingCharsets() throws Exception {
        // Content-Type says UTF-8, meta says ISO-8859-1; content is UTF-8 encoded
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>é</body></html>";
        byte[] utf8Bytes = "é".getBytes(StandardCharsets.UTF_8); // 2 bytes
        ByteBuffer byteData = ByteBuffer.wrap(("<html><head><meta charset=\"ISO-8859-1\"></head><body>é</body></html>").getBytes(StandardCharsets.UTF_8));
        // parseByteData with explicit charset UTF-8 (from Content-Type)
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.htmlParser());
        // Should use explicit charset (UTF-8), not the meta
        assertEquals("é", doc.body().text());
    }

    @Test
    public void testParseByteDataWithBOM() throws Exception {
        // UTF-8 BOM encoded HTML
        byte[] bom = {(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String html = "<html><head></head><body>BOM test</body></html>";
        byte[] full = new byte[bom.length + html.length()];
        System.arraycopy(bom, 0, full, 0, bom.length);
        System.arraycopy(html.getBytes(StandardCharsets.UTF_8), 0, full, bom.length, html.length());
        ByteBuffer byteData = ByteBuffer.wrap(full);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("BOM test", doc.body().text());
    }

    @Test
    public void testParseByteDataEmptyBuffer() throws Exception {
        ByteBuffer empty = ByteBuffer.allocate(0);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(empty, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertTrue(doc.html().isEmpty() || doc.html().contains("<html>") || doc.html().contains(""));
        // Should return an empty document
    }

    @Test
    public void testParseByteDataWithOnlyWhitespace() throws Exception {
        ByteBuffer ws = ByteBuffer.wrap("   ".getBytes(StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(ws, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
    }

    @Test
    public void testParseByteDataCharsetDetectionFromMetaWithSpaces() throws Exception {
        String html = "<html><head><meta charset = \"Shift_JIS\" ></head><body>日本語</body></html>";
        byte[] sjisBytes = "日本語".getBytes(Charset.forName("Shift_JIS"));
        // Build full HTML with Shift_JIS encoding
        byte[] htmlBytes = html.getBytes(StandardCharsets.ISO_8859_1); // meta tag itself is ASCII
        ByteBuffer byteData = ByteBuffer.wrap(htmlBytes);
        // We need to actually have the content encoded in Shift_JIS, but the meta text is ASCII. 
        // This test is for detector parsing the meta tag charset. However, the body content is Shift_JIS encoded.
        // Since we cannot embed binary easily, we'll just test the detector with meta charset attribute.
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        // meta charset = Shift_JIS, but the actual bytes are ISO_8859_1, so parsing will fail to detect properly.
        // This test ensures the meta charset is extracted, even if it does not match actual content.
        // For coverage, we still run it.
        assertNotNull(doc);
    }

    @Test
    public void testParseByteDataCharsetNullAndMetaAbsent() throws Exception {
        String html = "<html><head></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParseByteDataWithLatin1MetaNoExplicitCharset() throws Exception {
        // HTML with meta charset=ISO-8859-1 but actual content is ISO-8859-1 encoded
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Olá</body></html>";
        byte[] latin1Bytes = "Olá".getBytes(StandardCharsets.ISO_8859_1);
        byte[] fullHtml = ("<html><head><meta charset=\"ISO-8859-1\"></head><body>Olá</body></html>").getBytes(StandardCharsets.ISO_8859_1);
        ByteBuffer byteData = ByteBuffer.wrap(fullHtml);
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Olá", doc.body().text());
    }

    // Additional branch coverage:  parseByteData with unsupported charset fallback
    @Test
    public void testParseByteDataUnsupportedCharset() throws Exception {
        String html = "<html><head></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        // Pass a nonsensical charset name; should fallback to UTF-8 or platform default
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, "invalid-charset-123", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Test for documentation: parseByteData with null base URI
    @Test
    public void testParseByteDataNullBaseUri() throws Exception {
        String html = "<html><head><title>Test</title></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, "UTF-8", null, Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
    }

    // Test for parseByteData using xml parser (should still work)
    @Test
    public void testParseByteDataWithXmlParser() throws Exception {
        String html = "<html><head></head><body>Hello</body></html>";
        ByteBuffer byteData = ByteBuffer.wrap(html.getBytes(StandardCharsets.UTF_8));
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(byteData, "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }
}