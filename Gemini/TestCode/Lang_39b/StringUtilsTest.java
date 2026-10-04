package org.apache.commons.lang3;

import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Test
    public void testReplace_StringStringArrayStringArray() {
        // Defects4J Lang-39 triggering test case
        // replaceEach with null elements inside replacement array
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{null}));
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{null, null}));
        assertEquals("d", StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{"c", "d"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", null, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[0], new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, null));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{"a"}, new String[0]));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{null}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEach("aba", new String[]{""}, new String[]{"b"}));

        assertEquals("bcc", StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"b", "c"}));
        assertEquals("qba", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{"q"}));
        assertEquals("cbc", StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{"c", "b"}));

        // Testing length increase / decrease logic and null elements
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}));
        assertEquals("longer-b-longer", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{"longer"}));
        assertEquals("b", StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{null}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEach_DifferentLengths() {
        StringUtils.replaceEach("aba", new String[]{"a", "b"}, new String[]{"c"});
    }

    @Test
    public void testReplaceEachRepeatedly() {
        assertNull(StringUtils.replaceEachRepeatedly(null, new String[]{"a"}, new String[]{"b"}));
        assertEquals("", StringUtils.replaceEachRepeatedly("", new String[]{"a"}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEachRepeatedly("aba", null, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEachRepeatedly("aba", new String[0], new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, null));
        assertEquals("aba", StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, new String[0]));
        assertEquals("aba", StringUtils.replaceEachRepeatedly("aba", new String[]{null}, new String[]{"b"}));
        assertEquals("aba", StringUtils.replaceEachRepeatedly("aba", new String[]{""}, new String[]{"b"}));

        assertEquals("ccc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a", "b"}, new String[]{"b", "c"}));
        assertEquals("b", StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, new String[]{null}));
    }

    @Test(expected = IllegalStateException.class)
    public void testReplaceEachRepeatedly_TimeToLive() {
        StringUtils.replaceEachRepeatedly("aba", new String[]{"a"}, new String[]{"a"});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReplaceEachRepeatedly_DifferentLengths() {
        StringUtils.replaceEachRepeatedly("aba", new String[]{"a", "b"}, new String[]{"c"});
    }

    @Test
    public void testReplace_StringStringString() {
        assertNull(StringUtils.replace(null, "a", "b"));
        assertEquals("", StringUtils.replace("", "a", "b"));
        assertEquals("any", StringUtils.replace("any", null, "b"));
        assertEquals("any", StringUtils.replace("any", "", "b"));
        assertEquals("any", StringUtils.replace("any", "a", null));
        assertEquals("any", StringUtils.replace("any", "a", "b", 0));
        assertEquals("bny", StringUtils.replace("any", "a", "b", 1));
        assertEquals("bny", StringUtils.replace("any", "a", "b", -1));
        assertEquals("bb", StringUtils.replace("aa", "a", "b"));
        assertEquals("bba", StringUtils.replace("aaa", "a", "b", 2));
    }

    @Test
    public void testReplaceOnce() {
        assertNull(StringUtils.replaceOnce(null, "a", "b"));
        assertEquals("bba", StringUtils.replaceOnce("aba", "a", "b"));
    }

    @Test
    public void testIsEmptyAndIsBlank() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertFalse(StringUtils.isEmpty(" "));
        assertFalse(StringUtils.isEmpty("bob"));

        assertTrue(StringUtils.isNotEmpty("bob"));
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));

        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank(" \t \n "));
        assertFalse(StringUtils.isBlank("  bob  "));

        assertTrue(StringUtils.isNotBlank("bob"));
        assertFalse(StringUtils.isNotBlank("   "));
        assertFalse(StringUtils.isNotBlank(null));
    }

    @Test
    public void testTrim() {
        assertNull(StringUtils.trim(null));
        assertEquals("", StringUtils.trim(""));
        assertEquals("", StringUtils.trim("   "));
        assertEquals("abc", StringUtils.trim("  abc  "));
        assertEquals("abc", StringUtils.trim("abc"));

        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));

        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }

    @Test
    public void testEquals() {
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
    public void testIndexOfAndContains() {
        assertEquals(-1, StringUtils.indexOf(null, 'a'));
        assertEquals(-1, StringUtils.indexOf("", 'a'));
        assertEquals(0, StringUtils.indexOf("aabaabaa", 'a'));
        assertEquals(2, StringUtils.indexOf("aabaabaa", 'b'));

        assertEquals(-1, StringUtils.indexOf(null, "a"));
        assertEquals(-1, StringUtils.indexOf("aabaabaa", (String) null));
        assertEquals(0, StringUtils.indexOf("aabaabaa", "a"));
        assertEquals(2, StringUtils.indexOf("aabaabaa", "b"));

        assertFalse(StringUtils.contains(null, 'a'));
        assertFalse(StringUtils.contains("", 'a'));
        assertTrue(StringUtils.contains("abc", 'a'));
        assertFalse(StringUtils.contains("abc", 'z'));

        assertFalse(StringUtils.contains(null, "a"));
        assertFalse(StringUtils.contains("abc", (String) null));
        assertTrue(StringUtils.contains("abc", "a"));
        assertFalse(StringUtils.contains("abc", "z"));

        assertTrue(StringUtils.containsIgnoreCase("ABC", "a"));
        assertFalse(StringUtils.containsIgnoreCase("ABC", "z"));
        assertFalse(StringUtils.containsIgnoreCase(null, "a"));
    }

    @Test
    public void testSubstring() {
        assertNull(StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring("", 0));
        assertEquals("abc", StringUtils.substring("abc", 0));
        assertEquals("bc", StringUtils.substring("abc", 1));
        assertEquals("", StringUtils.substring("abc", 4));
        assertEquals("c", StringUtils.substring("abc", -1));
        assertEquals("abc", StringUtils.substring("abc", -4));

        assertNull(StringUtils.substring(null, 0, 1));
        assertEquals("", StringUtils.substring("", 0, 1));
        assertEquals("ab", StringUtils.substring("abcd", 0, 2));
        assertEquals("bc", StringUtils.substring("abcd", 1, 3));
        assertEquals("", StringUtils.substring("abcd", 2, 1));
        assertEquals("cd", StringUtils.substring("abcd", -2, 4));
        assertEquals("ab", StringUtils.substring("abcd", 0, -2));
    }

    @Test
    public void testLeftRightMid() {
        assertNull(StringUtils.left(null, 2));
        assertEquals("", StringUtils.left("abc", -1));
        assertEquals("", StringUtils.left("abc", 0));
        assertEquals("ab", StringUtils.left("abc", 2));
        assertEquals("abc", StringUtils.left("abc", 5));

        assertNull(StringUtils.right(null, 2));
        assertEquals("", StringUtils.right("abc", -1));
        assertEquals("", StringUtils.right("abc", 0));
        assertEquals("bc", StringUtils.right("abc", 2));
        assertEquals("abc", StringUtils.right("abc", 5));

        assertNull(StringUtils.mid(null, 0, 2));
        assertEquals("", StringUtils.mid("abc", 0, -1));
        assertEquals("", StringUtils.mid("abc", 4, 2));
        assertEquals("ab", StringUtils.mid("abc", 0, 2));
        assertEquals("bc", StringUtils.mid("abc", 1, 2));
        assertEquals("bc", StringUtils.mid("abc", 1, 5));
        assertEquals("ab", StringUtils.mid("abc", -1, 2));
    }

    @Test
    public void testJoin() {
        assertNull(StringUtils.join((Object[]) null, ","));
        assertEquals("", StringUtils.join(new Object[0], ","));
        assertEquals("a,b,c", StringUtils.join(new Object[]{"a", "b", "c"}, ","));
        assertEquals("abc", StringUtils.join(new Object[]{"a", "b", "c"}, null));
        assertEquals("1,2,3", StringUtils.join(new int[]{1, 2, 3}, ','));
        assertEquals("1.0,2.0", StringUtils.join(new double[]{1.0, 2.0}, ','));
        assertEquals("a,b", StringUtils.join(Arrays.asList("a", "b"), ","));
    }

    @Test
    public void testSplit() {
        assertNull(StringUtils.split(null));
        assertEquals(0, StringUtils.split("").length);
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a b  c"));
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("a,b,c", ","));
        assertArrayEquals(new String[]{"a", "b,c"}, StringUtils.split("a,b,c", ",", 2));
    }

    @Test
    public void testCaseConversions() {
        assertNull(StringUtils.upperCase(null));
        assertEquals("ABC", StringUtils.upperCase("abc"));
        assertEquals("ABC", StringUtils.upperCase("abc", Locale.ENGLISH));

        assertNull(StringUtils.lowerCase(null));
        assertEquals("abc", StringUtils.lowerCase("ABC"));
        assertEquals("abc", StringUtils.lowerCase("ABC", Locale.ENGLISH));

        assertNull(StringUtils.capitalize(null));
        assertEquals("Cat", StringUtils.capitalize("cat"));
        assertEquals("Cat", StringUtils.capitalize("Cat"));

        assertNull(StringUtils.uncapitalize(null));
        assertEquals("cat", StringUtils.uncapitalize("Cat"));
        assertEquals("cat", StringUtils.uncapitalize("cat"));
    }

    @Test
    public void testPaddingAndCenter() {
        assertNull(StringUtils.repeat(null, 2));
        assertEquals("", StringUtils.repeat("a", 0));
        assertEquals("", StringUtils.repeat("a", -1));
        assertEquals("aaa", StringUtils.repeat("a", 3));

        assertNull(StringUtils.leftPad(null, 5));
        assertEquals("   ab", StringUtils.leftPad("ab", 5));
        assertEquals("000ab", StringUtils.leftPad("ab", 5, '0'));
        assertEquals("xyzab", StringUtils.leftPad("ab", 5, "xyz"));

        assertNull(StringUtils.rightPad(null, 5));
        assertEquals("ab   ", StringUtils.rightPad("ab", 5));
        assertEquals("ab000", StringUtils.rightPad("ab", 5, '0'));
        assertEquals("abxyz", StringUtils.rightPad("ab", 5, "xyz"));

        assertNull(StringUtils.center(null, 5));
        assertEquals(" ab  ", StringUtils.center("ab", 5));
        assertEquals("00ab0", StringUtils.center("ab", 5, '0'));
        assertEquals("xyabx", StringUtils.center("ab", 5, "xyz"));
    }

    @Test
    public void testDefaultString() {
        assertEquals("", StringUtils.defaultString(null));
        assertEquals("", StringUtils.defaultString(""));
        assertEquals("abc", StringUtils.defaultString("abc"));
        assertEquals("default", StringUtils.defaultString(null, "default"));
        assertEquals("abc", StringUtils.defaultString("abc", "default"));

        assertEquals("default", StringUtils.defaultIfBlank(null, "default"));
        assertEquals("default", StringUtils.defaultIfBlank("   ", "default"));
        assertEquals("abc", StringUtils.defaultIfBlank("abc", "default"));

        assertEquals("default", StringUtils.defaultIfEmpty(null, "default"));
        assertEquals("default", StringUtils.defaultIfEmpty("", "default"));
        assertEquals("   ", StringUtils.defaultIfEmpty("   ", "default"));
        assertEquals("abc", StringUtils.defaultIfEmpty("abc", "default"));
    }

    @Test
    public void testRemove() {
        assertNull(StringUtils.remove(null, "a"));
        assertEquals("bc", StringUtils.remove("abc", "a"));
        assertEquals("abc", StringUtils.remove("abc", "z"));
        assertEquals("bc", StringUtils.remove("abc", 'a'));
        assertEquals("abc", StringUtils.remove("abc", 'z'));

        assertNull(StringUtils.removeStart(null, "a"));
        assertEquals("bc", StringUtils.removeStart("abc", "a"));
        assertEquals("abc", StringUtils.removeStart("abc", "b"));

        assertNull(StringUtils.removeEnd(null, "c"));
        assertEquals("ab", StringUtils.removeEnd("abc", "c"));
        assertEquals("abc", StringUtils.removeEnd("abc", "b"));
    }

    @Test
    public void testStartsWithEndsWith() {
        assertTrue(StringUtils.startsWith(null, null));
        assertFalse(StringUtils.startsWith(null, "abc"));
        assertFalse(StringUtils.startsWith("abc", null));
        assertTrue(StringUtils.startsWith("abcdef", "abc"));
        assertFalse(StringUtils.startsWith("ABCDEF", "abc"));

        assertTrue(StringUtils.startsWithIgnoreCase("ABCDEF", "abc"));
        assertFalse(StringUtils.startsWithIgnoreCase("ABCDEF", "xyz"));

        assertTrue(StringUtils.endsWith(null, null));
        assertFalse(StringUtils.endsWith(null, "def"));
        assertFalse(StringUtils.endsWith("def", null));
        assertTrue(StringUtils.endsWith("abcdef", "def"));
        assertFalse(StringUtils.endsWith("ABCDEF", "def"));

        assertTrue(StringUtils.endsWithIgnoreCase("ABCDEF", "def"));
        assertFalse(StringUtils.endsWithIgnoreCase("ABCDEF", "xyz"));
    }

    @Test
    public void testConstructor() {
        assertNotNull(new StringUtils());
    }
}