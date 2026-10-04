package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    // ==================== Constructor and isEmpty ====================

    @Test
    public void testConstructorWithNullString() {
        try {
            new CharacterReader(null);
            fail("Expected NullPointerException for null input");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testConstructorWithEmptyString() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
    }

    @Test
    public void testConstructorWithNonEmptyString() {
        CharacterReader reader = new CharacterReader("hello");
        assertFalse(reader.isEmpty());
        assertEquals(0, reader.pos());
    }

    // ==================== pos() ====================

    @Test
    public void testPosInitial() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
    }

    @Test
    public void testPosAfterAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals(1, reader.pos());
    }

    // ==================== isEmpty() ====================

    @Test
    public void testIsEmptyAfterConsumingAll() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.isEmpty());
        reader.advance();
        assertTrue(reader.isEmpty());
    }

    // ==================== current() ====================

    @Test
    public void testCurrentAtStart() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.current());
    }

    @Test
    public void testCurrentAfterAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals('b', reader.current());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCurrentOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        reader.current(); // should throw
    }

    // ==================== advance() ====================

    @Test
    public void testAdvanceNormal() {
        CharacterReader reader = new CharacterReader("ab");
        reader.advance();
        assertEquals('b', reader.current());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAdvanceBeyondEnd() {
        CharacterReader reader = new CharacterReader("a");
        reader.advance();
        reader.advance(); // should throw
    }

    // ==================== consumeTo(char) ====================

    @Test
    public void testConsumeToCharFound() {
        CharacterReader reader = new CharacterReader("hello world");
        String consumed = reader.consumeTo(' ');
        assertEquals("hello", consumed);
        assertEquals(' ', reader.current());
    }

    @Test
    public void testConsumeToCharNotFound() {
        CharacterReader reader = new CharacterReader("hello");
        String consumed = reader.consumeTo('x');
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToCharAtStart() {
        CharacterReader reader = new CharacterReader("hello");
        String consumed = reader.consumeTo('h');
        assertEquals("", consumed);
        assertEquals('h', reader.current());
    }

    @Test
    public void testConsumeToCharOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeTo('a');
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeTo(String) ====================

    @Test
    public void testConsumeToStringFound() {
        CharacterReader reader = new CharacterReader("hello world");
        String consumed = reader.consumeTo("wor");
        assertEquals("hello ", consumed);
        assertEquals('w', reader.current());
    }

    @Test
    public void testConsumeToStringNotFound() {
        CharacterReader reader = new CharacterReader("hello");
        String consumed = reader.consumeTo("xyz");
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToStringEmptyTarget() {
        CharacterReader reader = new CharacterReader("hello");
        String consumed = reader.consumeTo("");
        assertEquals("", consumed);
        assertEquals('h', reader.current());
    }

    @Test
    public void testConsumeToStringOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeTo("a");
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeToEnd() ====================

    @Test
    public void testConsumeToEndNonEmpty() {
        CharacterReader reader = new CharacterReader("hello");
        String consumed = reader.consumeToEnd();
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToEndEmpty() {
        // This is the bug scenario: empty reader should not throw
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeToEnd();
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToEndAfterPartialConsume() {
        CharacterReader reader = new CharacterReader("hello world");
        reader.consumeTo(' ');
        String rest = reader.consumeToEnd();
        assertEquals("world", rest);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeToAny ====================

    @Test
    public void testConsumeToAnyFound() {
        CharacterReader reader = new CharacterReader("hello world");
        String consumed = reader.consumeToAny(' ', 'o');
        assertEquals("hell", consumed);
        assertEquals('o', reader.current());
    }

    @Test
    public void testConsumeToAnyNotFound() {
        CharacterReader reader = new CharacterReader("hello");
        String consumed = reader.consumeToAny('x', 'y');
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAnyEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeToAny('a');
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeToAnySorted ====================

    @Test
    public void testConsumeToAnySortedFound() {
        CharacterReader reader = new CharacterReader("hello world");
        char[] sorted = {' ', 'o'};
        String consumed = reader.consumeToAnySorted(sorted);
        assertEquals("hell", consumed);
        assertEquals('o', reader.current());
    }

    @Test
    public void testConsumeToAnySortedNotFound() {
        CharacterReader reader = new CharacterReader("hello");
        char[] sorted = {'x', 'y'};
        String consumed = reader.consumeToAnySorted(sorted);
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAnySortedEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        char[] sorted = {'a'};
        String consumed = reader.consumeToAnySorted(sorted);
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeLetterSequence ====================

    @Test
    public void testConsumeLetterSequenceNormal() {
        CharacterReader reader = new CharacterReader("abc123");
        String seq = reader.consumeLetterSequence();
        assertEquals("abc", seq);
        assertEquals('1', reader.current());
    }

    @Test
    public void testConsumeLetterSequenceNoLetters() {
        CharacterReader reader = new CharacterReader("123");
        String seq = reader.consumeLetterSequence();
        assertEquals("", seq);
        assertEquals('1', reader.current());
    }

    @Test
    public void testConsumeLetterSequenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        String seq = reader.consumeLetterSequence();
        assertEquals("", seq);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeLetterThenDigitSequence ====================

    @Test
    public void testConsumeLetterThenDigitSequenceNormal() {
        CharacterReader reader = new CharacterReader("abc123def");
        String seq = reader.consumeLetterThenDigitSequence();
        assertEquals("abc123", seq);
        assertEquals('d', reader.current());
    }

    @Test
    public void testConsumeLetterThenDigitSequenceOnlyLetters() {
        CharacterReader reader = new CharacterReader("abc");
        String seq = reader.consumeLetterThenDigitSequence();
        assertEquals("abc", seq);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeLetterThenDigitSequenceOnlyDigits() {
        CharacterReader reader = new CharacterReader("123");
        String seq = reader.consumeLetterThenDigitSequence();
        assertEquals("", seq);
        assertEquals('1', reader.current());
    }

    @Test
    public void testConsumeLetterThenDigitSequenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        String seq = reader.consumeLetterThenDigitSequence();
        assertEquals("", seq);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeHexSequence ====================

    @Test
    public void testConsumeHexSequenceNormal() {
        CharacterReader reader = new CharacterReader("1a2b3cxyz");
        String seq = reader.consumeHexSequence();
        assertEquals("1a2b3c", seq);
        assertEquals('x', reader.current());
    }

    @Test
    public void testConsumeHexSequenceNoHex() {
        CharacterReader reader = new CharacterReader("xyz");
        String seq = reader.consumeHexSequence();
        assertEquals("", seq);
        assertEquals('x', reader.current());
    }

    @Test
    public void testConsumeHexSequenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        String seq = reader.consumeHexSequence();
        assertEquals("", seq);
        assertTrue(reader.isEmpty());
    }

    // ==================== consumeDigitSequence ====================

    @Test
    public void testConsumeDigitSequenceNormal() {
        CharacterReader reader = new CharacterReader("123abc");
        String seq = reader.consumeDigitSequence();
        assertEquals("123", seq);
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeDigitSequenceNoDigits() {
        CharacterReader reader = new CharacterReader("abc");
        String seq = reader.consumeDigitSequence();
        assertEquals("", seq);
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeDigitSequenceEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        String seq = reader.consumeDigitSequence();
        assertEquals("", seq);
        assertTrue(reader.isEmpty());
    }

    // ==================== matches(char) ====================

    @Test
    public void testMatchesCharTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches('a'));
    }

    @Test
    public void testMatchesCharFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches('b'));
    }

    @Test
    public void testMatchesCharOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matches('a'));
    }

    // ==================== matches(String) ====================

    @Test
    public void testMatchesStringTrue() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matches("hello"));
    }

    @Test
    public void testMatchesStringFalse() {
        CharacterReader reader = new CharacterReader("hello");
        assertFalse(reader.matches("world"));
    }

    @Test
    public void testMatchesStringPartial() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matches("hel"));
    }

    @Test
    public void testMatchesStringEmpty() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matches(""));
    }

    @Test
    public void testMatchesStringOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matches("a"));
    }

    @Test
    public void testMatchesStringLongerThanReader() {
        CharacterReader reader = new CharacterReader("ab");
        assertFalse(reader.matches("abc"));
    }

    // ==================== matchesIgnoreCase(String) ====================

    @Test
    public void testMatchesIgnoreCaseTrue() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matchesIgnoreCase("hello"));
    }

    @Test
    public void testMatchesIgnoreCaseFalse() {
        CharacterReader reader = new CharacterReader("Hello");
        assertFalse(reader.matchesIgnoreCase("world"));
    }

    @Test
    public void testMatchesIgnoreCaseEmpty() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matchesIgnoreCase(""));
    }

    @Test
    public void testMatchesIgnoreCaseOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesIgnoreCase("a"));
    }

    // ==================== matchesAny ====================

    @Test
    public void testMatchesAnyTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchesAny('a', 'b'));
    }

    @Test
    public void testMatchesAnyFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matchesAny('d', 'e'));
    }

    @Test
    public void testMatchesAnyOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesAny('a'));
    }

    // ==================== matchesLetter ====================

    @Test
    public void testMatchesLetterTrue() {
        CharacterReader reader = new CharacterReader("a");
        assertTrue(reader.matchesLetter());
    }

    @Test
    public void testMatchesLetterFalseDigit() {
        CharacterReader reader = new CharacterReader("1");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesLetterFalseSymbol() {
        CharacterReader reader = new CharacterReader("@");
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesLetterOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesLetter());
    }

    // ==================== matchesDigit ====================

    @Test
    public void testMatchesDigitTrue() {
        CharacterReader reader = new CharacterReader("5");
        assertTrue(reader.matchesDigit());
    }

    @Test
    public void testMatchesDigitFalse() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testMatchesDigitOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchesDigit());
    }

    // ==================== matchConsume(String) ====================

    @Test
    public void testMatchConsumeTrue() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matchConsume("hel"));
        assertEquals('l', reader.current());
    }

    @Test
    public void testMatchConsumeFalse() {
        CharacterReader reader = new CharacterReader("hello");
        assertFalse(reader.matchConsume("world"));
        assertEquals('h', reader.current());
    }

    @Test
    public void testMatchConsumeEmptyString() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.matchConsume(""));
        assertEquals('h', reader.current());
    }

    @Test
    public void testMatchConsumeOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchConsume("a"));
    }

    // ==================== matchConsumeIgnoreCase(String) ====================

    @Test
    public void testMatchConsumeIgnoreCaseTrue() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matchConsumeIgnoreCase("hello"));
        assertEquals('l', reader.current());
    }

    @Test
    public void testMatchConsumeIgnoreCaseFalse() {
        CharacterReader reader = new CharacterReader("Hello");
        assertFalse(reader.matchConsumeIgnoreCase("world"));
        assertEquals('H', reader.current());
    }

    @Test
    public void testMatchConsumeIgnoreCaseEmpty() {
        CharacterReader reader = new CharacterReader("Hello");
        assertTrue(reader.matchConsumeIgnoreCase(""));
        assertEquals('H', reader.current());
    }

    @Test
    public void testMatchConsumeIgnoreCaseOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matchConsumeIgnoreCase("a"));
    }

    // ==================== cacheString ====================

    @Test
    public void testCacheStringBasic() {
        CharacterReader reader = new CharacterReader("hello");
        // cacheString is used internally; we can test indirectly via consumeToEnd
        // but we can also test directly if method is public? It's package-private.
        // We'll test via public methods that use it.
        // Actually, cacheString is called by consumeToEnd and others.
        // We already tested consumeToEnd.
    }

    // ==================== rangeEquals ====================

    @Test
    public void testRangeEqualsTrue() {
        CharacterReader reader = new CharacterReader("hello world");
        assertTrue(reader.rangeEquals(0, 5, "hello"));
    }

    @Test
    public void testRangeEqualsFalse() {
        CharacterReader reader = new CharacterReader("hello world");
        assertFalse(reader.rangeEquals(0, 5, "world"));
    }

    @Test
    public void testRangeEqualsEmptyString() {
        CharacterReader reader = new CharacterReader("hello");
        assertTrue(reader.rangeEquals(0, 0, ""));
    }

    @Test
    public void testRangeEqualsOutOfBounds() {
        CharacterReader reader = new CharacterReader("hello");
        // start beyond length
        assertFalse(reader.rangeEquals(10, 1, "a"));
    }

    @Test
    public void testRangeEqualsWithCachedString() {
        // This tests the caching mechanism indirectly
        CharacterReader reader = new CharacterReader("hello");
        reader.consumeToEnd(); // caches the string
        assertTrue(reader.rangeEquals(0, 5, "hello"));
    }

    // ==================== Additional edge cases ====================

    @Test
    public void testConsumeToWithMultipleChars() {
        CharacterReader reader = new CharacterReader("a.b.c");
        String part = reader.consumeTo('.');
        assertEquals("a", part);
        assertEquals('.', reader.current());
        reader.advance();
        part = reader.consumeTo('.');
        assertEquals("b", part);
        assertEquals('.', reader.current());
        reader.advance();
        part = reader.consumeTo('.');
        assertEquals("c", part);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToAnyWithEmptyArray() {
        CharacterReader reader = new CharacterReader("hello");
        // consumeToAny with empty char array should consume nothing?
        // Implementation likely returns empty string and does not advance.
        String consumed = reader.consumeToAny();
        assertEquals("", consumed);
        assertEquals('h', reader.current());
    }

    @Test
    public void testMatchesAnyWithEmptyArray() {
        CharacterReader reader = new CharacterReader("hello");
        assertFalse(reader.matchesAny());
    }

    @Test
    public void testConsumeToAnySortedWithEmptyArray() {
        CharacterReader reader = new CharacterReader("hello");
        char[] empty = {};
        String consumed = reader.consumeToAnySorted(empty);
        assertEquals("", consumed);
        assertEquals('h', reader.current());
    }

    @Test
    public void testAdvanceAndCurrentAfterConsumeToEnd() {
        CharacterReader reader = new CharacterReader("hello");
        reader.consumeToEnd();
        assertTrue(reader.isEmpty());
        try {
            reader.current();
            fail("Expected exception");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testMultipleConsumeToEndCalls() {
        CharacterReader reader = new CharacterReader("hello");
        String first = reader.consumeToEnd();
        assertEquals("hello", first);
        String second = reader.consumeToEnd();
        assertEquals("", second);
        assertTrue(reader.isEmpty());
    }
}