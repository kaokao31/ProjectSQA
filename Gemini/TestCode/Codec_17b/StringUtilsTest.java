package org.apache.commons.codec.binary;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringUtilsTest {

    @Test
    public void testGetBytesIso8859_1WithValidString() {
        byte[] bytes = StringUtils.getBytesIso8859_1("ABC");
        assertNotNull(bytes);
        assertArrayEquals(new byte[] { 65, 66, 67 }, bytes);
    }

    @Test
    public void testGetBytesIso8859_1WithNull() {
        byte[] bytes = StringUtils.getBytesIso8859_1(null);
        assertNull(bytes);
    }

    @Test
    public void testGetBytesUsAsciiWithValidString() {
        byte[] bytes = StringUtils.getBytesUsAscii("ABC");
        assertNotNull(bytes);
        assertArrayEquals(new byte[] { 65, 66, 67 }, bytes);
    }

    @Test
    public void testGetBytesUsAsciiWithNull() {
        byte[] bytes = StringUtils.getBytesUsAscii(null);
        assertNull(bytes);
    }

    @Test
    public void testGetBytesUtf16WithValidString() {
        byte[] bytes = StringUtils.getBytesUtf16("A");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testGetBytesUtf16WithNull() {
        byte[] bytes = StringUtils.getBytesUtf16(null);
        assertNull(bytes);
    }

    @Test
    public void testGetBytesUtf16BeWithValidString() {
        byte[] bytes = StringUtils.getBytesUtf16Be("A");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testGetBytesUtf16BeWithNull() {
        byte[] bytes = StringUtils.getBytesUtf16Be(null);
        assertNull(bytes);
    }

    @Test
    public void testGetBytesUtf16LeWithValidString() {
        byte[] bytes = StringUtils.getBytesUtf16Le("A");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testGetBytesUtf16LeWithNull() {
        byte[] bytes = StringUtils.getBytesUtf16Le(null);
        assertNull(bytes);
    }

    @Test
    public void testGetBytesUtf8WithValidString() {
        byte[] bytes = StringUtils.getBytesUtf8("ABC");
        assertNotNull(bytes);
        assertArrayEquals(new byte[] { 65, 66, 67 }, bytes);
    }

    @Test
    public void testGetBytesUtf8WithNull() {
        byte[] bytes = StringUtils.getBytesUtf8(null);
        assertNull(bytes);
    }

    @Test
    public void testGetBytesUncheckedWithValidString() {
        byte[] bytes = StringUtils.getBytesUnchecked("ABC", "UTF-8");
        assertNotNull(bytes);
        assertArrayEquals(new byte[] { 65, 66, 67 }, bytes);
    }

    @Test
    public void testGetBytesUncheckedWithNullString() {
        byte[] bytes = StringUtils.getBytesUnchecked(null, "UTF-8");
        assertNull(bytes);
    }

    @Test
    public void testNewStringWithValidBytesAndCharset() {
        byte[] bytes = new byte[] { 65, 66, 67 };
        String str = StringUtils.newString(bytes, StandardCharsets.UTF_8);
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringWithNullBytes() {
        String str = StringUtils.newString(null, StandardCharsets.UTF_8);
        assertNull(str);
    }

    @Test
    public void testNewStringIso8859_1() {
        byte[] bytes = new byte[] { 65, 66, 67 };
        String str = StringUtils.newStringIso8859_1(bytes);
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringIso8859_1Null() {
        String str = StringUtils.newStringIso8859_1(null);
        assertNull(str);
    }

    @Test
    public void testNewStringUsAscii() {
        byte[] bytes = new byte[] { 65, 66, 67 };
        String str = StringUtils.newStringUsAscii(bytes);
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringUsAsciiNull() {
        String str = StringUtils.newStringUsAscii(null);
        assertNull(str);
    }

    @Test
    public void testNewStringUtf16() {
        byte[] bytes = StringUtils.getBytesUtf16("ABC");
        String str = StringUtils.newStringUtf16(bytes);
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringUtf16Null() {
        String str = StringUtils.newStringUtf16(null);
        assertNull(str);
    }

    @Test
    public void testNewStringUtf16Be() {
        byte[] bytes = StringUtils.getBytesUtf16Be("ABC");
        String str = StringUtils.newStringUtf16Be(bytes);
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringUtf16BeNull() {
        String str = StringUtils.newStringUtf16Be(null);
        assertNull(str);
    }

    @Test
    public void testNewStringUtf16Le() {
        byte[] bytes = StringUtils.getBytesUtf16Le("ABC");
        String str = StringUtils.newStringUtf16Le(bytes);
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringUtf16LeNull() {
        String str = StringUtils.newStringUtf16Le(null);
        assertNull(str);
    }

    @Test
    public void testNewStringUtf8() {
        byte[] bytes = new byte[] { 65, 66, 67 };
        String str = StringUtils.newStringUtf8(bytes);
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringUtf8Null() {
        String str = StringUtils.newStringUtf8(null);
        assertNull(str);
    }

    @Test
    public void testNewStringUnchecked() {
        byte[] bytes = new byte[] { 65, 66, 67 };
        String str = StringUtils.newString(bytes, "UTF-8");
        assertEquals("ABC", str);
    }

    @Test
    public void testNewStringUncheckedNullBytes() {
        String str = StringUtils.newString(null, "UTF-8");
        assertNull(str);
    }

    @Test
    public void testEqualsCharSequence() {
        CharSequence cs1 = "abc";
        CharSequence cs2 = "abc";
        CharSequence cs3 = "abd";
        CharSequence cs4 = null;

        assertTrue(StringUtils.equals(cs1, cs2));
        assertFalse(StringUtils.equals(cs1, cs3));
        assertFalse(StringUtils.equals(cs1, cs4));
        assertFalse(StringUtils.equals(cs4, cs1));
        assertTrue(StringUtils.equals(null, null));
        
        // Same reference
        assertTrue(StringUtils.equals(cs1, cs1));
        
        // Different length CharSequences
        StringBuilder sb1 = new StringBuilder("abcd");
        assertFalse(StringUtils.equals(cs1, sb1));
        assertFalse(StringUtils.equals(sb1, cs1));
        
        // Same characters, different implementations
        StringBuilder sb2 = new StringBuilder("abc");
        assertTrue(StringUtils.equals(cs1, sb2));
    }
}