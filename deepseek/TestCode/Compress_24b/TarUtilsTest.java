package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

public class TarUtilsTest {

    // ---------------------------------------------------------------------
    // parseOctal
    // ---------------------------------------------------------------------

    @Test
    public void testParseOctalEmptyField() {
        byte[] data = new byte[12];
        assertEquals(0L, TarUtils.parseOctal(data, 0, data.length));
    }

    @Test
    public void testParseOctalSimpleValue() {
        byte[] data = "0000123\0".getBytes(StandardCharsets.US_ASCII);
        assertEquals(83L, TarUtils.parseOctal(data, 0, data.length));
    }

    @Test
    public void testParseOctalLeadingSpaces() {
        byte[] data = "  12\0".getBytes(StandardCharsets.US_ASCII);
        assertEquals(10L, TarUtils.parseOctal(data, 0, data.length));
    }

    @Test
    public void testParseOctalStopsAtNul() {
        byte[] data = new byte[] {'1', '2', 0, '3'};
        assertEquals(10L, TarUtils.parseOctal(data, 0, data.length));
    }

    @Test
    public void testParseOctalTrailingSpace() {
        byte[] data = new byte[] {'1', '2', ' '};
        assertEquals(10L, TarUtils.parseOctal(data, 0, data.length));
    }

    @Test
    public void testParseOctalLargeValue() {
        byte[] data = "77777777777\0".getBytes(StandardCharsets.US_ASCII);
        assertEquals(8589934591L, TarUtils.parseOctal(data, 0, data.length));
    }

