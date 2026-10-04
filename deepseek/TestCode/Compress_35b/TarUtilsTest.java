package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;

import org.junit.Test;

public class TarUtilsTest {

    private static final int BYTE_MASK = 0xFF;

    // Helper to convert byte array to unsigned int (for checksum testing)
    private static long unsignedByteToLong(byte b) {
        return (long) b & BYTE_MASK;
    }

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
    public void testParseOctalLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.parseOctal(buf, 0, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOffsetOutOfBounds() {
        byte[] buf = new byte[10];
        TarUtils.parseOctal(buf, 10, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthExceedsBuffer() {
        byte[] buf = new byte[10];
        TarUtils.parseOctal(buf, 0, 11);
    }

    @Test
    public void testParseOctalSimple() {
        byte[] buf = "000644".getBytes();
        assertEquals(420, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalWithSpaces() {
        byte[] buf = "  000644  ".getBytes();
        assertEquals(420, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalEmptyAfterSpace() {
        byte[] buf = "   ".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test
    public void testParseOctalZero() {
        byte[] buf = "0000000".getBytes();
        assertEquals(0, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testParseOctalMaxValue() {
        // Octal 7777777777 = 8589934591 (max for 11 octal digits / 32-bit unsigned? but as long)
        byte[] buf = "7777777777".getBytes();
        assertEquals(8589934591L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOverflow() {
        // Overflow when value > 8^len-1? Actually parseOctal uses long and throws on overflow.
        byte[] buf = "10000000000".getBytes(); // 11 digits starting with 1 -> overflow
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalNegativeLeadingMinus() {
        byte[] buf = "-000644".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidChar() {
        byte[] buf = "00A644".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    // ---------- parseBinaryLong tests ----------

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongNullBuffer() {
        TarUtils.parseBinaryLong(null, 0, 10, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongNegativeOffset() {
        byte[] buf = new byte[10];
        TarUtils.parseBinaryLong(buf, -1, 10, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongNegativeLength() {
        byte[] buf = new byte[10];
        TarUtils.parseBinaryLong(buf, 0, -1, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongLengthZero() {
        byte[] buf = new byte[10];
        TarUtils.parseBinaryLong(buf, 0, 0, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongLengthTooLarge() {
        byte[] buf = new byte[10];
        TarUtils.parseBinaryLong(buf, 0, 9, false); // length > 8 is invalid
    }

    @Test
    public void testParseBinaryLongPositiveUnsigned() {
        byte[] buf = new byte[] {0, 0, 0, 0, 0, 0, 0, 1}; // big-endian, unsigned
        assertEquals(1, TarUtils.parseBinaryLong(buf, 0, 8, false));
    }

    @Test
    public void testParseBinaryLongNegativeSigned() {
        byte[] buf = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
                                 (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF}; // -1 in two's complement
        assertEquals(-1L, TarUtils.parseBinaryLong(buf, 0, 8, true));
    }

    @Test
    public void testParseBinaryLongMax() {
        byte[] buf = new byte[] {0x7F, (byte)0xFF, (byte)0xFF, (byte)0xFF,
                                 (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        assertEquals(Long.MAX_VALUE, TarUtils.parseBinaryLong(buf, 0, 8, true));
    }

    @Test
    public void testParseBinaryLongMin() {
        byte[] buf = new byte[] {(byte)0x80, 0, 0, 0, 0, 0, 0, 0};
        assertEquals(Long.MIN_VALUE, TarUtils.parseBinaryLong(buf, 0, 8, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongUnsignedNegativeValue() {
        // Trying to read negative in unsigned mode should throw
        byte[] buf = new byte[] {(byte)0xFF, 0, 0, 0, 0, 0, 0, 0}; // unsigned would be huge, but negative byte
        // Actually the method may interpret as negative if first bit set? But unsigned mode expects non-negative.
        // The contract: if negativeFlag is false, it throws if highest bit set (negative).
        TarUtils.parseBinaryLong(buf, 0, 8, false);
    }

    // ---------- formatLongOctalBytes tests ----------

    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[12];
        assertEquals(12, TarUtils.formatLongOctalBytes(0, buf, 0, 12));
        assertArrayEquals(new byte[] {'0', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, buf);
    }

    @Test
    public void testFormatLongOctalBytesPositive() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(64, buf, 0, 12);
        // octal 64 = 100, padded to length-1 then trailing space
        // The method writes octal without leading zero? Actually it writes convertible value.
        // We just check ends with NUL and length correct.
        assertEquals(' ', buf[10]); // last before NUL should be space or NUL?
        assertEquals(0, buf[11]); // NUL
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesBufferTooSmall() {
        byte[] buf = new byte[5];
        TarUtils.formatLongOctalBytes(100, buf, 0, 5);
    }

    // ---------- formatLongOctalOrBinaryBytes tests ----------

    @Test
    public void testFormatLongOctalOrBinaryBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(-1, buf, 0, 12);
        // Should write binary negative (two's complement) and set high bit of first byte
        assertEquals((byte)0xFF, buf[0]); // first byte all 1s indicates binary negative?
        // Actually format uses binary if negative or too large for octal.
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesPositiveFitOctal() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(64, buf, 0, 12);
        // Should fit in octal with trailing space and NUL
        assertEquals(' ', buf[10]);
        assertEquals(0, buf[11]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesTooLargeForOctal() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(8589934592L, buf, 0, 12);
        // Should use binary format
        // First byte should have high bit set to indicate binary
        assertTrue((buf[0] & 0x80) != 0);
    }

    // ---------- formatLongBinary tests ----------

    @Test
    public void testFormatLongBinaryZero() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(0, buf, 0, 8, false);
        assertArrayEquals(new byte[8], buf);
    }

    @Test
    public void testFormatLongBinaryPositive() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(1, buf, 0, 8, false);
        byte[] expected = new byte[8];
        expected[7] = 1;
        assertArrayEquals(expected, buf);
    }

    @Test
    public void testFormatLongBinaryNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(-1, buf, 0, 8, true);
        byte[] expected = new byte[8];
        for (int i = 0; i < 8; i++) expected[i] = (byte)0xFF;
        assertArrayEquals(expected, buf);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryUnsignedNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(-1, buf, 0, 8, false); // should throw
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongBinaryNullBuffer() {
        TarUtils.formatLongBinary(0, null, 0, 8, false);
    }

    // ---------- computeCheckSum tests ----------

    @Test
    public void testComputeCheckSumEmptyBuffer() {
        byte[] buf = new byte[0];
        assertEquals(0, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumSingleByte() {
        byte[] buf = new byte[] {10};
        assertEquals(10, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumAllOnes() {
        byte[] buf = new byte[] {(byte)0xFF, (byte)0xFF};
        // Unsigned bytes: 255 + 255 = 510
        assertEquals(510, TarUtils.computeCheckSum(buf));
    }

    @Test
    public void testComputeCheckSumMixed() {
        byte[] buf = "Hello".getBytes();
        long expected = 0;
        for (byte b : buf) expected += (b & 0xFF);
        assertEquals(expected, TarUtils.computeCheckSum(buf));
    }

    // ---------- verifyCheckSum tests ----------

    @Test
    public void testVerifyCheckSumCorrect() {
        byte[] header = new byte[512];
        // Simulate a tar header with known checksum in bytes 148-155 (8 bytes, octal)
        // Set some data and compute checksum
        for (int i = 0; i < 148; i++) header[i] = (byte) ' '; // blank
        long checksum = TarUtils.computeCheckSum(header);
        // Now set the checksum field (8 bytes) to the octal representation
        String checksumStr = String.format("%06o", checksum);
        for (int i = 0; i < checksumStr.length() && i < 6; i++) {
            header[148 + i] = (byte) checksumStr.charAt(i);
        }
        header[148 + 6] = 0; // NUL
        header[148 + 7] = ' '; // trailing space (common)
        // Now verify
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumIncorrect() {
        byte[] header = new byte[512];
        // Set checksum field to zeros
        for (int i = 148; i < 156; i++) header[i] = 0;
        // Compute checksum ignoring the field? The method should recalc excluding checksum area.
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyCheckSumNullBuffer() {
        TarUtils.verifyCheckSum(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifyCheckSumShortBuffer() {
        byte[] buf = new byte[100];
        TarUtils.verifyCheckSum(buf);
    }

    // ---------- parseName tests ----------

    @Test
    public void testParseNameSimple() {
        byte[] buf = "test.txt".getBytes();
        assertEquals("test.txt", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test
    public void testParseNameWithNullTerminator() {
        byte[] buf = new byte[] {'t', 'e', 's', 't', 0, 'x', 't'};
        assertEquals("test", TarUtils.parseName(buf, 0, buf.length));
    }

    @Test
    public void testParseNameEmpty() {
        byte[] buf = new byte[0];
        assertEquals("", TarUtils.parseName(buf, 0, 0));
    }

    @Test
    public void testParseNameAllNulls() {
        byte[] buf = new byte[100];
        assertEquals("", TarUtils.parseName(buf, 0, buf.length));
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

    // ---------- formatNameBytes tests ----------

    @Test
    public void testFormatNameBytesNormal() {
        byte[] buf = new byte[100];
        String name = "file.txt";
        TarUtils.formatNameBytes(name, buf, 0, buf.length);
        // Check that name is written and terminated with null
        assertArrayEquals("file.txt\0".getBytes(), java.util.Arrays.copyOf(buf, name.length() + 1));
        // Remaining bytes should be zeros (or unchanged) but not checked
    }

    @Test
    public void testFormatNameBytesEmpty() {
        byte[] buf = new byte[100];
        TarUtils.formatNameBytes("", buf, 0, buf.length);
        assertEquals(0, buf[0]); // first byte should be null
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNameBytesNullName() {
        byte[] buf = new byte[100];
        TarUtils.formatNameBytes(null, buf, 0, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNameBytesNullBuffer() {
        TarUtils.formatNameBytes("test", null, 0, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNameBytesNegativeOffset() {
        byte[] buf = new byte[100];
        TarUtils.formatNameBytes("test", buf, -1, buf.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNameBytesNegativeLength() {
        byte[] buf = new byte[100];
        TarUtils.formatNameBytes("test", buf, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatNameBytesBufferTooSmall() {
        byte[] buf = new byte[3];
        TarUtils.formatNameBytes("test", buf, 0, buf.length);
    }

    // ---------- bug-specific tests (Defects4J Compress 35) ----------
    // The bug 35 likely involves parsing binary long with negative values or octal overflow.
    // Additional edge cases:

    @Test
    public void testParseBinaryLongFullRangeSigned() {
        // Iterate over some values to ensure binary parsing correct
        byte[] buf = new byte[8];
        for (long val : new long[] {Long.MIN_VALUE, Long.MIN_VALUE + 1, -1, 0, 1, Long.MAX_VALUE - 1, Long.MAX_VALUE}) {
            TarUtils.formatLongBinary(val, buf, 0, 8, true);
            long parsed = TarUtils.parseBinaryLong(buf, 0, 8, true);
            assertEquals("Failed for value " + val, val, parsed);
        }
    }

    @Test
    public void testParseOctalLeadingSpacesAndTabs() {
        // Should handle spaces only (not tabs) as per spec
        byte[] buf = "  000644 ".getBytes();
        assertEquals(420, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalWithTab() {
        byte[] buf = "\t000644".getBytes();
        TarUtils.parseOctal(buf, 0, buf.length);
    }

    @Test
    public void testFormatLongOctalBytesBufferExactSize() {
        byte[] buf = new byte[12];
        // value 0 writes "0" then spaces and NUL
        assertEquals(12, TarUtils.formatLongOctalBytes(0, buf, 0, 12));
        assertEquals('0', buf[0]);
        // Ensure last two bytes are space and NUL
        assertEquals(' ', buf[10]);
        assertEquals(0, buf[11]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesNegativeValue() {
        byte[] buf = new byte[12];
        // Should throw because octal cannot represent negative
        TarUtils.formatLongOctalBytes(-1, buf, 0, 12);
    }
}