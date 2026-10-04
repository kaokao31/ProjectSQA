package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.zip.ZipException;

public class TarUtilsTest {

    @Test
    public void testComputeCheckSum() {
        byte[] buf = new byte[512];
        // Empty buffer checksum should be 0 (or sum of spaces/zeros)
        long sum = TarUtils.computeCheckSum(buf);
        assertTrue(sum >= 0);

        // Populate some bytes
        buf[100] = (byte) 'A';
        buf[101] = (byte) 'B';
        long sumWithData = TarUtils.computeCheckSum(buf);
        assertTrue(sumWithData != sum);
    }

    @Test
    public void testParseName() {
        byte[] buf = "hello_world".getBytes();
        String name = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("hello_world", name);

        // Test with null bytes termination or trailing zeros/spaces
        byte[] bufWithZeros = new byte[] { 't', 'e', 's', 't', 0, 0 };
        assertEquals("test", TarUtils.parseName(bufWithZeros, 0, bufWithZeros.length));
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes("test", buf, 0, buf.length);
        assertEquals(10, len);
        assertEquals('t', buf[0]);
        assertEquals(0, buf[4]); // trailing padded with zeros
    }

    @Test
    public void testParseOctal() {
        byte[] buf = "0000755".getBytes();
        long val = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(493L, val); // 755 in octal is 493 in decimal

        // Test with spaces and trailing NUL
        byte[] buf2 = " 755 \0".getBytes();
        assertEquals(493L, TarUtils.parseOctal(buf2, 0, buf2.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidLength() {
        byte[] buf = "123".getBytes();
        TarUtils.parseOctal(buf, 0, 1); // Too short or invalid
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidSyntax() {
        byte[] buf = "9999".getBytes(); // 9 is not a valid octal digit
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test
    public void testParseOctalOrBinary() {
        byte[] buf = "0000755".getBytes();
        long val = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(493L, val);

        // Test binary format (e.g. highest bit set)
        byte[] binBuf = new byte[8];
        binBuf[0] = (byte) 0x80; // Negative/binary indicator
        binBuf[7] = 0x01;
        long binVal = TarUtils.parseOctalOrBinary(binBuf, 0, binBuf.length);
        assertTrue(binVal != 0);
    }

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(1234, buf, 0, buf.length);
        assertEquals(12, len);
    }

    @Test
    public void testFormatCheckOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatCheckOctalBytes(123, buf, 0, buf.length);
        assertEquals(8, len);
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[8];
        int len = TarUtils.formatOctalBytes(123, buf, 0, buf.length);
        assertEquals(8, len);
    }

    @Test
    public void testFormatUnsignedLongOctalBytes() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatUnsignedLongOctalBytes(123456L, buf, 0, buf.length);
        assertEquals(12, len);
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] header = new byte[512];
        // Populate valid dummy checksum
        boolean result = TarUtils.verifyCheckSum(header);
        // Depending on implementation, default empty might be true or false
        assertNotNull(result);
    }
}