package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Test;

public class TarUtilsTest {

    // ---------- parseOctal tests ----------
    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNullBuffer() {
        TarUtils.parseOctal(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeOffset() {
        byte[] buf = new byte[10];
        TarUtils.parseOctal(buf, -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeLength() {
        byte[] buf = new byte[10];
        TarUtils.parseOctal(buf, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOffsetPlusLengthExceedsBuffer() {
        byte[] buf = new byte[10];
        TarUtils.parseOctal(buf, 5, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalEmptyBuffer() {
        byte[] buf = new byte[0];
        TarUtils.parseOctal(buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalAllZeros() {
        byte[] buf = new byte[12];
        TarUtils.parseOctal(buf, 0, 12);
    }

    @Test
    public void testParseOctalSimpleValue() {
        byte[] buf = "00000123 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(123L, result);
    }

    @Test
    public void testParseOctalWithLeadingSpaces() {
        byte[] buf = "  0000123 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(123L, result);
    }

    @Test
    public void testParseOctalWithTrailingSpaces() {
        byte[] buf = "0000123   ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(123L, result);
    }

    @Test
    public void testParseOctalMaxValue() {
        byte[] buf = "7777777777 ".getBytes(); // octal 7777777777 = 2^33 - 1? Actually 8^10-1 = 1073741823
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(1073741823L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOverflow() {
        // value > 8^11? Actually parseOctal uses long, but if buffer contains more than 11 octal digits? Let's test with 12 digits
        byte[] buf = "777777777777 ".getBytes(); // 12 octal digits
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidCharacter() {
        byte[] buf = "0000A123 ".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeValue() {
        // Negative values are not allowed in octal representation
        byte[] buf = "-0000123 ".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test
    public void testParseOctalZeroLength() {
        byte[] buf = new byte[10];
        long result = TarUtils.parseOctal(buf, 0, 0);
        assertEquals(0L, result);
    }

    // ---------- parseBoolean tests ----------
    @Test
    public void testParseBooleanTrue() {
        assertTrue(TarUtils.parseBoolean(new byte[]{'1'}));
    }

    @Test
    public void testParseBooleanFalse() {
        assertFalse(TarUtils.parseBoolean(new byte[]{'0'}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBooleanInvalid() {
        TarUtils.parseBoolean(new byte[]{'x'});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBooleanNull() {
        TarUtils.parseBoolean(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBooleanEmpty() {
        TarUtils.parseBoolean(new byte[0]);
    }

    // ---------- parseName tests ----------
    @Test
    public void testParseNameSimple() {
        byte[] buf = "hello\0world".getBytes();
        String result = TarUtils.parseName(buf, 0, 10);
        assertEquals("hello", result);
    }

    @Test
    public void testParseNameNoNull() {
        byte[] buf = "hello".getBytes();
        String result = TarUtils.parseName(buf, 0, 5);
        assertEquals("hello", result);
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buf = new byte[10];
        String result = TarUtils.parseName(buf, 0, 10);
        assertEquals("", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNameNullBuffer() {
        TarUtils.parseName(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNameNegativeOffset() {
        byte[] buf = new byte[10];
        TarUtils.parseName(buf, -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNameNegativeLength() {
        byte[] buf = new byte[10];
        TarUtils.parseName(buf, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNameOffsetPlusLengthExceedsBuffer() {
        byte[] buf = new byte[10];
        TarUtils.parseName(buf, 5, 10);
    }

    // ---------- formatLongOctalBytes tests ----------
    @Test
    public void testFormatLongOctalBytesPositive() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(123L, buf, 0, 12);
        assertEquals(12, len);
        String str = new String(buf, 0, 12);
        assertTrue(str.startsWith("00000000173")); // 123 octal = 173
        assertTrue(str.endsWith(" "));
    }

    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(0L, buf, 0, 12);
        assertEquals(12, len);
        String str = new String(buf, 0, 12);
        assertTrue(str.startsWith("00000000000"));
        assertTrue(str.endsWith(" "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(-1L, buf, 0, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesBufferTooSmall() {
        byte[] buf = new byte[5];
        TarUtils.formatLongOctalBytes(123L, buf, 0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNullBuffer() {
        TarUtils.formatLongOctalBytes(123L, null, 0, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegativeOffset() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(123L, buf, -1, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegativeLength() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(123L, buf, 0, -1);
    }

    // ---------- formatUnsignedOctalString tests ----------
    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buf = new byte[10];
        TarUtils.formatUnsignedOctalString(123L, buf, 0, 10);
        String str = new String(buf, 0, 10);
        assertEquals("173       ", str); // 123 octal = 173, padded with spaces
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringNegative() {
        byte[] buf = new byte[10];
        TarUtils.formatUnsignedOctalString(-1L, buf, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringBufferTooSmall() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(123L, buf, 0, 2);
    }

    // ---------- parseOctalOrBinary tests (if exists) ----------
    // Assuming TarUtils has parseOctalOrBinary method (common in compress)
    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buf = "00000123 ".getBytes();
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(123L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositive() {
        // Binary representation: first byte 0x80 indicates binary, then big-endian long
        byte[] buf = new byte[12];
        buf[0] = (byte) 0x80;
        // write 123 as big-endian long in last 8 bytes
        long val = 123L;
        for (int i = 11; i >= 4; i--) {
            buf[i] = (byte) (val & 0xff);
            val >>= 8;
        }
        long result = TarUtils.parseOctalOrBinary(buf, 0, 12);
        assertEquals(123L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegative() {
        byte[] buf = new byte[12];
        buf[0] = (byte) 0xFF; // indicates negative binary
        long val = -123L;
        for (int i = 11; i >= 4; i--) {
            buf[i] = (byte) (val & 0xff);
            val >>= 8;
        }
        long result = TarUtils.parseOctalOrBinary(buf, 0, 12);
        assertEquals(-123L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryNull() {
        TarUtils.parseOctalOrBinary(null, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryNegativeOffset() {
        byte[] buf = new byte[10];
        TarUtils.parseOctalOrBinary(buf, -1, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryNegativeLength() {
        byte[] buf = new byte[10];
        TarUtils.parseOctalOrBinary(buf, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryOffsetPlusLengthExceedsBuffer() {
        byte[] buf = new byte[10];
        TarUtils.parseOctalOrBinary(buf, 5, 10);
    }

    // ---------- formatLongOctalOrBinaryBytes tests ----------
    @Test
    public void testFormatLongOctalOrBinaryBytesOctal() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalOrBinaryBytes(123L, buf, 0, 12);
        assertEquals(12, len);
        String str = new String(buf, 0, 12);
        assertTrue(str.startsWith("00000000173"));
        assertTrue(str.endsWith(" "));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryPositive() {
        byte[] buf = new byte[12];
        // value too large for octal representation ( > 8^11-1 )? Actually 8^11 = 8589934592, so 8^11-1 = 8589934591
        // Use a value that forces binary: e.g., 8589934592L
        long val = 8589934592L;
        int len = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 12);
        assertEquals(12, len);
        // First byte should be 0x80 (binary marker)
        assertEquals((byte) 0x80, buf[0]);
        // Recover value
        long recovered = 0;
        for (int i = 4; i < 12; i++) {
            recovered = (recovered << 8) | (buf[i] & 0xff);
        }
        assertEquals(val, recovered);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryNegative() {
        byte[] buf = new byte[12];
        long val = -123L;
        int len = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 12);
        assertEquals(12, len);
        // First byte should be 0xFF (negative binary marker)
        assertEquals((byte) 0xFF, buf[0]);
        // Recover value (two's complement)
        long recovered = 0;
        for (int i = 4; i < 12; i++) {
            recovered = (recovered << 8) | (buf[i] & 0xff);
        }
        assertEquals(val, recovered);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesNullBuffer() {
        TarUtils.formatLongOctalOrBinaryBytes(123L, null, 0, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesNegativeOffset() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(123L, buf, -1, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesNegativeLength() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(123L, buf, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesBufferTooSmall() {
        byte[] buf = new byte[5];
        TarUtils.formatLongOctalOrBinaryBytes(123L, buf, 0, 5);
    }

    // ---------- parseEntryName tests (if exists) ----------
    // Not always present, but we can test if method exists
    // For safety, we'll skip if not sure.

    // ---------- Additional edge cases ----------
    @Test
    public void testParseOctalWithNullBytesInMiddle() {
        // Buffer with null bytes (0) in the middle - should be treated as end of number?
        byte[] buf = new byte[]{'1', '2', 0, '3', ' ', ' '};
        long result = TarUtils.parseOctal(buf, 0, 6);
        assertEquals(12L, result); // stops at null
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalAllSpaces() {
        byte[] buf = "          ".getBytes();
        TarUtils.parseOctal(buf, 0, 10);
    }

    @Test
    public void testParseOctalSingleDigit() {
        byte[] buf = "5 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, 2);
        assertEquals(5L, result);
    }

    @Test
    public void testParseOctalLeadingZeros() {
        byte[] buf = "0000000000 ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, 11);
        assertEquals(0L, result);
    }
}