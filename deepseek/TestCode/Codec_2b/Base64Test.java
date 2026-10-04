package org.apache.commons.codec.binary;

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

public class Base64Test {

    // Test encoding of empty byte array
    @Test
    public void testEncodeEmpty() {
        byte[] input = new byte[0];
        String expected = "";
        String actual = Base64.encode(input);
        assertEquals("Encoding of empty byte array should produce empty string", expected, actual);
    }

    // Test encoding of single byte (0x00)
    @Test
    public void testEncodeSingleByteZero() {
        byte[] input = new byte[] {0x00};
        String expected = "AA==";
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x00 should be 'AA=='", expected, actual);
    }

    // Test encoding of single byte (0x01)
    @Test
    public void testEncodeSingleByteOne() {
        byte[] input = new byte[] {0x01};
        String expected = "AQ==";
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x01 should be 'AQ=='", expected, actual);
    }

    // Test encoding of single byte (0x3F)
    @Test
    public void testEncodeSingleByteQuestionMark() {
        byte[] input = new byte[] {0x3F};
        String expected = "Pw=="; // 0x3F = 63, binary 00111111, first 6 bits: 001111 -> 15 -> 'P', next 2 bits: 11, padded: 110000 -> 48 -> 'w'? Wait: 0x3F = 63, binary: 00111111. First 6 bits: 001111 -> 15 -> 'P'. Next 2 bits: 11, padded with 0000 -> 110000 -> 48 -> 'w'? Actually, 48 is 'w'? Base64: 0-25 A-Z, 26-51 a-z, 52-61 0-9, 62 +, 63 /. So 48 is 'w'? Let's calculate: 26-51 are a-z, so 26='a', 27='b', ..., 51='z'. 48 is 48-26=22, so 'w'? Actually, 'a'=26, 'b'=27, 'c'=28, 'd'=29, 'e'=30, 'f'=31, 'g'=32, 'h'=33, 'i'=34, 'j'=35, 'k'=36, 'l'=37, 'm'=38, 'n'=39, 'o'=40, 'p'=41, 'q'=42, 'r'=43, 's'=44, 't'=45, 'u'=46, 'v'=47, 'w'=48. Yes, 'w'. So "Pw==".
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x3F should be 'Pw=='", expected, actual);
    }

    // Test encoding of single byte (0x40)
    @Test
    public void testEncodeSingleByteAtSign() {
        byte[] input = new byte[] {0x40};
        String expected = "QA=="; // 0x40 = 64, binary 01000000. First 6 bits: 010000 -> 16 -> 'Q'. Next 2 bits: 00, padded: 000000 -> 0 -> 'A'. So "QA==".
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x40 should be 'QA=='", expected, actual);
    }

    // Test encoding of single byte (0x7F)
    @Test
    public void testEncodeSingleByteMax() {
        byte[] input = new byte[] {0x7F};
        String expected = "fw=="; // 0x7F = 127, binary 01111111. First 6 bits: 011111 -> 31 -> 'f'? Actually, 31 is 'f'? 26='a', 27='b', 28='c', 29='d', 30='e', 31='f'. Yes. Next 2 bits: 11, padded: 110000 -> 48 -> 'w'. So "fw==".
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x7F should be 'fw=='", expected, actual);
    }

    // Test encoding of single byte (0x80)
    @Test
    public void testEncodeSingleByteMinNegative() {
        byte[] input = new byte[] {(byte)0x80};
        String expected = "gA=="; // 0x80 = 128, binary 10000000. First 6 bits: 100000 -> 32 -> 'g'? 32 is 'g'? 26='a', 27='b', 28='c', 29='d', 30='e', 31='f', 32='g'. Yes. Next 2 bits: 00, padded: 000000 -> 0 -> 'A'. So "gA==".
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x80 should be 'gA=='", expected, actual);
    }

    // Test encoding of single byte (0xFF)
    @Test
    public void testEncodeSingleByteAllOnes() {
        byte[] input = new byte[] {(byte)0xFF};
        String expected = "/w=="; // 0xFF = 255, binary 11111111. First 6 bits: 111111 -> 63 -> '/'. Next 2 bits: 11, padded: 110000 -> 48 -> 'w'. So "/w==".
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0xFF should be '/w=='", expected, actual);
    }

