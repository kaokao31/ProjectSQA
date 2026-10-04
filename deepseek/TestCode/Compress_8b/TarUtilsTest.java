package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * JUnit 4 test suite for TarUtils, targeting maximum coverage and fault detection.
 */
public class TarUtilsTest {

    // ---------- parseOctal ----------

    @Test
    public void testParseOctalNullBuffer() {
        try {
            TarUtils.parseOctal(null, 0, 10);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
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

    @Test
    public void testParseOctalEmptyBuffer() {
        byte[] buf = new byte[0];
        try {
            TarUtils.parseOctal(buf, 0, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalAllSpaces() {
        byte[] buf = "         ".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        byte[] buf = "  000001234 ".getBytes();
        assertEquals(1234L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalTrailingSpaces() {
        byte[] buf = "000001234   ".getBytes();
        assertEquals(1234L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalNormal() {
        byte[] buf = "000001234".getBytes();
        assertEquals(1234L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalZero() {
        byte[] buf = "0000000000".getBytes();
        assertEquals(0L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalMaxValue() {
        // 12 octal digits max for 64-bit? Actually 21 octal digits for unsigned long, but typical tar uses 12.
        byte[] buf = "777777777777".getBytes();
        assertEquals(0x1FFFFFFFFFL, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidCharacter() {
        byte[] buf = "00000123A4".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeValue() {
        byte[] buf = "-00001234".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test
    public void testParseOctalWithNullTerminator() {
        byte[] buf = "000001234\0".getBytes();
        assertEquals(1234L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalWithEmbeddedNull() {
        byte[] buf = "000\001234".getBytes();
        try {
            TarUtils.parseOctal(buf, 0, buf.length);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- parseName ----------

    @Test
    public void testParseNameNullBuffer() {
        try {
            TarUtils.parseName(null, 0, 10);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
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

    @Test
    public void testParseNameEmptyBuffer() {
        byte[] buf = new byte[0];
        try {
            TarUtils.parseName(buf, 0, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseNameAllNulls() {
        byte[] buf = new byte[10];
        assertEquals("", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test
    public void testParseNameNormal() {
        byte[] buf = "test.txt\0\0\0".getBytes();
        assertEquals("test.txt", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test
    public void testParseNameNoNullTerminator() {
        byte[] buf = "test.txt".getBytes();
        assertEquals("test.txt", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test
    public void testParseNameWithSpaces() {
        byte[] buf = "test file\0".getBytes();
        assertEquals("test file", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test
    public void testParseNameLeadingNulls() {
        byte[] buf = "\0\0test.txt".getBytes();
        assertEquals("", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test
    public void testParseNameEmptyString() {
        byte[] buf = "\0".getBytes();
        assertEquals("", TarUtils.parseName(buf, 0, buf.length));
    }

    // ---------- formatUnsignedOctalString ----------

    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[12];
        TarUtils.formatUnsignedOctalString(0, buf, 0, buf.length);
        assertEquals("000000000000", new String(buf));
    }

    @Test
    public void testFormatUnsignedOctalStringPositive() {
        byte[] buf = new byte[12];
        TarUtils.formatUnsignedOctalString(1234, buf, 0, buf.length);
        assertEquals("000000002322", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringNegativeValue() {
        byte[] buf = new byte[12];
        TarUtils.formatUnsignedOctalString(-1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringValueTooLarge() {
        byte[] buf = new byte[12];
        // 12 octal digits max value is 0x1FFFFFFFFFL, but we test overflow
        TarUtils.formatUnsignedOctalString(0x1FFFFFFFFFL + 1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringBufferTooSmall() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(1234, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringNegativeOffset() {
        byte[] buf = new byte[12];
        TarUtils.formatUnsignedOctalString(1234, buf, -1, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringNegativeLength() {
        byte[] buf = new byte[12];
        TarUtils.formatUnsignedOctalString(1234, buf, 0, -1);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatUnsignedOctalStringNullBuffer() {
        TarUtils.formatUnsignedOctalString(1234, null, 0, 10);
    }

    // ---------- formatOctalBytes ----------

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[12];
        int written = TarUtils.formatOctalBytes(1234, buf, 0, buf.length);
        assertEquals(12, written);
        assertEquals("000000002322 ", new String(buf));
    }

    @Test
    public void testFormatOctalBytesZero() {
        byte[] buf = new byte[12];
        int written = TarUtils.formatOctalBytes(0, buf, 0, buf.length);
        assertEquals(12, written);
        assertEquals("000000000000 ", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesNegativeValue() {
        byte[] buf = new byte[12];
        TarUtils.formatOctalBytes(-1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesValueTooLarge() {
        byte[] buf = new byte[12];
        TarUtils.formatOctalBytes(0x1FFFFFFFFFL + 1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesBufferTooSmall() {
        byte[] buf = new byte[5];
        TarUtils.formatOctalBytes(1234, buf, 0, buf.length);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatOctalBytesNullBuffer() {
        TarUtils.formatOctalBytes(1234, null, 0, 10);
    }

    // ---------- formatLongOctalBytes ----------

    @Test
    public void testFormatLongOctalBytes() {
        byte[] buf = new byte[12];
        int written = TarUtils.formatLongOctalBytes(1234, buf, 0, buf.length);
        assertEquals(12, written);
        assertEquals("000000002322\0", new String(buf));
    }

    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[12];
        int written = TarUtils.formatLongOctalBytes(0, buf, 0, buf.length);
        assertEquals(12, written);
        assertEquals("000000000000\0", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegativeValue() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(-1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesValueTooLarge() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(0x1FFFFFFFFFL + 1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesBufferTooSmall() {
        byte[] buf = new byte[5];
        TarUtils.formatLongOctalBytes(1234, buf, 0, buf.length);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatLongOctalBytesNullBuffer() {
        TarUtils.formatLongOctalBytes(1234, null, 0, 10);
    }

    // ---------- formatCheckSumOctalBytes ----------

    @Test
    public void testFormatCheckSumOctalBytes() {
        byte[] buf = new byte[12];
        int written = TarUtils.formatCheckSumOctalBytes(1234, buf, 0, buf.length);
        assertEquals(12, written);
        assertEquals("000000002322 ", new String(buf));
    }

    @Test
    public void testFormatCheckSumOctalBytesZero() {
        byte[] buf = new byte[12];
        int written = TarUtils.formatCheckSumOctalBytes(0, buf, 0, buf.length);
        assertEquals(12, written);
        assertEquals("000000000000 ", new String(buf));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesNegativeValue() {
        byte[] buf = new byte[12];
        TarUtils.formatCheckSumOctalBytes(-1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesValueTooLarge() {
        byte[] buf = new byte[12];
        TarUtils.formatCheckSumOctalBytes(0x1FFFFFFFFFL + 1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesBufferTooSmall() {
        byte[] buf = new byte[5];
        TarUtils.formatCheckSumOctalBytes(1234, buf, 0, buf.length);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatCheckSumOctalBytesNullBuffer() {
        TarUtils.formatCheckSumOctalBytes(1234, null, 0, 10);
    }

    // ---------- computeCheckSum ----------

    @Test
    public void testComputeCheckSumAllZeros() {
        byte[] buf = new byte[512];
        assertEquals(0L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumAllOnes() {
        byte[] buf = new byte[512];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (byte) 0xFF;
        }
        // Each byte as unsigned is 255, sum = 512 * 255 = 130560
        assertEquals(130560L, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumMixed() {
        byte[] buf = "Hello World!".getBytes();
        long expected = 0;
        for (byte b : buf) {
            expected += (b & 0xFF);
        }
        assertEquals(expected, TarUtils.computeCheckSum(buf));
    }

    @Test(expected = NullPointerException.class)
    public void testComputeCheckSumNullBuffer() {
        TarUtils.computeCheckSum(null);
    }

    // ---------- verifyCheckSum ----------

    @Test
    public void testVerifyCheckSumValid() {
        byte[] buf = new byte[512];
        // Set header checksum field (offset 148, length 8) to computed checksum
        long chk = TarUtils.computeCheckSum(buf);
        TarUtils.formatCheckSumOctalBytes(chk, buf, 148, 8);
        assertTrue(TarUtils.verifyCheckSum(buf));
    }

    @Test
    public void testVerifyCheckSumInvalid() {
        byte[] buf = new byte[512];
        // Set checksum field to something else
        TarUtils.formatCheckSumOctalBytes(0L, buf, 148, 8);
        assertFalse(TarUtils.verifyCheckSum(buf));
    }

    @Test(expected = NullPointerException.class)
    public void testVerifyCheckSumNullBuffer() {
        TarUtils.verifyCheckSum(null);
    }

    // ---------- parseBoolean ----------

    @Test
    public void testParseBooleanTrue() {
        byte[] buf = "1".getBytes();
        assertTrue(TarUtils.parseBoolean(buf, 0, 1));
    }

    @Test
    public void testParseBooleanFalse() {
        byte[] buf = "0".getBytes();
        assertFalse(TarUtils.parseBoolean(buf, 0, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBooleanInvalid() {
        byte[] buf = "2".getBytes();
        TarUtils.parseBoolean(buf, 0, 1);
    }

    @Test(expected = NullPointerException.class)
    public void testParseBooleanNullBuffer() {
        TarUtils.parseBoolean(null, 0, 1);
    }

    // ---------- formatLongOctalOrBinaryBytes ----------

    @Test
    public void testFormatLongOctalOrBinaryBytesOctal() {
        byte[] buf = new byte[12];
        int written = TarUtils.formatLongOctalOrBinaryBytes(1234, buf, 0, buf.length);
        assertEquals(12, written);
        assertEquals("000000002322\0", new String(buf));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesBinary() {
        byte[] buf = new byte[8];
        // Value too large for octal representation in 8 bytes (max octal 8 digits = 0x1FFFFFF)
        long val = 0x1FFFFFFL + 1; // 33554432
        int written = TarUtils.formatLongOctalOrBinaryBytes(val, buf, 0, buf.length);
        assertEquals(8, written);
        // Binary representation: high bit set, then value in big-endian
        assertTrue((buf[0] & 0x80) != 0);
        // Reconstruct value
        long reconstructed = 0;
        for (int i = 0; i < 8; i++) {
            reconstructed = (reconstructed << 8) | (buf[i] & 0xFF);
        }
        // Clear the high bit
        reconstructed &= 0x7FFFFFFFFFFFFFFFL;
        assertEquals(val, reconstructed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-1, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesBufferTooSmall() {
        byte[] buf = new byte[1];
        TarUtils.formatLongOctalOrBinaryBytes(1234, buf, 0, buf.length);
    }

    @Test(expected = NullPointerException.class)
    public void testFormatLongOctalOrBinaryBytesNullBuffer() {
        TarUtils.formatLongOctalOrBinaryBytes(1234, null, 0, 10);
    }

    // ---------- parseOctalOrBinary ----------

    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buf = "000001234".getBytes();
        assertEquals(1234L, TarUtils.parseOctalOrBinary(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalOrBinaryBinary() {
        byte[] buf = new byte[8];
        long val = 0x1FFFFFFL + 1;
        // Write binary representation with high bit set
        buf[0] = (byte) (0x80 | (val >> 56));
        for (int i = 1; i < 8; i++) {
            buf[i] = (byte) (val >> (56 - 8 * i));
        }
        assertEquals(val, TarUtils.parseOctalOrBinary(buf, 0, buf.length));
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

    @Test(expected = NullPointerException.class)
    public void testParseOctalOrBinaryNullBuffer() {
        TarUtils.parseOctalOrBinary(null, 0, 10);
    }

    // ---------- exception edge cases ----------

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.parseOctal(buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseNameLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.parseName(buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.formatUnsignedOctalString(0, buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatOctalBytesLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.formatOctalBytes(0, buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.formatLongOctalBytes(0, buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatCheckSumOctalBytesLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.formatCheckSumOctalBytes(0, buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalOrBinaryBytesLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.formatLongOctalOrBinaryBytes(0, buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.parseOctalOrBinary(buf, 0, 0);
    }
}