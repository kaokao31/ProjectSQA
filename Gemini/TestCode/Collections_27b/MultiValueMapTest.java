package org.apache.commons.collections4.map;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.Factory;
import org.apache.commons.collections4.MultiMap;
import org.junit.Test;

public class MultiValueMapTest {

    @Test
    public void testMultiValueMapConstructor() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testMultiValueMapWrappedConstructor() {
        Map<String, Object> innerMap = new HashMap<String, Object>();
        Factory<Collection<String>> factory = new Factory<Collection<String>>() {
            public Collection<String> create() {
                return new ArrayList<String>();
            }
        };
        MultiValueMap<String, String> map = new MultiValueMap<String, String>(innerMap, factory);
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiValueMapWrappedConstructorNullFactory() {
        Map<String, Object> innerMap = new HashMap<String, Object>();
        new MultiValueMap<String, String>(innerMap, null);
    }

    @Test
    public void testMultiMapFactoryMethod() {
        MultiMap<String, String> original = new MultiValueMap<String, String>();
        original.put("key1", "value1");

        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(original);
        assertNotNull(map);
        assertEquals("value1", map.get("key1", 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiMapFactoryMethodNull() {
        MultiValueMap.multiValueMap(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMultiMapFactoryMethodWrongType() {
        Map<String, String> normalMap = new HashMap<String, String>();
        MultiValueMap.multiValueMap((MultiMap) normalMap);
    }

    @Test
    public void testMapFactoryMethod() {
        Map<String, Collection<String>> innerMap = new HashMap<String, Collection<String>>();
        Factory<Collection<String>> factory = new Factory<Collection<String>>() {
            public Collection<String> create() {
                return new ArrayList<String>();
            }
        };
        MultiValueMap<String, String> map = MultiValueMap.multiValueMap(innerMap, factory);
        assertNotNull(map);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMapFactoryMethodNullFactory() {
        Map<String, Collection<String>> innerMap = new HashMap<String, Collection<String>>();
        MultiValueMap.multiValueMap(innerMap, null);
    }

    @Test
    public void testClear() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        assertFalse(map.isEmpty());
        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testRemoveMapping() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key1", "value2");

        // Remove single mapping
        assertEquals(Boolean.TRUE, map.removeMapping("key1", "value1"));
        assertEquals(1, map.size("key1"));

        // Remove non-existent mapping
        assertEquals(Boolean.FALSE, map.removeMapping("key1", "nonexistent"));
        assertEquals(Boolean.FALSE, map.removeMapping("nonexistentKey", "value2"));

        // Remove last mapping for key
        assertEquals(Boolean.TRUE, map.removeMapping("key1", "value2"));
        assertNull(map.get("key1"));
    }

    @Test
    public void testContainsValueWithKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        
        assertTrue(map.containsValue("key1", "value1"));
        assertFalse(map.containsValue("key1", "value2"));
        assertFalse(map.containsValue("nonexistent", "value1"));
    }

    @Test
    public void testContainsValueWithoutKey() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        assertTrue(map.containsValue("value1"));
        assertTrue(map.containsValue("value2"));
        assertFalse(map.containsValue("nonexistent"));
    }

    @Test
    public void testPut() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Object added = map.put("key1", "value1");
        assertNull(added);

        Object addedAgain = map.put("key1", "value2");
        assertEquals("value1", addedAgain);

        assertEquals(2, map.size("key1"));
    }

    @Test
    public void testPutAllCollection() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Collection<String> values = Arrays.asList("value1", "value2", "value3");
        
        boolean changed = map.putAll("key1", values);
        assertTrue(changed);
        assertEquals(3, map.size("key1"));

        boolean changedEmpty = map.putAll("key1", new ArrayList<String>());
        assertFalse(changedEmpty);

        boolean changedNull = map.putAll("key1", null);
        assertFalse(changedNull);
    }

    @Test
    public void testPutAllMap() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        Map<String, String> otherMap = new HashMap<String, String>();
        otherMap.put("key1", "value1");
        otherMap.put("key2", "value2");

        map.putAll(otherMap);
        assertEquals(1, map.size("key1"));
        assertEquals(1, map.size("key2"));
    }

    @Test
    public void testPutAllMultiMap() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        MultiValueMap<String, String> otherMap = new MultiValueMap<String, String>();
        otherMap.put("key1", "value1");
        otherMap.put("key1", "value2");

        map.putAll(otherMap);
        assertEquals(2, map.size("key1"));
    }

    @Test
    public void testIterator() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");

        Iterator<String> it = map.iterator("key1");
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("value1", it.next());
        assertTrue(it.hasNext());
        assertEquals("value2", it.next());
        assertFalse(it.hasNext());

        Iterator<String> emptyIt = map.iterator("nonexistent");
        assertNotNull(emptyIt);
        assertFalse(emptyIt.hasNext());
    }

    @Test
    public void testIteratorWildcard() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");

        Iterator<Map.Entry<String, String>> it = map.iterator();
        assertNotNull(it);
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testSizeOperations() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        assertEquals(0, map.totalSize());

        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");

        assertEquals(2, map.size("key1"));
        assertEquals(1, map.size("key2"));
        assertEquals(0, map.size("nonexistent"));
        assertEquals(3, map.totalSize());
    }

    @Test
    public void testGetCollection() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key1", "value2");

        Collection<Object> col = map.getCollection("key1");
        assertNotNull(col);
        assertEquals(2, col.size());

        assertNull(map.getCollection("nonexistent"));
    }

    @Test
    public void testGetIndex() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key1", "value2");

        assertEquals("value1", map.get("key1", 0));
        assertEquals("value2", map.get("key1", 1));
        assertNull(map.get("nonexistent", 0));
        
        try {
            map.get("key1", 5);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            map.get("key1", -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testRemoveIndex() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key1", "value2");

        Object removed = map.remove("key1", 0);
        assertEquals("value1", removed);
        assertEquals(1, map.size("key1"));

        // Remove last item, should remove key entirely
        Object removedLast = map.remove("key1", 0);
        assertEquals("value2", removedLast);
        assertNull(map.get("key1"));

        // Remove from nonexistent key
        assertNull(map.remove("nonexistent", 0));
    }

    @Test
    public void testValues() {
        MultiValueMap<String, String> map = new MultiValueMap<String, String>();
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value1");

        Collection<Object> values = map.values();
        assertNotNull(values);
        assertEquals(3, values.size());
    }
}