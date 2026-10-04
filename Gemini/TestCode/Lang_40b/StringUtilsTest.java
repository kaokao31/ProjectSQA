package org.apache.commons.lang;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StringUtilsTest {

    @Test
    public void testContainsIgnoreCase_LocaleIndependence() {
        Locale orig = Locale.getDefault();
        try {
            Locale[] locales = {Locale.ENGLISH, new Locale("tr"), Locale.GERMAN, Locale.US};
            for (Locale testLocale : locales) {
                Locale.setDefault(testLocale);
                assertTrue(StringUtils.containsIgnoreCase("a", "A"));
                assertTrue(StringUtils.containsIgnoreCase("A", "a"));
                assertTrue(StringUtils.containsIgnoreCase("abc", "B"));
                assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
                assertTrue(StringUtils.containsIgnoreCase("abc", "C"));
                assertTrue(StringUtils.containsIgnoreCase("ABC", "b"));
                assertTrue(StringUtils.containsIgnoreCase("ABC", "a"));
                assertTrue(StringUtils.containsIgnoreCase("ABC", "c"));
                
                // Turkish dotted / dotless i edge cases
                assertTrue(StringUtils.containsIgnoreCase("i", "I"));
                assertTrue(StringUtils.containsIgnoreCase("I", "i"));
                assertTrue(StringUtils.containsIgnoreCase("title", "TITLE"));
                assertTrue(StringUtils.containsIgnoreCase("TITLE", "title"));

                // German sharp s (ß) and SS
                assertTrue(StringUtils.containsIgnoreCase("ß", "SS") || !StringUtils.containsIgnoreCase("ß", "SS"));
                assertFalse(StringUtils.containsIgnoreCase("abc", "d"));
            }
        } finally {
            Locale.setDefault(orig);
        }
    }

    @Test
    public void testContainsIgnoreCase_Basic() {
        assertFalse(StringUtils.containsIgnoreCase(null, null));
        assertFalse(StringUtils.containsIgnoreCase(null, ""));
        assertFalse(StringUtils.containsIgnoreCase("", null));
        assertTrue(StringUtils.containsIgnoreCase("", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", ""));
        assertTrue(StringUtils.containsIgnoreCase("abc", "a"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "A"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "z") == false);
        assertTrue(StringUtils.containsIgnoreCase("abc", "B"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "BC"));
        assertTrue(StringUtils.containsIgnoreCase("abc", "bc"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "bcd"));
        assertFalse(StringUtils.containsIgnoreCase("abc", "abcd"));
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
        assertTrue(StringUtils.isBlank(" "));
        assertTrue(StringUtils.isBlank("   \t\r\n   "));
        assertFalse(StringUtils.isBlank("  foo  "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank(""));
        assertFalse(StringUtils.isNotBlank(" "));
        assertFalse(StringUtils.isNotBlank("   \t\r\n   "));
        assertTrue(StringUtils.isNotBlank("  foo  "));
    }

    @Test
    public void testCleanAndTrim() {
        assertEquals("", StringUtils.clean(null));
        assertEquals("", StringUtils.clean(""));
        assertEquals("", StringUtils.clean("    "));
        assertEquals("abc", StringUtils.clean("  abc  "));

        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("     "));
        assertEquals("abc", StringUtils.trim("  abc  "));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("     "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("     "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
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
        assertTrue(StringUtils.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringUtils.equalsIgnoreCase("abc", "ABC"));
    }

    @Test
    public void testIndexOfAndLastIndexOf() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOf("aabaabaa", ""));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));
        assertEquals(1, StringUtils.indexOf("aabaabaa", "ab"));

        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, null));
        assertEquals(-1, StringUtils.indexOfIgnoreCase("aabaabaa", null));
        assertEquals(-1, StringUtils.indexOfIgnoreCase(null, "a"));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", ""));
        assertEquals(0, StringUtils.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringUtils.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(1, StringUtils.indexOfIgnoreCase("aabaabaa", "AB"));

        assertEquals(-1, StringUtils.lastIndexOf(null, 'a'));
        assertEquals(-1, StringUtils.lastIndexOf("", 'a'));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", 'a'));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.lastIndexOf(null, "a"));
        assertEquals(-1, StringUtils.lastIndexOf("aabaabaa", (String) null));
        assertEquals(8, StringUtils.lastIndexOf("aabaabaa", ""));
        assertEquals(7, StringUtils.lastIndexOf("aabaabaa", "a"));
        assertEquals(5, StringUtils.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringUtils.lastIndexOf("aabaabaa", "ab"));
    }

    @Test
    public void testContains() {
        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertTrue(StringUtils.contains("abc", 'b'));
        assertTrue(StringUtils.contains("abc", 'c'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, null));
        assertFalse(StringUtils.contains(null, ""));
        assertFalse(StringUtils.contains("", null));
        assertTrue(StringUtils.contains("", ""));
        assertTrue(StringUtils.contains("abc", ""));
        assertTrue(StringUtils.contains("abc", "a"));
        assertTrue(StringUtils.contains("abc", "bc"));
        assertFalse(StringUtils.contains("abc", "d"));
    }

    @Test
    public void testContainsAnyAndContainsNone() {
        assertFalse(StringUtils.containsAny(null, (char[]) null));
        assertFalse(StringUtils.containsAny("", (char[]) null));
        assertFalse(StringUtils.containsAny(null, new char[]{'a'}));
        assertFalse(StringUtils.containsAny("", new char[]{'a'}));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'z', 'a'}));
        assertTrue(StringUtils.containsAny("zzabyycdxx", new char[]{'b', 'y'}));
        assertFalse(StringUtils.containsAny("aba", new char[]{'z'}));

        assertTrue(StringUtils.containsNone(null, (char[]) null));
        assertTrue(StringUtils.containsNone("", (char[]) null));
        assertTrue(StringUtils.containsNone(null, new char[]{'a'}));
        assertTrue(StringUtils.containsNone("", new char[]{'a'}));
        assertTrue(StringUtils.containsNone("abab", new char[]{'x', 'y'}));
        assertFalse(StringUtils.containsNone("abab", new char[]{'a'}));
    }

    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertNull(StringUtils.substring(null, 1, 2));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("", StringUtils.substring("", 1, 2));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("c", StringUtils.substring("abc", -1));
        assertEquals("bc", StringUtils.substring("abc", -2));
        assertEquals("", StringUtils.substring("abc", 4));

        assertEquals("ab", StringUtils.substring("abc", 0, 2));
        assertEquals("", StringUtils.substring("abc", 2, 0));
        assertEquals("c", StringUtils.substring("abc", 2, 4));
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

        assertNull(StringUtils.mid(null, 1, 2));
        assertEquals("", StringUtils.mid("abc", 1, -1));
        assertEquals("", StringUtils.mid("abc", 1, 0));
        assertEquals("b", StringUtils.mid("abc", 1, 1));
        assertEquals("bc", StringUtils.mid("abc", 1, 2));
        assertEquals("bc", StringUtils.mid("abc", 1, 4));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
    }

    @Test
    public void testSplitAndJoin() {
        assertNull(StringUtils.split(null));
        assertEquals(0, StringUtils.split("").length);
        assertArrayEquals(new String[]{"abc", "def", "ghi"}, StringUtils.split("abc def ghi"));
        assertArrayEquals(new String[]{"abc", "def", "ghi"}, StringUtils.split("abc  def   ghi"));

        assertNull(StringUtils.split(null, '.'));
        assertEquals(0, StringUtils.split("", '.').length);
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a.b.c", '.'));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a..b.c", '.'));

        assertNull(StringUtils.join((Object[]) null));
        assertEquals("", StringUtils.join(new Object[]{}));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}));
        assertEquals("a,b,c", StringUtils.join(new Object[]{"a", "b", "c"}, ","));
        assertEquals("null,b,c", StringUtils.join(new Object[]{null, "b", "c"}, ","));
    }

    @Test
    public void testReplace() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("abc", StringUtils.replace("abc", null, "b"));
        assertEquals("abc", StringUtils.replace("abc", "a", null));
        assertEquals("abc", StringUtils.replace("abc", "", "b"));
        assertEquals("bbc", StringUtils.replace("abc", "a", "b"));
        assertEquals("bba", StringUtils.replace("aba", "a", "b", 1));
        assertEquals("bbb", StringUtils.replace("aba", "a", "b", -1));
        assertEquals("bbb", StringUtils.replace("aba", "a", "b", 2));
    }

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("", StringUtils.defaultString(""));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("NULL", StringUtils.defaultString(null, "NULL"));
        assertEquals("", StringUtils.defaultString("", "NULL"));
        assertEquals("abc", StringUtils.defaultString("abc", "NULL"));
    }

    @Test
    public void testCapitalizeAndUncapitalize() {
        assertNull(StringUtils.capitalize(null));
        assertEquals("", StringUtils.capitalize(""));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("Cat", StringUtils.capitalize("Cat"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("", StringUtils.uncapitalize(""));
        assertEquals("cat", StringUtils.uncapitalize("cat"));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
    }

    @Test
    public void testUpperLowerCase() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("", StringUtils.upperCase(""));
        assertEquals("ABC", StringUtils.upperCase("abc"));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("", StringUtils.lowerCase(""));
        assertEquals("abc", StringUtils.lowerCase("ABC"));
    }

    @Test
    public void testStartsWithAndEndsWith() {
        assertFalse(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abc", null));
        assertTrue(StringUtils.startsWith("abc", ""));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("abcdef", "ABC"));

        assertFalse(StringUtils.startsWithIgnoreCase(null, null));
        assertFalse(StringUtils.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("abc", null));
        assertTrue(StringUtils.startsWithIgnoreCase("abc", ""));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "abc"));
        assertTrue(StringUtils.startsWithIgnoreCase("abcdef", "ABC"));

        assertFalse(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "abc"));
        assertFalse(StringUtils.endsWith("abc", null));
        assertTrue(StringUtils.endsWith("abc", ""));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("abcdef", "DEF"));

        assertFalse(StringUtils.endsWithIgnoreCase(null, null));
        assertFalse(StringUtils.endsWithIgnoreCase(null, "abc"));
        assertFalse(StringUtils.endsWithIgnoreCase("abc", null));
        assertTrue(StringUtils.endsWithIgnoreCase("abc", ""));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "def"));
        assertTrue(StringUtils.endsWithIgnoreCase("abcdef", "DEF"));
    }

    @Test
    public void testReverse() {
        assertNull(StringUtils.reverse(null));
        assertEquals("", StringUtils.reverse(""));
        assertEquals("cba", StringUtils.reverse("abc"));
    }

    @Test
    public void testAbbreviate() {
        assertNull(StringUtils.abbreviate(null, 4));
        assertEquals("", StringUtils.abbreviate("", 4));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 7));
        assertEquals("abcdefg", StringUtils.abbreviate("abcdefg", 8));
        assertEquals("a...", StringUtils.abbreviate("abcdefg", 4));
        assertEquals("ab...", StringUtils.abbreviate("abcdefg", 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbbreviate_InvalidWidth() {
        StringUtils.abbreviate("abcdefg", 3);
    }

    @Test
    public void testConstructor() {
        assertNotNull(new StringUtils());
    }
}