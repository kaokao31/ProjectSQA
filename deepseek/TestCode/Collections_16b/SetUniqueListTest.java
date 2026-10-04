package org.apache.commons.collections.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for SetUniqueList.
 * Targets all branches, edge cases, and known bugs (Defects4J bug 16).
 */
public class SetUniqueListTest {

    private SetUniqueList<String> list;
    private SetUniqueList<Integer> intList;
    private List<String> baseList;

    @Before
    public void setUp() {
        baseList = new ArrayList<String>();
        list = new SetUniqueList<String>(baseList, new HashSet<String>());
        intList = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());
    }

    // ---------- Core add / uniqueness ----------

    @Test
    public void testAddUnique() {
        assertTrue(list.add("A"));
        assertEquals(1, list.size());
        assertFalse(list.add("A"));
        assertEquals(1, list.size());
        assertTrue(list.add("B"));
        assertEquals(2, list.size());
    }

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
        // default SetUniqueList uses HashSet which does not allow null
        list.add(null);
    }

    @Test
    public void testAddAtIndexUnique() {
        list.add(0, "X");
        assertEquals(1, list.size());
        list.add(0, "Y");
        assertEquals(2, list.size());
        list.add(0, "X"); // duplicate, should not be added
        assertEquals(2, list.size());
        assertEquals("Y", list.get(0));
        assertEquals("X", list.get(1));
    }

    // ---------- addAll ----------

    @Test
    public void testAddAllCollectionUnique() {
        Collection<String> c = Arrays.asList("A", "B", "A", "C");
        assertTrue(list.addAll(c));
        assertEquals(3, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
        assertTrue(list.contains("C"));
    }

    @Test
    public void testAddAllDuplicatesExcluded() {
        list.add("A");
        list.add("B");
        Collection<String> c = Arrays.asList("A", "C", "B");
        assertTrue(list.addAll(c));
        assertEquals(3, list.size()); // only C added
    }

    @Test
    public void testAddAllAtIndexUnique() {
        list.add("X");
        list.add("Y");
        Collection<String> c = Arrays.asList("Y", "Z", "X");
        assertTrue(list.addAll(1, c));
        assertEquals(3, list.size()); // only Z added at index 1
        assertEquals("X", list.get(0));
        assertEquals("Z", list.get(1));
        assertEquals("Y", list.get(2));
    }

    // ---------- remove ----------

    @Test
    public void testRemoveObject() {
        list.add("A");
        list.add("B");
        assertTrue(list.remove("A"));
        assertFalse(list.contains("A"));
        assertEquals(1, list.size());
        assertFalse(list.remove("C"));
    }

    @Test
    public void testRemoveAtIndex() {
        list.add("A");
        list.add("B");
        list.add("C");
        String removed = list.remove(1);
        assertEquals("B", removed);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0));
        assertEquals("C", list.get(1));
    }

    // ---------- retainAll / removeAll ----------

    @Test
    public void testRetainAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        Collection<String> c = Arrays.asList("A", "C");
        assertTrue(list.retainAll(c));
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
    }

    @Test
    public void testRemoveAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        Collection<String> c = Arrays.asList("A", "C");
        assertTrue(list.removeAll(c));
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
        assertTrue(list.removeAll(c)); // nothing to remove but should still succeed
        assertEquals(1, list.size());
    }

    @Test
    public void testRemoveAllWithDuplicatesInCollection() {
        list.add("A");
        list.add("B");
        Collection<String> c = Arrays.asList("A", "A");
        assertTrue(list.removeAll(c));
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
    }

    // ---------- subList ----------

    @Test
    public void testSubList() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        List<String> sub = list.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
        // Modify subList and check main list
        sub.set(0, "E");
        assertEquals("E", list.get(1));
        assertFalse(list.contains("B"));
        // add unique element via subList
        sub.add("F");
        assertEquals(5, list.size());
        assertTrue(list.contains("F"));
        // add duplicate via subList should be ignored
        sub.add(0, "E");
        assertEquals(5, list.size());
    }

    @Test
    public void testSubListRemove() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(0, 2);
        sub.remove("A");
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSubListModificationAfterMainListAdd() {
        list.add("A");
        list.add("B");
        List<String> sub = list.subList(0, 1);
        list.add("C"); // modify main list
        sub.size(); // should throw ConcurrentModificationException
    }

    // ---------- set() ----------

    @Test
    public void testSetNewValue() {
        list.add("A");
        list.add("B");
        String old = list.set(0, "C");
        assertEquals("A", old);
        assertEquals("C", list.get(0));
        assertEquals(2, list.size());
    }

    @Test
    public void testSetDuplicateValue() {
        list.add("A");
        list.add("B");
        String old = list.set(0, "B"); // duplicate, should not change
        assertEquals("A", old);
        assertEquals(1, list.size()); // actually size should reduce because A is replaced, but duplicate B is removed? Check behavior
        // According to SetUniqueList.set(): removes the old element, then adds new; if new is duplicate, new is not added, so size decreases.
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
    }

    @Test
    public void testSetToSelf() {
        list.add("A");
        list.add("B");
        String old = list.set(1, "A"); // old is B, new is A (already exists so not added)
        assertEquals("B", old);
        assertEquals(1, list.size());
        assertEquals("A", list.get(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetOutOfBounds() {
        list.add("A");
        list.set(1, "B");
    }

    // ---------- clear ----------

    @Test
    public void testClear() {
        list.add("A");
        list.add("B");
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    // ---------- contains / containsAll ----------

    @Test
    public void testContains() {
        list.add("A");
        assertTrue(list.contains("A"));
        assertFalse(list.contains("B"));
    }

    @Test
    public void testContainsAll() {
        list.add("A");
        list.add("B");
        assertTrue(list.containsAll(Arrays.asList("A", "B")));
        assertFalse(list.containsAll(Arrays.asList("A", "C")));
    }

    // ---------- indexOf / lastIndexOf ----------

    @Test
    public void testIndexOf() {
        list.add("A");
        list.add("B");
        assertEquals(0, list.indexOf("A"));
        assertEquals(1, list.indexOf("B"));
        assertEquals(-1, list.indexOf("C"));
    }

    @Test
    public void testLastIndexOf() {
        list.add("A");
        list.add("B");
        list.add("A"); // will not be added because duplicate
        assertEquals(0, list.lastIndexOf("A"));
        assertEquals(1, list.lastIndexOf("B"));
        assertEquals(-1, list.lastIndexOf("C"));
    }

    // ---------- iterator ----------

    @Test
    public void testIteratorRemove() {
        list.add("A");
        list.add("B");
        list.add("C");
        java.util.Iterator<String> it = list.iterator();
        it.next();
        it.remove();
        assertEquals(2, list.size());
        assertFalse(list.contains("A"));
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        list.iterator().remove();
    }

    // ---------- listIterator ----------

    @Test
    public void testListIteratorSet() {
        list.add("A");
        list.add("B");
        java.util.ListIterator<String> it = list.listIterator();
        it.next();
        it.set("C");
        assertEquals("C", list.get(0));
        assertEquals(2, list.size());
    }

    @Test
    public void testListIteratorSetDuplicate() {
        list.add("A");
        list.add("B");
        java.util.ListIterator<String> it = list.listIterator();
        it.next(); // at A
        it.set("B"); // duplicate, B already exists -> size decreases
        assertEquals(1, list.size());
        assertEquals("B", list.get(0));
    }

    @Test
    public void testListIteratorAddUnique() {
        list.add("A");
        java.util.ListIterator<String> it = list.listIterator();
        it.add("B");
        assertEquals(2, list.size());
        assertEquals("B", list.get(0));
        assertEquals("A", list.get(1));
    }

    @Test
    public void testListIteratorAddDuplicate() {
        list.add("A");
        java.util.ListIterator<String> it = list.listIterator();
        it.add("A"); // duplicate, should not be added
        assertEquals(1, list.size());
    }

    // ---------- toArray ----------

    @Test
    public void testToArray() {
        list.add("A");
        list.add("B");
        Object[] arr = list.toArray();
        assertEquals(2, arr.length);
        assertEquals("A", arr[0]);
        assertEquals("B", arr[1]);
    }

    @Test
    public void testToArrayWithArg() {
        list.add("A");
        list.add("B");
        String[] arr = list.toArray(new String[0]);
        assertEquals(2, arr.length);
        assertEquals("A", arr[0]);
        assertEquals("B", arr[1]);
    }

    // ---------- equals / hashCode (inherited from AbstractList) ----------

    @Test
    public void testEquals() {
        list.add("A");
        list.add("B");
        List<String> other = new ArrayList<String>();
        other.add("A");
        other.add("B");
        assertEquals(other, list);
    }

    @Test
    public void testHashCode() {
        list.add("A");
        list.add("B");
        List<String> other = new ArrayList<String>();
        other.add("A");
        other.add("B");
        assertEquals(other.hashCode(), list.hashCode());
    }

    // ---------- Edge cases: empty list ----------

    @Test
    public void testEmptyList() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.contains("anything"));
        assertEquals(-1, list.indexOf("x"));
        assertTrue(list.retainAll(new ArrayList<String>()));
        assertFalse(list.remove("x"));
        try {
            list.get(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // ---------- Bulk operations with empty collections ----------

    @Test
    public void testAddAllEmptyCollection() {
        list.add("A");
        assertFalse(list.addAll(new ArrayList<String>()));
        assertEquals(1, list.size());
    }

    @Test
    public void testRemoveAllEmptyCollection() {
        list.add("A");
        assertFalse(list.removeAll(new ArrayList<String>()));
        assertEquals(1, list.size());
    }

    @Test
    public void testRetainAllEmptyCollection() {
        list.add("A");
        assertTrue(list.retainAll(new ArrayList<String>()));
        assertTrue(list.isEmpty());
    }

    // ---------- Duplicate handling in addAll at index ----------

    @Test
    public void testAddAllAtIndexAllDuplicates() {
        list.add("A");
        list.add("B");
        assertFalse(list.addAll(0, Arrays.asList("A", "B")));
        assertEquals(2, list.size());
    }

    // ---------- Edge: set with index equal to size (append) ----------

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetAtIndexSize() {
        list.add("A");
        list.set(1, "B");
    }

    // ---------- Bug 16 specific: subList modifications inducing inconsistencies ----------

    @Test
    public void testSubListAddDuplicateToSet() {
        list.add("A");
        list.add("B");
        List<String> sub = list.subList(0, 2);
        // add a duplicate that exists only in set? All elements are unique, so add duplicate
        assertFalse(sub.add("A")); // duplicate should not be added
        assertEquals(2, list.size());
    }

    @Test
    public void testSubListRemoveAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(0, 2);
        sub.removeAll(Arrays.asList("A", "C")); // C is not in sub, but A is
        assertEquals(2, list.size()); // only B and C should remain
        assertEquals("B", list.get(0));
        assertEquals("C", list.get(1));
    }

    @Test
    public void testSubListRetainAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(0, 2);
        assertTrue(sub.retainAll(Arrays.asList("A")));
        assertEquals(1, list.size());
        assertEquals("A", list.get(0));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSubListIteratorAfterModification() {
        list.add("A");
        list.add("B");
        java.util.ListIterator<String> subIt = list.subList(0, 1).listIterator();
        list.add("C"); // mod count changes
        subIt.next(); // should throw
    }

    // ---------- Large number of elements (stress) ----------

    @Test
    public void testAddMany() {
        List<Integer> ints = new ArrayList<Integer>();
        for (int i = 0; i < 1000; i++) {
            ints.add(i);
        }
        assertTrue(intList.addAll(ints));
        assertEquals(1000, intList.size());
        // try adding duplicates
        intList.addAll(ints);
        assertEquals(1000, intList.size());
    }

    // ---------- Edge: collection with all same elements ----------

    @Test
    public void testAddAllSameElements() {
        list.add("A");
        Collection<String> c = Arrays.asList("A", "A", "A");
        assertFalse(list.addAll(c));
        assertEquals(1, list.size());
    }

}