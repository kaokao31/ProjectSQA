package net.mooctest;

import org.junit.Test;
import static org.junit.Assert.*;

public class Base64Test {

    @Test
    public void testNullInputsAndEdgeCases() {
        assertNull(Base64.encodeBase64(null));
        assertNull(Base64.encodeBase64String(null));
        assertNull(Base64.encodeBase64URLSafe(null));
        assertNull(Base64.encodeBase64URLSafeString(null));
        assertNull(Base64.decodeBase64((byte[]) null));
        assertNull(Base64.decodeBase64((String) null));

        assertArrayEquals(new byte[0], Base64.encodeBase64(new byte[0]));
        assertEquals("", Base64.encodeBase64String(new byte[0]));
        assertArrayEquals(new byte[0], Base64.encodeBase64URLSafe(new byte[0]));
        assertEquals("", Base64.encodeBase64URLSafeString(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(new byte[0]));
        assertArrayEquals(new byte[0], Base64.decodeBase64(""));
    }

    @Test
    public void testIsArrayByteBase64() {
        assertFalse(Base64.isArrayByteBase64(null));
        
        byte[] validBytes = "SGVsbG8gV29ybGQ=".getBytes();
        assertTrue(Base64.isArrayByteBase64(validBytes));

        // Test non-base64 characters including whitespace and invalid chars
        byte[] invalidBytes = new byte[] { (byte) 'A', (byte) 'B', (byte) 127, (byte) -1 };
        assertFalse(Base64.isArrayByteBase64(invalidBytes));

        byte[] whitespaceBytes = new byte[] { (byte) 'A', (byte) 'B', (byte) ' ', (byte) '\n', (byte) '\r', (byte) '\t' };
        assertTrue(Base64.isArrayByteBase64(whitespaceBytes));
    }

    @Test
    public void testEncodeAndDecodeBasic() {
        String original = "Hello, World!";
        byte[] originalBytes = original.getBytes();

        // Standard encode
        byte[] encoded = Base64.encodeBase64(originalBytes);
        assertNotNull(encoded);

        // Standard decode
        byte[] decoded = Base64.decodeBase64(encoded);
        assertArrayEquals(originalBytes, decoded);

        // String variants
        String encodedStr = Base64.encodeBase64String(originalBytes);
        assertNotNull(encodedStr);
        byte[] decodedFromString = Base64.decodeBase64(encodedStr);
        assertArrayEquals(originalBytes, decodedFromString);
    }

    @Test
    public void testEncodeChunked() {
        String original = "This is a very long string that should definitely trigger chunking in the Base64 encoder to ensure all branches of chunked encoding are properly executed and tested.";
        byte[] originalBytes = original.getBytes();

        byte[] encodedChunked = Base64.encodeBase64Chunked(originalBytes);
        assertNotNull(encodedChunked);

        byte[] decoded = Base64.decodeBase64(encodedChunked);
        assertArrayEquals(originalBytes, decoded);
    }

    @Test
    public void testURLSafeEncodeAndDecode() {
        // Special chars that differ in URL safe vs standard base64: '+' -> '-', '/' -> '_'
        byte[] data = new byte[] { (byte) 0xfb, (byte) 0xff, (byte) 0xbf }; // yields + and / in standard

        byte[] urlSafeEncoded = Base64.encodeBase64URLSafe(data);
        assertNotNull(urlSafeEncoded);
        String urlSafeStr = Base64.encodeBase64URLSafeString(data);
        assertNotNull(urlSafeStr);

        byte[] decoded = Base64.decodeBase64(urlSafeEncoded);
        assertArrayEquals(data, decoded);

        byte[] decodedStr = Base64.decodeBase64(urlSafeStr);
        assertArrayEquals(data, decodedStr);
    }

    @Test
    public void testDecodeBase64WithPaddingAndWhitespace() {
        // Test decoding strings with whitespace, padding, etc.
        String padded = "  SGVs bG8= \r\n";
        byte[] decoded = Base64.decodeBase64(padded);
        assertNotNull(decoded);
        assertEquals("Hel", new String(decoded));
    }

    @Test
    public void testStaticIntegerMethodsAndInstantiation() {
        // Test helper static methods if present (like isBase64, discardWhitespace, etc.)
        assertTrue(Base64.isBase64((byte) 'A'));
        assertTrue(Base64.isBase64((byte) '='));
        assertFalse(Base64.isBase64((byte) '#'));

        // Test class instantiation just in case
        Base64 base64 = new Base64();
        assertNotNull(base64);
        
        Base64 chunkedBase64 = new Base64(true);
        assertNotNull(chunkedBase64);

        Base64 customBase64 = new Base64(76, new byte[] { '\n' });
        assertNotNull(customBase64);
        
        Base64 urlSafeBase64 = new Base64(10, new byte[] { '\n' }, true);
        assertNotNull(urlSafeBase64);
    }

    @Test
    public void testDecodeIntegerOrInvalidLengths() {
        // Test decoding invalid length arrays or malicious inputs
        byte[] invalidLength = new byte[] { (byte) 'A' }; // 1 char, invalid base64 input length
        byte[] result = Base64.decodeBase64(invalidLength);
        // Depending on strictness, might return empty or null or truncated
        assertNotNull(result);
    }

    @Test
    public void testEncodeDecodeVariousLengths() {
        // Test lengths % 3 == 0, 1, 2
        for (int i = 1; i <= 10; i++) {
            byte[] input = new byte[i];
            for (int j = 0; j < i; j++) {
                input[j] = (byte) j;
            }
            byte[] enc = Base64.encodeBase64(input);
            byte[] dec = Base64.decodeBase64(enc);
            assertArrayEquals(input, dec);
        }
    }
}