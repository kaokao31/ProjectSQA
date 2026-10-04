package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

public class WordUtilsTest {

    // Common input strings for testing
    private static final String EMPTY_STRING = "";
    private static final String SINGLE_WORD = "Hello";
    private static final String TWO_WORDS = "Hello World";
    private static final String MULTIPLE_WORDS = "Hello World Java Test";
    private static final String TRAILING_SPACE = "Hello World ";
    private static final String LEADING_SPACE = " Hello World";
    private static final String MULTIPLE_SPACES = "Hello   World";
    private static final String SPECIAL_CHARS = "Hello-World_Test";
    private static final String LONG_WORD = "Supercalifragilisticexpialidocious";
    private static final String NEWLINE_STRING = "Hello\nWorld";
    private static final String TAB_STRING = "Hello\tWorld";

    @Test
    public void testWrap_NullInput() {
        assertNull("Null input should return null", WordUtils.wrap(null, 10));
    }

    @Test
    public void testWrap_EmptyString() {
        assertEquals("Empty string should return empty", EMPTY_STRING, WordUtils.wrap(EMPTY_STRING, 10));
    }

    @Test
    public void testWrap_NoWrapNeeded() {
        assertEquals("String shorter than wrap length should remain unchanged",
                "Hello", WordUtils.wrap(SINGLE_WORD, 20));
    }

    @Test
    public void testWrap_WrapAtWordBoundary() {
        String result = WordUtils.wrap("Hello World", 6);
        assertEquals("Wrap at word boundary failed", "Hello\nWorld", result);
    }

    @Test
    public void testWrap_WrapInsideWord() {
        String result = WordUtils.wrap("HelloWorld", 5);
        assertEquals("Wrap inside word failed", "Hello\nWorld", result);
    }

    @Test
    public void testWrap_ExactWordLength() {
        String result = WordUtils.wrap("Hello World", 5);
        assertEquals("Wrap at exact length failed", "Hello\nWorld", result);
    }

    @Test
    public void testWrap_MultipleLines() {
        String input = "one two three four";
        String result = WordUtils.wrap(input, 7);
        assertEquals("Multiple line wrap failed", "one two\nthree\nfour", result);
    }

    @Test
    public void testWrap_LongWordExceedingWrapLength() {
        String result = WordUtils.wrap(LONG_WORD, 10);
        assertNotNull("Long word wrap should not return null", result);
        assertTrue("Long word should be broken into multiple lines",
                result.contains("\n"));
    }

    @Test
    public void testWrap_WithWrapString() {
        String result = WordUtils.wrap("Hello World", 6, "\n", false);
        assertEquals("Custom wrap string with 'break' false failed",
                "Hello\nWorld", result);
    }

    @Test
    public void testWrap_WithWrapStringAndBreakLongWords() {
        String result = WordUtils.wrap("HelloWorld", 5, "\n", true);
        assertEquals("Custom wrap string with 'break' true failed",
                "Hello\nWorld", result);
    }

    @Test
    public void testWrap_EmptyWrapString() {
        String result = WordUtils.wrap("Hello World", 6, "", false);
        assertEquals("Empty wrap string should still break", "Hello\nWorld", result);
    }

    @Test
    public void testWrap_TrailingSpace() {
        String result = WordUtils.wrap(TRAILING_SPACE, 6);
        assertNotNull("Trailing space wrap should not return null", result);
        assertFalse("Result should not have trailing space",
                result.endsWith(" "));
    }

    @Test
    public void testWrap_LeadingSpace() {
        String result = WordUtils.wrap(LEADING_SPACE, 6);
        assertNotNull("Leading space wrap should not return null", result);
    }

    @Test
    public void testWrap_MultipleSpaces() {
        String result = WordUtils.wrap(MULTIPLE_SPACES, 10);
        assertNotNull("Multiple spaces wrap should not return null", result);
    }

