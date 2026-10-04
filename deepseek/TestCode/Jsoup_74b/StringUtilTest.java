package org.jsoup.helper;

import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;

public class StringUtilTest {

    // Tests for in(String needle, String... haystack)
    @Test
    public void testInWithNullNeedle() {
        assertFalse(StringUtil.in(null, "a", "b"));
    }

    @Test
    public void testInWithEmptyNeedle() {
        assertTrue(StringUtil.in("", "a", "b", ""));
    }

    @Test
    public void testInWithExactMatch() {
        assertTrue(StringUtil.in("b", "a", "b", "c"));
    }

    @Test
    public void testInWithNoMatch() {
        assertFalse(StringUtil.in("d", "a", "b", "c"));
    }

    @Test
    public void testInWithNullInHaystack() {
        assertTrue(StringUtil.in(null, "a", null, "b"));
    }

    @Test
    public void testInWithEmptyHaystack() {
        assertFalse(StringUtil.in("a"));
    }

    @Test
    public void testInWithMultipleNulls() {
        assertTrue(StringUtil.in(null, null, null));
    }

    // Tests for isBlank(String string)
    @Test
    public void testIsBlankWithNull() {
        assertTrue(StringUtil.isBlank(null));
    }

    @Test
    public void testIsBlankWithEmpty() {
        assertTrue(StringUtil.isBlank(""));
    }

    @Test
    public void testIsBlankWithWhitespace() {
        assertTrue(StringUtil.isBlank("   "));
    }

    @Test
    public void testIsBlankWithTabAndNewline() {
        assertTrue(StringUtil.isBlank("\t\n\r"));
    }

    @Test
    public void testIsBlankWithNonBlank() {
        assertFalse(StringUtil.isBlank("a"));
    }

    @Test
    public void testIsBlankWithLeadingWhitespace() {
        assertFalse(StringUtil.isBlank(" a"));
    }

    @Test
    public void testIsBlankWithTrailingWhitespace() {
        assertFalse(StringUtil.isBlank("a "));
    }

    // Tests for isNumeric(String string)
    @Test
    public void testIsNumericWithNull() {
        assertFalse(StringUtil.isNumeric(null));
    }

    @Test
    public void testIsNumericWithEmpty() {
        assertFalse(StringUtil.isNumeric(""));
    }

    @Test
    public void testIsNumericWithDigits() {
        assertTrue(StringUtil.isNumeric("12345"));
    }

    @Test
    public void testIsNumericWithLeadingZero() {
        assertTrue(StringUtil.isNumeric("00123"));
    }

    @Test
    public void testIsNumericWithNonNumeric() {
        assertFalse(StringUtil.isNumeric("12a34"));
    }

    @Test
    public void testIsNumericWithNegativeSign() {
        assertFalse(StringUtil.isNumeric("-123"));
    }

    @Test
    public void testIsNumericWithDecimalPoint() {
        assertFalse(StringUtil.isNumeric("12.34"));
    }

    @Test
    public void testIsNumericWithWhitespace() {
        assertFalse(StringUtil.isNumeric(" 123"));
    }

    // Tests for padding(int width)
    @Test(expected = IllegalArgumentException.class)
    public void testPaddingWithNegativeWidth() {
        StringUtil.padding(-1);
    }

    @Test
    public void testPaddingWithZeroWidth() {
        assertEquals("", StringUtil.padding(0));
    }

    @Test
    public void testPaddingWithPositiveWidth() {
        assertEquals("     ", StringUtil.padding(5));
    }

    @Test
    public void testPaddingWithLargeWidth() {
        String result = StringUtil.padding(100);
        assertEquals(100, result.length());
        assertTrue(result.matches(" +"));
    }

    // Tests for join(Collection<?> collection, String sep)
    @Test
    public void testJoinWithNullCollection() {
        assertEquals("", StringUtil.join(null, ","));
    }

    @Test
    public void testJoinWithEmptyCollection() {
        assertEquals("", StringUtil.join(Collections.emptyList(), ","));
    }

    @Test
    public void testJoinWithSingleElement() {
        assertEquals("a", StringUtil.join(Collections.singletonList("a"), ","));
    }

