package org.jsoup.parser;

import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for CharacterReader.
 * Targets maximum coverage and fault detection (including Defects4J bug 18).
 */
public class CharacterReaderTest {

    private CharacterReader reader;

    @Before
    public void setUp() {
        // Default empty reader for tests that need fresh state
        reader = new CharacterReader("");
    }

    // ======================== Constructor Tests ========================

    @Test(expected = NullPointerException.class)
    public void constructorNullStringThrows() {
        new CharacterReader((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void constructorNullReaderThrows() {
        new CharacterReader((java.io.Reader) null);
    }

    @Test
    public void constructorEmptyString() {
        CharacterReader r = new CharacterReader("");
        assertTrue(r.isEmpty());
        assertEquals(0, r.pos());
    }

    @Test
    public void constructorStringWithContent() {
        CharacterReader r = new CharacterReader("abc");
        assertFalse(r.isEmpty());
        assertEquals('a', r.current());
    }

    @Test
    public void constructorReaderWithContent() {
        CharacterReader r = new CharacterReader(new StringReader("xyz"));
        assertFalse(r.isEmpty());
        assertEquals('x', r.current());
    }

    // ======================== Basic Reading ========================

    @Test
    public void posInitialZero() {
        assertEquals(0, reader.pos());
    }

    @Test
    public void isEmptyOnEmptyReader() {
        assertTrue(reader.isEmpty());
    }

    @Test
    public void isEmptyOnNonEmptyReader() {
        reader = new CharacterReader("a");
        assertFalse(reader.isEmpty());
    }

    @Test
    public void currentOnEmptyReader() {
        assertEquals(CharacterReader.EOF, reader.current());
    }

    @Test
    public void currentOnNonEmpty() {
        reader = new CharacterReader("hello");
        assertEquals('h', reader.current());
    }

    @Test
    public void consumeOnEmptyReader() {
        assertEquals(CharacterReader.EOF, reader.consume());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeSingleChar() {
        reader = new CharacterReader("a");
        assertEquals('a', reader.consume());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeMultipleChars() {
        reader = new CharacterReader("ab");
        assertEquals('a', reader.consume());
        assertEquals('b', reader.consume());
        assertTrue(reader.isEmpty());
    }

    @Test
    public void unconsumeAfterConsume() {
        reader = new CharacterReader("x");
        reader.consume();
        reader.unconsume();
        assertFalse(reader.isEmpty());
        assertEquals('x', reader.current());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void unconsumeAtStartThrows() {
        reader.unconsume(); // pos is 0, cannot go back
    }

    @Test
    public void advanceOnNonEmpty() {
        reader = new CharacterReader("abc");
        reader.advance();
        assertEquals('b', reader.current());
        reader.advance();
        assertEquals('c', reader.current());
        reader.advance();
        assertTrue(reader.isEmpty());
    }

    @Test
    public void advanceOnEmptyDoesNothing() {
        reader.advance(); // should not throw, but remain empty
        assertTrue(reader.isEmpty());
    }

    // ======================== Mark / Rewind ========================

    @Test
    public void markAndRewindToMark() {
        reader = new CharacterReader("abcdef");
        reader.consume(); // a
        reader.consume(); // b
        reader.mark();
        reader.consume(); // c
        reader.consume(); // d
        reader.rewindToMark();
        assertEquals('c', reader.current());
        reader.consume(); // c
        assertEquals('d', reader.consume());
    }

    @Test
    public void rewindToMarkWithoutMarkDoesNothing() {
        reader = new CharacterReader("abc");
        reader.consume(); // a
        reader.rewindToMark(); // no mark set, should stay at pos 1
        assertEquals('b', reader.current());
    }

    // ======================== consumeTo(char) ========================

    @Test
    public void consumeToCharNotFound() {
        reader = new CharacterReader("hello");
        String consumed = reader.consumeTo('z');
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToCharAtStart() {
        reader = new CharacterReader("abc");
        String consumed = reader.consumeTo('a');
        assertEquals("", consumed);
        assertEquals('a', reader.current());
    }

    @Test
    public void consumeToCharInMiddle() {
        reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo('d');
        assertEquals("abc", consumed);
        assertEquals('d', reader.current());
    }

    @Test
    public void consumeToCharAtEnd() {
        reader = new CharacterReader("abcdef");
        String consumed = reader.consumeTo('f');
        assertEquals("abcde", consumed);
        assertEquals('f', reader.current());
    }

    @Test
    public void consumeToCharOnEmptyReader() {
        String consumed = reader.consumeTo('a');
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // ======================== consumeTo(String) ========================
    // This is the area of Defects4J bug 18: caching issue when string not found.

    @Test
    public void consumeToStringNotFound() {
        reader = new CharacterReader("hello world");
        String consumed = reader.consumeTo("xyz");
        assertEquals("hello world", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToStringAtStart() {
        reader = new CharacterReader("abc");
        String consumed = reader.consumeTo("abc");
        assertEquals("", consumed);
        assertEquals('a', reader.current()); // should not consume the string itself
    }

    @Test
    public void consumeToStringInMiddle() {
        reader = new CharacterReader("prefix_target_suffix");
        String consumed = reader.consumeTo("target");
        assertEquals("prefix_", consumed);
        assertEquals('t', reader.current());
    }

    @Test
    public void consumeToStringAtEnd() {
        reader = new CharacterReader("prefix_target");
        String consumed = reader.consumeTo("target");
        assertEquals("prefix_", consumed);
        assertEquals('t', reader.current());
    }

    @Test
    public void consumeToStringEmptyString() {
        reader = new CharacterReader("abc");
        String consumed = reader.consumeTo("");
        assertEquals("", consumed);
        assertEquals('a', reader.current());
    }

    @Test
    public void consumeToStringOnEmptyReader() {
        String consumed = reader.consumeTo("abc");
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // Bug-specific: after consumeTo with string not found, subsequent consumeTo works.
    @Test
    public void consumeToStringNotFoundThenFound() {
        reader = new CharacterReader("abcdef");
        reader.consumeTo("xyz"); // consumes all, reader empty
        assertTrue(reader.isEmpty());
        // Now re-create reader to test sequence
        reader = new CharacterReader("abcdef");
        reader.consumeTo("xyz"); // not found, consumes all
        // After that, consumeTo another string should still work (reader empty)
        String s = reader.consumeTo("a");
        assertEquals("", s);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToStringFoundAfterPreviousConsume() {
        reader = new CharacterReader("abcde");
        reader.consumeTo('c'); // consumes "ab", pos at 'c'
        String s = reader.consumeTo("de");
        assertEquals("c", s); // consumes up to but not including "de"
        assertEquals('d', reader.current());
    }

    // ======================== consumeToAny ========================

    @Test
    public void consumeToAnyNotFound() {
        reader = new CharacterReader("hello");
        String consumed = reader.consumeToAny('x', 'y', 'z');
        assertEquals("hello", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToAnyFound() {
        reader = new CharacterReader("abc123");
        String consumed = reader.consumeToAny('1', '2', '3');
        assertEquals("abc", consumed);
        assertEquals('1', reader.current());
    }

    @Test
    public void consumeToAnyEmptyReader() {
        String consumed = reader.consumeToAny('a', 'b');
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    // ======================== consumeToEnd ========================

    @Test
    public void consumeToEndNonEmpty() {
        reader = new CharacterReader("test");
        String consumed = reader.consumeToEnd();
        assertEquals("test", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToEndEmpty() {
        String consumed = reader.consumeToEnd();
        assertEquals("", consumed);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToEndAfterPartialConsume() {
        reader = new CharacterReader("hello");
        reader.consumeTo('l'); // consumes "he", pos at 'l'
        String rest = reader.consumeToEnd();
        assertEquals("llo", rest);
        assertTrue(reader.isEmpty());
    }

    // ======================== matches(char) ========================

    @Test
    public void matchesCharTrue() {
        reader = new CharacterReader("a");
        assertTrue(reader.matches('a'));
    }

    @Test
    public void matchesCharFalse() {
        reader = new CharacterReader("a");
        assertFalse(reader.matches('b'));
    }

    @Test
    public void matchesCharOnEmpty() {
        assertFalse(reader.matches('a'));
    }

    // ======================== matches(String) ========================

    @Test
    public void matchesStringExact() {
        reader = new CharacterReader("abc");
        assertTrue(reader.matches("abc"));
    }

    @Test
    public void matchesStringPartial() {
        reader = new CharacterReader("abcd");
        assertTrue(reader.matches("abc"));
    }

    @Test
    public void matchesStringLongerThanInput() {
        reader = new CharacterReader("ab");
        assertFalse(reader.matches("abc"));
    }

    @Test
    public void matchesStringEmpty() {
        reader = new CharacterReader("abc");
        assertTrue(reader.matches(""));
    }

    @Test
    public void matchesStringOnEmptyReader() {
        assertFalse(reader.matches("a"));
    }

    // ======================== matchesAny ========================

    @Test
    public void matchesAnyTrue() {
        reader = new CharacterReader("a");
        assertTrue(reader.matchesAny('a', 'b', 'c'));
    }

    @Test
    public void matchesAnyFalse() {
        reader = new CharacterReader("d");
        assertFalse(reader.matchesAny('a', 'b', 'c'));
    }

    @Test
    public void matchesAnyOnEmpty() {
        assertFalse(reader.matchesAny('a'));
    }

    // ======================== matchConsume(String) ========================

    @Test
    public void matchConsumeExact() {
        reader = new CharacterReader("abc");
        assertTrue(reader.matchConsume("abc"));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void matchConsumePartial() {
        reader = new CharacterReader("abcdef");
        assertTrue(reader.matchConsume("abc"));
        assertEquals('d', reader.current());
    }

    @Test
    public void matchConsumeFalse() {
        reader = new CharacterReader("abc");
        assertFalse(reader.matchConsume("abd"));
        assertEquals('a', reader.current()); // position unchanged
    }

    @Test
    public void matchConsumeEmptyString() {
        reader = new CharacterReader("abc");
        assertTrue(reader.matchConsume(""));
        assertEquals('a', reader.current());
    }

    @Test
    public void matchConsumeOnEmptyReader() {
        assertFalse(reader.matchConsume("a"));
    }

    // ======================== matchConsumeIgnoreCase ========================

    @Test
    public void matchConsumeIgnoreCaseExact() {
        reader = new CharacterReader("ABC");
        assertTrue(reader.matchConsumeIgnoreCase("abc"));
        assertTrue(reader.isEmpty());
    }

    @Test
    public void matchConsumeIgnoreCasePartial() {
        reader = new CharacterReader("AbCdEf");
        assertTrue(reader.matchConsumeIgnoreCase("abc"));
        assertEquals('d', reader.current());
    }

    @Test
    public void matchConsumeIgnoreCaseFalse() {
        reader = new CharacterReader("abc");
        assertFalse(reader.matchConsumeIgnoreCase("abd"));
        assertEquals('a', reader.current());
    }

    // ======================== matchesIgnoreCase ========================

    @Test
    public void matchesIgnoreCaseTrue() {
        reader = new CharacterReader("Hello");
        assertTrue(reader.matchesIgnoreCase("hello"));
    }

    @Test
    public void matchesIgnoreCaseFalse() {
        reader = new CharacterReader("Hello");
        assertFalse(reader.matchesIgnoreCase("world"));
    }

    @Test
    public void matchesIgnoreCaseOnEmpty() {
        assertFalse(reader.matchesIgnoreCase("a"));
    }

    // ======================== containsIgnoreCase ========================

    @Test
    public void containsIgnoreCaseTrue() {
        reader = new CharacterReader("Hello World");
        assertTrue(reader.containsIgnoreCase("world"));
    }

    @Test
    public void containsIgnoreCaseFalse() {
        reader = new CharacterReader("Hello");
        assertFalse(reader.containsIgnoreCase("world"));
    }

    @Test
    public void containsIgnoreCaseOnEmpty() {
        assertFalse(reader.containsIgnoreCase("a"));
    }

    // ======================== toString ========================

    @Test
    public void toStringFullInput() {
        reader = new CharacterReader("test");
        assertEquals("test", reader.toString());
    }

    @Test
    public void toStringAfterPartialConsume() {
        reader = new CharacterReader("hello");
        reader.consumeTo('l');
        assertEquals("llo", reader.toString());
    }

    @Test
    public void toStringEmpty() {
        assertEquals("", reader.toString());
    }

    // ======================== Edge Cases and Bug Triggers ========================

    @Test
    public void consumeToWithStringThatIsPrefixOfAnother() {
        // Ensure caching doesn't interfere
        reader = new CharacterReader("ababc");
        String s = reader.consumeTo("abc");
        assertEquals("ab", s);
        assertEquals('a', reader.current()); // should be at start of "abc"
    }

    @Test
    public void multipleConsumeToWithSameString() {
        reader = new CharacterReader("abcabc");
        String first = reader.consumeTo("abc");
        assertEquals("", first);
        // now at 'a' of second "abc"
        String second = reader.consumeTo("abc");
        assertEquals("", second);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToWithStringThatNeverAppearsAfterCache() {
        // Bug 18: after a consumeTo that doesn't find the string, the cache may cause
        // subsequent consumeTo to behave incorrectly. This test ensures that after
        // a failed consumeTo, a new consumeTo with a different string works.
        reader = new CharacterReader("abcdef");
        reader.consumeTo("xyz"); // not found, consumes all
        // Now reader is empty. Create a new reader for a fresh test.
        reader = new CharacterReader("abcdef");
        reader.consumeTo("xyz"); // not found, consumes all
        // Now try to consumeTo "a" - should return empty because reader is empty
        String s = reader.consumeTo("a");
        assertEquals("", s);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void consumeToWithStringAfterPartialConsume() {
        reader = new CharacterReader("a_b_c");
        reader.consumeTo('_'); // consumes "a", pos at '_'
        String s = reader.consumeTo("_"); // should consume "_b" up to next '_'
        assertEquals("_b", s);
        assertEquals('_', reader.current());
    }

    @Test
    public void matchConsumeWithStringLongerThanRemaining() {
        reader = new CharacterReader("ab");
        assertFalse(reader.matchConsume("abc"));
        assertEquals('a', reader.current());
    }

    @Test
    public void matchConsumeIgnoreCaseWithStringLongerThanRemaining() {
        reader = new CharacterReader("AB");
        assertFalse(reader.matchConsumeIgnoreCase("abc"));
        assertEquals('A', reader.current());
    }

    @Test
    public void consumeToAnyWithMultipleCharsAtEnd() {
        reader = new CharacterReader("hello!");
        String s = reader.consumeToAny('!', '.');
        assertEquals("hello", s);
        assertEquals('!', reader.current());
    }

    @Test
    public void consumeToAnyWithNoMatchAndEmptyInput() {
        String s = reader.consumeToAny('a', 'b');
        assertEquals("", s);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void markRewindAfterConsumeTo() {
        reader = new CharacterReader("abcdef");
        reader.consumeTo('c'); // consumes "ab", pos at 'c'
        reader.mark();
        reader.consumeTo('e'); // consumes "cd", pos at 'e'
        reader.rewindToMark();
        assertEquals('c', reader.current());
        reader.consume(); // c
        assertEquals('d', reader.consume());
    }

    @Test
    public void largeInputToTestCache() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        String large = sb.toString();
        reader = new CharacterReader(large);
        // consumeTo a string that appears near the end
        String target = "xyz";
        // Insert target at position 5000
        String modified = large.substring(0, 5000) + target + large.substring(5000);
        reader = new CharacterReader(modified);
        String consumed = reader.consumeTo(target);
        assertEquals(5000, consumed.length());
        assertEquals(target.charAt(0), reader.current());
    }

    @Test
    public void consumeToEndAfterConsumeToNotFound() {
        reader = new CharacterReader("test");
        reader.consumeTo("xyz"); // not found, consumes all
        String end = reader.consumeToEnd();
        assertEquals("", end);
        assertTrue(reader.isEmpty());
    }

    @Test
    public void matchesAnyWithEmptyVarargs() {
        // matchesAny with no arguments should return false
        assertFalse(reader.matchesAny());
    }

    @Test
    public void consumeToAnyWithEmptyVarargs() {
        // consumeToAny with no arguments should consume nothing? Actually it will never match, so consume all.
        reader = new CharacterReader("abc");
        String s = reader.consumeToAny();
        assertEquals("abc", s);
        assertTrue(reader.isEmpty());
    }
}