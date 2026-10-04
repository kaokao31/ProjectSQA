package org.apache.commons.collections.list;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class TreeListTest {

    private TreeList emptyList;
    private TreeList singleList;
    private TreeList multiList;
    private static final Object A = "A";
    private static final Object B = "B";
    private static final Object C = "C";
    private static final Object D = "D";

    @Before
    public void setUp() {
        emptyList = new TreeList();
        singleList = new TreeList();
        singleList.add(A);
        multiList = new TreeList();
        multiList.addAll(Arrays.asList(A, B, C, D));
    }

    // ======================== Basic Operations ========================

    @Test
    public void testEmptyListSize() {
        assertEquals(0, emptyList.size());
        assertTrue(emptyList.isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetFromEmpty() {
        emptyList.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndex() {
        multiList.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetBeyondSize() {
        multiList.get(multiList.size());
    }

    @Test
    public void testGetSingle() {
        assertEquals(A, singleList.get(0));
    }

    @Test
    public void testGetMulti() {
        assertEquals(A, multiList.get(0));
        assertEquals(B, multiList.get(1));
        assertEquals(C, multiList.get(2));
        assertEquals(D, multiList.get(3));
    }

    @Test
    public void testAddAtEnd() {
        assertTrue(emptyList.add(E));
        assertEquals(1, emptyList.size());
        assertEquals(E, emptyList.get(0));

        assertTrue(singleList.add(F));
        assertEquals(2, singleList.size());
        assertEquals(F, singleList.get(1));
    }

    @Test
    public void testAddAtIndex() {
        // add at beginning
        emptyList.add(0, X);
        assertEquals(1, emptyList.size());
        assertEquals(X, emptyList.get(0));

        // add in middle
        multiList.add(2, M);
        assertEquals(5, multiList.size());
        assertEquals(M, multiList.get(2));
        assertEquals(C, multiList.get(3)); // shifted

        // add at end
        multiList.add(multiList.size(), Z);
        assertEquals(6, multiList.size());
        assertEquals(Z, multiList.get(5));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexNegative() {
        emptyList.add(-1, X);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexTooLarge() {
        emptyList.add(1, X);
    }

    @Test
    public void testSet() {
        Object old = multiList.set(1, X);
        assertEquals(B, old);
        assertEquals(X, multiList.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetNegative() {
        multiList.set(-1, X);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetBeyondSize() {
        multiList.set(multiList.size(), X);
    }

    @Test
    public void testRemoveByIndex() {
        Object removed = multiList.remove(2);
        assertEquals(C, removed);
        assertEquals(3, multiList.size());
        assertEquals(D, multiList.get(2)); // shifted
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveNegative() {
        multiList.remove(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveBeyondSize() {
        multiList.remove(multiList.size());
    }

    @Test
    public void testRemoveByObject() {
        assertTrue(multiList.remove(C));
        assertEquals(3, multiList.size());
        assertFalse(multiList.contains(C));
        assertFalse(multiList.remove(null)); // no null
    }

    @Test
    public void testRemoveFirstOccurrence() {
        multiList.add(B); // add duplicate
        assertTrue(multiList.remove(B));
        assertEquals(4, multiList.size());
        assertEquals(B, multiList.get(1)); // second occurrence remains
    }

    // ======================== Bulk Operations ========================

    @Test
    public void testAddAll() {
        List<Object> toAdd = Arrays.asList(E, F);
        assertTrue(emptyList.addAll(toAdd));
        assertEquals(2, emptyList.size());
        assertEquals(E, emptyList.get(0));
        assertEquals(F, emptyList.get(1));

        // addAll at index
        List<Object> more = Arrays.asList(G, H);
        assertTrue(multiList.addAll(2, more));
        assertEquals(6, multiList.size());
        assertEquals(G, multiList.get(2));
        assertEquals(H, multiList.get(3));
        assertEquals(C, multiList.get(4));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAllAtNegativeIndex() {
        emptyList.addAll(-1, Arrays.asList(E));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAllAtBeyondSize() {
        emptyList.addAll(1, Arrays.asList(E));
    }

    @Test
    public void testAddAllEmptyCollection() {
        assertFalse(emptyList.addAll(new ArrayList<>()));
        assertEquals(0, emptyList.size());
    }

    @Test
    public void testRemoveAll() {
        List<Object> toRemove = Arrays.asList(A, C);
        assertTrue(multiList.removeAll(toRemove));
        assertEquals(2, multiList.size());
        assertFalse(multiList.contains(A));
        assertFalse(multiList.contains(C));
        // removeAll on empty
        assertFalse(emptyList.removeAll(toRemove));
    }

    @Test
    public void testRetainAll() {
        List<Object> toRetain = Arrays.asList(B, D);
        assertTrue(multiList.retainAll(toRetain));
        assertEquals(2, multiList.size());
        assertEquals(B, multiList.get(0));
        assertEquals(D, multiList.get(1));
    }

    @Test
    public void testClear() {
        multiList.clear();
        assertEquals(0, multiList.size());
        assertTrue(multiList.isEmpty());
        // clearing empty list
        emptyList.clear();
        assertEquals(0, emptyList.size());
    }

    // ======================== Search Operations ========================

    @Test
    public void testIndexOf() {
        assertEquals(0, multiList.indexOf(A));
        assertEquals(2, multiList.indexOf(C));

        // duplicate
        multiList.add(B);
        assertEquals(1, multiList.indexOf(B)); // first occurrence

        // not found
        assertEquals(-1, multiList.indexOf("Z"));
        assertEquals(-1, emptyList.indexOf(A));
    }

    @Test
    public void testLastIndexOf() {
        multiList.add(A);
        assertEquals(4, multiList.lastIndexOf(A)); // last occurrence
        assertEquals(3, multiList.lastIndexOf(D));
        assertEquals(-1, multiList.lastIndexOf("Z"));
        assertEquals(-1, emptyList.lastIndexOf(A));
    }

    @Test
    public void testContains() {
        assertTrue(multiList.contains(B));
        assertFalse(multiList.contains("Z"));
        assertFalse(emptyList.contains(A));
    }

    @Test
    public void testContainsAll() {
        assertTrue(multiList.containsAll(Arrays.asList(A, C)));
        assertFalse(multiList.containsAll(Arrays.asList(A, "Z")));
        assertTrue(emptyList.containsAll(new ArrayList<>()));
    }

    // ======================== Iterator / ListIterator ========================

    @Test(expected = NoSuchElementException.class)
    public void testIteratorOnEmpty() {
        emptyList.iterator().next();
    }

    @Test
    public void testIterator() {
        java.util.Iterator<Object> it = multiList.iterator();
        assertTrue(it.hasNext());
        assertEquals(A, it.next());
        assertEquals(B, it.next());
        it.remove();
        assertEquals(3, multiList.size());
        assertFalse(multiList.contains(B));
    }

    @Test
    public void testListIteratorForward() {
        ListIterator<Object> it = multiList.listIterator();
        assertEquals(0, it.nextIndex());
        assertEquals(-1, it.previousIndex());
        assertTrue(it.hasNext());
        assertEquals(A, it.next());
        assertEquals(1, it.nextIndex());
        assertEquals(0, it.previousIndex());
    }

    @Test
    public void testListIteratorBackward() {
        ListIterator<Object> it = multiList.listIterator(multiList.size());
        assertEquals(4, it.nextIndex());
        assertEquals(3, it.previousIndex());
        assertTrue(it.hasPrevious());
        assertEquals(D, it.previous());
        assertEquals(3, it.nextIndex());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorNegativeIndex() {
        multiList.listIterator(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorBeyondSize() {
        multiList.listIterator(multiList.size() + 1);
    }

    @Test
    public void testListIteratorAdd() {
        ListIterator<Object> it = multiList.listIterator(2);
        it.add(M);
        assertEquals(5, multiList.size());
        assertEquals(M, multiList.get(2));
        // after add, iterator position is after the added element
        assertEquals(C, it.next()); // C was at index 3, now at 4? Actually after add, C shifts to index 3
    }

    @Test
    public void testListIteratorSet() {
        ListIterator<Object> it = multiList.listIterator(1);
        it.next(); // moves to B
        it.set(X);
        assertEquals(X, multiList.get(1));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModification() {
        java.util.Iterator<Object> it = multiList.iterator();
        multiList.add(E);
        it.next(); // should throw
    }

    // ======================== SubList ========================

    @Test
    public void testSubList() {
        List<Object> sub = multiList.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals(B, sub.get(0));
        assertEquals(C, sub.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListNegativeFrom() {
        multiList.subList(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListToIndexTooLarge() {
        multiList.subList(2, multiList.size() + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListFromGreaterThanTo() {
        multiList.subList(3, 2);
    }

    @Test
    public void testSubListClear() {
        List<Object> sub = multiList.subList(1, 3);
        sub.clear();
        assertEquals(2, multiList.size());
        assertEquals(A, multiList.get(0));
        assertEquals(D, multiList.get(1));
    }

    @Test
    public void testSubListAdd() {
        List<Object> sub = multiList.subList(1, 3);
        sub.add(1, X);
        assertEquals(3, sub.size());
        assertEquals(5, multiList.size());
        assertEquals(X, multiList.get(2));
        assertEquals(C, multiList.get(3));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testSubListConcurrentModification() {
        List<Object> sub = multiList.subList(1, 3);
        multiList.add(E);
        sub.size(); // should throw
    }

    // ======================== ToString / Equals / HashCode ========================

    @Test
    public void testToString() {
        assertEquals("[]", emptyList.toString());
        assertEquals("[A, B, C, D]", multiList.toString());
    }

    @Test
    public void testEquals() {
        assertEquals(emptyList, new TreeList());
        assertEquals(multiList, new TreeList() {{ addAll(Arrays.asList(A, B, C, D)); }});
        assertNotEquals(multiList, singleList);
    }

    @Test
    public void testHashCode() {
        assertEquals(emptyList.hashCode(), new TreeList().hashCode());
        assertEquals(multiList.hashCode(), new TreeList() {{ addAll(Arrays.asList(A, B, C, D)); }}.hashCode());
    }

    // ======================== Edge Cases and Bug Triggers ========================

    @Test
    public void testAddAllLargeNumberOfElements() {
        // Potential bug: addAll with many elements causing internal tree imbalance
        TreeList list = new TreeList();
        List<Object> many = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            many.add(i);
        }
        list.addAll(many);
        assertEquals(1000, list.size());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, list.get(i));
        }
        // remove from middle
        list.remove(500);
        assertEquals(999, list.size());
        assertEquals(501, list.get(500));
    }

    @Test
    public void testAddAtIndexPerformanceAndCorrectness() {
        TreeList list = new TreeList();
        // Insert at various positions to exercise tree rotations
        list.add(0, 1);
        list.add(1, 3);
        list.add(1, 2);
        assertEquals(Arrays.asList(1, 2, 3), list);
        // Further insert at beginning
        list.add(0, 0);
        assertEquals(Arrays.asList(0, 1, 2, 3), list);
    }

    @Test
    public void testRemoveAllRemovesAllOccurrences() {
        TreeList list = new TreeList();
        list.addAll(Arrays.asList(A, B, A, C, A));
        list.removeAll(Arrays.asList(A));
        assertEquals(2, list.size());
        assertEquals(B, list.get(0));
        assertEquals(C, list.get(1));
    }

    @Test
    public void testAddNullValues() {
        emptyList.add(null);
        assertEquals(1, emptyList.size());
        assertNull(emptyList.get(0));
        assertTrue(emptyList.contains(null));
        emptyList.add(0, null);
        assertEquals(2, emptyList.size());
        assertNull(emptyList.get(0));
        assertNull(emptyList.get(1));
    }

    @Test(expected = ClassCastException.class)
    public void testToArrayWithTypedArray() {
        // TreeList does not support generic array creation? Actually it does via toArray(T[])
        // But we'll just test normal toArray
        Object[] arr = multiList.toArray();
        assertEquals(4, arr.length);
        assertEquals(A, arr[0]);
        // The following might fail if TreeList has a bug in toArray with typed array
        String[] typed = new String[4];
        // This should work because elements are strings
        multiList.toArray(typed);
        assertEquals("A", typed[0]);
    }

    @Test
    public void testIteratorRemoveFirst() {
        java.util.Iterator<Object> it = multiList.iterator();
        it.next();
        it.remove();
        assertEquals(3, multiList.size());
        assertEquals(B, multiList.get(0));
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveWithoutNext() {
        java.util.Iterator<Object> it = multiList.iterator();
        it.remove();
    }

    @Test
    public void testListIteratorPreviousIndex() {
        ListIterator<Object> it = multiList.listIterator();
        assertEquals(-1, it.previousIndex());
        it.next();
        assertEquals(0, it.previousIndex());
        it.previous();
        assertEquals(-1, it.previousIndex());
    }

    // ======================== Bug-Specific Test (addAll with index) ========================

    @Test
    public void testAddAllAtIndexWithMultipleElements() {
        // Bug ID 20 might involve corruption when addAll at index with large collection
        TreeList list = new TreeList();
        list.add(A);
        list.add(C);
        // Insert B and B2 at index 1
        List<Object> mid = Arrays.asList(B, "B2");
        list.addAll(1, mid);
        assertEquals(4, list.size());
        assertEquals(A, list.get(0));
        assertEquals(B, list.get(1));
        assertEquals("B2", list.get(2));
        assertEquals(C, list.get(3));
    }

    @Test
    public void testSubListAddAll() {
        TreeList list = new TreeList();
        list.addAll(Arrays.asList(1, 4, 5));
        List<Object> sub = list.subList(1, 3);
        sub.addAll(1, Arrays.asList(2, 3));
        assertEquals(5, list.size());
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void testMultipleModificationsThroughSubList() {
        TreeList list = new TreeList();
        list.addAll(Arrays.asList(0, 1, 2, 3, 4));
        List<Object> sub = list.subList(1, 4);
        sub.remove(1); // removes 2
        assertEquals(4, list.size());
        assertEquals(Arrays.asList(0, 1, 3, 4), list);
        sub.add(1, 2);
        assertEquals(5, list.size());
        assertEquals(Arrays.asList(0, 1, 2, 3, 4), list);
    }
}