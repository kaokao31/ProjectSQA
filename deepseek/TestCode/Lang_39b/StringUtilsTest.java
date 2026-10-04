package org.apache.commons.lang3;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Test class for StringUtils, targeting bug 39 (replace with arrays).
 */
public class StringUtilsTest {

    @Before
    public void setUp() {
        // No setup needed for static methods
    }

    // ========== replace(String, String[], String[]) ==========

    @Test
    public void testReplace_StringStringArrayStringArray_NullText() {
        assertNull(StringUtils.replace(null, new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_NullSearchList() {
        assertNull(StringUtils.replace("abc", null, new String[]{"b"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_NullReplacementList() {
        assertNull(StringUtils.replace("abc", new String[]{"a"}, null));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_EmptyText() {
        assertEquals("", StringUtils.replace("", new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_EmptySearchList() {
        assertEquals("abc", StringUtils.replace("abc", new String[]{}, new String[]{"b"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_EmptyReplacementList() {
        assertEquals("abc", StringUtils.replace("abc", new String[]{"a"}, new String[]{}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_MismatchedLengths() {
        // Replacement list shorter than search list: remaining searches replaced with empty string
        assertEquals("bc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"x"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_ReplacementLonger() {
        assertEquals("xyzc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"x", "y", "z"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_NullInSearchList() {
        // Null in search list should be ignored or cause NPE? Bug 39: NPE when search list contains null
        // This test triggers the bug
        try {
            StringUtils.replace("abc", new String[]{"a", null}, new String[]{"x", "y"});
            fail("Expected NullPointerException due to null in search list");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testReplace_StringStringArrayStringArray_NullInReplacementList() {
        // Null in replacement list should be treated as empty string
        assertEquals("xbc", StringUtils.replace("abc", new String[]{"a"}, new String[]{null}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_SimpleReplace() {
        assertEquals("xbc", StringUtils.replace("abc", new String[]{"a"}, new String[]{"x"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_MultipleReplacements() {
        assertEquals("xyc", StringUtils.replace("abc", new String[]{"a", "b"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_OverlappingPatterns() {
        // Overlapping: "aa" replaced first, then "a" replaced
        assertEquals("xx", StringUtils.replace("aaa", new String[]{"aa", "a"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_NoMatch() {
        assertEquals("abc", StringUtils.replace("abc", new String[]{"d", "e"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_EmptyStringInSearch() {
        // Empty string in search list should be ignored or cause infinite loop? Usually ignored.
        assertEquals("abc", StringUtils.replace("abc", new String[]{""}, new String[]{"x"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_EmptyStringInReplacement() {
        assertEquals("bc", StringUtils.replace("abc", new String[]{"a"}, new String[]{""}));
    }

    // ========== replace(String, String, String) ==========

    @Test
    public void testReplace_StringStringString_NullText() {
        assertNull(StringUtils.replace(null, "a", "b"));
    }

    @Test
    public void testReplace_StringStringString_NullSearch() {
        assertNull(StringUtils.replace("abc", null, "b"));
    }

    @Test
    public void testReplace_StringStringString_NullReplacement() {
        assertEquals("abc", StringUtils.replace("abc", "a", null));
    }

    @Test
    public void testReplace_StringStringString_EmptyText() {
        assertEquals("", StringUtils.replace("", "a", "b"));
    }

    @Test
    public void testReplace_StringStringString_EmptySearch() {
        assertEquals("abc", StringUtils.replace("abc", "", "b"));
    }

    @Test
    public void testReplace_StringStringString_Simple() {
        assertEquals("xbc", StringUtils.replace("abc", "a", "x"));
    }

    @Test
    public void testReplace_StringStringString_AllOccurrences() {
        assertEquals("xxx", StringUtils.replace("aaa", "a", "x"));
    }

    @Test
    public void testReplace_StringStringString_NoMatch() {
        assertEquals("abc", StringUtils.replace("abc", "d", "x"));
    }

    // ========== replaceChars(String, String, String) ==========

    @Test
    public void testReplaceChars_StringStringString_NullText() {
        assertNull(StringUtils.replaceChars(null, "a", "b"));
    }

    @Test
    public void testReplaceChars_StringStringString_NullSearch() {
        assertNull(StringUtils.replaceChars("abc", null, "b"));
    }

    @Test
    public void testReplaceChars_StringStringString_NullReplacement() {
        assertEquals("abc", StringUtils.replaceChars("abc", "a", null));
    }

    @Test
    public void testReplaceChars_StringStringString_EmptyText() {
        assertEquals("", StringUtils.replaceChars("", "a", "b"));
    }

    @Test
    public void testReplaceChars_StringStringString_EmptySearch() {
        assertEquals("abc", StringUtils.replaceChars("abc", "", "b"));
    }

    @Test
    public void testReplaceChars_StringStringString_Simple() {
        assertEquals("xbc", StringUtils.replaceChars("abc", "a", "x"));
    }

    @Test
    public void testReplaceChars_StringStringString_ReplacementShorter() {
        assertEquals("bc", StringUtils.replaceChars("abc", "ab", "x"));
    }

    @Test
    public void testReplaceChars_StringStringString_ReplacementLonger() {
        assertEquals("xyzc", StringUtils.replaceChars("abc", "ab", "xyz"));
    }

    @Test
    public void testReplaceChars_StringStringString_DuplicateCharsInSearch() {
        assertEquals("xbc", StringUtils.replaceChars("abc", "aa", "x"));
    }

    // ========== replaceEach(String, String[], String[]) ==========

    @Test
    public void testReplaceEach_StringStringArrayStringArray_NullText() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_NullSearchList() {
        assertNull(StringUtils.replaceEach("abc", null, new String[]{"b"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_NullReplacementList() {
        assertNull(StringUtils.replaceEach("abc", new String[]{"a"}, null));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_EmptyText() {
        assertEquals("", StringUtils.replaceEach("", new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_EmptySearchList() {
        assertEquals("abc", StringUtils.replaceEach("abc", new String[]{}, new String[]{"b"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_EmptyReplacementList() {
        assertEquals("abc", StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_MismatchedLengths() {
        assertEquals("bc", StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"x"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_ReplacementLonger() {
        assertEquals("xyzc", StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"x", "y", "z"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_NullInSearchList() {
        // This triggers the NPE bug (Defects4J bug 39)
        try {
            StringUtils.replaceEach("abc", new String[]{"a", null}, new String[]{"x", "y"});
            fail("Expected NullPointerException due to null in search list");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_NullInReplacementList() {
        assertEquals("xbc", StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{null}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_Simple() {
        assertEquals("xbc", StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{"x"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_Multiple() {
        assertEquals("xyc", StringUtils.replaceEach("abc", new String[]{"a", "b"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_Overlapping() {
        assertEquals("xx", StringUtils.replaceEach("aaa", new String[]{"aa", "a"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_NoMatch() {
        assertEquals("abc", StringUtils.replaceEach("abc", new String[]{"d", "e"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_EmptyStringInSearch() {
        assertEquals("abc", StringUtils.replaceEach("abc", new String[]{""}, new String[]{"x"}));
    }

    @Test
    public void testReplaceEach_StringStringArrayStringArray_EmptyStringInReplacement() {
        assertEquals("bc", StringUtils.replaceEach("abc", new String[]{"a"}, new String[]{""}));
    }

    // ========== replaceEachRepeatedly(String, String[], String[]) ==========

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_NullText() {
        assertNull(StringUtils.replaceEachRepeatedly(null, new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_NullSearchList() {
        assertNull(StringUtils.replaceEachRepeatedly("abc", null, new String[]{"b"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_NullReplacementList() {
        assertNull(StringUtils.replaceEachRepeatedly("abc", new String[]{"a"}, null));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_EmptyText() {
        assertEquals("", StringUtils.replaceEachRepeatedly("", new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_EmptySearchList() {
        assertEquals("abc", StringUtils.replaceEachRepeatedly("abc", new String[]{}, new String[]{"b"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_EmptyReplacementList() {
        assertEquals("abc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a"}, new String[]{}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_MismatchedLengths() {
        assertEquals("bc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a", "b"}, new String[]{"x"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_ReplacementLonger() {
        assertEquals("xyzc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a", "b"}, new String[]{"x", "y", "z"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_NullInSearchList() {
        // This triggers the NPE bug (Defects4J bug 39)
        try {
            StringUtils.replaceEachRepeatedly("abc", new String[]{"a", null}, new String[]{"x", "y"});
            fail("Expected NullPointerException due to null in search list");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_NullInReplacementList() {
        assertEquals("xbc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a"}, new String[]{null}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_Simple() {
        assertEquals("xbc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a"}, new String[]{"x"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_Multiple() {
        assertEquals("xyc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a", "b"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_Overlapping() {
        assertEquals("xx", StringUtils.replaceEachRepeatedly("aaa", new String[]{"aa", "a"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_NoMatch() {
        assertEquals("abc", StringUtils.replaceEachRepeatedly("abc", new String[]{"d", "e"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_EmptyStringInSearch() {
        assertEquals("abc", StringUtils.replaceEachRepeatedly("abc", new String[]{""}, new String[]{"x"}));
    }

    @Test
    public void testReplaceEachRepeatedly_StringStringArrayStringArray_EmptyStringInReplacement() {
        assertEquals("bc", StringUtils.replaceEachRepeatedly("abc", new String[]{"a"}, new String[]{""}));
    }

    // ========== Additional edge cases for replace methods ==========

    @Test
    public void testReplace_StringStringArrayStringArray_SearchListContainsEmptyString() {
        // Empty string in search list should not cause issues
        assertEquals("abc", StringUtils.replace("abc", new String[]{""}, new String[]{"x"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_ReplacementListContainsEmptyString() {
        assertEquals("bc", StringUtils.replace("abc", new String[]{"a"}, new String[]{""}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_SearchListContainsSameStringMultipleTimes() {
        // Duplicate search strings: first occurrence replaced
        assertEquals("xbc", StringUtils.replace("abc", new String[]{"a", "a"}, new String[]{"x", "y"}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_ReplacementListContainsNull() {
        // Null replacement treated as empty string
        assertEquals("xbc", StringUtils.replace("abc", new String[]{"a"}, new String[]{null}));
    }

    @Test
    public void testReplace_StringStringArrayStringArray_AllNullsInSearchList() {
        // All null search strings: should throw NPE
        try {
            StringUtils.replace("abc", new String[]{null, null}, new String[]{"x", "y"});
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testReplace_StringStringArrayStringArray_AllNullsInReplacementList() {
        // All null replacements: should replace with empty strings
        assertEquals("", StringUtils.replace("abc", new String[]{"a", "b", "c"}, new String[]{null, null, null}));
    }

    // ========== Tests for replaceChars with char arrays ==========

    @Test
    public void testReplaceChars_StringCharArrayCharArray_NullText() {
        assertNull(StringUtils.replaceChars(null, new char[]{'a'}, new char[]{'b'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_NullSearchChars() {
        assertNull(StringUtils.replaceChars("abc", null, new char[]{'b'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_NullReplaceChars() {
        assertNull(StringUtils.replaceChars("abc", new char[]{'a'}, null));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_EmptyText() {
        assertEquals("", StringUtils.replaceChars("", new char[]{'a'}, new char[]{'b'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_EmptySearchChars() {
        assertEquals("abc", StringUtils.replaceChars("abc", new char[]{}, new char[]{'b'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_EmptyReplaceChars() {
        assertEquals("abc", StringUtils.replaceChars("abc", new char[]{'a'}, new char[]{}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_Simple() {
        assertEquals("xbc", StringUtils.replaceChars("abc", new char[]{'a'}, new char[]{'x'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_Multiple() {
        assertEquals("xyc", StringUtils.replaceChars("abc", new char[]{'a', 'b'}, new char[]{'x', 'y'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_ReplaceShorter() {
        assertEquals("bc", StringUtils.replaceChars("abc", new char[]{'a', 'b'}, new char[]{'x'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_ReplaceLonger() {
        assertEquals("xyzc", StringUtils.replaceChars("abc", new char[]{'a', 'b'}, new char[]{'x', 'y', 'z'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_NoMatch() {
        assertEquals("abc", StringUtils.replaceChars("abc", new char[]{'d'}, new char[]{'x'}));
    }

    @Test
    public void testReplaceChars_StringCharArrayCharArray_DuplicateSearchChars() {
        assertEquals("xbc", StringUtils.replaceChars("abc", new char[]{'a', 'a'}, new char[]{'x', 'y'}));
    }

    // ========== Tests for replaceOnce ==========

    @Test
    public void testReplaceOnce_StringStringString_NullText() {
        assertNull(StringUtils.replaceOnce(null, "a", "b"));
    }

    @Test
    public void testReplaceOnce_StringStringString_NullSearch() {
        assertNull(StringUtils.replaceOnce("abc", null, "b"));
    }

    @Test
    public void testReplaceOnce_StringStringString_NullReplacement() {
        assertEquals("abc", StringUtils.replaceOnce("abc", "a", null));
    }

    @Test
    public void testReplaceOnce_StringStringString_EmptyText() {
        assertEquals("", StringUtils.replaceOnce("", "a", "b"));
    }

    @Test
    public void testReplaceOnce_StringStringString_EmptySearch() {
        assertEquals("abc", StringUtils.replaceOnce("abc", "", "b"));
    }

    @Test
    public void testReplaceOnce_StringStringString_Simple() {
        assertEquals("xbc", StringUtils.replaceOnce("abc", "a", "x"));
    }

    @Test
    public void testReplaceOnce_StringStringString_OnlyFirst() {
        assertEquals("xba", StringUtils.replaceOnce("aba", "a", "x"));
    }

    @Test
    public void testReplaceOnce_StringStringString_NoMatch() {
        assertEquals("abc", StringUtils.replaceOnce("abc", "d", "x"));
    }

    // ========== Tests for replaceIgnoreCase ==========

    @Test
    public void testReplaceIgnoreCase_StringStringString_NullText() {
        assertNull(StringUtils.replaceIgnoreCase(null, "a", "b"));
    }

    @Test
    public void testReplaceIgnoreCase_StringStringString_NullSearch() {
        assertNull(StringUtils.replaceIgnoreCase("abc", null, "b"));
    }

    @Test
    public void testReplaceIgnoreCase_StringStringString_NullReplacement() {
        assertEquals("abc", StringUtils.replaceIgnoreCase("abc", "a", null));
    }

    @Test
    public void testReplaceIgnoreCase_StringStringString_EmptyText() {
        assertEquals("", StringUtils.replaceIgnoreCase("", "a", "b"));
    }

    @Test
    public void testReplaceIgnoreCase_StringStringString_EmptySearch() {
        assertEquals("abc", StringUtils.replaceIgnoreCase("abc", "", "b"));
    }

    @Test
    public void testReplaceIgnoreCase_StringStringString_Simple() {
        assertEquals("xbc", StringUtils.replaceIgnoreCase("abc", "A", "x"));
    }

    @Test
    public void testReplaceIgnoreCase_StringStringString_AllOccurrences() {
        assertEquals("xxx", StringUtils.replaceIgnoreCase("AaA", "a", "x"));
    }

    @Test
    public void testReplaceIgnoreCase_StringStringString_NoMatch() {
        assertEquals("abc", StringUtils.replaceIgnoreCase("abc", "d", "x"));
    }

    // ========== Tests for replaceOnceIgnoreCase ==========

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_NullText() {
        assertNull(StringUtils.replaceOnceIgnoreCase(null, "a", "b"));
    }

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_NullSearch() {
        assertNull(StringUtils.replaceOnceIgnoreCase("abc", null, "b"));
    }

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_NullReplacement() {
        assertEquals("abc", StringUtils.replaceOnceIgnoreCase("abc", "a", null));
    }

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_EmptyText() {
        assertEquals("", StringUtils.replaceOnceIgnoreCase("", "a", "b"));
    }

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_EmptySearch() {
        assertEquals("abc", StringUtils.replaceOnceIgnoreCase("abc", "", "b"));
    }

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_Simple() {
        assertEquals("xbc", StringUtils.replaceOnceIgnoreCase("abc", "A", "x"));
    }

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_OnlyFirst() {
        assertEquals("xba", StringUtils.replaceOnceIgnoreCase("Aba", "a", "x"));
    }

    @Test
    public void testReplaceOnceIgnoreCase_StringStringString_NoMatch() {
        assertEquals("abc", StringUtils.replaceOnceIgnoreCase("abc", "d", "x"));
    }

    // ========== Tests for replace with max ==========

    @Test
    public void testReplace_StringStringStringInt_NullText() {
        assertNull(StringUtils.replace(null, "a", "b", 1));
    }

    @Test
    public void testReplace_StringStringStringInt_NullSearch() {
        assertNull(StringUtils.replace("abc", null, "b", 1));
    }

    @Test
    public void testReplace_StringStringStringInt_NullReplacement() {
        assertEquals("abc", StringUtils.replace("abc", "a", null, 1));
    }

    @Test
    public void testReplace_StringStringStringInt_EmptyText() {
        assertEquals("", StringUtils.replace("", "a", "b", 1));
    }

    @Test
    public void testReplace_StringStringStringInt_EmptySearch() {
        assertEquals("abc", StringUtils.replace("abc", "", "b", 1));
    }

    @Test
    public void testReplace_StringStringStringInt_ZeroMax() {
        assertEquals("abc", StringUtils.replace("abc", "a", "x", 0));
    }

    @Test
    public void testReplace_StringStringStringInt_NegativeMax() {
        assertEquals("xbc", StringUtils.replace("abc", "a", "x", -1));
    }

    @Test
    public void testReplace_StringStringStringInt_OneReplacement() {
        assertEquals("xbc", StringUtils.replace("abc", "a", "x", 1));
    }

    @Test
    public void testReplace_StringStringStringInt_LimitReplacement() {
        assertEquals("xba", StringUtils.replace("aba", "a", "x", 1));
    }

    @Test
    public void testReplace_StringStringStringInt_AllReplacement() {
        assertEquals("xxx", StringUtils.replace("aaa", "a", "x", 10));
    }

    @Test
    public void testReplace_StringStringStringInt_NoMatch() {
        assertEquals("abc", StringUtils.replace("abc", "d", "x", 1));
    }
}