package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    @Test
    public void testConstructorAndBasicProperties() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals('a', reader.current());
        assertEquals(0, reader.pos());
        assertEquals("abc", reader.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        new CharacterReader(null);
    }

    @Test
    public void testEmptyInput() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
    }

    @Test
    public void testConsume() {
        CharacterReader reader = new CharacterReader("ab");
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals('b', reader.consume());
        assertEquals(2, reader.pos());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(2, reader.pos());
    }

    @Test
    public void testUnconsume() {
        CharacterReader reader = new CharacterReader("ab");
        assertEquals('a', reader.consume());
        assertEquals(1, reader.pos());
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('a', reader.consume());
    }

    @Test
    public void testAdvance() {
        CharacterReader reader = new CharacterReader("abc");
        assertEquals(0, reader.pos());
        reader.advance();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.current());
    }

    @Test
    public void testMarkAndReset() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        assertEquals(2, reader.pos());

        reader.mark();
        assertEquals('c', reader.consume());
        assertEquals('d', reader.consume());
        assertEquals(4, reader.pos());

        reader.rewindToMark();
        assertEquals(2, reader.pos());
        assertEquals('c', reader.consume());
    }

    @Test
    public void testLocate() {
        CharacterReader reader = new CharacterReader("line1\nline2");
        assertEquals(1, reader.lineNumber());
        assertEquals(0, reader.columnNumber());
        
        reader.consume(); // l
        reader.consume(); // i
        assertEquals(3, reader.columnNumber());
        
        // Consume up to newline
        reader.consume(); // n
        reader.consume(); // e
        reader.consume(); // 1
        reader.consume(); // \n
        assertEquals(2, reader.lineNumber());
        assertEquals(0, reader.columnNumber());
    }

    @Test
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("hello");
        assertEquals("he", reader.consumeAsString(2));
        assertEquals(2, reader.pos());
        // Test consuming more than remaining
        assertEquals("llo", reader.consumeAsString(10));
        assertEquals(5, reader.pos());
    }

    @Test
    public void testContains() {
        CharacterReader reader = new CharacterReader("hello world");
        assertTrue(reader.contains('w'));
        assertTrue(reader.contains('h'));
        assertFalse(reader.contains('z'));
    }

    @Test
    public void testMatch() {
        CharacterReader reader = new CharacterReader("hello world");
        assertTrue(reader.matchConsume("hello"));
        assertFalse(reader.matchConsume("notthere"));
        
        reader.rewindToMark(); // back to start (need to mark first)
        CharacterReader reader2 = new CharacterReader("hello world");
        assertTrue(reader2.matches("hello"));
        assertFalse(reader2.matches("world"));
        assertTrue(reader2.matchesIgnoreCase("HELLO"));
        assertFalse(reader2.matchesIgnoreCase("WORLD"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader reader = new CharacterReader("abc");
        char[] chars = {'z', 'b', 'a'};
        assertTrue(reader.matchesAny(chars));
        reader.consume();
        assertTrue(reader.matchesAny(chars));
        reader.consume();
        assertTrue(reader.matchesAny(chars));
        reader.consume();
        assertFalse(reader.matchesAny(chars));
    }

    @Test
    public void testMatchesAnyStringArray() {
        CharacterReader reader = new CharacterReader("foo bar");
        String[] seqs = {"baz", "foo", "bar"};
        assertTrue(reader.matchesAny(seqs));
    }

    @Test
    public void testMatchesLetter() {
        CharacterReader reader = new CharacterReader("A1_");
        assertTrue(reader.matchesLetter());
        reader.consume();
        assertFalse(reader.matchesLetter());
    }

    @Test
    public void testMatchesDigit() {
        CharacterReader reader = new CharacterReader("1A");
        assertTrue(reader.matchesDigit());
        reader.consume();
        assertFalse(reader.matchesDigit());
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("test string");
        reader.consume();
        assertEquals("est string", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeTo() {
        CharacterReader reader = new CharacterReader("foo-bar-baz");
        assertEquals("foo", reader.consumeTo('-'));
        assertEquals('-', reader.consume());
        assertEquals("bar-baz", reader.consumeTo("baz"));
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("foo,bar;baz");
        char[] terminators = {',', ';'};
        assertEquals("foo", reader.consumeToAny(terminators));
        assertEquals(',', reader.consume());
        assertEquals("bar", reader.consumeToAny(terminators));
    }

    @Test
    public void testConsumeData() {
        CharacterReader reader = new CharacterReader("abc&def");
        assertEquals("abc", reader.consumeData());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader reader = new CharacterReader("div id='foo'");
        assertEquals("div", reader.consumeTagName());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abc123xyz");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals('1', reader.current());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("abc123xyz");
        assertEquals("abc123", reader.consumeLetterThenDigitSequence());
        assertEquals('x', reader.current());
    }

    @Test
    public void testConsumeHex() {
        CharacterReader reader = new CharacterReader("FFg");
        assertEquals(255, reader.consumeHexSequence());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("123abc");
        assertEquals("123", reader.consumeDigitSequence());
        assertEquals('a', reader.current());
    }

    @Test
    public void testStringCache() {
        CharacterReader reader = new CharacterReader("abc");
        String[] cache = new String[5];
        char[] val = {'a', 'b', 'c'};
        
        // Exercise cache logic if present
        String s1 = CharacterReader.cacheString(cache, val, 0, 3);
        String s2 = CharacterReader.cacheString(cache, val, 0, 3);
        assertSame(s1, s2);
        
        // Test cache miss / overflow
        String s3 = CharacterReader.cacheString(cache, new char[]{'d', 'e'}, 0, 2);
        assertNotNull(s3);
    }
}