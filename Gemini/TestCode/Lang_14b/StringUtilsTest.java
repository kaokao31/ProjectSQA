package org.apache.commons.lang3;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

public class StringUtilsTest {

    // -----------------------------------------------------------------------
    // Tests targeting Lang-14: StringUtils.equals / equalsIgnoreCase with CharSequence
    // -----------------------------------------------------------------------

    @Test
    public void testEqualsCharSequence() {
        CharSequence fooCs = "foo";
        CharSequence barCs = "bar";
        CharSequence fooSb = new StringBuffer("foo");
        CharSequence barSb = new StringBuffer("bar");
        CharSequence fooBuilder = new StringBuilder("foo");
        CharSequence barBuilder = new StringBuilder("bar");
        CharSequence fooSameBuilder = new StringBuilder("foo");
        CharSequence fooCharBuffer = CharBuffer.wrap("foo");

        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(fooCs, null));
        assertFalse(StringUtils.equals(null, fooCs));
        assertTrue(StringUtils.equals(fooCs, fooCs));
        assertTrue(StringUtils.equals(fooCs, "foo"));
        assertTrue(StringUtils.equals(fooCs, fooSb));
        assertTrue(StringUtils.equals(fooSb, fooCs));
        assertTrue(StringUtils.equals(fooSb, fooSb));
        assertTrue(StringUtils.equals(fooSb, new StringBuffer("foo")));
        assertTrue(StringUtils.equals(fooBuilder, fooBuilder));
        assertTrue(StringUtils.equals(fooBuilder, fooSameBuilder));
        assertTrue(StringUtils.equals(fooBuilder, "foo"));
        assertTrue(StringUtils.equals("foo", fooBuilder));
        assertTrue(StringUtils.equals(fooSb, fooBuilder));
        assertTrue(StringUtils.equals(fooCharBuffer, fooCs));

