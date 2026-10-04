package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for StringUtils, targeting maximum coverage and fault detection,
 * especially for supplementary character handling in containsAny methods.
 */
public class StringUtilsTest {

    // ========== containsAny (char[]) ==========

    @Test
    public void testContainsAnyCharArrayWithSupplementaryChars() {
        // Supplementary character U+1F600 (😀) as surrogate pair
        String str = "a😀b";
        char[] searchChars = "😀".toCharArray(); // surrogate pair
        assertFalse("Should not find supplementary char when searchChars contain it",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyCharArrayWithSupplementaryCharsNotFound() {
        String str = "a😀b";
        char[] searchChars = "c".toCharArray();
        assertFalse("Should not find non-existing char",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyCharArrayWithSupplementaryCharsInSearchOnly() {
        String str = "abc";
        char[] searchChars = "😀".toCharArray();
        assertFalse("Should not find supplementary char not in string",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyCharArrayNull() {
        assertFalse("Null string should return false",
                StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse("Null searchChars should return false",
                StringUtils.containsAny("abc", (char[]) null));
    }

    @Test
    public void testContainsAnyCharArrayEmpty() {
        assertFalse("Empty string should return false",
                StringUtils.containsAny("", new char[]{'a'}));
        assertFalse("Empty searchChars should return false",
                StringUtils.containsAny("abc", new char[0]));
    }

    @Test
    public void testContainsAnyCharArrayNormal() {
        assertTrue("Should find matching char",
                StringUtils.containsAny("hello", new char[]{'h', 'e'}));
        assertFalse("Should not find non-matching char",
                StringUtils.containsAny("hello", new char[]{'x', 'y'}));
    }

    // ========== containsAny (String) ==========

    @Test
    public void testContainsAnyStringWithSupplementaryChars() {
        String str = "a😀b";
        String searchChars = "😀";
        assertFalse("Should not find supplementary char when searchChars contain it",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyStringWithSupplementaryCharsNotFound() {
        String str = "a😀b";
        String searchChars = "c";
        assertFalse("Should not find non-existing char",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyStringWithSupplementaryCharsInSearchOnly() {
        String str = "abc";
        String searchChars = "😀";
        assertFalse("Should not find supplementary char not in string",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyStringNull() {
        assertFalse("Null string should return false",
                StringUtils.containsAny(null, "a"));
        assertFalse("Null searchChars should return false",
                StringUtils.containsAny("abc", (String) null));
    }

    @Test
    public void testContainsAnyStringEmpty() {
        assertFalse("Empty string should return false",
                StringUtils.containsAny("", "a"));
        assertFalse("Empty searchChars should return false",
                StringUtils.containsAny("abc", ""));
    }

    @Test
    public void testContainsAnyStringNormal() {
        assertTrue("Should find matching char",
                StringUtils.containsAny("hello", "he"));
        assertFalse("Should not find non-matching char",
                StringUtils.containsAny("hello", "xy"));
    }

    // ========== contains (char) ==========

    @Test
    public void testContainsChar() {
        assertTrue("Should find char", StringUtils.contains("hello", 'h'));
        assertFalse("Should not find char", StringUtils.contains("hello", 'z'));
        assertFalse("Null string should return false", StringUtils.contains(null, 'a'));
        assertFalse("Empty string should return false", StringUtils.contains("", 'a'));
    }

    // ========== indexOf (char) ==========

    @Test
    public void testIndexOfChar() {
        assertEquals(0, StringUtils.indexOf("hello", 'h'));
        assertEquals(-1, StringUtils.indexOf("hello", 'z'));
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
    }

    // ========== indexOf (String) ==========

    @Test
    public void testIndexOfString() {
        assertEquals(0, StringUtils.indexOf("hello", "he"));
        assertEquals(-1, StringUtils.indexOf("hello", "xy"));
        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("", "a"));
        assertEquals(0, StringUtils.indexOf("", ""));
    }

    // ========== indexOf (String, int) ==========

    @Test
    public void testIndexOfStringWithStartPos() {
        assertEquals(2, StringUtils.indexOf("hello", 'l', 2));
        assertEquals(-1, StringUtils.indexOf("hello", 'l', 4));
        assertEquals(-1, StringUtils.indexOf(null, 'a', 0));
    }

    // ========== lastIndexOf ==========

    @Test
    public void testLastIndexOfChar() {
        assertEquals(3, StringUtils.lastIndexOf("hello", 'l'));
        assertEquals(-1, StringUtils.lastIndexOf("hello", 'z'));
        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
    }

    // ========== containsIgnoreCase ==========

    @Test
    public void testContainsIgnoreCase() {
        assertTrue(StringUtils.containsIgnoreCase("Hello", "HELLO"));
        assertFalse(StringUtils.containsIgnoreCase("Hello", "world"));
        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
        assertFalse(StringUtils.containsIgnoreCase("", "a"));
    }

    // ========== isEmpty / isBlank ==========

    @Test
    public void testIsEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("abc"));
    }

    @Test
    public void testIsBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" "));
        assertFalse(StringUtils.isBlank("abc"));
    }

    // ========== trim / strip ==========

    @Test
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("abc", StringUtils.trim("  abc  "));
    }

    @Test
    public void testStrip() {
        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("  abc  ", null));
    }

    // ========== equals ==========

    @Test
    public void testEquals() {
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals(null, null));
    }

    // ========== equalsIgnoreCase ==========

    @Test
    public void testEqualsIgnoreCase() {
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", "ab"));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
    }

    // ========== startsWith / endsWith ==========

    @Test
    public void testStartsWith() {
        assertTrue(StringUtils.startsWith("hello", "he"));
        assertFalse(StringUtils.startsWith("hello", "lo"));
        assertFalse(StringUtils.startsWith(null, "he"));
        assertFalse(StringUtils.startsWith("hello", null));
    }

    @Test
    public void testEndsWith() {
        assertTrue(StringUtils.endsWith("hello", "lo"));
        assertFalse(StringUtils.endsWith("hello", "he"));
        assertFalse(StringUtils.endsWith(null, "lo"));
        assertFalse(StringUtils.endsWith("hello", null));
    }

    // ========== substring ==========

    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("ell", StringUtils.substring("hello", 1, 4));
        assertEquals("hello", StringUtils.substring("hello", 0));
        assertEquals("", StringUtils.substring("hello", 5));
    }

    // ========== join ==========

    @Test
    public void testJoin() {
        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[]{}));
        assertEquals("a,b,c", StringUtils.join(new Object[]{"a", "b", "c"}, ","));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
    }

    // ========== split ==========

    @Test
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[]{}, StringUtils.split(""));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a b c"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a  b c"));
    }

    // ========== replace ==========

    @Test
    public void testReplace() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("bcd", StringUtils.replace("abc", "a", "b"));
        assertEquals("abc", StringUtils.replace("abc", "x", "y"));
    }

    // ========== repeat ==========

    @Test
    public void testRepeat() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("", 2));
        assertEquals("ababab", StringUtils.repeat("ab", 3));
        assertEquals("", StringUtils.repeat("ab", 0));
    }

    // ========== defaultString ==========

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("default", StringUtils.defaultString(null, "default"));
    }

    // ========== abbreviate ==========

    @Test
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 5));
        assertEquals("", StringUtils.abbreviate("", 5));
        assertEquals("hello", StringUtils.abbreviate("hello world", 5));
        assertEquals("hello...", StringUtils.abbreviate("hello world", 8));
    }

    // ========== capitalize / uncapitalize ==========

    @Test
    public void testCapitalize() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Hello", StringUtils.capitalize("hello"));
        assertEquals("Hello", StringUtils.capitalize("Hello"));
    }

    @Test
    public void testUncapitalize() {
        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("hello", StringUtils.uncapitalize("Hello"));
        assertEquals("hello", StringUtils.uncapitalize("hello"));
    }

    // ========== isNumeric ==========

    @Test
    public void testIsNumeric() {
        assertFalse(StringUtils.isNumeric(null));
        assertFalse(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));
        assertFalse(StringUtils.isNumeric("abc"));
    }

    // ========== isAlpha ==========

    @Test
    public void testIsAlpha() {
        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("abc123"));
    }

    // ========== isAlphanumeric ==========

    @Test
    public void testIsAlphanumeric() {
        assertFalse(StringUtils.isAlphanumeric(null));
        assertFalse(StringUtils.isAlphanumeric(""));
        assertTrue(StringUtils.isAlphanumeric("abc123"));
        assertFalse(StringUtils.isAlphanumeric("abc 123"));
    }

    // ========== isWhitespace ==========

    @Test
    public void testIsWhitespace() {
        assertFalse(StringUtils.isWhitespace(null));
        assertTrue(StringUtils.isWhitespace(""));
        assertTrue(StringUtils.isWhitespace(" "));
        assertFalse(StringUtils.isWhitespace("abc"));
    }

    // ========== lowerCase / upperCase ==========

    @Test
    public void testLowerCase() {
        assertNull(StringUtils.lowerCase(null));
        assertEquals("", StringUtils.lowerCase(""));
        assertEquals("hello", StringUtils.lowerCase("HELLO"));
    }

    @Test
    public void testUpperCase() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("", StringUtils.upperCase(""));
        assertEquals("HELLO", StringUtils.upperCase("hello"));
    }

    // ========== removeStart / removeEnd ==========

    @Test
    public void testRemoveStart() {
        assertNull(StringUtils.removeStart(null, "a"));
        assertEquals("", StringUtils.removeStart("", "a"));
        assertEquals("bc", StringUtils.removeStart("abc", "a"));
        assertEquals("abc", StringUtils.removeStart("abc", "x"));
    }

    @Test
    public void testRemoveEnd() {
        assertNull(StringUtils.removeEnd(null, "c"));
        assertEquals("", StringUtils.removeEnd("", "c"));
        assertEquals("ab", StringUtils.removeEnd("abc", "c"));
        assertEquals("abc", StringUtils.removeEnd("abc", "x"));
    }

    // ========== countMatches ==========

    @Test
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("", "a"));
        assertEquals(2, StringUtils.countMatches("ababa", "a"));
        assertEquals(0, StringUtils.countMatches("ababa", "x"));
    }

    // ========== isAllUpperCase / isAllLowerCase ==========

    @Test
    public void testIsAllUpperCase() {
        assertFalse(StringUtils.isAllUpperCase(null));
        assertFalse(StringUtils.isAllUpperCase(""));
        assertTrue(StringUtils.isAllUpperCase("ABC"));
        assertFalse(StringUtils.isAllUpperCase("AbC"));
    }

    @Test
    public void testIsAllLowerCase() {
        assertFalse(StringUtils.isAllLowerCase(null));
        assertFalse(StringUtils.isAllLowerCase(""));
        assertTrue(StringUtils.isAllLowerCase("abc"));
        assertFalse(StringUtils.isAllLowerCase("aBc"));
    }

    // ========== reverse ==========

    @Test
    public void testReverse() {
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("cba", StringUtils.reverse("abc"));
    }

    // ========== difference ==========

    @Test
    public void testDifference() {
        assertNull(StringUtils.difference(null, "a"));
        assertNull(StringUtils.difference("a", null));
        assertEquals("", StringUtils.difference("", ""));
        assertEquals("cde", StringUtils.difference("abc", "abcde"));
        assertEquals("", StringUtils.difference("abc", "abc"));
    }

    // ========== getLevenshteinDistance ==========

    @Test
    public void testGetLevenshteinDistance() {
        assertEquals(0, StringUtils.getLevenshteinDistance(null, null));
        assertEquals(3, StringUtils.getLevenshteinDistance("", "abc"));
        assertEquals(3, StringUtils.getLevenshteinDistance("abc", ""));
        assertEquals(1, StringUtils.getLevenshteinDistance("abc", "abd"));
        assertEquals(2, StringUtils.getLevenshteinDistance("abc", "abx"));
    }

    // ========== containsAny with supplementary chars (additional edge cases) ==========

    @Test
    public void testContainsAnyCharArrayWithSupplementaryCharsMixed() {
        String str = "a😀b😁c";
        char[] searchChars = "😁".toCharArray();
        assertFalse("Should not find supplementary char when searchChars contain it",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyStringWithSupplementaryCharsMixed() {
        String str = "a😀b😁c";
        String searchChars = "😁";
        assertFalse("Should not find supplementary char when searchChars contain it",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyCharArrayWithSupplementaryCharsMultiple() {
        String str = "a😀b";
        char[] searchChars = "😀😁".toCharArray();
        assertFalse("Should not find any supplementary char",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyStringWithSupplementaryCharsMultiple() {
        String str = "a😀b";
        String searchChars = "😀😁";
        assertFalse("Should not find any supplementary char",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyCharArrayWithSupplementaryCharsAndNormal() {
        String str = "a😀b";
        char[] searchChars = "ab".toCharArray();
        assertTrue("Should find normal char 'a'",
                StringUtils.containsAny(str, searchChars));
    }

    @Test
    public void testContainsAnyStringWithSupplementaryCharsAndNormal() {
        String str = "a😀b";
        String searchChars = "ab";
        assertTrue("Should find normal char 'a'",
                StringUtils.containsAny(str, searchChars));
    }

    // ========== Additional coverage for containsAny with null/empty in various combinations ==========

    @Test
    public void testContainsAnyCharArrayBothNull() {
        assertFalse(StringUtils.containsAny(null, (char[]) null));
    }

    @Test
    public void testContainsAnyStringBothNull() {
        assertFalse(StringUtils.containsAny(null, (String) null));
    }

    @Test
    public void testContainsAnyCharArrayNullString() {
        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
    }

    @Test
    public void testContainsAnyStringNullString() {
        assertFalse(StringUtils.containsAny(null, "a"));
    }

    @Test
    public void testContainsAnyCharArrayNullSearch() {
        assertFalse(StringUtils.containsAny("abc", (char[]) null));
    }

    @Test
    public void testContainsAnyStringNullSearch() {
        assertFalse(StringUtils.containsAny("abc", (String) null));
    }

    @Test
    public void testContainsAnyCharArrayEmptyString() {
        assertFalse(StringUtils.containsAny("", new char[]{'a'}));
    }

    @Test
    public void testContainsAnyStringEmptyString() {
        assertFalse(StringUtils.containsAny("", "a"));
    }

    @Test
    public void testContainsAnyCharArrayEmptySearch() {
        assertFalse(StringUtils.containsAny("abc", new char[0]));
    }

    @Test
    public void testContainsAnyStringEmptySearch() {
        assertFalse(StringUtils.containsAny("abc", ""));
    }
}