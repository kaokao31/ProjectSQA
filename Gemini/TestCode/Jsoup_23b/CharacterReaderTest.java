package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class CharacterReaderTest {

    @Test
    public void testConstructorAndBasicMethods() {
        CharacterReader reader = new CharacterReader("TestString");
        assertEquals('T', reader.current());
        assertEquals(0, reader.pos());
        assertEquals("TestString", reader.toString());
    }

    @Test
    public void testConsume() {
        CharacterReader reader = new CharacterReader("ABC");
        assertEquals('A', reader.consume());
        assertEquals(1, reader.pos());
        assertEquals('B', reader.consume());
        assertEquals('C', reader.consume());
        assertEquals(CharacterReader.EOF, reader.consume());
        assertEquals(CharacterReader.EOF, reader.consume()); // multiple EOF calls
    }

    @Test
    public void testUnconsume() {
        CharacterReader reader = new CharacterReader("ABC");
        assertEquals('A', reader.consume());
        assertEquals('B', reader.consume());
        reader.unconsume();
        assertEquals('B', reader.consume());
        assertEquals('C', reader.consume());
    }

    @Test
    public void testAdvance() {
        CharacterReader reader = new CharacterReader("ABC");
        assertEquals(0, reader.pos());
        reader.advance();
        assertEquals(1, reader.pos());
        assertEquals('B', reader.current());
    }

    @Test
    public void testMarkAndReset() {
        CharacterReader reader = new CharacterReader("ABCDE");
        assertEquals('A', reader.consume());
        assertEquals('B', reader.consume());
        
        reader.mark();
        assertEquals('C', reader.consume());
        assertEquals('D', reader.consume());
        
        reader.reset();
        assertEquals('C', reader.consume());
    }

    @Test
    public void testConsumeVisibleSequence() {
        CharacterReader reader = new CharacterReader("   ABC");
        assertEquals(3, reader.consumeVisibleSequence());
        assertEquals('A', reader.current());

        CharacterReader reader2 = new CharacterReader("ABC");
        assertEquals(0, reader2.consumeVisibleSequence());
        assertEquals('A', reader2.current());

        CharacterReader reader3 = new CharacterReader("      ");
        assertEquals(6, reader3.consumeVisibleSequence());
        assertTrue(reader3.isEmpty());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abc123XYZ");
        assertEquals("abc", reader.consumeLetterSequence());
        assertEquals('1', reader.current());

        CharacterReader reader2 = new CharacterReader("123abc");
        assertEquals("", reader2.consumeLetterSequence());
        assertEquals('1', reader2.current());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("abc123xyz#");
        assertEquals("abc123xyz", reader.consumeLetterThenDigitSequence());
        assertEquals('#', reader.current());

        CharacterReader reader2 = new CharacterReader("123abc");
        assertEquals("", reader2.consumeLetterThenDigitSequence());
    }

    @Test
    public void testConsumeHex() {
        CharacterReader reader = new CharacterReader("0123456789abcdefABCDEFXYZ");
        assertEquals("0123456789abcdefABCDEF", reader.consumeHexSequence());
        assertEquals('X', reader.current());

        CharacterReader reader2 = new CharacterReader("XYZ");
        assertEquals("", reader2.consumeHexSequence());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("12345a678");
        assertEquals("12345", reader.consumeDigitSequence());
        assertEquals('a', reader.current());

        CharacterReader reader2 = new CharacterReader("abc");
        assertEquals("", reader2.consumeDigitSequence());
    }

    @Test
    public void testMatches() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matches('H'));
        assertFalse(reader.matches('e'));
        assertTrue(reader.matches("Hell"));
        assertTrue(reader.matches("Hello World"));
        assertFalse(reader.matches("Hello World!"));
        assertFalse(reader.matches("World"));
    }

    @Test
    public void testMatchesIgnoreCase() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.matchesIgnoreCase("hello"));
        assertTrue(reader.matchesIgnoreCase("HELLO WORLD"));
        assertFalse(reader.matchesIgnoreCase("world"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader reader = new CharacterReader("Hello");
        char[] any = {'X', 'Y', 'H'};
        char[] none = {'X', 'Y', 'Z'};
        assertTrue(reader.matchesAny(any));
        assertFalse(reader.matchesAny(none));
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("Hello Beautiful World");
        assertTrue(reader.containsIgnoreCase("beautiful"));
        assertTrue(reader.containsIgnoreCase("HELLO"));
        assertFalse(reader.containsIgnoreCase("nonexistent"));
    }

    @Test
    public void testConsumeTo() {
        CharacterReader reader = new CharacterReader("Hello:World;End");
        assertEquals("Hello", reader.consumeTo(':'));
        assertEquals(':', reader.consume());
        
        assertEquals("World", reader.consumeTo(';'));
        assertEquals(';', reader.consume());
        
        assertEquals("End", reader.consumeTo('X')); // consume to char not present
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testConsumeToBoundary() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertEquals("Hello", reader.consumeTo(' '));
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("Hello, World;End");
        char[] terminators = {',', ';'};
        assertEquals("Hello", reader.consumeToAny(terminators));
        assertEquals(',', reader.consume());
        assertEquals(" World", reader.consumeToAny(terminators));
        assertEquals(';', reader.consume());
        assertEquals("End", reader.consumeToAny(terminators));
    }

    @Test
    public void testConsumeToEndString() {
        CharacterReader reader = new CharacterReader("Hello--End--Tail");
        assertEquals("Hello", reader.consumeTo("--"));
        assertEquals("--", reader.consumeTo("--")); // wait, consumeTo consumes up to string
        assertEquals("--", reader.consume("---")); // test false match handling if any
    }

    @Test
    public void testContains() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.contains("World"));
        assertFalse(reader.contains("Universe"));
    }

    @Test
    public void testSpotBugSpecificScenariosAndEdgeCases() {
        // Specifically targeting Jsoup 23 character reader string handling (e.g., entity parsing / markup matching bounds)
        CharacterReader reader = new CharacterReader("&#x30;");
        assertTrue(reader.matches('#'));
        reader.advance();
        assertTrue(reader.matches('x') || reader.matches('X'));
        
        CharacterReader emptyReader = new CharacterReader("");
        assertTrue(emptyReader.isEmpty());
        assertEquals(CharacterReader.EOF, emptyReader.current());
        assertEquals(CharacterReader.EOF, emptyReader.consume());
        assertEquals("", emptyReader.consumeTo('a'));
        assertEquals("", emptyReader.consumeTo("abc"));
        assertFalse(emptyReader.contains("a"));
        assertFalse(emptyReader.matches('a'));
        assertFalse(emptyReader.matches("a"));
        assertFalse(emptyReader.matchesIgnoreCase("a"));
    }
}