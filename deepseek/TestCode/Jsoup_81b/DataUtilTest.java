package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Test suite for DataUtil covering BOM handling, charset detection, and stream parsing.
 */
public class DataUtilTest {

    // --- getCharsetFromContentType ---

    @Test
    public void getCharsetFromContentTypeReturnsNullWhenNoCharset() {
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("application/xml"));
        assertNull(DataUtil.getCharsetFromContentType(""));
    }

    @Test
    public void getCharsetFromContentTypeReturnsCharsetWhenPresent() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=UTF-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("application/xml; charset=ISO-8859-1"));
    }

    @Test
    public void getCharsetFromContentTypeIgnoresCase() {
        assertEquals("windows-1252", DataUtil.getCharsetFromContentType("text/plain; Charset=Windows-1252"));
    }

    @Test
    public void getCharsetFromContentTypeTrimsWhitespace() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset = UTF-8"));
    }

    @Test
    public void getCharsetFromContentTypeHandlesQuotes() {
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\""));
    }

    @Test
    public void getCharsetFromContentTypeReturnsNullForNullInput() {
        assertNull(DataUtil.getCharsetFromContentType(null));
    }

    // --- readToByteBuffer ---

    @Test
    public void readToByteBufferReadsAllData() throws IOException {
        String data = "Hello, World!";
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(bytes));
        assertEquals(data, new String(buffer.array(), buffer.position(), buffer.limit(), StandardCharsets.UTF_8));
    }

    @Test
    public void readToByteBufferHandlesZeroLength() throws IOException {
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, buffer.remaining());
    }

    @Test
    public void readToByteBufferHandlesLargeData() throws IOException {
        byte[] data = new byte[1024 * 1024]; // 1 MB
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(data));
        assertEquals(data.length, buffer.remaining());
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], buffer.get(i));
        }
    }

    // --- parseInputStream ---

    @Test
    public void parseInputStreamBasicHtml() throws IOException {
        String html = "<html><head><title>Test</title></head><body><p>Hello</p></body></html>";
        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)),
                null,
                "http://example.com/",
                Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Test", doc.title());
        assertEquals("Hello", doc.select("p").text());
        assertEquals("http://example.com/", doc.baseUri());
    }

    @Test
    public void parseInputStreamBasicXml() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>value</child></root>";
        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                null,
                "http://example.com/",
                Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("value", doc.select("child").text());
    }

    @Test
    public void parseInputStreamUtf8BomXml() throws IOException {
        byte[] xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>text</root>"
                .getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[xml.length + 3];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(xml, 0, withBom, 3, xml.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(withBom),
                null,
                "",
                Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("text", doc.select("root").text());
    }

    @Test
    public void parseInputStreamUtf16LeBomXml() throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><root>data</root>";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16LE);
        byte[] withBom = new byte[contentBytes.length + 2];
        withBom[0] = (byte) 0xFF;
        withBom[1] = (byte) 0xFE;
        System.arraycopy(contentBytes, 0, withBom, 2, contentBytes.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(withBom),
                null,
                "",
                Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("data", doc.select("root").text());
    }

    @Test
    public void parseInputStreamUtf16BeBomXml() throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><root>data</root>";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_16BE);
        byte[] withBom = new byte[contentBytes.length + 2];
        withBom[0] = (byte) 0xFE;
        withBom[1] = (byte) 0xFF;
        System.arraycopy(contentBytes, 0, withBom, 2, contentBytes.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(withBom),
                null,
                "",
                Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("data", doc.select("root").text());
    }

    @Test
    public void parseInputStreamUtf32LeBomXml() throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-32\"?><root>data</root>";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_32LE);
        byte[] withBom = new byte[contentBytes.length + 4];
        withBom[0] = (byte) 0xFF;
        withBom[1] = (byte) 0xFE;
        withBom[2] = (byte) 0x00;
        withBom[3] = (byte) 0x00;
        System.arraycopy(contentBytes, 0, withBom, 4, contentBytes.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(withBom),
                null,
                "",
                Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("data", doc.select("root").text());
    }

    @Test
    public void parseInputStreamUtf32BeBomXml() throws IOException {
        String content = "<?xml version=\"1.0\" encoding=\"UTF-32\"?><root>data</root>";
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_32BE);
        byte[] withBom = new byte[contentBytes.length + 4];
        withBom[0] = (byte) 0x00;
        withBom[1] = (byte) 0x00;
        withBom[2] = (byte) 0xFE;
        withBom[3] = (byte) 0xFF;
        System.arraycopy(contentBytes, 0, withBom, 4, contentBytes.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(withBom),
                null,
                "",
                Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("root", doc.child(0).tagName());
        assertEquals("data", doc.select("root").text());
    }

    @Test
    public void parseInputStreamWithExplicitCharset() throws IOException {
        String html = "<html><body>Hello</body></html>";
        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(html.getBytes(StandardCharsets.ISO_8859_1)),
                "ISO-8859-1",
                "",
                Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void parseInputStreamWithContentTypeCharset() throws IOException {
        String html = "<html><body>Hello</body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        // Simulate content type with charset by adding meta? Actually DataUtil uses supplied charset param or default.
        // Here we just pass explicit charset to ensure it's used.
        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(bytes),
                "UTF-8",
                "",
                Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void parseInputStreamEmptyContent() throws IOException {
        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(new byte[0]),
                null,
                "",
                Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("", doc.body().text());
    }

    @Test(expected = IOException.class)
    public void parseInputStreamNullInputThrows() throws IOException {
        DataUtil.parseInputStream(null, null, "", Parser.htmlParser());
    }
}