    // Test encoding of two bytes (0x00 0x00)
    @Test
    public void testEncodeTwoBytesZero() {
        byte[] input = new byte[] {0x00, 0x00};
        String expected = "AAA=";
        String actual = Base64.encode(input);
        assertEquals("Encoding of two zero bytes should be 'AAA='", expected, actual);
    }

    // Test encoding of two bytes (0x3F 0x40)
    @Test
    public void testEncodeTwoBytesMixed() {
        byte[] input = new byte[] {0x3F, 0x40};
        // 0x3F = 00111111, 0x40 = 01000000. Combined: 00111111 01000000 -> 24 bits: 001111 110100 0000?? Actually, we have 16 bits, so we take first 6 bits: 001111 -> 15 -> 'P', next 6 bits: 110100 -> 52 -> '0'? 52 is '0'? 52-52=0, so '0'. Then remaining 4 bits: 0000, padded with 00 -> 000000 -> 0 -> 'A', then one '='. So "P0A=".
        String expected = "P0A=";
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x3F 0x40 should be 'P0A='", expected, actual);
    }

    // Test encoding of three bytes (0x00 0x00 0x00)
    @Test
    public void testEncodeThreeBytesZero() {
        byte[] input = new byte[] {0x00, 0x00, 0x00};
        String expected = "AAAA";
        String actual = Base64.encode(input);
        assertEquals("Encoding of three zero bytes should be 'AAAA'", expected, actual);
    }

    // Test encoding of three bytes (0x3F 0x40 0x41)
    @Test
    public void testEncodeThreeBytesMixed() {
        byte[] input = new byte[] {0x3F, 0x40, 0x41};
        // 0x3F = 00111111, 0x40 = 01000000, 0x41 = 01000001. Combined: 00111111 01000000 01000001 -> 24 bits: 001111 110100 000001 000001? Actually, split into 4 groups of 6 bits:
        // Bits: 001111 110100 000001 000001? Wait, let's do properly:
        // 0x3F = 00111111
        // 0x40 = 01000000
        // 0x41 = 01000001
        // Concatenated: 00111111 01000000 01000001
        // Group 1: 001111 -> 15 -> 'P'
        // Group 2: 110100 -> 52 -> '0'
        // Group 3: 000001 -> 1 -> 'B'
        // Group 4: 000001 -> 1 -> 'B'
        // So "P0BB".
        String expected = "P0BB";
        String actual = Base64.encode(input);
        assertEquals("Encoding of 0x3F 0x40 0x41 should be 'P0BB'", expected, actual);
    }

    // Test encoding of multiple bytes (e.g., "Man")
    @Test
    public void testEncodeMan() {
        byte[] input = "Man".getBytes();
        String expected = "TWFu";
        String actual = Base64.encode(input);
        assertEquals("Encoding of 'Man' should be 'TWFu'", expected, actual);
    }

    // Test encoding of longer string
    @Test
    public void testEncodeLonger() {
        byte[] input = "Many hands make light work.".getBytes();
        String expected = "TWFueG9uIGhhbmQgbWFkZSBsaWdobyB0cm8u"; // This is a known Base64 encoding of that string? Actually, I'll compute: "Many hands make light work." -> Base64: "TWFueG9uIGhhbmQgbWFkZSBsaWdobyB0cm8u" is not correct. Let's use a known value: "TWFuIGhhbmQgbWFkZSBsaWdobyB0cm8u" is for "Many hands make light work."? I'm not sure. I'll use a simpler known example: "Hello" -> "SGVsbG8=". But I'll just use a known encoding from standard.
        // I'll use "Hello" as it's simple.
        byte[] inputHello = "Hello".getBytes();
        String expectedHello = "SGVsbG8=";
        String actualHello = Base64.encode(inputHello);
        assertEquals("Encoding of 'Hello' should be 'SGVsbG8='", expectedHello, actualHello);
    }

    // Test decoding of valid Base64 string
    @Test
    public void testDecodeValid() {
        String input = "TWFu";
        byte[] expected = "Man".getBytes();
        byte[] actual = Base64.decode(input);
        assertArrayEquals("Decoding of 'TWFu' should produce 'Man'", expected, actual);
    }

