package org.apache.commons.collections4.map;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

public class MultiValueMapTest {

    @Test
    public void testDefaultConstructorIsEmpty() {
        MultiValueMap map = new MultiValueMap();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testPutAndGetCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        map.put("key", "value2");
        assertEquals(2, map.size());
        Collection values = map.getCollection("key");
        assertNotNull(values);
        assertEquals(2, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
    }

    @Test
    public void testGetCollectionReturnsNullForAbsentKey() {
        MultiValueMap map = new MultiValueMap();
        assertNull(map.getCollection("absent"));
    }

    @Test
    public void testContainsMapping() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        assertTrue(map.containsMapping("key", "value"));
        assertFalse(map.containsMapping("key", "other"));
        assertFalse(map.containsMapping("absent", "value"));
    }

    @Test
    public void testContainsMappingWithNullValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", null);
        assertTrue(map.containsMapping("key", null));
    }

    @Test
    public void testContainsValueUsesEquals() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", new String("value"));
        assertTrue(map.containsValue(new String("value")));
    }

    @Test
    public void testContainsValueNull() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", null);
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testValuesReturnsAllValues() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key2", "value3");
        Collection allValues = map.values();
        assertEquals(3, allValues.size());
        assertTrue(allValues.contains("value1"));
        assertTrue(allValues.contains("value2"));
        assertTrue(allValues.contains("value3"));
    }

    @Test
    public void testKeySet() {
        MultiValueMap map = new MultiValueMap();
        map.put("key1", "v1");
        map.put("key2", "v2");
        Set keys = map.keySet();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
    }

    @Test
    public void testNullKey() {
        MultiValueMap map = new MultiValueMap();
        map.put(null, "value");
        assertTrue(map.containsKey(null));
        Collection values = map.getCollection(null);
        assertNotNull(values);
        assertEquals(1, values.size());
        assertTrue(values.contains("value"));
    }

    @Test
    public void testRemoveMappingExistingValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value1");
        map.put("key", "value2");
        assertTrue(map.removeMapping("key", "value1"));
        assertTrue(map.containsKey("key"));
        assertEquals(1, map.size());
        assertFalse(map.containsMapping("key", "value1"));
        assertTrue(map.containsMapping("key", "value2"));
    }

    @Test
    public void testRemoveMappingLastValueRemovesKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        assertTrue(map.removeMapping("key", "value"));
        assertFalse(map.containsKey("key"));
        assertNull(map.getCollection("key"));
    }

    @Test
    public void testRemoveMappingNonExistingValueOnNonEmptyCollection() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        assertFalse(map.removeMapping("key", "absent"));
        assertTrue(map.containsKey("key"));
        assertEquals(1, map.size());
    }

    @Test
    public void testRemoveMappingNonExistingValueOnEmptyCollection() {
        Map decorated = new HashMap();
        decorated.put("key", new ArrayList());
        MultiValueMap map = MultiValueMap.multiValueMap(decorated);
        assertTrue(map.containsKey("key"));
        assertFalse(map.removeMapping("key", "absent"));
        assertTrue(map.containsKey("key"));
        assertNotNull(map.getCollection("key"));
    }

    @Test
    public void testRemoveCollectionForKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        Object removed = map.remove("key");
        assertNotNull(removed);
        assertTrue(removed instanceof Collection);
        assertFalse(map.containsKey("key"));
    }

    @Test
    public void testClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("key", "value");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.getCollection("key"));
    }

    @Test
    public void testCustomCollectionFactory() {
        Map decorated = new HashMap();
        MultiValueMap map = MultiValueMap.multiValueMap(decorated, LinkedList.class);
        map.put("key", "value");
        Collection values = map.getCollection("key");
        assertNotNull(values);
        assertTrue(values instanceof LinkedList);
        assertEquals(1, values.size());
    }
}