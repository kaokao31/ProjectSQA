package org.apache.commons.codec.binary;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class StringUtilsTest {

    private static CharSequence fixedCharSequence(final String value) {
        return new CharSequence() {
            @Override
            public int length() {
                return value.length();
            }

            @Override
            public char charAt(final int index) {
                return value.charAt(index);
            }

            @Override
            public CharSequence subSequence(final int start, final int end) {
                return value.substring(start, end);
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }

    @Test
    public void testEquals() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "a"));
        assertFalse(StringUtils.equals("a", null));

        assertTrue(StringUtils.equals("a", "a"));
        assertTrue(StringUtils.equals("", ""));
        assertTrue(StringUtils.equals("", new StringBuilder("")));

        assertTrue(StringUtils.equals("a", new StringBuilder("a")));
        assertTrue(StringUtils.equals(new StringBuilder("a"), "a"));
        assertTrue(StringUtils.equals(new StringBuilder("abc"), new StringBuffer("abc")));
        assertTrue(StringUtils.equals("abc", fixedCharSequence("abc")));
        assertTrue(StringUtils.equals(fixedCharSequence("abc"), new StringBuilder("abc")));

        assertFalse(StringUtils.equals("a", "b"));
        assertFalse(StringUtils.equals("a", "A"));
        assertFalse(StringUtils.equals("A", "a"));
        assertFalse(StringUtils.equals("a", "ab"));
        assertFalse(StringUtils.equals("ab", "a"));

        assertFalse(StringUtils.equals(new StringBuilder("a"), "ab"));
        assertFalse(StringUtils.equals("ab", new StringBuilder("a")));
        assertFalse(StringUtils.equals(new StringBuilder("a"), new StringBuilder("ab")));
        assertFalse(StringUtils.equals(new StringBuffer("ab"), new StringBuilder("abc")));
        assertFalse(StringUtils.equals("abc", fixedCharSequence("ab")));
        assertFalse(StringUtils.equals(fixedCharSequence("ab"), "abc"));
    }

    @Test
    public void testGetBytesUtf8() {
        assertNull(StringUtils.getBytesUtf8(null));
        final String input = "A\u00e9";
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_8), StringUtils.getBytesUtf8(input));
    }

    @Test
    public void testGetBytesIso8859_1() {
        assertNull(StringUtils.getBytesIso8859_1(null));
        final String input = "A\u00e9";
        assertArrayEquals(input.getBytes(StandardCharsets.ISO_8859_1), StringUtils.getBytesIso8859_1(input));
    }

    @Test
    public void testGetBytesUsAscii() {
        assertNull(StringUtils.getBytesUsAscii(null));
        final String input = "ABC";
        assertArrayEquals(input.getBytes(StandardCharsets.US_ASCII), StringUtils.getBytesUsAscii(input));
    }

    @Test
    public void testGetBytesUtf16() {
        assertNull(StringUtils.getBytesUtf16(null));
        final String input = "A\u00e9";
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_16), StringUtils.getBytesUtf16(input));
    }

    @Test
    public void testGetBytesUtf16Be() {
        assertNull(StringUtils.getBytesUtf16Be(null));
        final String input = "A\u00e9";
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_16BE), StringUtils.getBytesUtf16Be(input));
    }

    @Test
    public void testGetBytesUtf16Le() {
        assertNull(StringUtils.getBytesUtf16Le(null));
        final String input = "A\u00e9";
        assertArrayEquals(input.getBytes(StandardCharsets.UTF_16LE), StringUtils.getBytesUtf16Le(input));
    }

    @Test
    public void testGetBytesUnchecked() throws Exception {
        assertNull(StringUtils.getBytesUnchecked(null, "UTF-8"));
        final String input = "A\u00e9";
        assertArrayEquals(input.getBytes("UTF-8"), StringUtils.getBytesUnchecked(input, "UTF-8"));
        assertNull(StringUtils.getBytesUnchecked(input, "UNSUPPORTED-CHARSET"));
    }

    @Test
    public void testNewString() throws Exception {
        assertNull(StringUtils.newString(null, "UTF-8"));
        final String input = "A\u00e9";
        assertEquals(input, StringUtils.newString(input.getBytes("UTF-8"), "UTF-8"));
        assertNull(StringUtils.newString(new byte[] {65}, "UNSUPPORTED-CHARSET"));
    }

    @Test
    public void testNewStringUtf8() {
        assertNull(StringUtils.newStringUtf8(null));
        final String input = "A\u00e9";
        assertEquals(input, StringUtils.newStringUtf8(input.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testNewStringIso8859_1() {
        assertNull(StringUtils.newStringIso8859_1(null));
        final String input = "A\u00e9";
        assertEquals(input, StringUtils.newStringIso8859_1(input.getBytes(StandardCharsets.ISO_8859_1)));
    }

    @Test
    public void testNewStringUsAscii() {
        assertNull(StringUtils.newStringUsAscii(null));
        final String input = "ABC";
        assertEquals(input, StringUtils.newStringUsAscii(input.getBytes(StandardCharsets.US_ASCII)));
    }

    @Test
    public void testNewStringUtf16() {
        assertNull(StringUtils.newStringUtf16(null));
        final String input = "A\u00e9";
        assertEquals(input, StringUtils.newStringUtf16(input.getBytes(StandardCharsets.UTF_16)));
    }

    @Test
    public void testNewStringUtf16Be() {
        assertNull(StringUtils.newStringUtf16Be(null));
        final String input = "A\u00e9";
        assertEquals(input, StringUtils.newStringUtf16Be(input.getBytes(StandardCharsets.UTF_16BE)));
    }

    @Test
    public void testNewStringUtf16Le() {
        assertNull(StringUtils.newStringUtf16Le(null));
        final String input = "A\u00e9";
        assertEquals(input, StringUtils.newStringUtf16Le(input.getBytes(StandardCharsets.UTF_16LE)));
    }

    @Test
    public void testRoundTrips() {
        final String[] inputs = {"", "A", "A\u00e9", "\u20ac"};
        for (final String input : inputs) {
            assertEquals(input, StringUtils.newStringUtf8(StringUtils.getBytesUtf8(input)));
            assertEquals(input, StringUtils.newStringUtf16(StringUtils.getBytesUtf16(input)));
            assertEquals(input, StringUtils.newStringUtf16Be(StringUtils.getBytesUtf16Be(input)));
            assertEquals(input, StringUtils.newStringUtf16Le(StringUtils.getBytesUtf16Le(input)));
        }
    }
}