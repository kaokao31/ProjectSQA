package org.apache.commons.lang3;

import static org.junit.Assert.*;

import java.util.Locale;
import org.junit.Test;

/**
 * Unit tests for {@link org.apache.commons.lang3.StringUtils}.
 */
public class StringUtilsTest {

    private static final String CharU20000 = "\uD840\uDC00";
    private static final String CharU20001 = "\uD840\uDC01";
    private static final String CharUSuppCharChar = "a" + CharU20000 + "b";
    private static final String CharUSuppCharChar2 = "c" + CharU20001 + "d";

    @Test
    public void testIndexOfAny_StringCharArrayWithSupplementaryChars() {
        assertEquals(-1, StringUtils.indexOfAny(CharU20000, new char[] { 'a', 'b' }));
        assertEquals(0, StringUtils.indexOfAny(CharU20000, CharU20000.toCharArray()));
        assertEquals(0, StringUtils.indexOfAny(CharUSuppCharChar, CharU20000.toCharArray()));
        assertEquals(1, StringUtils.indexOfAny(CharUSuppCharChar, new char[] { CharU20000.charAt(0), CharU20000.charAt(1) }));
        assertEquals(2, StringUtils.indexOfAny("ab" + CharU20000, CharU20000.toCharArray()));
    }

    @Test
    public void testIndexOfAny_StringStringWithSupplementaryChars() {
        assertEquals(-1, StringUtils.indexOfAny(CharU20000, "ab"));
        assertEquals(0, StringUtils.indexOfAny(CharU20000, CharU20000));
        assertEquals(0, StringUtils.indexOfAny(CharUSuppCharChar, "a"));
        assertEquals(1, StringUtils.indexOfAny(CharUSuppCharChar, CharU20000));
        assertEquals(2, StringUtils.indexOfAny("ab" + CharU20000, CharU20000));
    }

