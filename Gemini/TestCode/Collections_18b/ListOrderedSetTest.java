package org.apache.commons.collections.set;

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

public class ListOrderedSetTest {

    @Test
    public void testFactoryMethods() {
        Set<String> set = new HashSet<String>();
        set.add("A");
        set.add("B");

        ListOrderedSet los1 = ListOrderedSet.decorate(set);
        Assert.assertNotNull(los1);
        Assert.assertEquals(2, los1.size());

        List<String> list = new ArrayList<String>();
        list.add("X");
        list.add("Y");

        ListOrderedSet los2 = ListOrderedSet.decorate(set, list);
        Assert.assertNotNull(los2);
        Assert.assertTrue(los2.contains("X"));
        Assert.assertTrue(los2.contains("Y"));
        Assert.assertTrue(los2.contains("A"));
    }

    @Test
    public void testConstructorAndBasicOperations() {
        ListOrderedSet los = new ListOrderedSet();
        Assert.assertTrue(los.isEmpty());
        Assert.assertEquals(0, los.size());

        // Test add
        Assert.assertTrue(los.add("One"));
        Assert.assertFalse(los.add("One")); // duplicate
        Assert.assertEquals(1, los.size());
        Assert.assertTrue(los.contains("One"));

        // Test add at index
        los.add(0, "Zero");
        Assert.assertEquals("Zero", los.get(0));
        Assert.assertEquals("One", los.get(1));

        // Test addAll
        List<String> list = Arrays.asList("Two", "Three", "One"); // "One" is duplicate
        Assert.assertTrue(los.addAll(list));
        Assert.assertEquals(4, los.size()); // Zero, One, Two, Three
        Assert.assertEquals("Two", los.get(2));
        Assert.assertEquals("Three", los.get(3));

        // Test addAll at index
        List<String> insertList = Arrays.asList("Half", "Zero"); // "Zero" is duplicate
        Assert.assertTrue(los.addAll(1, insertList));
        Assert.assertEquals("Half", los.get(1));
        Assert.assertEquals("Zero", los.get(2));

        // Test remove by object
        Assert.assertTrue(los.remove("Half"));
        Assert.assertFalse(los.remove("NonExistent"));

        // Test remove by index
        Object removed = los.remove(0);
        Assert.assertEquals("Zero", removed);

        // Test indexOf
        Assert.assertEquals(0, los.indexOf("One"));
        Assert.assertEquals(-1, los.indexOf("NonExistent"));

        // Test clear
        los.clear();
        Assert.assertTrue(los.isEmpty());
    }

    @Test
    public void testCollectionModificationMethods() {
        ListOrderedSet los = new ListOrderedSet();
        los.add("A");
        los.add("B");
        los.add("C");

        // removeAll
        List<String> toRemove = Arrays.asList("A", "C", "X");
        Assert.assertTrue(los.removeAll(toRemove));
        Assert.assertEquals(1, los.size());
        Assert.assertEquals("B", los.get(0));

        los.add("A");
        los.add("C");

        // retainAll
        List<String> toRetain = Arrays.asList("B", "C");
        Assert.assertTrue(los.retainAll(toRetain));
        Assert.assertEquals(2, los.size());
        Assert.assertTrue(los.contains("B"));
        Assert.assertTrue(los.contains("C"));
        Assert.assertFalse(los.contains("A"));
    }

    @Test
    public void testIteratorsAndViews() {
        ListOrderedSet los = new ListOrderedSet();
        los.add("One");
        los.add("Two");

        Iterator<String> it = los.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("One", it.next());
        it.remove();
        Assert.assertEquals(1, los.size());
        Assert.assertFalse(los.contains("One"));

        los.add("Three");
        ListIterator<String> lit = los.listIterator();
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("Two", lit.next());
        lit.set("NewTwo");
        Assert.assertEquals("NewTwo", los.get(0));

        List<String> asList = los.asList();
        Assert.assertNotNull(asList);
        Assert.assertEquals(los.size(), asList.size());
    }

    @Test
    public void testToArray() {
        ListOrderedSet los = new ListOrderedSet();
        los.add("A");
        los.add("B");

        Object[] arr1 = los.toArray();
        Assert.assertEquals(2, arr1.length);
        Assert.assertEquals("A", arr1[0]);
        Assert.assertEquals("B", arr1[1]);

        String[] arr2 = new String[2];
        String[] arr3 = (String[]) los.toArray(arr2);
        Assert.assertSame(arr2, arr3);
        Assert.assertEquals("A", arr3[0]);
        Assert.assertEquals("B", arr3[1]);

        String[] arr4 = new String[0];
        String[] arr5 = (String[]) los.toArray(arr4);
        Assert.assertEquals(2, arr5.length);
        Assert.assertEquals("A", arr5[0]);
        Assert.assertEquals("B", arr5[1]);
    }

    @Test
    public void testEqualsAndHashCode() {
        ListOrderedSet los1 = new ListOrderedSet();
        los1.add("A");
        los1.add("B");

        ListOrderedSet los2 = new ListOrderedSet();
        los2.add("A");
        los2.add("B");

        HashSet<String> set = new HashSet<String>();
        set.add("A");
        set.add("B");

        Assert.assertTrue(los1.equals(los2));
        Assert.assertTrue(los1.equals(los1));
        Assert.assertFalse(los1.equals(null));
        Assert.assertFalse(los1.equals("NotASet"));
        
        // ListOrderedSet equals contract regarding Lists vs Sets
        // Typically ListOrderedSet equals checks underlying set or list depending on implementation.
        // Let's verify hashCode consistency.
        Assert.assertEquals(los1.hashCode(), los2.hashCode());
    }

    @Test
    public void testSubList() {
        ListOrderedSet los = new ListOrderedSet();
        los.add("A");
        los.add("B");
        los.add("C");

        List<Object> sub = los.subList(1, 3);
        Assert.assertEquals(2, sub.size());
        Assert.assertEquals("B", sub.get(0));
        Assert.assertEquals("C", sub.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {
        ListOrderedSet los = new ListOrderedSet();
        los.get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveOutOfBounds() {
        ListOrderedSet los = new ListOrderedSet();
        los.remove(0);
    }
}