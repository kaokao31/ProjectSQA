package com.example.tokenbuffer; // Placeholder package; adjust to match actual source

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for TokenBuffer.
 * Designed to achieve high line/branch coverage and fault detection.
 */
public class TokenBufferTest {

    private TokenBuffer buffer;

    @Before
    public void setUp() {
        buffer = new TokenBuffer();
    }

    // ==================== Constructor and Initial State ====================

    @Test
    public void testInitialStateIsEmpty() {
        assertTrue("Buffer should be empty initially", buffer.isEmpty());
        assertEquals("Size should be 0 initially", 0, buffer.size());
    }

    // ==================== addToken() ====================

    @Test
    public void testAddSingleToken() {
        buffer.addToken("token1");
        assertFalse("Buffer should not be empty after add", buffer.isEmpty());
        assertEquals("Size should be 1", 1, buffer.size());
    }

    @Test
    public void testAddMultipleTokens() {
        buffer.addToken("a");
        buffer.addToken("b");
        buffer.addToken("c");
        assertEquals("Size should be 3", 3, buffer.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullToken() {
        buffer.addToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddEmptyToken() {
        buffer.addToken("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddWhitespaceToken() {
        buffer.addToken("   ");
    }

    // ==================== getToken() ====================

    @Test
    public void testGetTokenAtValidIndex() {
        buffer.addToken("first");
        buffer.addToken("second");
        assertEquals("first", buffer.getToken(0));
        assertEquals("second", buffer.getToken(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTokenNegativeIndex() {
        buffer.addToken("x");
        buffer.getToken(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTokenIndexEqualToSize() {
        buffer.addToken("x");
        buffer.getToken(1); // size=1, valid indices 0 only
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTokenIndexGreaterThanSize() {
        buffer.addToken("x");
        buffer.getToken(5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetTokenFromEmptyBuffer() {
        buffer.getToken(0);
    }

    // ==================== removeToken() ====================

    @Test
    public void testRemoveTokenFromFront() {
        buffer.addToken("a");
        buffer.addToken("b");
        String removed = buffer.removeToken(0);
        assertEquals("a", removed);
        assertEquals(1, buffer.size());
        assertEquals("b", buffer.getToken(0));
    }

    @Test
    public void testRemoveTokenFromMiddle() {
        buffer.addToken("a");
        buffer.addToken("b");
        buffer.addToken("c");
        String removed = buffer.removeToken(1);
        assertEquals("b", removed);
        assertEquals(2, buffer.size());
        assertEquals("a", buffer.getToken(0));
        assertEquals("c", buffer.getToken(1));
    }

    @Test
    public void testRemoveTokenFromEnd() {
        buffer.addToken("a");
        buffer.addToken("b");
        String removed = buffer.removeToken(1);
        assertEquals("b", removed);
        assertEquals(1, buffer.size());
        assertEquals("a", buffer.getToken(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveTokenNegativeIndex() {
        buffer.addToken("x");
        buffer.removeToken(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveTokenIndexEqualToSize() {
        buffer.addToken("x");
        buffer.removeToken(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveTokenFromEmptyBuffer() {
        buffer.removeToken(0);
    }

    // ==================== clear() ====================

    @Test
    public void testClearNonEmptyBuffer() {
        buffer.addToken("a");
        buffer.addToken("b");
        buffer.clear();
        assertTrue("Buffer should be empty after clear", buffer.isEmpty());
        assertEquals("Size should be 0 after clear", 0, buffer.size());
    }

    @Test
    public void testClearEmptyBuffer() {
        buffer.clear();
        assertTrue("Clearing empty buffer should remain empty", buffer.isEmpty());
    }

    // ==================== contains() ====================

    @Test
    public void testContainsExistingToken() {
        buffer.addToken("hello");
        assertTrue("Should contain 'hello'", buffer.contains("hello"));
    }

    @Test
    public void testContainsNonExistingToken() {
        buffer.addToken("hello");
        assertFalse("Should not contain 'world'", buffer.contains("world"));
    }

    @Test
    public void testContainsOnEmptyBuffer() {
        assertFalse("Empty buffer should not contain anything", buffer.contains("anything"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testContainsNullToken() {
        buffer.contains(null);
    }

    // ==================== toArray() ====================

    @Test
    public void testToArrayNonEmpty() {
        buffer.addToken("x");
        buffer.addToken("y");
        String[] expected = {"x", "y"};
        assertArrayEquals(expected, buffer.toArray());
    }

    @Test
    public void testToArrayEmpty() {
        String[] arr = buffer.toArray();
        assertNotNull("Array should not be null", arr);
        assertEquals("Array length should be 0", 0, arr.length);
    }

    // ==================== iterator() ====================

    @Test
    public void testIteratorIteratesInOrder() {
        buffer.addToken("a");
        buffer.addToken("b");
        buffer.addToken("c");
        java.util.Iterator<String> it = buffer.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertTrue(it.hasNext());
        assertEquals("b", it.next());
        assertTrue(it.hasNext());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testIteratorNextOnEmpty() {
        buffer.iterator().next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemoveNotSupported() {
        buffer.addToken("x");
        java.util.Iterator<String> it = buffer.iterator();
        it.next();
        it.remove(); // Should throw UnsupportedOperationException
    }

    // ==================== equals() and hashCode() ====================

    @Test
    public void testEqualsSameContent() {
        TokenBuffer b1 = new TokenBuffer();
        TokenBuffer b2 = new TokenBuffer();
        b1.addToken("a");
        b1.addToken("b");
        b2.addToken("a");
        b2.addToken("b");
        assertTrue("Buffers with same content should be equal", b1.equals(b2));
        assertEquals("Hash codes should be equal", b1.hashCode(), b2.hashCode());
    }

    @Test
    public void testEqualsDifferentContent() {
        TokenBuffer b1 = new TokenBuffer();
        TokenBuffer b2 = new TokenBuffer();
        b1.addToken("a");
        b2.addToken("b");
        assertFalse("Buffers with different content should not be equal", b1.equals(b2));
    }

    @Test
    public void testEqualsWithNull() {
        assertFalse("Buffer should not equal null", buffer.equals(null));
    }

    @Test
    public void testEqualsWithDifferentType() {
        assertFalse("Buffer should not equal a String", buffer.equals("some string"));
    }

    // ==================== Edge Cases and Bug Triggers ====================

    @Test
    public void testLargeNumberOfTokens() {
        for (int i = 0; i < 1000; i++) {
            buffer.addToken("token" + i);
        }
        assertEquals(1000, buffer.size());
        assertEquals("token0", buffer.getToken(0));
        assertEquals("token999", buffer.getToken(999));
    }

    @Test
    public void testRemoveAllTokensOneByOne() {
        buffer.addToken("a");
        buffer.addToken("b");
        buffer.addToken("c");
        buffer.removeToken(0);
        buffer.removeToken(0);
        buffer.removeToken(0);
        assertTrue("Buffer should be empty after removing all", buffer.isEmpty());
    }

    @Test
    public void testAddAfterRemove() {
        buffer.addToken("a");
        buffer.removeToken(0);
        buffer.addToken("b");
        assertEquals(1, buffer.size());
        assertEquals("b", buffer.getToken(0));
    }

    // Potential bug: off-by-one in internal array resizing
    @Test
    public void testResizeBoundary() {
        // Assuming default capacity is 10, add 11 tokens to trigger resize
        for (int i = 0; i < 11; i++) {
            buffer.addToken("t" + i);
        }
        assertEquals(11, buffer.size());
        assertEquals("t10", buffer.getToken(10));
    }

    // Potential bug: negative capacity or overflow
    @Test
    public void testAddAndRemoveManyTimes() {
        for (int i = 0; i < 100; i++) {
            buffer.addToken("x");
            buffer.removeToken(0);
        }
        assertTrue("Buffer should be empty after many add/remove cycles", buffer.isEmpty());
    }

    // ==================== toString() ====================

    @Test
    public void testToStringEmpty() {
        assertEquals("[]", buffer.toString());
    }

    @Test
    public void testToStringNonEmpty() {
        buffer.addToken("a");
        buffer.addToken("b");
        assertEquals("[a, b]", buffer.toString());
    }
}