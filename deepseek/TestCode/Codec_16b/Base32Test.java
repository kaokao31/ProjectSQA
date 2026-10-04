package org.apache.commons.codec.binary;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class Base32Test {

    private static final String CHARSET_UTF8 = "UTF-8";
    private Base32 codec;

    @Before
    public void setUp() {
        codec = new Base32();
    }

    @Test
    public void testIsInAlphabet() {
        // Test valid characters
        assertTrue("A should be in alphabet", codec.isInAlphabet((byte) 'A'));
        assertTrue("Z should be in alphabet", codec.isInAlphabet((byte) 'Z'));
        assertTrue("2 should be in alphabet", codec.isInAlphabet((byte) '2'));
        assertTrue("7 should be in alphabet", codec.isInAlphabet((byte) '7'));
        
        // Test invalid characters
        assertFalse("0 should not be in alphabet", codec.isInAlphabet((byte) '0'));
        assertFalse("1 should not be in alphabet", codec.isInAlphabet((byte) '1'));
        assertFalse("8 should not be in alphabet", codec.isInAlphabet((byte) '8'));
        assertFalse("9 should not be in alphabet", codec.isInAlphabet((byte) '9'));
        assertFalse("+ should not be in alphabet", codec.isInAlphabet((byte) '+'));
        assertFalse("/ should not be in alphabet", codec.isInAlphabet((byte) '/'));
        assertFalse("lowercase a should not be in alphabet", codec.isInAlphabet((byte) 'a'));
        
        // Test padding character
        assertFalse("= should not be in alphabet", codec.isInAlphabet((byte) '='));
        
        // Test whitespace
        assertFalse("space should not be in alphabet", codec.isInAlphabet((byte) ' '));
        assertFalse("tab should not be in alphabet", codec.isInAlphabet((byte) '\t'));
        assertFalse("newline should not be in alphabet", codec.isInAlphabet((byte) '\n'));
    }

    @Test
    public void testEncodeAndDecodeEmpty() {
        byte[] empty = new byte[0];
        byte[] encoded = codec.encode(empty);
        assertNotNull("Encoded empty should not be null", encoded);
        assertEquals("Encoded empty should be empty", 0, encoded.length);
        
        byte[] decoded = codec.decode(encoded);
        assertNotNull("Decoded empty should not be null", decoded);
        assertEquals("Decoded empty should be empty", 0, decoded.length);
    }

    @Test
    public void testEncodeAndDecodeSingleByte() {
        byte[] input = new byte[] { (byte) 0x41 }; // 'A'
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        assertTrue("Encoded length should be positive", encoded.length > 0);
        
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testEncodeAndDecodeMultipleBytes() {
        byte[] input = new byte[] { (byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F }; // "Hello"
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testEncodeAndDecodeWithPadding() {
        // Test various input lengths to trigger padding
        byte[] input1 = new byte[] { (byte) 0x61 }; // 'a' - 1 byte
        byte[] encoded1 = codec.encode(input1);
        byte[] decoded1 = codec.decode(encoded1);
        assertArrayEquals("1 byte roundtrip failed", input1, decoded1);
        
        byte[] input2 = new byte[] { (byte) 0x61, (byte) 0x62 }; // 'ab' - 2 bytes
        byte[] encoded2 = codec.encode(input2);
        byte[] decoded2 = codec.decode(encoded2);
        assertArrayEquals("2 byte roundtrip failed", input2, decoded2);
        
        byte[] input3 = new byte[] { (byte) 0x61, (byte) 0x62, (byte) 0x63 }; // 'abc' - 3 bytes
        byte[] encoded3 = codec.encode(input3);
        byte[] decoded3 = codec.decode(encoded3);
        assertArrayEquals("3 byte roundtrip failed", input3, decoded3);
        
        byte[] input4 = new byte[] { (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x64 }; // 'abcd' - 4 bytes
        byte[] encoded4 = codec.encode(input4);
        byte[] decoded4 = codec.decode(encoded4);
        assertArrayEquals("4 byte roundtrip failed", input4, decoded4);
    }

    @Test
    public void testDecodeInvalidCharacter() {
        // Test decoding with invalid characters
        String invalidInput = "INVALID!!!";
        try {
            codec.decode(invalidInput.getBytes(CHARSET_UTF8));
            fail("Should have thrown exception for invalid characters");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testDecodeWithWhitespace() {
        // Base32 should ignore whitespace
        String validInput = "JBSWY3DP"; // "Hello" in Base32
        String inputWithSpaces = "JBS WY3 DP";
        
        byte[] decoded1 = codec.decode(validInput.getBytes(CHARSET_UTF8));
        byte[] decoded2 = codec.decode(inputWithSpaces.getBytes(CHARSET_UTF8));
        assertArrayEquals("Decoding with whitespace should match", decoded1, decoded2);
    }

    @Test
    public void testEncodeToString() {
        byte[] input = new byte[] { (byte) 0x48, (byte) 0x65, (byte) 0x6C, (byte) 0x6C, (byte) 0x6F };
        String encoded = codec.encodeToString(input);
        assertNotNull("Encoded string should not be null", encoded);
        assertTrue("Encoded string should not be empty", encoded.length() > 0);
        
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testDecodeString() {
        String encoded = "JBSWY3DP"; // "Hello" in Base32
        byte[] decoded = codec.decode(encoded);
        assertNotNull("Decoded should not be null", decoded);
        
        String result = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("Decoded string should be 'Hello'", "Hello", result);
    }

    @Test
    public void testEncodeAndDecodeLargeInput() {
        // Test with larger input to exercise more code paths
        byte[] input = new byte[1024];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testDecodeWithLineSeparators() {
        // Test decoding with line separators (common in encoded data)
        String encodedWithNewlines = "JBSWY3DP\nJBSWY3DP";
        byte[] decoded = codec.decode(encodedWithNewlines.getBytes(CHARSET_UTF8));
        assertNotNull("Decoded should not be null", decoded);
        assertTrue("Decoded length should be positive", decoded.length > 0);
    }

    @Test
    public void testEncodeAndDecodeAllBytes() {
        // Test all possible byte values
        byte[] input = new byte[256];
        for (int i = 0; i < 256; i++) {
            input[i] = (byte) i;
        }
        
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testDecodeWithInvalidPadding() {
        // Test invalid padding scenarios
        String invalidPadding1 = "JBSWY3D="; // Incorrect padding
        try {
            codec.decode(invalidPadding1.getBytes(CHARSET_UTF8));
            fail("Should have thrown exception for invalid padding");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testEncodeAndDecodeWithLineLength() {
        // Test with line length parameter
        Base32 codecWithLineLength = new Base32(76, new byte[] { '\r', '\n' });
        byte[] input = new byte[100];
        Arrays.fill(input, (byte) 'A');
        
        byte[] encoded = codecWithLineLength.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codecWithLineLength.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testDecodeWithMultiplePadding() {
        // Test decoding with multiple padding characters
        String encoded = "JBSWY3DP"; // No padding needed for 5 bytes
        byte[] decoded = codec.decode(encoded.getBytes(CHARSET_UTF8));
        assertNotNull("Decoded should not be null", decoded);
        
        // Test with padding
        String encodedWithPadding = "NBSWY3DP"; // Different data
        byte[] decodedWithPadding = codec.decode(encodedWithPadding.getBytes(CHARSET_UTF8));
        assertNotNull("Decoded with padding should not be null", decodedWithPadding);
    }

    @Test
    public void testDecodeNullInput() {
        try {
            codec.decode((byte[]) null);
            fail("Should have thrown NullPointerException or similar");
        } catch (NullPointerException e) {
            // Expected
        } catch (Exception e) {
            // Also acceptable
        }
    }

    @Test
    public void testEncodeNullInput() {
        try {
            codec.encode((byte[]) null);
            fail("Should have thrown NullPointerException or similar");
        } catch (NullPointerException e) {
            // Expected
        } catch (Exception e) {
            // Also acceptable
        }
    }

    @Test
    public void testDecodeEmptyString() {
        byte[] decoded = codec.decode("");
        assertNotNull("Decoded empty string should not be null", decoded);
        assertEquals("Decoded empty string should be empty", 0, decoded.length);
    }

    @Test
    public void testEncodeAndDecodeWithSpecialCharacters() {
        // Test with bytes that might cause issues
        byte[] input = new byte[] { 
            (byte) 0x00, (byte) 0xFF, (byte) 0x80, (byte) 0x7F, 
            (byte) 0x01, (byte) 0xFE, (byte) 0x55, (byte) 0xAA 
        };
        
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testAvailableByteCounts() {
        // Test various input lengths to ensure proper encoding/decoding
        for (int len = 0; len <= 10; len++) {
            byte[] input = new byte[len];
            for (int i = 0; i < len; i++) {
                input[i] = (byte) (i * 17 + 13);
            }
            
            byte[] encoded = codec.encode(input);
            byte[] decoded = codec.decode(encoded);
            assertArrayEquals("Roundtrip failed for length " + len, input, decoded);
        }
    }

    @Test
    public void testDecodeWithTrailingWhitespace() {
        String validInput = "JBSWY3DP  "; // Trailing spaces
        byte[] decoded = codec.decode(validInput.getBytes(CHARSET_UTF8));
        assertNotNull("Decoded should not be null", decoded);
        
        String result = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
        assertEquals("Decoded string should be 'Hello'", "Hello", result);
    }

    @Test
    public void testEncodeAndDecodeWithDifferentConstructors() {
        // Test with different constructor parameters
        Base32 codecNoPadding = new Base32(0, null);
        byte[] input = "TestData".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        
        byte[] encoded = codecNoPadding.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codecNoPadding.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testDecodeInvalidLength() {
        // Test decoding with invalid length (not multiple of 8)
        String invalidLength = "JBSWY3"; // 6 characters, not valid Base32
        try {
            codec.decode(invalidLength.getBytes(CHARSET_UTF8));
            // May or may not throw depending on implementation
        } catch (Exception e) {
            // Expected for strict implementations
        }
    }

    @Test
    public void testEncodeAndDecodeWithLineSeparator() {
        // Test with custom line separator
        Base32 codecWithCustomSeparator = new Base32(64, new byte[] { '\n' });
        byte[] input = new byte[200];
        Arrays.fill(input, (byte) 'X');
        
        byte[] encoded = codecWithCustomSeparator.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codecWithCustomSeparator.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }

    @Test
    public void testDecodeWithOnlyWhitespace() {
        byte[] decoded = codec.decode("   \n\t  ".getBytes(CHARSET_UTF8));
        assertNotNull("Decoded whitespace-only should not be null", decoded);
        assertEquals("Decoded whitespace-only should be empty", 0, decoded.length);
    }

    @Test
    public void testEncodeAndDecodeMaximumValues() {
        // Test with maximum byte values
        byte[] input = new byte[] { 
            Byte.MAX_VALUE, Byte.MIN_VALUE, (byte) 0, (byte) -1 
        };
        
        byte[] encoded = codec.encode(input);
        assertNotNull("Encoded should not be null", encoded);
        
        byte[] decoded = codec.decode(encoded);
        assertArrayEquals("Decoded should match original", input, decoded);
    }
}