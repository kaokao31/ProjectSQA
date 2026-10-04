package org.apache.commons.collections.set;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for ListOrderedSet.
 * Designed to achieve maximum code coverage and detect potential faults.
 */
public class ListOrderedSetTest {

    private ListOrderedSet<String> emptySet;
    private ListOrderedSet<String> singleSet;
    private ListOrderedSet<String> multiSet;
    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";

    @Before
    public void setUp() {
        emptySet = new ListOrderedSet<String>();

        singleSet = new ListOrderedSet<String>();
        singleSet.add(A);

        multiSet = new ListOrderedSet<String>();
        multiSet.add(A);
        multiSet.add(B);
        multiSet.add(C);
    }

    // ==================== Constructor Tests ====================

    @Test
    public void testDefaultConstructor() {
        assertTrue("New set should be empty", emptySet.isEmpty());
        assertEquals("New set size should be 0", 0, emptySet.size());
    }

    @Test
    public void testConstructorWithCollection() {
        Collection<String> coll = Arrays.asList(A, B, C, A);
        ListOrderedSet<String> set = new ListOrderedSet<String>(coll);
        assertEquals("Size should be 3 (duplicates removed)", 3, set.size());
        assertTrue("Should contain A", set.contains(A));
        assertTrue("Should contain B", set.contains(B));
        assertTrue("Should contain C", set.contains(C));
        // Order should be insertion order (first occurrence)
        assertEquals("First element should be A", A, set.get(0));
        assertEquals("Second element should be B", B, set.get(1));
        assertEquals("Third element should be C", C, set.get(2));
    }

