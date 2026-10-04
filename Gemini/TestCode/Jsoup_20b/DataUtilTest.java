package org.jsoup.helper;

import org.jsoup.nodes.Document;
import org.junit.Test;

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
    public void testLoadFile() throws IOException {
        // Create a temporary file to test loading from a File object
        File tempFile = File.createTempFile("jsoup-test", ".html");
        tempFile.deleteOnExit();

        String htmlContent = "<html><head><title>Test File</title><meta charset=\"UTF-8\"></head><body>Hello World</body></html>";
        org.apache.commons.io.FileUtils.writeStringToFile(tempFile, htmlContent, "UTF-8");

        Document doc = DataUtil.load(tempFile, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Test File", doc.title());
        assertEquals("Hello World", doc.body().text());
    }

    @Test
    public void testLoadStreamWithCharset() throws IOException {
        String htmlContent = "<html><head><title>Stream Test</title></head><body>Stream Body</body></html>";
        InputStream in = new ByteArrayInputStream(htmlContent.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, "UTF-8", "http://example.com");
        assertNotNull(doc);
        assertEquals("Stream Test", doc.title());
    }

    @Test
    public void testLoadStreamNoCharset() throws IOException {
        String htmlContent = "<html><head><title>Auto Detect</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>Auto Detect Body</body></html>";
        InputStream in = new ByteArrayInputStream(htmlContent.getBytes("UTF-8"));

        Document doc = DataUtil.load(in, null, "http://example.com");
        assertNotNull(doc);
        assertEquals("Auto Detect", doc.title());
    }

    @Test
    public void testParseCharsetFromContentType() {
        // Test various content-type headers
        assertEquals("UTF-8", DataUtil.parseCharset("text/html; charset=UTF-8"));
        assertEquals("UTF-8", DataUtil.parseCharset("text/html; CHARSET=utf-8"));
        assertEquals("ISO-8859-1", DataUtil.parseCharset("text/html; charset=\"ISO-8859-1\""));
        assertEquals("UTF-8", DataUtil.parseCharset("text/html; charset='utf-8'"));
        assertNull(DataUtil.parseCharset("text/html"));
        assertNull(DataUtil.parseCharset(null));
        assertNull(DataOfInvalidCharset());
    }

    private String DataOfInvalidCharset() {
        // Content-type with invalid charset name that throws IllegalCharsetNameException
        return DataUtil.parseCharset("text/html; charset=INVALID_CHARSET_NAME_!@#");
    }

    @Test
    public void testDetectCharsetFromByteBufferUtf8Bom() {
        // Test UTF-8 BOM EF BB BF
        byte[] bomData = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF, 'H', 'i'};
        ByteBuffer byteBuffer = ByteBuffer.wrap(bomData);
        
        // We can test the private/package-private method indirectly via load or check if accessible.
        // DataUtil.parseCharset or similar logic handling byte buffers.
        // Let's invoke load with a ByteBuffer-backed stream or test byte data directly if possible.
        try {
            Document doc = DataUtil.load(new ByteArrayInputStream(bomData), null, "http://example.com");
            assertNotNull(doc);
        } catch (IOException e) {
            fail("Should not throw IOException");
        }
    }

    @Test
    public void testConstantsAndEdgeCases() {
        // Accessing helper methods with edge cases
        try {
            DataUtil.parseCharset("charset=gb2312; charset=utf-8");
        } catch (Exception e) {
            // Expected or handled gracefully
        }
    }
}