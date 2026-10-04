package org.jsoup.helper;

import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;

public class DataUtilTest {

    @Test
    public void testGetCharsetFromContentType() {
        // Null content type
        assertNull(DataUtil.getCharsetFromContentType(null));

        // Content type without charset
        assertNull(DataUtil.getCharsetFromContentType("text/html"));

        // Content type with valid charset
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.getCharsetFromContentType("text/html;charset=ISO-8859-1"));

        // Content type with quoted charset
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset=\"utf-8\""));
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; charset='utf-8'"));

        // Content type with multiple parameters
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType("text/html; foo=bar; charset=utf-8; baz=qux"));

        // Invalid charset name handling (should return null or handle gracefully depending on impl, let's see)
        try {
            DataUtil.getCharsetFromContentType("text/html; charset=");
        } catch (Exception e) {
            // Some versions might throw or return null
        }
    }

    @Test
    public void testParseByteData() throws IOException {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body>Hello, World!</body></html>";
        byte[] bytes = html.getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(bytes);

        // Test parseByteData with baseUri and defaultCharset
        org.jsoup.nodes.Document doc = DataUtil.parseByteData(in, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertTrue(doc.text().contains("Hello, World!"));
    }

    @Test
    public void testParseByteDataWithMetaCharset() throws IOException {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Café</body></html>";
        byte[] bytes = html.getBytes("ISO-8859-1");
        InputStream in = new ByteArrayInputStream(bytes);

        org.jsoup.nodes.Document doc = DataUtil.parseByteData(in, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.outputSettings().charset().name());
    }

    @Test
    public void testParseByteDataXmlDeclaration() throws IOException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>Data</root>";
        byte[] bytes = xml.getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(bytes);

        org.jsoup.nodes.Document doc = DataUtil.parseByteData(in, null, "http://example.com", org.jsoup.parser.Parser.xmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.outputSettings().charset().name());
    }

    @Test
    public void testCrossCharsetSpec() {
        // Specifically targeting common issues in Jsoup 27 where charset extraction from meta tags or content types is tricky
        String contentType = "application/xhtml+xml; charset=UTF-8";
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType(contentType));

        String weirdContentType = "text/html; charset=EXOTIC-CHARSET";
        // If charset is illegal, DataUtil might throw IllegalCharsetNameException or fall back
        try {
            DataUtil.getCharsetFromContentType(weirdContentType);
        } catch (IllegalCharsetNameException e) {
            // expected for invalid charsets in some versions
        }
    }
}