        assertFalse(StringUtils.equals(fooCs, barCs));
        assertFalse(StringUtils.equals(fooCs, barSb));
        assertFalse(StringUtils.equals(fooSb, barCs));
        assertFalse(StringUtils.equals(fooSb, barSb));
        assertFalse(StringUtils.equals(fooBuilder, barBuilder));
        assertFalse(StringUtils.equals(fooCs, "fo"));
        assertFalse(StringUtils.equals("fo", fooCs));
        assertFalse(StringUtils.equals(fooCs, "foo "));
    }

    @Test
    public void testEqualsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase("foo", null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "foo"));
        assertTrue(StringUtils.equalsIgnoreCase("foo", "foo"));
        assertTrue(StringUtils.equalsIgnoreCase("foo", "FOO"));
        assertTrue(StringUtils.equalsIgnoreCase("FOO", "foo"));
        assertTrue(StringUtils.equalsIgnoreCase(new StringBuffer("foo"), new StringBuilder("FOO")));
        assertFalse(StringUtils.equalsIgnoreCase("foo", "bar"));
        assertFalse(StringUtils.equalsIgnoreCase("foo", "fo"));
        assertFalse(StringUtils.equalsIgnoreCase("fo", "foo"));
    }

    // -----------------------------------------------------------------------
    // Basic checks and emptiness tests
    // -----------------------------------------------------------------------

    @Test
    public void testIsEmptyAndIsNotEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("foo"));
        assertFalse(StringUtils.isEmpty("  foo  "));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("foo"));
        assertTrue(StringUtils.isNotEmpty("  foo  "));
    }

    @Test
    public void testIsBlankAndIsNotBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank(" \t \r \n "));
        assertFalse(StringUtils.isBlank("foo"));
        assertFalse(StringUtils.isBlank("  foo  "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" "));
        assertFalse(StringUtils.isNotBlank(" \t \r \n "));
        assertTrue(StringUtils.isNotBlank("foo"));
        assertTrue(StringUtils.isNotBlank("  foo  "));
    }

    @Test
    public void testIsAnyBlankAndIsNoneBlank() {
        assertTrue(StringUtils.isAnyBlank((CharSequence[]) null));
        assertTrue(StringUtils.isAnyBlank(null, "foo"));
        assertTrue(StringUtils.isAnyBlank("", "bar"));
        assertTrue(StringUtils.isAnyBlank("  ", "bar"));
        assertFalse(StringUtils.isAnyBlank("foo", "bar"));

        assertFalse(StringUtils.isNoneBlank((CharSequence[]) null));
        assertFalse(StringUtils.isNoneBlank(null, "foo"));
        assertFalse(StringUtils.isNoneBlank("", "bar"));
        assertFalse(StringUtils.isNoneBlank("  ", "bar"));
        assertTrue(StringUtils.isNoneBlank("foo", "bar"));
    }

    @Test
    public void testIsAnyEmptyAndIsNoneEmpty() {
        assertTrue(StringUtils.isAnyEmpty((CharSequence[]) null));
        assertTrue(StringUtils.isAnyEmpty(null, "foo"));
        assertTrue(StringUtils.isAnyEmpty("", "bar"));
        assertFalse(StringUtils.isAnyEmpty(" ", "bar"));
        assertFalse(StringUtils.isAnyEmpty("foo", "bar"));

        assertFalse(StringUtils.isNoneEmpty((CharSequence[]) null));
        assertFalse(StringUtils.isNoneEmpty(null, "foo"));
        assertFalse(StringUtils.isNoneEmpty("", "bar"));
        assertTrue(StringUtils.isNoneEmpty(" ", "bar"));
        assertTrue(StringUtils.isNoneEmpty("foo", "bar"));
    }

    // -----------------------------------------------------------------------
    // Trim / Strip tests
    // -----------------------------------------------------------------------

    @Test
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("     "));
        assertEquals("abc", StringUtils.trim("abc"));
        assertEquals("abc", StringUtils.trim("  abc  "));
        assertEquals("abc", StringUtils.trim("   \t  abc \r \n "));
    }

    @Test
    public void testTrimToNull() {
        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("     "));
        assertEquals("abc", StringUtils.trimToNull("abc"));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));
    }

    @Test
    public void testTrimToEmpty() {
        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("     "));
        assertEquals("abc", StringUtils.trimToEmpty("abc"));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }

    @Test
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("", StringUtils.strip("   "));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("  abc  ", null));
        assertEquals("bc", StringUtils.strip("abc", "a"));
        assertEquals("ab", StringUtils.strip("abc", "c"));
        assertEquals("b", StringUtils.strip("abc", "ac"));
        assertEquals("abc", StringUtils.strip("  abc  ", "xyz"));
    }

    @Test
    public void testStripStartAndStripEnd() {
        assertNull(StringUtils.stripStart(null, "a"));
        assertEquals("", StringUtils.stripStart("", "a"));
        assertEquals("abc", StringUtils.stripStart("  abc", null));
        assertEquals("bc", StringUtils.stripStart("abc", "a"));
        assertEquals("abc", StringUtils.stripStart("abc", "c"));

        assertNull(StringUtils.stripEnd(null, "a"));
        assertEquals("", StringUtils.stripEnd("", "a"));
        assertEquals("abc", StringUtils.stripEnd("abc  ", null));
        assertEquals("ab", StringUtils.stripEnd("abc", "c"));
        assertEquals("abc", StringUtils.stripEnd("abc", "a"));
    }

    @Test
    public void testStripAll() {
        assertNull(StringUtils.stripAll((String[]) null));
        assertArrayEquals(new String[0], StringUtils.stripAll(new String[0]));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.stripAll(new String[]{"  a ", " b", "c "}));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.stripAll(new String[]{"xxax", "yby", "cz"}, "xyz"));
    }

    // -----------------------------------------------------------------------
    // IndexOf and Substring tests
    // -----------------------------------------------------------------------

    @Test
    public void testIndexOf() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", 'b', 10));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b', 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOf("", ""));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "mn"));

        assertEquals(-1, StringUtils.indexOf(null, "a", 0));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", null, 0));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 0));
        assertEquals(5, StringUtils.indexOf("aabaabaa", "b", 3));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", "b", 9));
    }

    @Test
    public void testIndexOfIgnoreCase() {
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOfIgnoreCase("", ""));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB"));
        assertEquals(5, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 3));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", "B", 9));
    }

    @Test
    public void testLastIndexOf() {
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", (String) null));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
    }

    @Test
    public void testContains() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertTrue(StringUtils.contains("abc", 'z') == false);

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("", ""));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "a"));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "z"));
    }

    @Test
    public void testContainsIgnoreCase() {
        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", null));
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertTrue(StringUtils.containsIgnoreCase("ABC", "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
    }

    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("", StringUtils.substring("abc", 3));
        assertEquals("c", StringUtils.substring("abc", -1));
        assertEquals("abc", StringUtils.substring("abc", -4));

        assertNull(StringUtils.substring(null, 0, 2));
        assertEquals("", StringUtils.substring("", 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("b", StringUtils.substring("abc", 1, -1));
        assertEquals("", StringUtils.substring("abc", -2, -2));
    }

    // -----------------------------------------------------------------------
    // StartsWith / EndsWith tests
    // -----------------------------------------------------------------------

    @Test
    public void testStartsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abc", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));
        assertTrue(StringUtils.startsWith("abc", ""));
    }

    @Test
    public void testStartsWithIgnoreCase() {
        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abc", null));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "ABC"));
        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abcdef", "xyz"));
    }

    @Test
    public void testEndsWith() {
        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "abc"));
        assertFalse(StringUtils.endsWith("abc", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));
        assertTrue(StringUtils.endsWith("abc", ""));
    }

    @Test
    public void testEndsWithIgnoreCase() {
        assertTrue(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.endsWithIgnoreCase("abc", null));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "DEF"));
        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("abcdef", "xyz"));
    }

    // -----------------------------------------------------------------------
    // Split and Join tests
    // -----------------------------------------------------------------------

    @Test
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[]{"abc", "def", "ghi"}, StringUtils.split("abc def\nghi"));
        assertArrayEquals(new String[]{"abc", "def", "ghi"}, StringUtils.split("abc.def.ghi", '.'));
        assertArrayEquals(new String[]{"abc", "def", "ghi"}, StringUtils.split("abc,def;ghi", ",;"));
    }

    @Test
    public void testJoin() {
        assertNull(StringUtils.join((Object[]) null, ","));
        assertEquals("", StringUtils.join(new Object[0], ","));
        assertEquals("a,b,c", StringUtils.join(new Object[]{"a", "b", "c"}, ","));
        assertEquals("a,b,c", StringUtils.join(Arrays.asList("a", "b", "c"), ","));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
    }

    // -----------------------------------------------------------------------
    // Replace and Repeat tests
    // -----------------------------------------------------------------------

    @Test
    public void testReplace() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("aba", StringUtils.replace("aba", null, "b"));
        assertEquals("aba", StringUtils.replace("aba", "a", null));
        assertEquals("b", StringUtils.replace("aba", "a", ""));
        assertEquals("zba", StringUtils.replace("aba", "a", "z", 1));
        assertEquals("zbz", StringUtils.replace("aba", "a", "z", -1));
    }

    @Test
    public void testRepeat() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("", StringUtils.repeat("abc", -2));
        assertEquals("abc", StringUtils.repeat("abc", 1));
        assertEquals("abcabcabc", StringUtils.repeat("abc", 3));
        assertEquals("a,a,a", StringUtils.repeat("a", ",", 3));
        assertEquals("a", StringUtils.repeat("a", ",", 1));
    }

    // -----------------------------------------------------------------------
    // Padding and Centering tests
    // -----------------------------------------------------------------------

    @Test
    public void testLeftPadRightPadCenter() {
        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("   abc", StringUtils.leftPad("abc", 6));
        assertEquals("000abc", StringUtils.leftPad("abc", 6, '0'));
        assertEquals("xyzabc", StringUtils.leftPad("abc", 6, "xyz"));
        assertEquals("abc", StringUtils.leftPad("abc", 2));

        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("abc   ", StringUtils.rightPad("abc", 6));
        assertEquals("abc000", StringUtils.rightPad("abc", 6, '0'));
        assertEquals("abcxyz", StringUtils.rightPad("abc", 6, "xyz"));
        assertEquals("abc", StringUtils.rightPad("abc", 2));

        assertNull(StringUtils.center(null, 5));
        assertEquals(" abc  ", StringUtils.center("abc", 6));
        assertEquals("  abc ", StringUtils.center("abc", 6, ' '));
        assertEquals("xxabcxx", StringUtils.center("abc", 7, "x"));
        assertEquals("abc", StringUtils.center("abc", 2));
    }

    // -----------------------------------------------------------------------
    // Case Conversion and Abbreviation tests
    // -----------------------------------------------------------------------

    @Test
    public void testCaseConversions() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("Cat", StringUtils.capitalize("Cat"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cat", StringUtils.uncapitalize("cat"));

        assertNull(StringUtils.swapCase(null));
        assertEquals("", StringUtils.swapCase(""));
        assertEquals("hELLO wORLD", StringUtils.swapCase("Hello World"));
    }

    @Test
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abcd...", StringUtils.abbreviate("abcdefghij", 7));
        assertEquals("...fgh...", StringUtils.abbreviate("abcdefghijklmno", 6, 9));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviateException() {
        StringUtils.abbreviate("abcdef", 3);
    }

    // -----------------------------------------------------------------------
    // Predicate / Content Inspection tests
    // -----------------------------------------------------------------------

    @Test
    public void testIsAlphaIsNumericIsWhitespace() {
        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("ab1c"));

        assertFalse(StringUtils.isNumeric(null));
        assertFalse(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));
        assertFalse(StringUtils.isNumeric("12a3"));

        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace("   \t \r \n "));
        assertFalse(StringUtils.isWhitespace("  a  "));
    }

    @Test
    public void testDefaultStringAndDefaultIfBlank() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("abc", StringUtils.defaultString("abc", "default"));

        assertEquals("default", StringUtils.defaultIfBlank(null, "default"));
        assertEquals("default", StringUtils.defaultIfBlank("", "default"));
        assertEquals("default", StringUtils.defaultIfBlank("   ", "default"));
        assertEquals("abc", StringUtils.defaultIfBlank("abc", "default"));

        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("   ", StringUtils.defaultIfEmpty("   ", "default"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "default"));
    }

    @Test
    public void testConstructor() {
        assertNotNull(new StringUtils());
    }
}