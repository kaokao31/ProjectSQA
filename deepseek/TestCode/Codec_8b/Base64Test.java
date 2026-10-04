package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Comprehensive JUnit 4 test suite for Base64 encoding/decoding.
 * Targets maximum code coverage and fault detection, including edge cases,
 * boundary values, null/empty inputs, padding, whitespace, and invalid characters.
 */
public class Base64Test {

    private Base64 base64;

    @Before
    public void setUp() {
        base64 = new Base64();
    }

    // ======================== encode(byte[]) ========================

    @Test
    public void testEncodeNullInput() {
        assertNull("Encoding null should return null", base64.encode(null));
    }

    @Test
    public void testEncodeEmptyArray() {
        byte[] input = new byte[0];
        byte[] expected = new byte[0];
        assertArrayEquals("Encoding empty array should return empty array", expected, base64.encode(input));
    }

    @Test
    public void testEncodeSingleByte() {
        byte[] input = new byte[]{(byte) 0x41}; // 'A'
        byte[] expected = "QQ==".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Encoding single byte 'A'", expected, base64.encode(input));
    }

    @Test
    public void testEncodeTwoBytes() {
        byte[] input = new byte[]{(byte) 0x41, (byte) 0x42}; // "AB"
        byte[] expected = "QUI=".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Encoding two bytes 'AB'", expected, base64.encode(input));
    }

    @Test
    public void testEncodeThreeBytes() {
        byte[] input = new byte[]{(byte) 0x41, (byte) 0x42, (byte) 0x43}; // "ABC"
        byte[] expected = "QUJD".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Encoding three bytes 'ABC'", expected, base64.encode(input));
    }

    @Test
    public void testEncodeMultipleOfThree() {
        byte[] input = "ManMan".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "TWFuTWFu".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Encoding 'ManMan' (6 bytes)", expected, base64.encode(input));
    }

    @Test
    public void testEncodeNotMultipleOfThree() {
        byte[] input = "ManM".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "TWFuTQ==".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Encoding 'ManM' (4 bytes)", expected, base64.encode(input));
    }

    @Test
    public void testEncodeAllByteValues() {
        byte[] input = new byte[256];
        for (int i = 0; i < 256; i++) {
            input[i] = (byte) i;
        }
        byte[] encoded = base64.encode(input);
        assertNotNull("Encoding all byte values should not return null", encoded);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals("Roundtrip all byte values", input, decoded);
    }

    @Test
    public void testEncodeLargeInput() {
        byte[] input = new byte[10000];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        byte[] encoded = base64.encode(input);
        assertNotNull("Encoding large input should succeed", encoded);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals("Roundtrip large input", input, decoded);
    }

    // ======================== decode(byte[]) ========================

    @Test
    public void testDecodeNullInput() {
        assertNull("Decoding null should return null", base64.decode(null));
    }

    @Test
    public void testDecodeEmptyArray() {
        byte[] input = new byte[0];
        byte[] expected = new byte[0];
        assertArrayEquals("Decoding empty array should return empty array", expected, base64.decode(input));
    }

    @Test
    public void testDecodeValidString() {
        byte[] input = "TWFu".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "Man".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Decoding 'TWFu'", expected, base64.decode(input));
    }

    @Test
    public void testDecodeWithPadding() {
        byte[] input = "TWFuTQ==".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "ManM".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Decoding 'TWFuTQ=='", expected, base64.decode(input));
    }

    @Test
    public void testDecodeWithWhitespace() {
        byte[] input = "T W F u".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "Man".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Decoding with whitespace", expected, base64.decode(input));
    }

