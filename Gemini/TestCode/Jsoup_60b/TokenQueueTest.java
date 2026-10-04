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
    public void testIsEmpty() {
        TokenQueue tq = new TokenQueue("");
        assertTrue(tq.isEmpty());
        
        TokenQueue tq2 = new TokenQueue("   ");
        assertTrue(tq2.isEmpty()); // based on matches
        
        TokenQueue tq3 = new TokenQueue("a");
        assertFalse(tq3.isEmpty());
    }

    @Test
    public void testPeek() {
        TokenQueue tq = new TokenQueue("abc");
        assertEquals('a', tq.peek());
        assertEquals('a', tq.consume());
        assertEquals('b', tq.peek());
        
        tq.consumeTo("c");
        tq.consume();
        assertEquals(0, tq.peek());
    }

    @Test
    public void testAddFirstString() {
        TokenQueue tq = new TokenQueue("c");
        tq.addFirst("ab");
        assertEquals("abc", tq.toString());
    }

    @Test
    public void testAddFirstChar() {
        TokenQueue tq = new TokenQueue("bc");
        tq.addFirst('a');
        assertEquals("abc", tq.toString());
    }

    @Test
    public void testMatches() {
        TokenQueue tq = new TokenQueue("Hello World");
        assertTrue(tq.matches("Hello"));
        assertTrue(tq.matches("hello")); // case insensitive check
        assertFalse(tq.matches("World"));
        
        tq.consume("Hello");
        assertTrue(tq.matches(' '));
        assertFalse(tq.matches('H'));
    }

    @Test
    public void testMatchesAny() {
        TokenQueue tq = new TokenQueue("abc");
        assertTrue(tq.matchesAny('x', 'y', 'a'));
        assertFalse(tq.matchesAny('x', 'y', 'z'));
        
        assertTrue(tq.matchesAny(new String[]{"b", "c"}));
        assertFalse(tq.matchesAny(new String[]{"x", "y"}));
    }

    @Test
    public void testMatchChomp() {
        TokenQueue tq = new TokenQueue("abcabc");
        assertTrue(tq.matchChomp("ab"));
        assertEquals("cabc", tq.toString());
        assertFalse(tq.matchChomp("ab"));
    }

    @Test
    public void testAdvance() {
        TokenQueue tq = new TokenQueue("ab");
        tq.advance();
        assertEquals('b', tq.peek());
        
        // advance past end
        tq.advance();
        tq.advance();
        assertEquals(0, tq.peek());
    }

    @Test
    public void testConsume() {
        TokenQueue tq = new TokenQueue("ab");
        assertEquals('a', tq.consume());
        
        tq.consume("b");
        assertTrue(tq.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeExpectedFail() {
        TokenQueue tq = new TokenQueue("ab");
        tq.consume("c");
    }

    @Test
    public void testConsumeTo() {
        TokenQueue tq = new TokenQueue("one,two,three");
        assertEquals("one", tq.consumeTo(","));
        assertEquals(",", tq.consume());
        assertEquals("two", tq.consumeTo(","));
        
        // Consume to non-existent
        TokenQueue tq2 = new TokenQueue("abc");
        assertEquals("abc", tq2.consumeTo("xyz"));
    }

    @Test
    public void testConsumeToIgnoreCase() {
        TokenQueue tq = new TokenQueue("One<BR>Two");
        assertEquals("One", tq.consumeToIgnoreCase("<br>"));
        assertEquals("<BR>", tq.consumeTo(">"));
        assertEquals(">", tq.consume());
        assertEquals("Two", tq.consumeToIgnoreCase("none"));
    }

    @Test
    public void testConsumeToAny() {
        TokenQueue tq = new TokenQueue("one-two_three");
        assertEquals("one", tq.consumeToAny("-", "_"));
    }

    @Test
    public void testChompTo() {
        TokenQueue tq = new TokenQueue("one<br>two");
        assertEquals("one", tq.chompTo("<br>"));
        assertEquals("two", tq.toString());
        
        // Not present
        TokenQueue tq2 = new TokenQueue("onetwo");
        assertEquals("onetwo", tq2.chompTo("<br>"));
    }

    @Test
    public void testChompToIgnoreCase() {
        TokenQueue tq = new TokenQueue("one<BR>two");
        assertEquals("one", tq.chompToIgnoreCase("<br>"));
        assertEquals("two", tq.toString());
    }

    @Test
    public void testRStrip() {
        TokenQueue tq = new TokenQueue("   abc   ");
        tq.rStrip();
        assertEquals("   abc", tq.toString());
    }

    @Test
    public void testConsumeWord() {
        TokenQueue tq = new TokenQueue("abc-123_xyz !");
        assertEquals("abc-123_xyz", tq.consumeWord());
    }

    @Test
    public void testConsumeTagName() {
        TokenQueue tq = new TokenQueue("div#id.class");
        assertEquals("div#id.class", tq.consumeTagName());
    }

    @Test
    public void testConsumeElementSelector() {
        TokenQueue tq = new TokenQueue("div.class#id[attr]");
        assertEquals("div.class#id[attr]", tq.consumeElementSelector());
    }

    @Test
    public void testConsumeCssIdentifier() {
        TokenQueue tq = new TokenQueue("my-class_name rest");
        assertEquals("my-class_name", tq.consumeCssIdentifier());
    }

    @Test
    public void testConsumeAttributeKey() {
        TokenQueue tq = new TokenQueue("data-id-123=val");
        assertEquals("data-id-123", tq.consumeAttributeKey());
    }

    @Test
    public void testRemainder() {
        TokenQueue tq = new TokenQueue("abcdef");
        tq.consume();
        tq.consume();
        assertEquals("cdef", tq.remainder());
    }

    @Test
    public void testUnescape() {
        assertEquals("a\\b", TokenQueue.unescape("a\\\\b"));
    }

    @Test
    public void testBalanced() {
        TokenQueue tq = new TokenQueue("a(b(c)d)e");
        tq.consumeTo("(");
        assertEquals("b(c)d", tq.chompBalanced('(', ')'));
        
        // Test escaped quotes and braces inside balanced
        TokenQueue tq2 = new TokenQueue("(')')");
        tq2.consumeTo("(");
        assertEquals("')", tq2.chompBalanced('(', ')'));
        
        // Test unbalance / exhausted
        TokenQueue tq3 = new TokenQueue("(abc");
        tq3.consumeTo("(");
        assertEquals("abc", tq3.chompBalanced('(', ')'));
        
        // Test open and close same
        TokenQueue tq4 = new TokenQueue("\"abc\"xyz");
        tq4.consume(); // quote
        assertEquals("abc", tq4.chompBalanced('"', '"'));
    }
}