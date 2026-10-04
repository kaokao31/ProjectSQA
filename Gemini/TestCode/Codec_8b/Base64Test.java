package net.mooctest;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    @Test
    public void testNullInputsAndEmpty() {
        assertNull(Base64.encode(null));
        assertNull(Base64.encode(null, 0, 0));
        assertNull(Base64.decode(null));
        assertNull(Base64.decode(null, 0, 0));
        assertNull(Base64.decodeFast(null));

        assertArrayEquals(new byte[0], Base64.encode(new byte[0]));
        assertArrayEquals(new byte[0], Base64.encode(new byte[0], 0, 0));
        assertArrayEquals(new byte[0], Base64.decode(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decode(new byte[0], 0, 0));
        assertArrayEquals(new byte[0], Base64.decodeFast(new byte[0]));
        
        assertEquals("", Base64.encodeToString(new byte[0]));
        assertNull(Base64.encodeToString(null));
        assertNull(Base64.decodeFast((String) null));
        assertNull(Base64.decode((String) null));
        assertArrayEquals(new byte[0], Base64.decodeFast(""));
        assertArrayEquals(new byte[0], Base64.decode(""));
    }

    @Test
    public void testBasicEncodingDecoding() {
        String original = "Hello, World! This is a test for Base64 encoding and decoding.";
        byte[] bytes = original.getBytes();

        byte[] encoded = Base64.encode(bytes);
        assertNotNull(encoded);

        byte[] decoded = Base64.decode(encoded);
        assertArrayEquals(bytes, decoded);

        String encodedStr = Base64.encodeToString(bytes);
        assertNotNull(encodedStr);

        byte[] decodedFromStr = Base64.decode(encodedStr);
        assertArrayEquals(bytes, decodedFromStr);

        byte[] decodedFastFromStr = Base64.decodeFast(encodedStr);
        assertArrayEquals(bytes, decodedFastFromStr);
    }

    @Test
    public void testPaddingVariations() {
        // Test different lengths to trigger 0, 1, and 2 padding characters
        byte[] b1 = "A".getBytes(); // 1 char -> 2 padding chars needed
        byte[] b2 = "AB".getBytes(); // 2 chars -> 1 padding char needed
        byte[] b3 = "ABC".getBytes(); // 3 chars -> 0 padding chars

        testRoundtrip(b1);
        testRoundtrip(b2);
        testRoundtrip(b3);
    }

    @Test
    public void testLineBreaks() {
        // Generate a longer byte array to trigger line breaking  logic in encoders
        byte[] data = new byte[1000];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }

        byte[] encodedMime = Base64.encodeMime(data);
        assertNotNull(encodedMime);
        byte[] decodedMime = Base64.decodeMime(encodedMime);
        assertArrayEquals(data, decodedMime);

        byte[] encodedUrl = Base64.encodeUrlSafe(data);
        assertNotNull(encodedUrl);
        byte[] decodedUrl = Base64.decode(encodedUrl);
        assertArrayEquals(data, decodedUrl);
        
        String mimeStr = Base64.encodeMimeToString(data);
        assertNotNull(mimeStr);
        byte[] decodedMimeStr = Base64.decodeMime(mimeStr);
        assertArrayEquals(data, decodedMimeStr);
    }

    @Test
    public void testInvalidInputsAndEdgeCases() {
        // Invalid characters, bad lengths, out of bounds
        try {
            Base64.decode("Invalid@Char#");
        } catch (Exception e) {
            // Expected for strict decode
        }

        try {
            Base64.decode(new byte[]{'A', 'B'}, 0, 3); // out of bounds / invalid length
            fail("Expected exception for invalid range");
        } catch (Exception e) {
            // Expected
        }

        try {
            Base64.encode(new byte[]{1, 2}, -1, 1);
            fail("Expected exception for negative offset");
        } catch (Exception e) {
            // Expected
        }

        try {
            Base64.encode(new byte[]{1, 2}, 0, -1);
            fail("Expected exception for negative length");
        } catch (Exception e) {
            // Expected
        }
        
        try {
            Base64.encode(new byte[]{1, 2}, 1, 5);
            fail("Expected exception for range overflow");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testUrlSafeVariants() {
        byte[] data = new byte[] { (byte)0xff, (byte)0xef, (byte)0xdf, (byte)0xcf };
        byte[] encoded = Base64.encodeUrlSafe(data);
        assertNotNull(encoded);
        byte[] decoded = Base64.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    @Test
    public void testDecodeFastVariants() {
        String encoded = Base64.encodeToString("Testing Fast Decode!".getBytes());
        byte[] decoded = Base64.decodeFast(encoded);
        assertArrayEquals("Testing Fast Decode!".getBytes(), decoded);
        
        byte[] decodedBytes = Base64.decodeFast(encoded.getBytes());
        assertArrayEquals("Testing Fast Decode!".getBytes(), decodedBytes);
    }

    private void testRoundtrip(byte[] input) {
        byte[] enc = Base64.encode(input);
        byte[] dec = Base64.decode(enc);
        assertArrayEquals(input, dec);

        String encStr = Base64.encodeToString(input);
        byte[] decStr = Base64.decode(encStr);
        assertArrayEquals(input, decStr);
    }
}