    @Test
    public void testDecodeWithTabAndNewline() {
        byte[] input = "T\tW\nF\nu".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "Man".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Decoding with tab and newline", expected, base64.decode(input));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidCharacter() {
        byte[] input = "TWFu!".getBytes(StandardCharsets.UTF_8);
        base64.decode(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidPadding() {
        byte[] input = "TWFu===".getBytes(StandardCharsets.UTF_8);
        base64.decode(input);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidLength() {
        byte[] input = "TWF".getBytes(StandardCharsets.UTF_8);
        base64.decode(input);
    }

    @Test
    public void testDecodeSingleCharacter() {
        byte[] input = "QQ==".getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[]{(byte) 0x41};
        assertArrayEquals("Decoding 'QQ=='", expected, base64.decode(input));
    }

    @Test
    public void testDecodeTwoCharacters() {
        byte[] input = "QUI=".getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[]{(byte) 0x41, (byte) 0x42};
        assertArrayEquals("Decoding 'QUI='", expected, base64.decode(input));
    }

    @Test
    public void testDecodeThreeCharacters() {
        byte[] input = "QUJD".getBytes(StandardCharsets.UTF_8);
        byte[] expected = new byte[]{(byte) 0x41, (byte) 0x42, (byte) 0x43};
        assertArrayEquals("Decoding 'QUJD'", expected, base64.decode(input));
    }

    // ======================== encodeToString(byte[]) ========================

    @Test
    public void testEncodeToStringNull() {
        assertNull("encodeToString(null) should return null", base64.encodeToString(null));
    }

    @Test
    public void testEncodeToStringEmpty() {
        assertEquals("encodeToString(empty) should return empty string", "", base64.encodeToString(new byte[0]));
    }

    @Test
    public void testEncodeToStringBasic() {
        String result = base64.encodeToString("Man".getBytes(StandardCharsets.UTF_8));
        assertEquals("encodeToString('Man')", "TWFu", result);
    }

    // ======================== decode(String) ========================

    @Test
    public void testDecodeStringNull() {
        assertNull("decode(null) should return null", base64.decode((String) null));
    }

    @Test
    public void testDecodeStringEmpty() {
        assertArrayEquals("decode('') should return empty array", new byte[0], base64.decode(""));
    }

    @Test
    public void testDecodeStringValid() {
        byte[] result = base64.decode("TWFu");
        assertArrayEquals("decode('TWFu')", "Man".getBytes(StandardCharsets.UTF_8), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeStringInvalidCharacter() {
        base64.decode("TWFu!");
    }

    // ======================== isBase64(byte) ========================

    @Test
    public void testIsBase64ValidCharacters() {
        assertTrue("'A' should be Base64", Base64.isBase64((byte) 'A'));
        assertTrue("'Z' should be Base64", Base64.isBase64((byte) 'Z'));
        assertTrue("'a' should be Base64", Base64.isBase64((byte) 'a'));
        assertTrue("'z' should be Base64", Base64.isBase64((byte) 'z'));
        assertTrue("'0' should be Base64", Base64.isBase64((byte) '0'));
        assertTrue("'9' should be Base64", Base64.isBase64((byte) '9'));
        assertTrue("'+' should be Base64", Base64.isBase64((byte) '+'));
        assertTrue("'/' should be Base64", Base64.isBase64((byte) '/'));
        assertTrue("'=' should be Base64", Base64.isBase64((byte) '='));
    }

    @Test
    public void testIsBase64InvalidCharacters() {
        assertFalse("'!' should not be Base64", Base64.isBase64((byte) '!'));
        assertFalse("space should not be Base64", Base64.isBase64((byte) ' '));
        assertFalse("tab should not be Base64", Base64.isBase64((byte) '\t'));
        assertFalse("newline should not be Base64", Base64.isBase64((byte) '\n'));
        assertFalse("'@' should not be Base64", Base64.isBase64((byte) '@'));
        assertFalse("'[' should not be Base64", Base64.isBase64((byte) '['));
    }

    // ======================== isArrayByteBase64(byte[]) ========================

    @Test
    public void testIsArrayByteBase64Null() {
        assertFalse("isArrayByteBase64(null) should be false", Base64.isArrayByteBase64(null));
    }

    @Test
    public void testIsArrayByteBase64Empty() {
        assertTrue("isArrayByteBase64(empty) should be true", Base64.isArrayByteBase64(new byte[0]));
    }

    @Test
    public void testIsArrayByteBase64Valid() {
        byte[] valid = "TWFu".getBytes(StandardCharsets.UTF_8);
        assertTrue("Valid Base64 array", Base64.isArrayByteBase64(valid));
    }

    @Test
    public void testIsArrayByteBase64Invalid() {
        byte[] invalid = "TWFu!".getBytes(StandardCharsets.UTF_8);
        assertFalse("Invalid Base64 array", Base64.isArrayByteBase64(invalid));
    }

    @Test
    public void testIsArrayByteBase64WithWhitespace() {
        byte[] withSpace = "T W F u".getBytes(StandardCharsets.UTF_8);
        assertFalse("Whitespace should make array invalid", Base64.isArrayByteBase64(withSpace));
    }

    // ======================== Defects4J specific bug triggers ========================

    // Bug: Decoding a string with incorrect padding length (e.g., 2 padding chars for 1 byte)
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeIncorrectPaddingLength() {
        // "QQ" is 2 chars, should have 2 padding chars "QQ==", but we give "QQ=" (1 padding)
        base64.decode("QQ=");
    }

    // Bug: Decoding a string with extra padding characters beyond valid length
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeExtraPadding() {
        base64.decode("QQ====");
    }

    // Bug: Decoding a string with padding in the middle (invalid)
    @Test(expected = IllegalArgumentException.class)
    public void testDecodePaddingInMiddle() {
        base64.decode("Q=Q=");
    }

    // Bug: Decoding a string with non-base64 characters that are whitespace but not ignored?
    // (Assuming whitespace is ignored, but if implementation doesn't handle it, this test may fail)
    @Test
    public void testDecodeWithOnlyWhitespace() {
        byte[] result = base64.decode("   ");
        assertArrayEquals("Decoding whitespace only should return empty array", new byte[0], result);
    }

    // Bug: Encoding a byte array that contains negative values (signed bytes)
    @Test
    public void testEncodeNegativeBytes() {
        byte[] input = new byte[]{(byte) 0x80, (byte) 0xFF};
        byte[] encoded = base64.encode(input);
        assertNotNull("Encoding negative bytes should succeed", encoded);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals("Roundtrip negative bytes", input, decoded);
    }

    // Bug: Decoding a string that is exactly the length of a valid encoding but with wrong padding
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeValidLengthWrongPadding() {
        // "TWF" is 3 chars, should have 1 padding "TWF=", but we give "TWF" (no padding)
        base64.decode("TWF");
    }

    // Bug: Decoding a string with characters outside the Base64 alphabet (e.g., '-')
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeHyphen() {
        base64.decode("TWFu-");
    }

    // Bug: Roundtrip with empty string
    @Test
    public void testRoundtripEmpty() {
        byte[] input = new byte[0];
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals("Roundtrip empty array", input, decoded);
    }

    // Bug: Roundtrip with single byte 0x00
    @Test
    public void testRoundtripZeroByte() {
        byte[] input = new byte[]{(byte) 0x00};
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals("Roundtrip zero byte", input, decoded);
    }

    // Bug: Roundtrip with all zeros
    @Test
    public void testRoundtripAllZeros() {
        byte[] input = new byte[100];
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals("Roundtrip all zeros", input, decoded);
    }

    // Bug: Decoding a string that contains only padding characters
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeOnlyPadding() {
        base64.decode("====");
    }

    // Bug: Decoding a string with a single character (invalid length)
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeSingleChar() {
        base64.decode("Q");
    }

    // Bug: Decoding a string with two characters (invalid length without padding)
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeTwoCharsNoPadding() {
        base64.decode("QQ");
    }

    // Bug: Decoding a string with three characters (invalid length without padding)
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeThreeCharsNoPadding() {
        base64.decode("QUJ");
    }

    // Bug: Encoding and decoding a string with Unicode characters (should work on bytes)
    @Test
    public void testRoundtripUnicode() {
        String original = "日本語";
        byte[] input = original.getBytes(StandardCharsets.UTF_8);
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        String result = new String(decoded, StandardCharsets.UTF_8);
        assertEquals("Roundtrip Unicode string", original, result);
    }

    // Bug: Large input with specific pattern to trigger buffer overflow or misalignment
    @Test
    public void testLargeInputBoundary() {
        byte[] input = new byte[1024 * 1024]; // 1 MB
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 251); // prime to avoid patterns
        }
        byte[] encoded = base64.encode(input);
        assertNotNull("Encoding 1MB should succeed", encoded);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals("Roundtrip 1MB", input, decoded);
    }

    // Bug: Decoding a string with line breaks (CRLF) - should be ignored
    @Test
    public void testDecodeWithCRLF() {
        byte[] input = "TWFu\r\nTQ==".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "ManM".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Decoding with CRLF", expected, base64.decode(input));
    }

    // Bug: Decoding a string with mixed whitespace and valid characters
    @Test
    public void testDecodeMixedWhitespace() {
        byte[] input = " T W F u ".getBytes(StandardCharsets.UTF_8);
        byte[] expected = "Man".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals("Decoding with surrounding whitespace", expected, base64.decode(input));
    }

    // Bug: Decoding a string with only whitespace and padding
    @Test
    public void testDecodeWhitespaceAndPadding() {
        byte[] input = " = = ".getBytes(StandardCharsets.UTF_8);
        // This is invalid because padding cannot be preceded by whitespace? Actually whitespace should be ignored, so "==" is valid padding for empty? But empty input would be 0 bytes, which is valid.
        // However, "==" is not a valid encoding for any non-empty input. It might decode to empty array.
        // We'll test that it throws or returns empty depending on implementation.
        // For robustness, we expect it to throw because "==" alone is not a valid encoding (it would represent 0 bytes? Actually "==" is not a valid Base64 string because it has no data bits. Usually it's invalid.)
        // We'll assume it throws.
        try {
            base64.decode(input);
            fail("Decoding whitespace and padding only should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // Bug: Encoding null byte array via encodeToString
    @Test
    public void testEncodeToStringNullInput() {
        assertNull("encodeToString(null) should return null", base64.encodeToString(null));
    }

    // Bug: Decoding null string via decode(String)
    @Test
    public void testDecodeStringNullInput() {
        assertNull("decode(null) should return null", base64.decode((String) null));
    }
}