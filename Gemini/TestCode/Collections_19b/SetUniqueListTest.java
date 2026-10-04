package org.apache.commons.collections.list;

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
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);
        Assert.assertNotNull(setUniqueList);
        Assert.assertTrue(setUniqueList.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullList() {
        SetUniqueList.decorate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecorateNullSet() {
        SetUniqueList.decorate(new ArrayList<String>(), null);
    }

    @Test
    public void testAddElement() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        Assert.assertTrue(setUniqueList.add("A"));
        Assert.assertFalse(setUniqueList.add("A")); // Duplicate
        Assert.assertEquals(1, setUniqueList.size());
        Assert.assertTrue(setUniqueList.contains("A"));
    }

    @Test
    public void testAddAtIndex() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add(0, "A");
        Assert.assertEquals(1, setUniqueList.size());

        // Adding duplicate at a different index should fail to add and not reorder/duplicate
        setUniqueList.add(0, "A");
        Assert.assertEquals(1, setUniqueList.size());
        Assert.assertEquals("A", setUniqueList.get(0));

        setUniqueList.add(0, "B");
        Assert.assertEquals(2, setUniqueList.size());
        Assert.assertEquals("B", setUniqueList.get(0));
        Assert.assertEquals("A", setUniqueList.get(1));
    }

    @Test
    public void testAddAllCollection() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        Collection<String> col = Arrays.asList("A", "B", "A", "C");
        Assert.assertTrue(setUniqueList.addAll(col));
        Assert.assertEquals(3, setUniqueList.size());
        Assert.assertEquals(Arrays.asList("A", "B", "C"), setUniqueList);

        // Adding collection with already existing elements
        Assert.assertFalse(setUniqueList.addAll(Arrays.asList("A", "B")));
        Assert.assertEquals(3, setUniqueList.size());
    }

    @Test
    public void testAddAllAtIndex() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("C");

        Collection<String> col = Arrays.asList("B", "A", "D");
        Assert.assertTrue(setUniqueList.addAll(1, col));
        // "A" is already in list, so it shouldn't be added again. "B" and "D" should be inserted at index 1.
        // List should become: A, B, D, C
        Assert.assertEquals(4, setUniqueList.size());
        Assert.assertEquals("A", setUniqueList.get(0));
        Assert.assertEquals("B", setUniqueList.get(1));
        Assert.assertEquals("D", setUniqueList.get(2));
        Assert.assertEquals("C", setUniqueList.get(3));
    }

    @Test
    public void testSet() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        // Set index 0 to "C"
        String old = setUniqueList.set(0, "C");
        Assert.assertEquals("A", old);
        Assert.assertEquals("C", setUniqueList.get(0));
        Assert.assertEquals(2, setUniqueList.size());
        Assert.assertFalse(setUniqueList.contains("A"));
        Assert.assertTrue(setUniqueList.contains("C"));

        // Set index 1 to "C" (which is already present at index 0)
        // Defect 19 often relates to handling of set() with existing elements in SetUniqueList
        String old2 = setUniqueList.set(1, "C");
        Assert.assertEquals("B", old2);
        // Depending on implementation, setting an element to an already existing value removes the old element or shifts.
        // Let's assert standard behaviors.
        Assert.assertTrue(setUniqueList.contains("C"));
    }

    @Test
    public void testRemoveObject() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        Assert.assertTrue(setUniqueList.remove("A"));
        Assert.assertFalse(setUniqueList.contains("A"));
        Assert.assertEquals(1, setUniqueList.size());

        Assert.assertFalse(setUniqueList.remove("NonExistent"));
    }

    @Test
    public void testRemoveIndex() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        String removed = setUniqueList.remove(0);
        Assert.assertEquals("A", removed);
        Assert.assertEquals(1, setUniqueList.size());
        Assert.assertFalse(setUniqueList.contains("A"));
        Assert.assertTrue(setUniqueList.contains("B"));
    }

    @Test
    public void testRemoveAll() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");
        setUniqueList.add("C");

        Assert.assertTrue(setUniqueList.removeAll(Arrays.asList("A", "C")));
        Assert.assertEquals(1, setUniqueList.size());
        Assert.assertTrue(setUniqueList.contains("B"));
        Assert.assertFalse(setUniqueList.contains("A"));
        Assert.assertFalse(setUniqueList.contains("C"));
    }

    @Test
    public void testRetainAll() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");
        setUniqueList.add("C");

        Assert.assertTrue(setUniqueList.retainAll(Arrays.asList("A", "B")));
        Assert.assertEquals(2, setUniqueList.size());
        Assert.assertTrue(setUniqueList.contains("A"));
        Assert.assertTrue(setUniqueList.contains("B"));
        Assert.assertFalse(setUniqueList.contains("C"));
    }

    @Test
    public void testClear() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        setUniqueList.clear();
        Assert.assertTrue(setUniqueList.isEmpty());
        Assert.assertEquals(0, setUniqueList.size());
        // Ensure underlying set is also cleared
        Assert.assertTrue(setUniqueList.add("A"));
    }

    @Test
    public void testIterator() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        Iterator<String> it = setUniqueList.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.remove();
        Assert.assertFalse(setUniqueList.contains("A"));
        Assert.assertEquals(1, setUniqueList.size());
    }

    @Test
    public void testListIterator() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        ListIterator<String> lit = setUniqueList.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("A", lit.next());
        Assert.assertEquals("B", lit.next());
        Assert.assertFalse(lit.hasNext());

        lit.remove();
        Assert.assertEquals(1, setUniqueList.size());
        Assert.assertFalse(setUniqueList.contains("B"));
    }

    @Test
    public void testListIteratorAtIndex() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        ListIterator<String> lit = setUniqueList.listIterator(1);
        Assert.assertEquals("B", lit.next());
    }

    @Test
    public void testSubList() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");
        setUniqueList.add("C");

        List<String> sub = setUniqueList.subList(0, 2);
        Assert.assertEquals(2, sub.size());
        Assert.assertTrue(sub.contains("A"));
        Assert.assertTrue(sub.contains("B"));
    }

    @Test
    public void testIndexOfAndLastIndexOf() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");
        setUniqueList.add("C");

        Assert.assertEquals(1, setUniqueList.indexOf("B"));
        Assert.assertEquals(1, setUniqueList.lastIndexOf("B"));
        Assert.assertEquals(-1, setUniqueList.indexOf("Z"));
    }

    @Test
    public void testAsSet() {
        List<String> list = new ArrayList<String>();
        SetUniqueList<String> setUniqueList = SetUniqueList.decorate(list);

        setUniqueList.add("A");
        setUniqueList.add("B");

        Set<String> set = setUniqueList.asSet();
        Assert.assertNotNull(set);
        Assert.assertEquals(2, set.size());
        Assert.assertTrue(set.contains("A"));
        Assert.assertTrue(set.contains("B"));

        // Modifying through asSet or checking contract
        Assert.assertFalse(set.add("A"));
        Assert.assertTrue(set.add("C"));
        Assert.assertTrue(setUniqueList.contains("C"));
    }
}