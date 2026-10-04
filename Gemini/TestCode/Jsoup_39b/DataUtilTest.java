package org.jsoup.helper;

import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import static org.junit.Assert.*;

public class DataUtilTest {

    @Test
    public void testDetectCharsetFromBom() {
        // UTF-8 BOM: EF BB BF
        byte[] utf8BomBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'h', 'e', 'l', 'l', 'o'};
        ByteBuffer buffer = ByteBuffer.wrap(utf8BomBytes);
        String charset = DataUtil.parseCharsetFromBOM(buffer, "UTF-8");
        assertEquals("UTF-8", charset);
    }

    @Test
    public void testDetectCharsetFromBomUtf16LE() {
        // UTF-16LE BOM: FF FE
        byte[] utf16LeBytes = {(byte) 0xFF, (byte) 0xFE, 'h', 0, 'e', 0};
        ByteBuffer buffer = ByteBuffer.wrap(utf16LeBytes);
        String charset = DataUtil.parseCharsetFromBOM(buffer, "UTF-8");
        assertEquals("UTF-16LE", charset);
    }

    @Test
    public void testDetectCharsetFromBomUtf16BE() {
        // UTF-16BE BOM: FE FF
        byte[] utf16BeBytes = {(byte) 0xFE, (byte) 0xFF, 0, 'h', 0, 'e'};
        ByteBuffer buffer = ByteBuffer.wrap(utf16BeBytes);
        String charset = DataUtil.parseCharsetFromBOM(buffer, "UTF-8");
        assertEquals("UTF-16BE", charset);
    }

    @Test
    public void testParseCharsetFromMetaContentValid() {
        String contentType = "text/html; charset=UTF-8";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("UTF-8", charset);
    }

    @Test
    public void testParseCharsetFromMetaContentQuotes() {
        String contentType = "text/html; charset=\"ISO-8859-1\"";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("ISO-8859-1", charset);
    }

    @Test
    public void testParseCharsetFromMetaContentSingleQuotes() {
        String contentType = "text/html; charset='windows-1252'";
        String charset = DataUtil.getCharsetFromContentType(contentType);
        assertEquals("windows-1252", charset);
    }

    @Test
    public void testParseCharsetFromMetaContentNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test
    public void testParseCharsetFromMetaContentNoCharset() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; foo=bar"));
    }

    @Test
    public void testParseCharsetFromMetaContentInvalidCharset() {
        // Should handle unsupported/invalid charset names gracefully or return null
        String charset = DataUtil.getCharsetFromContentType("text/html; charset=invalid-charset-name-xyz");
        assertNull(charset);
    }

    @Test
    public void testValidCharset() {
        assertTrue(Charset.isSupported("UTF-8"));
    }

    @Test
    public void testCrossLoadStream() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>Hello Jsoup</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.text().contains("Hello Jsoup"));
    }

    @Test
    public void testLoadEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testLoadWithDefaultCharsetFallback() throws IOException {
        // Stream without meta charset, relying on defaultCharset
        String html = "<html><head></head><body>Fallback test</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("ISO-8859-1"));
        Document doc = DataUtil.load(in, "ISO-8859-1", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.text().contains("Fallback test"));
    }

    @Test
    public void testLoadMalformedHtmlWithBOM() throws IOException {
        byte[] htmlBytes = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, '<', 'h', 't', 'm', 'l', '>', '<', 'h', 'e', 'a', 'd', '>', '<', '/', 'h', 'e', 'a', 'd', '>', '<', 'b', 'o', 'd', 'y', '>', 'B', 'O', 'M', ' ', 'T', 'e', 's', 't', '<', '/', 'b', 'o', 'd', 'y', '>', '<', '/', 'h', 't', 'm', 'l', '>'};
        InputStream in = new ByteArrayInputStream(htmlBytes);
        Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.text().contains("BOM Test"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadNullFile() throws IOException {
        File file = null;
        DataUtil.load(file, "UTF-8", "http://example.com");
    }

    @Test
    public void testConstantsAndMethodsSafety() {
        // Exercise alternative paths of internal helper methods if package-visible
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        assertNull(DataUtil.parseCharsetFromBOM(emptyBuffer, "UTF-8"));
    }

    @Test
    public void testStreamPinningAndRead() throws IOException {
        String testString = "Test data stream buffering capabilities in DataUtil.";
        InputStream in = new ByteArrayInputStream(testString.getBytes("UTF-8"));
        ByteBuffer byteBuffer = DataUtil.readToByteBuffer(in, 0);
        assertNotNull(byteBuffer);
        assertTrue(byteBuffer.capacity() > 0);
    }
}