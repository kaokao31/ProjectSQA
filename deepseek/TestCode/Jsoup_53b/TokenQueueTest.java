package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for TokenQueue.
 * Designed to achieve maximum coverage and detect potential faults (including Defects4J bug 53).
 */
public class TokenQueueTest {

    private TokenQueue queue;

    @Before
    public void setUp() {
        // Default empty queue; tests will set specific data
        queue = new TokenQueue("");
    }

    // ======================== Constructor and Basic State ========================

    @Test
    public void testConstructorNullInput() {
        // Should handle null gracefully (likely throws or creates empty)
        try {
            new TokenQueue(null);
            // If no exception, verify it's empty
            assertTrue(new TokenQueue(null).isEmpty());
        } catch (Exception e) {
            // Acceptable if constructor throws on null
            assertTrue(e instanceof IllegalArgumentException || e instanceof NullPointerException);
        }
    }

    @Test
    public void testConstructorEmptyString() {
        TokenQueue tq = new TokenQueue("");
        assertTrue(tq.isEmpty());
        assertEquals(0, tq.remainingLength());
    }

    @Test
    public void testConstructorNonEmpty() {
        TokenQueue tq = new TokenQueue("abc");
        assertFalse(tq.isEmpty());
        assertEquals(3, tq.remainingLength());
    }

    // ======================== isEmpty / remainingLength ========================

