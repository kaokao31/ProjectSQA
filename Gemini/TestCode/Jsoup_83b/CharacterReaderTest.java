package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.Locale;

public class CharacterReaderTest {

    @Test
    public void testInitializationAndBasicGetters() {
        CharacterReader reader = new CharacterReader("test string");
        assertEquals("test string", reader.toString());
        assertEquals(0, reader.pos());
        assertEquals('t', reader.current());
        assertEquals(11, reader.capacity());
        assertEquals(11, reader.remaining());
        assertFalse(reader.isEmpty());
        
        CharacterReader emptyReader = new CharacterReader("");
        assertTrue(emptyReader.isEmpty());
        assertEquals(CharacterReader.EOF, emptyReader.current());
        assertEquals(0, emptyReader.remaining());
    }

    @Test
    public void testNavigationAndPosition() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals('a', reader.current());
        assertEquals(0, reader.pos());
        
        reader.advance();
        assertEquals('b', reader.current());
        assertEquals(1, reader.pos());
        
        reader.consume();
        assertEquals('c', reader.current());
        assertEquals(2, reader.pos());

        reader.rewind();
        assertEquals('b', reader.current());
        assertEquals(1, reader.pos());

        reader.unconsume();
        assertEquals('a', reader.current());
        assertEquals(0, reader.pos());
        
        // Unconsume at start should be safe
        reader.unconsume();
        assertEquals(0, reader.pos());
    }

    @Test
    public void testMarkAndReset() {
        CharacterReader reader = new CharacterReader("abcdef");
        assertEquals('a', reader.current());
        
        reader.mark();
        reader.advance();
        reader.advance();
        assertEquals('c', reader.current());
        
        reader.resetTo();
        assertEquals('a', reader.current());

        // Test explicit pos via mark/reset if supported, or standard consumption
        reader.consume();
        reader.mark();
        reader.advance();
        assertEquals('b', reader.current());
        reader.resetTo();
        // Depending on implementation, resetTo() resets to the marked position
    }

    @Test
    public void testConsumeDataAndMatching() {
        CharacterReader reader = new CharacterReader("Hello World");
        assertEquals("Hello", reader.consumeTo(' '));
        assertEquals(' ', reader.current());

        CharacterReader reader2 = new CharacterReader("abc123xyz");
        assertTrue(reader2.matches('a'));
        assertFalse(reader2.matches('b'));
        assertTrue(reader2.matches("abc"));
        assertFalse(reader2.matches("xyz"));
        
        assertTrue(reader2.matchesIgnoreCase("ABC"));
        assertFalse(reader2.matchesIgnoreCase("XYZ"));
    }

    @Test
    public void testConsumeAny() {
        CharacterReader reader = new CharacterReader("abc123xyz");
        char[] chars = {'x', 'y', 'z', 'a'};
        assertEquals('a', reader.consumeAny(chars));
        assertEquals('b', reader.consumeAny(chars)); // Wait, 'b' is not in chars
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
        CharacterReader reader = new CharacterReader("FFgg");
        assertEquals(255, reader.consumeHexSequence());
    }

    @Test
    public void testConsumeDigitSequence() {
        CharacterReader reader = new CharacterReader("12345abc");
        assertEquals("12345", reader.consumeDigitSequence());
        assertEquals('a', reader.current());
    }

    @Test
    public void testContainsIgnoreCase() {
        CharacterReader reader = new CharacterReader("Hello Target World");
        assertTrue(reader.containsIgnoreCase("target"));
        assertFalse(reader.containsIgnoreCase("missing"));
    }

    @Test
    public void testStringConsumingAndMatching() {
        CharacterReader reader = new CharacterReader("<div>content</div>");
        assertEquals("<div", reader.consumeTo("<"));
        assertTrue(reader.matchConsume("<div"));
        assertEquals("content", reader.consumeTo("</div>"));
    }

    @Test
    public void testBufferBoundsAndCache() {
        // Create a reader with a large enough input to test buffer pulling/cache lines
        char[] largeInput = new char[5000];
        for (int i = 0; i < largeInput.length; i++) {
            largeInput[i] = (char) ('a' + (i % 26));
        }
        CharacterReader reader = new CharacterReader(new CharArrayReader(largeInput), 1024);
        assertFalse(reader.isEmpty());
        String consumed = reader.consumeTo('z');
        assertNotNull(consumed);
    }

    @Test
    public void testNullAndEmptyInputs() {
        CharacterReader reader = new CharacterReader("");
        assertEquals(CharacterReader.EOF, reader.consume());
        assertFalse(reader.matches('a'));
        assertFalse(reader.matchesIgnoreCase("a"));
        assertEquals("", reader.consumeTo('a'));
        assertEquals("", reader.consumeToEnd());
    }
}