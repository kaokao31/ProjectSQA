package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    @Test
    public void testConstructorAndBasicProperties() {
        CharacterReader reader = new CharacterReader("test string");
        assertEquals('t', reader.current());
        assertEquals(0, reader.pos());
        assertEquals("test string", reader.toString());
        assertEquals(11, reader.length());
        
        reader.advance();
        assertEquals('e', reader.current());
        assertEquals(1, reader.pos());
    }

    @Test
    public void testEmptyAndNullInput() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(0, reader.pos());

        CharacterReader nullReader = new CharacterReader(null);
        assertTrue(nullReader.isEmpty());
        assertEquals(CharacterReader.EOF, nullReader.current());
    }

    @Test
    public void testConsumeMethods() {
        CharacterReader reader = new CharacterReader("abcde");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        assertEquals(2, reader.pos());

        reader.unconsume();
        assertEquals(1, reader.pos());
        assertEquals('b', reader.consume());

        assertEquals("cde", reader.consumeTo('f'));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("abc,def");
        assertEquals("abc", reader.consumeTo(','));
        assertEquals(',', reader.current());
        reader.advance();
        assertEquals("def", reader.consumeTo('x'));
    }

    @Test
    public void testConsumeToString() {
        CharacterReader reader = new CharacterReader("one--two--three");
        assertEquals("one", reader.consumeTo("--"));
        assertEquals(3, reader.pos());
        
        reader.consumeData(); // consume '--'
        assertEquals("two", reader.consumeTo("--"));
    }

    @Test
    public void testConsumeAny() {
        CharacterReader reader = new CharacterReader("abc123xyz");
        assertEquals("abc", reader.consumeAny("123", "abc"));
        assertEquals('1', reader.current());
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
        CharacterReader reader = new CharacterReader("456abc");
        assertEquals("456", reader.consumeDigitSequence());
        assertEquals('a', reader.current());
    }

    @Test
    public void testMatches() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matches("Hello"));
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('e'));
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertFalse(reader.matchesIgnoreCase("world"));
        assertTrue(reader.matchesAny('H', 'X'));
        assertFalse(reader.matchesAny('a', 'b'));
        assertTrue(reader.matchesAny("Hello", "World"));
        assertTrue(reader.matchesAnyIgnoreCase("HELLO", "XYZ"));
    }

    @Test
    public void testContains() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.contains('o'));
        assertFalse(reader.contains('z'));
        assertTrue(reader.containsIgnoreCase("WORLD"));
        assertFalse(reader.containsIgnoreCase("NOTFOUND"));
    }

    @Test
    public void testRangeEquals() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.rangeEquals(0, 5, "Hello"));
        assertFalse(reader.rangeEquals(0, 5, "World"));
        assertFalse(reader.rangeEquals(-1, 5, "Hello"));
        assertFalse(reader.rangeEquals(0, 50, "Hello"));
    }

    @Test
    public void testMarkAndReset() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        
        reader.mark();
        assertEquals('c', reader.consume());
        assertEquals('d', reader.consume());
        
        reader.rewindToMark();
        assertEquals('c', reader.consume());

        reader.unconsume();
        reader.destroyMark();
        reader.rewindToMark(); // Should do nothing or handle safely
    }

    @Test
    public void testConsumeBodyAndSequenceEdgeCases() {
        CharacterReader reader = new CharacterReader("abc&amp;def");
        assertEquals("abc", reader.consumeTo('&'));
        assertEquals('&', reader.consume());
        assertEquals("amp", reader.consumeTo(';'));
        assertEquals(';', reader.consume());
        assertEquals("def", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testIndexOf() {
        CharacterReader reader = new CharacterReader("abc-def-ghi");
        assertEquals(3, reader.nextIndexOf('-'));
        assertEquals(7, reader.nextIndexOf("--"));
    }

    @Test
    public void testCacheStringCoverage() {
        CharacterReader reader = new CharacterReader("cached string cached string");
        String s1 = reader.consumeTo(' ');
        reader.advance();
        String s2 = reader.consumeTo(' ');
        // Testing that cache mechanism is exercised
        assertNotNull(s1);
        assertNotNull(s2);
    }
}