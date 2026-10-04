package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringUtilsTest {

    @Test
    public void testEqualsCharSequence() {
        CharSequence cs1 = "test";
        CharSequence cs2 = "test";
        CharSequence cs3 = "other";
        CharSequence cs4 = "tes";

        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(cs1, null));
        assertFalse(StringUtils.equals(null, cs1));
        assertTrue(StringUtils.equals(cs1, cs2));
        assertFalse(StringUtils.equals(cs1, cs3));
        assertFalse(StringUtils.equals(cs1, cs4));
        assertFalse(StringUtils.equals(cs4, cs1));
    }

    @Test
    public void testNewStringUtf8() {
        byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_8);
        assertEquals("Hello", StringUtils.newStringUtf8(bytes));
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test
    public void testNewStringUtf16() {
        byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_16);
        assertEquals("Hello", StringUtils.newStringUtf16(bytes));
        assertNull(StringUtils.newStringUtf16(null));
    }

    @Test
    public void testNewStringUtf16Le() {
        byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_16LE);
        assertEquals("Hello", StringUtils.newStringUtf16Le(bytes));
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    @Test
    public void testNewStringUtf16Be() {
        byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_16BE);
        assertEquals("Hello", StringUtils.newStringUtf16Be(bytes));
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    @Test
    public void testNewStringUsAscii() {
        byte[] bytes = "Hello".getBytes(StandardCharsets.US_ASCII);
        assertEquals("Hello", StringUtils.newStringUsAscii(bytes));
        assertNull(StringUtils.newStringUsAscii(null));
    }

    @Test
    public void testNewStringIso8859_1() {
        byte[] bytes = "Hello".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals("Hello", StringUtils.newStringIso8859_1(bytes));
        assertNull(StringUtils.newStringIso8859_1(null));
    }

    @Test
    public void testNewStringGeneric() {
        byte[] bytes = "Hello".getBytes(StandardCharsets.UTF_8);
        assertEquals("Hello", StringUtils.newString(bytes, StandardCharsets.UTF_8));
        assertNull(StringUtils.newString(null, StandardCharsets.UTF_8));
    }

    @Test
    public void testGetBytesUtf8() {
        String str = "Hello";
        assertArrayEquals(str.getBytes(StandardCharsets.UTF_8), StringUtils.getBytesUtf8(str));
        assertNull(StringUtils.getBytesUtf8(null));
    }

    @Test
    public void testGetBytesUtf16() {
        String str = "Hello";
        assertArrayEquals(str.getBytes(StandardCharsets.UTF_16), StringUtils.getBytesUtf16(str));
        assertNull(StringUtils.getBytesUtf16(null));
    }

    @Test
    public void testGetBytesUtf16Le() {
        String str = "Hello";
        assertArrayEquals(str.getBytes(StandardCharsets.UTF_16LE), StringUtils.getBytesUtf16Le(str));
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    @Test
    public void testGetBytesUtf16Be() {
        String str = "Hello";
        assertArrayEquals(str.getBytes(StandardCharsets.UTF_16BE), StringUtils.getBytesUtf16Be(str));
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    @Test
    public void testGetBytesUsAscii() {
        String str = "Hello";
        assertArrayEquals(str.getBytes(StandardCharsets.US_ASCII), StringUtils.getBytesUsAscii(str));
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    @Test
    public void testGetBytesIso8859_1() {
        String str = "Hello";
        assertArrayEquals(str.getBytes(StandardCharsets.ISO_8859_1), StringUtils.getBytesIso8859_1(str));
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    @Test
    public void testGetBytesUnchecked() {
        String str = "Hello";
        assertArrayEquals(str.getBytes(StandardCharsets.UTF_8), StringUtils.getBytesUnchecked(str, "UTF-8"));
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUncheckedInvalidCharset() {
        StringUtils.getBytesUnchecked("Hello", "INVALID_CHARSET_NAME");
    }

    @Test
    public void testNewStringByteBuffer() {
        ByteBuffer buffer = ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8));
        assertEquals("Hello", StringUtils.newString(buffer, StandardCharsets.UTF_8));
        assertNull(StringUtils.newString((ByteBuffer) null, StandardCharsets.UTF_8));
    }

    @Test
    public void testConstructor() {
        // StringUtils has a public constructor in some versions, or default constructor
        StringUtils utils = new StringUtils();
        assertNotNull(utils);
    }
}