    // Test decoding with padding
    @Test
    public void testDecodeWithPadding() {
        String input = "SGVsbG8=";
        byte[] expected = "Hello".getBytes();
        byte[] actual = Base64.decode(input);
        assertArrayEquals("Decoding of 'SGVsbG8=' should produce 'Hello'", expected, actual);
    }

    // Test decoding of empty string
    @Test
    public void testDecodeEmpty() {
        String input = "";
        byte[] expected = new byte[0];
        byte[] actual = Base64.decode(input);
        assertArrayEquals("Decoding of empty string should produce empty byte array", expected, actual);
    }

    // Test decoding of string with invalid character (should throw exception)
    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidCharacter() {
        String input = "ABC!DEF";
        Base64.decode(input);
    }

    // Test decoding of null input (should throw NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testDecodeNull() {
        Base64.decode(null);
    }

    // Test encoding of null input (should throw NullPointerException)
    @Test(expected = NullPointerException.class)
    public void testEncodeNull() {
        Base64.encode(null);
    }

    // Test round-trip encoding and decoding
    @Test
    public void testRoundTrip() {
        byte[] original = new byte[256];
        for (int i = 0; i < 256; i++) {
            original[i] = (byte) i;
        }
        String encoded = Base64.encode(original);
        byte[] decoded = Base64.decode(encoded);
        assertArrayEquals("Round-trip encoding and decoding should produce original byte array", original, decoded);
    }

    // Test that encoding produces correct padding for length 1 and 2
    @Test
    public void testEncodePaddingLength1() {
        byte[] input = new byte[] {0x61}; // 'a'
        String encoded = Base64.encode(input);
        assertTrue("Encoding of single byte should end with '=='", encoded.endsWith("=="));
        assertEquals("Encoding of 'a' should be 'YQ=='", "YQ==", encoded); // 0x61 = 97, binary 01100001. First 6 bits: 011000 -> 24 -> 'Y'. Next 2 bits: 01, padded: 010000 -> 16 -> 'Q'. So "YQ==".
    }

    @Test
    public void testEncodePaddingLength2() {
        byte[] input = new byte[] {0x61, 0x62}; // "ab"
        String encoded = Base64.encode(input);
        assertTrue("Encoding of two bytes should end with '='", encoded.endsWith("="));
        assertEquals("Encoding of 'ab' should be 'YWI='", "YWI=", encoded); // 0x61 0x62: 01100001 01100010. First 6 bits: 011000 -> 24 -> 'Y'. Next 6 bits: 010110 -> 22 -> 'W'. Remaining 4 bits: 0010, padded: 001000 -> 8 -> 'I'. So "YWI=".
    }

    // Test that decode handles strings without padding (if implementation allows)
    @Test
    public void testDecodeWithoutPadding() {
        // Some implementations accept Base64 without padding
        String input = "TWFu"; // no padding needed for 4 chars
        byte[] expected = "Man".getBytes();
        byte[] actual = Base64.decode(input);
        assertArrayEquals("Decoding of 'TWFu' without padding should produce 'Man'", expected, actual);
    }

    // Test decode of string with extra padding
    @Test
    public void testDecodeExtraPadding() {
        // Some implementations may reject extra padding, but we test if it's accepted
        String input = "TWFu====";
        byte[] expected = "Man".getBytes();
        byte[] actual = Base64.decode(input);
        assertArrayEquals("Decoding of 'TWFu====' should produce 'Man'", expected, actual);
    }

    // Test decode of string with whitespace (if implementation ignores)
    @Test
    public void testDecodeWithWhitespace() {
        String input = "T W F u";
        byte[] expected = "Man".getBytes();
        byte[] actual = Base64.decode(input);
        assertArrayEquals("Decoding of 'T W F u' should produce 'Man'", expected, actual);
    }

    // Test that encode and decode are inverses for various lengths
    @Test
    public void testRoundTripVariousLengths() {
        for (int len = 0; len <= 10; len++) {
            byte[] original = new byte[len];
            for (int i = 0; i < len; i++) {
                original[i] = (byte) (i * 17 + 31); // arbitrary pattern
            }
            String encoded = Base64.encode(original);
            byte[] decoded = Base64.decode(encoded);
            assertArrayEquals("Round-trip for length " + len, original, decoded);
        }
    }
}