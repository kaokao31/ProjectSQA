package com.example;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for TextBuffer class.
 * Assumes a typical mutable character sequence implementation with methods:
 * append(String), append(char), insert(int, String), delete(int, int),
 * charAt(int), length(), substring(int, int), toString(), clear(), etc.
 * Tests cover edge cases, boundary conditions, null handling, and potential bugs.
 */
public class TextBufferTest {

    private TextBuffer buffer;

    @Before
    public void setUp() {
        buffer = new TextBuffer();
    }

    // ========== length() ==========
    @Test
    public void testLengthEmpty() {
        assertEquals(0, buffer.length());
    }

    @Test
    public void testLengthAfterAppend() {
        buffer.append("hello");
        assertEquals(5, buffer.length());
    }

    // ========== append(String) ==========
    @Test
    public void testAppendNullString() {
        buffer.append((String) null);
        assertEquals(0, buffer.length()); // assuming null is ignored or treated as empty
    }

    @Test
    public void testAppendEmptyString() {
        buffer.append("");
        assertEquals(0, buffer.length());
    }

    @Test
    public void testAppendSingleCharString() {
        buffer.append("a");
        assertEquals(1, buffer.length());
        assertEquals('a', buffer.charAt(0));
    }

    @Test
    public void testAppendMultipleStrings() {
        buffer.append("abc");
        buffer.append("def");
        assertEquals("abcdef", buffer.toString());
    }

    // ========== append(char) ==========
    @Test
    public void testAppendChar() {
        buffer.append('x');
        assertEquals(1, buffer.length());
        assertEquals('x', buffer.charAt(0));
    }

    @Test
    public void testAppendMultipleChars() {
        buffer.append('a');
        buffer.append('b');
        buffer.append('c');
        assertEquals("abc", buffer.toString());
    }

