package org.apache.commons.collections.map;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class Flat3MapTest {

    private Flat3Map<Object, Object> map;

    @Before
    public void setUp() {
        map = new Flat3Map<>();
    }

    @Test
    public void testInitialState() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("key"));
        assertFalse(map.containsValue("value"));
        assertEquals(0, map.keySet().size());
        assertEquals(0, map.values().size());
        assertEquals(0, map.entrySet().size());
    }

    @Test
    public void testPutAndGetSingleEntry() {
        assertNull(map.put("a", "1"));
        assertEquals(1, map.size());
        assertEquals("1", map.get("a"));
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsValue("1"));
    }

    @Test
    public void testPutNullKeyAndValue() {
        assertNull(map.put(null, "nullValue"));
        assertNull(map.put("key", null));
        assertEquals(2, map.size());
        assertEquals("nullValue", map.get(null));
        assertEquals(null, map.get("key"));
        assertTrue(map.containsKey(null));
        assertTrue(map.containsKey("key"));
        assertTrue(map.containsValue(null));
        assertTrue(map.containsValue("nullValue"));
    }

    @Test
    public void testPutDuplicateKeyReturnsOldValue() {
        assertNull(map.put("a", "1"));
        assertEquals("1", map.put("a", "2"));
        assertEquals(1, map.size());
        assertEquals("2", map.get("a"));
    }

    @Test
    public void testPutThreeEntries() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        assertEquals(3, map.size());
        assertFalse(map.isEmpty());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
    }

    @Test
    public void testPutTriggersConversionToHashMap() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertEquals(4, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
        assertEquals("3", map.get("c"));
        assertEquals("4", map.get("d"));
    }

    @Test
    public void testGetAfterConversionFromMissing() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertNull(map.get("x"));
        assertEquals("1", map.get("a"));
    }

    @Test
    public void testRemoveOnFlatMap() {
        assertNull(map.remove("a"));
        map.put("a", "1");
        map.put("b", "2");
        assertEquals("1", map.remove("a"));
        assertEquals(1, map.size());
        assertEquals(null, map.get("a"));
        assertFalse(map.containsKey("a"));
        assertFalse(map.containsValue("1"));
        assertTrue(map.containsKey("b"));
    }

    @Test
    public void testRemoveOnHashMap() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertEquals("1", map.remove("a"));
        assertEquals(3, map.size());
        assertEquals(null, map.get("a"));
        assertFalse(map.containsKey("a"));
        assertFalse(map.containsValue("1"));
        assertTrue(map.containsKey("b"));
    }

    @Test
    public void testRemoveWhenKeyExistsInHashMode() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertEquals("4", map.remove("d"));
        assertEquals(3, map.size());
        assertEquals(null, map.get("d"));
        assertFalse(map.containsKey("d"));
    }

    @Test
    public void testClear() {
        map.put("a", "1");
        map.put("b", "2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("a"));
        assertFalse(map.containsValue("1"));
    }

    @Test
    public void testClearOnEmptyMap() {
        map.clear();
        assertTrue(map.isEmpty());
    }

    @Test
    public void testContainsKeyValueNull() {
        assertFalse(map.containsKey(null));
        assertFalse(map.containsValue(null));
        map.put(null, null);
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testKeySetOnFlatMap() {
        map.put("a", "1");
        map.put("b", "2");
        Set<Object> keySet = map.keySet();
        assertEquals(2, keySet.size());
        assertTrue(keySet.contains("a"));
        assertTrue(keySet.contains("b"));
        assertFalse(keySet.contains("c"));
    }

    @Test
    public void testKeySetOnHashMap() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        Set<Object> keySet = map.keySet();
        assertEquals(4, keySet.size());
        assertTrue(keySet.containsAll(Arrays.asList("a", "b", "c", "d")));
    }

    @Test
    public void testValuesOnFlatMap() {
        map.put("a", "1");
        map.put("b", "2");
        Collection<Object> values = map.values();
        assertEquals(2, values.size());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
    }

    @Test
    public void testValuesOnHashMap() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        Collection<Object> values = map.values();
        assertEquals(4, values.size());
        assertTrue(values.containsAll(Arrays.asList("1", "2", "3", "4")));
    }

    @Test
    public void testEntrySetOnFlatMap() {
        map.put("a", "1");
        map.put("b", "2");
        Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
        assertEquals(2, entrySet.size());
        for (Map.Entry<Object, Object> entry : entrySet) {
            if (entry.getKey().equals("a")) {
                assertEquals("1", entry.getValue());
            } else if (entry.getKey().equals("b")) {
                assertEquals("2", entry.getValue());
            } else {
                fail("Unexpected key: " + entry.getKey());
            }
        }
    }

    @Test
    public void testEntrySetOnHashMap() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
        assertEquals(4, entrySet.size());
        Map<Object, Object> copy = new HashMap<>();
        for (Map.Entry<Object, Object> entry : entrySet) {
            copy.put(entry.getKey(), entry.getValue());
        }
        assertEquals(4, copy.size());
        assertEquals("1", copy.get("a"));
        assertEquals("2", copy.get("b"));
        assertEquals("3", copy.get("c"));
        assertEquals("4", copy.get("d"));
    }

    @Test
    public void testEntrySetSetValue() {
        map.put("a", "1");
        map.put("b", "2");
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            if (entry.getKey().equals("a")) {
                entry.setValue("10");
            }
        }
        assertEquals("10", map.get("a"));
        assertTrue(map.containsValue("10"));
    }

    @Test
    public void testEqualsWithSameContentFlatVsFlat() {
        Flat3Map<Object, Object> map2 = new Flat3Map<>();
        map.put("a", "1");
        map.put("b", "2");
        map2.put("a", "1");
        map2.put("b", "2");
        assertTrue(map.equals(map2));
        assertTrue(map2.equals(map));
        assertTrue(map.hashCode() == map2.hashCode());
    }

    @Test
    public void testEqualsWithSameContentFlatVsHash() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        Flat3Map<Object, Object> map2 = new Flat3Map<>();
        map2.put("a", "1");
        map2.put("b", "2");
        map2.put("c", "3");
        map2.put("d", "4");
        assertTrue(map.equals(map2));
        assertTrue(map2.equals(map));
        assertTrue(map.hashCode() == map2.hashCode());
    }

    @Test
    public void testNotEqualsDifferentContent() {
        map.put("a", "1");
        Flat3Map<Object, Object> map2 = new Flat3Map<>();
        map2.put("a", "2");
        assertFalse(map.equals(map2));
    }

    @Test
    public void testEqualsWithHashMap() {
        map.put("a", "1");
        map.put("b", "2");
        Map<Object, Object> other = new HashMap<>();
        other.put("a", "1");
        other.put("b", "2");
        assertTrue(map.equals(other));
        assertTrue(other.equals(map));
    }

    @Test
    public void testEqualsWithNullValues() {
        map.put("a", null);
        map.put("b", null);
        Flat3Map<Object, Object> map2 = new Flat3Map<>();
        map2.put("a", null);
        map2.put("b", null);
        assertTrue(map.equals(map2));
    }

    @Test
    public void testHashCodeConsistency() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        int hashFlat = map.hashCode();
        map.put("d", "4"); // triggers conversion
        Map<Object, Object> other = new HashMap<>();
        other.put("a", "1");
        other.put("b", "2");
        other.put("c", "3");
        other.put("d", "4");
        assertTrue(map.equals(other));
        assertEquals(other.hashCode(), map.hashCode());
    }

    @Test
    public void testClone() {
        map.put("a", "1");
        map.put("b", "2");
        Flat3Map<Object, Object> clone = (Flat3Map<Object, Object>) map.clone();
        assertNotSame(map, clone);
        assertEquals(map, clone);
        assertEquals(map.hashCode(), clone.hashCode());
        clone.put("c", "3");
        assertNotEquals(map, clone);
        assertEquals(3, clone.size());
        assertEquals(2, map.size());
    }

    @Test
    public void testCloneInHashMode() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        Flat3Map<Object, Object> clone = (Flat3Map<Object, Object>) map.clone();
        assertEquals(map, clone);
        assertEquals(map.hashCode(), clone.hashCode());
        clone.put("e", "5");
        assertEquals(5, clone.size());
        assertEquals(4, map.size());
    }

    @Test
    public void testPutAllOnEmpty() {
        Map<Object, Object> source = new HashMap<>();
        source.put("a", "1");
        source.put("b", "2");
        map.putAll(source);
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    @Test
    public void testPutAllOnNonEmpty() {
        map.put("a", "1");
        map.put("b", "2");
        Map<Object, Object> source = new HashMap<>();
        source.put("b", "20");
        source.put("c", "3");
        map.putAll(source);
        assertEquals(3, map.size());
        assertEquals("20", map.get("b"));
        assertEquals("3", map.get("c"));
    }

    @Test
    public void testPutAllTriggersConversion() {
        Map<Object, Object> source = new HashMap<>();
        source.put("a", "1");
        source.put("b", "2");
        source.put("c", "3");
        source.put("d", "4");
        map.putAll(source);
        assertEquals(4, map.size());
        assertEquals("1", map.get("a"));
        assertEquals("4", map.get("d"));
    }

    @Test
    public void testSizeAfterRemoval() {
        map.put("a", "1");
        map.put("b", "2");
        map.remove("a");
        assertEquals(1, map.size());
        map.put("c", "3");
        assertEquals(2, map.size());
        map.remove("b");
        map.remove("c");
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testSizeAfterClearThenPut() {
        map.put("a", "1");
        map.clear();
        map.put("b", "2");
        assertEquals(1, map.size());
        assertTrue(map.containsKey("b"));
    }

    @Test
    public void testManyPutOperations() {
        for (int i = 0; i < 100; i++) {
            map.put(i, i * 2);
        }
        assertEquals(100, map.size());
        assertEquals(0, map.get(0));
        assertEquals(198, map.get(99));
        for (int i = 0; i < 50; i++) {
            assertEquals(i * 2, map.remove(i));
        }
        assertEquals(50, map.size());
        for (int i = 50; i < 100; i++) {
            assertTrue(map.containsKey(i));
        }
    }

    @Test
    public void testEntrySetIteratorRemove() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        Iterator<Map.Entry<Object, Object>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            if (entry.getKey().equals("a")) {
                it.remove();
            }
        }
        assertEquals(3, map.size());
        assertFalse(map.containsKey("a"));
    }

    @Test
    public void testKeySetIteratorRemove() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        Iterator<Object> it = map.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            if (key.equals("b")) {
                it.remove();
            }
        }
        assertEquals(2, map.size());
        assertFalse(map.containsKey("b"));
    }

    @Test
    public void testValuesIteratorRemove() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        Iterator<Object> it = map.values().iterator();
        while (it.hasNext()) {
            Object value = it.next();
            if (value.equals("2")) {
                it.remove();
            }
        }
        assertEquals(3, map.size());
        assertFalse(map.containsValue("2"));
    }

    @Test
    public void testNullKeyWithConversion() {
        map.put(null, "nullValue");
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertEquals("nullValue", map.get(null));
        assertTrue(map.containsKey(null));
        assertEquals(5, map.size());
    }

    @Test
    public void testRemoveNullKey() {
        map.put(null, "value");
        assertEquals("value", map.remove(null));
        assertEquals(0, map.size());
        assertFalse(map.containsKey(null));
    }

    @Test
    public void testContainsAfterRemoval() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        map.remove("b");
        assertFalse(map.containsKey("b"));
        assertTrue(map.containsKey("a"));
        assertFalse(map.containsValue("2"));
    }

    @Test
    public void testPutExistingKeyWhenFullDoesNotConvert() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        assertEquals("1", map.put("a", "10"));
        assertEquals(3, map.size());
        assertEquals("10", map.get("a"));
    }

    @Test
    public void testRemoveAllEntriesAfterConversion() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        map.remove("a");
        map.remove("b");
        map.remove("c");
        map.remove("d");
        assertTrue(map.isEmpty());
        map.put("x", "y");
        assertEquals(1, map.size());
        assertEquals("y", map.get("x"));
    }

    @Test
    public void testNullKeyValueAfterConversion() {
        map.put(null, null);
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertTrue(map.containsKey(null));
        assertEquals(null, map.get(null));
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testPutDuplicateKeyAfterConversion() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        assertEquals("1", map.put("a", "100"));
        assertEquals(4, map.size());
        assertEquals("100", map.get("a"));
    }

    @Test
    public void testRemoveMissingKey() {
        assertNull(map.remove("missing"));
    }
}