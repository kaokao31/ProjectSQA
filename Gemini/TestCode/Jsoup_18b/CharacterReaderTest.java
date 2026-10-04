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
        
        // consume one
        assertEquals('T', reader.consume());
        assertEquals('e', reader.current());
        assertEquals(1, reader.pos());

        // unconsume
        reader.unconsume();
        assertEquals('T', reader.current());
        assertEquals(0, reader.pos());
    }

    @Test
    public void testIsEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        assertEquals(CharacterReader.EOF, reader.current());
        assertEquals(CharacterReader.EOF, reader.consume());

        CharacterReader reader2 = new CharacterReader("a");
        assertFalse(reader2.isEmpty());
        reader2.consume();
        assertTrue(reader2.isEmpty());
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
        assertEquals('c', reader.current());
    }

    @Test
    public void testConsumeAsString() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals("abc", reader.consumeAsString(3));
        assertEquals(3, reader.pos());
        assertEquals("def", reader.consumeAsString(10)); // past end
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testNextIndexOfChar() {
        CharacterReader reader = new CharacterReader("hello world");
        assertEquals(4, reader.nextIndexOf('o'));
        assertEquals(-1, reader.nextIndexOf('z'));
        
        // check from current position
        reader.consume(); // 'h'
        reader.consume(); // 'e'
        assertEquals(2, reader.nextIndexOf('l')); // relative index from current pos? Let's check implementation behavior
    }

    @Test
    public void testNextIndexCharSequence() {
        CharacterReader reader = new CharacterReader("jsoup parser");
        assertEquals(6, reader.nextIndexOf("parser"));
        assertEquals(-1, reader.nextIndexOf("html"));
    }

    @Test
    public void testConsumeToChar() {
        CharacterReader reader = new CharacterReader("key=value");
        assertEquals("key", reader.consumeTo('='));
        assertEquals('=', reader.current());
        
        // consume rest when char not found
        CharacterReader reader2 = new CharacterReader("keyval");
        assertEquals("keyval", reader2.consumeTo('='));
        assertTrue(reader2.isEmpty());
    }

    @Test
    public void testConsumeToCharSequence() {
        CharacterReader reader = new CharacterReader("start--end--finish");
        assertEquals("start", reader.consumeTo("--"));
        assertEquals("--", reader.consumeAsString(2));
        assertEquals("end", reader.consumeTo("--"));
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("hello, world; test");
        char[] chars = {',', ';', ' '};
        assertEquals("hello", reader.consumeToAny(chars));
    }

    @Test
    public void testConsumeData() {
        CharacterReader reader = new CharacterReader("abc&def");
        assertEquals("abc", reader.consumeData());
        assertEquals('&', reader.current());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader reader = new CharacterReader("div id=\"1\"/>");
        assertEquals("div", reader.consumeTagName());
        assertEquals(' ', reader.current());
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("remaining content");
        reader.consume();
        reader.consume();
        assertEquals("maining content", reader.consumeToEnd());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void testStringConsumeMatchAndCheck() {
        CharacterReader reader = new CharacterReader("ABCdef");
        assertTrue(reader.matches("ABC"));
        assertTrue(reader.matchesIgnoreCase("abc"));
        assertFalse(reader.matches("def"));

        assertTrue(reader.matchesAny('A', 'B', 'Z'));
        assertFalse(reader.matchesAny('X', 'Y', 'Z'));
        
        assertTrue(reader.matchesLetter());
        reader.consumeAsString(3); // consume "ABC"
        assertTrue(reader.matchesAsciiAlpha());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("<html><body>Hello</body></html>");
        assertTrue(reader.containsIgnoreCase("BODY"));
        assertFalse(reader.containsIgnoreCase("MISSING"));
    }

    @Test
    public void testRangeEquals() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertTrue(reader.rangeEquals(0, 3, "abc"));
        assertFalse(reader.rangeEquals(0, 3, "xyz"));
        assertFalse(reader.rangeEquals(-1, 3, "abc"));
        assertFalse(reader.rangeEquals(10, 3, "abc"));
    }
}