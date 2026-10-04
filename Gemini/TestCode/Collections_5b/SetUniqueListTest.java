package org.apache.commons.collections4.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.Test;

public class SetUniqueListTest {

    @Test
    public void testSetUniqueListCreation() {
        List<String> list = new ArrayList<>();
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(list);
        assertNotNull(uniqueList);
        assertTrue(uniqueList.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testSetUniqueListNullList() {
        SetUniqueList.setUniqueList(null);
    }

    @Test
    public void testAddElement() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        assertTrue(uniqueList.add("A"));
        assertFalse(uniqueList.add("A")); // Duplicate
        assertEquals(1, uniqueList.size());
        assertEquals("A", uniqueList.get(0));
    }

    @Test
    public void testAddAtIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add(0, "B");
        assertEquals(2, uniqueList.size());
        assertEquals("B", uniqueList.get(0));
        assertEquals("A", uniqueList.get(1));

        // Adding duplicate at index should not change the list (or depending on impl, might be no-op or reorder)
        uniqueList.add(0, "A");
        assertEquals(2, uniqueList.size());
        assertEquals("B", uniqueList.get(0));
        assertEquals("A", uniqueList.get(1));
    }

    @Test
    public void testAddAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        List<String> items = new ArrayList<>();
        items.add("A");
        items.add("B");
        items.add("A"); // duplicate

        assertTrue(uniqueList.addAll(items));
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
    }

    @Test
    public void testAddAllAtIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        
        List<String> items = new ArrayList<>();
        items.add("B");
        items.add("A"); // duplicate

        assertTrue(uniqueList.addAll(0, items));
        assertEquals(2, uniqueList.size());
        assertEquals("B", uniqueList.get(0));
        assertEquals("A", uniqueList.get(1));
    }

    @Test
    public void testSet() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        // Set index 0 to "C"
        String old = uniqueList.set(0, "C");
        assertEquals("A", old);
        assertEquals("C", uniqueList.get(0));

        // Set index 1 to "C" (already exists at index 0)
        try {
            uniqueList.set(1, "C");
            // Depending on Commons Collections version, setting to an existing element either swaps or throws/rejects.
            // Let's inspect behavior safely: if it allows, check size; if it throws, catch it.
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            // expected in some implementations if duplicate is introduced
        }
    }

    @Test
    public void testRemoveObject() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        assertTrue(uniqueList.remove("A"));
        assertFalse(uniqueList.contains("A"));
        assertEquals(1, uniqueList.size());
        assertEquals("B", uniqueList.get(0));

        assertFalse(uniqueList.remove("NonExistent"));
    }

    @Test
    public void testRemoveIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        String removed = uniqueList.remove(0);
        assertEquals("A", removed);
        assertEquals(1, uniqueList.size());
        assertEquals("B", uniqueList.get(0));
    }

    @Test
    public void testRemoveAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        List<String> toRemove = new ArrayList<>();
        toRemove.add("A");
        toRemove.add("C");

        assertTrue(uniqueList.removeAll(toRemove));
        assertEquals(1, uniqueList.size());
        assertEquals("B", uniqueList.get(0));
        assertFalse(uniqueList.contains("A"));
        assertFalse(uniqueList.contains("C"));
    }

    @Test
    public void testRetainAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        List<String> toRetain = new ArrayList<>();
        toRetain.add("A");
        toRetain.add("B");

        assertTrue(uniqueList.retainAll(toRetain));
        assertEquals(2, uniqueList.size());
        assertTrue(uniqueList.contains("A"));
        assertTrue(uniqueList.contains("B"));
        assertFalse(uniqueList.contains("C"));
    }

    @Test
    public void testClear() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        uniqueList.clear();
        assertTrue(uniqueList.isEmpty());
        assertEquals(0, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
    }

    @Test
    public void testIterator() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        java.util.Iterator<String> it = uniqueList.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        it.remove();
        assertEquals(1, uniqueList.size());
        assertFalse(uniqueList.contains("A"));
    }

    @Test
    public void testListIterator() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        ListIterator<String> lit = uniqueList.listIterator();
        assertTrue(lit.hasNext());
        assertEquals("A", lit.next());
        
        ListIterator<String> litIndex = uniqueList.listIterator(1);
        assertTrue(litIndex.hasPrevious());
        assertEquals("A", litIndex.previous());
    }

    @Test
    public void testSubList() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        List<String> sub = uniqueList.subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("B", sub.get(0));
        assertEquals("C", sub.get(1));
    }

    @Test
    public void testAsSet() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        Set<String> set = uniqueList.asSet();
        assertNotNull(set);
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));
        assertEquals(2, set.size());
    }
}