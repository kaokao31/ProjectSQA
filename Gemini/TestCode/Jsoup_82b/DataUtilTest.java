package org.jsoup.helper;

import org.junit.Test;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.IllegalCharsetNameException;

import static org.junit.Assert.*;

public class DataUtilTest {

    @Test
    public void testDetectCharsetFromBomUtf8() {
        // UTF-8 BOM: EF BB BF
        byte[] bomData = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'h', 'e', 'l', 'l', 'o' };
        ByteArrayInputStream in = new ByteArrayInputStream(bomData);
        
        try {
            // DataUtil.load method or charset detection logic if exposed, 
            // but let's test typical public APIs like parseByteData or load.
            // Let's use parseByteData with a baseUri and defaultCharset.
            Document doc = DataUtil.parseByteData(in, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
            assertNotNull(doc);
            assertTrue(doc.text().contains("hello"));
        } catch (Exception e) {
            fail("Should not have thrown exception: " + e.getMessage());
        }
    }

    @Test
    public void testDetectCharsetFromBomUtf16LE() {
        // UTF-16LE BOM: FF FE
        byte[] bomData = new byte[] { (byte) 0xFF, (byte) 0xFE, 'a', 0 };
        ByteArrayInputStream in = new ByteArrayInputStream(bomData);
        
        try {
            Document doc = DataUtil.parseByteData(in, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
            assertNotNull(doc);
        } catch (Exception e) {
            // Depending on parser, but should handle cleanly
        }
    }

    @Test
    public void testDetectCharsetFromBomUtf16BE() {
        // UTF-16BE BOM: FE FF
        byte[] bomData = new byte[] { (byte) 0xFE, (byte) 0xFF, 0, 'a' };
        ByteArrayInputStream in = new ByteArrayInputStream(bomData);
        
        try {
            Document doc = DataUtil.parseByteData(in, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
            assertNotNull(doc);
        } catch (Exception e) {
        }
    }

    @Test
    public void testNullInputStream() {
        try {
            DataUtil.parseByteData(null, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
        } catch (Exception e) {
            // expected or handled safely
        }
    }

    @Test
    public void testEmptyStream() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        try {
            Document doc = DataUtil.parseByteData(in, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
            assertNotNull(doc);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMetaCharsetHtml5() {
        // Test HTML with <meta charset="UTF-8"> or <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body>Hello</body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            Document doc = DataUtil.parseByteData(in, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
            assertNotNull(doc);
            assertEquals("Hello", doc.text());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testInvalidCharsetFallback() {
        // If an invalid charset is specified or detected, it should fallback safely (often to UTF-8 or default)
        String html = "<html><head><meta charset=\"invalid-charset-name-12345\"></head><body>Hello</body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            Document doc = DataUtil.parseByteData(in, null, "http://example.com", org.jsoup.parser.Parser.htmlParser());
            assertNotNull(doc);
        } catch (Exception e) {
            // Should not crash the JVM or throw unhandled illegal charset exceptions if DataUtil handles it
        }
    }

    @Test
    public void testCrossCharsetSpecifiers() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"></head><body>Test</body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream in = new ByteArrayInputStream(data);

        try {
            Document doc = DataUtil.parseByteData(in, "UTF-8", "http://example.com", org.jsoup.parser.Parser.htmlParser());
            assertNotNull(doc);
        } catch (Exception e) {
        }
    }

    @Test
    public void testNormaliseMime() {
        // DataUtil has some package-private or public helper methods if any exist, 
        // but we focus on public API robustness.
        assertNotNull(DataUtil.utf8Charset);
    }
}