    // ========== charAt(int) ==========
    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtNegativeIndex() {
        buffer.append("test");
        buffer.charAt(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtIndexEqualToLength() {
        buffer.append("test");
        buffer.charAt(4);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testCharAtEmptyBuffer() {
        buffer.charAt(0);
    }

    @Test
    public void testCharAtValidIndex() {
        buffer.append("hello");
        assertEquals('h', buffer.charAt(0));
        assertEquals('e', buffer.charAt(1));
        assertEquals('l', buffer.charAt(2));
        assertEquals('l', buffer.charAt(3));
        assertEquals('o', buffer.charAt(4));
    }

    // ========== insert(int, String) ==========
    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertNegativeIndex() {
        buffer.append("world");
        buffer.insert(-1, "hello");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertIndexGreaterThanLength() {
        buffer.append("abc");
        buffer.insert(4, "d");
    }

    @Test
    public void testInsertAtBeginning() {
        buffer.append("world");
        buffer.insert(0, "hello ");
        assertEquals("hello world", buffer.toString());
    }

    @Test
    public void testInsertAtEnd() {
        buffer.append("hello");
        buffer.insert(5, " world");
        assertEquals("hello world", buffer.toString());
    }

    @Test
    public void testInsertInMiddle() {
        buffer.append("helorld");
        buffer.insert(3, "lo w");
        assertEquals("hello world", buffer.toString());
    }

    @Test
    public void testInsertEmptyString() {
        buffer.append("abc");
        buffer.insert(1, "");
        assertEquals("abc", buffer.toString());
    }

    @Test
    public void testInsertNullString() {
        buffer.append("abc");
        buffer.insert(1, null);
        assertEquals("abc", buffer.toString()); // assuming null is ignored
    }

    // ========== delete(int, int) ==========
    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteNegativeStart() {
        buffer.append("hello");
        buffer.delete(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteEndGreaterThanLength() {
        buffer.append("hello");
        buffer.delete(1, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteStartGreaterThanEnd() {
        buffer.append("hello");
        buffer.delete(3, 1);
    }

    @Test
    public void testDeleteEntireBuffer() {
        buffer.append("hello");
        buffer.delete(0, 5);
        assertEquals(0, buffer.length());
        assertEquals("", buffer.toString());
    }

    @Test
    public void testDeletePartial() {
        buffer.append("hello world");
        buffer.delete(5, 11); // remove " world"
        assertEquals("hello", buffer.toString());
    }

    @Test
    public void testDeleteEmptyRange() {
        buffer.append("abc");
        buffer.delete(1, 1); // no removal
        assertEquals("abc", buffer.toString());
    }

    // ========== substring(int, int) ==========
    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringNegativeStart() {
        buffer.append("hello");
        buffer.substring(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringEndGreaterThanLength() {
        buffer.append("hello");
        buffer.substring(1, 10);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubstringStartGreaterThanEnd() {
        buffer.append("hello");
        buffer.substring(3, 1);
    }

    @Test
    public void testSubstringValid() {
        buffer.append("hello world");
        assertEquals("hello", buffer.substring(0, 5));
        assertEquals("world", buffer.substring(6, 11));
        assertEquals(" ", buffer.substring(5, 6));
    }

    @Test
    public void testSubstringEmptyRange() {
        buffer.append("abc");
        assertEquals("", buffer.substring(1, 1));
    }

    // ========== toString() ==========
    @Test
    public void testToStringEmpty() {
        assertEquals("", buffer.toString());
    }

    @Test
    public void testToStringAfterOperations() {
        buffer.append("abc");
        buffer.insert(3, "def");
        buffer.delete(0, 3);
        assertEquals("def", buffer.toString());
    }

    // ========== clear() ==========
    @Test
    public void testClear() {
        buffer.append("some text");
        buffer.clear();
        assertEquals(0, buffer.length());
        assertEquals("", buffer.toString());
    }

    // ========== Edge cases and potential bugs ==========
    @Test
    public void testAppendAfterClear() {
        buffer.append("first");
        buffer.clear();
        buffer.append("second");
        assertEquals("second", buffer.toString());
    }

    @Test
    public void testInsertAtZeroLength() {
        buffer.insert(0, "hello");
        assertEquals("hello", buffer.toString());
    }

    @Test
    public void testDeleteAllThenAppend() {
        buffer.append("temp");
        buffer.delete(0, buffer.length());
        buffer.append("new");
        assertEquals("new", buffer.toString());
    }

    @Test
    public void testLargeAppend() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        buffer.append(sb.toString());
        assertEquals(1000, buffer.length());
        assertEquals('a', buffer.charAt(999));
    }

    @Test
    public void testInsertAtBoundary() {
        buffer.append("ab");
        buffer.insert(2, "c"); // at end
        assertEquals("abc", buffer.toString());
        buffer.insert(0, "z"); // at start
        assertEquals("zabc", buffer.toString());
    }

    @Test
    public void testDeleteAtBoundary() {
        buffer.append("abcd");
        buffer.delete(0, 1); // remove first char
        assertEquals("bcd", buffer.toString());
        buffer.delete(buffer.length() - 1, buffer.length()); // remove last char
        assertEquals("bc", buffer.toString());
    }

    @Test
    public void testCharAtAfterInsert() {
        buffer.append("abc");
        buffer.insert(1, "xyz");
        assertEquals('a', buffer.charAt(0));
        assertEquals('x', buffer.charAt(1));
        assertEquals('y', buffer.charAt(2));
        assertEquals('z', buffer.charAt(3));
        assertEquals('b', buffer.charAt(4));
        assertEquals('c', buffer.charAt(5));
    }

    @Test
    public void testMultipleOperationsConsistency() {
        buffer.append("Hello");
        buffer.append(' ');
        buffer.append("World");
        buffer.insert(5, "Beautiful ");
        buffer.delete(6, 16); // remove "Beautiful "
        assertEquals("Hello World", buffer.toString());
    }

    // ========== Null handling for append(String) ==========
    @Test
    public void testAppendNullDoesNotChangeState() {
        buffer.append("initial");
        buffer.append((String) null);
        assertEquals("initial", buffer.toString());
    }

    // ========== Insert with null ==========
    @Test
    public void testInsertNullDoesNotChangeState() {
        buffer.append("abc");
        buffer.insert(1, null);
        assertEquals("abc", buffer.toString());
    }
}