    @Test
    public void testParseOctalWithOffset() {
        byte[] data = new byte[] {'x', 'x', '1', '2', '3', 0};
        assertEquals(83L, TarUtils.parseOctal(data, 2, 4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalRejectsInvalidDigit() {
        byte[] data = "128\0".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(data, 0, data.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalRejectsNegativeSign() {
        byte[] data = "-1\0".getBytes(StandardCharsets.US_ASCII);
        TarUtils.parseOctal(data, 0, data.length);
    }

    // ---------------------------------------------------------------------
    // parseBinaryLong / parseOctalOrBinary
    // ---------------------------------------------------------------------

    @Test
    public void testParseBinaryLongNegativeTwoBytes() {
        byte[] data = new byte[] {(byte) 0xff, (byte) 0xfe};
        assertEquals(-2L, TarUtils.parseBinaryLong(data, 0, 2, true));
    }

    @Test
    public void testParseBinaryLongNegativeEightBytesMinusOne() {
        byte[] data = new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
        };
        assertEquals(-1L, TarUtils.parseBinaryLong(data, 0, 8, true));
    }

    @Test
    public void testParseBinaryLongNegativeEightBytesMinusTwo() {
        byte[] data = new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe
        };
        assertEquals(-2L, TarUtils.parseBinaryLong(data, 0, 8, true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseBinaryLongLengthTooBig() {
        byte[] data = new byte[9];
        TarUtils.parseBinaryLong(data, 0, 9, true);
    }

    @Test
    public void testParseOctalOrBinaryOctalValue() {
        byte[] data = "0000123\0".getBytes(StandardCharsets.US_ASCII);
        assertEquals(83L, TarUtils.parseOctalOrBinary(data, 0, data.length));
    }

    @Test
    public void testParseOctalOrBinaryNegativeEightBytes() {
        byte[] data = new byte[] {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe
        };
        assertEquals(-2L, TarUtils.parseOctalOrBinary(data, 0, 8));
    }

    @Test
    public void testParseOctalOrBinaryWithOffset() {
        byte[] data = new byte[] {
            0,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xfe
        };
        assertEquals(-2L, TarUtils.parseOctalOrBinary(data, 1, 8));
    }

    // ---------------------------------------------------------------------
    // isBinary
    // ---------------------------------------------------------------------

    @Test
    public void testIsBinaryFalseForOctal() {
        byte[] data = "123\0".getBytes(StandardCharsets.US_ASCII);
        assertFalse(TarUtils.isBinary(data, 0, data.length));
    }

    @Test
    public void testIsBinaryTrueForNegativeMarker() {
        byte[] data = new byte[] {(byte) 0xff, (byte) 0xfe};
        assertTrue(TarUtils.isBinary(data, 0, data.length));
    }

    @Test
    public void testIsBinaryTrueForBinaryMarker() {
        byte[] data = new byte[] {(byte) 0x80, 0, 0, 0, 0, 0, 0, 0};
        assertTrue(TarUtils.isBinary(data, 0, data.length));
    }

    // ---------------------------------------------------------------------
    // formatLongOctalBytes
    // ---------------------------------------------------------------------

    @Test
    public void testFormatLongOctalBytesRoundTrip() {
        long[] values = {0L, 1L, 10L, 83L, 8589934591L};
        for (long value : values) {
            byte[] buf = new byte[12];
            int result = TarUtils.formatLongOctalBytes(value, buf, 0, 12);
            assertEquals(12, result);
            assertEquals(value, TarUtils.parseOctal(buf, 0, 12));
        }
    }

    @Test
    public void testFormatLongOctalBytesWithOffset() {
        byte[] buf = new byte[20];
        TarUtils.formatLongOctalBytes(10L, buf, 4, 12);
        assertEquals(10L, TarUtils.parseOctal(buf, 4, 12));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatLongOctalBytesRejectsNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(-1L, buf, 0, 12);
    }

    // ---------------------------------------------------------------------
    // formatLongBinary / formatLongOctalOrBinaryBytes
    // ---------------------------------------------------------------------

    @Test
    public void testFormatLongBinaryRoundTripNegativeTwoBytes() {
        byte[] buf = new byte[2];
        TarUtils.formatLongBinary(-2L, buf, 0, 2, true);
        assertEquals(-2L, TarUtils.parseBinaryLong(buf, 0, 2, true));
    }

    @Test
    public void testFormatLongBinaryRoundTripNegativeEightBytes() {
        byte[] buf = new byte[8];
        TarUtils.formatLongBinary(-2L, buf, 0, 8, true);
        assertEquals(-2L, TarUtils.parseBinaryLong(buf, 0, 8, true));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesPositiveRoundTrip() {
        for (long value : new long[] {0L, 1L, 12L, 83L}) {
            byte[] buf = new byte[12];
            Arrays.fill(buf, (byte) 0);
            TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, 12);
            assertEquals(value, TarUtils.parseOctalOrBinary(buf, 0, 12));
        }
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesNegativeRoundTrip() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-2L, buf, 0, 8);
        assertEquals(-2L, TarUtils.parseOctalOrBinary(buf, 0, 8));
    }

    // ---------------------------------------------------------------------
    // computeCheckSum / verifyCheckSum
    // ---------------------------------------------------------------------

    @Test
    public void testComputeCheckSum() {
        byte[] data = new byte[] {1, 2, 3};
        assertEquals(6L, TarUtils.computeCheckSum(data));

        byte[] negative = new byte[] {(byte) 0xff};
        assertEquals(255L, TarUtils.computeCheckSum(negative));
    }

    @Test
    public void testVerifyCheckSumTrue() {
        byte[] header = new byte[512];
        long sum = 0;
        for (int i = 0; i < header.length; i++) {
            if (i >= 148 && i < 156) {
                sum += ' ';
            } else {
                sum += header[i] & 0xff;
            }
        }
        TarUtils.formatLongOctalBytes(sum, header, 148, 8);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumFalse() {
        byte[] header = new byte[512];
        assertFalse(TarUtils.verifyCheckSum(header));
    }

    // ---------------------------------------------------------------------
    // parseName / formatName
    // ---------------------------------------------------------------------

    @Test
    public void testParseNameWithNullTerminator() {
        byte[] data = new byte[] {'h', 'e', 'l', 'l', 'o', 0, 'x'};
        assertEquals("hello", TarUtils.parseName(data, 0, data.length));
    }

    @Test
    public void testParseNameWithoutNullTerminator() {
        byte[] data = "hello".getBytes(StandardCharsets.US_ASCII);
        assertEquals("hello", TarUtils.parseName(data, 0, data.length));
    }

    @Test
    public void testFormatName() {
        byte[] buf = new byte[100];
        TarUtils.formatName("hello", buf, 0, buf.length);
        assertEquals("hello\0", new String(buf, 0, 6, StandardCharsets.US_ASCII));
    }
}