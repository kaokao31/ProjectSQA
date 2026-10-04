package org.apache.commons.collections4.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.BoundedCollection;
import org.apache.commons.collections4.collection.UnmodifiableBoundedCollection;
import org.junit.Before;
import org.junit.Test;

public class UnmodifiableBoundedCollectionTest {

    private List<String> backingList;
    private UnmodifiableBoundedCollection<String> unmodifiableBoundedCollection;

    @Before
    public void setUp() {
        backingList = new ArrayList<String>(Arrays.asList("a", "b", "c"));
        unmodifiableBoundedCollection = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(backingList);
    }

    // ------------------------------------------------------------------
    // Factory method tests
    // ------------------------------------------------------------------

    @Test
    public void testUnmodifiableBoundedCollectionWithBoundedCollection() {
        BoundedCollection<String> bounded = new MockBoundedCollection<String>(backingList, 5);
        UnmodifiableBoundedCollection<String> result = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.isFull());
        assertEquals(5, result.maxSize());
    }

    @Test
    public void testUnmodifiableBoundedCollectionWithUnmodifiableBoundedCollection() {
        UnmodifiableBoundedCollection<String> first = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(backingList);
        UnmodifiableBoundedCollection<String> second = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(first);
        assertSame(first, second);
    }

    @Test
    public void testUnmodifiableBoundedCollectionWithNull() {
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection((BoundedCollection<String>) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testUnmodifiableBoundedCollectionWithNonBoundedCollection() {
        Collection<String> regular = new ArrayList<String>(Arrays.asList("x", "y"));
        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(regular);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // Decorator behavior tests
    // ------------------------------------------------------------------

    @Test
    public void testSize() {
        assertEquals(3, unmodifiableBoundedCollection.size());
    }

    @Test
    public void testIsEmpty() {
        assertFalse(unmodifiableBoundedCollection.isEmpty());
        UnmodifiableBoundedCollection<String> empty = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(
                new ArrayList<String>());
        assertTrue(empty.isEmpty());
    }

    @Test
    public void testContains() {
        assertTrue(unmodifiableBoundedCollection.contains("a"));
        assertFalse(unmodifiableBoundedCollection.contains("z"));
        assertFalse(unmodifiableBoundedCollection.contains(null));
    }

    @Test
    public void testContainsNull() {
        backingList.add(null);
        assertTrue(unmodifiableBoundedCollection.contains(null));
    }

    @Test
    public void testIterator() {
        Iterator<String> it = unmodifiableBoundedCollection.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void testIteratorRemoveThrows() {
        Iterator<String> it = unmodifiableBoundedCollection.iterator();
        it.next();
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testToArray() {
        Object[] array = unmodifiableBoundedCollection.toArray();
        assertEquals(3, array.length);
        assertEquals("a", array[0]);
        assertEquals("b", array[1]);
        assertEquals("c", array[2]);
    }

    @Test
    public void testToArrayWithType() {
        String[] array = unmodifiableBoundedCollection.toArray(new String[0]);
        assertEquals(3, array.length);
        assertEquals("a", array[0]);
        assertEquals("b", array[1]);
        assertEquals("c", array[2]);
    }

    @Test
    public void testToArrayWithLargerArray() {
        String[] input = new String[5];
        String[] result = unmodifiableBoundedCollection.toArray(input);
        assertSame(input, result);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
        assertEquals("c", result[2]);
        assertNull(result[3]);
    }

    // ------------------------------------------------------------------
    // Mutation methods - all should throw UnsupportedOperationException
    // ------------------------------------------------------------------

    @Test
    public void testAddThrows() {
        try {
            unmodifiableBoundedCollection.add("d");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testAddAllThrows() {
        try {
            unmodifiableBoundedCollection.addAll(Arrays.asList("d", "e"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testRemoveThrows() {
        try {
            unmodifiableBoundedCollection.remove("a");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testRemoveAllThrows() {
        try {
            unmodifiableBoundedCollection.removeAll(Arrays.asList("a", "b"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testRetainAllThrows() {
        try {
            unmodifiableBoundedCollection.retainAll(Arrays.asList("a"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testClearThrows() {
        try {
            unmodifiableBoundedCollection.clear();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ------------------------------------------------------------------
    // BoundedCollection interface methods
    // ------------------------------------------------------------------

    @Test
    public void testIsFull() {
        assertTrue(unmodifiableBoundedCollection.isFull());
        UnmodifiableBoundedCollection<String> notFull = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(
                new ArrayList<String>(Arrays.asList("a")));
        assertFalse(notFull.isFull());
    }

    @Test
    public void testMaxSize() {
        assertEquals(3, unmodifiableBoundedCollection.maxSize());
        UnmodifiableBoundedCollection<String> empty = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(
                new ArrayList<String>());
        assertEquals(0, empty.maxSize());
    }

    // ------------------------------------------------------------------
    // equals and hashCode
    // ------------------------------------------------------------------

    @Test
    public void testEquals() {
        UnmodifiableBoundedCollection<String> other = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(
                new ArrayList<String>(Arrays.asList("a", "b", "c")));
        assertTrue(unmodifiableBoundedCollection.equals(other));
        assertTrue(other.equals(unmodifiableBoundedCollection));
        assertTrue(unmodifiableBoundedCollection.equals(unmodifiableBoundedCollection));
        assertFalse(unmodifiableBoundedCollection.equals(null));
        assertFalse(unmodifiableBoundedCollection.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        UnmodifiableBoundedCollection<String> other = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(
                new ArrayList<String>(Arrays.asList("a", "b", "c")));
        assertEquals(unmodifiableBoundedCollection.hashCode(), other.hashCode());
    }

    @Test
    public void testToString() {
        String str = unmodifiableBoundedCollection.toString();
        assertNotNull(str);
        assertTrue(str.contains("a"));
        assertTrue(str.contains("b"));
        assertTrue(str.contains("c"));
    }

    // ------------------------------------------------------------------
    // Edge cases and boundary conditions
    // ------------------------------------------------------------------

    @Test
    public void testEmptyCollection() {
        UnmodifiableBoundedCollection<String> empty = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(
                new ArrayList<String>());
        assertTrue(empty.isEmpty());
        assertEquals(0, empty.size());
        assertTrue(empty.isFull());
        assertEquals(0, empty.maxSize());
        assertFalse(empty.contains("anything"));
        assertEquals(0, empty.toArray().length);
    }

    @Test
    public void testSingleElementCollection() {
        UnmodifiableBoundedCollection<String> single = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(
                new ArrayList<String>(Arrays.asList("only")));
        assertFalse(single.isEmpty());
        assertEquals(1, single.size());
        assertTrue(single.isFull());
        assertEquals(1, single.maxSize());
        assertTrue(single.contains("only"));
    }

    @Test
    public void testLargeCollection() {
        List<Integer> largeList = new ArrayList<Integer>();
        for (int i = 0; i < 1000; i++) {
            largeList.add(i);
        }
        UnmodifiableBoundedCollection<Integer> large = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(largeList);
        assertEquals(1000, large.size());
        assertTrue(large.isFull());
        assertEquals(1000, large.maxSize());
        assertTrue(large.contains(999));
        assertFalse(large.contains(1000));
    }

    @Test
    public void testNullElements() {
        List<String> withNull = new ArrayList<String>();
        withNull.add("a");
        withNull.add(null);
        withNull.add("c");
        UnmodifiableBoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(withNull);
        assertEquals(3, coll.size());
        assertTrue(coll.contains(null));
        assertTrue(coll.contains("a"));
        assertTrue(coll.contains("c"));
    }

    @Test
    public void testDuplicateElements() {
        List<String> duplicates = new ArrayList<String>(Arrays.asList("a", "a", "b", "b", "c"));
        UnmodifiableBoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(duplicates);
        assertEquals(5, coll.size());
        assertTrue(coll.contains("a"));
        assertTrue(coll.contains("b"));
        assertTrue(coll.contains("c"));
    }

    @Test
    public void testContainsAll() {
        assertTrue(unmodifiableBoundedCollection.containsAll(Arrays.asList("a", "b")));
        assertTrue(unmodifiableBoundedCollection.containsAll(Arrays.asList("a", "b", "c")));
        assertFalse(unmodifiableBoundedCollection.containsAll(Arrays.asList("a", "z")));
        assertFalse(unmodifiableBoundedCollection.containsAll(Arrays.asList("a", "b", "c", "d")));
    }

    @Test
    public void testContainsAllEmpty() {
        assertTrue(unmodifiableBoundedCollection.containsAll(Collections.emptyList()));
    }

    @Test
    public void testIteratorHasNextAfterExhaustion() {
        Iterator<String> it = unmodifiableBoundedCollection.iterator();
        while (it.hasNext()) {
            it.next();
        }
        assertFalse(it.hasNext());
    }

    @Test
    public void testToArrayWithNullArray() {
        try {
            unmodifiableBoundedCollection.toArray(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testMaxSizeWithBoundedCollection() {
        BoundedCollection<String> bounded = new MockBoundedCollection<String>(backingList, 10);
        UnmodifiableBoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertEquals(10, coll.maxSize());
        assertFalse(coll.isFull());
    }

    @Test
    public void testIsFullWithBoundedCollection() {
        BoundedCollection<String> bounded = new MockBoundedCollection<String>(backingList, 3);
        UnmodifiableBoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertTrue(coll.isFull());
    }

    @Test
    public void testIsFullWithBoundedCollectionNotFull() {
        BoundedCollection<String> bounded = new MockBoundedCollection<String>(backingList, 5);
        UnmodifiableBoundedCollection<String> coll = UnmodifiableBoundedCollection.unmodifiableBoundedCollection(bounded);
        assertFalse(coll.isFull());
    }

    // ------------------------------------------------------------------
    // Helper mock class for BoundedCollection
    // ------------------------------------------------------------------

    private static class MockBoundedCollection<E> extends ArrayList<E> implements BoundedCollection<E> {
        private static final long serialVersionUID = 1L;
        private final int maxSize;

        MockBoundedCollection(Collection<? extends E> c, int maxSize) {
            super(c);
            this.maxSize = maxSize;
        }

        @Override
        public boolean isFull() {
            return size() >= maxSize;
        }

        @Override
        public int maxSize() {
            return maxSize;
        }
    }
}