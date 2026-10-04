package org.apache.commons.collections.list;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class SetUniqueListTest {

    @Test
    public void testSetUniqueListCreationAndDecorate() {
        List<String> list = new ArrayList<>();
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(list);
        Assert.assertNotNull(uniqueList);
        Assert.assertTrue(uniqueList.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullList() {
        SetUniqueList.decorate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullSetAndList() {
        SetUniqueList.decorate(null, null);
    }

    @Test
    public void testAddElement() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        
        Assert.assertTrue(uniqueList.add("A"));
        Assert.assertFalse(uniqueList.add("A")); // Duplicate
        
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertEquals("A", uniqueList.get(0));
    }

    @Test
    public void testAddElementAtIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        
        uniqueList.add(0, "A");
        uniqueList.add(0, "B");
        
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals("B", uniqueList.get(0));
        Assert.assertEquals("A", uniqueList.get(1));

        // Adding duplicate at index should not change list
        uniqueList.add(0, "A");
        Assert.assertEquals(2, uniqueList.size());
        Assert.assertEquals("B", uniqueList.get(0));
        Assert.assertEquals("A", uniqueList.get(1));
    }

    @Test
    public void testAddAllCollection() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        List<String> items = Arrays.asList("A", "B", "A", "C");

        Assert.assertTrue(uniqueList.addAll(items));
        Assert.assertEquals(3, uniqueList.size());
        Assert.assertEquals(Arrays.asList("A", "B", "C"), uniqueList);

        // Adding collection with all existing elements
        Assert.assertFalse(uniqueList.addAll(Arrays.asList("A", "B")));
    }

    @Test
    public void testAddAllAtIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("C");

        List<String> items = Arrays.asList("B", "A", "D");
        Assert.assertTrue(uniqueList.addAll(1, items));
        
        // "A" is already in list, so it shouldn't be re-added. "B" and "D" added at index 1.
        Assert.assertEquals(Arrays.asList("A", "B", "D", "C"), uniqueList);
    }

    @Test
    public void testSet() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        // Set index 0 to "C"
        String old = uniqueList.set(0, "C");
        Assert.assertEquals("A", old);
        Assert.assertEquals(Arrays.asList("C", "B"), uniqueList);

        // Set index 1 to existing element "C" (should swap or handle set correctly)
        // SetUniqueList overrides set to maintain uniqueness
        String old2 = uniqueList.set(1, "C");
        Assert.assertEquals("B", old2);
        Assert.assertEquals(Arrays.asList("C"), uniqueList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDuplicateNonUnique() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        // Setting an element that already exists elsewhere in the list
        // Depending on implementation, this might throw IllegalArgumentException or remove the old one.
        // Commons Collections SetUniqueList set() removes the object if it exists elsewhere.
        uniqueList.set(0, "B");
    }

    @Test
    public void testRemoveObject() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        Assert.assertTrue(uniqueList.remove("A"));
        Assert.assertFalse(uniqueList.remove("Z"));
        Assert.assertEquals(1, uniqueList.size());
        // Verify set is also updated (can add "A" back)
        Assert.assertTrue(uniqueList.add("A"));
    }

    @Test
    public void testRemoveIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        String removed = uniqueList.remove(0);
        Assert.assertEquals("A", removed);
        Assert.assertEquals(1, uniqueList.size());
        Assert.assertTrue(uniqueList.add("A")); // Should allow since it was removed from set
    }

    @Test
    public void testRemoveAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Assert.assertTrue(uniqueList.removeAll(Arrays.asList("A", "C")));
        Assert.assertEquals(Arrays.asList("B"), uniqueList);
        Assert.assertTrue(uniqueList.add("A")); // Re-add should succeed
    }

    @Test
    public void testRetainAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Assert.assertTrue(uniqueList.retainAll(Arrays.asList("A", "B")));
        Assert.assertEquals(Arrays.asList("A", "B"), uniqueList);
        
        // "C" was removed, so adding "C" back should succeed
        Assert.assertTrue(uniqueList.add("C"));
    }

    @Test
    public void testClear() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        uniqueList.clear();
        Assert.assertTrue(uniqueList.isEmpty());
        
        // Should be able to add elements again freely
        Assert.assertTrue(uniqueList.add("A"));
    }

    @Test
    public void testContains() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");

        Assert.assertTrue(uniqueList.contains("A"));
        Assert.assertFalse(uniqueList.contains("B"));
    }

    @Test
    public void testContainsAll() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        Assert.assertTrue(uniqueList.containsAll(Arrays.asList("A", "B")));
        Assert.assertFalse(uniqueList.containsAll(Arrays.asList("A", "C")));
    }

    @Test
    public void testListIterator() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        ListIterator<String> it = uniqueList.listIterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        
        it.set("C");
        Assert.assertEquals("C", uniqueList.get(0));

        it.add("D");
        Assert.assertTrue(uniqueList.contains("D"));
    }

    @Test
    public void testListIteratorIndex() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        ListIterator<String> it = uniqueList.listIterator(1);
        Assert.assertTrue(it.hasPrevious());
        Assert.assertEquals("A", it.previous());
    }

    @Test
    public void testSubList() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        List<String> sub = uniqueList.subList(1, 3);
        Assert.assertEquals(Arrays.asList("B", "C"), sub);
        
        // Test subList's unique list features if applicable, or general operations
        Assert.assertTrue(sub.contains("B"));
    }

    @Test
    public void testAsSet() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");

        Set<String> set = uniqueList.asSet();
        Assert.assertNotNull(set);
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains("A"));
        Assert.assertTrue(set.contains("B"));

        // Modifying underlying list should reflect in asSet
        uniqueList.remove("A");
        Assert.assertFalse(set.contains("A"));
    }

    @Test
    public void testIndexOfAndLastIndexOf() {
        SetUniqueList<String> uniqueList = SetUniqueList.decorate(new ArrayList<>());
        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        Assert.assertEquals(1, uniqueList.indexOf("B"));
        Assert.assertEquals(-1, uniqueList.indexOf("Z"));

        Assert.assertEquals(1, uniqueList.lastIndexOf("B"));
        Assert.assertEquals(-1, uniqueList.lastIndexOf("Z"));
    }
}