    @Test
    public void testWrap_NegativeLength() {
        assertEquals("Negative length with break=false wraps at word boundary",
                "Hello", WordUtils.wrap("Hello World", -1, "\n", false));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testWrap_NegativeLengthWithBreakLongWords() {
        // This is the bug trigger: negative length with breakLongWords=true causes StringIndexOutOfBoundsException
        WordUtils.wrap("Hello World", -1, "\n", true);
    }

    @Test
    public void testWrap_ZeroLength() {
        assertEquals("Zero length with break=false", "Hello", WordUtils.wrap("Hello World", 0, "\n", false));
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testWrap_ZeroLengthWithBreakLongWords() {
        // Zero length with breakLongWords=true triggers the same bug
        WordUtils.wrap("Hello World", 0, "\n", true);
    }

    @Test
    public void testWrap_NullWrapString() {
        String result = WordUtils.wrap("Hello World", 6, null, false);
        assertEquals("Null wrap string should default to newline", "Hello\nWorld", result);
    }

    @Test
    public void testWrap_SingleCharacterLength() {
        String result = WordUtils.wrap("Hello", 1, "\n", true);
        assertNotNull("Single character length wrap", result);
        assertTrue("Each character should be on its own line",
                result.length() >= 5);
    }

    @Test
    public void testWrap_SpecialChars() {
        String result = WordUtils.wrap(SPECIAL_CHARS, 6);
        assertNotNull("Special chars wrap should not return null", result);
    }

    @Test
    public void testWrap_NewlineInInput() {
        String result = WordUtils.wrap(NEWLINE_STRING, 10);
        assertNotNull("Newline in input wrap", result);
        assertTrue("Result should contain newline", result.contains("\n"));
    }

    @Test
    public void testWrap_TabInInput() {
        String result = WordUtils.wrap(TAB_STRING, 10);
        assertNotNull("Tab in input wrap", result);
    }

    @Test
    public void testWrap_NonBreakableLongWordWithBreakFalse() {
        String result = WordUtils.wrap("Supercalifragilisticexpialidocious", 5, "\n", false);
        // Since breakLongWords is false, the long word should not be broken
        assertEquals("Long word not broken when breakLongWords is false",
                "Supercalifragilisticexpialidocious", result);
    }

    @Test
    public void testWrap_ExactlyAtWordLength() {
        String result = WordUtils.wrap("Hello World", 11);
        assertEquals("Wrap at exactly word boundary", "Hello World", result);
    }

    @Test
    public void testCapitalize_NullInput() {
        assertNull(WordUtils.capitalize(null));
    }

    @Test
    public void testCapitalize_EmptyString() {
        assertEquals("", WordUtils.capitalize(""));
    }

    @Test
    public void testCapitalize_SingleWord() {
        assertEquals("Hello", WordUtils.capitalize("hello"));
    }

    @Test
    public void testCapitalize_MultipleWords() {
        assertEquals("Hello World", WordUtils.capitalize("hello world"));
    }

    @Test
    public void testCapitalize_AlreadyCapitalized() {
        assertEquals("Hello", WordUtils.capitalize("Hello"));
    }

    @Test
    public void testCapitalize_WithLeadingSpaces() {
        assertEquals(" Hello World", WordUtils.capitalize(" hello world"));
    }

    @Test
    public void testCapitalize_WithTrailingSpaces() {
        assertEquals("Hello World ", WordUtils.capitalize("hello world "));
    }

    @Test
    public void testCapitalize_SpecialChars() {
        assertEquals("Hello World", WordUtils.capitalize("hello world"));
    }

    @Test
    public void testCapitalize_AllCaps() {
        assertEquals("HELLO WORLD", WordUtils.capitalize("HELLO WORLD"));
    }

    @Test
    public void testCapitalize_MixedCase() {
        assertEquals("Hello World", WordUtils.capitalize("hELLO wORLD"));
    }

    @Test
    public void testCapitalize_WithNumbers() {
        assertEquals("Hello123 World", WordUtils.capitalize("hello123 world"));
    }

    @Test
    public void testCapitalize_NullDelimiters() {
        assertNull(WordUtils.capitalize(null, null));
    }

    @Test
    public void testCapitalize_WithCustomDelimiters() {
        assertEquals("Hello-world_test", WordUtils.capitalize("hello-world_test", new char[]{'-', '_'}));
    }

    @Test
    public void testCapitalize_WithEmptyDelimiters() {
        assertEquals("hello world", WordUtils.capitalize("hello world", new char[]{}));
    }

    @Test
    public void testCapitalize_WithMultipleCustomDelimiters() {
        assertEquals("Hello:world;test", WordUtils.capitalize("hello:world;test", new char[]{':', ';'}));
    }

    @Test
    public void testCapitalizeFully_NullInput() {
        assertNull(WordUtils.capitalizeFully(null));
    }

    @Test
    public void testCapitalizeFully_EmptyString() {
        assertEquals("", WordUtils.capitalizeFully(""));
    }

    @Test
    public void testCapitalizeFully_SingleWord() {
        assertEquals("Hello", WordUtils.capitalizeFully("hello"));
    }

    @Test
    public void testCapitalizeFully_MultipleWords() {
        assertEquals("Hello World", WordUtils.capitalizeFully("HELLO WORLD"));
    }

    @Test
    public void testCapitalizeFully_MixedCase() {
        assertEquals("Hello World", WordUtils.capitalizeFully("hELLO wORLD"));
    }

    @Test
    public void testCapitalizeFully_WithCustomDelimiters() {
        assertEquals("Hello-world_test", WordUtils.capitalizeFully("HELLO-WORLD_TEST", new char[]{'-', '_'}));
    }

    @Test
    public void testCapitalizeFully_WithNumbers() {
        assertEquals("Hello123 World", WordUtils.capitalizeFully("hello123 world"));
    }

    @Test
    public void testUncapitalize_NullInput() {
        assertNull(WordUtils.uncapitalize(null));
    }

    @Test
    public void testUncapitalize_EmptyString() {
        assertEquals("", WordUtils.uncapitalize(""));
    }

    @Test
    public void testUncapitalize_SingleWord() {
        assertEquals("hello", WordUtils.uncapitalize("Hello"));
    }

    @Test
    public void testUncapitalize_MultipleWords() {
        assertEquals("hello world", WordUtils.uncapitalize("Hello World"));
    }

    @Test
    public void testUncapitalize_AllLowerCase() {
        assertEquals("hello", WordUtils.uncapitalize("hello"));
    }

    @Test
    public void testUncapitalize_WithCustomDelimiters() {
        assertEquals("hello-world_test", WordUtils.uncapitalize("Hello-World_Test", new char[]{'-', '_'}));
    }

    @Test
    public void testSwapCase_NullInput() {
        assertNull(WordUtils.swapCase(null));
    }

    @Test
    public void testSwapCase_EmptyString() {
        assertEquals("", WordUtils.swapCase(""));
    }

    @Test
    public void testSwapCase_AllLowerCase() {
        assertEquals("HELLO", WordUtils.swapCase("hello"));
    }

    @Test
    public void testSwapCase_AllUpperCase() {
        assertEquals("hello", WordUtils.swapCase("HELLO"));
    }

    @Test
    public void testSwapCase_MixedCase() {
        assertEquals("hELLO wORLD", WordUtils.swapCase("Hello World"));
    }

    @Test
    public void testSwapCase_WithNumbersAndSymbols() {
        assertEquals("hELLO123 wORLD!", WordUtils.swapCase("Hello123 World!"));
    }

    @Test
    public void testSwapCase_WithLeadingSpaces() {
        assertEquals(" hELLO", WordUtils.swapCase(" Hello"));
    }

    @Test
    public void testAbbreviate_NullInput() {
        assertNull(WordUtils.abbreviate(null, 10, 20, ""));
    }

    @Test
    public void testAbbreviate_EmptyString() {
        assertEquals("", WordUtils.abbreviate("", 10, 20, ""));
    }

    @Test
    public void testAbbreviate_BasicFunctionality() {
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", 5, 15, ""));
    }

    @Test
    public void testAbbreviate_WithDefaultAbbrevMarker() {
        assertEquals("Hello...", WordUtils.abbreviate("Hello World", 5, 8, "..."));
    }

    @Test
    public void testAbbreviate_ShortStringNoAbbreviation() {
        assertEquals("Hello", WordUtils.abbreviate("Hello", 0, 10, "..."));
    }

    @Test
    public void testAbbreviate_LowerGreaterThanUpper() {
        assertEquals("Hello", WordUtils.abbreviate("Hello", 10, 5, "..."));
    }

    @Test
    public void testAbbreviate_ExactMatchNoAbbreviation() {
        assertEquals("Hello", WordUtils.abbreviate("Hello", 0, 5, "..."));
    }

    @Test
    public void testAbbreviate_WithNullAbbrevMarker() {
        assertEquals("Hello", WordUtils.abbreviate("Hello", 0, 10, null));
    }

    @Test
    public void testAbbreviate_WordBoundaryAbbreviation() {
        assertEquals("Hello...", WordUtils.abbreviate("Hello World", 0, 10, "..."));
    }

    @Test
    public void testAbbreviate_AllInLowerCase() {
        assertEquals("hello...", WordUtils.abbreviate("hello world", 0, 8, "..."));
    }

    @Test
    public void testAbbreviate_WithMultipleWords() {
        assertEquals("one two three...", WordUtils.abbreviate("one two three four", 0, 20, "..."));
    }

    @Test
    public void testAbbreviate_JustAtBoundaryNoAbbrev() {
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", 0, 11, "..."));
    }

    @Test
    public void testAbbreviate_LargeUpper() {
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", 0, 100, "..."));
    }

    // This test specifically triggers the Defects4J bug (StringIndexOutOfBoundsException)
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAbbreviate_TriggerBugIndexOutOfBounds() {
        // Input that causes the bug: lower > length and upper < lower
        // This triggers the path where str.substring(0, upper) is called with invalid index
        WordUtils.abbreviate("Hello", 10, 15, "...");
    }

    @Test(expected = StringIndexOutOfBoundsException.class)
    public void testAbbreviate_TriggerBugWithLongString() {
        // Another variant that triggers the bug
        WordUtils.abbreviate("Hello World Java Test", 20, 25, "...");
    }

    @Test
    public void testAbbreviate_NegativeLower() {
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", -1, 20, "..."));
    }

    @Test
    public void testAbbreviate_NegativeUpper() {
        assertEquals("Hello", WordUtils.abbreviate("Hello World", 0, -1, "..."));
    }

    @Test
    public void testAbbreviate_ZeroUpper() {
        assertEquals("...", WordUtils.abbreviate("Hello World", 0, 0, "..."));
    }

    @Test
    public void testAbbreviate_LargeNegativeValues() {
        assertEquals("...", WordUtils.abbreviate("Hello World", -10, -5, "..."));
    }

    @Test
    public void testAbbreviate_TwoWordString() {
        assertEquals("Hello...", WordUtils.abbreviate("Hello World", 0, 7, "..."));
    }

    @Test
    public void testAbbreviate_EmptyAbbrevMarker() {
        assertEquals("Hello World", WordUtils.abbreviate("Hello World", 0, 20, ""));
    }

    @Test
    public void testAbbreviate_AbbrevMarkerLengthGreaterThanUpper() {
        assertEquals("......", WordUtils.abbreviate("Hello", 0, 6, "......"));
    }

    @Test
    public void testAbbreviate_MultiWordWithBoundary() {
        assertEquals("one two...", WordUtils.abbreviate("one two three", 0, 12, "..."));
    }

    @Test
    public void testContainsAllWords_NullInput() {
        assertFalse(WordUtils.containsAllWords(null, "test"));
    }

    @Test
    public void testContainsAllWords_NullWords() {
        assertFalse(WordUtils.containsAllWords("test", (String) null));
    }

    @Test
    public void testContainsAllWords_EmptyString() {
        assertFalse(WordUtils.containsAllWords("", "test"));
    }

    @Test
    public void testContainsAllWords_EmptyWords() {
        assertFalse(WordUtils.containsAllWords("test", ""));
    }

    @Test
    public void testContainsAllWords_SingleWord() {
        assertTrue(WordUtils.containsAllWords("Hello World", "Hello"));
    }

    @Test
    public void testContainsAllWords_MultipleWords() {
        assertTrue(WordUtils.containsAllWords("Hello World Java", "Hello", "Java"));
    }

    @Test
    public void testContainsAllWords_WordNotFound() {
        assertFalse(WordUtils.containsAllWords("Hello World", "Java"));
    }

    @Test
    public void testContainsAllWords_PartialWord() {
        assertFalse(WordUtils.containsAllWords("Hello World", "Hel"));
    }

    @Test
    public void testContainsAllWords_WithExtraSpaces() {
        assertTrue(WordUtils.containsAllWords("Hello   World", "Hello", "World"));
    }

    @Test
    public void testContainsAllWords_CaseSensitive() {
        assertFalse(WordUtils.containsAllWords("Hello World", "hello"));
    }

    @Test
    public void testContainsAllWords_EmptyWordInList() {
        assertFalse(WordUtils.containsAllWords("Hello World", "Hello", ""));
    }

    @Test
    public void testInitials_NullInput() {
        assertNull(WordUtils.initials(null));
    }

    @Test
    public void testInitials_EmptyString() {
        assertEquals("", WordUtils.initials(""));
    }

    @Test
    public void testInitials_Basic() {
        assertEquals("HW", WordUtils.initials("Hello World"));
    }

    @Test
    public void testInitials_SingleWord() {
        assertEquals("H", WordUtils.initials("Hello"));
    }

    @Test
    public void testInitials_WithMultipleSpaces() {
        assertEquals("HW", WordUtils.initials("Hello   World"));
    }

    @Test
    public void testInitials_WithLeadingSpaces() {
        assertEquals("H", WordUtils.initials(" Hello"));
    }

    @Test
    public void testInitials_WithCustomDelimiters() {
        assertEquals("HW", WordUtils.initials("Hello World", new char[]{' '}));
    }

    @Test
    public void testInitials_NullDelimiters() {
        assertNull(WordUtils.initials(null, null));
    }

    @Test
    public void testInitials_EmptyDelimiters() {
        assertEquals("", WordUtils.initials("Hello World", new char[]{}));
    }

    @Test
    public void testInitials_WithCustomDelimitersMultiple() {
        assertEquals("HWT", WordUtils.initials("Hello World Test", new char[]{' '}));
    }

    @Test
    public void testInitials_AllLowerCase() {
        assertEquals("hw", WordUtils.initials("hello world"));
    }

    @Test
    public void testInitials_MixedCase() {
        assertEquals("hW", WordUtils.initials("hello World"));
    }

    @Test
    public void testInitials_WithNumbers() {
        assertEquals("H1W", WordUtils.initials("Hello1 World"));
    }

    @Test
    public void testInitials_NullInputWithCustomDelimiters() {
        assertNull(WordUtils.initials(null, new char[]{' '}));
    }

    @Test
    public void testInitials_EmptyStringWithCustomDelimiters() {
        assertEquals("", WordUtils.initials("", new char[]{' '}));
    }
}