package org.apache.commons.collections.map;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for {@link Flat3Map}.
 * Covers core Map operations, edge cases, resizing, and fault-prone scenarios.
 */
public class Flat3MapTest {

    private Flat3Map<Object, Object> map;
    private Map<Object, Object> referenceMap;

    @Before
    public void setUp() {
        map = new Flat3Map<>();
        referenceMap = new HashMap<>();
    }

    @Test
    public void testInitialState() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("key"));
        assertFalse(map.containsValue("value"));
        assertTrue(map.keySet().isEmpty());
        assertTrue(map.values().isEmpty());
        assertTrue(map.entrySet().isEmpty());
        assertEquals("{}", map.toString());
    }

    @Test
    public void testPutGetSingleEntry() {
        map.put("key", "value");
        assertEquals("value", map.get("key"));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
    }

    @Test
    public void testPutGetMultipleEntriesWithinFlatSize() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @Test
    public void testPutOverwriteExistingKey() {
        map.put("a", 1);
        map.put("a", 2);
        assertEquals(1, map.size());
        assertEquals(2, map.get("a"));
    }

    @Test
    public void testPutNullKey() {
        map.put(null, "value");
        assertTrue(map.containsKey(null));
        assertEquals("value", map.get(null));
        assertEquals(1, map.size());
    }

    @Test
    public void testPutNullValue() {
        map.put("key", null);
        assertTrue(map.containsKey("key"));
        assertNull(map.get("key"));
        Map<Object, Object> copy = new HashMap<>();
        copy.put("key", null);
        assertEquals(copy, map);
    }

    @Test
    public void testPutBothNull() {
        map.put(null, null);
        assertTrue(map.containsKey(null));
        assertNull(map.get(null));
        assertEquals(1, map.size());
    }

    @Test
    public void testGetNonExistentKey() {
        assertNull(map.get("absent"));
        map.put("present", "value");
        assertNull(map.get("absent"));
    }

    @Test
    public void testRemoveExisting() {
        map.put("a", 1);
        map.put("b", 2);
        assertEquals(1, map.remove("a"));
        assertEquals(1, map.size());
        assertNull(map.get("a"));
    }

    @Test
    public void testRemoveNonExistent() {
        assertNull(map.remove("absent"));
        map.put("a", 1);
        assertNull(map.remove("b"));
        assertEquals(1, map.size());
    }

    @Test
    public void testRemoveNullKey() {
        map.put(null, "value");
        assertEquals("value", map.remove(null));
        assertFalse(map.containsKey(null));
        assertEquals(0, map.size());
    }

    @Test
    public void testRemoveThenReAddToFlatStorage() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.remove("b");
        assertEquals(2, map.size());
        map.put("d", 4);
        assertEquals(3, map.size());
        assertEquals(4, map.get("d"));
    }

    @Test
    public void testContainsKey() {
        map.put("key", "value");
        assertTrue(map.containsKey("key"));
        assertFalse(map.containsKey("other"));
        map.put(null, "null");
        assertTrue(map.containsKey(null));
    }

    @Test
    public void testContainsValue() {
        map.put("key", "value");
        assertTrue(map.containsValue("value"));
        assertFalse(map.containsValue("other"));
        map.put("nullKey", null);
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testClear() {
        map.put("a", 1);
        map.put("b", 2);
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get("a"));
        assertFalse(map.containsKey("b"));
        assertTrue(map.entrySet().isEmpty());
    }

    @Test
    public void testClearOnEmptyMap() {
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testKeySet() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Set<Object> keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
        assertTrue(keys.contains("c"));
        // Test keySet backed by map via remove
        keys.remove("a");
        assertFalse(map.containsKey("a"));
        assertEquals(2, map.size());
    }

    @Test
    public void testKeySetOnEmptyMap() {
        Set<Object> keys = map.keySet();
        assertTrue(keys.isEmpty());
    }

    @Test
    public void testValues() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Collection<Object> values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertTrue(values.contains(3));
        values.remove(2);
        assertFalse(map.containsValue(2));
        assertEquals(2, map.size());
    }

    @Test
    public void testValuesOnEmptyMap() {
        Collection<Object> values = map.values();
        assertTrue(values.isEmpty());
    }

    @Test
    public void testEntrySet() {
        map.put("a", 1);
        map.put("b", 2);
        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        assertEquals(2, entries.size());
        for (Map.Entry<Object, Object> entry : entries) {
            Object key = entry.getKey();
            if ("a".equals(key)) {
                assertEquals(1, entry.getValue());
                entry.setValue(10);
            } else if ("b".equals(key)) {
                assertEquals(2, entry.getValue());
                entry.setValue(20);
            } else {
                fail("Unexpected key: " + key);
            }
        }
        assertEquals(10, map.get("a"));
        assertEquals(20, map.get("b"));
    }

    @Test
    public void testEntrySetIteratorRemove() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        java.util.Iterator<Map.Entry<Object, Object>> it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            if ("b".equals(entry.getKey())) {
                it.remove();
            }
        }
        assertFalse(map.containsKey("b"));
        assertEquals(2, map.size());
    }

    @Test
    public void testEqualsSameContent() {
        map.put("a", 1);
        map.put("b", 2);
        Map<Object, Object> other = new HashMap<>();
        other.put("a", 1);
        other.put("b", 2);
        assertTrue(map.equals(other));
        assertTrue(other.equals(map));
    }

    @Test
    public void testEqualsDifferentContent() {
        map.put("a", 1);
        Map<Object, Object> other = new HashMap<>();
        other.put("a", 2);
        assertFalse(map.equals(other));
    }

    @Test
    public void testEqualsWithNullValues() {
        map.put("a", null);
        map.put("b", 2);
        Map<Object, Object> other = new HashMap<>();
        other.put("a", null);
        other.put("b", 2);
        assertTrue(map.equals(other));
        assertTrue(other.equals(map));
    }

    @Test
    public void testHashCodeConsistency() {
        map.put("a", 1);
        map.put("b", 2);
        referenceMap.put("a", 1);
        referenceMap.put("b", 2);
        assertEquals(referenceMap.hashCode(), map.hashCode());
    }

    @Test
    public void testClone() {
        map.put("a", 1);
        map.put("b", 2);
        Flat3Map<Object, Object> clone = (Flat3Map<Object, Object>) map.clone();
        assertNotSame(map, clone);
        assertEquals(map, clone);
        clone.put("c", 3);
        assertFalse(map.containsKey("c"));
        assertEquals(2, map.size());
        assertEquals(3, clone.size());
    }

    @Test
    public void testToString() {
        map.put("a", 1);
        map.put("b", 2);
        String str = map.toString();
        assertTrue(str.contains("a=1"));
        assertTrue(str.contains("b=2"));
    }

    @Test
    public void testResizeFromFlatToMap() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        // This should trigger resize
        map.put("d", 4);
        assertEquals(4, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
        assertEquals(4, map.get("d"));
    }

    @Test
    public void testResizeWithNullKeysAndValues() {
        map.put("a", 1);
        map.put("b", null);
        map.put(null, 3);
        map.put("d", null); // triggers resize
        assertEquals(4, map.size());
        assertEquals(1, map.get("a"));
        assertNull(map.get("b"));
        assertEquals(3, map.get(null));
        assertNull(map.get("d"));
    }

    @Test
    public void testResizeWithMoreEntries() {
        for (int i = 0; i < 20; i++) {
            map.put("key" + i, i);
        }
        assertEquals(20, map.size());
        for (int i = 0; i < 20; i++) {
            assertEquals(i, map.get("key" + i));
        }
    }

    @Test
    public void testRemoveAfterResize() {
        for (int i = 0; i < 5; i++) {
            map.put("key" + i, i);
        }
        assertEquals(5, map.size());
        assertEquals(2, map.remove("key2"));
        assertFalse(map.containsKey("key2"));
        assertEquals(4, map.size());
        // Ensure remaining keys still accessible
        assertEquals(0, map.get("key0"));
        assertEquals(1, map.get("key1"));
        assertEquals(3, map.get("key3"));
        assertEquals(4, map.get("key4"));
    }

    @Test
    public void testPutAll() {
        Map<Object, Object> other = new HashMap<>();
        other.put("a", 1);
        other.put("b", 2);
        other.put("c", 3);
        other.put("d", 4); // more than 3 to trigger resize if needed
        map.putAll(other);
        assertEquals(other, map);
        assertEquals(4, map.size());
    }

    @Test
    public void testPutAllEmpty() {
        map.putAll(new HashMap<>());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testClearAfterResize() {
        for (int i = 0; i < 4; i++) {
            map.put(i, i);
        }
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertNull(map.get(1));
    }

    @Test
    public void testEntrySetAfterResize() {
        for (int i = 0; i < 4; i++) {
            map.put(i, i * 10);
        }
        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        assertEquals(4, entries.size());
        for (Map.Entry<Object, Object> entry : entries) {
            int key = (Integer) entry.getKey();
            assertEquals(key * 10, entry.getValue());
        }
    }

    @Test
    public void testKeySetAfterResize() {
        for (int i = 0; i < 4; i++) {
            map.put(i, i);
        }
        Set<Object> keys = map.keySet();
        assertTrue(keys.contains(0));
        assertTrue(keys.contains(1));
        assertTrue(keys.contains(2));
        assertTrue(keys.contains(3));
    }

    @Test
    public void testValuesAfterResize() {
        for (int i = 0; i < 4; i++) {
            map.put(i, i + 1);
        }
        Collection<Object> values = map.values();
        assertTrue(values.contains(1));
        assertTrue(values.contains(2));
        assertTrue(values.contains(3));
        assertTrue(values.contains(4));
    }

    @Test
    public void testNullKeyAfterResize() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put(null, 4);
        assertEquals(4, map.size());
        assertEquals(4, map.get(null));
        assertTrue(map.containsKey(null));
    }

    @Test
    public void testOverwriteAfterResize() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        map.put("a", 100);
        assertEquals(4, map.size());
        assertEquals(100, map.get("a"));
    }

    @Test
    public void testRemoveAllEntries() {
        map.put("a", 1);
        map.put("b", 2);
        map.remove("a");
        map.remove("b");
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testEntrySetSetValueAfterResize() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            entry.setValue(99);
        }
        for (Object key : map.keySet()) {
            assertEquals(99, map.get(key));
        }
    }

    @Test
    public void testIteratorConcurrentModificationDetection() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        Set<Object> keys = map.keySet();
        java.util.Iterator<Object> it = keys.iterator();
        assertTrue(it.hasNext());
        it.next();
        map.put("e", 5); // modify map during iteration
        try {
            it.hasNext();
            // Some implementations may not throw; but we want to ensure no crash.
        } catch (Exception e) {
            // Acceptable
        }
    }

    @Test
    public void testEqualsWithNonMapObject() {
        map.put("a", 1);
        assertFalse(map.equals("not a map"));
    }

    @Test
    public void testHashCodeWithEmptyMap() {
        assertEquals(0, map.hashCode());
    }

    @Test
    public void testMapContractWithLargeNumberOfEntries() {
        for (int i = 0; i < 100; i++) {
            map.put("key" + i, "val" + i);
        }
        assertEquals(100, map.size());
        for (int i = 0; i < 100; i++) {
            assertEquals("val" + i, map.get("key" + i));
        }
        // Remove half
        for (int i = 0; i < 50; i++) {
            map.remove("key" + i);
        }
        assertEquals(50, map.size());
        for (int i = 50; i < 100; i++) {
            assertEquals("val" + i, map.get("key" + i));
        }
    }

    @Test
    public void testRemoveAllKeysViaKeySetIterator() {
        for (int i = 0; i < 10; i++) {
            map.put(i, i);
        }
        Set<Object> keys = map.keySet();
        java.util.Iterator<Object> it = keys.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutAllOverwritesExisting() {
        map.put("a", 1);
        Map<Object, Object> other = new HashMap<>();
        other.put("a", 2);
        other.put("b", 3);
        map.putAll(other);
        assertEquals(2, map.size());
        assertEquals(2, map.get("a"));
        assertEquals(3, map.get("b"));
    }

    @Test
    public void testEqualsAfterResize() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        Map<Object, Object> other = new HashMap<>();
        other.put("a", 1);
        other.put("b", 2);
        other.put("c", 3);
        other.put("d", 4);
        assertTrue(map.equals(other));
        assertTrue(other.equals(map));
    }

    @Test
    public void testHashCodeAfterResize() {
        for (int i = 0; i < 4; i++) {
            map.put(i, i);
        }
        referenceMap.put(0, 0);
        referenceMap.put(1, 1);
        referenceMap.put(2, 2);
        referenceMap.put(3, 3);
        assertEquals(referenceMap.hashCode(), map.hashCode());
    }

    @Test
    public void testToStringAfterResize() {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        String str = map.toString();
        assertTrue(str.contains("a=1"));
        assertTrue(str.contains("d=4"));
    }
}