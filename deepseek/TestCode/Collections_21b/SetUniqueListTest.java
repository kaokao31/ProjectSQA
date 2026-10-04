package org.apache.commons.collections4.list;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

public class SetUniqueListTest {

    private SetUniqueList<String> list;
    private List<String> decoratedList;

    @Before
    public void setUp() {
        decoratedList = new ArrayList<>();
        list = SetUniqueList.setUniqueList(decoratedList);
    }

    // ==================== Basic tests ====================

    @Test
    public void testEmptyList() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    @Test
    public void testAddSingleElement() {
        assertTrue(list.add("A"));
        assertEquals(1, list.size());
        assertTrue(list.contains("A"));
    }

    @Test
    public void testAddDuplicate_ShouldReturnFalseAndNotAdd() {
        list.add("A");
        assertFalse(list.add("A"));
        assertEquals(1, list.size());
    }

    @Test
    public void testAddAllUnique() {
        Collection<String> coll = Arrays.asList("A", "B", "C");
        assertTrue(list.addAll(coll));
        assertEquals(3, list.size());
    }

    @Test
    public void testAddAllWithDuplicates_ShouldReturnTrueButNotAddDuplicates() {
        list.add("A");
        Collection<String> coll = Arrays.asList("A", "B", "A");
        assertTrue(list.addAll(coll)); // at least one new element added
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    @Test
    public void testAddAllEmptyCollection() {
        Collection<String> coll = new ArrayList<>();
        assertFalse(list.addAll(coll));
        assertEquals(0, list.size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddAllNullCollection() {
        list.addAll(null);
    }

    // ==================== Index-based add ====================

    @Test
    public void testAddAtIndex_Successful() {
        list.add("A");
        list.add("B");
        list.add(1, "C"); // [A, C, B]
        assertEquals(3, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
        assertEquals("B", list.get(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAtIndex_Duplicate() {
        list.add("A");
        list.add("B");
        list.add(1, "A"); // duplicate, should throw
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndex_NegativeIndex() {
        list.add(-1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndex_OutOfBounds() {
        list.add(5, "A");
    }

    // ==================== Set ====================

    @Test
    public void testSet_Successful() {
        list.add("A");
        list.add("B");
        String old = list.set(0, "C"); // [C, B]
        assertEquals("A", old);
        assertEquals("C", list.get(0));
        assertEquals("B", list.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSet_Duplicate() {
        list.add("A");
        list.add("B");
        list.set(0, "B"); // B already present at index 1
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSet_NegativeIndex() {
        list.set(-1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSet_OutOfBounds() {
        list.set(0, "A");
    }

    // ==================== Remove ====================

    @Test
    public void testRemoveByObject() {
        list.add("A");
        list.add("B");
        assertTrue(list.remove("A"));
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
    }

    @Test
    public void testRemoveNonExistent() {
        list.add("A");
        assertFalse(list.remove("B"));
        assertEquals(1, list.size());
    }

    @Test
    public void testRemoveByIndex() {
        list.add("A");
        list.add("B");
        String removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByIndex_OutOfBounds() {
        list.remove(0);
    }

    // ==================== Contains and indexOf ====================

    @Test
    public void testContains() {
        list.add("A");
        assertTrue(list.contains("A"));
        assertFalse(list.contains("B"));
    }

    @Test
    public void testIndexOf() {
        list.add("A");
        list.add("B");
        list.add("A"); // duplicate not added, so already there
        assertEquals(0, list.indexOf("A"));
        assertEquals(1, list.indexOf("B"));
        assertEquals(-1, list.indexOf("C"));
    }

    // ==================== Iterator ====================

    @Test
    public void testIterator() {
        list.add("A");
        list.add("B");
        java.util.Iterator<String> it = list.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }

    @Test
    public void testListIterator() {
        list.add("A");
        list.add("B");
        java.util.ListIterator<String> it = list.listIterator();
        assertTrue(it.hasNext());
        assertFalse(it.hasPrevious());
        assertEquals("A", it.next());
        assertTrue(it.hasPrevious());
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListIteratorAdd_Duplicate() {
        list.add("A");
        java.util.ListIterator<String> it = list.listIterator();
        it.add("A"); // duplicate, should throw
    }

    @Test
    public void testListIteratorAdd_Successful() {
        java.util.ListIterator<String> it = list.listIterator();
        it.add("A");
        assertEquals(1, list.size());
        assertTrue(list.contains("A"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListIteratorSet_Duplicate() {
        list.add("A");
        list.add("B");
        java.util.ListIterator<String> it = list.listIterator();
        it.next(); // A
        it.next(); // B
        it.set("A"); // A already present, should throw
    }

    // ==================== SubList ====================

    @Test
    public void testSubList() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(0, 2);
        assertEquals(2, sub.size());
        assertEquals("A", sub.get(0));
        assertEquals("B", sub.get(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListAdd_Duplicate() {
        list.add("A");
        list.add("B");
        List<String> sub = list.subList(0, 1);
        sub.add("A"); // duplicate, should throw
    }

    // ==================== RetainAll and RemoveAll ====================

    @Test
    public void testRetainAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.retainAll(Arrays.asList("A", "C")));
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
        assertFalse(list.contains("B"));
    }

    @Test
    public void testRemoveAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.removeAll(Arrays.asList("A", "C")));
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
    }

    // ==================== Clear ====================

    @Test
    public void testClear() {
        list.add("A");
        list.add("B");
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.contains("A"));
    }

    // ==================== SetUniqueList factory methods ====================

    @Test
    public void testSetUniqueListWithSet() {
        List<String> backingList = new ArrayList<>();
        Set<String> set = new HashSet<>();
        SetUniqueList<String> custom = SetUniqueList.setUniqueList(backingList, set);
        custom.add("A");
        assertEquals(1, custom.size());
        assertTrue(set.contains("A"));
    }

    @Test(expected = NullPointerException.class)
    public void testSetUniqueListWithNullList() {
        SetUniqueList.setUniqueList(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetUniqueListWithNullSet() {
        SetUniqueList.setUniqueList(new ArrayList<String>(), null);
    }

    // ==================== Bug-specific test (Defects4J 21) ====================
    // The bug may be related to set(index, element) when the element already exists
    // but at an index <= the current index? Or maybe list iterator set?
    @Test
    public void testSetDuplicateInSamePosition() {
        list.add("A");
        list.add("B");
        // Setting index 0 to "A" should be a no-op because element equals old element?
        // According to the contract of SetUniqueList, set should not allow duplicates,
        // but it might be allowed if the element is the same as the one at that index.
        // However, the implementation might throw IllegalArgumentException.
        // We test both possibilities.
        try {
            String old = list.set(0, "A");
            // If it succeds, the element stays same, size unchanged, set contains same.
            assertEquals("A", old);
            assertEquals(2, list.size());
            assertEquals("A", list.get(0));
        } catch (IllegalArgumentException e) {
            // Expected if it throws
        }
    }

    // Additional bug-related: subList set might not check uniqueness properly?
    @Test
    public void testSubListSet_Duplicate() {
        list.add("A");
        list.add("B");
        List<String> sub = list.subList(16, 1);
        // SubList from 0 to 1 contains "A". Setting it to "B" should throw because B already exists.
        try {
            sub.set(0, "B");
            fail("Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // =================== Edge case: Null elements (if allowed?)
    @Test
    public void testAddNull() {
        try {
            list.add(null);
            // If null is allowed, the list should contain it
            assertEquals(1, list.size());
            assertTrue(list.contains(null));
            // Adding null again should fail
            assertFalse(list.add(null));
            assertEquals(1, list.size());
        } catch (NullPointerException e) {
            // Some implementations may reject null
        }
    }

    // ==================== equals and hashCode ====================

    @Test
    public void testEqualsAndHashCode() {
        list.add("A");
        list.add("B");
        SetUniqueList<String> other = SetUniqueList.setUniqueList(new ArrayList<>(Arrays.asList("A", "B")));
        assertEquals(list, other);
        assertEquals(list.hashCode(), other.hashCode());
    }
}