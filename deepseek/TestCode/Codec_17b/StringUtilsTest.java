package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class StringUtilsTest {

    private static final String LATIN_SMALL_E_ACUTE = "\u00E9";

    @Test
    public void testGetBytesIso8859_1() {
        assertArrayEquals(new byte[] { (byte) 0xE9 }, StringUtils.getBytesIso8859_1(LATIN_SMALL_E_ACUTE));
        assertArrayEquals(new byte[] { 0x41, (byte) 0xE9 },
                StringUtils.getBytesIso8859_1("A" + LATIN_SMALL_E_ACUTE));
    }

    @Test
    public void testGetBytesIso8859_1Null() {
        assertNull(StringUtils.getBytesIso8859_1(null));
    }

    @Test
    public void testGetBytesUsAscii() {
        assertArrayEquals(new byte[] { 0x3F }, StringUtils.getBytesUsAscii(LATIN_SMALL_E_ACUTE));
        assertArrayEquals(new byte[] { 0x41, 0x3F },
                StringUtils.getBytesUsAscii("A" + LATIN_SMALL_E_ACUTE));
    }

    @Test
    public void testGetBytesUsAsciiNull() {
        assertNull(StringUtils.getBytesUsAscii(null));
    }

    @Test
    public void testGetBytesUtf8() {
        assertArrayEquals(new byte[] { 0x41, (byte) 0xC3, (byte) 0xA9 },
                StringUtils.getBytesUtf8("A" + LATIN_SMALL_E_ACUTE));
    }

    @Test
    public void testGetBytesUtf8Null() {
        assertNull(StringUtils.getBytesUtf8(null));
    }

    @Test
    public void testGetBytesUtf16() {
        assertArrayEquals(new byte[] { (byte) 0xFE, (byte) 0xFF, 0x00, 0x41 },
                StringUtils.getBytesUtf16("A"));
    }

    @Test
    public void testGetBytesUtf16Null() {
        assertNull(StringUtils.getBytesUtf16(null));
    }

    @Test
    public void testGetBytesUtf16Be() {
        assertArrayEquals(new byte[] { 0x00, 0x41 }, StringUtils.getBytesUtf16Be("A"));
    }

    @Test
    public void testGetBytesUtf16BeNull() {
        assertNull(StringUtils.getBytesUtf16Be(null));
    }

    @Test
    public void testGetBytesUtf16Le() {
        assertArrayEquals(new byte[] { 0x41, 0x00 }, StringUtils.getBytesUtf16Le("A"));
    }

    @Test
    public void testGetBytesUtf16LeNull() {
        assertNull(StringUtils.getBytesUtf16Le(null));
    }

    @Test
    public void testGetBytesUnchecked() {
        assertArrayEquals(new byte[] { 0x41 }, StringUtils.getBytesUnchecked("A", "US-ASCII"));
        assertArrayEquals(new byte[] { (byte) 0xE9 },
                StringUtils.getBytesUnchecked(LATIN_SMALL_E_ACUTE, "ISO-8859-1"));
    }

    @Test
    public void testGetBytesUncheckedNull() {
        assertNull(StringUtils.getBytesUnchecked(null, "ISO-8859-1"));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBytesUncheckedUnknownCharset() {
        StringUtils.getBytesUnchecked("A", "UNKNOWN-CHARSET");
    }

    @Test
    public void testNewString() {
        assertEquals("A", StringUtils.newString(new byte[] { 0x41 }, "US-ASCII"));
        assertEquals("", StringUtils.newString(new byte[0], "UTF-8"));
    }

    @Test
    public void testNewStringNullBytes() {
        assertNull(StringUtils.newString(null, "UTF-8"));
    }

    @Test(expected = IllegalStateException.class)
    public void testNewStringUnknownCharset() {
        StringUtils.newString(new byte[] { 0x41 }, "UNKNOWN-CHARSET");
    }

    @Test
    public void testNewStringIso8859_1() {
        assertEquals(LATIN_SMALL_E_ACUTE, StringUtils.newStringIso8859_1(new byte[] { (byte) 0xE9 }));
        assertEquals("A" + LATIN_SMALL_E_ACUTE,
                StringUtils.newStringIso8859_1(new byte[] { 0x41, (byte) 0xE9 }));
        assertEquals("", StringUtils.newStringIso8859_1(new byte[0]));
    }

    @Test
    public void testNewStringIso8859_1Null() {
        assertNull(StringUtils.newStringIso8859_1(null));
    }

    @Test
    public void testNewStringUsAscii() {
        assertEquals("A\uFFFD", StringUtils.newStringUsAscii(new byte[] { 0x41, (byte) 0xE9 }));
        assertEquals("", StringUtils.newStringUsAscii(new byte[0]));
    }

    @Test
    public void testNewStringUsAsciiNull() {
        assertNull(StringUtils.newStringUsAscii(null));
    }

    @Test
    public void testNewStringUtf8() {
        assertEquals(LATIN_SMALL_E_ACUTE,
                StringUtils.newStringUtf8(new byte[] { (byte) 0xC3, (byte) 0xA9 }));
        assertEquals("", StringUtils.newStringUtf8(new byte[0]));
    }

    @Test
    public void testNewStringUtf8Malformed() {
        assertEquals("\uFFFD", StringUtils.newStringUtf8(new byte[] { (byte) 0xE9 }));
    }

    @Test
    public void testNewStringUtf8Null() {
        assertNull(StringUtils.newStringUtf8(null));
    }

    @Test
    public void testNewStringUtf16() {
        assertEquals("A", StringUtils.newStringUtf16(
                new byte[] { (byte) 0xFE, (byte) 0xFF, 0x00, 0x41 }));
        assertEquals("", StringUtils.newStringUtf16(new byte[0]));
    }

    @Test
    public void testNewStringUtf16Null() {
        assertNull(StringUtils.newStringUtf16(null));
    }

    @Test
    public void testNewStringUtf16Be() {
        assertEquals("A", StringUtils.newStringUtf16Be(new byte[] { 0x00, 0x41 }));
        assertEquals("", StringUtils.newStringUtf16Be(new byte[0]));
    }

    @Test
    public void testNewStringUtf16BeNull() {
        assertNull(StringUtils.newStringUtf16Be(null));
    }

    @Test
    public void testNewStringUtf16Le() {
        assertEquals("A", StringUtils.newStringUtf16Le(new byte[] { 0x41, 0x00 }));
        assertEquals("", StringUtils.newStringUtf16Le(new byte[0]));
    }

    @Test
    public void testNewStringUtf16LeNull() {
        assertNull(StringUtils.newStringUtf16Le(null));
    }

    @Test
    public void testRoundTripIso8859_1() {
        String original = "A" + LATIN_SMALL_E_ACUTE;
        assertEquals(original, StringUtils.newStringIso8859_1(StringUtils.getBytesIso8859_1(original)));
    }

    @Test
    public void testRoundTripUtf8() {
        String original = "A" + LATIN_SMALL_E_ACUTE + "\u4E2D\uD83D\uDE00";
        assertEquals(original, StringUtils.newStringUtf8(StringUtils.getBytesUtf8(original)));
    }

    @Test
    public void testRoundTripUtf16() {
        String original = "A" + LATIN_SMALL_E_ACUTE + "\u4E2D";
        assertEquals(original, StringUtils.newStringUtf16(StringUtils.getBytesUtf16(original)));
    }

    @Test
    public void testRoundTripUtf16Be() {
        String original = "A" + LATIN_SMALL_E_ACUTE + "\u4E2D";
        assertEquals(original, StringUtils.newStringUtf16Be(StringUtils.getBytesUtf16Be(original)));
    }

    @Test
    public void testRoundTripUtf16Le() {
        String original = "A" + LATIN_SMALL_E_ACUTE + "\u4E2D";
        assertEquals(original, StringUtils.newStringUtf16Le(StringUtils.getBytesUtf16Le(original)));
    }

    @Test
    public void testEmptyStringConversions() {
        assertArrayEquals(new byte[0], StringUtils.getBytesIso8859_1(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUsAscii(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf8(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Be(""));
        assertArrayEquals(new byte[0], StringUtils.getBytesUtf16Le(""));
    }
}