    @Test
    public void testJoinWithMultipleElements() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a,b,c", StringUtil.join(list, ","));
    }

    @Test
    public void testJoinWithNullElements() {
        List<String> list = Arrays.asList("a", null, "c");
        assertEquals("a,null,c", StringUtil.join(list, ","));
    }

    @Test
    public void testJoinWithEmptySeparator() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("abc", StringUtil.join(list, ""));
    }

    @Test
    public void testJoinWithNonStringObjects() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertEquals("1,2,3", StringUtil.join(list, ","));
    }

    // Tests for normaliseWhitespace(String string)
    @Test
    public void testNormaliseWhitespaceWithNull() {
        assertNull(StringUtil.normaliseWhitespace(null));
    }

    @Test
    public void testNormaliseWhitespaceWithEmpty() {
        assertEquals("", StringUtil.normaliseWhitespace(""));
    }

    @Test
    public void testNormaliseWhitespaceWithMultipleSpaces() {
        assertEquals("a b c", StringUtil.normaliseWhitespace("a   b   c"));
    }

    @Test
    public void testNormaliseWhitespaceWithLeadingTrailingSpaces() {
        assertEquals("a b", StringUtil.normaliseWhitespace("  a   b  "));
    }

    @Test
    public void testNormaliseWhitespaceWithTabsAndNewlines() {
        assertEquals("a b c", StringUtil.normaliseWhitespace("a\t\nb\n\nc"));
    }

    @Test
    public void testNormaliseWhitespaceWithOnlyWhitespace() {
        assertEquals("", StringUtil.normaliseWhitespace("   \t\n  "));
    }

    @Test
    public void testNormaliseWhitespaceWithNoChange() {
        assertEquals("hello world", StringUtil.normaliseWhitespace("hello world"));
    }

    // Tests for resolve(String base, String rel)
    @Test
    public void testResolveWithAbsoluteBaseAndRelative() {
        assertEquals("http://example.com/path", StringUtil.resolve("http://example.com", "path"));
    }

    @Test
    public void testResolveWithAbsoluteBaseAndAbsoluteRel() {
        assertEquals("http://other.com/path", StringUtil.resolve("http://example.com", "http://other.com/path"));
    }

    @Test
    public void testResolveWithNullBase() {
        assertEquals("path", StringUtil.resolve(null, "path"));
    }

    @Test
    public void testResolveWithNullRel() {
        assertEquals("http://example.com", StringUtil.resolve("http://example.com", null));
    }

    @Test
    public void testResolveWithEmptyRel() {
        assertEquals("http://example.com", StringUtil.resolve("http://example.com", ""));
    }

    @Test
    public void testResolveWithRelativePath() {
        assertEquals("http://example.com/dir/page.html", StringUtil.resolve("http://example.com/dir/", "page.html"));
    }

    @Test
    public void testResolveWithParentPath() {
        assertEquals("http://example.com/page.html", StringUtil.resolve("http://example.com/dir/", "../page.html"));
    }

    @Test
    public void testResolveWithQueryString() {
        assertEquals("http://example.com/path?q=1", StringUtil.resolve("http://example.com/path", "?q=1"));
    }

    // Additional edge cases for in method (potential bug trigger)
    @Test
    public void testInWithNeedleContainingSpecialChars() {
        assertTrue(StringUtil.in("a.b", "a.b", "c"));
    }

    @Test
    public void testInWithNeedleAsSubstringOfHaystack() {
        // Should be false if exact match is required
        assertFalse(StringUtil.in("ab", "abc", "def"));
    }

    @Test
    public void testInWithCaseSensitivity() {
        // Assuming case-sensitive
        assertFalse(StringUtil.in("A", "a", "b"));
    }

    @Test
    public void testInWithLargeHaystack() {
        String[] haystack = new String[1000];
        Arrays.fill(haystack, "x");
        haystack[500] = "needle";
        assertTrue(StringUtil.in("needle", haystack));
    }

    // Tests for isBlank with non-string objects? Not applicable.

    // Test for padding with width that might cause overflow (if using int)
    @Test
    public void testPaddingWithMaxWidth() {
        // Assuming padding uses StringBuilder, should handle large width
        String result = StringUtil.padding(10000);
        assertEquals(10000, result.length());
    }

    // Test for join with collection containing null and empty strings
    @Test
    public void testJoinWithMixedNullAndEmpty() {
        List<String> list = Arrays.asList("", null, "a");
        assertEquals(",null,a", StringUtil.join(list, ","));
    }

    // Test for normaliseWhitespace with non-breaking space? Not required.

    // Test for resolve with base having fragment
    @Test
    public void testResolveWithBaseFragment() {
        assertEquals("http://example.com/page#section", StringUtil.resolve("http://example.com/page#section", ""));
    }

    @Test
    public void testResolveWithRelFragment() {
        assertEquals("http://example.com/page#new", StringUtil.resolve("http://example.com/page", "#new"));
    }
}