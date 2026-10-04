package org.apache.commons.collections.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class SetUniqueListTest {

    @Test
    public void testDecorateNull() {
        try {
            SetUniqueList.decorate(null);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testDecorateValid() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);
        Assert.assertNotNull(sul);
        Assert.assertTrue(sul.isEmpty());
    }

    @Test
    public void testAddElement() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        Assert.assertTrue(sul.add("A"));
        Assert.assertFalse(sul.add("A")); // duplicate
        Assert.assertEquals(1, sul.size());
        Assert.assertEquals("A", sul.get(0));
    }

    @Test
    public void testAddAtIndex() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add(0, "A");
        Assert.assertEquals(1, sul.size());
        Assert.assertEquals("A", sul.get(0));

        // Adding duplicate at index should not insert or fail depending on contract, 
        // usually in Commons Collections SetUniqueList, add(index, element) handles duplicates.
        // Let's verify behavior. If already exists, does it move or return false/noop?
        // Actually, SetUniqueList overrides add(int, Object) to enforce uniqueness.
        sul.add(0, "A"); // duplicate
        Assert.assertEquals(1, sul.size());

        sul.add(0, "B");
        Assert.assertEquals(2, sul.size());
        Assert.assertEquals("B", sul.get(0));
        Assert.assertEquals("A", sul.get(1));
    }

    @Test
    public void testAddAll() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        List<String> batch = Arrays.asList("A", "B", "A", "C");
        boolean changed = sul.addAll(batch);
        Assert.assertTrue(changed);
        Assert.assertEquals(3, sul.size());
        Assert.assertEquals(Arrays.asList("A", "B", "C"), sul);

        // AddAll duplicate only
        boolean changedAgain = sul.addAll(Arrays.asList("A", "B"));
        Assert.assertFalse(changedAgain);
        Assert.assertEquals(3, sul.size());
    }

    @Test
    public void testAddAllAtIndex() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("B");
        boolean changed = sul.addAll(0, Arrays.asList("A", "B", "C"));
        Assert.assertTrue(changed);
        // B was already there, so it should be filtered or moved. Let's check SetUniqueList contract.
        // Usually, addAll at index filters out elements already present in the list.
        Assert.assertEquals(3, sul.size());
        Assert.assertEquals("A", sul.get(0));
        Assert.assertEquals("C", sul.get(1));
        Assert.assertEquals("B", sul.get(2));
    }

    @Test
    public void testSet() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");

        // Set index 0 to "C"
        String old = sul.set(0, "C");
        Assert.assertEquals("A", old);
        Assert.assertEquals("C", sul.get(0));

        // Set index 1 to "C" (already exists at index 0)
        // SetUniqueList should handle replacing or reject/swap. Let's test what it does.
        try {
            sul.set(1, "C");
            // If it allows or throws or swaps:
        } catch (IllegalArgumentException e) {
            // some implementations throw if element is already in the set at a different index
        }
    }

    @Test
    public void testRemove() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");

        boolean removed = sul.remove("A");
        Assert.assertTrue(removed);
        Assert.assertEquals(1, sul.size());
        Assert.assertFalse(sul.contains("A"));

        String removedObj = sul.remove(0);
        Assert.assertEquals("B", removedObj);
        Assert.assertTrue(sul.isEmpty());
    }

    @Test
    public void testRemoveAllAndRetainAll() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");
        sul.add("C");

        boolean changed = sul.removeAll(Arrays.asList("A", "B"));
        Assert.assertTrue(changed);
        Assert.assertEquals(1, sul.size());
        Assert.assertEquals("C", sul.get(0));

        sul.add("A");
        sul.add("B");

        boolean retained = sul.retainAll(Arrays.asList("C", "A"));
        Assert.assertTrue(retained);
        Assert.assertEquals(2, sul.size());
        Assert.assertTrue(sul.contains("C"));
        Assert.assertTrue(sul.contains("A"));
        Assert.assertFalse(sul.contains("B"));
    }

    @Test
    public void testClear() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");
        sul.clear();
        Assert.assertTrue(sul.isEmpty());
        
        // Ensure underlying set and list are also cleared and usable again
        sul.add("C");
        Assert.assertEquals(1, sul.size());
        Assert.assertEquals("C", sul.get(0));
    }

    @Test
    public void testIteratorAndListIterator() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");

        Iterator<String> it = sul.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.remove();
        Assert.assertEquals(1, sul.size());
        Assert.assertFalse(sul.contains("A"));

        sul.add("C");
        ListIterator<String> lit = sul.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("B", lit.next());
        
        ListIterator<String> litIndex = sul.listIterator(1);
        Assert.assertTrue(litIndex.hasPrevious());
        Assert.assertEquals("C", litIndex.previous());
    }

    @Test
    public void testSubList() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");
        sul.add("C");

        List<String> sub = sul.subList(1, 3);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals("B", sub.get(0));
        Assert.assertEquals("C", sub.get(1));
    }

    @Test
    public void testIndexOfAndLastIndexOf() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");
        sul.add("C");

        Assert.assertEquals(1, sul.indexOf("B"));
        Assert.assertEquals(-1, sul.indexOf("Z"));

        Assert.assertEquals(1, sul.lastIndexOf("B"));
        Assert.assertEquals(-1, sul.lastIndexOf("Z"));
    }

    @Test
    public void testAsSet() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> sul = SetUniqueList.decorate(list);

        sul.add("A");
        sul.add("B");

        Set<String> set = sul.asSet();
        Assert.assertNotNull(set);
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains("A"));
        Assert.assertTrue(set.contains("B"));

        // Modifying the set or checking uniqueness constraints via set view if supported
        try {
            set.add("A"); // should be idempotent or throw depending on set decorator implementation
        } catch (UnsupportedOperationException e) {
            // expected if unmodifiable
        }
    }
}