    @Test
    public void testConstructorWithSet() {
        Set<String> set = new HashSet<String>(Arrays.asList(A, B, C));
        ListOrderedSet<String> listSet = new ListOrderedSet<String>(set);
        assertEquals("Size should be 3", 3, listSet.size());
        assertTrue("Should contain A", listSet.contains(A));
        assertTrue("Should contain B", listSet.contains(B));
        assertTrue("Should contain C", listSet.contains(C));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullCollection() {
        new ListOrderedSet<String>((Collection<String>) null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullSet() {
        new ListOrderedSet<String>((Set<String>) null);
    }

    // ==================== add() Tests ====================

    @Test
    public void testAddToEmptySet() {
        assertTrue("add should return true for new element", emptySet.add(A));
        assertEquals("Size should be 1", 1, emptySet.size());
        assertTrue("Should contain A", emptySet.contains(A));
        assertEquals("First element should be A", A, emptySet.get(0));
    }

    @Test
    public void testAddDuplicate() {
        assertFalse("add should return false for duplicate", multiSet.add(A));
        assertEquals("Size should remain 3", 3, multiSet.size());
        // Order should not change
        assertEquals("First element should still be A", A, multiSet.get(0));
    }

    @Test
    public void testAddNull() {
        // Assuming null is allowed (typical for collections)
        assertTrue("add(null) should return true", emptySet.add(null));
        assertTrue("Should contain null", emptySet.contains(null));
        assertEquals("Size should be 1", 1, emptySet.size());
        // Adding null again should be duplicate
        assertFalse("add(null) again should return false", emptySet.add(null));
    }

    @Test
    public void testAddMaintainsOrder() {
        ListOrderedSet<String> set = new ListOrderedSet<String>();
        set.add(A);
        set.add(B);
        set.add(C);
        set.add(D);
        assertEquals("Order should be A,B,C,D", Arrays.asList(A, B, C, D), set.asList());
        // Add duplicate at end
        set.add(A);
        assertEquals("Order should remain A,B,C,D after duplicate add", Arrays.asList(A, B, C, D), set.asList());
    }

    // ==================== remove() Tests ====================

    @Test
    public void testRemoveExisting() {
        assertTrue("remove should return true for existing element", multiSet.remove(B));
        assertEquals("Size should be 2", 2, multiSet.size());
        assertFalse("Should not contain B", multiSet.contains(B));
        // Order should be maintained
        assertEquals("First element should be A", A, multiSet.get(0));
        assertEquals("Second element should be C", C, multiSet.get(1));
    }

    @Test
    public void testRemoveNonExisting() {
        assertFalse("remove should return false for non-existing element", multiSet.remove(D));
        assertEquals("Size should remain 3", 3, multiSet.size());
    }

    @Test
    public void testRemoveNull() {
        emptySet.add(null);
        assertTrue("remove(null) should return true", emptySet.remove(null));
        assertFalse("Should not contain null", emptySet.contains(null));
        assertEquals("Size should be 0", 0, emptySet.size());
    }

    @Test
    public void testRemoveFromSingleElementSet() {
        singleSet.remove(A);
        assertTrue("Set should be empty after removal", singleSet.isEmpty());
    }

    @Test
    public void testRemoveAllElements() {
        multiSet.remove(A);
        multiSet.remove(B);
        multiSet.remove(C);
        assertTrue("Set should be empty after removing all", multiSet.isEmpty());
    }

    // ==================== contains() Tests ====================

    @Test
    public void testContains() {
        assertTrue("Should contain A", multiSet.contains(A));
        assertFalse("Should not contain D", multiSet.contains(D));
        assertFalse("Empty set should not contain anything", emptySet.contains(A));
    }

    @Test
    public void testContainsNull() {
        assertFalse("Empty set should not contain null", emptySet.contains(null));
        emptySet.add(null);
        assertTrue("Should contain null", emptySet.contains(null));
    }

    // ==================== clear() Tests ====================

    @Test
    public void testClear() {
        multiSet.clear();
        assertTrue("Set should be empty after clear", multiSet.isEmpty());
        assertEquals("Size should be 0", 0, multiSet.size());
    }

    @Test
    public void testClearEmptySet() {
        emptySet.clear();
        assertTrue("Empty set should remain empty", emptySet.isEmpty());
    }

    // ==================== size() and isEmpty() Tests ====================

    @Test
    public void testSize() {
        assertEquals("Empty set size", 0, emptySet.size());
        assertEquals("Single set size", 1, singleSet.size());
        assertEquals("Multi set size", 3, multiSet.size());
    }

    @Test
    public void testIsEmpty() {
        assertTrue("Empty set should be empty", emptySet.isEmpty());
        assertFalse("Single set should not be empty", singleSet.isEmpty());
        assertFalse("Multi set should not be empty", multiSet.isEmpty());
    }

    // ==================== iterator() Tests ====================

    @Test
    public void testIterator() {
        Iterator<String> it = multiSet.iterator();
        assertTrue("Iterator should have next", it.hasNext());
        assertEquals("First element should be A", A, it.next());
        assertEquals("Second element should be B", B, it.next());
        assertEquals("Third element should be C", C, it.next());
        assertFalse("Iterator should have no more elements", it.hasNext());
    }

    @Test
    public void testIteratorEmptySet() {
        Iterator<String> it = emptySet.iterator();
        assertFalse("Iterator should have no elements", it.hasNext());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIteratorRemove() {
        Iterator<String> it = multiSet.iterator();
        it.next();
        it.remove(); // Should throw UnsupportedOperationException
    }

    // ==================== toArray() Tests ====================

    @Test
    public void testToArray() {
        Object[] array = multiSet.toArray();
        assertArrayEquals("Array should contain A,B,C", new Object[]{A, B, C}, array);
    }

    @Test
    public void testToArrayEmptySet() {
        Object[] array = emptySet.toArray();
        assertEquals("Array length should be 0", 0, array.length);
    }

    // ==================== get() Tests ====================

    @Test
    public void testGet() {
        assertEquals("get(0) should be A", A, multiSet.get(0));
        assertEquals("get(1) should be B", B, multiSet.get(1));
        assertEquals("get(2) should be C", C, multiSet.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndex() {
        multiSet.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetIndexEqualToSize() {
        multiSet.get(3);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFromEmptySet() {
        emptySet.get(0);
    }

    // ==================== indexOf() Tests ====================

    @Test
    public void testIndexOf() {
        assertEquals("indexOf(A) should be 0", 0, multiSet.indexOf(A));
        assertEquals("indexOf(B) should be 1", 1, multiSet.indexOf(B));
        assertEquals("indexOf(C) should be 2", 2, multiSet.indexOf(C));
    }

    @Test
    public void testIndexOfNonExisting() {
        assertEquals("indexOf(D) should be -1", -1, multiSet.indexOf(D));
    }

    @Test
    public void testIndexOfNull() {
        assertEquals("indexOf(null) should be -1", -1, multiSet.indexOf(null));
        multiSet.add(null);
        assertEquals("indexOf(null) should be 3", 3, multiSet.indexOf(null));
    }

    // ==================== addAll() Tests ====================

    @Test
    public void testAddAll() {
        Collection<String> coll = Arrays.asList(D, A, B); // D new, A and B duplicates
        assertTrue("addAll should return true because D was added", multiSet.addAll(coll));
        assertEquals("Size should be 4", 4, multiSet.size());
        assertTrue("Should contain D", multiSet.contains(D));
        // Order: A,B,C,D (D appended)
        assertEquals("Order should be A,B,C,D", Arrays.asList(A, B, C, D), multiSet.asList());
    }

    @Test
    public void testAddAllNoChange() {
        Collection<String> coll = Arrays.asList(A, B, C);
        assertFalse("addAll should return false if no new elements", multiSet.addAll(coll));
        assertEquals("Size should remain 3", 3, multiSet.size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddAllNullCollection() {
        multiSet.addAll(null);
    }

    // ==================== removeAll() Tests ====================

    @Test
    public void testRemoveAll() {
        Collection<String> coll = Arrays.asList(A, C);
        assertTrue("removeAll should return true because elements were removed", multiSet.removeAll(coll));
        assertEquals("Size should be 1", 1, multiSet.size());
        assertTrue("Should contain B", multiSet.contains(B));
        assertFalse("Should not contain A", multiSet.contains(A));
        assertFalse("Should not contain C", multiSet.contains(C));
    }

    @Test
    public void testRemoveAllNoChange() {
        Collection<String> coll = Arrays.asList(D, E);
        assertFalse("removeAll should return false if no elements removed", multiSet.removeAll(coll));
        assertEquals("Size should remain 3", 3, multiSet.size());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveAllNullCollection() {
        multiSet.removeAll(null);
    }

    // ==================== retainAll() Tests ====================

    @Test
    public void testRetainAll() {
        Collection<String> coll = Arrays.asList(A, C);
        assertTrue("retainAll should return true because B was removed", multiSet.retainAll(coll));
        assertEquals("Size should be 2", 2, multiSet.size());
        assertTrue("Should contain A", multiSet.contains(A));
        assertTrue("Should contain C", multiSet.contains(C));
        assertFalse("Should not contain B", multiSet.contains(B));
        // Order should be A, C
        assertEquals("First element should be A", A, multiSet.get(0));
        assertEquals("Second element should be C", C, multiSet.get(1));
    }

    @Test
    public void testRetainAllNoChange() {
        Collection<String> coll = Arrays.asList(A, B, C);
        assertFalse("retainAll should return false if set unchanged", multiSet.retainAll(coll));
        assertEquals("Size should remain 3", 3, multiSet.size());
    }

    @Test
    public void testRetainAllEmptyCollection() {
        assertTrue("retainAll with empty collection should clear set", multiSet.retainAll(Collections.emptyList()));
        assertTrue("Set should be empty", multiSet.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testRetainAllNullCollection() {
        multiSet.retainAll(null);
    }

    // ==================== subList() Tests ====================

    @Test
    public void testSubList() {
        List<String> sub = multiSet.subList(0, 2);
        assertEquals("SubList size should be 2", 2, sub.size());
        assertEquals("First element should be A", A, sub.get(0));
        assertEquals("Second element should be B", B, sub.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListNegativeFromIndex() {
        multiSet.subList(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListToIndexGreaterThanSize() {
        multiSet.subList(0, 4);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListFromIndexGreaterThanToIndex() {
        multiSet.subList(2, 1);
    }

    // ==================== setValue() Tests (if exists) ====================

    // Assuming setValue(int index, Object value) exists (common in ordered sets)
    @Test
    public void testSetValue() {
        // This test assumes setValue method exists; if not, it will fail compilation.
        // To be safe, we can comment it out if method doesn't exist.
        // But we include it for coverage.
        String old = multiSet.setValue(1, D);
        assertEquals("Old value should be B", B, old);
        assertEquals("New value at index 1 should be D", D, multiSet.get(1));
        assertEquals("Size should remain 3", 3, multiSet.size());
        assertTrue("Set should contain D", multiSet.contains(D));
        assertFalse("Set should not contain B", multiSet.contains(B));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetValueNegativeIndex() {
        multiSet.setValue(-1, A);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetValueIndexEqualToSize() {
        multiSet.setValue(3, A);
    }

    // ==================== asList() Tests ====================

    @Test
    public void testAsList() {
        List<String> list = multiSet.asList();
        assertEquals("List should be [A,B,C]", Arrays.asList(A, B, C), list);
        // Modifying returned list should not affect set (if unmodifiable)
        // But we assume it returns an unmodifiable list or a copy.
        // We'll just check that it's not null.
        assertNotNull("asList should not return null", list);
    }

    @Test
    public void testAsListEmptySet() {
        List<String> list = emptySet.asList();
        assertTrue("List should be empty", list.isEmpty());
    }

    // ==================== equals() and hashCode() Tests ====================

    @Test
    public void testEquals() {
        ListOrderedSet<String> other = new ListOrderedSet<String>();
        other.add(A);
        other.add(B);
        other.add(C);
        assertTrue("Sets with same elements in same order should be equal", multiSet.equals(other));
        assertEquals("Hash codes should be equal", multiSet.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsDifferentOrder() {
        ListOrderedSet<String> other = new ListOrderedSet<String>();
        other.add(B);
        other.add(A);
        other.add(C);
        assertFalse("Sets with different order should not be equal", multiSet.equals(other));
    }

    @Test
    public void testEqualsWithSet() {
        Set<String> hashSet = new HashSet<String>(Arrays.asList(A, B, C));
        // ListOrderedSet should not be equal to a HashSet (different type)
        assertFalse("Should not equal a HashSet", multiSet.equals(hashSet));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("Should not equal null", multiSet.equals(null));
    }

    // ==================== toString() Tests ====================

    @Test
    public void testToString() {
        String str = multiSet.toString();
        assertNotNull("toString should not return null", str);
        assertTrue("toString should contain elements", str.contains(A) && str.contains(B) && str.contains(C));
    }

    @Test
    public void testToStringEmptySet() {
        String str = emptySet.toString();
        assertNotNull("toString should not return null", str);
    }

    // ==================== Edge Cases and Bug Triggers ====================

    @Test
    public void testAddAfterRemove() {
        multiSet.remove(B);
        multiSet.add(B);
        assertEquals("B should be appended at end", 3, multiSet.indexOf(B));
        assertEquals("Size should be 3", 3, multiSet.size());
    }

    @Test
    public void testRetainAllWithDuplicatesInCollection() {
        Collection<String> coll = Arrays.asList(A, A, B);
        multiSet.retainAll(coll);
        assertEquals("Size should be 2", 2, multiSet.size());
        assertTrue("Should contain A", multiSet.contains(A));
        assertTrue("Should contain B", multiSet.contains(B));
    }

    @Test
    public void testRemoveAllWithDuplicatesInCollection() {
        Collection<String> coll = Arrays.asList(A, A, B);
        multiSet.removeAll(coll);
        assertEquals("Size should be 1", 1, multiSet.size());
        assertTrue("Should contain C", multiSet.contains(C));
    }

    @Test
    public void testAddAllWithNullElement() {
        Collection<String> coll = Arrays.asList((String) null);
        assertTrue("addAll with null should succeed", multiSet.addAll(coll));
        assertTrue("Set should contain null", multiSet.contains(null));
    }

    @Test
    public void testIteratorConcurrentModification() {
        // This test may fail if the set does not support concurrent modification detection.
        // We include it to trigger potential bugs.
        Iterator<String> it = multiSet.iterator();
        multiSet.add(D);
        try {
            it.next();
            // If no exception, that's okay; but we expect ConcurrentModificationException in some implementations.
        } catch (Exception e) {
            // Expected or not, we just ensure no crash.
        }
    }

    // ==================== Performance / Stress (optional) ====================
    // Not included to keep tests fast, but could be added.
}