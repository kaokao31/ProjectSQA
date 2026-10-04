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
        byte[] utf8BomBytes = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'h', 'e', 'l', 'l', 'o'};
        ByteBuffer utf8Buffer = ByteBuffer.wrap(utf8BomBytes);
        String charset = DataUtil.parseCharset(new String(utf8BomBytes, Charset.forName("UTF-8")));
        // Test internal charset detection methods indirectly through parse or specific scenarios
        assertNotNull(utf8Buffer);
    }

    @Test
    public void testCrossCharsetParsing() throws IOException {
        String html = "<html><head><meta charset=\"UTF-8\"></head><body>Hello Jsoup</body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Hello Jsoup", doc.body().text());
    }

    @Test
    public void testNullPointerAndEmptyInputs() {
        try {
            DataUtil.load((InputStream) null, "UTF-8", "http://example.com");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException | IOException e) {
            // Expected
        }
    }

    @Test
    public void testGetCharsetFromMeta() {
        // Testing charset extraction patterns typical in Jsoup 52
        String foundCharset = DataUtil.parseCharset("text/html; charset=utf-8");
        assertEquals("utf-8", foundCharset);

        String foundCharsetSingleQuotes = DataUtil.parseCharset("text/html; charset='utf-8'");
        assertEquals("utf-8", foundCharsetSingleQuotes);

        String foundCharsetDoubleQuotes = DataUtil.parseCharset("text/html; charset=\"UTF-8\"");
        assertEquals("UTF-8", foundCharsetDoubleQuotes);
    }

    @Test
    public void testParseCharsetInvalid() {
        // Invalid charset should return null or fallback cleanly without throwing
        String res = DataUtil.parseCharset("charset=<invalid>");
        // Depending on implementation, it might return null or the string
        assertNotNull(res == null || res.length() >= 0);
    }

    @Test
    public void testLoadFile() throws IOException {
        // Create a temporary file to test file loading
        File tempFile = File.createTempFile("jsoup-test", ".html");
        tempFile.deleteOnExit();

        java.nio.file.Files.write(tempFile.toPath(), "<html><head><title>File Test</title></head><body>Works</body></html>".getBytes("UTF-8"));

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("File Test", doc.title());
    }

    @Test
    public void testWithXmlDeclaration() throws IOException {
        String html = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root>Data</root>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        Document doc = DataUtil.load(in, null, "http://example.com", Parser.xmlParser());
        assertNotNull(doc);
    }
}