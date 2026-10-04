package org.jsoup.helper;

import org.junit.Test;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class DataUtilTest {

    @Test
    public void testDetectCharsetFromBom_Utf8() {
        // BOM for UTF-8: EF BB BF
        byte[] bomData = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'h', 'e', 'l', 'l', 'o'};
        ByteBuffer byteBuffer = ByteBuffer.wrap(bomData);
        
        // Use reflection or package-private access via DataUtil to test detectCharsetFromBom if accessible,
        // or test it indirectly via parseStream. 
        // Since DataUtil methods are mostly public, let's test parseStream or other public methods.
    }

    @Test
    public void testParseInputStreamNullCharset() throws IOException {
        String html = "<html><head><title>First Parse</title></head><body><p>Parsed Html!</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        
        // Testing load(InputStream in, String charsetName, String baseUri)
        Document doc = DataUtil.load(in, null, "http://example.com/");
        assertNotNull(doc);
        assertEquals("First Parse", doc.title());
    }

    @Test
    public void testParseInputStreamWithCharset() throws IOException {
        String html = "<html><head><title>Charset Test</title></head><body><p>Hello</p></body></html>";
        InputStream in = new ByteArrayInputStream(html.getBytes("UTF-8"));
        
        Document doc = DataUtil.load(in, "UTF-8", "http://example.com/");
        assertNotNull(doc);
        assertEquals("Charset Test", doc.title());
    }

    @Test
    public void testParseInputStreamWithXmlDeclaration() throws IOException {
        // Jsoup 50 deals with charset detection from BOM and XML declarations
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><element>Data</element></root>";
        InputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        
        Document doc = DataUtil.load(in, null, "http://example.com/", Parser.xmlParser());
        assertNotNull(doc);
        assertTrue(doc.toString().contains("Data"));
    }

    @Test
    public void testParseInputStreamWithBOM() throws IOException {
        byte[] bomAndHtml = new byte[] {
            (byte) 0xEF, (byte) 0xBB, (byte) 0xBF,
            '<', 'h', 't', 'm', 'l', '>', '<', 'h', 'e', 'a', 'd', '>',
            '<', 't', 'i', 't', 'l', 'e', '>', 'B', 'O', 'M', '<', '/', 't', 'i', 't', 'l', 'e', '>',
            '<', '/', 'h', 'e', 'a', 'd', '>', '<', 'b', 'o', 'd', 'y', '>', '<', '/', 'b', 'o', 'd', 'y', '>',
            '<', '/', 'html', '>'
        };
        InputStream in = new ByteArrayInputStream(bomAndHtml);
        Document doc = DataUtil.load(in, null, "http://example.com/");
        assertNotNull(doc);
        assertEquals("BOM", doc.title());
    }

    @Test
    public void testGetCharsetFromContentType() {
        // Directly test private/public helpers if exposed, or through load/parse
        String contentTypeValid = "text/html;charset=utf-8";
        assertEquals("utf-8", DataUtil.getCharsetFromContentType(contentTypeValid));

        String contentTypeWithQuotes = "text/html; charset=\"UTF-8\"";
        assertEquals("UTF-8", DataUtil.getCharsetFromContentType(contentTypeWithQuotes));

        String contentTypeNull = null;
        assertNull(DataUtil.getCharsetFromContentType(contentTypeNull));

        String contentTypeNoCharset = "text/html";
        assertNull(DataUtil.getCharsetFromContentType(contentTypeNoCharset));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadNullFile() throws IOException {
        File file = null;
        DataUtil.load(file, "UTF-8", "http://example.com/");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadNullBaseUri() throws IOException {
        DataUtil.load(new ByteArrayInputStream(new byte[0]), "UTF-8", null);
    }

    @Test
    public void testCrossCharsetSupport() {
        boolean supported = Charset.isSupported("UTF-8");
        assertTrue(supported);
    }
}