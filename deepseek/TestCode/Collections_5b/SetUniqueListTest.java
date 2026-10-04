package org.apache.commons.collections4.list;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SetUniqueListTest {

    private SetUniqueList<String> list;
    private List<String> uniqueList;

    @Before
    public void setUp() {
        List<String> baseList = new ArrayList<>();
        baseList.add("A");
        baseList.add("B");
        baseList.add("C");
        list = SetUniqueList.setUniqueList(baseList);
        uniqueList = new ArrayList<>();
        uniqueList.add("X");
        uniqueList.add("Y");
    }

    // Test setUniqueList() static factory method
    @Test
    public void testSetUniqueList() {
        assertNotNull(list);
        assertEquals(3, list.size());
    }

    // Test add() - adding unique element
    @Test
    public void testAddUnique() {
        assertTrue(list.add("D"));
        assertEquals(4, list.size());
        assertTrue(list.contains("D"));
    }

    // Test add() - adding duplicate element
    @Test
    public void testAddDuplicate() {
        assertFalse(list.add("A"));
        assertEquals(3, list.size());
    }

    // Test add() - adding null
    @Test
    public void testAddNull() {
        list.add(null);
        assertTrue(list.contains(null));
        assertEquals(4, list.size());
        // Adding null again should fail
        assertFalse(list.add(null));
        assertEquals(4, list.size());
    }

    // Test addAll() - unique collection
    @Test
    public void testAddAllUnique() {
        assertTrue(list.addAll(uniqueList));
        assertEquals(5, list.size());
        assertTrue(list.contains("X"));
        assertTrue(list.contains("Y"));
    }

    // Test addAll() - collection with duplicates
    @Test
    public void testAddAllWithDuplicates() {
        List<String> dups = Arrays.asList("A", "B", "D");
        assertTrue(list.addAll(dups));
        assertEquals(4, list.size()); // Only "D" added
    }

    // Test addAll() - empty collection
    @Test
    public void testAddAllEmpty() {
        assertFalse(list.addAll(new ArrayList<>()));
        assertEquals(3, list.size());
    }

    // Test addAll() - null collection
    @Test(expected = NullPointerException.class)
    public void testAddAllNull() {
        list.addAll(null);
    }

    // Test set() - setting unique element
    @Test
    public void testSetUnique() {
        String old = list.set(1, "D");
        assertEquals("B", old);
        assertEquals(3, list.size());
        assertEquals("D", list.get(1));
    }

    // Test set() - setting duplicate element
    @Test
    public void testSetDuplicate() {
        String old = list.set(1, "A");
        assertEquals("B", old);
        assertEquals(2, list.size()); // Duplicate removed, only "A" and "C"
        assertEquals("A", list.get(1));
    }

    // Test set() - setting null
    @Test
    public void testSetNull() {
        String old = list.set(0, null);
        assertEquals("A", old);
        assertTrue(list.contains(null));
        assertEquals(3, list.size());
    }

    // Test set() - index out of bounds
    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetIndexOutOfBounds() {
        list.set(5, "Z");
    }

    // Test get() and contains
    @Test
    public void testGetAndContains() {
        assertEquals("A", list.get(0));
        assertTrue(list.contains("A"));
        assertFalse(list.contains("Z"));
    }

    // Test remove() - removing existing element
    @Test
    public void testRemoveExisting() {
        assertTrue(list.remove("B"));
        assertEquals(2, list.size());
    }

    // Test remove() - removing non-existing element
    @Test
    public void testRemoveNonExisting() {
        assertFalse(list.remove("Z"));
        assertEquals(3, list.size());
    }

    // Test remove() - removing at index
    @Test
    public void testRemoveIndex() {
        String removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(2, list.size());
    }

    // Test containsAll()
    @Test
    public void testContainsAll() {
        assertTrue(list.containsAll(Arrays.asList("A", "B")));
        assertFalse(list.containsAll(Arrays.asList("A", "Z")));
    }

    // Test addAll at index - unique elements
    @Test
    public void testAddAllAtIndexUnique() {
        assertTrue(list.addAll(1, uniqueList));
        assertEquals(5, list.size());
        assertEquals("X", list.get(1));
    }

    // Test addAll at index - with duplicates
    @Test
    public void testAddAllAtIndexWithDuplicates() {
        List<String> dups = Arrays.asList("A", "D");
        assertTrue(list.addAll(1, dups));
        assertEquals(4, list.size());
        assertEquals("D", list.get(1));
    }

    // Test addAll at index - empty collection
    @Test
    public void testAddAllAtIndexEmpty() {
        assertFalse(list.addAll(1, new ArrayList<>()));
        assertEquals(3, list.size());
    }

    // Test subList() - basics
    @Test
    public void testSubList() {
        List<String> sub = list.subList(0, 2);
        assertEquals(2, sub.size());
        assertEquals("A", sub.get(0));
        assertEquals("B", sub.get(1));
    }

    // Test subList() - view consistency
    @Test
    public void testSubListView() {
        List<String> sub = list.subList(0, 2);
        list.add(1, "D"); // Insert "D" after "A"
        assertEquals(3, sub.size()); // Sub-list now "A", "D", "B"
        assertEquals("D", sub.get(1));
    }

    // Test retainAll() - keeping some elements
    @Test
    public void testRetainAll() {
        assertTrue(list.retainAll(Arrays.asList("A", "B")));
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    // Test retainAll() - keeping all
    @Test
    public void testRetainAllNoChange() {
        assertFalse(list.retainAll(Arrays.asList("A", "B", "C")));
        assertEquals(3, list.size());
    }

    // Test removeAll()
    @Test
    public void testRemoveAll() {
        assertTrue(list.removeAll(Arrays.asList("A", "C")));
        assertEquals(1, list.size());
        assertTrue(list.contains("B"));
    }

    // Test removeAll() - non existing
    @Test
    public void testRemoveAllNonExisting() {
        assertFalse(list.removeAll(Arrays.asList("X", "Y")));
        assertEquals(3, list.size());
    }

    // Test clear()
    @Test
    public void testClear() {
        list.clear();
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
    }

    // Test iterator remove
    @Test
    public void testIteratorRemove() {
        var it = list.iterator();
        it.next();
        it.remove();
        assertEquals(2, list.size());
        assertFalse(list.contains("A"));
    }

    // Test setUniqueList with null list
    @Test(expected = NullPointerException.class)
    public void testSetUniqueListNull() {
        SetUniqueList.setUniqueList(null);
    }

    // Test setUniqueList with duplicates in base list
    @Test
    public void testSetUniqueListWithDuplicates() {
        List<String> base = new ArrayList<>();
        base.add("A");
        base.add("B");
        base.add("A");
        SetUniqueList<String> sul = SetUniqueList.setUniqueList(base);
        assertEquals(2, sul.size());
        assertTrue(sul.contains("A"));
        assertTrue(sul.contains("B"));
    }

    // Test ensureCapacity and trimToSize
    @Test
    public void testEnsureCapacityAndTrimToSize() {
        ((ArrayList<String>) list.decorated()).ensureCapacity(100);
        ((ArrayList<String>) list.decorated()).trimToSize();
        assertNotNull(list);
    }

    // Test constructor with set
    @Test
    public void testConstructorWithSet() {
        Set<String> set = new HashSet<>(Arrays.asList("A", "B", "C"));
        List<String> base = new ArrayList<>(set);
        SetUniqueList<String> sul = SetUniqueList.setUniqueList(base);
        assertEquals(3, sul.size());
    }

    // Test edge case: adding and removing null
    @Test
    public void testAddRemoveNull() {
        list.add(null);
        assertTrue(list.contains(null));
        assertTrue(list.remove(null));
        assertFalse(list.contains(null));
        assertEquals(3, list.size());
    }

    // Test indexOf and lastIndexOf
    @Test
    public void testIndexOf() {
        assertEquals(0, list.indexOf("A"));
        assertEquals(-1, list.indexOf("Z"));
        assertEquals(0, list.lastIndexOf("A")); // Unique list, so same
    }

    // Test toArray methods
    @Test
    public void testToArray() {
        Object[] arr = list.toArray();
        assertEquals(3, arr.length);
        assertEquals("A", arr[0]);

        String[] typedArr = list.toArray(new String[0]);
        assertEquals(3, typedArr.length);
        assertEquals("B", typedArr[1]);
    }

    // Test listIterator set
    @Test
    public void testListIteratorSet() {
        var it = list.listIterator();
        it.next();
        it.set("Z");
        assertEquals(3, list.size());
        assertEquals("Z", list.get(0));
    }

    // Test listIterator add
    @Test
    public void testListIteratorAdd() {
        var it = list.listIterator();
        it.next();
        it.add("Z");
        assertEquals(4, list.size());
        assertEquals("Z", list.get(1));
    }

    // Test equals and hashCode
    @Test
    public void testEqualsAndHashCode() {
        List<String> other = new ArrayList<>(Arrays.asList("A", "B", "C"));
        assertEquals(list, other);
        assertEquals(list.hashCode(), other.hashCode());
    }

    // Test toString
    @Test
    public void testToString() {
        String str = list.toString();
        assertTrue(str.contains("A"));
        assertTrue(str.contains("B"));
        assertTrue(str.contains("C"));
    }

    // Test isEmpty
    @Test
    public void testIsEmpty() {
        assertFalse(list.isEmpty());
        list.clear();
        assertTrue(list.isEmpty());
    }

    // Test size after operations
    @Test
    public void testSizeAfterOperations() {
        assertEquals(3, list.size());
        list.add("D");
        assertEquals(4, list.size());
        list.remove("A");
        assertEquals(3, list.size());
    }

    // Test that setUniqueList returns SetUniqueList instance
    @Test
    public void testSetUniqueListReturnType() {
        List<String> base = new ArrayList<>();
        base.add("X");
        SetUniqueList<String> sul = SetUniqueList.setUniqueList(base);
        assertNotNull(sul);
    }

    // Test edge: set at index 0
    @Test
    public void testSetFirstElement() {
        list.set(0, "D");
        assertEquals("D", list.get(0));
        assertEquals(3, list.size());
    }

    // Test edge: set at last index
    @Test
    public void testSetLastElement() {
        list.set(2, "D");
        assertEquals("D", list.get(2));
        assertEquals(3, list.size());
    }
}