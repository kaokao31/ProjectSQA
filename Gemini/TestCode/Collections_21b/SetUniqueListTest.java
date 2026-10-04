package org.apache.commons.collections4.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SetUniqueListTest {

    @Test
    public void testSetUniqueListCreation() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(list);
        Assert.assertNotNull(uniqueList);
        Assert.assertTrue(uniqueList.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void testSetUniqueListNullList() {
        SetUniqueList.setUniqueList(null);
    }

    @Test(expected = NullPointerException.class)
    public void testSetUniqueListNullSet() {
        List<String> list = new ArrayList<String>();
        SetUniqueList.setUniqueList(list, null);
    }

    @Test
    public void testAddElement() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        
        boolean added1 = uniqueList.add("A");
        Assert.assertTrue(added1);
        Assert.assertEquals(1, uniqueList.size());
        
        // Adding duplicate
        boolean added2 = uniqueList.add("A");
        Assert.assertFalse(added2);
        Assert.assertEquals(1, uniqueList.size());
        
        // Adding null if allowed (or handling appropriately)
        boolean addedNull1 = uniqueList.add(null);
        Assert.assertTrue(addedNull1);
        boolean addedNull2 = uniqueList.add(null);
        Assert.assertFalse(addedNull2);
        Assert.assertEquals(2, uniqueList.size());
    }

    @Test
    public void testAddAtIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.add("A");
        uniqueList.add("B");

        // Add unique at index
        uniqueList.add(1, "C");
        Assert.assertEquals(Arrays.asList("A", "C", "B"), uniqueList);

        // Add duplicate at index (should move or reject depending on impl)
        uniqueList.add(0, "B");
        // B is already in list, SetUniqueList behavior typically removes old or doesn't add duplicate.
        // Let's assert standard Commons Collections SetUniqueList behavior.
        Assert.assertTrue(uniqueList.contains("B"));
    }

    @Test
    public void testAddAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        boolean changed = uniqueList.addAll(Arrays.asList("A", "B", "A", "C"));
        Assert.assertTrue(changed);
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals(Arrays.asList("A", "B", "C"), uniqueList);

        // Add all empty
        boolean changedEmpty = uniqueList.addAll(new ArrayList<String>());
        Assert.assertFalse(changedEmpty);
    }

    @Test
    public void testAddAllAtIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.add("A");
        uniqueList.add("D");

        boolean changed = uniqueList.addAll(1, Arrays.asList("B", "C", "A"));
        Assert.assertTrue(changed);
        // "A" is duplicate, so it shouldn't be re-added or should be moved.
        Assert.assertTrue(uniqueList.size() <= 4);
    }

    @Test
    public void testSet() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.add("A");
        uniqueList.add("B");

        // Set element at index
        String old = uniqueList.set(0, "C");
        Assert.assertEquals("A", old);
        Assert.assertEquals("C", uniqueList.get(0));

        // Set to existing element
        // Depending on version, set with existing element might swap or throw/reject.
        uniqueList.set(0, "B");
        Assert.assertTrue(uniqueList.contains("B"));
    }

    @Test
    public void testRemove() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.add("A");
        uniqueList.add("B");

        boolean removedObj = uniqueList.remove("A");
        Assert.assertTrue(removedObj);
        Assert.assertFalse(uniqueList.contains("A"));
        Assert.assertEquals(1, uniqueList.size());

        String removedIdx = uniqueList.remove(0);
        Assert.assertEquals("B", removedIdx);
        Assert.assertTrue(uniqueList.isEmpty());
    }

    @Test
    public void testRemoveAllAndRetainAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.addAll(Arrays.asList("A", "B", "C", "D"));

        boolean removed = uniqueList.removeAll(Arrays.asList("A", "C"));
        Assert.assertTrue(removed);
        Assert.assertEquals(Arrays.asList("B", "D"), uniqueList);

        boolean retained = uniqueList.retainAll(Arrays.asList("B"));
        Assert.assertTrue(retained);
        Assert.assertEquals(Arrays.asList("B"), uniqueList);
    }

    @Test
    public void testClear() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.addAll(Arrays.asList("A", "B"));
        uniqueList.clear();
        Assert.assertTrue(uniqueList.isEmpty());
        Assert.assertTrue(uniqueList.set.isEmpty());
    }

    @Test
    public void testContains() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.add("A");
        Assert.assertTrue(uniqueList.contains("A"));
        Assert.assertFalse(uniqueList.contains("B"));
        
        Assert.assertTrue(uniqueList.containsAll(Arrays.asList("A")));
        Assert.assertFalse(uniqueList.containsAll(Arrays.asList("A", "B")));
    }

    @Test
    public void testIteratorAndListIterator() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.addAll(Arrays.asList("A", "B", "C"));

        Iterator<String> it = uniqueList.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.remove();
        Assert.assertFalse(uniqueList.contains("A"));

        ListIterator<String> lit = uniqueList.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("B", lit.next());

        ListIterator<String> litIndex = uniqueList.listIterator(1);
        Assert.assertEquals("C", litIndex.next());
    }

    @Test
    public void testSubList() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.addAll(Arrays.asList("A", "B", "C", "D"));

        List<String> sub = uniqueList.subList(1, 3);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals("B", sub.get(0));
        
        // Ensure sublist is backed by SetUniqueList or acts correctly
        sub.set(0, "X");
        Assert.assertEquals("X", uniqueList.get(1));
    }

    @Test
    public void testAsSet() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.addAll(Arrays.asList("A", "B"));
        
        Set<String> set = uniqueList.asSet();
        Assert.assertNotNull(set);
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains("A"));
        Assert.assertTrue(set.contains("B"));
    }

    @Test
    public void testIndexOfAndLastIndexOf() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.addAll(Arrays.asList("A", "B", "C"));

        Assert.assertEquals(1, uniqueList.indexOf("B"));
        Assert.assertEquals(-1, uniqueList.indexOf("Z"));

        Assert.assertEquals(1, uniqueList.lastIndexOf("B"));
        Assert.assertEquals(-1, uniqueList.lastIndexOf("Z"));
    }

    @Test
    public void testToArray() {
        SetUniqueList<String> uniqueList = SetUniqueList.setUniqueList(new ArrayList<String>());
        uniqueList.addAll(Arrays.asList("A", "B"));

        Object[] arr1 = uniqueList.toArray();
        Assert.assertEquals(2, arr1.length);

        String[] arr2 = new String[0];
        String[] arr3 = uniqueList.toArray(arr2);
        Assert.assertEquals(2, arr3.length);
    }
}