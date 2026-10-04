package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Test suite for SetUniqueList.
 * Designed to achieve high code coverage and detect potential faults.
 */
public class SetUniqueListTest {

    private SetUniqueList<String> list;

    @Before
    public void setUp() {
        list = SetUniqueList.decorate(new ArrayList<String>());
    }

    // ---------- Basic add and uniqueness ----------
    @Test
    public void testAddUniqueElements() {
        assertTrue(list.add("A"));
        assertTrue(list.add("B"));
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("B"));
    }

    @Test
    public void testAddDuplicateElement() {
        list.add("A");
        assertFalse(list.add("A"));
        assertEquals(1, list.size());
    }

    @Test
    public void testAddAtIndexUnique() {
        list.add("A");
        list.add("C");
        list.add(1, "B");
        assertEquals(Arrays.asList("A", "B", "C"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAtIndexDuplicate() {
        list.add("A");
        list.add("B");
        list.add(1, "A"); // duplicate, should throw or return false? Typically throws IllegalArgumentException
    }

    // ---------- addAll ----------
    @Test
    public void testAddAllUniqueCollection() {
        list.add("A");
        assertTrue(list.addAll(Arrays.asList("B", "C")));
        assertEquals(3, list.size());
    }

    @Test
    public void testAddAllWithDuplicates() {
        list.add("A");
        list.add("B");
        assertFalse(list.addAll(Arrays.asList("A", "B")));
        assertEquals(2, list.size());
    }

    @Test
    public void testAddAllAtIndexUnique() {
        list.add("A");
        list.add("D");
        assertTrue(list.addAll(1, Arrays.asList("B", "C")));
        assertEquals(Arrays.asList("A", "B", "C", "D"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddAllAtIndexWithDuplicate() {
        list.add("A");
        list.add("B");
        list.addAll(1, Arrays.asList("A", "C")); // duplicate A
    }

    // ---------- set ----------
    @Test
    public void testSetUnique() {
        list.add("A");
        list.add("B");
        String old = list.set(0, "C");
        assertEquals("A", old);
        assertEquals(Arrays.asList("C", "B"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDuplicate() {
        list.add("A");
        list.add("B");
        list.set(0, "B"); // duplicate
    }

    // ---------- remove ----------
    @Test
    public void testRemoveByObject() {
        list.add("A");
        list.add("B");
        assertTrue(list.remove("A"));
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
    }

    @Test
    public void testRemoveByIndex() {
        list.add("A");
        list.add("B");
        String removed = list.remove(0);
        assertEquals("A", removed);
        assertEquals(1, list.size());
    }

    @Test
    public void testRemoveAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.removeAll(Arrays.asList("A", "C")));
        assertEquals(1, list.size());
        assertTrue(list.contains("B"));
    }

    @Test
    public void testRemoveAllNoMatch() {
        list.add("A");
        assertFalse(list.removeAll(Arrays.asList("B")));
        assertEquals(1, list.size());
    }

    // ---------- retainAll ----------
    @Test
    public void testRetainAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        assertTrue(list.retainAll(Arrays.asList("A", "C")));
        assertEquals(2, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
    }

    @Test
    public void testRetainAllNoChange() {
        list.add("A");
        assertFalse(list.retainAll(Arrays.asList("A")));
        assertEquals(1, list.size());
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

    // ---------- contains and containsAll ----------
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

    // ---------- indexOf and lastIndexOf ----------
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
        list.add("A"); // duplicate not allowed, so only one A
        assertEquals(0, list.lastIndexOf("A"));
        assertEquals(1, list.lastIndexOf("B"));
    }

    // ---------- iterator ----------
    @Test
    public void testIteratorRemove() {
        list.add("A");
        list.add("B");
        Iterator<String> it = list.iterator();
        it.next();
        it.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModification() {
        list.add("A");
        Iterator<String> it = list.iterator();
        list.add("B");
        it.next(); // should throw
    }

    // ---------- listIterator ----------
    @Test
    public void testListIteratorAdd() {
        list.add("A");
        list.add("C");
        ListIterator<String> it = list.listIterator(1);
        it.add("B");
        assertEquals(Arrays.asList("A", "B", "C"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListIteratorAddDuplicate() {
        list.add("A");
        list.add("B");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.add("A"); // duplicate
    }

    @Test
    public void testListIteratorSet() {
        list.add("A");
        list.add("B");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.set("C");
        assertEquals(Arrays.asList("C", "B"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListIteratorSetDuplicate() {
        list.add("A");
        list.add("B");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.set("B"); // duplicate
    }

    @Test
    public void testListIteratorRemove() {
        list.add("A");
        list.add("B");
        ListIterator<String> it = list.listIterator();
        it.next();
        it.remove();
        assertEquals(1, list.size());
        assertFalse(list.contains("A"));
    }

    // ---------- subList ----------
    @Test
    public void testSubListGet() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(0, 2);
        assertEquals(Arrays.asList("A", "B"), sub);
    }

    @Test
    public void testSubListAdd() {
        list.add("A");
        list.add("C");
        List<String> sub = list.subList(0, 1);
        sub.add("B");
        assertEquals(Arrays.asList("A", "B", "C"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListAddDuplicate() {
        list.add("A");
        list.add("B");
        List<String> sub = list.subList(0, 1);
        sub.add("B"); // duplicate in parent
    }

    @Test
    public void testSubListRemove() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 3);
        sub.remove("B");
        assertEquals(Arrays.asList("A", "C"), list);
    }

    @Test
    public void testSubListClear() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 3);
        sub.clear();
        assertEquals(1, list.size());
        assertTrue(list.contains("A"));
    }

    @Test
    public void testSubListSet() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 2);
        sub.set(0, "D");
        assertEquals(Arrays.asList("A", "D", "C"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListSetDuplicate() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 2);
        sub.set(0, "A"); // duplicate
    }

    // ---------- edge cases ----------
    @Test
    public void testEmptyList() {
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertFalse(list.contains("A"));
        assertEquals(-1, list.indexOf("A"));
    }

    @Test
    public void testNullElement() {
        // Assuming null is allowed (check SetUniqueList behavior)
        list.add(null);
        assertTrue(list.contains(null));
        assertEquals(1, list.size());
        assertFalse(list.add(null)); // duplicate null
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexNegative() {
        list.add(-1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexTooLarge() {
        list.add(1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {
        list.get(0);
    }

    @Test
    public void testToArray() {
        list.add("A");
        list.add("B");
        Object[] arr = list.toArray();
        assertArrayEquals(new Object[]{"A", "B"}, arr);
    }

    @Test
    public void testToArrayWithArg() {
        list.add("A");
        list.add("B");
        String[] arr = list.toArray(new String[0]);
        assertArrayEquals(new String[]{"A", "B"}, arr);
    }

    @Test
    public void testHashCodeAndEquals() {
        list.add("A");
        list.add("B");
        SetUniqueList<String> other = SetUniqueList.decorate(new ArrayList<String>());
        other.add("A");
        other.add("B");
        assertEquals(list, other);
        assertEquals(list.hashCode(), other.hashCode());
    }

    @Test
    public void testSubListStructuralChangeReflectedInParent() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(0, 2);
        sub.add(1, "D");
        assertEquals(Arrays.asList("A", "D", "B", "C"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListAddAllWithDuplicate() {
        list.add("A");
        list.add("B");
        List<String> sub = list.subList(0, 1);
        sub.addAll(Arrays.asList("B", "C")); // B is duplicate
    }

    // ---------- bulk operations on subList ----------
    @Test
    public void testSubListRetainAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 3);
        sub.retainAll(Arrays.asList("B"));
        assertEquals(Arrays.asList("A", "B"), list);
    }

    @Test
    public void testSubListRemoveAll() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 3);
        sub.removeAll(Arrays.asList("B"));
        assertEquals(Arrays.asList("A", "C"), list);
    }

    // ---------- iterator on subList ----------
    @Test
    public void testSubListIteratorRemove() {
        list.add("A");
        list.add("B");
        list.add("C");
        List<String> sub = list.subList(1, 3);
        Iterator<String> it = sub.iterator();
        it.next();
        it.remove();
        assertEquals(Arrays.asList("A", "C"), list);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListIteratorAddDuplicate() {
        list.add("A");
        list.add("B");
        List<String> sub = list.subList(0, 1);
        ListIterator<String> it = sub.listIterator();
        it.add("B"); // duplicate
    }

    // ---------- test that set is consistent ----------
    @Test
    public void testSetConsistencyAfterMultipleOperations() {
        list.add("A");
        list.add("B");
        list.add("C");
        list.remove("B");
        list.add("D");
        list.add(1, "E");
        assertEquals(4, list.size());
        assertTrue(list.contains("A"));
        assertTrue(list.contains("C"));
        assertTrue(list.contains("D"));
        assertTrue(list.contains("E"));
        assertFalse(list.contains("B"));
    }

    @Test
    public void testDecoratedListMaintainsOrder() {
        List<String> decorated = new ArrayList<>();
        decorated.add("B");
        decorated.add("A");
        SetUniqueList<String> sul = SetUniqueList.decorate(decorated);
        assertEquals(Arrays.asList("B", "A"), sul);
        sul.add("C");
        assertEquals(Arrays.asList("B", "A", "C"), sul);
    }
}