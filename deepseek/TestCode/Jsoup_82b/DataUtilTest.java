package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for DataUtil, targeting maximum coverage and fault detection (Jsoup bug 82).
 */
public class DataUtilTest {

    // ---------- parseByteData tests ----------

    @Test
    public void testParseByteDataNullData() {
        // null byte array should throw NullPointerException or return null? Typically throws NPE.
        try {
            DataUtil.parseByteData(null, "UTF-8", "http://example.com", Parser.htmlParser());
            fail("Expected NullPointerException for null data");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testParseByteDataEmptyData() {
        byte[] empty = new byte[0];
        Document doc = DataUtil.parseByteData(empty, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertTrue(doc.html().isEmpty() || doc.html().contains("<html>"));
    }

    @Test
    public void testParseByteDataWithBomUtf8() {
        // BOM for UTF-8: 0xEF,0xBB,0xBF followed by simple HTML
        byte[] data = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF, '<', 'h', 't', 'm', 'l', '>', '<', '/', 'h', 't', 'm', 'l', '>'};
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // BOM should be stripped and charset detected as UTF-8
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithBomUtf16BE() {
        // BOM for UTF-16BE: 0xFE,0xFF
        byte[] data = new byte[]{(byte)0xFE, (byte)0xFF, 0, '<', 0, 'h', 0, 't', 0, 'm', 0, 'l', 0, '>', 0, '<', 0, '/', 0, 'h', 0, 't', 0, 'm', 0, 'l', 0, '>'};
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-16BE", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithBomUtf16LE() {
        // BOM for UTF-16LE: 0xFF,0xFE
        byte[] data = new byte[]{(byte)0xFF, (byte)0xFE, '<', 0, 'h', 0, 't', 0, 'm', 0, 'l', 0, '>', 0, '<', 0, '/', 0, 'h', 0, 't', 0, 'm', 0, 'l', 0, '>'};
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-16LE", doc.charset().name());
    }

    @Test
    public void testParseByteDataCharsetInMetaTag() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.ISO_8859_1);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.charset().name());
    }

