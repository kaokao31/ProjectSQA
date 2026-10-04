package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Test;

public class TarUtilsTest {

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[512];
        // Populate some dummy bytes
        for (int i = 0; i < 512; i++) {
            buf[i] = (byte) (i % 256);
        }
        long checksum = TarUtils.computeCheckSum(buf);
        // The checksum should be a deterministic positive/zero value
        assertTrue(checksum >= 0);
    }

    @Test
    public void testParseName() {
        byte[] buf = "hello_world".getBytes();
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("hello_world", name);
    }

    @Test
    public void testParseNameWithOffsetAndLength() {
        byte[] buf = "---hello_world---".getBytes();
        String name = TarUtils.parseName(buf, 3, 11);
        assertEquals("hello_world", name);
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes("test", buf, 0, 10);
        assertEquals(10, len);
        assertEquals('t', buf[0]);
        assertEquals(0, buf[4]);
    }

    @Test
    public void testParseOctal() {
        byte[] buf = "0123".getBytes();
        long val = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, val);
    }

    @Test
    public void testParseOctalWithSpacesAndNuls() {
        byte[] buf = " 123 \0".getBytes();
        long val = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, val);
    }

    @Test
    public void testParseOctalInvalid() {
        // Test parsing invalid octal or empty/whitespace-only buffers if supported safely
        byte[] buf = "".getBytes();
        long val = TarUtils.parseOctal(buf, 0, 0);
        assertEquals(0L, val);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(12345L, buf, 0, 12);
        assertEquals(12, len);
        long parsed = TarUtils.parseOctal(buf, 0, 12);
        assertEquals(12345L, parsed);
    }

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatCheckSumOctalBytes(123L, buf, 0, 8);
        assertEquals(8, len);
        long parsed = TarUtils.parseOctal(buf, 0, 8);
        assertEquals(123L, parsed);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatOctalBytes(55L, buf, 0, 8);
        assertEquals(8, len);
        long parsed = TarUtils.parseOctal(buf, 0, 8);
        assertEquals(55L, parsed);
    }

    @Test
    public void testParseBooleanTrue() {
        byte[] buf = {(byte) 1};
        assertTrue(TarUtils.parseBoolean(buf, 0));
        
        byte[] buf2 = {(byte) '1'};
        assertTrue(TarUtils.parseBoolean(buf2, 0));
    }

    @Test
    public void testParseBooleanFalse() {
        byte[] buf = {(byte) 0};
        assertFalse(TarUtils.parseBoolean(buf, 0));
        
        byte[] buf2 = {(byte) '0'};
        assertFalse(TarUtils.parseBoolean(buf2, 0));
    }

    @Test
    public void testFormatBoolean() {
        byte[] buf = new byte[1];
        int len = TarUtils.formatBoolean(true, buf, 0);
        assertEquals(1, len);
        assertTrue(TarUtils.parseBoolean(buf, 0));

        int len2 = TarUtils.formatBoolean(false, buf, 0);
        assertEquals(1, len2);
        assertFalse(TarUtils.parseBoolean(buf, 0));
    }
}