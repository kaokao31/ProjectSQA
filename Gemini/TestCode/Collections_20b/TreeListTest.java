package org.apache.commons.collections.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class TreeListTest {

    @Test
    public void testEmptyList() {
        TreeList list = new TreeList();
        Assert.assertEquals(0, list.size());
        Assert.assertTrue(list.isEmpty());
        Assert.assertFalse(list.contains("A"));
        Assert.assertEquals(-1, list.indexOf("A"));
        Assert.assertEquals(-1, list.lastIndexOf("A"));
        
        Object[] array = list.toArray();
        Assert.assertEquals(0, array.length);

        String[] strArray = new String[0];
        String[] result = (String[]) list.toArray(strArray);
        Assert.assertEquals(0, result.length);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBoundsNegative() {
        TreeList list = new TreeList();
        list.get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBoundsPositive() {
        TreeList list = new TreeList();
        list.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBoundsNegative() {
        TreeList list = new TreeList();
        list.remove(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBoundsPositive() {
        TreeList list = new TreeList();
        list.remove(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddOutOfBoundsNegative() {
        TreeList list = new TreeList();
        list.add(-1, "A");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddOutOfBoundsPositive() {
        TreeList list = new TreeList();
        list.add(1, "A");
    }

    @Test
    public void testAddAndGet() {
        TreeList list = new TreeList();
        list.add("B");
        list.add(0, "A");
        list.add(2, "C");
        
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
        Assert.assertEquals("C", list.get(2));
    }

    @Test
    public void testLargeInsertionAndBalancing() {
        TreeList list = new TreeList();
        int count = 100;
        for (int i = 0; i < count; i++) {
            list.add("Item " + i);
        }
        Assert.assertEquals(count, list.size());
        for (int i = 0; i < count; i++) {
            Assert.assertEquals("Item " + i, list.get(i));
        }

        // Insert at beginning, middle, end
        list.add(0, "First");
        list.add(50, "Middle");
        list.add(list.size(), "Last");

        Assert.assertEquals("First", list.get(0));
        Assert.assertEquals("Last", list.get(list.size() - 1));
        
        // Remove items
        Assert.assertEquals("First", list.remove(0));
        Assert.assertEquals("Last", list.remove(list.size() - 1));
        
        list.clear();
        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void testSet() {
        TreeList list = new TreeList();
        list.add("A");
        list.add("B");
        
        Object old = list.set(1, "C");
        Assert.assertEquals("B", old);
        Assert.assertEquals("C", list.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSetOutOfBounds() {
        TreeList list = new TreeList();
        list.set(0, "A");
    }

    @Test
    public void testIndexOfAndLastIndexOfAndContains() {
        TreeList list = new TreeList();
        list.add("A");
        list.add("B");
        list.add("A");
        list.add("C");

        Assert.assertTrue(list.contains("B"));
        Assert.assertFalse(list.contains("Z"));
        Assert.assertFalse(list.contains(null));

        Assert.assertEquals(0, list.indexOf("A"));
        Assert.assertEquals(2, list.lastIndexOf("A"));
        Assert.assertEquals(1, list.indexOf("B"));
        Assert.assertEquals(-1, list.indexOf("Z"));
        Assert.assertEquals(-1, list.indexOf(null));
    }

    @Test
    public void testToArray() {
        TreeList list = new TreeList();
        list.add("A");
        list.add("B");

        Object[] array = list.toArray();
        Assert.assertEquals(2, array.length);
        Assert.assertEquals("A", array[0]);
        Assert.assertEquals("B", array[1]);

        String[] target = new String[2];
        String[] result = (String[]) list.toArray(target);
        Assert.assertSame(target, result);
        Assert.assertEquals("A", result[0]);
        Assert.assertEquals("B", result[1]);

        String[] smallTarget = new String[1];
        String[] smallResult = (String[]) list.toArray(smallTarget);
        Assert.assertEquals(2, smallResult.length);
        Assert.assertEquals("A", smallResult[0]);
        Assert.assertEquals("B", smallResult[1]);

        String[] largeTarget = new String[5];
        largeTarget[4] = "Notnull";
        String[] largeResult = (String[]) list.toArray(largeTarget);
        Assert.assertSame(largeTarget, largeResult);
        Assert.assertEquals("A", largeResult[0]);
        Assert.assertEquals("B", largeResult[1]);
        Assert.assertNull(largeResult[2]);
        Assert.assertNull(largeResult[4]);
    }

    @Test
    public void testAddAllCollection() {
        List<String> c = new ArrayList<>();
        c.add("X");
        c.add("Y");

        TreeList list = new TreeList();
        Assert.assertTrue(list.addAll(c));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("X", list.get(0));
        Assert.assertEquals("Y", list.get(1));

        Assert.assertFalse(list.addAll(new ArrayList<>()));

        List<String> c2 = new ArrayList<>();
        c2.add("Z");
        Assert.assertTrue(list.addAll(1, c2));
        Assert.assertEquals("X", list.get(0));
        Assert.assertEquals("Z", list.get(1));
        Assert.assertEquals("Y", list.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testAddAllIndexOutOfBounds() {
        TreeList list = new TreeList();
        list.addAll(1, new ArrayList<>());
    }

    @Test
    public void testIterator() {
        TreeList list = new TreeList();
        list.add("A");
        list.add("B");
        list.add("C");

        Iterator<Object> it = list.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("B", it.next());
        it.remove();
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("C", list.get(1));

        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("C", it.next());
        Assert.assertFalse(it.hasNext());

        try {
            it.next();
            Assert.fail("Expected NoSuchElementException");
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test(expected = ConcurrentModificationException.class)
    public void testIteratorConcurrentModification() {
        TreeList list = new TreeList();
        list.add("A");
        list.add("B");

        Iterator<Object> it = list.iterator();
        list.add("C");
        it.next();
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorRemoveBeforeNext() {
        TreeList list = new TreeList();
        list.add("A");
        Iterator<Object> it = list.iterator();
        it.remove();
    }

    @Test(expected = IllegalStateException.class)
    public void testIteratorDoubleRemove() {
        TreeList list = new TreeList();
        list.add("A");
        Iterator<Object> it = list.iterator();
        it.next();
        it.remove();
        it.remove();
    }

    @Test
    public void testListIterator() {
        TreeList list = new TreeList();
        list.add("A");
        list.add("B");

        ListIterator<Object> lit = list.listIterator(1);
        Assert.assertTrue(lit.hasPrevious());
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("A", lit.previous());
        Assert.assertEquals(0, lit.nextIndex());
        Assert.assertEquals(-1, lit.previousIndex());

        Assert.assertEquals("A", lit.next());
        Assert.assertEquals("B", lit.next());
        Assert.assertFalse(lit.hasNext());

        lit.set("BB");
        Assert.assertEquals("BB", list.get(1));

        lit.add("C");
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("C", list.get(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorOutOfBounds() {
        TreeList list = new TreeList();
        list.listIterator(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorNegativeBounds() {
        TreeList list = new TreeList();
        list.listIterator(-1);
    }

    @Test
    public void testConstructorWithCollection() {
        List<String> c = new ArrayList<>();
        c.add("A");
        c.add("B");

        TreeList list = new TreeList(c);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("A", list.get(0));
        Assert.assertEquals("B", list.get(1));
    }

    @Test
    public void testRemove() {
        TreeList list = new TreeList();
        list.add("A");
        list.add("B");
        list.add("A");

        Assert.assertFalse(list.remove("Z"));
        Assert.assertTrue(list.remove("A"));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("B", list.get(0));
        Assert.assertEquals("A", list.get(1));
    }
}