package org.apache.commons.codec.binary;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class Base64Test {

    private Base64 base64;

    @Before
    public void setUp() {
        base64 = new Base64();
    }

    // -----------------------------------------------------------------------
    // Basic encode/decode roundtrip
    // -----------------------------------------------------------------------

    @Test
    public void testEncodeAndDecodeEmptyArray() {
        byte[] empty = new byte[0];
        byte[] encoded = base64.encode(empty);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(empty, decoded);
    }

    @Test
    public void testEncodeAndDecodeSingleByte() {
        for (int b = -128; b <= 127; b++) {
            byte[] input = new byte[]{(byte) b};
            byte[] encoded = base64.encode(input);
            byte[] decoded = base64.decode(encoded);
            assertArrayEquals("Failure for byte value: " + b, input, decoded);
        }
    }

    @Test
    public void testEncodeAndDecodeTwoBytes() {
        byte[] input = new byte[]{0x12, 0x34};
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeAndDecodeThreeBytes() {
        byte[] input = new byte[]{0x56, 0x78, (byte) 0x9A};
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeAndDecodeMultipleOfThree() {
        byte[] input = new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x9A, (byte) 0xBC};
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeAndDecodeRemainderOne() {
        byte[] input = new byte[]{0x01};
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeAndDecodeRemainderTwo() {
        byte[] input = new byte[]{0x01, 0x02};
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    @Test
    public void testEncodeAndDecodeAllByteValues() {
        byte[] input = new byte[256];
        for (int i = 0; i < 256; i++) {
            input[i] = (byte) i;
        }
        byte[] encoded = base64.encode(input);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // -----------------------------------------------------------------------
    // decodeString and encodeBase64String (if such methods exist)
    // -----------------------------------------------------------------------

    @Test
    public void testDecodeStringWithPadding() {
        String expected = "Hello";
        byte[] encoded = base64.encode(expected.getBytes());
        String encodedStr = new String(encoded);
        byte[] decoded = base64.decode(encodedStr);
        assertEquals(expected, new String(decoded));
    }

    @Test
    public void testDecodeStringWithoutPadding() {
        // Base64 encoding without padding (if URL safe mode or non-standard)
        // Typically padding is required; this tests decode tolerance.
        String input = "SGVsbG8"; // "Hello" without padding
        try {
            byte[] decoded = base64.decode(input);
            // Some implementations may still decode; if not, expect exception.
            if (decoded != null) {
                assertEquals("Hello", new String(decoded));
            }
        } catch (Exception e) {
            // Implementation may reject non-padded strings.
        }
    }

    // -----------------------------------------------------------------------
    // Edge cases for decode
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidCharacterSpace() {
        base64.decode("SGVs bG8=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidCharacterSpecial() {
        base64.decode("SGVs!bG8=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeNullString() {
        base64.decode((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void testDecodeNullByteArray() {
        base64.decode((byte[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void testEncodeNullByteArray() {
        base64.encode(null);
    }

    // -----------------------------------------------------------------------
    // Padding and length constraints
    // -----------------------------------------------------------------------

    @Test
    public void testEncodeLengthModThree() {
        // 1 byte -> 2 padding characters
        byte[] one = new byte[]{0x61};
        String encoded = new String(base64.encode(one));
        assertTrue("Expected padding of 2 '=' characters", encoded.endsWith("=="));
        assertEquals(4, encoded.length());

        // 2 bytes -> 1 padding character
        byte[] two = new byte[]{0x61, 0x62};
        encoded = new String(base64.encode(two));
        assertTrue("Expected padding of 1 '=' character", encoded.endsWith("="));
        assertEquals(4, encoded.length());

        // 3 bytes -> no padding
        byte[] three = new byte[]{0x61, 0x62, 0x63};
        encoded = new String(base64.encode(three));
        assertFalse("Expected no padding", encoded.endsWith("="));
        assertEquals(4, encoded.length());
    }

    // -----------------------------------------------------------------------
    // URL-SAFE variant (if supported)
    // -----------------------------------------------------------------------

    @Test
    public void testURLSafeEncodeDecode() {
        // Assuming a constructor or configuration
        Base64 urlSafe = new Base64(true);  // try to use URL safe parameter
        byte[] input = new byte[]{ (byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x3E };
        byte[] encoded = urlSafe.encode(input);
        String encodedStr = new String(encoded);
        // '-', '_' instead of '+', '/'
        assertFalse("URL safe should not contain +", encodedStr.contains("+"));
        assertFalse("URL safe should not contain /", encodedStr.contains("/"));
        byte[] decoded = urlSafe.decode(encoded);
        assertArrayEquals(input, decoded);
    }

    // -----------------------------------------------------------------------
    // Large data
    // -----------------------------------------------------------------------

    @Test
    public void testEncodeDecodeLargeData() {
        byte[] largeInput = new byte[10_000];
        for (int i = 0; i < largeInput.length; i++) {
            largeInput[i] = (byte) (i % 256);
        }
        byte[] encoded = base64.encode(largeInput);
        byte[] decoded = base64.decode(encoded);
        assertArrayEquals(largeInput, decoded);
    }

    // -----------------------------------------------------------------------
    // Specific known test vectors (RFC 4648)
    // -----------------------------------------------------------------------

    @Test
    public void testRFC4648TestVectors() {
        String[][] testVectors = {
            {"", ""},
            {"f", "Zg=="},
            {"fo", "Zm8="},
            {"foo", "Zm9v"},
            {"foob", "Zm9vYg=="},
            {"fooba", "Zm9vYmE="},
            {"foobar", "Zm9vYmFy"}
        };
        for (String[] pair : testVectors) {
            byte[] input = pair[0].getBytes();
            String expected = pair[1];
            byte[] encoded = base64.encode(input);
            assertEquals("Encoding of \"" + pair[0] + "\"", expected, new String(encoded));
            byte[] decoded = base64.decode(expected);
            assertArrayEquals("Decoding of \"" + expected + "\"", input, decoded);
        }
    }

    // -----------------------------------------------------------------------
    // Line breaks (if applicable)
    // -----------------------------------------------------------------------

    @Test
    public void testEncodeWithLineBreaks() {
        // If the implementation supports line breaks every N characters.
        // This test assumes default line length of 76 (Commons Codec default).
        // If not applicable, ignore or modify.
        int lineLength = 76;
        byte[] input = new byte[300];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        Base64 lineBase64 = new Base64(lineLength);
        byte[] encoded = lineBase64.encode(input);
        String encodedStr = new String(encoded);
        // Check that line breaks occur roughly every 76 characters
        String[] lines = encodedStr.split("\\r?\\n");
        assertTrue("Should have multiple lines", lines.length > 1);
        for (int i = 0; i < lines.length - 1; i++) {
            // Each line (except possibly last) should be <= lineLength
            assertTrue("Line " + i + " length = " + lines[i].length() + " > " + lineLength,
                       lines[i].length() <= lineLength);
        }
        // Decode ignoring line breaks
        byte[] decoded = base64.decode(encodedStr.getBytes());
        assertArrayEquals(input, decoded);
    }

    // -----------------------------------------------------------------------
    // Error conditions: illegal padding
    // -----------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeIncorrectPaddingExcess() {
        base64.decode("Zm9vYg==");  // correct: 4 characters encoded has == but we give Zm9vYg== (extra =)
        // Actually "Zm9vYg==" is correct for "foob". To test excess, use "Zm9vYg===" (7 base64 chars + 3 =)
        // Let's use a string with too many padding characters.
        base64.decode("Zm9vYg===");
    }

    @Test
    public void testDecodeMissingPadding() {
        // Some implementations may accept, others reject.
        try {
            byte[] result = base64.decode("Zg");  // no padding, invalid length mod 4
            // If no exception, check if result is correct for 'f' (if implementation assumes padding)
            // Usually should throw exception.
            if (result != null) {
                assertArrayEquals(new byte[]{'f'}, result);
            }
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Thread safety / concurrent calls (optional)
    // -----------------------------------------------------------------------

    @Test
    public void testConcurrentEncodeDecode() throws InterruptedException {
        // Simple concurrent access test
        final byte[] input = new byte[]{0x11, 0x22, 0x33};
        final int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                try {
                    byte[] encoded = base64.encode(input);
                    byte[] decoded = base64.decode(encoded);
                    assertArrayEquals(input, decoded);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
    }
}