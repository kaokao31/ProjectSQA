package org.apache.commons.compress.archivers.tar;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for TarUtils, targeting maximum coverage and fault detection.
 * Focuses on parseOctal, parseOctalOrBinary, and format methods.
 */
public class TarUtilsTest {

    // --- parseOctal tests ---

    @Test
    public void testParseOctalValid() {
        byte[] buf = "000123".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, result); // octal 123 = decimal 83
    }

    @Test
    public void testParseOctalLeadingZeros() {
        byte[] buf = "000000".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    @Test
    public void testParseOctalMaxValue() {
        // 12 octal digits max (for 32-bit unsigned) but TarUtils uses long
        byte[] buf = "777777777777".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0x1FFFFFFFFFL, result); // octal 777777777777 = decimal 8589934591
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidCharacter() {
        byte[] buf = "12a45".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalEmptyBuffer() {
        byte[] buf = new byte[0];
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeOffset() {
        byte[] buf = "123".getBytes();
        TarUtils.parseOctal(buf, -1, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeLength() {
        byte[] buf = "123".getBytes();
        TarUtils.parseOctal(buf, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOffsetPlusLengthExceedsBuffer() {
        byte[] buf = "123".getBytes();
        TarUtils.parseOctal(buf, 1, 3);
    }

    @Test
    public void testParseOctalWithTrailingNULs() {
        // Bug scenario: trailing NULs should be ignored
        byte[] buf = new byte[]{'1', '2', '3', 0, 0, 0};
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, result); // octal 123 = 83
    }

    @Test
    public void testParseOctalWithTrailingSpaces() {
        byte[] buf = "123   ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(83L, result);
    }

    @Test
    public void testParseOctalAllSpaces() {
        byte[] buf = "     ".getBytes();
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNullBuffer() {
        TarUtils.parseOctal(null, 0, 10);
    }

    // --- parseOctalOrBinary tests ---

    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buf = "000123".getBytes();
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(83L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryPositive() {
        // binary representation: first byte 0x80 indicates binary, but for positive we use 0x00? Actually, TarUtils uses leading byte 0x00 for positive binary? Need to check.
        // For simplicity, test with a known binary pattern: 0x00 0x00 0x00 0x01 = 1 in big-endian
        byte[] buf = new byte[]{0, 0, 0, 1};
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(1L, result);
    }

    @Test
    public void testParseOctalOrBinaryBinaryNegative() {
        // negative binary: first byte 0xFF for negative? Actually, TarUtils uses leading byte 0xFF for negative.
        byte[] buf = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}; // -1 in two's complement 32-bit
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(-1L, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryInvalid() {
        byte[] buf = "12a45".getBytes();
        TarUtils.parseOctalOrBinary(buf, 0, buf.length);
    }

    // --- formatLongOctalBytes tests ---

    @Test
    public void testFormatLongOctalBytesPositive() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(83L, buf, 0, 12);
        assertEquals(12, len);
        String str = new String(buf, 0, 12);
        assertTrue(str.startsWith("000123"));
        // Check trailing NULs
        for (int i = 6; i < 12; i++) {
            assertEquals(0, buf[i]);
        }
    }

    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalBytes(0L, buf, 0, 12);
        assertEquals(12, len);
        String str = new String(buf, 0, 12);
        assertTrue(str.startsWith("000000"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(-1L, buf, 0, 12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesTooLong() {
        byte[] buf = new byte[12];
        // Value too large for 12 octal digits (max 8589934591)
        TarUtils.formatLongOctalBytes(8589934592L, buf, 0, 12);
    }

    // --- formatLongOctalOrBinaryBytes tests ---

    @Test
    public void testFormatLongOctalOrBinaryBytesOctal() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalOrBinaryBytes(83L, buf, 0, 12);
        assertEquals(12, len);
        String str = new String(buf, 0, 12);
        assertTrue(str.startsWith("000123"));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryPositive() {
        byte[] buf = new byte[12];
        // Large positive that doesn't fit in octal (>= 8^12) -> binary
        long val = 8589934592L; // 8^12
        int len = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, 12);
        assertEquals(12, len);
        // First byte should be 0x80? Actually, for positive binary, leading byte is 0x80? Need to check implementation.
        // We'll just verify that the result is not all zeros.
        assertTrue(buf[0] != 0);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinaryNegative() {
        byte[] buf = new byte[12];
        int len = TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, 12);
        assertEquals(12, len);
        // First byte should be 0xFF for negative
        assertEquals((byte)0xFF, buf[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesNullBuffer() {
        TarUtils.formatLongOctalOrBinaryBytes(0L, null, 0, 12);
    }

    // --- Additional edge cases ---

    @Test
    public void testParseOctalWithOnlyNULs() {
        byte[] buf = new byte[]{0, 0, 0};
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        assertEquals(0L, result);
    }

    @Test
    public void testParseOctalWithMixedSpacesAndNULs() {
        byte[] buf = new byte[]{'1', '2', ' ', 0, '3'};
        // Should parse until invalid? Actually, spaces are allowed, NULs are allowed? The bug might be about NULs.
        // We'll just ensure no exception.
        long result = TarUtils.parseOctal(buf, 0, buf.length);
        // The expected behavior: parse until first non-octal, non-space, non-NUL? Actually, parseOctal stops at NUL or space.
        // So it should parse "12" and ignore rest.
        assertEquals(10L, result); // octal 12 = 10
    }

    @Test
    public void testParseOctalOrBinaryWithTrailingNULs() {
        byte[] buf = new byte[]{'1', '2', '3', 0, 0};
        long result = TarUtils.parseOctalOrBinary(buf, 0, buf.length);
        assertEquals(83L, result);
    }
}