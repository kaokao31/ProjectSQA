package org.apache.commons.collections4.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ListOrderedMapTest {

    @Test
    public void testConstructionAndDecorate() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        assertNotNull(map);
        assertTrue(map.isEmpty());

        Map<String, String> ordinaryMap = new HashMap<String, String>();
        ordinaryMap.put("A", "1");
        ordinaryMap.put("B", "2");

        ListOrderedMap<String, String> decorated = ListOrderedMap.listOrderedMap(ordinaryMap);
        assertNotNull(decorated);
        assertEquals(2, decorated.size());
        assertEquals("1", decorated.get("A"));
        assertEquals("2", decorated.get("B"));

        // Decorating already a ListOrderedMap should return the same or wrapped instance properly
        ListOrderedMap<String, String> redocorated = ListOrderedMap.listOrderedMap(decorated);
        assertNotNull(redocorated);
        assertEquals(2, redocorated.size());
    }

    @Test
    public void testPutAndGet() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        assertNull(map.put("key1", "value1"));
        assertEquals("value1", map.put("key1", "value1-updated"));
        assertEquals("value1-updated", map.get("key1"));
        assertEquals(1, map.size());

        map.put("key2", "value2");
        assertEquals(2, map.size());
        assertEquals("key1", map.get(0));
        assertEquals("key2", map.get(1));
    }

    @Test
    public void testPutAtIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("key1", "value1");
        map.put("key3", "value3");

        // Insert at index 1
        map.put(1, "key2", "value2");
        assertEquals(3, map.size());
        assertEquals("key1", map.get(0));
        assertEquals("key2", map.get(1));
        assertEquals("key3", map.get(2));

        assertEquals("value2", map.get("key2"));

        // Put at existing index with same key
        map.put(1, "key2", "value2-bis");
        assertEquals("value2-bis", map.get("key2"));
        assertEquals(3, map.size());

        // Put existing key at a different index
        map.put(0, "key3", "value3-bis");
        assertEquals("key3", map.get(0));
        assertEquals("key1", map.get(1));
        assertEquals("key2", map.get(2));
        assertEquals(3, map.size());

        try {
            map.put(-1, "bad", "bad");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            map.put(10, "bad", "bad");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testPutAll() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");

        Map<String, String> other = new HashMap<String, String>();
        other.put("B", "2");
        other.put("C", "3");

        map.putAll(other);
        assertEquals(3, map.size());
        assertEquals("A", map.get(0));
        assertEquals("B", map.get(1));
        assertEquals("C", map.get(2));

        // putAll at index
        ListOrderedMap<String, String> map2 = new ListOrderedMap<String, String>();
        map2.put("A", "1");
        map2.put("D", "4");

        Map<String, String> insertMap = new HashMap<String, String>();
        insertMap.put("B", "2");
        insertMap.put("C", "3");

        map2.putAll(1, insertMap);
        assertEquals(4, map2.size());
        assertEquals("A", map2.get(0));
        assertEquals("B", map2.get(1));
        assertEquals("C", map2.get(2));
        assertEquals("D", map2.get(3));

        try {
            map2.putAll(-1, insertMap);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            map2.putAll(10, insertMap);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testRemove() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        assertEquals("2", map.remove("B"));
        assertEquals(2, map.size());
        assertEquals("A", map.get(0));
        assertEquals("C", map.get(1));
        assertNull(map.remove("NON-EXISTENT"));

        // Remove by index
        assertEquals("A", map.remove(0));
        assertEquals(1, map.size());
        assertEquals("C", map.get(0));

        try {
            map.remove(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            map.remove(5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testClearAndEmpty() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());

        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("A"));
    }

    @Test
    public void testIndexOf() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        assertEquals(0, map.indexOf("A"));
        assertEquals(1, map.indexOf("B"));
        assertEquals(2, map.indexOf("C"));
        assertEquals(-1, map.indexOf("Z"));
    }

    @Test
    public void testGetAndSetByIndex() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");

        assertEquals("A", map.get(0));
        assertEquals("B", map.get(1));

        try {
            map.get(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            map.get(2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        // test valueByIndex
        assertEquals("1", map.getValue(0));
        assertEquals("2", map.getValue(1));

        try {
            map.getValue(-1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            map.getValue(2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        // test setValueByIndex
        assertEquals("1", map.setValue(0, "1-new"));
        assertEquals("1-new", map.get("A"));

        try {
            map.setValue(-1, "bad");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            map.setValue(2, "bad");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testViewsAndIterators() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        // keyList()
        List<String> keyList = map.keyList();
        assertNotNull(keyList);
        assertEquals(3, keyList.size());
        assertEquals("A", keyList.get(0));

        // asList()
        List<String> asList = map.asList();
        assertNotNull(asList);
        assertEquals(3, asList.size());

        // keySet()
        Set<String> keySet = map.keySet();
        assertNotNull(keySet);
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains("A"));

        // values()
        Collection<String> values = map.values();
        assertNotNull(values);
        assertEquals(3, values.size());

        // entrySet()
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        assertNotNull(entrySet);
        assertEquals(3, entrySet.size());

        Iterator<Map.Entry<String, String>> entryIter = entrySet.iterator();
        assertTrue(entryIter.hasNext());
        Map.Entry<String, String> entry = entryIter.next();
        assertEquals("A", entry.getKey());
        assertEquals("1", entry.getValue());
        
        entry.setValue("1-alt");
        assertEquals("1-alt", map.get("A"));

        // test orderedMapIterator
        OrderedMapIterator<String, String> iter = map.mapIterator();
        assertNotNull(iter);
        assertTrue(iter.hasNext());
        assertEquals("A", iter.next());
        assertEquals("A", iter.getKey());
        assertEquals("1-alt", iter.getValue());

        assertEquals("1-alt", iter.setValue("1-new"));
        assertEquals("1-new", map.get("A"));

        assertTrue(iter.hasPrevious());
        assertEquals("A", iter.previous());

        iter.remove();
        assertEquals(2, map.size());
        assertFalse(map.containsKey("A"));
    }

    @Test
    public void testToString() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        String str = map.toString();
        assertNotNull(str);
        assertTrue(str.contains("A=1"));
    }

    @Test
    public void testAliasingAndDuplicates() {
        ListOrderedMap<String, String> map = new ListOrderedMap<String, String>();
        map.put("A", "1");
        map.put("B", "2");
        // Overwriting existing key should not change its order index
        map.put("A", "1-updated");
        assertEquals(2, map.size());
        assertEquals("A", map.get(0));
        assertEquals("B", map.get(1));
        assertEquals("1-updated", map.get("A"));
    }
}