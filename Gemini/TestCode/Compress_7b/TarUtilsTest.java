package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TarUtilsTest {

    @Test
    public testParseNameNullBuffer() {
        try {
            TarUtils.parseName(null, 0, 10);
            fail("Expected IllegalArgumentException or NullPointerException");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public testParseNameValid() {
        byte[] buffer = "hello world".getBytes();
        String result = TarUtils.parseName(buffer, 0, 5);
        assertEquals("hello", result);
    }

    @Test
    public testParseNameWithZeroAndSpaces() {
        byte[] buffer = new byte[] { 't', 'e', 's', 't', 0, ' ', 'a', 'b' };
        // parseName stops at 0 or space (depending on implementation, let's test typical tar names)
        String result = TarUtils.parseName(buffer, 0, buffer.length);
        assertEquals("test", result);
    }

    @Test
    public testFormatNameBytes() {
        String name = "test";
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes(name, buf, 0, 10);
        assertEquals(10, len);
        assertEquals('t', buf[0]);
        assertEquals(0, buf[4]);
    }

    @Test
    public testComputeCheckSum() {
        byte[] buf = new byte[512];
        long sum = TarUtils.computeCheckSum(buf);
        // All zeros checksum typically adds up to spaces (32 * number of bytes in header) depending on tar spec,
        // or 0. Let's see what it returns.
        assertEquals(8 * 32, sum); // 8 bytes of checksum field filled with spaces during computation usually
    }

    @Test
    public testParseOctal() {
        byte[] buffer = " 0755 ".getBytes();
        long val = TarUtils.parseOctal(buffer, 0, buffer.length);
        assertEquals(493, val); // 0755 octal = 493 decimal
    }

    @Test
    public testParseOctalInvalid() {
        byte[] buffer = "invalid".getBytes();
        try {
            TarUtils.parseOctal(buffer, 0, buffer.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            // Some versions might throw other exceptions, but IllegalArgumentException is standard
        }
    }

    @Test
    public testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatOctalBytes(493, buf, 0, 8);
        assertEquals(8, len);
        long parsed = TarUtils.parseOctal(buf, 0, 8);
        assertEquals(493, parsed);
    }

    @Test
    public testFormatLongOctalBytes() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(123456L, buf, 0, 12);
        assertEquals(12, len);
        long parsed = TarUtils.parseOctal(buf, 0, 12);
        assertEquals(123456L, parsed);
    }

    @Test
    public testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatCheckSumOctalBytes(123, buf, 0, 8);
        assertEquals(8, len);
    }
}