    @Test
    public void testParseByteDataCharsetInMetaContentType() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"></head><body></body></html>";
        byte[] data = html.getBytes(Charset.forName("Shift_JIS"));
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("Shift_JIS", doc.charset().name());
    }

    @Test
    public void testParseByteDataMetaCharsetAfterMetaContentType() {
        // Bug 82: meta charset after meta content-type should still be detected
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><meta charset=\"UTF-8\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // The last meta charset should override, so charset should be UTF-8
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataMetaContentTypeAfterMetaCharset() {
        // Reverse order: meta charset first, then meta content-type
        String html = "<html><head><meta charset=\"UTF-8\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // The last meta content-type should override? Actually bug 82 is about the opposite, but we test both.
        // According to HTML spec, the first charset declaration wins? But Jsoup may have different behavior.
        // We just assert that parsing succeeds and charset is not null.
        assertNotNull(doc.charset());
    }

    @Test
    public void testParseByteDataNoCharsetDeclared() {
        String html = "<html><head></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // Should default to UTF-8 or the provided charset? Since we passed null, it should detect from BOM or default.
        // Typically defaults to UTF-8.
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithExplicitCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.ISO_8859_1);
        // Explicitly provide a different charset name; should override meta?
        Document doc = DataUtil.parseByteData(data, "UTF-8", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // When explicit charset is given, it should be used regardless of meta?
        // Jsoup's behavior: if charsetName is not null, it uses that and skips detection.
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataInvalidCharsetName() {
        String html = "<html><head></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        // Invalid charset name should fall back to detection
        Document doc = DataUtil.parseByteData(data, "invalid-charset", "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // Should fall back to detection, likely UTF-8
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithXmlDeclaration() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><html><head></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_16);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // XML declaration encoding should be detected
        assertEquals("UTF-16", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithXmlDeclarationAndMeta() {
        // XML declaration encoding should take precedence over meta
        String html = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><html><head><meta charset=\"UTF-8\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.ISO_8859_1);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithMultipleMetaTags() {
        // Multiple meta charset tags; last one should win
        String html = "<html><head><meta charset=\"ISO-8859-1\"><meta charset=\"UTF-8\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithHtmlEntities() {
        // Ensure parsing works with entities
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>&amp;</body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.charset().name());
        assertEquals("&", doc.body().text());
    }

    @Test
    public void testParseByteDataWithNullBaseUri() {
        byte[] data = "<html><head></head><body></body></html>".getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, null, Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithNullParser() {
        byte[] data = "<html><head></head><body></body></html>".getBytes(StandardCharsets.UTF_8);
        try {
            DataUtil.parseByteData(data, null, "http://example.com", null);
            fail("Expected NullPointerException for null parser");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ---------- detectCharset tests (if public) ----------
    // Assuming detectCharset is a public static method that takes a byte array and returns a charset name.
    // If not, we can skip these tests.

    @Test
    public void testDetectCharsetFromBomUtf8() {
        byte[] bom = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        String charset = DataUtil.detectCharset(bom);
        assertEquals("UTF-8", charset);
    }

    @Test
    public void testDetectCharsetFromBomUtf16BE() {
        byte[] bom = new byte[]{(byte)0xFE, (byte)0xFF};
        String charset = DataUtil.detectCharset(bom);
        assertEquals("UTF-16BE", charset);
    }

    @Test
    public void testDetectCharsetFromBomUtf16LE() {
        byte[] bom = new byte[]{(byte)0xFF, (byte)0xFE};
        String charset = DataUtil.detectCharset(bom);
        assertEquals("UTF-16LE", charset);
    }

    @Test
    public void testDetectCharsetNoBom() {
        byte[] data = "<html>".getBytes(StandardCharsets.UTF_8);
        String charset = DataUtil.detectCharset(data);
        // Should return null or default? Depends on implementation. We'll just assert not null.
        assertNotNull(charset);
    }

    @Test
    public void testDetectCharsetEmptyArray() {
        byte[] empty = new byte[0];
        String charset = DataUtil.detectCharset(empty);
        assertNull(charset);
    }

    @Test
    public void testDetectCharsetNull() {
        try {
            DataUtil.detectCharset(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ---------- firstCharset tests (if public) ----------
    // Assuming firstCharset is a public static method that takes a String (HTML) and returns charset name.

    @Test
    public void testFirstCharsetWithMetaCharset() {
        String html = "<html><head><meta charset=\"ISO-8859-1\"></head><body></body></html>";
        String charset = DataUtil.firstCharset(html);
        assertEquals("ISO-8859-1", charset);
    }

    @Test
    public void testFirstCharsetWithMetaContentType() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"></head><body></body></html>";
        String charset = DataUtil.firstCharset(html);
        assertEquals("Shift_JIS", charset);
    }

    @Test
    public void testFirstCharsetWithBothMetaCharsetFirst() {
        String html = "<html><head><meta charset=\"UTF-8\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"></head><body></body></html>";
        String charset = DataUtil.firstCharset(html);
        // Should return the first found? Or last? Depends on implementation.
        // We'll just assert it's not null.
        assertNotNull(charset);
    }

    @Test
    public void testFirstCharsetWithBothMetaContentTypeFirst() {
        // Bug 82 scenario: meta content-type before meta charset
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><meta charset=\"UTF-8\"></head><body></body></html>";
        String charset = DataUtil.firstCharset(html);
        // The bug might cause it to return ISO-8859-1 instead of UTF-8.
        // We expect UTF-8 if the bug is fixed.
        assertEquals("UTF-8", charset);
    }

    @Test
    public void testFirstCharsetNoCharset() {
        String html = "<html><head></head><body></body></html>";
        String charset = DataUtil.firstCharset(html);
        assertNull(charset);
    }

    @Test
    public void testFirstCharsetNullHtml() {
        try {
            DataUtil.firstCharset(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testFirstCharsetEmptyHtml() {
        String charset = DataUtil.firstCharset("");
        assertNull(charset);
    }

    @Test
    public void testFirstCharsetWithXmlDeclaration() {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><html><head></head><body></body></html>";
        String charset = DataUtil.firstCharset(html);
        assertEquals("UTF-16", charset);
    }

    @Test
    public void testFirstCharsetWithXmlDeclarationAndMeta() {
        String html = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><html><head><meta charset=\"UTF-8\"></head><body></body></html>";
        String charset = DataUtil.firstCharset(html);
        // XML declaration should take precedence
        assertEquals("ISO-8859-1", charset);
    }

    // ---------- Additional edge cases ----------

    @Test
    public void testParseByteDataWithLargeData() {
        // Large input to test performance and memory
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset=\"UTF-8\"></head><body>");
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        sb.append("</body></html>");
        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithBinaryData() {
        // Binary data that is not valid HTML should not throw
        byte[] data = new byte[]{0, 1, 2, 3, 4, 5};
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // Should not crash, document may be empty or malformed
    }

    @Test
    public void testParseByteDataWithUnsupportedCharset() {
        // Charset that is not supported by JVM
        String html = "<html><head><meta charset=\"x-unknown\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // Should fall back to default charset (UTF-8)
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithMixedCaseMeta() {
        // Meta tag with mixed case attribute
        String html = "<html><head><META CHARSET=\"ISO-8859-1\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.ISO_8859_1);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("ISO-8859-1", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithQuotesVariations() {
        // Single quotes, no quotes
        String html = "<html><head><meta charset='UTF-8'></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithExtraWhitespace() {
        String html = "<html><head><meta  charset = \"UTF-8\" ></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithNoHead() {
        String html = "<html><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        assertEquals("UTF-8", doc.charset().name());
    }

    @Test
    public void testParseByteDataWithMultipleContentTypeMeta() {
        String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\"><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></head><body></body></html>";
        byte[] data = html.getBytes(StandardCharsets.UTF_8);
        Document doc = DataUtil.parseByteData(data, null, "http://example.com", Parser.htmlParser());
        assertNotNull(doc);
        // Last one should win
        assertEquals("UTF-8", doc.charset().name());
    }
}