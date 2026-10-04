package org.jsoup.helper;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class DataUtilTest {
    
    private static final String UTF_8 = "UTF-8";
    private static final String ISO_8859_1 = "ISO-8859-1";
    
    @Test
    public void testNullInputStream() throws IOException {
        InputStream nullStream = null;
        String result = DataUtil.detectCharset(nullStream, UTF_8);
        assertNotNull("Result should not be null", result);
        assertEquals("Default charset should be returned for null input", UTF_8, result);
    }
    
    @Test
    public void testEmptyInputStream() throws IOException {
        byte[] emptyData = new byte[0];
        InputStream emptyStream = new ByteArrayInputStream(emptyData);
        String result = DataUtil.detectCharset(emptyStream, UTF_8);
        assertEquals("Default charset should be returned for empty input", UTF_8, result);
    }
    
    @Test
    public void testSingleByteBomStream() throws IOException {
        byte[] singleByte = {0xEF};
        InputStream singleByteStream = new ByteArrayInputStream(singleByte);
        String result = DataUtil.detectCharset(singleByteStream, UTF_8);
        assertEquals("Charset should be default for incomplete BOM", UTF_8, result);
    }
    
    @Test
    public void testUtf8BomStream() throws IOException {
        byte[] utf8BomData = {(byte)0xEF, (byte)0xBB, (byte)0xBF, 0x48, 0x65, 0x6C, 0x6C, 0x6F};
        InputStream bomStream = new ByteArrayInputStream(utf8BomData);
        String result = DataUtil.detectCharset(bomStream, UTF_8);
        assertEquals("UTF-8 charset should be detected from BOM", UTF_8, result);
    }
    
    @Test
    public void testUtf16LeBomStream() throws IOException {
        byte[] utf16LeBom = {(byte)0xFF, (byte)0xFE, 0x48, 0x00, 0x65, 0x00};
        InputStream leStream = new ByteArrayInputStream(utf16LeBom);
        String result = DataUtil.detectCharset(leStream, UTF_8);
        assertEquals("UTF-16LE charset should be detected from BOM", "UTF-16LE", result);
    }
    
    @Test
    public void testUtf16BeBomStream() throws IOException {
        byte[] utf16BeBom = {(byte)0xFE, (byte)0xFF, 0x00, 0x48, 0x00, 0x65};
        InputStream beStream = new ByteArrayInputStream(utf16BeBom);
        String result = DataUtil.detectCharset(beStream, UTF_8);
        assertEquals("UTF-16BE charset should be detected from BOM", "UTF-16BE", result);
    }
    
    @Test
    public void testNoBomUtf8Stream() throws IOException {
        byte[] utf8Data = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
        InputStream utf8Stream = new ByteArrayInputStream(utf8Data);
        String result = DataUtil.detectCharset(utf8Stream, UTF_8);
        assertEquals("Default charset should be returned when no BOM", UTF_8, result);
    }
    
    @Test
    public void testNoBomIso88591Stream() throws IOException {
        byte[] isoData = {0x48, (byte)0xF6, 0x6C, 0x6C, 0x65};
        InputStream isoStream = new ByteArrayInputStream(isoData);
        String result = DataUtil.detectCharset(isoStream, ISO_8859_1);
        assertEquals("Default ISO-8859-1 charset should be returned when no BOM", ISO_8859_1, result);
    }
    
    @Test
    public void testIncompleteBomUtf8Stream() throws IOException {
        byte[] incompleteBom = {(byte)0xEF, (byte)0xBB};
        InputStream incompleteStream = new ByteArrayInputStream(incompleteBom);
        String result = DataUtil.detectCharset(incompleteStream, UTF_8);
        assertEquals("Default charset should be returned for incomplete BOM", UTF_8, result);
    }
    
    @Test
    public void testLargeInputStreamWithoutBom() throws IOException {
        byte[] largeData = new byte[5000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte)(i % 128);
        }
        InputStream largeStream = new ByteArrayInputStream(largeData);
        String result = DataUtil.detectCharset(largeStream, UTF_8);
        assertEquals("Default charset should be returned for large stream without BOM", UTF_8, result);
    }
    
    @Test
    public void testLargeInputStreamWithUtf8Bom() throws IOException {
        byte[] largeDataWithBom = new byte[5003];
        largeDataWithBom[0] = (byte)0xEF;
        largeDataWithBom[1] = (byte)0xBB;
        largeDataWithBom[2] = (byte)0xBF;
        for (int i = 3; i < largeDataWithBom.length; i++) {
            largeDataWithBom[i] = (byte)((i - 3) % 128);
        }
        InputStream largeBomStream = new ByteArrayInputStream(largeDataWithBom);
        String result = DataUtil.detectCharset(largeBomStream, UTF_8);
        assertEquals("UTF-8 should be detected for large stream with BOM", UTF_8, result);
    }
    
    @Test
    public void testStreamWithMultipleBoms() throws IOException {
        byte[] multipleBomData = {(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)0xEF, (byte)0xBB, (byte)0xBF, 0x48};
        InputStream multipleBomStream = new ByteArrayInputStream(multipleBomData);
        String result = DataUtil.detectCharset(multipleBomStream, UTF_8);
        assertEquals("UTF-8 should be detected even with multiple BOMs", UTF_8, result);
    }
    
    @Test(expected = NullPointerException.class)
    public void testDetectCharsetWithNullCharset() throws IOException {
        byte[] data = {0x48, 0x65};
        InputStream stream = new ByteArrayInputStream(data);
        DataUtil.detectCharset(stream, null);
    }
    
    @Test
    public void testMaxBufferSizeStream() throws IOException {
        byte[] maxBufferData = new byte[32768];
        for (int i = 0; i < maxBufferData.length; i++) {
            maxBufferData[i] = (byte)(i % 256);
        }
        InputStream maxBufferStream = new ByteArrayInputStream(maxBufferData);
        String result = DataUtil.detectCharset(maxBufferStream, UTF_8);
        assertEquals("Default charset should be returned for max buffer size stream", UTF_8, result);
    }
    
    @Test
    public void testReadToByteBuffer() throws IOException {
        byte[] testData = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
        InputStream stream = new ByteArrayInputStream(testData);
        java.nio.ByteBuffer buffer = DataUtil.readToByteBuffer(stream);
        assertNotNull("ByteBuffer should not be null", buffer);
        assertEquals("Buffer should contain 5 bytes", 5, buffer.remaining());
        assertEquals("First byte should be 'H'", 0x48, buffer.get());
    }
    
    @Test
    public void testReadToByteBufferWithLargeData() throws IOException {
        byte[] testData = new byte[10000];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte)(i % 256);
        }
        InputStream stream = new ByteArrayInputStream(testData);
        java.nio.ByteBuffer buffer = DataUtil.readToByteBuffer(stream);
        assertNotNull("ByteBuffer should not be null", buffer);
        assertEquals("Buffer should contain 10000 bytes", 10000, buffer.remaining());
    }
    
    @Test
    public void testReadToByteBufferWithEmptyStream() throws IOException {
        byte[] emptyData = new byte[0];
        InputStream emptyStream = new ByteArrayInputStream(emptyData);
        java.nio.ByteBuffer buffer = DataUtil.readToByteBuffer(emptyStream);
        assertNotNull("ByteBuffer should not be null", buffer);
        assertEquals("Buffer should be empty", 0, buffer.remaining());
    }
    
    @Test
    public void testFirstByteBufferRead() throws IOException {
        byte[] testData = new byte[32768];
        for (int i = 0; i < testData.length; i++) {
            testData[i] = (byte)(i % 256);
        }
        InputStream stream = new ByteArrayInputStream(testData);
        java.nio.ByteBuffer buffer = DataUtil.readToByteBuffer(stream);
        assertNotNull("ByteBuffer should not be null", buffer);
        assertTrue("Buffer should have data", buffer.remaining() > 0);
    }
    
    @Test(expected = IOException.class)
    public void testDetectCharsetWithFailingStream() throws IOException {
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read failure");
            }
        };
        DataUtil.detectCharset(failingStream, UTF_8);
    }
    
    @Test
    public void testReadToByteBufferWithMarkReset() throws IOException {
        byte[] testData = {0x01, 0x02, 0x03, 0x04, 0x05};
        InputStream stream = new ByteArrayInputStream(testData);
        java.nio.ByteBuffer buffer = DataUtil.readToByteBuffer(stream);
        byte[] result = new byte[buffer.remaining()];
        buffer.get(result);
        assertArrayEquals("Data should match", new byte[]{0x01, 0x02, 0x03, 0x04, 0x05}, result);
    }
}