    @Test
    public void testIsEmptyAfterConsumeAll() {
        queue = new TokenQueue("test");
        queue.consume("test");
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.remainingLength());
    }

    @Test
    public void testRemainingLengthAfterPartialConsume() {
        queue = new TokenQueue("hello world");
        queue.consume("hello ");
        assertEquals(5, queue.remainingLength());
    }

    // ======================== peek / removeFirst ========================

    @Test
    public void testPeekEmpty() {
        assertEquals(0, queue.peek());
    }

    @Test
    public void testPeekNonEmpty() {
        queue = new TokenQueue("a");
        assertEquals('a', queue.peek());
    }

    @Test
    public void testRemoveFirstEmpty() {
        assertEquals(0, queue.removeFirst());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testRemoveFirstNonEmpty() {
        queue = new TokenQueue("ab");
        assertEquals('a', queue.removeFirst());
        assertEquals('b', queue.peek());
    }

    // ======================== matches ========================

    @Test
    public void testMatchesExact() {
        queue = new TokenQueue("abc");
        assertTrue(queue.matches("abc"));
        assertFalse(queue.matches("ab"));
        assertFalse(queue.matches("abcd"));
    }

    @Test
    public void testMatchesEmptyString() {
        queue = new TokenQueue("abc");
        assertTrue(queue.matches("")); // empty always matches
    }

    @Test
    public void testMatchesNullString() {
        queue = new TokenQueue("abc");
        // Should not throw; treat null as empty or false
        try {
            boolean result = queue.matches(null);
            // If no exception, assert false (null not a valid sequence)
            assertFalse(result);
        } catch (Exception e) {
            // Acceptable if throws
        }
    }

    @Test
    public void testMatchesAtEnd() {
        queue = new TokenQueue("abc");
        queue.consume("abc");
        assertFalse(queue.matches("a"));
    }

    // ======================== matchesAny ========================

    @Test
    public void testMatchesAnySingle() {
        queue = new TokenQueue("xyz");
        assertTrue(queue.matchesAny("x", "y", "z"));
        assertFalse(queue.matchesAny("a", "b"));
    }

    @Test
    public void testMatchesAnyEmptyArray() {
        queue = new TokenQueue("abc");
        assertFalse(queue.matchesAny()); // no sequences
    }

    @Test
    public void testMatchesAnyWithEmptyString() {
        queue = new TokenQueue("abc");
        // Empty string should match at any position
        assertTrue(queue.matchesAny(""));
        assertTrue(queue.matchesAny("", "x"));
    }

    @Test
    public void testMatchesAnyWithNullElement() {
        queue = new TokenQueue("abc");
        // Should handle null gracefully (skip or treat as empty)
        try {
            boolean result = queue.matchesAny("a", null, "b");
            // If no exception, should match "a"
            assertTrue(result);
        } catch (Exception e) {
            // Acceptable
        }
    }

    @Test
    public void testMatchesAnyMultipleMatches() {
        queue = new TokenQueue("hello");
        assertTrue(queue.matchesAny("he", "hell", "hello"));
    }

    @Test
    public void testMatchesAnyNoMatch() {
        queue = new TokenQueue("world");
        assertFalse(queue.matchesAny("hello", "hi"));
    }

    // ======================== matchesStart ========================

    @Test
    public void testMatchesStartTrue() {
        queue = new TokenQueue("abcdef");
        assertTrue(queue.matchesStart("abc"));
    }

    @Test
    public void testMatchesStartFalse() {
        queue = new TokenQueue("abcdef");
        assertFalse(queue.matchesStart("abd"));
    }

    @Test
    public void testMatchesStartEmpty() {
        queue = new TokenQueue("abc");
        assertTrue(queue.matchesStart(""));
    }

    // ======================== matchChomp ========================

    @Test
    public void testMatchChompSuccess() {
        queue = new TokenQueue("test data");
        assertTrue(queue.matchChomp("test"));
        assertEquals(" data", queue.remainder());
    }

    @Test
    public void testMatchChompFail() {
        queue = new TokenQueue("test data");
        assertFalse(queue.matchChomp("tes"));
        assertEquals("test data", queue.remainder());
    }

    @Test
    public void testMatchChompEmpty() {
        queue = new TokenQueue("abc");
        assertTrue(queue.matchChomp(""));
        assertEquals("abc", queue.remainder());
    }

    // ======================== consume ========================

    @Test
    public void testConsumeExact() {
        queue = new TokenQueue("consume");
        queue.consume("consume");
        assertTrue(queue.isEmpty());
    }

    @Test(expected = IllegalStateException.class)
    public void testConsumeMismatch() {
        queue = new TokenQueue("abc");
        queue.consume("abx");
    }

    @Test
    public void testConsumeEmptyString() {
        queue = new TokenQueue("abc");
        queue.consume("");
        assertEquals("abc", queue.remainder());
    }

    // ======================== consumeTo ========================

    @Test
    public void testConsumeToSimple() {
        queue = new TokenQueue("before|after");
        String consumed = queue.consumeTo("|");
        assertEquals("before", consumed);
        assertEquals("|after", queue.remainder());
    }

    @Test
    public void testConsumeToNotFound() {
        queue = new TokenQueue("whole string");
        String consumed = queue.consumeTo("|");
        assertEquals("whole string", consumed);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeToEmptySeq() {
        queue = new TokenQueue("abc");
        String consumed = queue.consumeTo("");
        assertEquals("", consumed);
        assertEquals("abc", queue.remainder());
    }

    @Test
    public void testConsumeToAtStart() {
        queue = new TokenQueue("|abc");
        String consumed = queue.consumeTo("|");
        assertEquals("", consumed);
        assertEquals("|abc", queue.remainder());
    }

    // ======================== consumeToAny ========================

    @Test
    public void testConsumeToAnySingle() {
        queue = new TokenQueue("hello world");
        String consumed = queue.consumeToAny(" ", ",");
        assertEquals("hello", consumed);
        assertEquals(" world", queue.remainder());
    }

    @Test
    public void testConsumeToAnyMultiple() {
        queue = new TokenQueue("a,b.c");
        String consumed = queue.consumeToAny(",", ".");
        assertEquals("a", consumed);
        assertEquals(",b.c", queue.remainder());
    }

    @Test
    public void testConsumeToAnyNotFound() {
        queue = new TokenQueue("abcdef");
        String consumed = queue.consumeToAny("x", "y");
        assertEquals("abcdef", consumed);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeToAnyEmptyArray() {
        queue = new TokenQueue("test");
        String consumed = queue.consumeToAny();
        assertEquals("test", consumed);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeToAnyWithEmptyString() {
        queue = new TokenQueue("abc");
        // Empty string should match immediately
        String consumed = queue.consumeToAny("");
        assertEquals("", consumed);
        assertEquals("abc", queue.remainder());
    }

    @Test
    public void testConsumeToAnyWithNullElement() {
        queue = new TokenQueue("abc");
        // Should handle null gracefully (skip or treat as empty)
        try {
            String consumed = queue.consumeToAny("a", null);
            // If no exception, should consume up to "a"
            assertEquals("", consumed);
            assertEquals("abc", queue.remainder());
        } catch (Exception e) {
            // Acceptable
        }
    }

    // ======================== consumeWhitespace ========================

    @Test
    public void testConsumeWhitespaceNone() {
        queue = new TokenQueue("abc");
        assertFalse(queue.consumeWhitespace());
        assertEquals("abc", queue.remainder());
    }

    @Test
    public void testConsumeWhitespaceLeading() {
        queue = new TokenQueue("   abc");
        assertTrue(queue.consumeWhitespace());
        assertEquals("abc", queue.remainder());
    }

    @Test
    public void testConsumeWhitespaceAll() {
        queue = new TokenQueue(" \t\n ");
        assertTrue(queue.consumeWhitespace());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeWhitespaceMixed() {
        queue = new TokenQueue(" \t abc");
        assertTrue(queue.consumeWhitespace());
        assertEquals("abc", queue.remainder());
    }

    // ======================== consumeWord ========================

    @Test
    public void testConsumeWordSimple() {
        queue = new TokenQueue("hello123");
        String word = queue.consumeWord();
        assertEquals("hello", word);
        assertEquals("123", queue.remainder());
    }

    @Test
    public void testConsumeWordNoLetter() {
        queue = new TokenQueue("123abc");
        String word = queue.consumeWord();
        assertEquals("", word);
        assertEquals("123abc", queue.remainder());
    }

    @Test
    public void testConsumeWordEmpty() {
        queue = new TokenQueue("");
        String word = queue.consumeWord();
        assertEquals("", word);
        assertTrue(queue.isEmpty());
    }

    // ======================== consumeElementSelector ========================

    @Test
    public void testConsumeElementSelectorSimple() {
        queue = new TokenQueue("div.class");
        String sel = queue.consumeElementSelector();
        assertEquals("div", sel);
        assertEquals(".class", queue.remainder());
    }

    @Test
    public void testConsumeElementSelectorWithPseudo() {
        queue = new TokenQueue("a:hover");
        String sel = queue.consumeElementSelector();
        assertEquals("a", sel);
        assertEquals(":hover", queue.remainder());
    }

    @Test
    public void testConsumeElementSelectorEmpty() {
        queue = new TokenQueue("");
        String sel = queue.consumeElementSelector();
        assertEquals("", sel);
    }

    // ======================== consumeAttributeKey ========================

    @Test
    public void testConsumeAttributeKeySimple() {
        queue = new TokenQueue("data-value=123");
        String key = queue.consumeAttributeKey();
        assertEquals("data-value", key);
        assertEquals("=123", queue.remainder());
    }

    @Test
    public void testConsumeAttributeKeyWithSpaces() {
        queue = new TokenQueue("class = \"test\"");
        String key = queue.consumeAttributeKey();
        assertEquals("class", key);
        assertEquals(" = \"test\"", queue.remainder());
    }

    @Test
    public void testConsumeAttributeKeyEmpty() {
        queue = new TokenQueue("");
        String key = queue.consumeAttributeKey();
        assertEquals("", key);
    }

    // ======================== chompBalanced ========================

    @Test
    public void testChompBalancedRoundBrackets() {
        queue = new TokenQueue("(hello)");
        String inner = queue.chompBalanced('(', ')');
        assertEquals("hello", inner);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testChompBalancedNested() {
        queue = new TokenQueue("(outer(inner))");
        String inner = queue.chompBalanced('(', ')');
        assertEquals("outer(inner)", inner);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testChompBalancedWithQuotes() {
        queue = new TokenQueue("(\"quoted\")");
        String inner = queue.chompBalanced('(', ')');
        assertEquals("\"quoted\"", inner);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testChompBalancedUnmatched() {
        queue = new TokenQueue("(abc");
        // Should throw or return partial? Typically throws
        try {
            queue.chompBalanced('(', ')');
            fail("Expected exception for unbalanced brackets");
        } catch (Exception e) {
            // Expected
        }
    }

    @Test
    public void testChompBalancedEmpty() {
        queue = new TokenQueue("()");
        String inner = queue.chompBalanced('(', ')');
        assertEquals("", inner);
        assertTrue(queue.isEmpty());
    }

    // ======================== consumeTagName ========================

    @Test
    public void testConsumeTagNameSimple() {
        queue = new TokenQueue("div.class");
        String name = queue.consumeTagName();
        assertEquals("div", name);
        assertEquals(".class", queue.remainder());
    }

    @Test
    public void testConsumeTagNameWithHyphen() {
        queue = new TokenQueue("my-tag");
        String name = queue.consumeTagName();
        assertEquals("my-tag", name);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeTagNameEmpty() {
        queue = new TokenQueue("");
        String name = queue.consumeTagName();
        assertEquals("", name);
    }

    // ======================== remainder / toString ========================

    @Test
    public void testRemainderFull() {
        queue = new TokenQueue("full string");
        assertEquals("full string", queue.remainder());
    }

    @Test
    public void testRemainderAfterConsume() {
        queue = new TokenQueue("prefix suffix");
        queue.consume("prefix ");
        assertEquals("suffix", queue.remainder());
    }

    @Test
    public void testToString() {
        queue = new TokenQueue("data");
        assertEquals("data", queue.toString());
    }

    // ======================== Edge Cases for Bug 53 ========================

    // Bug 53: Potential issue with consumeToAny when sequence contains empty string or null
    @Test
    public void testConsumeToAnyWithEmptySeqInArray() {
        queue = new TokenQueue("abc");
        // If empty string is in array, should match immediately
        String result = queue.consumeToAny("", "a");
        assertEquals("", result);
        assertEquals("abc", queue.remainder());
    }

    @Test
    public void testConsumeToAnyWithNullInArray() {
        queue = new TokenQueue("abc");
        // Should not throw; treat null as empty or skip
        try {
            String result = queue.consumeToAny("b", null);
            // If null is skipped, should consume up to "b"
            assertEquals("a", result);
            assertEquals("bc", queue.remainder());
        } catch (Exception e) {
            // Acceptable if throws
        }
    }

    @Test
    public void testMatchesAnyWithEmptyAndNull() {
        queue = new TokenQueue("abc");
        // Should handle gracefully
        assertTrue(queue.matchesAny("", null));
    }

    @Test
    public void testConsumeToAnyAtEnd() {
        queue = new TokenQueue("end");
        String result = queue.consumeToAny("d");
        assertEquals("en", result);
        assertEquals("d", queue.remainder());
    }

    @Test
    public void testConsumeToAnyWithSpecialChars() {
        queue = new TokenQueue("a[b]c");
        String result = queue.consumeToAny("[", "]");
        assertEquals("a", result);
        assertEquals("[b]c", queue.remainder());
    }

    // ======================== Additional Coverage ========================

    @Test
    public void testConsumeWhitespaceOnlySpaces() {
        queue = new TokenQueue("   ");
        assertTrue(queue.consumeWhitespace());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeWordWithLeadingWhitespace() {
        queue = new TokenQueue("  word");
        queue.consumeWhitespace();
        String word = queue.consumeWord();
        assertEquals("word", word);
    }

    @Test
    public void testChompBalancedWithSingleChar() {
        queue = new TokenQueue("(a)");
        String inner = queue.chompBalanced('(', ')');
        assertEquals("a", inner);
    }

    @Test
    public void testChompBalancedWithEscapedQuotes() {
        queue = new TokenQueue("(\\\"escaped\")");
        String inner = queue.chompBalanced('(', ')');
        assertEquals("\\\"escaped\"", inner);
    }

    @Test
    public void testConsumeElementSelectorWithMultiple() {
        queue = new TokenQueue("div#id.class");
        String sel = queue.consumeElementSelector();
        assertEquals("div", sel);
        assertEquals("#id.class", queue.remainder());
    }

    @Test
    public void testConsumeAttributeKeyWithSpecialChars() {
        queue = new TokenQueue("data-foo_bar=val");
        String key = queue.consumeAttributeKey();
        assertEquals("data-foo_bar", key);
        assertEquals("=val", queue.remainder());
    }

    @Test
    public void testConsumeTagNameWithDigit() {
        queue = new TokenQueue("h1");
        String name = queue.consumeTagName();
        assertEquals("h1", name);
    }

    @Test
    public void testConsumeToAnyWithMultipleCharsSeq() {
        queue = new TokenQueue("abcdef");
        String result = queue.consumeToAny("cd", "ef");
        assertEquals("ab", result);
        assertEquals("cdef", queue.remainder());
    }

    @Test
    public void testConsumeToAnyWithOverlappingSeq() {
        queue = new TokenQueue("aaaa");
        String result = queue.consumeToAny("aa");
        assertEquals("", result);
        assertEquals("aaaa", queue.remainder());
    }

    @Test
    public void testMatchChompWithMultipleChars() {
        queue = new TokenQueue("chomp");
        assertTrue(queue.matchChomp("cho"));
        assertEquals("mp", queue.remainder());
    }

    @Test
    public void testMatchChompAtEnd() {
        queue = new TokenQueue("end");
        assertTrue(queue.matchChomp("end"));
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testMatchesStartWithPartial() {
        queue = new TokenQueue("partial");
        assertTrue(queue.matchesStart("par"));
        assertFalse(queue.matchesStart("parti"));
    }

    @Test
    public void testConsumeToWithEmptyQueue() {
        queue = new TokenQueue("");
        String result = queue.consumeTo("x");
        assertEquals("", result);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeToAnyWithEmptyQueue() {
        queue = new TokenQueue("");
        String result = queue.consumeToAny("a", "b");
        assertEquals("", result);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeWhitespaceWithNewline() {
        queue = new TokenQueue("\n\r\t");
        assertTrue(queue.consumeWhitespace());
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testConsumeWordWithUnderscore() {
        queue = new TokenQueue("_underscore");
        String word = queue.consumeWord();
        assertEquals("_underscore", word);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testChompBalancedWithSquareBrackets() {
        queue = new TokenQueue("[content]");
        String inner = queue.chompBalanced('[', ']');
        assertEquals("content", inner);
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testChompBalancedWithNestedQuotes() {
        queue = new TokenQueue("(\"inner 'quote'\")");
        String inner = queue.chompBalanced('(', ')');
        assertEquals("\"inner 'quote'\"", inner);
    }

    @Test
    public void testConsumeElementSelectorWithPseudoClass() {
        queue = new TokenQueue("a:visited");
        String sel = queue.consumeElementSelector();
        assertEquals("a", sel);
        assertEquals(":visited", queue.remainder());
    }

    @Test
    public void testConsumeAttributeKeyWithLeadingWhitespace() {
        queue = new TokenQueue("  key=val");
        queue.consumeWhitespace();
        String key = queue.consumeAttributeKey();
        assertEquals("key", key);
        assertEquals("=val", queue.remainder());
    }

    @Test
    public void testConsumeTagNameWithLeadingWhitespace() {
        queue = new TokenQueue("  tag");
        queue.consumeWhitespace();
        String name = queue.consumeTagName();
        assertEquals("tag", name);
    }

    @Test
    public void testRemainingLengthAfterConsumeToAny() {
        queue = new TokenQueue("abc|def");
        queue.consumeToAny("|");
        assertEquals(4, queue.remainingLength());
    }

    @Test
    public void testPeekAfterConsume() {
        queue = new TokenQueue("abc");
        queue.consume("a");
        assertEquals('b', queue.peek());
    }

    @Test
    public void testRemoveFirstMultiple() {
        queue = new TokenQueue("abc");
        assertEquals('a', queue.removeFirst());
        assertEquals('b', queue.removeFirst());
        assertEquals('c', queue.removeFirst());
        assertEquals(0, queue.removeFirst());
    }

    @Test
    public void testMatchesAnyWithLongestMatch() {
        queue = new TokenQueue("longest");
        assertTrue(queue.matchesAny("long", "longer", "longest"));
    }

    @Test
    public void testConsumeToAnyWithEscape() {
        // If TokenQueue handles escape sequences, test it
        queue = new TokenQueue("a\\,b,c");
        String result = queue.consumeToAny(",");
        // Depending on implementation, backslash might escape comma
        // We'll just test basic behavior
        assertEquals("a\\", result);
        assertEquals(",b,c", queue.remainder());
    }
}