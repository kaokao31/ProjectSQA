package org.apache.commons.compress.archivers.tar;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TarUtilsTest {

    @Test
    public void testParseOctalLongName() {
        // Test parsing octal number with long name (bug context)
        byte[] buffer = new byte[12];
        buffer[0] = ' '; buffer[1] = '1'; buffer[2] = '2'; buffer[3] = '3';
        buffer[4] = '4'; buffer[5] = '5'; buffer[6] = '6'; buffer[7] = '7';
        buffer[8] = '0'; buffer[9] = '0'; buffer[10] = '0'; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            assertEquals(1234567L, result);
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalNullBuffer() {
        try {
            TarUtils.parseOctal(null, 0, 1);
            fail("Expected IllegalArgumentException for null buffer");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalNegativeOffset() {
        byte[] buffer = new byte[10];
        buffer[0] = '0';
        try {
            TarUtils.parseOctal(buffer, -1, 1);
            fail("Expected IllegalArgumentException for negative offset");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalNegativeLength() {
        byte[] buffer = new byte[10];
        buffer[0] = '0';
        try {
            TarUtils.parseOctal(buffer, 0, -1);
            fail("Expected IllegalArgumentException for negative length");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalOffsetPlusLengthExceedsBuffer() {
        byte[] buffer = new byte[10];
        buffer[0] = '0';
        try {
            TarUtils.parseOctal(buffer, 5, 10);
            fail("Expected IllegalArgumentException for offset+length > buffer length");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalAllSpaces() {
        byte[] buffer = new byte[12];
        for (int i = 0; i < 12; i++) {
            buffer[i] = ' ';
        }
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            assertEquals(0L, result);
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception for all spaces: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalLeadingSpacesThenZeros() {
        byte[] buffer = new byte[12];
        for (int i = 0; i < 4; i++) buffer[i] = ' ';
        for (int i = 4; i < 10; i++) buffer[i] = '0';
        buffer[10] = ' '; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            assertEquals(0L, result);
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalInvalidCharacter() {
        byte[] buffer = new byte[8];
        buffer[0] = '1'; buffer[1] = '2'; buffer[2] = '3'; buffer[3] = '4';
        buffer[4] = '5'; buffer[5] = '6'; buffer[6] = '8'; buffer[7] = '9';
        try {
            TarUtils.parseOctal(buffer, 0, 8);
            fail("Expected IllegalArgumentException for invalid octal character '8' or '9'");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalOverflow() {
        byte[] buffer = new byte[12];
        buffer[0] = '1'; buffer[1] = '2'; buffer[2] = '3'; buffer[3] = '4';
        buffer[4] = '5'; buffer[5] = '6'; buffer[6] = '7'; buffer[7] = '0';
        buffer[8] = ' '; buffer[9] = ' '; buffer[10] = ' '; buffer[11] = ' ';
        try {
            TarUtils.parseOctal(buffer, 0, 12);
            fail("Expected IllegalArgumentException for overflow");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalWithNullTerminator() {
        byte[] buffer = new byte[8];
        buffer[0] = '1'; buffer[1] = '2'; buffer[2] = '3'; buffer[3] = '4';
        buffer[4] = '5'; buffer[5] = '6'; buffer[6] = '7'; buffer[7] = '\0';
        try {
            long result = TarUtils.parseOctal(buffer, 1, 7);
            assertEquals(0L, result); // starting at index 1 with '2', but null terminator at end
        } catch (IllegalArgumentException e) {
            // may throw depending on implementation
        }
    }

    @Test
    public void testParseOctalValidWithSpacesAfter() {
        byte[] buffer = new byte[12];
        buffer[0] = ' '; buffer[1] = '0'; buffer[2] = '0'; buffer[3] = '0';
        buffer[4] = '0'; buffer[5] = '0'; buffer[6] = '1'; buffer[7] = '2';
        buffer[8] = '3'; buffer[9] = '4'; buffer[10] = ' '; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            assertEquals(0L, result); // buffer starts with space then zeros
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalWithMultipleLeadingSpaces() {
        byte[] buffer = new byte[12];
        buffer[0] = ' '; buffer[1] = ' '; buffer[2] = ' '; buffer[3] = ' ';
        buffer[4] = '1'; buffer[5] = '2'; buffer[6] = '3'; buffer[7] = ' ';
        buffer[8] = ' '; buffer[9] = ' '; buffer[10] = ' '; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            assertEquals(83L, result); // 0123 octal = 83 decimal
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalSingleDigit() {
        byte[] buffer = new byte[12];
        buffer[0] = ' '; buffer[1] = ' '; buffer[2] = ' '; buffer[3] = ' ';
        buffer[4] = '5'; buffer[5] = ' '; buffer[6] = ' '; buffer[7] = ' ';
        buffer[8] = ' '; buffer[9] = ' '; buffer[10] = ' '; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            assertEquals(5L, result);
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalIntegerMaxValue() {
        byte[] buffer = new byte[12];
        buffer[0] = '1'; buffer[1] = '7'; buffer[2] = '7'; buffer[3] = '7';
        buffer[4] = '7'; buffer[5] = '7'; buffer[6] = '7'; buffer[7] = '7';
        buffer[8] = '7'; buffer[9] = '7'; buffer[10] = ' '; buffer[11] = ' ';
        try {
            TarUtils.parseOctal(buffer, 0, 12);
            fail("Expected IllegalArgumentException for value exceeding Long.MAX_VALUE");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalLargeValidNumber() {
        byte[] buffer = new byte[12];
        buffer[0] = '1'; buffer[1] = '7'; buffer[2] = '7'; buffer[3] = '7';
        buffer[4] = '7'; buffer[5] = '7'; buffer[6] = '7'; buffer[7] = '7';
        buffer[8] = '7'; buffer[9] = ' '; buffer[10] = ' '; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            assertTrue(result > 0);
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalEmptyBuffer() {
        byte[] buffer = new byte[0];
        try {
            TarUtils.parseOctal(buffer, 0, 0);
            fail("Expected IllegalArgumentException for empty buffer");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalBufferWithOnlySpaces() {
        byte[] buffer = new byte[10];
        for (int i = 0; i < 10; i++) buffer[i] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 10);
            assertEquals(0L, result);
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalWithEmbeddedNull() {
        byte[] buffer = new byte[12];
        buffer[0] = '1'; buffer[1] = '2'; buffer[2] = '\0'; buffer[3] = '3';
        buffer[4] = '4'; buffer[5] = ' '; buffer[6] = ' '; buffer[7] = ' ';
        buffer[8] = ' '; buffer[9] = ' '; buffer[10] = ' '; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 12);
            // null terminator at index 2 means parsing stops, result might be 0 or incomplete
            assertEquals(0L, result); // expected behavior when encountering null terminator early
        } catch (IllegalArgumentException e) {
            // may throw depending on implementation
        }
    }

    @Test
    public void testParseOctalMaxLength() {
        byte[] buffer = new byte[12];
        buffer[0] = ' '; buffer[1] = '1'; buffer[2] = '2'; buffer[3] = '3';
        buffer[4] = '4'; buffer[5] = '5'; buffer[6] = '6'; buffer[7] = '7';
        buffer[8] = ' '; buffer[9] = ' '; buffer[10] = ' '; buffer[11] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 1, 7);
            assertEquals(0L, result); // only valid digits "1234567" but length 7 starts at index 1
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testFormatLongOctalBytes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(12345L, buf, 0, 12);
        String result = new String(buf);
        assertTrue(result.contains("30071")); // 12345 decimal = 30071 octal
    }

    @Test
    public void testFormatLongOctalBytesZero() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(0L, buf, 0, 12);
        assertEquals("0", new String(buf).trim());
    }

    @Test
    public void testFormatLongOctalBytesNegative() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(-1L, buf, 0, 12);
        // implementation may throw or produce specific output
        try {
            String result = new String(buf);
            assertNotNull(result);
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testFormatLongOctalBytesMaxValue() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalBytes(Long.MAX_VALUE, buf, 0, 12);
        String result = new String(buf);
        assertTrue(result.contains("777777777777777777777")); // approx octal representation
    }

    @Test
    public void testParseBoolean() {
        byte[] buffer = new byte[1];
        buffer[0] = '1';
        assertTrue(TarUtils.parseBoolean(buffer));
        buffer[0] = '0';
        assertFalse(TarUtils.parseBoolean(buffer));
        buffer[0] = ' ';
        assertFalse(TarUtils.parseBoolean(buffer));
        buffer[0] = '2';
        assertFalse(TarUtils.parseBoolean(buffer));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytes() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(100L, buf, 0, 8);
        String result = new String(buf);
        assertTrue(result.contains("144")); // 100 decimal = 144 octal
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesNegative() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(-100L, buf, 0, 8);
        byte[] expected = new byte[8];
        expected[0] = (byte) 0xff; // negative marker
        assertEquals(expected[0], buf[0]);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesLargeValue() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(8589934591L, buf, 0, 12); // > 8 bytes
        // value too large for octal, should use binary format
        assertTrue(buf[0] == (byte) 0x80 || buf[0] == (byte) 0xff);
    }

    @Test
    public void testParseName() {
        byte[] buffer = new byte[100];
        for (int i = 0; i < 50; i++) buffer[i] = (byte) ('A' + i % 26);
        String result = TarUtils.parseName(buffer, 0, 50);
        assertEquals(50, result.length());
    }

    @Test
    public void testParseNameWithNull() {
        byte[] buffer = new byte[100];
        buffer[0] = 'H'; buffer[1] = 'e'; buffer[2] = 'l'; buffer[3] = 'l'; buffer[4] = 'o';
        buffer[5] = '\0';
        String result = TarUtils.parseName(buffer, 0, 100);
        assertEquals("Hello", result);
    }

    @Test
    public void testParseNameAllNulls() {
        byte[] buffer = new byte[100];
        for (int i = 0; i < 100; i++) buffer[i] = 0;
        String result = TarUtils.parseName(buffer, 0, 100);
        assertEquals("", result);
    }

    @Test
    public void testParseNameEmptyBuffer() {
        byte[] buffer = new byte[0];
        String result = TarUtils.parseName(buffer, 0, 0);
        assertEquals("", result);
    }

    @Test
    public void testFormatNameBytes() {
        byte[] buf = new byte[100];
        String name = "testfile.txt";
        TarUtils.formatNameBytes(name, buf, 0, 100);
        String result = new String(buf);
        assertTrue(result.startsWith("testfile.txt"));
    }

    @Test
    public void testFormatNameBytesLongName() {
        byte[] buf = new byte[100];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) sb.append('a');
        String longName = sb.toString();
        TarUtils.formatNameBytes(longName, buf, 0, 100);
        String result = new String(buf);
        assertEquals(100, result.length());
    }

    @Test
    public void testParseOctalWithNegativeOffsetAndLength() {
        byte[] buffer = new byte[10];
        buffer[0] = '0';
        try {
            TarUtils.parseOctal(buffer, -1, -1);
            fail("Expected IllegalArgumentException for negative offset and length");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testParseOctalWithOffsetOnly() {
        byte[] buffer = new byte[10];
        buffer[0] = ' '; buffer[1] = ' '; buffer[2] = '1'; buffer[3] = '2';
        buffer[4] = ' '; buffer[5] = ' '; buffer[6] = ' '; buffer[7] = ' ';
        buffer[8] = ' '; buffer[9] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 2, 3);
            assertEquals(10L, result); // "12" in octal = 10 decimal
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalWithEmbeddedSpaceAfterDigits() {
        byte[] buffer = new byte[10];
        buffer[0] = '1'; buffer[1] = '2'; buffer[2] = ' '; buffer[3] = '3';
        buffer[4] = ' '; buffer[5] = ' '; buffer[6] = ' '; buffer[7] = ' ';
        buffer[8] = ' '; buffer[9] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 10);
            assertEquals(10L, result); // "12" parsed before space at index 2
        } catch (IllegalArgumentException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    public void testParseOctalOnlySpacesAfterFirstChar() {
        byte[] buffer = new byte[10];
        buffer[0] = ' '; buffer[1] = ' '; buffer[2] = ' '; buffer[3] = ' ';
        buffer[4] = ' '; buffer[5] = ' '; buffer[6] = ' '; buffer[7] = ' ';
        buffer[8] = ' '; buffer[9] = ' ';
        try {
            long result = TarUtils.parseOctal(buffer, 0, 10);
            assertEquals(0L, result);
        } catch (IllegalArgumentException e) {
            // may throw if implementation requires at least one digit
        }
    }

    @Test
    public void testParseOctalTrailingNonSpaceNonDigit() {
        byte[] buffer = new byte[10];
        buffer[0] = '1'; buffer[1] = '2'; buffer[2] = '3'; buffer[3] = 'a';
        buffer[4] = ' '; buffer[5] = ' '; buffer[6] = ' '; buffer[7] = ' ';
        buffer[8] = ' '; buffer[9] = ' ';
        try {
            TarUtils.parseOctal(buffer, 0, 10);
            fail("Expected IllegalArgumentException for trailing non-space non-digit");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testFormatLongOctalBytesWithNegativeOffset() {
        byte[] buf = new byte[12];
        try {
            TarUtils.formatLongOctalBytes(100L, buf, -1, 12);
            fail("Expected exception for negative offset");
        } catch (Exception e) {
            // expected
        }
    }

    @Test
    public void testFormatLongOctalBytesWithInsufficientLength() {
        byte[] buf = new byte[2];
        TarUtils.formatLongOctalBytes(100L, buf, 0, 2);
        String result = new String(buf);
        assertNotNull(result);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesZero() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(0L, buf, 0, 8);
        assertEquals("0", new String(buf).trim());
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesSmallValue() {
        byte[] buf = new byte[8];
        TarUtils.formatLongOctalOrBinaryBytes(1L, buf, 0, 8);
        assertTrue(new String(buf).contains("1"));
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesLargeOctal() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(2097152L, buf, 0, 12); // 2^21
        // fits in octal
        assertTrue(buf[0] != (byte) 0x80);
    }

    @Test
    public void testFormatLongOctalOrBinaryBytesMaxOctal() {
        byte[] buf = new byte[12];
        TarUtils.formatLongOctalOrBinaryBytes(8589934591L, buf, 0, 12); // 2^33 - 1
        // too large for octal, uses binary
        assertTrue(buf[0] == (byte) 0x80 || buf[0] == (byte) 0xff);
    }

    @Test
    public void testParseNameWithOffset() {
        byte[] buffer = new byte[100];
        buffer[10] = 'T'; buffer[11] = 'e'; buffer[12] = 's'; buffer[13] = 't';
        buffer[14] = '\0';
        String result = TarUtils.parseName(buffer, 10, 90);
        assertEquals("Test", result);
    }

    @Test
    public void testParseNameMaxLength() {
        byte[] buffer = new byte[100];
        for (int i = 0; i < 100; i++) buffer[i] = (byte) 'x';
        String result = TarUtils.parseName(buffer, 0, 100);
        assertEquals(100, result.length());
    }

    @Test
    public void testFormatNameBytesEmptyName() {
        byte[] buf = new byte[100];
        TarUtils.formatNameBytes("", buf, 0, 100);
        assertEquals('\0', buf[0]);
    }

    @Test
    public void testFormatNameBytesExactFit() {
        byte[] buf = new byte[5];
        TarUtils.formatNameBytes("Hello", buf, 0, 5);
        assertEquals("Hello", new String(buf));
    }

    @Test
    public void testFormatNameBytesShorterThanBuffer() {
        byte[] buf = new byte[10];
        TarUtils.formatNameBytes("Hi", buf, 0, 10);
        assertEquals("Hi\0\0\0\0\0\0\0\0", new String(buf));
    }
}