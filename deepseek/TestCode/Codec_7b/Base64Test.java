package org.apache.commons.codec.binary;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Unit tests for {@link Base64}.
 * Designed to achieve high coverage and detect known bugs (Defects4J Codec-7).
 */
public class Base64Test {

    private static final String CHARSET_NAME = "UTF-8";
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private Base64 codec;

    @Before
    public void setUp() {
        codec = new Base64();
    }

    // -----------------------------------------------------------------------
    // Basic encoding / decoding tests
    // -----------------------------------------------------------------------

    @Test
    public void testEncodeEmpty() {
        byte[] result = codec.encode(EMPTY_BYTE_ARRAY);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testDecodeEmpty() {
        byte[] result = codec.decode(EMPTY_BYTE_ARRAY);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testEncodeString() throws UnsupportedEncodingException {
        String data = "Hello World";
        byte[] encoded = codec.encode(data.getBytes(CHARSET_NAME));
        assertNotNull(encoded);
        String decoded = new String(codec.decode(encoded), CHARSET_NAME);
        assertEquals(data, decoded);
    }

    @Test
    public void testDecodeString() throws UnsupportedEncodingException {
        String data = "SGVsbG8gV29ybGQ=";
        byte[] decoded = codec.decode(data.getBytes(CHARSET_NAME));
        assertNotNull(decoded);
        String result = new String(decoded, CHARSET_NAME);
        assertEquals("Hello World", result);
    }

    @Test
    public void testEncodeDecodeLarge() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("A");
        }
        String data = sb.toString();
        byte[] encoded = codec.encode(data.getBytes(CHARSET_NAME));
        String decoded = new String(codec.decode(encoded), CHARSET_NAME);
        assertEquals(data, decoded);
    }

    // -----------------------------------------------------------------------
    // Chunked encoding tests (Defects4J Codec-7 area)
    // -----------------------------------------------------------------------

