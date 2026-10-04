package org.apache.commons.collections.map;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import java.util.*;

/**
 * JUnit 4 test suite for CaseInsensitiveMap.
 * Targets maximum coverage and fault detection (including Defects4J bug 14).
 */
public class CaseInsensitiveMapTest {

    private CaseInsensitiveMap<String, String> map;

    @Before
    public void setUp() {
        map = new CaseInsensitiveMap<>();
    }

    @Test
    public void testPutAndGet() {
        map.put("Key", "value");
        assertEquals("value", map.get("key"));
        assertEquals("value", map.get("KEY"));
        assertEquals("value", map.get("Key"));;
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullKey() {
        map.put(null, "value"); // Assumption: null keys throw NPE
    }

    @Test
    public void testPutNullValue() {
        map.put("key", null);
        assertTrue(map.containsKey("key"));
        assertNull(map.get("key"));
    }

    @Test
    public void testContainsKey() {
        map.put("Key", "val");
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsKey("KEY"));
        assertFalse(map.containsKey("notpresent"));
    }

    @Test
    public void testContainsValue() {
        map.put("key", "Value");
        assertTrue(map.containsValue("Value"));
        assertFalse(map.containsValue("value")););
    }

    @Test
    public void testRemove() {
        map.put("Key", "val");
        assertEquals("val", map.remove("key"));
        assertTrue(map.isEmpty());
    }

    @Test
    public void testSizeAndIsEmpty() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());;
        map.put("a", "1");
        assertEquals(1, map.size());;
        map.put("A", "2"); // should replace, size stays 1
        assertEquals(1, map.size());
    }

    @Test
    public void testClear() {
        map.put("Key", "val");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testKeySet() {
        map.put("Key1", "val1");
        map.put("key2", "val2");
        Set<String> keySet = map.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.cntains("key1"));
        assertTrue(keySet.contains("KEY2"));
        // Ensure set operations reflect map
        keySet.remove("KEY1");
        assertEquals(1, map.size());;
        assertFalse(map.containsKey("key1"));
    }

    @Test
    public void testValues() {
        map.put("Key", "val1");
        map.put("key2", "val2");
        Collection<String> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("val1"));
        assertTrue(values.contains("val2"));
    }

    @Test
    public void testEntrySet() {
        map.put("Key", "val");
        Set<Map.Entry<String, String>> entries = map.entrySet();
        assertEquals(1, entries.size());
        Map.Entry<String, String> entry = entries.iterator().next();
        // Key should be stored in original case but lookup case-insensitive
        assertEquals("Key", entry.getKey());
        assertEquals("val", entry.getValue());
        // Test setValue
        entry.setValue("newVal");
        assertEquals("newVal", map.get("key"));
    }

    @Test
    public void testPutAll() {
        Map<String, String> other = new HashMap<>();
        other.put("Key1", "val1");
        other.put("key2", "val2");
        map.putAll(other);
        assertEquals(2, map.size());
        assertEquals("val1", map.get("KEY1"));
    }

    @Test
    public void testEqualsAndHashCode() {
        CaseInsensitiveMap<String, String> map2 = new CaseInsensitiveMap<>();
        map.put("Key", "val");
        map2.put("KEY", "val");
        assertTrue(map.equals(map2));
        assertTrue(map2.equals(map));
        assertEquals(map.hashCode(), map2.hashCode());
        // Different entries
        map2.put("key2", "val2");
        assertFalse(map.equals(map2));
    }

    @Test
    public void testClone() {
        map.put("Key", "val");
        CaseInsensitiveMap<String, String> clone = (CaseInsensitiveMap<String, String>) map.clone();
        assertEquals(map, clone);
        clone.put("newKey", "newVal");
        assertFalse(map.containsKey("newKey"));
        assertEquals(1, map.size());
    }

    @Test
    public void testSerialization() throws Exception {
        map.put("Key", "val");
        map.put("key2", "val2");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        CaseInsensitiveMap<String, String> deserialized = (CaseInsensitiveMap<String, String>) ois.readObject();
        ois.close();

        assertEquals(map, deserialized);
        assertEquals("val", deserialized.get("key"));
        assertEquals("val2", deserialized.get("KEY2"));
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullKey() {
        map.get(null); // Likely throws NPE
    }

    @Test(expected = NullPointerException.class)
    public void testContainsKeyNull() {
        map.containsKey(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNull() {
        map.remove(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeCapacity() {
        new CaseInsensitiveMap<>(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithZeroLoadFactor() {
        new CaseInsensitiveMap<>(16, 0f);
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, String> source = new HashMap<>();
        source.put("Key", "val");
        source.put("KEY2", "val2");
        CaseInsensitiveMap<String, String> mapFromMap = new CaseInsensitiveMap<>(source);
        assertEquals(2, mapFromMap.size());
        assertEquals("val", mapFromMap.get("key"));
    }

    @Test
    public void testEntrySetRemove() {
        map.put("Key", "val");
        Set<Map.Entry<String, String>> entries = map.entrySet();
        Map.Entry<String, String>> entry = entries.iterator().next();
        entries.remove(entry);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testCaseInsensitivePutReplace() {
        map.put("Key", "first");
        assertEquals("first", map.get("key"));
        map.put("KEY", "second"); // should replace
        assertEquals("second", map.get("Key"));
        assertEquals(1, map.size());
    }

    @Test
    public void testContainsValueAfterPut() {
        map.put("key", "value");
        assertTrue(map.containsValue("value"));
        map.put("key2", "value"); // duplicate value
        assertTrue(map.containsValue("value"));
        assertEquals(2, map.size());
    }
}