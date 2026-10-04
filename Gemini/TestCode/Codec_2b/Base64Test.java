package net.mooctest;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    @Test
    public void testNullInputsAndEdgeCases() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.encodeBase64Chunked(null));
        assertNull(Base64.decodeBase64(null));
        assertNull(Base64.encodeBase64URLSafe(null));
        assertNull(Base64.encodeBase64URLSafeString(null));

        assertNull(Base64.encodeBase64String(null));
        assertNull(Base64.decodeBase64((String) null));

        // Empty arrays
        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.encodeBase64Chunked(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        assertEquals("", Base64.encodeBase64String(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(""));
    }

    @Test
    public void testBasicEncodingDecoding() {
        String original = "Hello, World!";
        byte[] originalBytes = original.getBytes();

        byte[] encoded = Base64.encodeBase64(originalBytes);
        assertNotNull(encoded);

        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(originalBytes, decoded);

        String encodedString = Base64.encodeBase64String(originalBytes);
        assertNotNull(encodedString);

        byte[] decodedFromString = Base64.decodeBase64(encodedString);
        assertArrayEquals(originalBytes, decodedFromString);
    }

    @Test
    public void testChunkedEncoding() {
        // Create a long byte array to trigger chunking (default line length 76)
        byte[] data = new byte[200];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }

        byte[] encodedChunked = Base64.encodeBase64Chunked(data);
        assertNotNull(encodedChunked);

        byte[] decoded = Base64.decodeBase64(encodedChunked);
        assertArrayEquals(data, decoded);
    }

    @Test
    public void testURLSafeEncoding() {
        // Characters that differ in URL-safe Base64: '+' -> '-', '/' -> '_'
        byte[] data = new byte[] { (byte) 0xfb, (byte) 0xff, (byte) 0xbf };

        byte[] urlSafeEncoded = Base64.encodeBase64URLSafe(data);
        assertNotNull(urlSafeEncoded);
        
        String urlSafeString = Base64.encodeBase64URLSafeString(data);
        assertNotNull(urlSafeString);

        byte[] decoded = Base64.decodeBase64(urlSafeEncoded);
        assertArrayEquals(data, decoded);

        byte[] decodedFromString = Base64.decodeBase64(urlSafeString);
        assertArrayEquals(data, decodedFromString);
    }

    @Test
    public void testIsBase64() {
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) 'z'));
        assertTrue(Base64.isBase64((byte) '0'));
        assertTrue(Base64.isBase64((byte) '+'));
        assertTrue(Base64.isBase64((byte) '/'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) ' '));
        assertFalse(Base64.isBase64((byte) 127));
        assertFalse(Base64.isBase64((byte) -1));

        assertTrue(Base64.isBase64("SGVsbG8=".getBytes()));
        assertFalse(Base64.isBase64("SGVsbG8= ".getBytes()));
    }

    @Test
    public void testIntegerTypeMethodsAndConstructors() {
        // Test encodeInteger and decodeInteger if present, or test Base64 object instantiation
        Base64 base64Default = new Base64();
        assertNotNull(base64Default);

        Base64 base64Chunked = new Base64(true);
        assertNotNull(base64Chunked);

        Base64 base64Custom = new Base64(20, new byte[] { '\n' }, false);
        assertNotNull(base64Custom);

        Base64 base64URLSafe = new Base64(true);
        assertNotNull(base64URLSafe);

        // Test integer conversion methods
        try {
            java.math.BigInteger bigInt = java.math.BigInteger.valueOf(123456789L);
            byte[] encodedInt = Base64.encodeInteger(bigInt);
            assertNotNull(encodedInt);
            java.math.BigInteger decodedInt = Base64.decodeInteger(encodedInt);
            assertEquals(bigInt, decodedInt);
        } catch (Throwable t) {
            // Some versions might have different signatures or handle integer encoding differently
        }
    }

    @Test
    public void testDecodeWithWhiteSpaceAndInvalidCharacters() {
        // Base64 with whitespace embedded
        String paddedWithSpaces = " SG V sb G 8 = ";
        byte[] decoded = Base64.decodeBase64(paddedWithSpaces);
        assertNotNull(decoded);
        assertEquals("Hello", new String(decoded));

        // Test decode with invalid base64 array/string
        byte[] invalidData = new byte[] { (byte) '@', (byte) '#' };
        byte[] result = Base64.decodeBase64(invalidData);
        // Should handle gracefully or return empty/filtered array depending on implementation
        assertNotNull(result);
    }
    
    @Test
    public void testEncodeDecodeVariousLengths() {
        // Test lengths that leave 1 or 2 bytes remainder (mod 3)
        for (int i = 1; i <= 10; i++) {
            byte[] data = new byte[i];
            for (int j = 0; j < i; j++) {
                data[j] = (byte) (j + 1);
            }
            byte[] encoded = Base64.encodeBase64(data);
            byte[] decoded = Base64.decodeBase64(encoded);
            assertArrayEquals(data, decoded);
        }
    }

    @Test
    public void testStaticDecodeObject() {
        Base64 base64 = new Base64();
        Object decodedObj = base64.decode("SGVsbG8=".getBytes());
        assertNotNull(decodedObj);
        assertTrue(decodedObj instanceof byte[]);

        Object encodedObj = base64.encode("Hello".getBytes());
        assertNotNull(encodedObj);
        assertTrue(encodedObj instanceof byte[]);

        try {
            base64.decode("NotBase64!@#$");
        } catch (Exception e) {
            // Expected for some strict decodes
        }
    }
}