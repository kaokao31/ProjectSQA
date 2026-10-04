package org.apache.commons.lang3;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for org.apache.commons.lang3.StringUtils.
 * Designed to achieve maximum line and branch coverage and to detect the known
 * Defects4J bug #14 (StringUtils.equals(null, null) incorrectly returns false).
 */
public class StringUtilsTest {

    // ===== equals =====

    @Test
    public void testEquals() {
        // Defects4J bug #14: null vs null must return true
        assertTrue("null vs null should be true", StringUtils.equals(null, null));
        assertFalse("null vs empty should be false", StringUtils.equals(null, ""));
        assertFalse("empty vs null should be false", StringUtils.equals("", null));
        assertTrue("empty vs empty should be true", StringUtils.equals("", ""));
        assertTrue("same string should be true", StringUtils.equals("abc", "abc"));
        assertFalse("different case should be false", StringUtils.equals("abc", "ABC"));
        assertFalse("different strings should be false", StringUtils.equals("abc", "def"));
        assertFalse("different lengths should be false", StringUtils.equals("abc", "abcd"));
        // Additional boundary coverage
        assertFalse("null vs single char should be false", StringUtils.equals(null, "x"));
        assertFalse("single char vs null should be false", StringUtils.equals("x", null));
    }

    @Test
    public void testEqualsIgnoreCase() {
        assertTrue("null vs null should be true", StringUtils.equalsIgnoreCase(null, null));
        assertFalse("null vs empty should be false", StringUtils.equalsIgnoreCase(null, ""));
        assertFalse("empty vs null should be false", StringUtils.equalsIgnoreCase("", null));
        assertTrue("empty vs empty should be true", StringUtils.equalsIgnoreCase("", ""));
        assertTrue("same case should be true", StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue("different case should be true", StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertFalse("different strings should be false", StringUtils.equalsIgnoreCase("abc", "def"));
        assertFalse("different lengths should be false", StringUtils.equalsIgnoreCase("abc", "abcd"));
        assertTrue("mixed case should be true", StringUtils.equalsIgnoreCase("AbC", "aBc"));
    }

    // ===== isEmpty =====

    @Test
    public void testIsEmpty() {
        assertTrue("null should be empty", StringUtils.isEmpty(null));
        assertTrue("empty string should be empty", StringUtils.isEmpty(""));
        assertFalse("whitespace should not be empty", StringUtils.isEmpty(" "));
        assertFalse("non-empty should not be empty", StringUtils.isEmpty("a"));
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse("null should be empty", StringUtils.isNotEmpty(null));
        assertFalse("empty string should be empty", StringUtils.isNotEmpty(""));
        assertTrue("whitespace should be considered not empty", StringUtils.isNotEmpty(" "));
        assertTrue("non-empty should be considered not empty", StringUtils.isNotEmpty("a"));
    }

    // ===== isBlank =====

    @Test
    public void testIsBlank() {
        assertTrue("null should be blank", StringUtils.isBlank(null));
        assertTrue("empty string should be blank", StringUtils.isBlank(""));
        assertTrue("whitespace only should be blank", StringUtils.isBlank("   "));
        assertTrue("whitespace with tabs should be blank", StringUtils.isBlank("\t\n "));
        assertFalse("non-whitespace should not be blank", StringUtils.isBlank(" a "));
        assertFalse("single char should not be blank", StringUtils.isBlank("a"));
    }

    @Test
    public void testIsNotBlank() {
        assertFalse("null should be blank", StringUtils.isNotBlank(null));
        assertFalse("empty string should be blank", StringUtils.isNotBlank(""));
        assertFalse("whitespace only should be blank", StringUtils.isNotBlank("   "));
        assertTrue("non-whitespace should not be blank", StringUtils.isNotBlank(" a "));
        assertTrue("single char should not be blank", StringUtils.isNotBlank("a"));
    }

    // ===== trim =====

    @Test
    public void testTrim() {
        assertNull("trim(null) should return null", StringUtils.trim(null));
        assertEquals("trim(empty) should return empty", "", StringUtils.trim(""));
        assertEquals("trim(whitespace) should return empty", "", StringUtils.trim("   "));
        assertEquals("trim(leading and trailing) should work", "abc", StringUtils.trim("  abc  "));
        assertEquals("trim(no whitespace) should remain same", "abc", StringUtils.trim("abc"));
    }

    // ===== strip =====

    @Test
    public void testStrip() {
        assertNull("strip(null) should return null", StringUtils.strip(null));
        assertEquals("strip(empty) should return empty", "", StringUtils.strip(""));
        assertEquals("strip(whitespace) should return empty", "", StringUtils.strip("   "));
        assertEquals("strip(leading and trailing) should work", "abc", StringUtils.strip("  abc  "));
        assertEquals("strip(no whitespace) should remain same", "abc", StringUtils.strip("abc"));
    }

    // ===== capitalize =====

    @Test
    public void testCapitalize() {
        assertNull("capitalize(null) should return null", StringUtils.capitalize(null));
        assertEquals("capitalize(empty) should return empty", "", StringUtils.capitalize(""));
        assertEquals("capitalize(single char) should uppercase", "A", StringUtils.capitalize("a"));
        assertEquals("capitalize(already capitalized) should stay", "Abc", StringUtils.capitalize("Abc"));
        assertEquals("capitalize(lowercase) should capitalize first", "Abc", StringUtils.capitalize("abc"));
        assertEquals("capitalize(full uppercase) should keep first", "ABC", StringUtils.capitalize("ABC"));
    }

    // ===== uncapitalize =====

    @Test
    public void testUncapitalize() {
        assertNull("uncapitalize(null) should return null", StringUtils.uncapitalize(null));
        assertEquals("uncapitalize(empty) should return empty", "", StringUtils.uncapitalize(""));
        assertEquals("uncapitalize(single char) should lowercase", "a", StringUtils.uncapitalize("A"));
        assertEquals("uncapitalize(already lowercase) should stay", "abc", StringUtils.uncapitalize("abc"));
        assertEquals("uncapitalize(capitalized) should lowercase first", "aBC", StringUtils.uncapitalize("ABC"));
    }

    // ===== deleteWhitespace =====

    @Test
    public void testDeleteWhitespace() {
        assertNull("deleteWhitespace(null) should return null", StringUtils.deleteWhitespace(null));
        assertEquals("deleteWhitespace(empty) should return empty", "", StringUtils.deleteWhitespace(""));
        assertEquals("deleteWhitespace(no whitespace) should remain", "abc", StringUtils.deleteWhitespace("abc"));
        assertEquals("deleteWhitespace(whitespace removed)", "abc", StringUtils.deleteWhitespace(" a b  c "));
        assertEquals("deleteWhitespace(tabs and newlines)", "abc", StringUtils.deleteWhitespace("\ta b\nc\r"));
    }

    // ===== removeStart =====

    @Test
    public void testRemoveStart() {
        assertNull("removeStart(null, ...) should return null", StringUtils.removeStart(null, "a"));
        assertEquals("removeStart(empty, ...) should return empty", "", StringUtils.removeStart("", "a"));
        assertEquals("removeStart(abc, ab) should return c", "c", StringUtils.removeStart("abc", "ab"));
        assertEquals("removeStart(abc, bc) should remain abc", "abc", StringUtils.removeStart("abc", "bc"));
        assertEquals("removeStart(abc, empty) should remain abc", "abc", StringUtils.removeStart("abc", ""));
        assertEquals("removeStart(abc, null) should remain abc", "abc", StringUtils.removeStart("abc", null));
    }

    // ===== removeEnd =====

    @Test
    public void testRemoveEnd() {
        assertNull("removeEnd(null, ...) should return null", StringUtils.removeEnd(null, "a"));
        assertEquals("removeEnd(empty, ...) should return empty", "", StringUtils.removeEnd("", "a"));
        assertEquals("removeEnd(abc, bc) should return a", "a", StringUtils.removeEnd("abc", "bc"));
        assertEquals("removeEnd(abc, ab) should remain abc", "abc", StringUtils.removeEnd("abc", "ab"));
        assertEquals("removeEnd(abc, empty) should remain abc", "abc", StringUtils.removeEnd("abc", ""));
        assertEquals("removeEnd(abc, null) should remain abc", "abc", StringUtils.removeEnd("abc", null));
    }

    // ===== replace =====

    @Test
    public void testReplace() {
        assertNull("replace(null, ..., ...) should return null", StringUtils.replace(null, "a", "b"));
        assertEquals("replace(empty, ..., ...) should return empty", "", StringUtils.replace("", "a", "b"));
        assertEquals("replace with no occurrence", "abc", StringUtils.replace("abc", "x", "y"));
        assertEquals("replace single occurrence", "dbc", StringUtils.replace("abc", "a", "d"));
        assertEquals("replace multiple occurrences", "dbd", StringUtils.replace("aba", "a", "d"));
        assertEquals("replace empty old string should return original", "abc", StringUtils.replace("abc", "", "x"));
        assertEquals("replace null old string should return original", "abc", StringUtils.replace("abc", null, "x"));
    }

    // ===== split =====

    @Test
    public void testSplit() {
        assertNull("split(null) should return null", StringUtils.split(null));
        assertArrayEquals("split(empty) should return empty array", new String[0], StringUtils.split(""));
        assertArrayEquals("split(normal)", new String[]{"a", "b", "c"}, StringUtils.split("a b c"));
        assertArrayEquals("split(with multiple spaces)", new String[]{"a", "b", "c"}, StringUtils.split("a   b   c"));
        assertArrayEquals("split(leading/trailing spaces)", new String[]{"a", "b"}, StringUtils.split(" a b "));
    }

    @Test
    public void testSplitWithChar() {
        assertNull("split(null, char) should return null", StringUtils.split(null, ','));
        assertArrayEquals("split(empty, char) should return empty array", new String[0], StringUtils.split("", ','));
        assertArrayEquals("split with comma", new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ','));
        assertArrayEquals("split with not present", new String[]{"abc"}, StringUtils.split("abc", ','));
    }

    // ===== join =====

    @Test
    public void testJoin() {
        assertNull("join(null array) should return null", StringUtils.join((Object[]) null));
        assertEquals("join(empty array) should return empty", "", StringUtils.join(new Object[0]));
        assertEquals("join(single element)", "a", StringUtils.join(new Object[]{"a"}));
        assertEquals("join(two elements)", "ab", StringUtils.join(new Object[]{"a", "b"}));
        assertEquals("join with separator", "a,b", StringUtils.join(new Object[]{"a", "b"}, ","));
        assertEquals("join with null elements", "a,null", StringUtils.join(new Object[]{"a", null}, ","));
    }

    // ===== leftPad =====

    @Test
    public void testLeftPad() {
        assertNull("leftPad(null, ...) should return null", StringUtils.leftPad(null, 5));
        assertEquals("leftPad(empty, ...) should pad spaces", "     ", StringUtils.leftPad("", 5));
        assertEquals("leftPad(abc, 5) should pad two spaces", "  abc", StringUtils.leftPad("abc", 5));
        assertEquals("leftPad(abc, 3) should be abc", "abc", StringUtils.leftPad("abc", 3));
        assertEquals("leftPad(abc, 2) should be abc", "abc", StringUtils.leftPad("abc", 2));
        assertEquals("leftPad(abc, 5, 'x') should be xxabc", "xxabc", StringUtils.leftPad("abc", 5, 'x'));
        assertEquals("leftPad(abc, 3, 'x') should be abc", "abc", StringUtils.leftPad("abc", 3, 'x'));
    }

    // ===== repeat =====

    @Test
    public void testRepeat() {
        assertNull("repeat(null, ...) should return null", StringUtils.repeat(null, 3));
        assertEquals("repeat(empty, ...) should return empty", "", StringUtils.repeat("", 3));
        assertEquals("repeat(abc, 0) should return empty", "", StringUtils.repeat("abc", 0));
        assertEquals("repeat(abc, 1) should return abc", "abc", StringUtils.repeat("abc", 1));
        assertEquals("repeat(abc, 3) should return abcabcabc", "abcabcabc", StringUtils.repeat("abc", 3));
        // Edge: negative count (should treat as 0? - commons-lang treats as 0)
        assertEquals("repeat(abc, -1) should return empty", "", StringUtils.repeat("abc", -1));
    }

    // ===== reverse =====

    @Test
    public void testReverse() {
        assertNull("reverse(null) should return null", StringUtils.reverse(null));
        assertEquals("reverse(empty) should return empty", "", StringUtils.reverse(""));
        assertEquals("reverse(single char)", "a", StringUtils.reverse("a"));
        assertEquals("reverse(palindrome)", "aba", StringUtils.reverse("aba"));
        assertEquals("reverse(normal)", "cba", StringUtils.reverse("abc"));
    }

    // ===== substring =====

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringNegativeStart() {
        StringUtils.substring("abc", -1);
    }

    @Test
    public void testSubstring() {
        assertNull("substring(null, ...) should return null", StringUtils.substring(null, 0));
        assertEquals("substring(empty, ...) should return empty", "", StringUtils.substring("", 0));
        assertEquals("substring(abc, 0) should return abc", "abc", StringUtils.substring("abc", 0));
        assertEquals("substring(abc, 1) should return bc", "bc", StringUtils.substring("abc", 1));
        assertEquals("substring(abc, 3) should return empty", "", StringUtils.substring("abc", 3));
        assertEquals("substring(abc, 0, 2) should return ab", "ab", StringUtils.substring("abc", 0, 2));
        assertEquals("substring(abc, 1, 3) should return bc", "bc", StringUtils.substring("abc", 1, 3));
        assertEquals("substring(abc, 1, 1) should return empty", "", StringUtils.substring("abc", 1, 1));
    }

    // ===== rotate =====

    @Test
    public void testRotate() {
        assertNull("rotate(null, ...) should return null", StringUtils.rotate(null, 2));
        assertEquals("rotate(empty, ...) should return empty", "", StringUtils.rotate("", 2));
        assertEquals("rotate(abc, 0) should return abc", "abc", StringUtils.rotate("abc", 0));
        assertEquals("rotate(abc, 1) should return bca", "bca", StringUtils.rotate("abc", 1));
        assertEquals("rotate(abc, 2) should return cab", "cab", StringUtils.rotate("abc", 2));
        assertEquals("rotate(abc, 3) should return abc", "abc", StringUtils.rotate("abc", 3));
        assertEquals("rotate(abc, -1) should return cab", "cab", StringUtils.rotate("abc", -1));
        assertEquals("rotate(abc, 5) should rotate 2 steps", "cab", StringUtils.rotate("abc", 5));
    }

    // ===== defaultString =====

    @Test
    public void testDefaultString() {
        assertEquals("defaultString(null) should return empty", "", StringUtils.defaultString(null));
        assertEquals("defaultString(abc) should return abc", "abc", StringUtils.defaultString("abc"));
        assertEquals("defaultString(null, def) should return def", "def", StringUtils.defaultString(null, "def"));
        assertEquals("defaultString(abc, def) should return abc", "abc", StringUtils.defaultString("abc", "def"));
        assertEquals("defaultString(empty, def) should return empty", "", StringUtils.defaultString("", "def"));
    }

    // ===== abbreviate =====

    @Test
    public void testAbbreviate() {
        assertNull("abbreviate(null, ...) should return null", StringUtils.abbreviate(null, 5));
        assertEquals("abbreviate(empty, ...) should return empty", "", StringUtils.abbreviate("", 5));
        assertEquals("abbreviate(short string)", "abc", StringUtils.abbreviate("abc", 5));
        assertEquals("abbreviate(long string, 10)", "abcdefg...", StringUtils.abbreviate("abcdefghijklm", 10));
        try {
            StringUtils.abbreviate("abc", -1);
            fail("Expected IllegalArgumentException for negative width");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            StringUtils.abbreviate("abc", 3);
            // Should work
        } catch (Exception e) {
            fail("abbreviate with width 3 should not throw exception");
        }
    }

    // ===== difference =====

    @Test
    public void testDifference() {
        assertNull("difference(null, ...) should return null", StringUtils.difference(null, "a"));
        assertNull("difference(..., null) should return null", StringUtils.difference("a", null));
        assertEquals("difference(empty, empty)", "", StringUtils.difference("", ""));
        assertEquals("difference(abc, abc)", "", StringUtils.difference("abc", "abc"));
        assertEquals("difference(abc, abx)", "x", StringUtils.difference("abc", "abx"));
        assertEquals("difference(abx, abc)", "c", StringUtils.difference("abx", "abc"));
        assertEquals("difference(abc, ab)", "", StringUtils.difference("abc", "ab"));
    }
}