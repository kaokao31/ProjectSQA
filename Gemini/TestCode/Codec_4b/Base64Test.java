package net.mooctest;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    @Test
    public void testNullInputsAndEmpty() {
        assertNull(Base64.encode(null));
        assertNull(Base64.decode(null));
        
        byte[] emptyBytes = new byte[0];
        assertArrayEquals(emptyBytes, Base64.encode(emptyBytes));
        assertArrayEquals(emptyBytes, Base64.decode(emptyBytes));
        
        assertEquals("", Base64.encodeToString(emptyBytes));
        assertNull(Base64.encodeToString(null));
    }

    @Test
    public void testBasicEncodingAndDecoding() {
        String original = "Hello, World!";
        byte[] bytes = original.getBytes();
        
        byte[] encoded = Base64.encode(bytes);
        assertNotNull(encoded);
        
        byte[] decoded = Base64.decode(encoded);
        assertArrayEquals(bytes, decoded);
        assertEquals(original, new String(decoded));
        
        String encodedStr = Base64.encodeToString(bytes);
        assertNotNull(encodedStr);
        assertEquals(encodedStr, new String(encoded));
    }

    @Test
    public void testPaddingCases() {
        // 1 byte -> needs 2 padding '='
        byte[] oneByte = new byte[] { 0x01 };
        byte[] enc1 = Base64.encode(oneByte);
        assertArrayEquals(oneByte, Base64.decode(enc1));

        // 2 bytes -> needs 1 padding '='
        byte[] twoBytes = new byte[] { 0x01, 0x02 };
        byte[] enc2 = Base64.encode(twoBytes);
        assertArrayEquals(twoBytes, Base64.decode(enc2));

        // 3 bytes -> no padding
        byte[] threeBytes = new byte[] { 0x01, 0x02, 0x03 };
        byte[] enc3 = Base64.encode(threeBytes);
        assertArrayEquals(threeBytes, Base64.decode(enc3));
    }

    @Test
    public void testVariousLengthsAndValues() {
        // Test all possible byte values from -128 to 127
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) (i - 128);
        }
        
        byte[] encoded = Base64.encode(allBytes);
        byte[] decoded = Base64.decode(encoded);
        assertArrayEquals(allBytes, decoded);
    }

    @Test
    public void testStringMethods() {
        String testStr = "Defects4J Base64 Test Suite";
        String encoded = Base64.encodeToString(testStr.getBytes());
        assertNotNull(encoded);
        
        // Assuming there might be a decodeToObject or specific string decoding if available,
        // but let's test what's standard.
        byte[] decodedBytes = Base64.decode(encoded.getBytes());
        assertEquals(testStr, new String(decodedBytes));
    }

    @Test
    public void testInvalidCharactersAndPadding() {
        // Invalid characters or malformed length should be handled gracefully or throw RuntimeException
        try {
            Base64.decode(new byte[] { 0x01 }); // Too short or invalid
        } catch (Exception e) {
            // Expected for some implementations
        }
        
        try {
            // Invalid base64 character like '!'
            Base64.decode("AB!C".getBytes());
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testInstantiation() {
        // Just in case there's a private or public constructor
        Base64 b64 = new Base64();
        assertNotNull(b64);
    }
}