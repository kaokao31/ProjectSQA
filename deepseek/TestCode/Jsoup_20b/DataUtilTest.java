package org.jsoup.helper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class DataUtilTest {

    // Helper to create a byte array from a string with a given charset
    private byte[] bytes(String s, Charset charset) {
        return s.getBytes(charset);
    }

    // Helper to create an InputStream from a byte array
    private InputStream stream(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    // Test parseByteData with null byte array
    @Test(expected = IllegalArgumentException.class)
    public void parseByteDataNullBytes() throws IOException {
        DataUtil.parseByteData(null, "http://example.com", "", Parser.htmlParser());
    }

    // Test parseByteData with empty byte array
    @Test
    public void parseByteDataEmptyBytes() throws IOException {
        byte[] empty = new byte[0];
        Document doc = DataUtil.parseByteData(empty, "http://example.com", "", Parser.htmlParser());
        assertNotNull(doc);
        assertTrue(doc.html().isEmpty());
    }

    // Test parseByteData with a simple HTML and no charset specified
    @Test
    public void parseByteDataNoCharset() throws IOException {
        String html = "<html><head></head><body>Hello</body></html>";
        byte[] data = bytes(html, StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Test parseByteData with charset in meta tag (bug scenario)
    @Test
    public void parseByteDataWithMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Héllo</body></html>";
        byte[] data = bytes(html, StandardCharsets.ISO_8859_1);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Héllo", doc.body().text());
        // Verify the document's charset is correctly set
        assertTrue(doc.charset().name().equalsIgnoreCase("ISO-8859-1") ||
                   doc.charset().name().equalsIgnoreCase("windows-1252")); // fallback
    }

    // Test parseByteData with charset in content-type header
    @Test
    public void parseByteDataWithContentTypeCharset() throws IOException {
        String html = "<html><head></head><body>Hello</body></html>";
        byte[] data = bytes(html, StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "text/html; charset=UTF-8", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
        assertEquals("UTF-8", doc.charset().name());
    }

    // Test parseByteData with conflicting charsets: content-type vs meta
    @Test
    public void parseByteDataConflictingCharsets() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Héllo</body></html>";
        byte[] data = bytes(html, StandardCharsets.ISO_8859_1);
        // Content-type says UTF-8, but meta says ISO-8859-1. Content-type should win.
        Document doc = DataUtil.parseByteData(data, "http://example.com", "text/html; charset=UTF-8", Parser.htmlParser());
        assertNotNull(doc);
        // The document should be parsed as UTF-8, but the bytes are ISO-8859-1, so text may be garbled.
        // We just check that it doesn't throw and returns something.
        assertNotNull(doc.body().text());
    }

    // Test parseByteData with BOM (UTF-8 BOM)
    @Test
    public void parseByteDataWithBOM() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String html = "<html><head></head><body>Hello</body></html>";
        byte[] data = new byte[bom.length + html.getBytes(StandardCharsets.UTF_8).length];
        System.arraycopy(bom, 0, data, 0, bom.length);
        System.arraycopy(html.getBytes(StandardCharsets.UTF_8), 0, data, bom.length, html.getBytes(StandardCharsets.UTF_8).length);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Test parseByteData with unsupported charset in meta
    @Test
    public void parseByteDataUnsupportedMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"UNSUPPORTED\"></head><body>Hello</body></html>";
        byte[] data = bytes(html, StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "", Parser.htmlParser());
        assertNotNull(doc);
        // Should fallback to UTF-8 or default
        assertEquals("Hello", doc.body().text());
    }

    // Test parseByteData with null charset name in content-type
    @Test
    public void parseByteDataNullContentTypeCharset() throws IOException {
        String html = "<html><head></head><body>Hello</body></html>";
        byte[] data = bytes(html, StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "text/html", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    // Test detectCharset with null ByteBuffer
    @Test(expected = IllegalArgumentException.class)
    public void detectCharsetNullBuffer() {
        DataUtil.detectCharset(null);
    }

    // Test detectCharset with empty buffer
    @Test
    public void detectCharsetEmptyBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(0);
        String charset = DataUtil.detectCharset(buffer);
        assertNull(charset);
    }

    // Test detectCharset with a buffer containing a valid charset declaration
    @Test
    public void detectCharsetValid() {
        String html = "<html><head><meta charset=\"UTF-8\"></head></html>";
        ByteBuffer buffer = ByteBuffer.wrap(bytes(html, StandardCharsets.UTF_8));
        String charset = DataUtil.detectCharset(buffer);
        assertEquals("UTF-8", charset);
    }

    // Test detectCharset with a buffer containing no charset declaration
    @Test
    public void detectCharsetNoDeclaration() {
        String html = "<html><head></head><body>Hello</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(bytes(html, StandardCharsets.UTF_8));
        String charset = DataUtil.detectCharset(buffer);
        assertNull(charset);
    }

    // Test detectCharset with a buffer containing a charset in a meta http-equiv
    @Test
    public void detectCharsetHttpEquiv() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head></html>";
        ByteBuffer buffer = ByteBuffer.wrap(bytes(html, StandardCharsets.ISO_8859_1));
        String charset = DataUtil.detectCharset(buffer);
        assertEquals("ISO-8859-1", charset);
    }

    // Test detectCharset with a buffer containing an unsupported charset
    @Test
    public void detectCharsetUnsupported() {
        String html = "<html><head><meta charset=\"UNSUPPORTED\"></head></html>";
        ByteBuffer buffer = ByteBuffer.wrap(bytes(html, StandardCharsets.UTF_8));
        String charset = DataUtil.detectCharset(buffer);
        assertNull(charset);
    }

    // Test readInputStream with null stream
    @Test(expected = IllegalArgumentException.class)
    public void readInputStreamNull() throws IOException {
        DataUtil.readInputStream(null);
    }

    // Test readInputStream with empty stream
    @Test
    public void readInputStreamEmpty() throws IOException {
        InputStream empty = new ByteArrayInputStream(new byte[0]);
        ByteBuffer buffer = DataUtil.readInputStream(empty);
        assertNotNull(buffer);
        assertEquals(0, buffer.remaining());
    }

    // Test readInputStream with some data
    @Test
    public void readInputStreamWithData() throws IOException {
        byte[] data = "Hello".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        ByteBuffer buffer = DataUtil.readInputStream(in);
        assertNotNull(buffer);
        assertEquals(data.length, buffer.remaining());
        byte[] read = new byte[buffer.remaining()];
        buffer.get(read);
        assertArrayEquals(data, read);
    }

    // Test parseByteData with a large input to ensure no buffer issues
    @Test
    public void parseByteDataLargeInput() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head></head><body>");
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        sb.append("</body></html>");
        byte[] data = bytes(sb.toString(), StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "", Parser.htmlParser());
        assertNotNull(doc);
        assertTrue(doc.body().text().length() > 0);
    }

    // Test parseByteData with a document that has a BOM and a meta charset
    @Test
    public void parseByteDataBomAndMeta() throws IOException {
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Hello</body></html>";
        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[bom.length + htmlBytes.length];
        System.arraycopy(bom, 0, data, 0, bom.length);
        System.arraycopy(htmlBytes, 0, data, bom.length, htmlBytes.length);
        Document doc = DataUtil.parseByteData(data, "http://example.com", "", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
        assertEquals("UTF-8", doc.charset().name());
    }
}