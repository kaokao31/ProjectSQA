package org.jsoup.helper;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Locale;

import static org.junit.Assert.*;

public class DataUtilTest {

    @Test
    public void testDetectCharsetFromContentType() {
        // Test standard charset extraction
        String contentType1 = "text/html; charset=UTF-8";
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType(contentType1));

        String contentType2 = "text/html; charset=\"ISO-8859-1\"";
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType(contentType2));

        String contentType3 = "text/html; charset=utf-16";
        assertEquals("utf-16", DataUtil.getCharsetFromContentType(contentType3));

        // Test no charset
        String contentType4 = "text/html";
        assertNull(DataUtil.getCharsetFromContentType(contentType4));

        // Test null
        assertNull(DataUtil.getCharsetFromContentType(null));

        // Test empty/blank
        assertNull(DataUtil.getCharsetFromContentType(""));
        assertNull(DataUtil.getCharsetFromContentType("; charset="));
    }

    @Test
    public void testParseCharsetNullHandling() {
        assertNull(DataUtil.parseCharset(null));
        assertNull(DataUtil.parseCharset(""));
        assertNull(DataUtil.parseCharset("invalid-charset-name-!@#$"));
    }

    @Test
    public void testConstants() {
        // Access constants to ensure coverage if any
        assertEquals("UTF-8", DataUtil.DEFAULT_CHARSET);
    }

    @Test
    public void testCrossCharsetHandling() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>Hello World</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        
        org.jsoup.nodes.Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertTrue(doc.text().contains("Hello World"));
    }

    @Test
    public void testLoadEmptyStream() throws IOException {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        org.jsoup.nodes.Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
    }

    @Test
    public void testDetectCharsetFromMeta() throws IOException {
        // Testing html with meta charset
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Test</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        org.jsoup.nodes.Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("Test", doc.body().text());
    }
}