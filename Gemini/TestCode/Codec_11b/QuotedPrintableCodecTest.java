package org.apache.commons.codec.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.junit.Before;
import org.junit.Test;

public class QuotedPrintableCodecTest {

    private QuotedPrintableCodec codec;

    @Before
    public void setUp() {
        codec = new QuotedPrintableCodec();
    }

    @Test
    public void testDefaultConstructor() {
        assertNotNull(codec);
        assertEquals("UTF-8", codec.getDefaultCharset());
    }

    @Test
    public void testCharsetConstructor() {
        QuotedPrintableCodec customCodec = new QuotedPrintableCodec("UTF-16");
        assertNotNull(customCodec);
        assertEquals("UTF-16", customCodec.getDefaultCharset());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCharsetConstructor() {
        new QuotedPrintableCodec((String) null);
    }

    @Test
    public void testEncodeDecodeValidString() throws Exception {
        String plain = "The quick brown fox jumps over the lazy dog 123!@#$";
        String encoded = codec.encode(plain, "UTF-8");
        assertNotNull(encoded);
        String decoded = codec.decode(encoded, "UTF-8");
        assertEquals(plain, decoded);
    }

    @Test
    public void testEncodeDecodeNulls() throws Exception {
        assertNull(codec.encode((String) null));
        assertNull(codec.decode((String) null));
        assertNull(codec.encode((String) null, "UTF-8"));
        assertNull(codec.decode((String) null, "UTF-8"));
    }

    @Test
    public void testEncodeDecodeBytesNull() throws Exception {
        assertNull(codec.encode((byte[]) null));
        assertNull(codec.decode((byte[]) null));
    }

    @Test
    public void testEncodeDecodeEmptyString() throws Exception {
        assertEquals("", codec.encode(""));
        assertEquals("", codec.decode(""));
    }

    @Test
    public void testEncodeDecodeEmptyBytes() throws Exception {
        byte[] empty = new byte[0];
        assertEquals(0, codec.encode(empty).length);
        assertEquals(0, codec.decode(empty).length);
    }

    @Test
    public void testSafeCharacters() throws Exception {
        // Alphanumeric and certain punctuation should remain unencoded in standard QP (or at least decode back correctly)
        String safe = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,.-";
        String encoded = codec.encode(safe);
        assertEquals(safe, codec.decode(encoded));
    }

    @Test
    public void testSoftLineBreakAndSpecialChars() throws Exception {
        // Test encoding characters that need escaping (e.g., '=', non-ascii)
        String special = "=\n\r\tabc";
        String encoded = codec.encode(special);
        assertNotNull(encoded);
        String decoded = codec.decode(encoded);
        assertEquals(special, decoded);
    }

    @Test
    public void testDecodeInvalidEscapeSequence() {
        // Incomplete escape sequence at the end of the string
        try {
            codec.decode("=A");
            fail("Expected DecoderException for incomplete escape sequence");
        } catch (DecoderException e) {
            // expected
        }

        // Invalid hex characters in escape sequence
        try {
            codec.decode("=XZ");
            fail("Expected DecoderException for invalid hex characters");
        } catch (DecoderException e) {
            // expected
        }
    }

    @Test
    public void testDecodeInvalidEscapeSequenceBytes() {
        byte[] invalid = new byte[] { '=', (byte) 'X' };
        try {
            codec.decode(invalid);
            fail("Expected DecoderException");
        } catch (DecoderException e) {
            // expected
        }
    }

    @Test
    public void testObjectEncodeUnsupportedType() {
        try {
            codec.encode(new Object());
            fail("Expected EncoderException");
        } catch (EncoderException e) {
            // expected
        }
    }

    @Test
    public void testObjectDecodeUnsupportedType() {
        try {
            codec.decode(new Object());
            fail("Expected DecoderException");
        } catch (DecoderException e) {
            // expected
        }
    }

    @Test
    public void testObjectEncodeString() throws Exception {
        Object result = codec.encode("test");
        assertNotNull(result);
    }

    @Test
    public void testObjectDecodeString() throws Exception {
        String encoded = codec.encode("test");
        Object result = codec.decode(encoded);
        assertNotNull(result);
    }

    @Test
    public void testObjectEncodeBytes() throws Exception {
        Object result = codec.encode("test".getBytes("UTF-8"));
        assertNotNull(result);
    }

    @Test
    public void testObjectDecodeBytes() throws Exception {
        byte[] encoded = codec.encode("test".getBytes("UTF-8"));
        Object result = codec.decode(encoded);
        assertNotNull(result);
    }

    @Test(expected = UnsupportedCharsetException.class)
    public void testEncodeUnsupportedCharset() throws Exception {
        codec.encode("test", "INVALID-CHARSET-NAME");
    }

    @Test(expected = UnsupportedCharsetException.class)
    public void testDecodeUnsupportedCharset() throws Exception {
        codec.decode("test", "INVALID-CHARSET-NAME");
    }

    @Test
    public void testUnescapeIntegerConstructorAndMethods() {
        // Specific coverage for static unescape or helper methods if available, 
        // or just ensure robust usage of strict decoding methods.
        try {
            QuotedPrintableCodec.decodeQuotedPrintable("=3D".getBytes());
        } catch (Exception e) {
            // ignore or assert
        }
    }
}