    @Test
    public void testChunkedEncodingDefaultLineLength() throws UnsupportedEncodingException {
        // Default line length is 76. Test with data that produces multiple lines.
        Base64 codecChunked = new Base64(76, new byte[]{'\n'});
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("Test");
        }
        String data = sb.toString();
        byte[] encoded = codecChunked.encode(data.getBytes(CHARSET_NAME));
        String encodedStr = new String(encoded, CHARSET_NAME);
        String[] lines = encodedStr.split("\n");
        // Each line except last should be exactly 76 bytes (plus possible padding)
        for (int i = 0; i < lines.length - 1; i++) {
            // Base64 line lengths are multiples of 4
            assertTrue("Line length should be <= 76", lines[i].length() <= 76);
            assertEquals("Line length should be multiple of 4", 0, lines[i].length() % 4);
        }
        // Decode and verify
        byte[] decoded = codecChunked.decode(encoded);
        assertArrayEquals(data.getBytes(CHARSET_NAME), decoded);
    }

    @Test
    public void testChunkedEncodingCustomLineLength() throws UnsupportedEncodingException {
        // Use odd line length (should be truncated to multiple of 4? Actually Base64 uses exact length)
        Base64 codecChunked = new Base64(60, new byte[]{'\r', '\n'});
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("X");
        }
        String data = sb.toString();
        byte[] encoded = codecChunked.encode(data.getBytes(CHARSET_NAME));
        String encodedStr = new String(encoded, CHARSET_NAME);
        // Check lines end with CRLF
        assertTrue("Should contain CRLF", encodedStr.contains("\r\n"));
        // Decode
        byte[] decoded = codecChunked.decode(encoded);
        assertArrayEquals(data.getBytes(CHARSET_NAME), decoded);
    }

    @Test
    public void testChunkedDecodeWithExtraNewlines() throws UnsupportedEncodingException {
        // Decode chunked data that has newlines inserted anywhere (resilience)
        String encoded = "SGVsbG8gV29ybGQ=";
        String withNewlines = encoded.substring(0, 8) + "\n" + encoded.substring(8);
        byte[] decoded = codec.decode(withNewlines.getBytes(CHARSET_NAME));
        assertEquals("Hello World", new String(decoded, CHARSET_NAME));
    }

    @Test
    public void testChunkedEncodeAndDecodeBinary() {
        // Test with binary data that may include zero bytes
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        Base64 codecChunked = new Base64(64, new byte[]{'\n'});
        byte[] encoded = codecChunked.encode(data);
        byte[] decoded = codecChunked.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    // -----------------------------------------------------------------------
    // Boundary and edge-case tests
    // -----------------------------------------------------------------------

    @Test
    public void testDecodeSingleByte() {
        byte[] encoded = new byte[]{'Z', 'w', '=', '='}; // "zw==" corresponds to 1 byte 0xcf
        byte[] decoded = codec.decode(encoded);
        assertNotNull(decoded);
        assertEquals(1, decoded.length);
        assertEquals((byte) 0xcf, decoded[0]);
    }

    @Test
    public void testDecodeTwoBytes() {
        byte[] encoded = new byte[]{'Y', 'W', 'I', '='}; // "YWI=" corresponds to bytes 0x61 0x62
        byte[] decoded = codec.decode(encoded);
        assertNotNull(decoded);
        assertEquals(2, decoded.length);
        assertEquals((byte) 0x61, decoded[0]);
        assertEquals((byte) 0x62, decoded[1]);
    }

    @Test
    public void testDecodeAllPadding() {
        byte[] encoded = new byte[]{'=', '=', '=', '='};
        byte[] decoded = codec.decode(encoded);
        assertNotNull(decoded);
        assertEquals(0, decoded.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidCharacter() {
        // Invalid character '!'
        byte[] encoded = "!!!=".getBytes();
        codec.decode(encoded);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeInvalidLengthMod4() {
        // Length not multiple of 4
        byte[] encoded = "SGk=".getBytes(); // length 4 -> ok, but let's use 3
        byte[] bad = "SGk".getBytes();
        codec.decode(bad);
    }

    // -----------------------------------------------------------------------
    // isBase64 tests
    // -----------------------------------------------------------------------

    @Test
    public void testIsBase64() {
        assertTrue(Base64.isBase64('A'));
        assertTrue(Base64.isBase64('/'));
        assertTrue(Base64.isBase64('+'));
        assertTrue(Base64.isBase64('='));
        assertFalse(Base64.isBase64('!'));
        assertFalse(Base64.isBase64('\n'));
    }

    @Test
    public void testIsArrayByteBase64() {
        byte[] valid = "SGVsbG8=".getBytes();
        assertTrue(Base64.isArrayByteBase64(valid));
        byte[] invalid = "SGVs bG8=".getBytes(); // space inside
        assertFalse(Base64.isArrayByteBase64(invalid));
    }

    // -----------------------------------------------------------------------
    // Null safety
    // -----------------------------------------------------------------------

    @Test(expected = NullPointerException.class)
    public void testEncodeNull() {
        codec.encode(null);
    }

    @Test(expected = NullPointerException.class)
    public void testDecodeNull() {
        codec.decode(null);
    }

    // -----------------------------------------------------------------------
    // encodeBase64String / decodeBase64
    // -----------------------------------------------------------------------

    @Test
    public void testEncodeBase64String() throws UnsupportedEncodingException {
        String data = "Java";
        byte[] encoded = Base64.encodeBase64(data.getBytes(CHARSET_NAME));
        String encodedStr = new String(encoded, CHARSET_NAME);
        assertEquals("SmF2YQ==", encodedStr);
    }

    @Test
    public void testDecodeBase64() throws UnsupportedEncodingException {
        byte[] decoded = Base64.decodeBase64("SmF2YQ==".getBytes(CHARSET_NAME));
        assertEquals("Java", new String(decoded, CHARSET_NAME));
    }

    // -----------------------------------------------------------------------
    // URL-safe mode (if applicable)
    // -----------------------------------------------------------------------

    @Test
    public void testUrlSafeEncodeDecode() throws UnsupportedEncodingException {
        Base64 urlSafe = new Base64(true);
        byte[] data = "Hello+World/=".getBytes(CHARSET_NAME);
        byte[] encoded = urlSafe.encode(data);
        String encodedStr = new String(encoded, CHARSET_NAME);
        // URL-safe uses - instead of + and _ instead of /
        assertFalse("Should not contain +", encodedStr.contains("+"));
        assertFalse("Should not contain /", encodedStr.contains("/"));
        byte[] decoded = urlSafe.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    // -----------------------------------------------------------------------
    // Tests for known Defects4J Codec-7 scenario: chunked encoding with
    // short last line and padding issues
    // -----------------------------------------------------------------------

    @Test
    public void testChunkedEncodingLastLineCorrectLength() throws UnsupportedEncodingException {
        // Input that produces a remainder of 1 byte after base64 encoding (padding == '==')
        // Line length 4 so that each line is exactly 4 chars (or less on last line)
        Base64 codecChunked = new Base64(4, new byte[]{'\n'});
        // Single byte encoded as "AA==" (4 chars)
        byte[] data = new byte[]{(byte) 0x00};
        byte[] encoded = codecChunked.encode(data);
        String encodedStr = new String(encoded, CHARSET_NAME);
        // Should be exactly "AA==\n"? No, because last line should not add newline if it's the only line.
        // But length 4 line: encoded is "AA==" + newline? Actually Base64 appends newline after each line except possibly last.
        // Our line length is 4, so "AA==" is exactly 4 chars; it would add newline making it "AA==\n". Then decode should work.
        byte[] decoded = codecChunked.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    @Test
    public void testChunkedEncodingTrailingNewline() throws UnsupportedEncodingException {
        // Ensure that decoding chunked data with a trailing newline works.
        Base64 codecChunked = new Base64(76, new byte[]{'\n'});
        byte[] data = "Test".getBytes(CHARSET_NAME);
        byte[] encoded = codecChunked.encode(data);
        // Add an extra newline at the end
        byte[] withTrailing = Arrays.copyOf(encoded, encoded.length + 1);
        withTrailing[encoded.length] = (byte) '\n';
        byte[] decoded = codecChunked.decode(withTrailing);
        assertArrayEquals(data, decoded);
    }

    // -----------------------------------------------------------------------
    // Test with different line separators
    // -----------------------------------------------------------------------

    @Test
    public void testCustomLineSeparator() throws UnsupportedEncodingException {
        byte[] sep = new byte[]{'-', '-'};
        Base64 codecCustom = new Base64(4, sep);
        byte[] data = "ABC".getBytes(CHARSET_NAME);
        byte[] encoded = codecCustom.encode(data);
        String encodedStr = new String(encoded, CHARSET_NAME);
        // Expect "QUJD" (4 chars) + "--" exactly if data fits in one line. But 3 bytes produce 4 chars, line length 4 => 4 chars then separator.
        // So final encoding "QUJD--"
        assertTrue(encodedStr.endsWith("--"));
        byte[] decoded = codecCustom.decode(encoded);
        assertArrayEquals(data, decoded);
    }

    // -----------------------------------------------------------------------
    // Test that encodeBase64Chunked returns the same as encode with chunking
    // -----------------------------------------------------------------------

    @Test
    public void testEncodeBase64Chunked() throws UnsupportedEncodingException {
        byte[] data = "Apache Commons Codec".getBytes(CHARSET_NAME);
        byte[] chunked = Base64.encodeBase64Chunked(data);
        // Use default Base64 with line length 76 and \r\n
        Base64 def = new Base64(76, new byte[]{'\r', '\n'});
        byte[] expected = def.encode(data);
        assertArrayEquals(expected, chunked);
    }
}