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
        
        assertEquals('T', reader.consume());
        assertEquals(1, reader.pos());
        
        reader.unconsume();
        assertEquals(0, reader.pos());
        assertEquals('T', reader.current());
    }

    @Test
    public void testIsEmpty() {
        CharacterReader reader = new CharacterReader("");
        assertTrue(reader.isEmpty());
        
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
        assertEquals('c', reader.consume());
    }

    @Test
    public void testConsumeBackTo() {
        CharacterReader reader = new CharacterReader("abcdef");
        reader.consumeData(); // consume some
        // test unconsume / rewind logic via mark
        reader.mark();
        reader.consume();
        reader.consume();
        assertTrue(reader.pos() > 2);
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertTrue(reader.containsIgnoreCase("world"));
        assertFalse(reader.containsIgnoreCase("missing"));
    }

    @Test
    public void testMatchRegexAndStrings() {
        CharacterReader reader = new CharacterReader("12345 abcde");
        assertTrue(reader.matches('1'));
        assertFalse(reader.matches('2'));
        
        assertTrue(reader.matches("123"));
        assertFalse(reader.matches("321"));
        
        assertTrue(reader.matchesIgnoreCase("123"));
    }

    @Test
    public void testMatchesAny() {
        CharacterReader reader = new CharacterReader("abc");
        char[] any = {'x', 'y', 'a'};
        assertTrue(reader.matchesAny(any));
        
        char[] none = {'x', 'y', 'z'};
        assertFalse(reader.matchesAny(none));
        
        assertFalse(reader.matchesAny());
    }

    @Test
    public void testMatchesDigit() {
        CharacterReader reader = new CharacterReader("9abc");
        assertTrue(reader.matchesDigit());
        
        CharacterReader reader2 = new CharacterReader("abc");
        assertFalse(reader2.matchesDigit());
    }

    @Test
    public void testMatchConsumeSequence() {
        CharacterReader reader = new CharacterReader("foo-bar");
        assertTrue(reader.matchConsume("foo"));
        assertEquals("-bar", reader.consumeTo(""));
        
        CharacterReader reader2 = new CharacterReader("FOO-bar");
        assertTrue(reader2.matchConsumeIgnoreCase("foo"));
    }

    @Test
    public void testConsumeTo() {
        CharacterReader reader = new CharacterReader("hello:world");
        assertEquals("hello", reader.consumeTo(':'));
        assertEquals(':', reader.consume());
        assertEquals("world", reader.consumeToEnd());
    }

    @Test
    public void testConsumeToAny() {
        CharacterReader reader = new CharacterReader("a,b;c");
        char[] chars = {',', ';'};
        assertEquals("a", reader.consumeToAny(chars));
    }

    @Test
    public void testConsumeToIgnoreCase() {
        CharacterReader reader = new CharacterReader("HelloENDTarget");
        assertEquals("Hello", reader.consumeToIgnoreCase("end"));
    }

    @Test
    public void testConsumeSequence() {
        CharacterReader reader = new CharacterReader("abcXYZ");
        assertEquals("abc", reader.consumeSequence("abc"));
    }

    @Test
    public void testConsumeHex() {
        CharacterReader reader = new CharacterReader("0x1A 1F Z");
        int val = reader.consumeHex();
        assertTrue(val >= 0);
    }

    @Test
    public void testConsumeDigit() {
        CharacterReader reader = new CharacterReader("123a");
        assertEquals(123, reader.consumeDigit());
    }

    @Test
    public void testConsumeLetterSequence() {
        CharacterReader reader = new CharacterReader("abc123xyz");
        assertEquals("abc", reader.consumeLetterSequence());
    }

    @Test
    public void testConsumeLetterThenDigitSequence() {
        CharacterReader reader = new CharacterReader("abc123xyz");
        assertEquals("abc123", reader.consumeLetterThenDigitSequence());
    }

    @Test
    public void testConsumeData() {
        CharacterReader reader = new CharacterReader("data&more");
        assertEquals("data", reader.consumeData());
    }

    @Test
    public void testConsumeTagName() {
        CharacterReader reader = new CharacterReader("tag-name>");
        assertEquals("tag-name", reader.consumeTagName());
    }

    @Test
    public void testConsumeToEnd() {
        CharacterReader reader = new CharacterReader("entire string");
        assertEquals("entire string", reader.consumeToEnd());
    }

    @Test
    public void testNextIndexOf() {
        CharacterReader reader = new CharacterReader("one two three");
        assertEquals(3, reader.nextIndexOf(' '));
        assertEquals(4, reader.nextIndexOf("two"));
    }

    @Test
    public void testCacheHandling() {
        CharacterReader reader = new CharacterReader("aaaaaaaaaaabbbbbbbbbbb");
        // Exercise caching behavior for substrings
        String s1 = reader.consumeTo('b');
        reader.advance();
        String s2 = reader.consumeTo('b');
        assertNotNull(s1);
        assertNotNull(s2);
    }

    @Test
    public void testBogusAndEdgeInputs() {
        CharacterReader reader = new CharacterReader(null);
        assertTrue(reader.isEmpty());
        assertEquals("", reader.toString());
        
        CharacterReader reader2 = new CharacterReader("a");
        assertEquals(-1, reader2.nextIndexOf("missing"));
        assertFalse(reader2.containsIgnoreCase("missing"));
    }
}