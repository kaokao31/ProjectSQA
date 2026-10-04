package org.jsoup.parser;

import org.junit.Test;
import static org.junit.Assert.*;

public class TokenQueueTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNull() {
        new TokenQueue(null);
    }

    @Test
    public void testConstructorValid() {
        TokenQueue tq = new TokenQueue("test");
        assertEquals("test", tq.toString());
        assertFalse(tq.isEmpty());
    }

    @Test
    public void testPeek() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals('a', tq.peek());
        assertEquals('a', tq.consume());
        assertEquals('b', tq.peek());
        tq.consume();
        tq.consume();
        assertTrue(tq.isEmpty());
        assertEquals('\0', tq.peek());
    }

    @Test
    public void testAddFirstString() {
        TokenQueue tq = new TokenQueue("c");
        tq.addFirst("ab");
        assertEquals("abc", tq.toString());
        assertEquals('a', tq.consume());
    }

    @Test
    public void testAddFirstChar() {
        TokenQueue tq = new TokenQueue("bc");
        tq.addFirst('a');
        assertEquals("abc", tq.toString());
    }

    @Test
    public void testMatches() {
        TokenQueue tq = new TokenQueue("hello world");
        assertTrue(tq.matches("hello"));
        assertFalse(tq.matches("world"));
        
        tq.consumeWord(); // consumes "hello"
        tq.consume(); // space
        assertTrue(tq.matches("world"));
        assertTrue(tq.matches(""));
    }

    @Test
    public void testMatchesIgnoreCase() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertTrue(tq.matchesIgnoreCase("hello"));
        assertFalse(tq.matchesIgnoreCase("world"));
    }

    @Test
    public void testMatchesAny() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny('x', 'y', 'a'));
        assertFalse(tq.matchesAny('x', 'y', 'z'));
        assertTrue(tq.matchesAny());
    }

    @Test
    public void testMatchesAnyStringArray() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny("def", "ab"));
        assertFalse(tq.matchesAny("def", "xyz"));
    }

    @Test
    public void testMatchChomp() {
        TokenQueue tq = new TokenQueue("<div>content</div>");
        assertTrue(tq.matchChomp("<div"));
        assertEquals(">", tq.remainder());
    }

    @Test
    public void testAdvance() {
        TokenQueue tq = new TokenQueue("abc");
        tq.advance();
        assertEquals('b', tq.peek());
    }

    @Test
    public void testConsume() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals('a', tq.consume());
        assertEquals('b', tq.consume());
        assertEquals('c', tq.consume());
        assertEquals('\0', tq.consume());
    }

    @Test
    public void testConsumeExpected() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume("ab");
        assertEquals('c', tq.peek());

        try {
            tq.consume("x");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testConsumeTo() {
        TokenQueue tq = new TokenQueue("hello:world");
        assertEquals("hello", tq.consumeTo(":"));
        assertEquals(":", tq.remainder().substring(0, 1));
        
        // Consume to non-existent
        TokenQueue tq2 = new TokenQueue("hello");
        assertEquals("hello", tq2.consumeTo("missing"));
        assertTrue(tq2.isEmpty());
    }

    @Test
    public void testConsumeToIgnoreCase() {
        TokenQueue tq = new TokenQueue("Hello:World");
        assertEquals("Hello", tq.consumeToIgnoreCase(":"));
    }

    @Test
    public void testConsumeToAny() {
        TokenQueue tq = new TokenQueue("one,two;three");
        assertEquals("one", tq.consumeToAny(",", ";"));
        assertEquals(",", tq.remainder().substring(0, 1));
    }

    @Test
    public void testChompTo() {
        TokenQueue tq = new TokenQueue("one<br>two");
        assertEquals("one", tq.chompTo("<br>"));
        assertEquals("two", tq.remainder());

        // Not found case
        TokenQueue tq2 = new TokenQueue("one");
        assertEquals("one", tq2.chompTo("missing"));
        assertTrue(tq2.isEmpty());
    }

    @Test
    public void testChompToIgnoreCase() {
        TokenQueue tq = new TokenQueue("one<BR>two");
        assertEquals("one", tq.chompToIgnoreCase("<br>"));
        assertEquals("two", tq.remainder());
    }

    @Test
    public void testChompBalanced() {
        TokenQueue tq = new TokenQueue("(a (b) c) remainder");
        assertEquals("a (b) c", tq.chompBalanced('(', ')'));
        assertEquals(" remainder", tq.remainder());

        // Test escaping and quotes inside balanced
        TokenQueue tq2 = new TokenQueue("( \"(abc)\" ) rest");
        assertEquals(" \"(abc)\" ", tq2.chompBalanced('(', ')'));
        
        // Test single quotes and escaped quotes
        TokenQueue tq3 = new TokenQueue("( 'a\\'b' ) rest");
        assertEquals(" 'a\\'b' ", tq3.chompBalanced('(', ')'));
        
        // Test unbalanced
        TokenQueue tq4 = new TokenQueue("(abc");
        assertEquals("abc", tq4.chompBalanced('(', ')'));
    }

    @Test
    public void testUnescape() {
        assertEquals("a\\b", TokenQueue.unescape("a\\\\b"));
        assertEquals("a", TokenQueue.unescape("a"));
    }

    @Test
    public void testConsumeWord() {
        TokenQueue tq = new TokenQueue("Word_123!rest");
        assertEquals("Word_123", tq.consumeWord());
        assertEquals('!', tq.peek());
    }

    @Test
    public void testConsumeTagName() {
        TokenQueue tq = new TokenQueue("div#id.class");
        assertEquals("div#id.class", tq.consumeTagName());
    }

    @Test
    public void testConsumeCssIdentifier() {
        TokenQueue tq = new TokenQueue("my-class_name!rest");
        assertEquals("my-class_name", tq.consumeCssIdentifier());
        assertEquals('!', tq.peek());
    }

    @Test
    public void testConsumeAttributeKey() {
        TokenQueue tq = new TokenQueue("data-id-123=val");
        assertEquals("data-id-123", tq.consumeAttributeKey());
        assertEquals('=', tq.peek());
    }

    @Test
    public void testConsumeWhitespace() {
        TokenQueue tq = new TokenQueue("   \t\n\rword");
        assertTrue(tq.consumeWhitespace());
        assertFalse(tq.consumeWhitespace());
        assertEquals('w', tq.peek());
    }

    @Test
    public void testConsumeNormalizedWord() {
        TokenQueue tq = new TokenQueue("  Hello   World  ");
        assertEquals("hello", tq.consumeNormalizedWord());
    }

    @Test
    public void testMatchesWhitespace() {
        TokenQueue tq = new TokenQueue(" a");
        assertTrue(tq.matchesWhitespace());
        tq.consume();
        assertFalse(tq.matchesWhitespace());
    }

    @Test
    public void testMatchesStart() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesStart());
    }

    @Test
    public void testRemainder() {
        TokenQueue tq = new TokenQueue("abc");
        tq.consume();
        assertEquals("bc", tq.remainder());
    }
}