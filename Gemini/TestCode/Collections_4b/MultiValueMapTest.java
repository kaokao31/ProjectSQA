package org.apache.commons.collections4.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MultiValueMapTest {

    private MultiValueMap<String, String> map;

    @Before
    public void setUp() {
        map = new MultiValueMap<>();
    }

    @Test
    public void testDefaultConstructor() {
        MultiValueMap<String, String> m = new MultiValueMap<>();
        assertNotNull(m);
        assertTrue(m.isEmpty());
    }

    @Test
    public void testWrappedConstructor() {
        Map<String, Collection<String>> originalMap = new HashMap<>();
        MultiValueMap<String, String> m = new MultiValueMap<>(originalMap, ArrayList.class);
        assertNotNull(m);
        assertTrue(m.isEmpty());
    }

    @Test
    public void testMultiValueMapFactoryMethod() {
        MultiValueMap<String, String> m = MultiValueMap.multiValueMap(new HashMap<>());
        assertNotNull(m);
        assertTrue(m.isEmpty());

        MultiValueMap<String, String> mWithFactory = MultiValueMap.multiValueMap(new HashMap<>(), ArrayList.class);
        assertNotNull(mWithFactory);
        assertTrue(mWithFactory.isEmpty());
    }

    @Test
    public void testPutSingleValue() {
        Object val = map.put("key1", "value1");
        assertNull(val);
        assertEquals(1, map.size());
        assertTrue(map.containsKey("key1"));
        assertTrue(map.containsValue("value1"));
        assertTrue(map.containsValue("key1", "value1"));
        assertFalse(map.containsValue("key1", "wrongValue"));
        assertFalse(map.containsValue("wrongKey", "value1"));
    }

    @Test
    public void testPutMultipleValuesSameKey() {
        map.put("key1", "value1");
        map.put("key1", "value2");
        assertEquals(1, map.size()); // Map size is number of keys in Apache Commons Collections MultiValueMap
        assertEquals(2, map.totalSize());
        
        Collection<String> coll = map.get("key1");
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertTrue(coll.contains("value1"));
        assertTrue(coll.contains("value2"));
    }

    @Test
    public void testPutAllCollection() {
        List<String> values = new ArrayList<>();
        values.add("v1");
        values.add("v2");

        boolean changed = map.putAll("key1", values);
        assertTrue(changed);
        assertEquals(2, map.totalSize());

        // Putting empty collection
        boolean changedEmpty = map.putAll("key1", new ArrayList<>());
        assertFalse(changedEmpty);

        // Putting null collection
        boolean changedNull = map.putAll("key1", null);
        assertFalse(changedNull);
    }

    @Test
    public void testPutAllMap() {
        MultiValueMap<String, String> other = new MultiValueMap<>();
        other.put("a", "1");
        other.put("b", "2");

        map.putAll(other);
        assertEquals(2, map.size());
        assertEquals("1", map.getCollection("a").iterator().next());

        // Test putAll with standard Map
        Map<String, String> standardMap = new HashMap<>();
        standardMap.put("c", "3");
        map.putAll(standardMap);
        assertEquals(3, map.size());
    }

    @Test
    public void testIterator() {
        map.put("key1", "v1");
        map.put("key1", "v2");
        map.put("key2", "v3");

        Iterator<String> it = map.iterator("key1");
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        assertTrue(it.hasNext());
        assertEquals("v2", it.next());
        assertFalse(it.hasNext());

        // Iterator for non-existent key
        Iterator<String> emptyIt = map.iterator("nonexistent");
        assertNotNull(emptyIt);
        assertFalse(emptyIt.hasNext());
    }

    @Test
    public void testTotalSize() {
        assertEquals(0, map.totalSize());
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v3");
        assertEquals(3, map.totalSize());
    }

    @Test
    public void testGetCollection() {
        map.put("k1", "v1");
        Collection<Object> c = map.getCollection("k1");
        assertNotNull(c);
        assertEquals(1, c.size());

        assertNull(map.getCollection("nonexistent"));
    }

    @Test
    public void testSizeOfKey() {
        assertEquals(0, map.size("k1"));
        map.put("k1", "v1");
        map.put("k1", "v2");
        assertEquals(2, map.size("k1"));
        assertEquals(0, map.size("nonexistent"));
    }

    @Test
    public void testClear() {
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals(2, map.size());
        
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.totalSize());
    }

    @Test
    public void testRemoveKey() {
        map.put("k1", "v1");
        map.put("k1", "v2");
        assertEquals(2, map.totalSize());

        Object removed = map.remove("k1");
        assertNotNull(removed);
        assertTrue(map.isEmpty());
        assertEquals(0, map.totalSize());

        assertNull(map.remove("nonexistent"));
    }

    @Test
    public void testRemoveKeyValue() {
        map.put("k1", "v1");
        map.put("k1", "v2");
        
        // Remove non-existing value
        boolean res1 = map.remove("k1", "v3");
        assertFalse(res1);
        assertEquals(2, map.totalSize());

        // Remove existing value
        boolean res2 = map.remove("k1", "v1");
        assertTrue(res2);
        assertEquals(1, map.totalSize());

        // Remove from non-existent key
        boolean res3 = map.remove("nonexistent", "v1");
        assertFalse(res3);

        // Remove last value should remove the key entirely
        boolean res4 = map.remove("k1", "v2");
        assertTrue(res4);
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey("k1"));
    }

    @Test
    public void testContainsValueKeySpecific() {
        map.put("k1", "v1");
        map.put("k2", "v2");

        assertTrue(map.containsValue("k1", "v1"));
        assertFalse(map.containsValue("k1", "v2"));
        assertFalse(map.containsValue("nonexistent", "v1"));
    }

    @Test
    public void testValuesCollection() {
        map.put("k1", "v1");
        map.put("k1", "v2");
        map.put("k2", "v2");

        Collection<Object> values = map.values();
        assertNotNull(values);
        // values() in MultiValueMap typically returns a collection of all values across all keys
        assertEquals(3, values.size());
        assertTrue(values.contains("v1"));
        assertTrue(values.contains("v2"));
    }
}