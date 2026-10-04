package org.apache.commons.lang3.text.translate;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

public class LookupTranslatorTest {

    private LookupTranslator translator;
    private LookupTranslator emptyTranslator;
    private LookupTranslator singleCharTranslator;
    private LookupTranslator multiCharTranslator;
    private LookupTranslator overlappingTranslator;

    @Before
    public void setUp() {
        // Empty lookup
        emptyTranslator = new LookupTranslator(new String[][]{});

        // Single character lookup
        singleCharTranslator = new LookupTranslator(new String[][]{
            {"<", "&lt;"},
            {">", "&gt;"},
            {"&", "&amp;"}
        });

        // Multi-character lookup
        multiCharTranslator = new LookupTranslator(new String[][]{
            {"abc", "xyz"},
            {"def", "uvw"},
            {"ghi", "rst"}
        });

        // Overlapping lookups (potential bug trigger)
        overlappingTranslator = new LookupTranslator(new String[][]{
            {"ab", "xy"},
            {"abc", "123"},
            {"abcd", "wxyz"}
        });

        // General purpose translator
        translator = new LookupTranslator(new String[][]{
            {"hello", "world"},
            {"foo", "bar"},
            {"test", "case"}
        });
    }

    @Test
    public void testNullInput() {
        try {
            translator.translate(null, 0, null);
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testNullInputWithWriter() {
        try {
            translator.translate(null, 0, new java.io.StringWriter());
            fail("Expected IllegalArgumentException for null input");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testNegativeIndex() {
        try {
            translator.translate("hello", -1, new java.io.StringWriter());
            fail("Expected StringIndexOutOfBoundsException for negative index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testIndexOutOfBounds() {
        try {
            translator.translate("hello", 10, new java.io.StringWriter());
            fail("Expected StringIndexOutOfBoundsException for out-of-bounds index");
        } catch (StringIndexOutOfBoundsException e) {
            // Expected
        }
    }

    @Test
    public void testEmptyInput() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testEmptyLookup() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = emptyTranslator.translate("test", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testSingleCharTranslation() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = singleCharTranslator.translate("<test>", 0, writer);
        assertEquals(1, result);
        assertEquals("&lt;", writer.toString());
    }

    @Test
    public void testMultiCharTranslation() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = multiCharTranslator.translate("abcdef", 0, writer);
        assertEquals(3, result);
        assertEquals("xyz", writer.toString());
    }

    @Test
    public void testNoMatchAtPosition() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("xyz", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testPartialMatchAtEnd() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("hello", 3, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testOverlappingLookupShortestMatch() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = overlappingTranslator.translate("abcd", 0, writer);
        // Should match "ab" first (shortest), not "abc" or "abcd"
        assertEquals(2, result);
        assertEquals("xy", writer.toString());
    }

    @Test
    public void testOverlappingLookupLongerMatch() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = overlappingTranslator.translate("abc", 0, writer);
        // Should match "ab" (shortest)
        assertEquals(2, result);
        assertEquals("xy", writer.toString());
    }

    @Test
    public void testMultipleSequentialTranslations() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result1 = translator.translate("hello foo", 0, writer);
        assertEquals(5, result1);
        assertEquals("world", writer.toString());

        int result2 = translator.translate("hello foo", 6, writer);
        assertEquals(3, result2);
        assertEquals("worldbar", writer.toString());
    }

    @Test
    public void testTranslationWithSpecialCharacters() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = singleCharTranslator.translate("a & b", 2, writer);
        assertEquals(1, result);
        assertEquals("&amp;", writer.toString());
    }

    @Test
    public void testTranslationAtEndOfString() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("test", 0, writer);
        assertEquals(4, result);
        assertEquals("case", writer.toString());
    }

    @Test
    public void testTranslationWithIndexInMiddle() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("footest", 3, writer);
        assertEquals(4, result);
        assertEquals("case", writer.toString());
    }

    @Test
    public void testNoTranslationForNonMatching() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("xyzabc", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testSequentialNonMatchingThenMatching() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result1 = translator.translate("xyzhello", 0, writer);
        assertEquals(0, result1);
        assertEquals("", writer.toString());

        int result2 = translator.translate("xyzhello", 3, writer);
        assertEquals(5, result2);
        assertEquals("world", writer.toString());
    }

    @Test
    public void testLookupWithEmptyKey() {
        LookupTranslator emptyKeyTranslator = new LookupTranslator(new String[][]{
            {"", "empty"}
        });
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = emptyKeyTranslator.translate("test", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testLookupWithEmptyValue() {
        LookupTranslator emptyValueTranslator = new LookupTranslator(new String[][]{
            {"key", ""}
        });
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = emptyValueTranslator.translate("key", 0, writer);
        assertEquals(3, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testMultipleLookupsSamePrefix() {
        LookupTranslator prefixTranslator = new LookupTranslator(new String[][]{
            {"a", "1"},
            {"ab", "2"},
            {"abc", "3"}
        });
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = prefixTranslator.translate("abc", 0, writer);
        // Should match "a" (shortest)
        assertEquals(1, result);
        assertEquals("1", writer.toString());
    }

    @Test
    public void testLongestLookupFirst() {
        LookupTranslator longestFirstTranslator = new LookupTranslator(new String[][]{
            {"abc", "3"},
            {"ab", "2"},
            {"a", "1"}
        });
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = longestFirstTranslator.translate("abc", 0, writer);
        // Should match "a" (shortest) regardless of insertion order
        assertEquals(1, result);
        assertEquals("1", writer.toString());
    }

    @Test
    public void testWriterContentAfterMultipleTranslations() {
        java.io.StringWriter writer = new java.io.StringWriter();
        translator.translate("hello", 0, writer);
        translator.translate(" foo", 0, writer);
        assertEquals("world", writer.toString());
    }

    @Test
    public void testIndexAtEndOfString() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("hello", 5, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testNullWriter() {
        try {
            translator.translate("test", 0, null);
            fail("Expected NullPointerException for null writer");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test
    public void testLargeInputString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("hello");
        }
        String largeInput = sb.toString();
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate(largeInput, 0, writer);
        assertEquals(5, result);
        assertEquals("world", writer.toString());
    }

    @Test
    public void testSequentialLookupsWithSameStart() {
        LookupTranslator seqTranslator = new LookupTranslator(new String[][]{
            {"aa", "bb"},
            {"aaa", "ccc"}
        });
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = seqTranslator.translate("aaa", 0, writer);
        // Should match "aa" (shortest)
        assertEquals(2, result);
        assertEquals("bb", writer.toString());
    }

    @Test
    public void testTranslationWithUnicodeCharacters() {
        LookupTranslator unicodeTranslator = new LookupTranslator(new String[][]{
            {"\u00E9", "e"},
            {"\u00FC", "ue"}
        });
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = unicodeTranslator.translate("\u00E9test", 0, writer);
        assertEquals(1, result);
        assertEquals("e", writer.toString());
    }

    @Test
    public void testTranslationWithWhitespace() {
        LookupTranslator whitespaceTranslator = new LookupTranslator(new String[][]{
            {" ", "&nbsp;"},
            {"\t", "&tab;"}
        });
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = whitespaceTranslator.translate(" ", 0, writer);
        assertEquals(1, result);
        assertEquals("&nbsp;", writer.toString());
    }

    @Test
    public void testNoTranslationForSingleCharacter() {
        java.io.StringWriter writer = new java.io.StringWriter();
        int result = translator.translate("a", 0, writer);
        assertEquals(0, result);
        assertEquals("", writer.toString());
    }

    @Test
    public void testTranslationWithMultipleMatchesInString() {
        java.io.StringWriter writer = new java.io.StringWriter();
        translator.translate("hello", 0, writer);
        translator.translate("test", 0, writer);
        assertEquals("worldcase", writer.toString());
    }
}