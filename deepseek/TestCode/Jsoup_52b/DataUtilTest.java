package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class DataUtilTest {

    @Test
    public void parseByteData_utf8WithoutCharsetParses() {
        ByteBuffer data = ByteBuffer.wrap("<html><body><p>Hello</p></body></html>".getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com/", Parser.htmlParser());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void parseByteData_explicitCharsetDecodes() {
        String html = "<html><head><title>Hello</title></head><body><p>Hello</p></body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(data, "UTF-8", "http://example.com/", Parser.htmlParser());
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void parseByteData_utf8BomDoesNotRetainBom() throws IOException {
        String html = "<html><head><title>Bom</title></head><body><p>Hello</p></body></html>";
        ByteBuffer data = ByteBuffer.wrap(concat(
                new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                html.getBytes(Charset.forName("UTF-8"))));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com/", Parser.htmlParser());
        assertEquals("Hello", doc.body().text());
        assertFalse("BOM should not be present in parsed document", doc.toString().contains("\uFEFF"));
    }

    @Test
    public void parseByteData_utf8BomXmlParses() throws IOException {
        String xml = "<root>Hello</root>";
        ByteBuffer data = ByteBuffer.wrap(concat(
                new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                xml.getBytes(Charset.forName("UTF-8"))));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com/", Parser.xmlParser());
        assertEquals("Hello", doc.text());
        assertFalse("BOM should not be present in parsed XML document", doc.toString().contains("\uFEFF"));
    }

    @Test
    public void parseByteData_utf16LeBomDoesNotRetainBom() throws IOException {
        String html = "<html><head><title>Bom</title></head><body><p>Hello</p></body></html>";
        ByteBuffer data = ByteBuffer.wrap(concat(
                new byte[]{(byte) 0xFF, (byte) 0xFE},
                html.getBytes(Charset.forName("UTF-16LE"))));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com/", Parser.htmlParser());
        assertEquals("Hello", doc.body().text());
        assertFalse("BOM should not be present in parsed document", doc.toString().contains("\uFEFF"));
    }

    @Test
    public void parseByteData_utf16BeBomDoesNotRetainBom() throws IOException {
        String html = "<html><head><title>Bom</title></head><body><p>Hello</p></body></html>";
        ByteBuffer data = ByteBuffer.wrap(concat(
                new byte[]{(byte) 0xFE, (byte) 0xFF},
                html.getBytes(Charset.forName("UTF-16BE"))));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com/", Parser.htmlParser());
        assertEquals("Hello", doc.body().text());
        assertFalse("BOM should not be present in parsed document", doc.toString().contains("\uFEFF"));
    }

    @Test
    public void parseByteData_metaCharsetUsedToDecode() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"><title>Café</title></head><body><p>Café</p></body></html>";
        ByteBuffer data = ByteBuffer.wrap(html.getBytes(Charset.forName("ISO-8859-1")));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com/", Parser.htmlParser());
        assertEquals("Café", doc.title());
        assertEquals("Café", doc.body().text());
    }

    @Test
    public void loadInputStream_utf8BomParses() throws IOException {
        String html = "<html><head><title>Bom</title></head><body><p>Hello</p></body></html>";
        byte[] bytes = concat(
                new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF},
                html.getBytes(Charset.forName("UTF-8")));
        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = DataUtil.load(in, null, "http://example.com/");
        assertEquals("Hello", doc.body().text());
        assertFalse(doc.toString().contains("\uFEFF"));
    }

    @Test
    public void loadInputStream_utf8WithoutCharsetParses() throws IOException {
        String html = "<html><body><p>Hello</p></body></html>";
        Document doc = DataUtil.load(new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8"))), null, "http://example.com/");
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void readToByteBuffer_readsAllBytes() throws IOException {
        byte[] bytes = "1234567890".getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(bytes));
        byte[] out = new byte[buffer.remaining()];
        buffer.get(out);
        assertArrayEquals(bytes, out);
    }

    @Test
    public void getCharsetFromContentType_nullIsNull() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    @Test
    public void getCharsetFromContentType_noCharsetIsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset="));
    }

    @Test
    public void getCharsetFromContentType_detectsCharset() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("utf-8", DataUtil.getCharsetFromContentType("text/html;charset=utf-8"));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html;charset=ISO-8859-1"));
    }

    @Test
    public void getCharsetFromContentType_invalidCharsetIsNull() {
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=invalid-charset"));
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] c = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
}