package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TarUtilsTest {

    @Test
    public void testComputeCheckSum() {
        byte[] buffer = new byte[512];
        for (int i = 0; i < 512; i++) {
            buffer[i] = (byte) 1;
        }
        long sum = TarUtils.computeCheckSum(buffer);
        assertTrue(sum > 0);
    }

    @Test
    public void testParseName() {
        byte[] buffer = "helloWorld".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("helloWorld", name);
    }

    @Test
    public void testParseNameWithNul() {
        byte[] buffer = "hello\0world".getBytes();
        String name = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("hello", name);
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
        byte[] buffer = "0077".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(63, val);
    }

    @Test
    public void testParseOctalInvalid() {
        byte[] buffer = "invalid".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testParseOctalOrBinary() {
        byte[] buffer = new byte[] { (byte) 0x80, 0, 0, 0, 5 };
        long val = TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
        assertEquals(5, val);

        byte[] bufferLarge = new byte[] { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF };
        try {
            TarUtils.parseOctalOrBinary(bufferLarge, 0, bufferLarge.length);
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
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
    public void testFormatCheckOctalChecksumBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatCheckOctalChecksumBytes(123L, buf, 0, 8);
        assertEquals(8, len);
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
    public void testFormatBinaryLong() {
        byte[] buf = new byte[8];
        TarUtils.formatBinaryLong(100L, buf, 0, 8);
        long val = TarUtils.parseOctalOrBinary(buf, 0, 8);
        assertEquals(100L, val);
    }

    @Test
    public void testFormatBinaryLongNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatBinaryLong(-1L, buf, 0, 8);
        long val = TarUtils.parseOctalOrBinary(buf, 0, 8);
        assertEquals(-1L, val);
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] buffer = new byte[512];
        // Fill with dummy data and format checksum
        long sum = TarUtils.computeCheckSum(buffer);
        TarUtils.formatCheckOctalChecksumBytes(sum, buffer, 148, 8);
        assertTrue(TarUtils.verifyCheckSum(buffer));
    }
}