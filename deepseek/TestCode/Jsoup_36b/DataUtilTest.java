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
import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DataUtilTest {

    private static final String SIMPLE_HTML =
            "<html><head><title>One</title></head><body>Two</body></html>";

    @Test
    public void testGetCharsetFromContentType() {
        assertNull(DataUtil.getCharsetFromContentType(null));
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("text/html"));
        assertNull(DataUtil.getCharsetFromContentType("text/html; charset=NotACharset"));

        assertEquals("utf-8",
                DataUtil.getCharsetFromContentType("text/html; charset=UTF-8").toLowerCase(Locale.ENGLISH));
        assertEquals("utf-8",
                DataUtil.getCharsetFromContentType("text/html;charset=UTF-8").toLowerCase(Locale.ENGLISH));
        assertEquals("utf-8",
                DataUtil.getCharsetFromContentType("text/html; charset=\"UTF-8\"").toLowerCase(Locale.ENGLISH));
        assertEquals("utf-8",
                DataUtil.getCharsetFromContentType("text/html; charset='UTF-8'").toLowerCase(Locale.ENGLISH));
    }

    @Test
    public void testParseByteDataWithExplicitCharset() {
        ByteBuffer buffer = ByteBuffer.wrap(SIMPLE_HTML.getBytes(Charset.forName("UTF-8")));
        Document doc = DataUtil.parseByteData(
                buffer, "UTF-8", "http://example.com/", Parser.htmlParser());

        assertEquals("One", doc.title());
        assertEquals("Two", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataWithNullCharsetDefaultsToUtf8() {
        Document doc = parse(SIMPLE_HTML.getBytes(Charset.forName("UTF-8")), null);

        assertEquals("One", doc.title());
        assertEquals("Two", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataDetectsMetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>caf\u00e9</body></html>";
        Document doc = parse(html.getBytes(Charset.forName("ISO-8859-1")), null);

        assertEquals("caf\u00e9", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataDetectsCharsetFromHttpEquivContentType() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" " +
                "content=\"text/html; charset=ISO-8859-1\"></head><body>caf\u00e9</body></html>";
        Document doc = parse(html.getBytes(Charset.forName("ISO-8859-1")), null);

        assertEquals("caf\u00e9", doc.body().text());
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataFallsBackWhenMetaCharsetIsInvalid() {
        String html = "<html><head><meta charset=\"NoSuchCharset\"></head><body>Hello</body></html>";
        Document doc = parse(html.getBytes(Charset.forName("UTF-8")), null);

        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParseByteDataFallsBackWhenHttpEquivCharsetIsInvalid() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" " +
                "content=\"text/html; charset=NoSuchCharset\"></head><body>Hello</body></html>";
        Document doc = parse(html.getBytes(Charset.forName("UTF-8")), null);

        assertEquals("Hello", doc.body().text());
    }

    @Test
    public void testParseByteDataWithUtf8Bom() {
        byte[] htmlBytes = SIMPLE_HTML.getBytes(Charset.forName("UTF-8"));
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        Document doc = parse(concat(bom, htmlBytes), null);

        assertEquals("One", doc.title());
        assertEquals("Two", doc.body().text());
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataWithUtf16BeBom() {
        byte[] htmlBytes = SIMPLE_HTML.getBytes(Charset.forName("UTF-16BE"));
        byte[] bom = new byte[]{(byte) 0xFE, (byte) 0xFF};
        Document doc = parse(concat(bom, htmlBytes), null);

        assertEquals("One", doc.title());
        assertEquals("Two", doc.body().text());
        assertEquals("UTF-16BE", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataWithUtf16LeBom() {
        byte[] htmlBytes = SIMPLE_HTML.getBytes(Charset.forName("UTF-16LE"));
        byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
        Document doc = parse(concat(bom, htmlBytes), null);

        assertEquals("One", doc.title());
        assertEquals("Two", doc.body().text());
        assertEquals("UTF-16LE", doc.outputSettings().charset().name());
    }

    @Test
    public void testLoadInputStreamWithExplicitCharset() throws IOException {
        String html = "<html><head><title>caf\u00e9</title></head><body>caf\u00e9</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));

        Document doc = DataUtil.load(
                new ByteArrayInputStream(bytes), "ISO-8859-1", "http://example.com/");

        assertEquals("caf\u00e9", doc.title());
        assertEquals("caf\u00e9", doc.body().text());
    }

    @Test
    public void testLoadInputStreamWithMetaCharset() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" " +
                "content=\"text/html; charset=ISO-8859-1\"></head><body>caf\u00e9</body></html>";
        byte[] bytes = html.getBytes(Charset.forName("ISO-8859-1"));

        Document doc = DataUtil.load(new ByteArrayInputStream(bytes), null, "http://example.com/");

        assertEquals("caf\u00e9", doc.body().text());
    }

    @Test
    public void testLoadFile() throws IOException {
        File file = File.createTempFile("DataUtilTest", ".html");
        try {
            FileOutputStream out = new FileOutputStream(file);
            try {
                out.write(SIMPLE_HTML.getBytes(Charset.forName("UTF-8")));
            } finally {
                out.close();
            }

            Document doc = DataUtil.load(file, "UTF-8", "http://example.com/");

            assertEquals("One", doc.title());
            assertEquals("Two", doc.body().text());
        } finally {
            file.delete();
        }
    }

    @Test
    public void testReadToByteBufferEmpty() throws IOException {
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(new byte[0]));
        assertEquals(0, buffer.remaining());
    }

    @Test
    public void testReadToByteBufferReadsAllInput() throws IOException {
        byte[] data = "Hello DataUtil".getBytes(Charset.forName("UTF-8"));
        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(data));

        byte[] out = new byte[buffer.remaining()];
        buffer.get(out);
        assertArrayEquals(data, out);
    }

    @Test
    public void testReadToByteBufferReadsMoreThanBufferSize() throws IOException {
        byte[] data = new byte[200000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 251);
        }

        ByteBuffer buffer = DataUtil.readToByteBuffer(new ByteArrayInputStream(data));

        byte[] out = new byte[buffer.remaining()];
        buffer.get(out);
        assertArrayEquals(data, out);
    }

    private static Document parse(byte[] bytes, String charsetName) {
        return DataUtil.parseByteData(
                ByteBuffer.wrap(bytes), charsetName, "http://example.com/", null);
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}