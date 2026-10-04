package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    // --- Constructor and basic state ---

    @Test
    public void testEmptyInput() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(0, reader.pos());
        assertEquals(0, reader.consumeToEnd().length());
    }

    @Test
    public void testSingleCharacter() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.isEmpty());
        assertEquals('a', reader.current());
        assertEquals(0, reader.pos());
        assertEquals('a', reader.consume());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testMultipleCharacters() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        assertEquals('c', reader.consume());
        assertTrue(reader.isEmpty());
    }

    // --- consumeTo(char) ---

    @Test
    public void testConsumeToCharPresent() {
        CharacterReader reader = new CharacterReader("abcde");
        String consumed = reader.consumeTo('c');
        assertEquals("ab", consumed);
        assertEquals('c', reader.current());
    }

    @Test
    public void testConsumeToCharNotPresent() {
        CharacterReader reader = new CharacterReader("abcde");
        String consumed = reader.consumeTo('z');
        assertEquals("abcde", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToCharAtStart() {
        CharacterReader reader = new CharacterReader("abc");
        String consumed = reader.consumeTo('a');
        assertEquals("", consumed);
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeToCharAtEnd() {
        CharacterReader reader = new CharacterReader("abc");
        String consumed = reader.consumeTo('c');
        assertEquals("ab", consumed);
        assertEquals('c', reader.current());
        reader.consume();
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToCharEmptyInput() {
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeTo('a');
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // --- consumeTo(String) ---

    @Test
    public void testConsumeToStringPresent() {
        CharacterReader reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo("cd");
        assertEquals("ab", consumed);
        assertEquals('c', reader.current());
    }

    @Test
    public void testConsumeToStringNotPresent() {
        CharacterReader reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo("zz");
        assertEquals("abcdef", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToStringAtStart() {
        CharacterReader reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo("ab");
        assertEquals("", consumed);
        assertEquals('a', reader.current());
    }

    @Test
    public void testConsumeToStringAtEnd() {
        CharacterReader reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo("ef");
        assertEquals("abcd", consumed);
        assertEquals('e', reader.current());
    }

    @Test
    public void testConsumeToStringEmptyInput() {
        CharacterReader reader = new CharacterReader("");
        String consumed = reader.consumeTo("ab");
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // --- consumeToEnd ---

    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals("hello", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToEndAfterPartialConsume() {
        CharacterReader reader = new CharacterReader("hello");
        reader.consumeTo('l');
        assertEquals("llo", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    // --- matches(char) ---

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
    public void testMatchesCharEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matches('a'));
    }

    // --- matches(String) ---

    @Test
    public void testMatchesStringTrue() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches("ab"));
    }

    @Test
    public void testMatchesStringFalse() {
        CharacterReader reader = new CharacterReader("abc");
        assertFalse(reader.matches("ac"));
    }

    @Test
    public void testMatchesStringLongerThanRemaining() {
        CharacterReader reader = new CharacterReader("ab");
        assertFalse(reader.matches("abc"));
    }

    @Test
    public void testMatchesStringEmpty() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matches(""));
    }

    // --- matchesIgnoreCase(String) ---

    @Test
    public void testMatchesIgnoreCaseTrue() {
        CharacterReader reader = new CharacterReader("AbC");
        assertTrue(reader.matchesIgnoreCase("abc"));
    }

    @Test
    public void testMatchesIgnoreCaseFalse() {
        CharacterReader reader = new CharacterReader("AbC");
        assertFalse(reader.matchesIgnoreCase("abd"));
    }

    // --- matchConsume(String) ---

    @Test
    public void testMatchConsumeTrue() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertTrue(reader.matchConsume("abc"));
        assertEquals('d', reader.current());
        assertEquals(3, reader.pos());
    }

    @Test
    public void testMatchConsumeFalse() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertFalse(reader.matchConsume("abd"));
        assertEquals('a', reader.current());
        assertEquals(0, reader.pos());
    }

    @Test
    public void testMatchConsumeAtEnd() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consumeToEnd();
        assertFalse(reader.matchConsume("a"));
        assertTrue(reader.isEmpty());
    }

    // --- matchConsumeIgnoreCase(String) ---

    @Test
    public void testMatchConsumeIgnoreCaseTrue() {
        CharacterReader reader = new CharacterReader("AbCdef");
        assertTrue(reader.matchConsumeIgnoreCase("abc"));
        assertEquals('d', reader.current());
    }

    @Test
    public void testMatchConsumeIgnoreCaseFalse() {
        CharacterReader reader = new CharacterReader("AbCdef");
        assertFalse(reader.matchConsumeIgnoreCase("abd"));
        assertEquals('A', reader.current());
    }

    // --- consume() ---

    @Test(expected = IndexOutOfBoundsException.class)
    public void testConsumeEmpty() {
        CharacterReader reader = new CharacterReader("");
        reader.consume();
    }

    @Test
    public void testConsumeAdvancesPosition() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals('b', reader.consume());
        assertEquals(2, reader.pos());
    }

    // --- unconsume() ---

    @Test
    public void testUnconsume() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume();
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.current());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUnconsumeAtStart() {
        CharacterReader reader = new CharacterReader("abc");
        reader.unconsume();
    }

    // --- advance() ---

    @Test
    public void testAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        reader.advance();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAdvanceBeyondEnd() {
        CharacterReader reader = new CharacterReader("a");
        reader.advance();
        reader.advance(); // should throw
    }

    // --- isEmpty() ---

    @Test
    public void testIsEmptyInitiallyFalse() {
        CharacterReader reader = new CharacterReader("a");
        assertFalse(reader.isEmpty());
    }

    @Test
    public void testIsEmptyAfterConsumeAll() {
        CharacterReader reader = new CharacterReader("a");
        reader.consume();
        assertTrue(reader.isEmpty());
    }

    // --- pos() ---

    @Test
    public void testPosAfterConsume() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume();
        reader.consume();
        assertEquals(2, reader.pos());
    }

    // --- current() ---

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCurrentAtEnd() {
        CharacterReader reader = new CharacterReader("");
        reader.current();
    }

    @Test
    public void testCurrentAfterConsume() {
        CharacterReader reader = new CharacterReader("abc");
        reader.consume();
        assertEquals('b', reader.current());
    }

    // --- Edge case: consumeTo with character that appears multiple times ---

    @Test
    public void testConsumeToCharMultipleOccurrences() {
        CharacterReader reader = new CharacterReader("abacad");
        String consumed = reader.consumeTo('a');
        assertEquals("", consumed);
        assertEquals('a', reader.current());
        reader.consume(); // consume first 'a'
        consumed = reader.consumeTo('a');
        assertEquals("b", consumed);
        assertEquals('a', reader.current());
    }

    // --- Edge case: consumeTo with string that appears multiple times ---

    @Test
    public void testConsumeToStringMultipleOccurrences() {
        CharacterReader reader = new CharacterReader("ababcab");
        String consumed = reader.consumeTo("ab");
        assertEquals("", consumed);
        assertEquals('a', reader.current());
        reader.consume(); // consume 'a'
        reader.consume(); // consume 'b'
        consumed = reader.consumeTo("ab");
        assertEquals("c", consumed);
        assertEquals('a', reader.current());
    }

    // --- Bug-specific: consumeTo when character not found should not loop ---

    @Test(timeout = 1000)
    public void testConsumeToCharNotPresentNoInfiniteLoop() {
        CharacterReader reader = new CharacterReader("abcdef");
        String result = reader.consumeTo('z');
        assertEquals("abcdef", result);
        assertTrue(reader.isEmpty());
    }

    @Test(timeout = 1000)
    public void testConsumeToStringNotPresentNoInfiniteLoop() {
        CharacterReader reader = new CharacterReader("abcdef");
        String result = reader.consumeTo("zz");
        assertEquals("abcdef", result);
        assertTrue(reader.isEmpty());
    }

    // --- Additional edge: consumeTo with empty string? (if allowed) ---

    @Test
    public void testConsumeToEmptyString() {
        CharacterReader reader = new CharacterReader("abc");
        String result = reader.consumeTo("");
        assertEquals("", result);
        assertEquals('a', reader.current());
    }

    // --- Test matches with empty reader ---

    @Test
    public void testMatchesOnEmptyReader() {
        CharacterReader reader = new CharacterReader("");
        assertFalse(reader.matches('a'));
        assertFalse(reader.matches("a"));
        assertTrue(reader.matches(""));
    }

    // --- Test matchConsume with empty string ---

    @Test
    public void testMatchConsumeEmptyString() {
        CharacterReader reader = new CharacterReader("abc");
        assertTrue(reader.matchConsume(""));
        assertEquals('a', reader.current());
    }
}