package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {
    private CharacterReader reader;

    @Before
    public void setUp() {
        // Default empty reader for tests that need a fresh instance
        reader = new CharacterReader("");
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testConstructorWithNullInput() {
        // Expect NullPointerException as per typical behavior
        try {
            new CharacterReader(null);
            fail("Expected NullPointerException for null input");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithEmptyString() {
        CharacterReader r = new CharacterReader("");
        assertTrue("Should be empty", r.isEmpty());
        assertEquals("Current char should be -1", -1, r.current());
    }

    @Test
    public void testConstructorWithNonEmptyString() {
        CharacterReader r = new CharacterReader("abc");
        assertFalse("Should not be empty", r.isEmpty());
        assertEquals("Current char should be 'a'", 'a', r.current());
    }

    // ==================== isEmpty() Tests ====================

    @Test
    public void testIsEmptyInitially() {
        assertTrue("Empty reader should be empty", reader.isEmpty());
    }

    @Test
    public void testIsEmptyAfterConsumeAll() {
        reader = new CharacterReader("x");
        assertFalse("Before consume, not empty", reader.isEmpty());
        reader.consume();
        assertTrue("After consume, empty", reader.isEmpty());
    }

    @Test
    public void testIsEmptyAfterConsumeToEnd() {
        reader = new CharacterReader("hello");
        reader.consumeToEnd();
        assertTrue("After consumeToEnd, empty", reader.isEmpty());
    }

    // ==================== current() Tests ====================

    @Test
    public void testCurrentOnEmptyReader() {
        assertEquals("Current on empty should be -1", -1, reader.current());
    }

    @Test
    public void testCurrentAfterConsume() {
        reader = new CharacterReader("ab");
        assertEquals("Initial current", 'a', reader.current());
        reader.consume();
        assertEquals("After consume", 'b', reader.current());
        reader.consume();
        assertEquals("After second consume", -1, reader.current());
    }

    // ==================== consume() Tests ====================

    @Test
    public void testConsumeSingleChar() {
        reader = new CharacterReader("a");
        assertEquals("Consumed char should be 'a'", 'a', reader.consume());
        assertTrue("Reader should be empty", reader.isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testConsumeOnEmptyReader() {
        reader.consume();
    }

    // ==================== unconsume() Tests ====================

    @Test
    public void testUnconsume() {
        reader = new CharacterReader("abc");
        reader.consume(); // pos=1
        reader.unconsume(); // pos=0
        assertEquals("After unconsume, current should be 'a'", 'a', reader.current());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUnconsumeAtStart() {
        reader.unconsume(); // pos=0, cannot go back
    }

    // ==================== mark() / rewindToMark() Tests ====================

    @Test
    public void testMarkAndRewind() {
        reader = new CharacterReader("abcdef");
        reader.consume(); // 'a'
        reader.consume(); // 'b'
        reader.mark(); // mark at pos=2
        reader.consume(); // 'c'
        reader.consume(); // 'd'
        reader.rewindToMark(); // back to pos=2
        assertEquals("After rewind, current should be 'c'", 'c', reader.current());
    }

    @Test
    public void testRewindToMarkWithoutMark() {
        reader = new CharacterReader("abc");
        reader.consume();
        // No mark set, rewindToMark should do nothing? Actually it sets pos to mark which is 0 by default.
        reader.rewindToMark();
        assertEquals("After rewind without mark, should go to start", 'a', reader.current());
    }

    // ==================== consumeTo(char) Tests ====================

    @Test
    public void testConsumeToCharFound() {
        reader = new CharacterReader("a,b,c");
        String consumed = reader.consumeTo(',');
        assertEquals("Should consume up to comma", "a", consumed);
        assertEquals("After consumeTo, current should be comma", ',', reader.current());
    }

    @Test
    public void testConsumeToCharNotFound() {
        reader = new CharacterReader("hello");
        String consumed = reader.consumeTo('x');
        assertEquals("Should consume entire string", "hello", consumed);
        assertTrue("Reader should be empty", reader.isEmpty());
    }

    @Test
    public void testConsumeToCharAtStart() {
        reader = new CharacterReader(",a");
        String consumed = reader.consumeTo(',');
        assertEquals("Should consume empty string", "", consumed);
        assertEquals("Current should be comma", ',', reader.current());
    }

    @Test
    public void testConsumeToCharOnEmptyReader() {
        String consumed = reader.consumeTo('a');
        assertEquals("Should return empty string", "", consumed);
    }

    // ==================== consumeTo(String) Tests ====================
    // These are critical for bug 72

    @Test
    public void testConsumeToStringFound() {
        reader = new CharacterReader("abcd");
        String consumed = reader.consumeTo("cd");
        assertEquals("Should consume up to 'cd'", "ab", consumed);
        assertEquals("Current should be 'c'", 'c', reader.current());
    }

    @Test
    public void testConsumeToStringNotFound() {
        reader = new CharacterReader("abc");
        String consumed = reader.consumeTo("xyz");
        assertEquals("Should consume entire string", "abc", consumed);
        assertTrue("Reader should be empty", reader.isEmpty());
    }

    @Test
    public void testConsumeToStringLongerThanRemaining() {
        // This triggers the bug: sequence longer than remaining input
        reader = new CharacterReader("ab");
        String consumed = reader.consumeTo("abc");
        assertEquals("Should consume entire remaining", "ab", consumed);
        assertTrue("Reader should be empty", reader.isEmpty());
    }

    @Test
    public void testConsumeToStringAtStart() {
        reader = new CharacterReader("abc");
        String consumed = reader.consumeTo("ab");
        assertEquals("Should consume empty string", "", consumed);
        assertEquals("Current should be 'a'", 'a', reader.current());
    }

    @Test
    public void testConsumeToStringOnEmptyReader() {
        String consumed = reader.consumeTo("a");
        assertEquals("Should return empty string", "", consumed);
    }

    // ==================== consumeToAny(char...) Tests ====================

    @Test
    public void testConsumeToAnyFound() {
        reader = new CharacterReader("hello world");
        String consumed = reader.consumeToAny(' ', 'o');
        // Should stop at first match: 'o' at index 4? Actually 'h','e','l','l','o' -> stop at 'o'
        assertEquals("Should consume up to first matching char", "hell", consumed);
        assertEquals("Current should be 'o'", 'o', reader.current());
    }

    @Test
    public void testConsumeToAnyNotFound() {
        reader = new CharacterReader("abc");
        String consumed = reader.consumeToAny('x', 'y');
        assertEquals("Should consume entire string", "abc", consumed);
        assertTrue("Reader should be empty", reader.isEmpty());
    }

    @Test
    public void testConsumeToAnyOnEmptyReader() {
        String consumed = reader.consumeToAny('a', 'b');
        assertEquals("Should return empty string", "", consumed);
    }

    // ==================== consumeToEnd() Tests ====================

    @Test
    public void testConsumeToEnd() {
        reader = new CharacterReader("test");
        String consumed = reader.consumeToEnd();
        assertEquals("Should consume entire string", "test", consumed);
        assertTrue("Reader should be empty", reader.isEmpty());
    }

    @Test
    public void testConsumeToEndOnEmptyReader() {
        String consumed = reader.consumeToEnd();
        assertEquals("Should return empty string", "", consumed);
    }

    // ==================== matches(String) Tests ====================

    @Test
    public void testMatchesStringExact() {
        reader = new CharacterReader("abc");
        assertTrue("Should match 'abc'", reader.matches("abc"));
    }

    @Test
    public void testMatchesStringPartial() {
        reader = new CharacterReader("abcd");
        assertTrue("Should match 'abc'", reader.matches("abc"));
    }

    @Test
    public void testMatchesStringNoMatch() {
        reader = new CharacterReader("abc");
        assertFalse("Should not match 'abx'", reader.matches("abx"));
    }

    @Test
    public void testMatchesStringLongerThanRemaining() {
        // This triggers the bug if not guarded
        reader = new CharacterReader("ab");
        assertFalse("Should not match longer string", reader.matches("abc"));
    }

    @Test
    public void testMatchesStringOnEmptyReader() {
        assertFalse("Empty reader should not match any string", reader.matches("a"));
    }

    // ==================== matches(char) Tests ====================

    @Test
    public void testMatchesChar() {
        reader = new CharacterReader("a");
        assertTrue("Should match 'a'", reader.matches('a'));
    }

    @Test
    public void testMatchesCharNoMatch() {
        reader = new CharacterReader("a");
        assertFalse("Should not match 'b'", reader.matches('b'));
    }

    @Test
    public void testMatchesCharOnEmptyReader() {
        assertFalse("Empty reader should not match any char", reader.matches('a'));
    }

    // ==================== matchesIgnoreCase(String) Tests ====================

    @Test
    public void testMatchesIgnoreCase() {
        reader = new CharacterReader("AbC");
        assertTrue("Should match ignoring case", reader.matchesIgnoreCase("abc"));
    }

    @Test
    public void testMatchesIgnoreCaseNoMatch() {
        reader = new CharacterReader("abc");
        assertFalse("Should not match different string", reader.matchesIgnoreCase("abd"));
    }

    @Test
    public void testMatchesIgnoreCaseLongerThanRemaining() {
        reader = new CharacterReader("ab");
        assertFalse("Should not match longer string", reader.matchesIgnoreCase("abc"));
    }

    // ==================== matchAny(char...) Tests ====================

    @Test
    public void testMatchAnyFound() {
        reader = new CharacterReader("hello");
        assertTrue("Should match one of the chars", reader.matchAny('h', 'e', 'l'));
    }

    @Test
    public void testMatchAnyNotFound() {
        reader = new CharacterReader("hello");
        assertFalse("Should not match any", reader.matchAny('x', 'y', 'z'));
    }

    @Test
    public void testMatchAnyOnEmptyReader() {
        assertFalse("Empty reader should not match any", reader.matchAny('a'));
    }

    // ==================== containsIgnoreCase(String) Tests ====================

    @Test
    public void testContainsIgnoreCaseFound() {
        reader = new CharacterReader("Hello World");
        assertTrue("Should contain 'world' ignoring case", reader.containsIgnoreCase("world"));
    }

    @Test
    public void testContainsIgnoreCaseNotFound() {
        reader = new CharacterReader("Hello");
        assertFalse("Should not contain 'xyz'", reader.containsIgnoreCase("xyz"));
    }

    @Test
    public void testContainsIgnoreCaseOnEmptyReader() {
        assertFalse("Empty reader should not contain anything", reader.containsIgnoreCase("a"));
    }

    // ==================== Additional Edge Cases ====================

    @Test
    public void testConsumeToCharWithMultipleOccurrences() {
        reader = new CharacterReader("a,b,c,d");
        String first = reader.consumeTo(',');
        assertEquals("First segment", "a", first);
        reader.consume(); // consume the comma
        String second = reader.consumeTo(',');
        assertEquals("Second segment", "b", second);
    }

    @Test
    public void testConsumeToStringWithMultipleOccurrences() {
        reader = new CharacterReader("abcabc");
        String first = reader.consumeTo("abc");
        assertEquals("First segment", "", first);
        reader.consume(); // consume 'a'? Actually after consumeTo, pos is at start of match, so we need to consume the sequence
        // Better: after consumeTo("abc"), pos is at 'a', so we can consume the sequence
        reader.consume(); // 'a'
        reader.consume(); // 'b'
        reader.consume(); // 'c'
        String second = reader.consumeTo("abc");
        assertEquals("Second segment", "", second);
    }

    @Test
    public void testUnconsumeAfterMark() {
        reader = new CharacterReader("abc");
        reader.consume(); // 'a'
        reader.mark(); // mark at pos=1
        reader.consume(); // 'b'
        reader.unconsume(); // back to pos=1
        assertEquals("After unconsume, current should be 'b'", 'b', reader.current());
        reader.rewindToMark(); // back to mark pos=1
        assertEquals("After rewind, current should be 'b'", 'b', reader.current());
    }

    @Test
    public void testConsumeToEndAfterPartialConsume() {
        reader = new CharacterReader("12345");
        reader.consume(); // '1'
        reader.consume(); // '2'
        String rest = reader.consumeToEnd();
        assertEquals("Remaining should be '345'", "345", rest);
    }

    @Test
    public void testMatchesStringWithSpecialCharacters() {
        reader = new CharacterReader("test\n");
        assertTrue("Should match 'test\\n'", reader.matches("test\n"));
    }

    @Test
    public void testConsumeToCharWithSpecialCharacters() {
        reader = new CharacterReader("line1\nline2");
        String first = reader.consumeTo('\n');
        assertEquals("First line", "line1", first);
        reader.consume(); // consume newline
        assertEquals("After newline, current should be 'l'", 'l', reader.current());
    }

    @Test
    public void testMultipleOperations() {
        reader = new CharacterReader("a,b.c;d");
        assertEquals("a", reader.consumeTo(','));
        reader.consume(); // ','
        assertEquals("b", reader.consumeTo('.'));
        reader.consume(); // '.'
        assertEquals("c", reader.consumeTo(';'));
        reader.consume(); // ';'
        assertEquals("d", reader.consumeToEnd());
    }

    @Test
    public void testConsumeToAnyWithEmptyCharArray() {
        // Edge case: empty char array should not match anything
        reader = new CharacterReader("abc");
        String consumed = reader.consumeToAny();
        assertEquals("Should consume entire string", "abc", consumed);
        assertTrue("Reader should be empty", reader.isEmpty());
    }

    @Test
    public void testMatchAnyWithEmptyCharArray() {
        reader = new CharacterReader("a");
        assertFalse("Empty char array should not match", reader.matchAny());
    }

    @Test
    public void testConsumeToStringWithEmptyString() {
        reader = new CharacterReader("abc");
        // Empty sequence should match immediately
        String consumed = reader.consumeTo("");
        assertEquals("Should consume empty string", "", consumed);
        assertEquals("Current should be 'a'", 'a', reader.current());
    }

    @Test
    public void testMatchesStringWithEmptyString() {
        reader = new CharacterReader("abc");
        assertTrue("Empty string should always match", reader.matches(""));
    }

    @Test
    public void testContainsIgnoreCaseWithEmptyString() {
        reader = new CharacterReader("abc");
        assertTrue("Empty string should be contained", reader.containsIgnoreCase(""));
    }
}