    @Test
    public void testIndexOfAnyBut_StringCharArrayWithSupplementaryChars() {
        assertEquals(-1, StringUtils.indexOfAnyBut(CharU20000, CharU20000.toCharArray()));
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + "a", CharU20000.toCharArray()));
        assertEquals(0, StringUtils.indexOfAnyBut("a" + CharU20000, CharU20000.toCharArray()));
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20000.toCharArray()));
    }

    @Test
    public void testIndexOfAnyBut_StringStringWithSupplementaryChars() {
        assertEquals(-1, StringUtils.indexOfAnyBut(CharU20000, CharU20000));
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + "a", CharU20000));
        assertEquals(0, StringUtils.indexOfAnyBut("a" + CharU20000, CharU20000));
        assertEquals(2, StringUtils.indexOfAnyBut(CharU20000 + CharU20001, CharU20000));
    }

    @Test
    public void testContainsAny_StringCharArrayWithBadSupplementaryChars() {
        // High surrogate without low surrogate
        assertFalse(StringUtils.containsAny(CharU20000, new char[] { CharU20000.charAt(0) }));
        assertFalse(StringUtils.containsAny(CharU20000, new char[] { CharU20000.charAt(1) }));
        assertFalse(StringUtils.containsAny(CharU20000, new char[] { CharU20000.charAt(1), CharU20000.charAt(0) }));
    }

    @Test
    public void testContainsAny_StringWithBadSupplementaryChars() {
        assertFalse(StringUtils.containsAny(CharU20000, "" + CharU20000.charAt(0)));
        assertFalse(StringUtils.containsAny(CharU20000, "" + CharU20000.charAt(1)));
        assertFalse(StringUtils.containsAny(CharU20000, "" + CharU20000.charAt(1) + CharU20000.charAt(0)));
    }

    @Test
    public void testContainsNone_CharArrayWithSupplementaryChars() {
        assertTrue(StringUtils.containsNone(CharU20000, new char[] { 'a', 'b' }));
        assertFalse(StringUtils.containsNone(CharU20000, CharU20000.toCharArray()));
        assertFalse(StringUtils.containsNone(CharUSuppCharChar, CharU20000.toCharArray()));
        assertTrue(StringUtils.containsNone(CharUSuppCharChar, CharU20001.toCharArray()));
    }

    @Test
    public void testContainsNone_StringWithSupplementaryChars() {
        assertTrue(StringUtils.containsNone(CharU20000, "ab"));
        assertFalse(StringUtils.containsNone(CharU20000, CharU20000));
        assertFalse(StringUtils.containsNone(CharUSuppCharChar, CharU20000));
        assertTrue(StringUtils.containsNone(CharUSuppCharChar, CharU20001));
    }

    @Test
    public void testContainsNone_CharArrayWithBadSupplementaryChars() {
        assertTrue(StringUtils.containsNone(CharU20000, new char[] { CharU20000.charAt(0) }));
        assertTrue(StringUtils.containsNone(CharU20000, new char[] { CharU20000.charAt(1) }));
        assertTrue(StringUtils.containsNone(CharU20000, new char[] { CharU20000.charAt(1), CharU20000.charAt(0) }));
    }

    @Test
    public void testContainsNone_StringWithBadSupplementaryChars() {
        assertTrue(StringUtils.containsNone(CharU20000, "" + CharU20000.charAt(0)));
        assertTrue(StringUtils.containsNone(CharU20000, "" + CharU20000.charAt(1)));
        assertTrue(StringUtils.containsNone(CharU20000, "" + CharU20000.charAt(1) + CharU20000.charAt(0)));
    }

    @Test
    public void testIsEmptyAndIsNotEmpty() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("foo"));

        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertTrue(StringUtils.isNotEmpty(" "));
        assertTrue(StringUtils.isNotEmpty("foo"));
    }

    @Test
    public void testIsBlankAndIsNotBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" \t\r\n "));
        assertFalse(StringUtils.isBlank(" foo "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank(" foo "));
    }

    @Test
    public void testTrimAndStrip() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("   \t\r\n   "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));

        assertNull(StringUtils.strip(null));
        assertEquals("", StringUtils.strip(""));
        assertEquals("abc", StringUtils.strip("  abc  "));
        assertEquals("abc", StringUtils.strip("xxabcxx", "x"));
        assertEquals("abc", StringUtils.stripStart("xxabcxx", "x"));
        assertEquals("xxabc", StringUtils.stripEnd("xxabcxx", "x"));
    }

    @Test
    public void testEqualsAndEqualsIgnoreCase() {
        assertTrue(StringUtils.equals(null, null));
        assertFalse(StringUtils.equals(null, "abc"));
        assertFalse(StringUtils.equals("abc", null));
        assertTrue(StringUtils.equals("abc", "abc"));
        assertFalse(StringUtils.equals("abc", "ABC"));

        assertTrue(StringUtils.equalsIgnoreCase(null, null));
        assertFalse(StringUtils.equalsIgnoreCase(null, "abc"));
        assertFalse(StringUtils.equalsIgnoreCase("abc", null));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
    }

    @Test
    public void testIndexOfAndLastIndexOf() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));
        assertEquals(5, StringUtils.indexOf("aabaabaa", 'b', 3));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", 'b', 4));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b", 1));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", (String) null));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(2, StringUtils.lastIndexOf("aabaabaa", "b", 4));
    }

    @Test
    public void testContains() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", null));
        assertTrue(StringUtils.contains("abc", "a"));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "z"));

        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertTrue(StringUtils.containsIgnoreCase("ABC", "a"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "Z"));
    }

    @Test
    public void testIndexOfAny_CharArray() {
        assertEquals(-1, StringUtils.indexOfAny(null, new char[] { 'a' }));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (char[]) null));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new char[0]));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", new char[] { 'z', 'a' }));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", new char[] { 'b', 'a' }));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", new char[] { 'm', 'n' }));
    }

    @Test
    public void testIndexOfAny_String() {
        assertEquals(-1, StringUtils.indexOfAny(null, "a"));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", (String) null));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", ""));
        assertEquals(0, StringUtils.indexOfAny("zzabyycdxx", "za"));
        assertEquals(3, StringUtils.indexOfAny("zzabyycdxx", "ba"));
        assertEquals(-1, StringUtils.indexOfAny("zzabyycdxx", "mn"));
    }

    @Test
    public void testContainsOnly() {
        assertFalse(StringUtils.containsOnly(null, new char[] { 'a' }));
        assertFalse(StringUtils.containsOnly("ab", (char[]) null));
        assertTrue(StringUtils.containsOnly("", new char[] { 'a' }));
        assertFalse(StringUtils.containsOnly("ab", new char[0]));
        assertTrue(StringUtils.containsOnly("abab", new char[] { 'a', 'b' }));
        assertFalse(StringUtils.containsOnly("abac", new char[] { 'a', 'b' }));

        assertFalse(StringUtils.containsOnly(null, "a"));
        assertFalse(StringUtils.containsOnly("ab", (String) null));
        assertTrue(StringUtils.containsOnly("", "a"));
        assertFalse(StringUtils.containsOnly("ab", ""));
        assertTrue(StringUtils.containsOnly("abab", "ab"));
        assertFalse(StringUtils.containsOnly("abac", "ab"));
    }

    @Test
    public void testContainsNone() {
        assertTrue(StringUtils.containsNone(null, new char[] { 'a' }));
        assertTrue(StringUtils.containsNone("ab", (char[]) null));
        assertTrue(StringUtils.containsNone("", new char[] { 'a' }));
        assertTrue(StringUtils.containsNone("ab", new char[0]));
        assertFalse(StringUtils.containsNone("abab", new char[] { 'a', 'b' }));
        assertTrue(StringUtils.containsNone("abab", new char[] { 'x', 'y' }));

        assertTrue(StringUtils.containsNone(null, "a"));
        assertTrue(StringUtils.containsNone("ab", (String) null));
        assertTrue(StringUtils.containsNone("", "a"));
        assertTrue(StringUtils.containsNone("ab", ""));
        assertFalse(StringUtils.containsNone("abab", "ab"));
        assertTrue(StringUtils.containsNone("abab", "xy"));
    }

    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("c", StringUtils.substring("abc", -1));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("abc", StringUtils.substring("abc", -4));

        assertNull(StringUtils.substring(null, 0, 2));
        assertEquals("", StringUtils.substring("", 0, 2));
        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("b", StringUtils.substring("abc", -2, -1));
    }

    @Test
    public void testLeftRightMid() {
        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 4));

        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 4));

        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("bc", StringUtils.mid("abc", 1, 2));
        assertEquals("c", StringUtils.mid("abc", 2, 2));
        assertEquals("", StringUtils.mid("abc", 4, 2));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
    }

    @Test
    public void testSplitAndJoin() {
        assertNull(StringUtils.split(null));
        assertArrayEquals(new String[0], StringUtils.split(""));
        assertArrayEquals(new String[] { "abc", "def" }, StringUtils.split("abc def"));
        assertArrayEquals(new String[] { "abc", "def" }, StringUtils.split("abc  def"));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtils.split("a:b:c", ':'));
        assertArrayEquals(new String[] { "a", "b", "c" }, StringUtils.split("a:b:c", ":"));

        assertNull(StringUtils.join((Object[]) null, ","));
        assertEquals("", StringUtils.join(new Object[0], ","));
        assertEquals("a,b,c", StringUtils.join(new Object[] { "a", "b", "c" }, ","));
        assertEquals("abc", StringUtils.join(new Object[] { "a", "b", "c" }, null));
    }

    @Test
    public void testReplaceAndRemove() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("aba", StringUtils.replace("aba", null, "b"));
        assertEquals("aba", StringUtils.replace("aba", "a", null));
        assertEquals("bba", StringUtils.replaceOnce("aba", "a", "b"));
        assertEquals("bbb", StringUtils.replace("aba", "a", "b"));
        assertEquals("zbz", StringUtils.replaceChars("aba", "a", "z"));

        assertNull(StringUtils.remove(null, "a"));
        assertEquals("", StringUtils.remove("", "a"));
        assertEquals("queued", StringUtils.remove("queued", ""));
        assertEquals("qu", StringUtils.remove("queued", "ed"));
        assertEquals("qud", StringUtils.remove("queued", 'e'));
    }

    @Test
    public void testUpperLowerCaseAndCapitalize() {
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
    }

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("abc", StringUtils.defaultString("abc", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfEmpty(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfEmpty("", "NULL"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "NULL"));

        assertEquals("NULL", StringUtils.defaultIfBlank(null, "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank("", "NULL"));
        assertEquals("NULL", StringUtils.defaultIfBlank("   ", "NULL"));
        assertEquals("abc", StringUtils.defaultIfBlank("abc", "NULL"));
    }

    @Test
    public void testRepeatAndPadding() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("abc", 0));
        assertEquals("abcabc", StringUtils.repeat("abc", 2));
        assertEquals("a, a, a", StringUtils.repeat("a", ", ", 3));

        assertEquals("  bat", StringUtils.leftPad("bat", 5));
        assertEquals("bat  ", StringUtils.rightPad("bat", 5));
        assertEquals("xxbat", StringUtils.leftPad("bat", 5, 'x'));
        assertEquals("batxx", StringUtils.rightPad("bat", 5, 'x'));
        assertEquals("  bat  ", StringUtils.center("bat", 7));
        assertEquals("xxbatxx", StringUtils.center("bat", 7, 'x'));
    }

    @Test
    public void testCountMatches() {
        assertEquals(0, StringUtils.countMatches(null, "a"));
        assertEquals(0, StringUtils.countMatches("abc", null));
        assertEquals(0, StringUtils.countMatches("", "a"));
        assertEquals(0, StringUtils.countMatches("abc", ""));
        assertEquals(2, StringUtils.countMatches("abba", "a"));
        assertEquals(1, StringUtils.countMatches("abba", "bb"));
        assertEquals(0, StringUtils.countMatches("abba", "c"));
    }

    @Test
    public void testIsNumericAndAlpha() {
        assertFalse(StringUtils.isAlpha(null));
        assertFalse(StringUtils.isAlpha(""));
        assertTrue(StringUtils.isAlpha("abc"));
        assertFalse(StringUtils.isAlpha("abc1"));

        assertFalse(StringUtils.isNumeric(null));
        assertFalse(StringUtils.isNumeric(""));
        assertTrue(StringUtils.isNumeric("123"));
        assertFalse(StringUtils.isNumeric("12.3"));

        assertFalse(StringUtils.isAlphanumeric(null));
        assertFalse(StringUtils.isAlphanumeric(""));
        assertTrue(StringUtils.isAlphanumeric("abc123"));
        assertFalse(StringUtils.isAlphanumeric("abc 123"));
    }
}