package org.apache.commons.codec.binary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class CharSequenceUtilsTest {

    private CharSequence nonString(CharSequence cs) {
        return new StringBuilder(cs);
    }

    // regionMatches tests
    @Test
    public void testRegionMatchesExact() {
        assertTrue(CharSequenceUtils.regionMatches("hello", false, 0, "hello", 0, 5));
        assertTrue(CharSequenceUtils.regionMatches("hello", false, 1, "ell", 0, 3));
    }

    @Test
    public void testRegionMatchesIgnoreCase() {
        assertTrue(CharSequenceUtils.regionMatches("hello", true, 0, "HELLO", 0, 5));
        assertTrue(CharSequenceUtils.regionMatches("Hello", true, 1, "ELL", 0, 3));
    }

    @Test
    public void testRegionMatchesCaseSensitiveFalse() {
        assertFalse(CharSequenceUtils.regionMatches("hello", false, 0, "HELLO", 0, 5));
    }

    @Test
    public void testRegionMatchesIgnoreCaseFalseWhenDifferent() {
        assertFalse(CharSequenceUtils.regionMatches("hello", true, 0, "hxllo", 0, 5));
    }

    @Test
    public void testRegionMatchesLengthZero() {
        assertTrue(CharSequenceUtils.regionMatches("hello", false, 0, "world", 0, 0));
        assertTrue(CharSequenceUtils.regionMatches("hello", true, 5, "world", 3, 0));
    }

    @Test
    public void testRegionMatchesInvalidThisStartNegative() {
        try {
            assertFalse(CharSequenceUtils.regionMatches("hello", false, -1, "hello", 0, 3));
        } catch (IndexOutOfBoundsException e) {
            fail("regionMatches should return false for negative start, not throw");
        }
    }

    @Test
    public void testRegionMatchesInvalidStartNegative() {
        try {
            assertFalse(CharSequenceUtils.regionMatches("hello", false, 0, "hello", -1, 3));
        } catch (IndexOutOfBoundsException e) {
            fail("regionMatches should return false for negative substring start, not throw");
        }
    }

    @Test
    public void testRegionMatchesLengthTooLongForCs() {
        try {
            assertFalse(CharSequenceUtils.regionMatches("hello", false, 3, "hello", 0, 3));
        } catch (IndexOutOfBoundsException e) {
            fail("regionMatches should return false when length exceeds cs length, not throw");
        }
    }

    @Test
    public void testRegionMatchesLengthTooLongForSubstring() {
        try {
            assertFalse(CharSequenceUtils.regionMatches("hello", false, 0, "hello", 2, 4));
        } catch (IndexOutOfBoundsException e) {
            fail("regionMatches should return false when length exceeds substring length, not throw");
        }
    }

    @Test
    public void testRegionMatchesWithNonString() {
        CharSequence cs = nonString("hello");
        CharSequence sub = nonString("HELLO");
        assertTrue(CharSequenceUtils.regionMatches(cs, true, 0, sub, 0, 5));
        assertFalse(CharSequenceUtils.regionMatches(cs, false, 0, sub, 0, 5));
        try {
            assertFalse(CharSequenceUtils.regionMatches(cs, false, -1, sub, 0, 3));
        } catch (IndexOutOfBoundsException e) {
            fail("regionMatches with non-String should also return false for negative start");
        }
    }

    // indexOf(int) tests
    @Test
    public void testIndexOfIntWithString() {
        assertEquals(0, CharSequenceUtils.indexOf("abc", 'a', 0));
        assertEquals(2, CharSequenceUtils.indexOf("abc", 'c', 0));
        assertEquals(-1, CharSequenceUtils.indexOf("abc", 'd', 0));
        assertEquals(1, CharSequenceUtils.indexOf("abc", 'b', 1));
        assertEquals(-1, CharSequenceUtils.indexOf("abc", 'a', 1));
        assertEquals(0, CharSequenceUtils.indexOf("abc", 'a', -5));
        assertEquals(-1, CharSequenceUtils.indexOf("abc", 'a', 5));
    }

    @Test
    public void testIndexOfIntWithNonString() {
        CharSequence cs = nonString("abc");
        assertEquals(0, CharSequenceUtils.indexOf(cs, 'a', 0));
        assertEquals(2, CharSequenceUtils.indexOf(cs, 'c', 0));
        assertEquals(-1, CharSequenceUtils.indexOf(cs, 'd', 0));
        assertEquals(1, CharSequenceUtils.indexOf(cs, 'b', 1));
        assertEquals(-1, CharSequenceUtils.indexOf(cs, 'a', 1));
        assertEquals(0, CharSequenceUtils.indexOf(cs, 'a', -5));
        assertEquals(-1, CharSequenceUtils.indexOf(cs, 'a', 5));
    }

    // indexOf(CharSequence) tests
    @Test
    public void testIndexOfCharSequenceWithStrings() {
        assertEquals(0, CharSequenceUtils.indexOf("hello world", "hello", 0));
        assertEquals(6, CharSequenceUtils.indexOf("hello world", "world", 0));
        assertEquals(-1, CharSequenceUtils.indexOf("hello world", "xyz", 0));
        assertEquals(0, CharSequenceUtils.indexOf("hello world", "hello", -5));
        assertEquals(-1, CharSequenceUtils.indexOf("hello world", "hello", 1));
        assertEquals(-1, CharSequenceUtils.indexOf("hello world", "", 0));
        assertEquals(-1, CharSequenceUtils.indexOf("", "hello", 0));
    }

    @Test
    public void testIndexOfCharSequenceWithNonStringCs() {
        CharSequence cs = nonString("hello world");
        assertEquals(0, CharSequenceUtils.indexOf(cs, "hello", 0));
        assertEquals(6, CharSequenceUtils.indexOf(cs, "world", 0));
        assertEquals(-1, CharSequenceUtils.indexOf(cs, "xyz", 0));
        assertEquals(-1, CharSequenceUtils.indexOf(cs, "hello", 1));
        assertEquals(-1, CharSequenceUtils.indexOf(cs, "", 0));
    }

    @Test
    public void testIndexOfCharSequenceWithNonStringSubstring() {
        CharSequence sub = nonString("world");
        assertEquals(6, CharSequenceUtils.indexOf("hello world", sub, 0));
        assertEquals(-1, CharSequenceUtils.indexOf("hello world", nonString("xyz"), 0));
    }

    @Test
    public void testIndexOfCharSequenceWithBothNonString() {
        CharSequence cs = nonString("hello world");
        CharSequence sub = nonString("world");
        assertEquals(6, CharSequenceUtils.indexOf(cs, sub, 0));
        assertEquals(-1, CharSequenceUtils.indexOf(cs, nonString("xyz"), 0));
    }

    // lastIndexOf(int) tests
    @Test
    public void testLastIndexOfIntWithString() {
        assertEquals(2, CharSequenceUtils.lastIndexOf("abcabc", 'a', 5));
        assertEquals(5, CharSequenceUtils.lastIndexOf("abcabc", 'c', 5));
        assertEquals(-1, CharSequenceUtils.lastIndexOf("abcabc", 'd', 5));
        assertEquals(-1, CharSequenceUtils.lastIndexOf("abcabc", 'a', -1));
        assertEquals(0, CharSequenceUtils.lastIndexOf("abcabc", 'a', 0));
        assertEquals(2, CharSequenceUtils.lastIndexOf("abcabc", 'a', 2));
        assertEquals(3, CharSequenceUtils.lastIndexOf("abcabc", 'a', 10));
    }

    @Test
    public void testLastIndexOfIntWithNonString() {
        CharSequence cs = nonString("abcabc");
        assertEquals(3, CharSequenceUtils.lastIndexOf(cs, 'a', 5));
        assertEquals(5, CharSequenceUtils.lastIndexOf(cs, 'c', 5));
        assertEquals(-1, CharSequenceUtils.lastIndexOf(cs, 'd', 5));
        assertEquals(-1, CharSequenceUtils.lastIndexOf(cs, 'a', -1));
        assertEquals(0, CharSequenceUtils.lastIndexOf(cs, 'a', 0));
        assertEquals(3, CharSequenceUtils.lastIndexOf(cs, 'a', 10));
    }

    // lastIndexOf(CharSequence) tests
    @Test
    public void testLastIndexOfCharSequenceWithStrings() {
        assertEquals(6, CharSequenceUtils.lastIndexOf("hello world world", "world", 20));
        assertEquals(-1, CharSequenceUtils.lastIndexOf("hello world", "xyz", 20));
        assertEquals(-1, CharSequenceUtils.lastIndexOf("hello world", "world", -1));
        assertEquals(6, CharSequenceUtils.lastIndexOf("hello world world", "world", 6));
        assertEquals(-1, CharSequenceUtils.lastIndexOf("hello world", "", 0));
        assertEquals(-1, CharSequenceUtils.lastIndexOf("", "hello", 0));
    }

    @Test
    public void testLastIndexOfCharSequenceWithNonStringCs() {
        CharSequence cs = nonString("hello world world");
        assertEquals(12, CharSequenceUtils.lastIndexOf(cs, "world", 20));
        assertEquals(-1, CharSequenceUtils.lastIndexOf(cs, "xyz", 20));
        assertEquals(-1, CharSequenceUtils.lastIndexOf(cs, "world", -1));
    }

    @Test
    public void testLastIndexOfCharSequenceWithNonStringSubstring() {
        CharSequence sub = nonString("world");
        assertEquals(6, CharSequenceUtils.lastIndexOf("hello world", sub, 20));
        assertEquals(-1, CharSequenceUtils.lastIndexOf("hello world", nonString("xyz"), 20));
    }

    @Test
    public void testLastIndexOfCharSequenceWithBothNonString() {
        CharSequence cs = nonString("hello world world");
        CharSequence sub = nonString("world");
        assertEquals(12, CharSequenceUtils.lastIndexOf(cs, sub, 20));
        assertEquals(-1, CharSequenceUtils.lastIndexOf(cs, nonString("xyz"), 20));
    }
}