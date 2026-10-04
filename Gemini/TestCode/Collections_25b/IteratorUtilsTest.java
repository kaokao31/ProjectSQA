/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations.
 */
package org.apache.commons.collections4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.collections4.iterators.ArrayIterator;
import org.apache.commons.collections4.iterators.EnumerationIterator;
import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;
import org.apache.commons.collections4.iterators.NodeListIterator;
import org.apache.commons.collections4.iterators.ZippedIterator;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Comprehensive test suite for {@link IteratorUtils}.
 * Designed for maximum coverage and fault detection in Commons Collections 25.
 */
public class IteratorUtilsTest {

    @Test
    public void testConstants() {
        Assert.assertNotNull(IteratorUtils.EMPTY_ITERATOR);
        Assert.assertNotNull(IteratorUtils.EMPTY_LIST_ITERATOR);
        Assert.assertNotNull(IteratorUtils.EMPTY_MAP_ITERATOR);
        Assert.assertNotNull(IteratorUtils.EMPTY_SORTED_MAP_ITERATOR);
        Assert.assertNotNull(IteratorUtils.EMPTY_MODIFIABLE_ITERATOR);

        Assert.assertFalse(IteratorUtils.EMPTY_ITERATOR.hasNext());
        Assert.assertFalse(IteratorUtils.EMPTY_LIST_ITERATOR.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyIteratorNext() {
        IteratorUtils.EMPTY_ITERATOR.next();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEmptyIteratorRemove() {
        IteratorUtils.EMPTY_ITERATOR.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void testEmptyListIteratorPrevious() {
        IteratorUtils.EMPTY_LIST_ITERATOR.previous();
    }

    @Test
    public void testAsIteratorWithIterator() {
        List<String> list = Arrays.asList("a", "b", "c");
        Iterator<String> it = list.iterator();
        Iterator<String> result = IteratorUtils.asIterator(it);
        Assert.assertSame(it, result);
    }

    @Test
    public void testAsIteratorWithEnumeration() {
        Vector<String> vector = new Vector<String>();
        vector.add("a");
        vector.add("b");
        Enumeration<String> en = vector.elements();
        Iterator<String> it = IteratorUtils.asIterator(en);
        Assert.assertNotNull(it);
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("a", it.next());
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorNull() {
        IteratorUtils.asIterator((Enumeration<Object>) null);
    }

    @Test
    public void testAsIteratorWithEnumerationAndHandler() {
        Vector<String> vector = new Vector<String>();
        vector.add("a");
        Enumeration<String> en = vector.elements();
        ResettableIterator<String> it = IteratorUtils.asIterator(en, new ResettableIterator<String>() {
            @Override
            public void remove() {}
            @Override
            public String next() { return null; }
            @Override
            public boolean hasNext() { return false; }
            @Override
            public void reset() {}
        });
        Assert.assertNotNull(it);
    }

    @Test(expected = NullPointerException.class)
    public void testAsIteratorNullHandler() {
        Vector<String> vector = new Vector<String>();
        Enumeration<String> en = vector.elements();
        IteratorUtils.asIterator(en, null);
    }

    @Test
    public void testAsListIterator() {
        List<String> list = Arrays.asList("a", "b", "c");
        ListIterator<String> lit = IteratorUtils.asListIterator(list.iterator());
        Assert.assertNotNull(lit);
        Assert.assertTrue(lit.hasNext());
        Assert.assertEquals("a", lit.next());
        Assert.assertEquals("b", lit.next());
        Assert.assertEquals("a", lit.previous());
    }

    @Test
    public void testToArray() {
        List<String> list = Arrays.asList("a", "b", "c");
        Object[] array = IteratorUtils.toArray(list.iterator());
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, array);
    }

    @Test
    public void testToArrayWithClass() {
        List<String> list = Arrays.asList("a", "b", "c");
        String[] array = IteratorUtils.toArray(list.iterator(), String.class);
        Assert.assertArrayEquals(new String[]{"a", "b", "c"}, array);
    }

    @Test
    public void testToList() {
        List<String> list = Arrays.asList("a", "b", "c");
        List<String> result = IteratorUtils.toList(list.iterator());
        Assert.assertEquals(list, result);
    }

    @Test
    public void testToListWithEstimatedSize() {
        List<String> list = Arrays.asList("a", "b", "c");
        List<String> result = IteratorUtils.toList(list.iterator(), 2);
        Assert.assertEquals(list, result);
    }

    @Test
    public void testGet() {
        List<String> list = Arrays.asList("a", "b", "c");
        Assert.assertEquals("a", IteratorUtils.get(list.iterator(), 0));
        Assert.assertEquals("b", IteratorUtils.get(list.iterator(), 1));
        Assert.assertEquals("c", IteratorUtils.get(list.iterator(), 2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetOutOfBounds() {
        List<String> list = Arrays.asList("a", "b", "c");
        IteratorUtils.get(list.iterator(), 5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetNegativeIndex() {
        List<String> list = Arrays.asList("a", "b", "c");
        IteratorUtils.get(list.iterator(), -1);
    }

    @Test
    public void testIndexOf() {
        List<String> list = Arrays.asList("a", "b", "c", "b");
        Assert.assertEquals(0, IteratorUtils.indexOf(list.iterator(), new Predicate<String>() {
            @Override
            public boolean evaluate(String object) {
                return "a".equals(object);
            }
        }));
        Assert.assertEquals(1, IteratorUtils.indexOf(list.iterator(), new Predicate<String>() {
            @Override
            public boolean evaluate(String object) {
                return "b".equals(object);
            }
        }));
        Assert.assertEquals(-1, IteratorUtils.indexOf(list.iterator(), new Predicate<String>() {
            @Override
            public boolean evaluate(String object) {
                return "z".equals(object);
            }
        }));
        Assert.assertEquals(-1, IteratorUtils.indexOf(null, null));
    }

    @Test
    public void testContains() {
        List<String> list = Arrays.asList("a", "b", "c");
        Assert.assertTrue(IteratorUtils.contains(list.iterator(), "b"));
        Assert.assertFalse(IteratorUtils.contains(list.iterator(), "z"));
        Assert.assertFalse(IteratorUtils.contains(null, "a"));
    }

    @Test
    public void testSize() {
        List<String> list = Arrays.asList("a", "b", "c");
        Assert.assertEquals(3, IteratorUtils.size(list.iterator()));
        Assert.assertEquals(0, IteratorUtils.size(null));
    }

    @Test
    public void testIsEmpty() {
        List<String> list = Arrays.asList("a", "b", "c");
        Assert.assertFalse(IteratorUtils.isEmpty(list.iterator()));
        Assert.assertTrue(IteratorUtils.isEmpty(IteratorUtils.EMPTY_ITERATOR));
        Assert.assertTrue(IteratorUtils.isEmpty(null));
    }

    @Test
    public void testFilteredIterator() {
        List<String> list = Arrays.asList("a", "apple", "banana");
        Iterator<String> filtered = IteratorUtils.filteredIterator(list.iterator(), new Predicate<String>() {
            @Override
            public boolean evaluate(String object) {
                return object.startsWith("a");
            }
        });
        List<String> result = IteratorUtils.toList(filtered);
        Assert.assertEquals(Arrays.asList("a", "apple"), result);
    }

    @Test
    public void testFilteredListIterator() {
        List<String> list = Arrays.asList("a", "apple", "banana");
        ListIterator<String> filtered = IteratorUtils.filteredListIterator(list.listIterator(), new Predicate<String>() {
            @Override
            public boolean evaluate(String object) {
                return object.startsWith("a");
            }
        });
        Assert.assertNotNull(filtered);
        Assert.assertTrue(filtered.hasNext());
        Assert.assertEquals("a", filtered.next());
    }

    @Test
    public void testTransformIterator() {
        List<String> list = Arrays.asList("a", "b");
        Iterator<String> transformed = IteratorUtils.transformedIterator(list.iterator(), new Transformer<String, String>() {
            @Override
            public String transform(String input) {
                return input.toUpperCase();
            }
        });
        List<String> result = IteratorUtils.toList(transformed);
        Assert.assertEquals(Arrays.asList("A", "B"), result);
    }

    @Test
    public void testZippedIterator() {
        List<String> list1 = Arrays.asList("a", "c");
        List<String> list2 = Arrays.asList("b", "d");
        Iterator<String> zipped = IteratorUtils.zippedIterator(list1.iterator(), list2.iterator());
        List<String> result = IteratorUtils.toList(zipped);
        Assert.assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }

    @Test
    public void testChainedIterator() {
        List<String> list1 = Arrays.asList("a", "b");
        List<String> list2 = Arrays.asList("c", "d");
        Iterator<String> chained = IteratorUtils.chainedIterator(list1.iterator(), list2.iterator());
        List<String> result = IteratorUtils.toList(chained);
        Assert.assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }

    @Test
    public void testLoopingIterator() {
        List<String> list = Arrays.asList("a", "b");
        ResettableIterator<String> looping = IteratorUtils.loopingIterator(list);
        Assert.assertTrue(looping.hasNext());
        Assert.assertEquals("a", looping.next());
        Assert.assertEquals("b", looping.next());
        Assert.assertTrue(looping.hasNext());
        Assert.assertEquals("a", looping.next());
    }

    @Test
    public void testLoopingListIterator() {
        List<String> list = Arrays.asList("a", "b");
        ResettableListIterator<String> looping = IteratorUtils.loopingListIterator(list);
        Assert.assertTrue(looping.hasNext());
        Assert.assertEquals("a", looping.next());
        Assert.assertEquals("b", looping.next());
        Assert.assertEquals("a", looping.next());
    }

    @Test
    public void testCollatedIterator() {
        List<String> list1 = Arrays.asList("a", "c");
        List<String> list2 = Arrays.asList("b", "d");
        Iterator<String> collated = IteratorUtils.collatedIterator(null, list1.iterator(), list2.iterator());
        List<String> result = IteratorUtils.toList(collated);
        Assert.assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }

    @Test
    public void testUnmodifiableIterator() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        Iterator<String> unmod = IteratorUtils.unmodifiableIterator(list.iterator());
        Assert.assertTrue(unmod.hasNext());
        Assert.assertEquals("a", unmod.next());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnmodifiableIteratorRemove() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        Iterator<String> unmod = IteratorUtils.unmodifiableIterator(list.iterator());
        unmod.remove();
    }

    @Test
    public void testUnmodifiableListIterator() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        ListIterator<String> unmod = IteratorUtils.unmodifiableListIterator(list.listIterator());
        Assert.assertTrue(unmod.hasNext());
        Assert.assertEquals("a", unmod.next());
    }

    @Test
    public void testUnmodifiableMapIterator() {
        MapIterator<String, String> empty = IteratorUtils.emptyMapIterator();
        MapIterator<String, String> unmod = IteratorUtils.unmodifiableMapIterator(empty);
        Assert.assertNotNull(unmod);
        Assert.assertFalse(unmod.hasNext());
    }

    @Test
    public void testUnmodifiableSortedMapIterator() {
        SortedMapIterator<String, String> empty = IteratorUtils.emptySortedMapIterator();
        SortedMapIterator<String, String> unmod = IteratorUtils.unmodifiableSortedMapIterator(empty);
        Assert.assertNotNull(unmod);
        Assert.assertFalse(unmod.hasNext());
    }

    @Test
    public void testArrayIterator() {
        String[] array = {"a", "b", "c"};
        Iterator<String> it = IteratorUtils.arrayIterator(array);
        Assert.assertNotNull(it);
        Assert.assertEquals("a", it.next());
    }

    @Test
    public void testEnumerationIterator() {
        Vector<String> vector = new Vector<String>();
        vector.add("a");
        Iterator<String> it = IteratorUtils.asIterator(vector.elements());
        Assert.assertNotNull(it);
        Assert.assertEquals("a", it.next());
    }

    @Test
    public void testForEach() {
        List<String> list = Arrays.asList("a", "b");
        Closure<String> closure = new Closure<String>() {
            @Override
            public void execute(String input) {
                Assert.assertNotNull(input);
            }
        };
        IteratorUtils.forEach(list.iterator(), closure);
    }

    @Test
    public void testFind() {
        List<String> list = Arrays.asList("a", "b", "c");
        String found = IteratorUtils.find(list.iterator(), new Predicate<String>() {
            @Override
            public boolean evaluate(String object) {
                return "b".equals(object);
            }
        });
        Assert.assertEquals("b", found);
    }

    @Test
    public void testObjectArrayIterator() {
        Iterator<String> it = IteratorUtils.arrayIterator(new String[]{"x", "y"}, 0, 2);
        Assert.assertNotNull(it);
        Assert.assertEquals("x", it.next());
    }

    @Test
    public void testObjectListIterator() {
        ListIterator<String> lit = IteratorUtils.listIterator(new String[]{"x", "y"});
        Assert.assertNotNull(lit);
        Assert.assertEquals("x", lit.next());
    }

    @Test
    public void testFluentIterable() {
        Iterable<String> iterable = IteratorUtils.asIterable(IteratorUtils.EMPTY_ITERATOR);
        Assert.assertNotNull(iterable);
    }
}