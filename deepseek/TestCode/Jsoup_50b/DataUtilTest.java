package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataUtilTest {

    @Test
    public void loadFromInputStreamWithExplicitCharset() throws IOException {
        String html = "<html><head><title>Hello</title></head><body>Content</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("UTF-8"));
        Document doc = DataUtil.load(new ByteArrayInputStream(bytes), "UTF-8", "http://example.com");
        assertEquals("Hello", doc.title());
        assertEquals("Content", doc.body().text());
    }

    @Test
    public void loadFromInputStreamWithNullCharsetDefaultsToUtf8() throws IOException {
        String html = "<html><head><title>Default</title></head><body>Body</body></html>";
        Document doc = DataUtil.load(
                new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8"))),
                null, "http://example.com");
        assertEquals("Default", doc.title());
        assertEquals("Body", doc.body().text());
    }

    @Test
    public void loadFromInputStreamDetectsMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"iso-8859-1\"><title>caf\u00e9</title></head><body>caf\u00e9</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));
        Document doc = DataUtil.load(new ByteArrayInputStream(bytes), null, "http://example.com");
        assertEquals("caf\u00e9", doc.title());
        assertEquals("caf\u00e9", doc.body().text());
    }

    @Test
    public void loadFromInputStreamDetectsMetaContentTypeCharset() throws IOException {
        String html = "<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=windows-1252\"><title>caf\u00e9</title></head><body>caf\u00e9</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("windows-1252"));
        Document doc = DataUtil.load(new ByteArrayInputStream(bytes), null, "http://example.com");
        assertEquals("caf\u00e9", doc.title());
        assertEquals("caf\u00e9", doc.body().text());
    }

    @Test
    public void parseByteDataHandlesExplicitCharset() {
        String html = "<html><head><title>Explicit</title></head><body>Data</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, "UTF-8", "http://example.com", Parser.htmlParser());
        assertEquals("Explicit", doc.title());
        assertEquals("Data", doc.body().text());
    }

    @Test
    public void parseByteDataHandlesNullCharsetWithoutMeta() {
        String html = "<html><head><title>No Meta</title></head><body>Data</body></html>";
        ByteBuffer buffer = ByteBuffer.wrap(html.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(buffer, null, "http://example.com", Parser.htmlParser());
        assertEquals("No Meta", doc.title());
        assertEquals("Data", doc.body().text());
    }

    @Test
    public void parseByteDataHandlesEmptyContent() {
        Document doc = DataUtil.parseByteData(ByteBuffer.wrap(new byte[0]), null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
    }

    @Test
    public void loadFromLargeInputStreamReadsAllContent() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Large</title></head><body>");
        for (int i = 0; i < 200000; i++) {
            sb.append('a');
        }
        sb.append("</body></html>");
        String html = sb.toString();

        Document doc = DataUtil.load(
                new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8"))),
                "UTF-8", "http://example.com");
        assertEquals("Large", doc.title());
        assertEquals(200000, doc.body().text().length());
    }

    @Test
    public void loadFromFile() throws IOException {
        File file = File.createTempFile("jsoup-datautil", ".html");
        file.deleteOnExit();
        String html = "<html><head><title>File</title></head><body>File body</body></html>";

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(html.getBytes(Charset.forName("UTF-8")));
        }

        Document doc = DataUtil.load(file, "UTF-8", "http://example.com");
        assertEquals("File", doc.title());
        assertEquals("File body", doc.body().text());
    }

    @Test
    public void loadWithCustomParser() throws IOException {
        String xml = "<root><item>one</item></root>";
        Document doc = DataUtil.load(
                new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))),
                "UTF-8", "http://example.com", Parser.xmlParser());
        assertNotNull(doc.select("item").first());
        assertEquals("one", doc.select("item").first().text());
    }

    @Test
    public void parseInputStreamWithUtf8Bom() throws IOException {
        String html = "<html><head><title>BOM</title></head><body>BOM</body></html>";
        byte[] bom = new byte[] {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] content = html.getBytes(Charset.forName("UTF-8"));
        byte[] all = new byte[bom.length + content.length];

        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(all), null, "http://example.com", Parser.htmlParser());
        assertEquals("BOM", doc.title());
    }

    @Test
    public void parseInputStreamWithUtf16BeBom() throws IOException {
        String html = "<html><head><title>BOM16</title></head><body>BOM16</body></html>";
        byte[] bom = new byte[] {(byte) 0xFE, (byte) 0xFF};
        byte[] content = html.getBytes(Charset.forName("UTF-16BE"));
        byte[] all = new byte[bom.length + content.length];

        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(all), null, "http://example.com", Parser.htmlParser());
        assertEquals("BOM16", doc.title());
    }

    @Test
    public void parseInputStreamWithUtf16LeBom() throws IOException {
        String html = "<html><head><title>BOM16LE</title></head><body>BOM16LE</body></html>";
        byte[] bom = new byte[] {(byte) 0xFF, (byte) 0xFE};
        byte[] content = html.getBytes(Charset.forName("UTF-16LE"));
        byte[] all = new byte[bom.length + content.length];

        System.arraycopy(bom, 0, all, 0, bom.length);
        System.arraycopy(content, 0, all, bom.length, content.length);

        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(all), null, "http://example.com", Parser.htmlParser());
        assertEquals("BOM16LE", doc.title());
    }

    @Test
    public void parseInputStreamWithNoBomAndNullCharset() throws IOException {
        String html = "<html><head><title>No BOM</title></head><body>No BOM</body></html>";
        Document doc = DataUtil.parseInputStream(
                new ByteArrayInputStream(html.getBytes(Charset.forName("UTF-8"))),
                null, "http://example.com", Parser.htmlParser());
        assertEquals("No BOM", doc.title());
    }
}