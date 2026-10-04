package <package_name>; // Replace with actual package from source

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for Base64 encoding/decoding.
 * Targets maximum line/branch coverage and common Defects4J faults.
 */
public class Base64Test {

    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final String EMPTY_STRING = "";

    // Sample test data
    private static final byte[] TEST_BYTES = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private static final String TEST_ENCODED = "AAECAwQFBgcICQoLDA0ODw==";
    private static final byte[] TEST_DECODED = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};

    // ---------------------------------------------------------------
    // Null and empty input tests
    // ---------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void testEncodeNullInput() {
        Base64.encode(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDecodeNullInput() {
        Base64.decode(null);
    }

    @Test
    public void testEncodeEmptyArray() {
        assertArrayEquals(EMPTY_BYTE_ARRAY, Base64.encode(EMPTY_BYTE_ARRAY));
    }

    @Test
    public void testDecodeEmptyString() {
        assertArrayEquals(EMPTY_BYTE_ARRAY, Base64.decode(EMPTY_STRING));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidCharacter() {
        // Assuming invalid character causes exception
        Base64.decode("ABC!DEF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidPadding() {
        // Too much padding
        Base64.decode("AB======");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeIncorrectPadding() {
        // Padding in the middle
        Base64.decode("AB=C");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeLengthNotMultipleOf4() {
        Base64.decode("ABC");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeIllegalCharacterWhitespace() {
        // Spaces are typically not allowed
        Base64.decode("AB C");
    }

    // ---------------------------------------------------------------
    // Basic encoding/decoding round-trip
    // ---------------------------------------------------------------

    @Test
    public void testEncodeDecodeRoundTrip() {
        assertArrayEquals(TEST_BYTES, Base64.decode(Base64.encode(TEST_BYTES)));
    }

    @Test
    public void testEncodeSingleByte() {
        byte[] single = {42};
        String encoded = Base64.encodeToString(single);
        assertArrayEquals(single, Base64.decode(encoded));
    }

    @Test
    public void testEncodeTwoBytes() {
        byte[] two = {1, 2};
        String encoded = Base64.encodeToString(two);
        assertArrayEquals(two, Base64.decode(encoded));
    }

    @Test
    public void testEncodeThreeBytes() {
        byte[] three = {1, 2, 3};
        String encoded = Base64.encodeToString(three);
        assertArrayEquals(three, Base64.decode(encoded));
    }

    @Test
    public void testEncodeAllBytes() {
        byte[] all = new byte[256];
        for (int i = 0; i < 256; i++) {
            all[i] = (byte) i;
        }
        byte[] encoded = Base64.encode(all);
        assertArrayEquals(all, Base64.decode(encoded));
    }

    @Test
    public void testEncodeLargeData() {
        byte[] large = new byte[1024];
        for (int i = 0; i < large.length; i++) {
            large[i] = (byte) (i % 256);
        }
        byte[] encoded = Base64.encode(large);
        assertArrayEquals(large, Base64.decode(encoded));
    }

    // ---------------------------------------------------------------
    // Known encoded strings (if standard table)
    // ---------------------------------------------------------------

    @Test
    public void testKnownDecode() {
        assertArrayEquals(TEST_DECODED, Base64.decode(TEST_ENCODED));
    }

    @Test
    public void testKnownEncode() {
        assertArrayEquals(TEST_ENCODED.getBytes("UTF-8"), Base64.encode(TEST_DECODED));
    }

    // ---------------------------------------------------------------
    // Edge cases for padding
    // ---------------------------------------------------------------

    @Test
    public void testZeroPadding() {
        // 3 bytes input -> 4 chars with no padding needed
        byte[] in = {1, 2, 3};
        String encoded = Base64.encodeToString(in);
        assertFalse(encoded.endsWith("="));
    }

    @Test
    public void testOnePadding() {
        // 2 bytes input -> 3 chars + 1 padding
        byte[] in = {1, 2};
        String encoded = Base64.encodeToString(in);
        assertTrue(encoded.endsWith("="));
    }

    @Test
    public void testTwoPadding() {
        // 1 byte input -> 2 chars + 2 padding
        byte[] in = {1};
        String encoded = Base64.encodeToString(in);
        assertTrue(encoded.endsWith("=="));
    }

    @Test
    public void testDecodeWithSinglePadding() {
        byte[] expected = {1, 2};
        assertArrayEquals(expected, Base64.decode("AQI="));
    }

    @Test
    public void testDecodeWithDoublePadding() {
        byte[] expected = {1};
        assertArrayEquals(expected, Base64.decode("AQ=="));
    }

    // ---------------------------------------------------------------
    // URL-safe variant detection (if present)
    // ---------------------------------------------------------------

    @Test
    public void testUrlSafeEncoding() {
        // If URL safe is available, test it
        byte[] bytes = {0x3E, 0x3C, 0x3D}; // > < =
        String standard = Base64.encodeToString(bytes);
        String urlSafe = Base64.encodeUrlSafeToString(bytes);
        assertFalse(standard.equals(urlSafe)); // Should differ due to chars
        assertArrayEquals(bytes, Base64.decode(urlSafe));
    }

    @Test
    public void testDecodeUrlSafe() {
        // URL safe does not contain + or /
        String urlSafe = "Pjw_PQ=="; // Hypothetical
        // Ensure no exception
        byte[] decoded = Base64.decode(urlSafe);
        assertNotNull(decoded);
        assertTrue(decoded.length > 0);
    }

    // ---------------------------------------------------------------
    // Exception handling for malformed input
    // ---------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidBase64CharacterDash() {
        Base64.decode("ABC-");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidBase64CharacterUnderscore() {
        Base64.decode("ABC_");
    }

    // If implementation allows line breaks, test that
    // But assuming standard no-line-break

    // ---------------------------------------------------------------
    // Ensure no side effects (null after decode)
    // ---------------------------------------------------------------

    @Test
    public void testDecodeReturnsNewArray() {
        byte[] inputIn = TEST_BYTES;
        String encoded = Base64.encodeToString(inputIn);
        byte[] decoded = Base64.decode(encoded);
        assertNotSame(inputIn, decoded);
        assertArrayEquals(inputIn, decoded);
    }

    // ---------------------------------------------------------------
    // Stress test with maximum sized input
    // ---------------------------------------------------------------

    @Test
    public void testEncodeDecodeLargeArray() {
        int size = 10_000;
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (i % 256);
        }
        byte[] encoded = Base64.encode(data);
        byte[] decoded = Base64.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    // ---------------------------------------------------------------
    // Edge: all zeros
    // ---------------------------------------------------------------

    @Test
    public void testEncodeAllZeros() {
        byte[] zeros = new byte[10];
        byte[] encoded = Base64.encode(zeros);
        // Known: AAAAAA... with padding
        assertArrayEquals(zeros, Base64.decode(encoded));
    }

    // ---------------------------------------------------------------
    // Edge: bytes resulting in + and / characters
    // ---------------------------------------------------------------

    @Test
    public void testEncodeBytesWithPlusAndSlash() {
        // Byte sequence that produces '+' and '/' in standard Base64
        byte[] plusSlash = {(byte) 0xFB, (byte) 0xFF, (byte) 0xFF};
        String encoded = Base64.encodeToString(plusSlash);
        assertTrue(encoded.contains("+") || encoded.contains("/")); // may depend
        assertArrayEquals(plusSlash, Base64.decode(encoded));
    }

    // ---------------------------------------------------------------
    // Verify that encoding is deterministic
    // ---------------------------------------------------------------

    @Test
    public void testEncodeDeterministic() {
        byte[] data = {1, 2, 3, 4, 5};
        String first = Base64.encodeToString(data);
        String second = Base64.encodeToString(data);
        assertEquals(first, second);
    }

    // ---------------------------------------------------------------
    // Additional fault triggers: multi-byte alignment
    // ---------------------------------------------------------------

    @Test
    public void testLengthsFrom0To256() {
        for (int len = 0; len <= 256; len++) {
            byte[] data = new byte[len];
            for (int i = 0; i < len; i++) {
                data[i] = (byte) (i % 256);
            }
            byte[] encoded = Base64.encode(data);
            byte[] decoded = Base64.decode(encoded);
            assertArrayEquals("Failed at length " + len, data, decoded);
        }
    }

    @Test
    public void testFourBytesMultiple() {
        byte[] data = {1, 2, 3, 4};
        byte[] encoded = Base64.encode(data);
        assertArrayEquals(data, Base64.decode(encoded));
    }

    @Test
    public void testFiveBytes() {
        byte[] data = {1, 2, 3, 4, 5};
        byte[] encoded = Base64.encode(data);
        assertArrayEquals(data, Base64.decode(encoded));
    }

    @Test
    public void testSevenBytes() {
        byte[] data = {1, 2, 3, 4, 5, 6, 7};
        byte[] encoded = Base64.encode(data);
        assertArrayEquals(data, Base64.decode(encoded));
    }
}