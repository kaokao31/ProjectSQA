package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Test;

public class TarUtilsTest {

    // ========== parseOctal ==========

    @Test
    public void testParseOctalNormal() {
        byte[] buf = "000123 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83, result);
    }

    @Test
    public void testParseOctalLeadingZeros() {
        byte[] buf = "000000 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeValue() {
        byte[] buf = "-0001 ".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidCharacter() {
        byte[] buf = "00a123 ".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNullByteInMiddle() {
        byte[] buf = new byte[]{'0', '0', '\0', '1', '2', ' '};
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNullByteAtEnd() {
        byte[] buf = new byte[]{'0', '0', '1', '2', '3', '\0'};
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalEmptyBuffer() {
        byte[] buf = new byte[0];
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalBufferTooShort() {
        byte[] buf = "12 ".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOffsetOutOfBounds() {
        byte[] buf = "000123 ".getBytes();
        TarUtils.parseOctal(buf, 10, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthNegative() {
        byte[] buf = "000123 ".getBytes();
        TarUtils.parseOctal(buf, 0, -1);
    }

    // ========== parseOctalOrBinary ==========

    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buf = "000123 ".getBytes();
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(83, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositive() {
        byte[] buf = new byte[]{0, 0, 0, 0, 0, 0, 0, 0x7f};
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(127, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegative() {
        byte[] buf = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(-1, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryInvalid() {
        byte[] buf = "not octal".getBytes();
        TarUtils.parseOctalOrBinary(buf, 0, buf.length);
    }

    // ========== parseName ==========

    @Test
    public void testParseNameNormal() {
        byte[] buf = "test.txt\0".getBytes();
        String result = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("test.txt", result);
    }

    @Test
    public void testParseNameNoNull() {
        byte[] buf = "test.txt".getBytes();
        String result = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("test.txt", result);
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buf = new byte[0];
        String result = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("", result);
    }

    @Test
    public void testParseNameAllNulls() {
        byte[] buf = new byte[]{0, 0, 0};
        String result = TarUtils.parseName(buf, 0, buf.length);
        assertEquals("", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNameNegativeOffset() {
        byte[] buf = "test.txt".getBytes();
        TarUtils.parseName(buf, -1, buf.length);
    }

    // ========== formatLongOctalBytes ==========

    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(0, buf, 0, buf.length);
        assertEquals(12, len);
        assertEquals("00000000000 ", new String(buf));
    }

    @Test
    public void testFormatLongOctalBytesPositive() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(123, buf, 0, buf.length);
        assertEquals(12, len);
        assertEquals("00000000173 ", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(-1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesTooLarge() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(1L << 33, buf, 0, buf.length);
    }

    // ========== formatLongOctalOrBinaryBytes ==========

    @Test
    public void testFormatLongOctalOrBinaryBytesOctal() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalOrBinaryBytes(100, buf, 0, buf.length);
        assertEquals(12, len);
        assertEquals("00000000144 ", new String(buf));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryPositive() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalOrBinaryBytes(1L << 33, buf, 0, buf.length);
        assertEquals(12, len);
        // binary representation: first byte 0x80 indicates binary, rest is big-endian
        assertTrue((buf[0] & 0x80) != 0);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryNegative() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalOrBinaryBytes(-1, buf, 0, buf.length);
        assertEquals(12, len);
        assertTrue((buf[0] & 0x80) != 0);
    }

    // ========== formatLongBinary ==========

    @Test
    public void testFormatLongBinaryZero() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(0, buf, 0, buf.length);
        byte[] expected = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        assertArrayEquals(expected, buf);
    }

    @Test
    public void testFormatLongBinaryPositive() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(255, buf, 0, buf.length);
        byte[] expected = new byte[]{0, 0, 0, 0, 0, 0, 0, (byte) 0xff};
        assertArrayEquals(expected, buf);
    }

    @Test
    public void testFormatLongBinaryNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(-1, buf, 0, buf.length);
        byte[] expected = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
        assertArrayEquals(expected, buf);
    }

    // ========== formatNameBytes ==========

    @Test
    public void testFormatNameBytesNormal() {
        byte[] buf = new byte[100];
        int len = TarUtils.formatNameBytes("test.txt", buf, 0, buf.length);
        assertEquals(8, len);
        assertEquals("test.txt", new String(buf, 0, 8));
        assertEquals('\0', buf[8]);
    }

    @Test
    public void testFormatNameBytesTruncated() {
        byte[] buf = new byte[5];
        int len = TarUtils.formatNameBytes("longname.txt", buf, 0, buf.length);
        assertEquals(5, len);
        assertEquals("longn", new String(buf, 0, 5));
    }

    @Test
    public void testFormatNameBytesEmpty() {
        byte[] buf = new byte[10];
        int len = TarUtils.formatNameBytes("", buf, 0, buf.length);
        assertEquals(0, len);
        assertEquals('\0', buf[0]);
    }

    // ========== Additional edge cases for known bug (Defects4J bug 14) ==========

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalWithNullByteBeforeEnd() {
        // This is the specific bug: buffer with NUL before the end should throw
        byte[] buf = new byte[]{'0', '0', '1', '2', '\0', ' '};
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalWithOnlyNullAndSpace() {
        byte[] buf = new byte[]{'\0', ' '};
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalWithTrailingNullNoSpace() {
        byte[] buf = new byte[]{'0', '0', '1', '\0'};
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test
    public void testParseOctalWithLeadingSpaces() {
        byte[] buf = "   123 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalWithOnlySpaces() {
        byte[] buf = "      ".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalWithNullBuffer() {
        TarUtils.parseOctal(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryWithNullBuffer() {
        TarUtils.parseOctalOrBinary(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNameWithNullBuffer() {
        TarUtils.parseName(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesWithNullBuffer() {
        TarUtils.formatLongOctalBytes(0, null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesWithNullBuffer() {
        TarUtils.formatLongOctalOrBinaryBytes(0, null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryWithNullBuffer() {
        TarUtils.formatLongBinary(0, null, 0, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNameBytesWithNullBuffer() {
        TarUtils.formatNameBytes("test", null, 0, 10);
    }
}