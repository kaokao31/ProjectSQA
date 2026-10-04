package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class TarUtilsTest {

    @Test
    public void testParseOctalValid() {
        byte[] buffer = new byte[] {'0','1','2','3','4','5','6','7', 0};
        assertEquals(01234567L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalWithLeadingSpaces() {
        byte[] buffer = new byte[] {' ', ' ', '1', '2', 0};
        assertEquals(012L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalWithLeadingZeros() {
        byte[] buffer = new byte[] {'0', '0', '0', '0', '1', '2', '7', 0};
        assertEquals(0127L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalZero() {
        byte[] buffer = new byte[] {'0', '0', '0', 0};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalEmptyField() {
        byte[] buffer = new byte[] {0, 0, 0, 0};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalSpacesOnly() {
        byte[] buffer = new byte[] {' ', ' ', ' '};
        assertEquals(0L, TarUtils.parseOctal(buffer, 0, buffer.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalLengthTooShort() {
        TarUtils.parseOctal(new byte[] {'0'}, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalInvalidDigit() {
        byte[] buffer = new byte[] {'1', '2', '8', 0};
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalRejectsMissingTrailer() {
        byte[] buffer = new byte[] {'1', '2', '3', '4', '5'};
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalRejectsInvalidTrailerChar() {
        byte[] buffer = new byte[] {'1', '2', '3', '4', 'x'};
        TarUtils.parseOctal(buffer, 0, buffer.length);
    }

    @Test
    public void testParseOctalWithOffset() {
        byte[] buffer = new byte[] {9, 9, '1', '2', 0, 0};
        assertEquals(012L, TarUtils.parseOctal(buffer, 2, 4));
    }

    @Test
    public void testParseOctalOrBinaryOctal() {
        byte[] buffer = new byte[] {'1', '2', 0};
        assertEquals(012L, TarUtils.parseOctalOrBinary(buffer, 0, buffer.length));
    }

    @Test
    public void testParseOctalOrBinaryBinary() {
        byte[] buffer = new byte[] {
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
        };
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buffer, 0, buffer.length));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseOctalOrBinaryRejectsMissingTrailer() {
        byte[] buffer = new byte[] {'1', '2', '3', '4', '5'};
        TarUtils.parseOctalOrBinary(buffer, 0, buffer.length);
    }

    @Test
    public void testParseBinaryPositive() {
        byte[] buffer = new byte[] {0,0,0,0,0,0,0,1};
        assertEquals(1L, TarUtils.parseBinary(buffer, 0, buffer.length));
    }

    @Test
    public void testParseBinaryNegative() {
        byte[] buffer = new byte[] {
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff,
            (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff
        };
        assertEquals(-1L, TarUtils.parseBinary(buffer, 0, buffer.length));
    }

    @Test
    public void testParseBinaryUsesLastEightBytesWhenLengthGreaterThanEight() {
        byte[] buffer = new byte[] {1,2,3,4, 0,0,0,0,0,0,0,1};
        assertEquals(1L, TarUtils.parseBinary(buffer, 0, buffer.length));
    }

    @Test
    public void testParseBinaryWithOffset() {
        byte[] buffer = new byte[] {9, 9, 0,0,0,0,0,0,0,1};
        assertEquals(1L, TarUtils.parseBinary(buffer, 2, 8));
    }

    @Test
    public void testIsOctal() {
        assertTrue(TarUtils.isOctal(new byte[] {'1','2','3',0}, 0, 4));
        assertTrue(TarUtils.isOctal(new byte[] {'1','2','3',' '}, 0, 4));
    }

    @Test
    public void testIsOctalFalse() {
        assertFalse(TarUtils.isOctal(new byte[] {'1','2','8',0}, 0, 4));
        assertFalse(TarUtils.isOctal(new byte[] {'1'}, 0, 1));
        assertFalse(TarUtils.isOctal(new byte[0], 0, 0));
    }

    @Test
    public void testParseName() {
        byte[] buffer = "Hello\0world".getBytes(StandardCharsets.US_ASCII);
        assertEquals("Hello", TarUtils.parseName(buffer, 0, buffer.length));
    }

    @Test
    public void testParseNameNoNull() {
        byte[] buffer = "Hello".getBytes(StandardCharsets.US_ASCII);
        assertEquals("Hello", TarUtils.parseName(buffer, 0, buffer.length));
    }

    @Test
    public void testParseNameWithOffset() {
        byte[] buffer = new byte[] {9,9,'H','i',0,9};
        assertEquals("Hi", TarUtils.parseName(buffer, 2, 4));
    }

    @Test
    public void testParseNameEmpty() {
        assertEquals("", TarUtils.parseName(new byte[0], 0, 0));
        assertEquals("", TarUtils.parseName(new byte[] {0, 1, 2}, 0, 3));
    }

    @Test
    public void testFormatNameShorterThanField() {
        byte[] buf = new byte[5];
        TarUtils.formatName("Hi", buf, 0, buf.length);
        assertEquals("Hi\u0000\u0000\u0000", new String(buf, StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testFormatNameLongerThanField() {
        byte[] buf = new byte[5];
        TarUtils.formatName("HelloWorld", buf, 0, buf.length);
        assertEquals("Hell\u0000", new String(buf, StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testFormatNameWithOffset() {
        byte[] buf = new byte[7];
        TarUtils.formatName("Hi", buf, 2, 5);
        assertEquals("Hi\u0000\u0000\u0000", new String(buf, 2, 5, StandardCharsets.ISO_8859_1));
    }

    @Test
    public void testFormatUnsignedOctalString() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(8L, buf, 0, buf.length);
        assertEquals("00010", new String(buf, StandardCharsets.US_ASCII));
    }

    @Test
    public void testFormatUnsignedOctalStringZero() {
        byte[] buf = new byte[5];
        TarUtils.formatUnsignedOctalString(0L, buf, 0, buf.length);
        assertEquals("00000", new String(buf, StandardCharsets.US_ASCII));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFormatUnsignedOctalStringTooLarge() {
        byte[] buf = new byte[2];
        TarUtils.formatUnsignedOctalString(64L, buf, 0, buf.length);
    }

    @Test
    public void testFormatUnsignedOctalStringWithOffset() {
        byte[] buf = new byte[6];
        TarUtils.formatUnsignedOctalString(8L, buf, 1, 3);
        assertEquals("010", new String(buf, 1, 3, StandardCharsets.US_ASCII));
    }

    @Test
    public void testFormatOctalBytes() {
        byte[] buf = new byte[6];
        TarUtils.formatOctalBytes(8L, buf, 0, buf.length);
        assertEquals('0', buf[0]);
        assertEquals('0', buf[1]);
        assertEquals('1', buf[2]);
        assertEquals('0', buf[3]);
        assertEquals(' ', buf[4]);
        assertEquals(0, buf[5]);
    }

    @Test
    public void testFormatOctalBytesRoundTrip() {
        byte[] buf = new byte[12];
        TarUtils.formatOctalBytes(8L, buf, 0, buf.length);
        assertEquals(8L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testFormatLongOctalBytesRoundTrip() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(123456789L, buf, 0, buf.length);
        assertEquals(123456789L, TarUtils.parseOctal(buf, 0, buf.length));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesRoundTrip() {
        long[] values = {
            0L, 1L, 8L, 077L, 01234567L, 123456789L,
            8589934592L, Long.MAX_VALUE, -1L, -123456789L
        };
        for (long value : values) {
            byte[] buf = new byte[12];
            TarUtils.formatLongOctalOrBinaryBytes(value, buf, 0, buf.length);
            assertEquals(value, TarUtils.parseOctalOrBinary(buf, 0, buf.length));
        }
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(-1L, buf, 0, buf.length);
        assertEquals(-1L, TarUtils.parseOctalOrBinary(buf, 0, buf.length));
    }

    @Test
    public void testComputeCheckSum() {
        byte[] header = new byte[512];
        header[0] = 1;
        header[1] = 2;
        header[2] = 3;
        assertEquals(6L, TarUtils.computeCheckSum(header));
    }

    @Test
    public void testComputeCheckSumWithNegativeBytes() {
        byte[] header = new byte[512];
        header[0] = (byte) 0xff;
        assertEquals(255L, TarUtils.computeCheckSum(header));
    }

    @Test
    public void testVerifyCheckSum() {
        byte[] header = new byte[512];
        for (int i = 148; i < 156; i++) {
            header[i] = ' ';
        }
        long sum = TarUtils.computeCheckSum(header);
        TarUtils.formatOctalBytes(sum, header, 148, 8);
        assertTrue(TarUtils.verifyCheckSum(header));
    }

    @Test
    public void testVerifyCheckSumInvalid() {
        byte[] header = new byte[512];
        assertFalse(TarUtils.verifyCheckSum(header));
    }
}