package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DataUtilTest {

    private static final String HTML = "<html><head><title>Test</title></head><body>Hello</body></html>";

    @Test
    public void emptyByteBufferWithNullCharsetShouldNotThrow() {
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(new byte[0]), null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void singleByteBufferWithNullCharsetShouldNotThrow() {
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(new byte[] { '<' }), null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
    }

    @Test
    public void twoByteBufferStartingWithUtf8BomPrefixShouldNotThrow() {
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(new byte[] { (byte) 0xEF, (byte) 0xBB }),
                null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
    }

    @Test
    public void parsesUtf8BomAndSetsCharset() {
        byte[] content = HTML.getBytes(Charset.forName("UTF-8"));
        ByteBuffer buf = ByteBuffer.allocate(3 + content.length);
        buf.put((byte) 0xEF).put((byte) 0xBB).put((byte) 0xBF).put(content);
        buf.flip();

        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void parsesUtf16BeBomAndSetsCharset() {
        byte[] content = HTML.getBytes(Charset.forName("UTF-16BE"));
        ByteBuffer buf = ByteBuffer.allocate(2 + content.length);
        buf.put((byte) 0xFE).put((byte) 0xFF).put(content);
        buf.flip();

        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("UTF-16BE", doc.outputSettings().charset().name());
    }

    @Test
    public void parsesUtf16LeBomAndSetsCharset() {
        byte[] content = HTML.getBytes(Charset.forName("UTF-16LE"));
        ByteBuffer buf = ByteBuffer.allocate(2 + content.length);
        buf.put((byte) 0xFF).put((byte) 0xFE).put(content);
        buf.flip();

        Document doc = DataUtil.parseByteData(buf, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("UTF-16LE", doc.outputSettings().charset().name());
    }

    @Test
    public void parseByteDataUsesDefaultCharsetWhenNoCharset() {
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(HTML.getBytes(Charset.forName("UTF-8"))),
                null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void parseByteDataUsesExplicitCharset() {
        byte[] bytes = HTML.getBytes(Charset.forName("ISO-8859-1"));
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(bytes), "ISO-8859-1", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void parseByteDataHandlesInvalidCharsetByFallingBackToDefault() {
        Document doc = DataUtil.parseByteData(
                ByteBuffer.wrap(HTML.getBytes(Charset.forName("UTF-8"))),
                "this-charset-does-not-exist", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void parseByteDataNullByteDataThrows() {
        try {
            DataUtil.parseByteData(null, "UTF-8", "http://example.com", Parser.htmlParser());
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException for null byteData");
    }

    @Test
    public void parseByteDataNullBaseUriThrows() {
        try {
            DataUtil.parseByteData(
                    ByteBuffer.wrap(HTML.getBytes(Charset.forName("UTF-8"))),
                    "UTF-8", null, Parser.htmlParser());
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException for null baseUri");
    }

    @Test
    public void parseByteDataNullParserThrows() {
        try {
            DataUtil.parseByteData(
                    ByteBuffer.wrap(HTML.getBytes(Charset.forName("UTF-8"))),
                    "UTF-8", "http://example.com", null);
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException for null parser");
    }

    @Test
    public void loadEmptyInputStreamWithNullCharsetShouldNotThrow() throws IOException {
        Document doc = DataUtil.load(
                new ByteArrayInputStream(new byte[0]), null, "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void loadInputStreamWithUtf8Bom() throws IOException {
        byte[] content = HTML.getBytes(Charset.forName("UTF-8"));
        ByteArrayOutputStreamWithBom stream = new ByteArrayOutputStreamWithBom();
        stream.write((byte) 0xEF);
        stream.write((byte) 0xBB);
        stream.write((byte) 0xBF);
        stream.write(content);

        Document doc = DataUtil.load(
                new ByteArrayInputStream(stream.toByteArray()), null, "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void loadInputStreamWithExplicitCharset() throws IOException {
        byte[] bytes = HTML.getBytes(Charset.forName("ISO-8859-1"));
        Document doc = DataUtil.load(
                new ByteArrayInputStream(bytes), "ISO-8859-1", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void getCharsetFromContentTypeNullIsSafe() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test
    public void getCharsetFromContentTypeReturnsNullWhenNoCharset() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
    }

    @Test
    public void getCharsetFromContentTypeParsesSimpleCharset() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
    }

    @Test
    public void getCharsetFromContentTypeParsesUppercaseCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html;charset=UTF-8"));
    }

    @Test
    public void getCharsetFromContentTypeParsesDoubleQuotedCharset() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
    }

    @Test
    public void getCharsetFromContentTypeParsesSingleQuotedCharset() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset='utf-8'"));
    }

    @Test
    public void getCharsetFromContentTypeParsesCharsetWithTrailingSemicolon() {
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8;"));
    }

    private static class ByteArrayOutputStreamWithBom extends java.io.ByteArrayOutputStream {
        @Override
        public synchronized void write(int b) {
            super.write(b);
        }
    }
}