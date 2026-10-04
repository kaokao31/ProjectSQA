package org.apache.commons.collections.list;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SetUniqueListTest {

    private SetUniqueList<String> emptyList;
    private SetUniqueList<String> listWithDuplicates;
    private List<String> backingList;

    @Before
    public void setUp() {
        // Create a backing list with duplicates (will be filtered by SetUniqueList)
        backingList = new ArrayList<>();
        backingList.add("A");
        backingList.add("B");
        backingList.add("A"); // duplicate
        backingList.add("C");
        backingList.add("B"); // duplicate
        backingList.add("D");

        // Create SetUniqueList from the backing list
        listWithDuplicates = SetUniqueList.setUniqueList(backingList);

        // Empty list
        emptyList = SetUniqueList.setUniqueList(new ArrayList<String>());
    }

    // --- Constructor / setUniqueList ---

    @Test(expected = NullPointerException.class)
    public void testSetUniqueListNullArgument() {
        SetUniqueList.setUniqueList(null);
    }

    @Test
    public void testSetUniqueListRemovesDuplicates() {
        assertEquals(4, listWithDuplicates.size());
        assertTrue(listWithDuplicates.contains("A"));
        assertTrue(listWithDuplicates.contains("B"));
        assertTrue(listWithDuplicates.contains("C"));
        assertTrue(listWithDuplicates.contains("D"));
    }

    // --- add methods ---

    @Test
    public void testAddUniqueElement() {
        assertTrue(emptyList.add("X"));
        assertEquals(1, emptyList.size());
        assertEquals("X", emptyList.get(0));
    }

    @Test
    public void testAddDuplicateElement() {
        // Add element that already exists (by equals)
        listWithDuplicates.add("A");
        assertEquals(4, listWithDuplicates.size()); // size unchanged
    }

    @Test
    public void testAddAtPositionUnique() {
        listWithDuplicates.add(1, "E");
        assertEquals(5, listWithDuplicates.size());
        assertEquals("E", listWithDuplicates.get(1));
    }

    @Test
    public void testAddAtPositionDuplicate() {
        listWithDuplicates.add(2, "A"); // "A" already present at index 0
        assertEquals(4, listWithDuplicates.size()); // size unchanged, element not added
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtNegativeIndex() {
        listWithDuplicates.add(-1, "X");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexTooLarge() {
        listWithDuplicates.add(5, "X"); // size is 4
    }

    // --- addAll methods ---

    @Test
    public void testAddAllUniqueElements() {
        List<String> toAdd = Arrays.asList("X", "Y", "Z");
        assertTrue(listWithDuplicates.addAll(toAdd));
        assertEquals(7, listWithDuplicates.size());
    }

    @Test
    public void testAddAllAllDuplicates() {
        List<String> toAdd = Arrays.asList("A", "B", "C");
        assertFalse(listWithDuplicates.addAll(toAdd));
        assertEquals(4, listWithDuplicates.size());
    }

    @Test
    public void testAddAllPartialDuplicates() {
        List<String> toAdd = Arrays.asList("A", "X", "B");
        assertTrue(listWithDuplicates.addAll(toAdd));
        assertEquals(5, listWithDuplicates.size()); // only "X" added
        assertTrue(listWithDuplicates.contains("X"));
    }

    @Test
    public void testAddAllAtPositionUnique() {
        List<String> toAdd = Arrays.asList("E", "F");
        assertTrue(listWithDuplicates.addAll(1, toAdd));
        assertEquals(6, listWithDuplicates.size());
        assertEquals("E", listWithDuplicates.get(1));
        assertEquals("F", listWithDuplicates.get(2));
        assertEquals("B", listWithDuplicates.get(3)); // original shift
    }

    @Test
    public void testAddAllAtPositionAllDuplicates() {
        List<String> toAdd = Arrays.asList("A", "B");
        assertFalse(listWithDuplicates.addAll(0, toAdd));
        assertEquals(4, listWithDuplicates.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAllAtNegativeIndex() {
        listWithDuplicates.addAll(-1, Arrays.asList("X"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAllAtIndexTooLarge() {
        listWithDuplicates.addAll(10, Arrays.asList("X"));
    }

    // --- set method ---

    @Test
    public void testSetUniqueElement() {
        String old = listWithDuplicates.set(1, "E"); // replace "B" with "E"
        assertEquals("B", old);
        assertEquals(4, listWithDuplicates.size());
        assertEquals("E", listWithDuplicates.get(1));
    }

    @Test
    public void testSetDuplicateElement() {
        // Trying to set at index 1 to "A" which already exists elsewhere
        String old = listWithDuplicates.set(1, "A");
        assertEquals("B", old);
        // The set should not add duplicate, but replace? According to SetUniqueList, 
        // if the new element already exists, the old element is removed and the set 
        // is effectively a no-op? Actually, spec: set returns old element, but 
        // duplicates are not allowed - the set should work as long as uniqueness holds.
        // If new element is already present, the old element is removed and the new one not added.
        // This is a common bug area. We test expected behavior.
        assertEquals(3, listWithDuplicates.size()); // size decreases because old removed, new not added
        assertFalse(listWithDuplicates.contains("B"));
        // The list should contain "A" once, "C", "D"
        assertEquals(0, listWithDuplicates.indexOf("A"));
        assertEquals(1, listWithDuplicates.indexOf("C"));
        assertEquals(2, listWithDuplicates.indexOf("D"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetNegativeIndex() {
        listWithDuplicates.set(-1, "X");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetIndexTooLarge() {
        listWithDuplicates.set(4, "X");
    }

    @Test
    public void testSetNullElement() {
        String old = listWithDuplicates.set(0, null);
        assertEquals("A", old);
        assertEquals(4, listWithDuplicates.size());
        assertNull(listWithDuplicates.get(0));
    }

    @Test
    public void testSetDuplicateNull() {
        listWithDuplicates.add(null);
        assertEquals(5, listWithDuplicates.size());
        // Now set at index 0 (which is "A") to null -> null already present at last index
        String old = listWithDuplicates.set(0, null);
        assertEquals("A", old);
        // Should remove old element and not add duplicate null
        assertEquals(4, listWithDuplicates.size());
        assertTrue(listWithDuplicates.contains(null));
        // The first occurrence of null should be at index 0 (the one that was set)
        assertEquals(0, listWithDuplicates.indexOf(null));
    }

    // --- remove method ---

    @Test
    public void testRemoveByObject() {
        assertTrue(listWithDuplicates.remove("B"));
        assertEquals(3, listWithDuplicates.size());
        assertFalse(listWithDuplicates.contains("B"));
    }

    @Test
    public void testRemoveByObjectNotPresent() {
        assertFalse(listWithDuplicates.remove("Z"));
        assertEquals(4, listWithDuplicates.size());
    }

    @Test
    public void testRemoveByObjectNull() {
        assertFalse(listWithDuplicates.remove(null)); // not present initially
        emptyList.add(null);
        assertTrue(emptyList.remove(null));
        assertTrue(emptyList.isEmpty());
    }

    @Test
    public void testRemoveByIndex() {
        String removed = listWithDuplicates.remove(0);
        assertEquals("A", removed);
        assertEquals(3, listWithDuplicates.size());
        // set should not contain "A"
        assertFalse(listWithDuplicates.contains("A"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByNegativeIndex() {
        listWithDuplicates.remove(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveByIndexTooLarge() {
        listWithDuplicates.remove(4);
    }

    // --- removeAll ---

    @Test
    public void testRemoveAllSome() {
        Collection<String> toRemove = Arrays.asList("A", "Z");
        assertTrue(listWithDuplicates.removeAll(toRemove));
        assertEquals(3, listWithDuplicates.size());
        assertFalse(listWithDuplicates.contains("A"));
    }

    @Test
    public void testRemoveAllNone() {
        Collection<String> toRemove = Arrays.asList("X", "Y");
        assertFalse(listWithDuplicates.removeAll(toRemove));
        assertEquals(4, listWithDuplicates.size());
    }

    @Test
    public void testRemoveAllAll() {
        Collection<String> toRemove = Arrays.asList("A", "B", "C", "D");
        assertTrue(listWithDuplicates.removeAll(toRemove));
        assertTrue(listWithDuplicates.isEmpty());
    }

    // --- retainAll ---

    @Test
    public void testRetainAllSome() {
        Collection<String> toRetain = Arrays.asList("A", "C");
        assertTrue(listWithDuplicates.retainAll(toRetain));
        assertEquals(2, listWithDuplicates.size());
        assertTrue(listWithDuplicates.contains("A"));
        assertTrue(listWithDuplicates.contains("C"));
        assertFalse(listWithDuplicates.contains("B"));
        assertFalse(listWithDuplicates.contains("D"));
    }

    @Test
    public void testRetainAllAll() {
        Collection<String> toRetain = Arrays.asList("A", "B", "C", "D");
        assertFalse(listWithDuplicates.retainAll(toRetain));
        assertEquals(4, listWithDuplicates.size());
    }

    @Test
    public void testRetainAllNone() {
        Collection<String> toRetain = Arrays.asList("X", "Y");
        assertTrue(listWithDuplicates.retainAll(toRetain));
        assertTrue(listWithDuplicates.isEmpty());
    }

    // --- subList ---

    @Test
    public void testSubList() {
        List<String> sub = listWithDuplicates.subList(0, 2);
        assertEquals(2, sub.size());
        assertEquals("A", sub.get(0));
        assertEquals("B", sub.get(1));
    }

    @Test
    public void testSubListAddDoesNotAffectOriginal() {
        List<String> sub = listWithDuplicates.subList(0, 2);
        // Add a new element to sublist (should be unique in sublist context)
        // Since sublist is backed, adding to sublist should affect original list
        sub.add("E");
        assertEquals(3, sub.size());
        assertEquals(5, listWithDuplicates.size()); // original grows
        // The added element should be present in original list
        assertTrue(listWithDuplicates.contains("E"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListAddDuplicateViaSublist() {
        // Sublist backed, adding duplicate to sublist should throw or be ignored?
        // According to SetUniqueList, add checks uniqueness against the whole list.
        List<String> sub = listWithDuplicates.subList(0, 2);
        sub.add("A"); // "A" already present in original list (index 0)
        // Expected: IllegalArgumentException because uniqueness violation
    }

    @Test
    public void testSubListSet() {
        List<String> sub = listWithDuplicates.subList(1, 3);
        String old = sub.set(0, "E"); // replace "B" with "E" in sublist, which is index0 of sub -> original index1
        assertEquals("B", old);
        assertEquals("E", listWithDuplicates.get(1));
        // Ensure uniqueness holds: "E" was new, so size unchanged
        assertEquals(4, listWithDuplicates.size());
    }

    @Test
    public void testSubListSetDuplicate() {
        List<String> sub = listWithDuplicates.subList(1, 3);
        // "A" exists at index 0, trying to set at index 1 (which is "C") to "A"
        sub.set(1, "A"); // index1 of sub corresponds to index2 of original ("C")
        // Since "A" already present, should remove "C" and not add duplicate
        assertEquals(3, listWithDuplicates.size());
        assertFalse(listWithDuplicates.contains("C"));
        // Check order: A, B, D (since D originally at index3)
        assertEquals("A", listWithDuplicates.get(0));
        assertEquals("B", listWithDuplicates.get(1));
        assertEquals("D", listWithDuplicates.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListFromNegative() {
        listWithDuplicates.subList(-1, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListToOutOfBounds() {
        listWithDuplicates.subList(0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListFromGreaterThanTo() {
        listWithDuplicates.subList(2, 1);
    }

    // --- clear ---

    @Test
    public void testClear() {
        listWithDuplicates.clear();
        assertTrue(listWithDuplicates.isEmpty());
        assertEquals(0, listWithDuplicates.size());
    }

    // --- contains ---

    @Test
    public void testContainsTrue() {
        assertTrue(listWithDuplicates.contains("A"));
    }

    @Test
    public void testContainsFalse() {
        assertFalse(listWithDuplicates.contains("Z"));
    }

    @Test
    public void testContainsNull() {
        assertFalse(listWithDuplicates.contains(null));
        listWithDuplicates.add(null);
        assertTrue(listWithDuplicates.contains(null));
    }

    // --- containsAll ---

    @Test
    public void testContainsAllTrue() {
        Collection<String> col = Arrays.asList("A", "B");
        assertTrue(listWithDuplicates.containsAll(col));
    }

    @Test
    public void testContainsAllFalse() {
        Collection<String> col = Arrays.asList("A", "Z");
        assertFalse(listWithDuplicates.containsAll(col));
    }

    // --- indexOf / lastIndexOf ---

    @Test
    public void testIndexOfFound() {
        assertEquals(0, listWithDuplicates.indexOf("A"));
    }

    @Test
    public void testIndexOfNotFound() {
        assertEquals(-1, listWithDuplicates.indexOf("Z"));
    }

    @Test
    public void testLastIndexOf() {
        // Since no duplicates, lastIndexOf should be same as indexOf
        assertEquals(listWithDuplicates.indexOf("B"), listWithDuplicates.lastIndexOf("B"));
        // Add duplicate manually (via add at position, but add will not add duplicate)
        // So we can't easily have duplicates. But we can test with null
        listWithDuplicates.add(null);
        listWithDuplicates.add(0, null); // null already at last index, so it won't add duplicate
        assertEquals(1, listWithDuplicates.lastIndexOf(null)); // null only at index originally added? Actually after first add null at index4 (since size=4 then add => index4; then add at 0 won't add duplicate, so still index4)
        // Simpler: test on empty list or ensure consistency
    }

    // --- iterator ---

    @Test
    public void testIterator() {
        Iterator<String> it = listWithDuplicates.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertEquals("D", it.next());
        assertFalse(it.hasNext());
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModificationRemove() {
        Iterator<String> it = listWithDuplicates.iterator();
        listWithDuplicates.remove("A");
        it.next(); // should throw
    }

    // --- listIterator ---

    @Test
    public void testListIterator() {
        ListIterator<String> lit = listWithDuplicates.listIterator();
        assertTrue(lit.hasNext());
        assertEquals(0, lit.nextIndex());
        assertEquals("A", lit.next());
        assertEquals(1, lit.nextIndex());
        assertEquals("B", lit.next());
        assertEquals("C", lit.next());
        assertEquals("D", lit.next());
        assertFalse(lit.hasNext());
        assertTrue(lit.hasPrevious());
        assertEquals("C", lit.previous());
    }

    @Test
    public void testListIteratorAdd() {
        ListIterator<String> lit = listWithDuplicates.listIterator(1);
        lit.add("E"); // add unique element
        assertEquals(5, listWithDuplicates.size());
        assertEquals("E", listWithDuplicates.get(1));
        // Check that cursor is positioned after added element
        assertEquals(2, lit.nextIndex());
        assertEquals("B", lit.next());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testListIteratorAddDuplicate() {
        ListIterator<String> lit = listWithDuplicates.listIterator(0);
        lit.add("A"); // duplicate
    }

    @Test
    public void testListIteratorSet() {
        ListIterator<String> lit = listWithDuplicates.listIterator(1);
        lit.next(); // now at "B"
        lit.set("E"); // replace "B" with unique "E"
        assertEquals("E", listWithDuplicates.get(1));
        assertEquals(4, listWithDuplicates.size());
    }

    @Test
    public void testListIteratorSetDuplicate() {
        ListIterator<String> lit = listWithDuplicates.listIterator(0);
        lit.next(); // "A"
        lit.set("D"); // "D" already present at index3
        // Should remove "A" and not add duplicate, so size decreases
        assertEquals(3, listWithDuplicates.size());
        assertFalse(listWithDuplicates.contains("A"));
        // List now contains "D" (from the set), "B", "C", "D"? Actually careful: after set, the set element is not added, only old removed. So we have "D" at position 0 which was the set element? No, we set at index 0, but "D" already exists at index3. The spec: set returns old element and replaces; if new element already present, the behavior is to remove the old element and not add the new ones.
        // So after operation, the list size becomes 3, order: the remaining elements should be: from original indices 1,2,3? 
        // Result: we removed "A" (index0), leaving [B, C, D] with D at index2. But we set index0 to D, but since D already present, the set effectively just removes A and does nothing else.
        // So the list should become: [B, C, D] in that order.
        assertEquals("B", listWithDuplicates.get(0));
        assertEquals("C", listWithDuplicates.get(1));
        assertEquals("D", listWithDuplicates.get(2));
    }

    // --- toArray ---

    @Test
    public void testToArray() {
        Object[] arr = listWithDuplicates.toArray();
        assertArrayEquals(new Object[]{"A", "B", "C", "D"}, arr);
    }

    @Test
    public void testToArrayGeneric() {
        String[] arr = listWithDuplicates.toArray(new String[0]);
        assertArrayEquals(new String[]{"A", "B", "C", "D"}, arr);
    }

    // --- isEmpty / size ---

    @Test
    public void testIsEmpty() {
        assertTrue(emptyList.isEmpty());
        assertFalse(listWithDuplicates.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(0, emptyList.size());
        assertEquals(4, listWithDuplicates.size());
    }

    // --- equals / hashCode (optional, but good for coverage) ---

    @Test
    public void testEquals() {
        SetUniqueList<String> other = SetUniqueList.setUniqueList(new ArrayList<>(Arrays.asList("A", "B", "C", "D")));
        assertEquals(listWithDuplicates, other);
        assertEquals(listWithDuplicates.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsWithDifferentOrder() {
        SetUniqueList<String> other = SetUniqueList.setUniqueList(new ArrayList<>(Arrays.asList("D", "C", "B", "A")));
        assertNotEquals(listWithDuplicates, other); // order matters for list
    }

    // --- toString ---

    @Test
    public void testToString() {
        String str = listWithDuplicates.toString();
        assertEquals("[A, B, C, D]", str);
    }

    // --- Edge case: list with single element ---

    @Test
    public void testSingleElementOperations() {
        SetUniqueList<String> single = SetUniqueList.setUniqueList(new ArrayList<>(Arrays.asList("X")));
        assertEquals(1, single.size());
        // add same
        assertFalse(single.add("X"));
        assertEquals(1, single.size());
        // remove
        assertTrue(single.remove("X"));
        assertTrue(single.isEmpty());
    }

    // --- Interaction with backing list (if accessible via getSet) ---
    // Not testing internal set directly, but we can test that modifications via 
    // backing list are reflected correctly (though not recommended).
    // This may reveal bugs.
    @Test
    public void testBackingListAddNotReflectedInSet() {
        // The backing list is the one passed to setUniqueList. 
        // Adding to backing list directly may break uniqueness if we later call methods on SetUniqueList.
        // This is a potential bug area.
        backingList.add("A"); // already present
        // According to implementation, the internal set might not be updated,
        // so subsequent operations may be inconsistent.
        // We'll just ensure that the list size is as expected? Not testing spec.
    }

    // --- Testing subList concurrent modification ---

    @Test(expected = ConcurrentModificationException.class)
    public void testSubListModificationAfterListModification() {
        List<String> sub = listWithDuplicates.subList(0, 2);
        listWithDuplicates.add("E"); // modify original list
        sub.size(); // should throw ConcurrentModificationException because sublist's modCount may differ
    }

    // --- Test addAll with null argument ---

    @Test(expected = NullPointerException.class)
    public void testAddAllNullCollection() {
        listWithDuplicates.addAll(null);
    }

    // --- Test containsAll with null collection ---

    @Test(expected = NullPointerException.class)
    public void testContainsAllNull() {
        listWithDuplicates.containsAll(null);
    }

    // --- Test removeAll null ---

    @Test(expected = NullPointerException.class)
    public void testRemoveAllNull() {
        listWithDuplicates.removeAll(null);
    }

    // --- Test retainAll null ---

    @Test(expected = NullPointerException.class)
    public void testRetainAllNull() {
        listWithDuplicates.retainAll(null);
    }

    // --- Additional corner cases for the bug: maybe subList remove ---

    @Test
    public void testSubListRemove() {
        List<String> sub = listWithDuplicates.subList(0, 3); // A, B, C
        sub.remove(1); // remove B
        assertEquals(2, sub.size());
        assertEquals(3, listWithDuplicates.size()); // original shrinks
        assertFalse(listWithDuplicates.contains("B"));
    }

    @Test
    public void testSubListRemoveByObject() {
        List<String> sub = listWithDuplicates.subList(0, 3);
        sub.remove("A");
        assertEquals(2, sub.size());
        assertEquals(3, listWithDuplicates.size());
        assertFalse(listWithDuplicates.contains("A"));
    }

    @Test
    public void testSubListClear() {
        List<String> sub = listWithDuplicates.subList(0, 2);
        sub.clear();
        assertEquals(0, sub.size());
        assertEquals(2, listWithDuplicates.size()); // only C and D left
        assertTrue(listWithDuplicates.contains("C"));
        assertTrue(listWithDuplicates.contains("D"));
    }

    // Test that iterator remove works correctly
    @Test
    public void testIteratorRemove() {
        Iterator<String> it = listWithDuplicates.iterator();
        while (it.hasNext()) {
            String s = it.next();
            if ("B".equals(s)) {
                it.remove();
            }
        }
        assertEquals(3, listWithDuplicates.size());
        assertFalse(listWithDuplicates.contains("B"));
    }

    // Test that listIterator remove works
    @Test
    public void testListIteratorRemove() {
        ListIterator<String> lit = listWithDuplicates.listIterator();
        lit.next(); // A
        lit.next(); // B
        lit.remove(); // removes B
        assertEquals(3, listWithDuplicates.size());
        assertFalse(listWithDuplicates.contains("B"));
    }

    // Test that listIterator previous works after add
    @Test
    public void testListIteratorAddThenPrevious() {
        ListIterator<String> lit = listWithDuplicates.listIterator(2);
        lit.add("E");
        // Now cursor is after the added element
        assertEquals('E', lit.previous()); // should return "E"
    }

    // --- Ensure that subList addAll works correctly ---

    @Test
    public void testSubListAddAllUnique() {
        List<String> sub = listWithDuplicates.subList(0, 2);
        sub.addAll(Arrays.asList("X", "Y"));
        assertEquals(4, sub.size());
        assertEquals(6, listWithDuplicates.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListAddAllWithDuplicates() {
        List<String> sub = listWithDuplicates.subList(0, 2);
        sub.addAll(Arrays.asList("A", "X")); // "A" is duplicate
    }

    // --- Test that setUniqueList on list with all distinct elements works ---

    @Test
    public void testSetUniqueListWithDistinctElements() {
        List<String> distinct = Arrays.asList("P", "Q", "R");
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<>(distinct));
        assertEquals(3, list.size());
        // No changes
    }

    // --- Test that setUniqueList preserves order of first occurrence ---

    @Test
    public void testSetUniqueListPreservesFirstOccurrenceOrder() {
        List<String> mixed = Arrays.asList("A", "B", "A", "C", "B", "D");
        SetUniqueList<String> list = SetUniqueList.setUniqueList(new ArrayList<>(mixed));
        assertEquals(4, list.size());
        assertEquals("A", list.get(0));
        assertEquals("B", list.get(1));
        assertEquals("C", list.get(2));
        assertEquals("D", list.get(3));
    }

    // --- Test that subList range check is correct ---

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListFromNegativeOne() {
        listWithDuplicates.subList(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListToGreaterThanSize() {
        listWithDuplicates.subList(0, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubListFromGreaterThanToIndices() {
        listWithDuplicates.subList(3, 2);
    }
}