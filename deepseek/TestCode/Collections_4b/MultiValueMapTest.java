package com.example; // Placeholder package, should match the source package

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for MultiValueMap.
 * This test class is designed to achieve high code coverage and detect potential faults.
 * It covers typical operations, edge cases, and boundary conditions.
 * Note: The actual source code for MultiValueMap was not provided, so this test assumes
 * a standard implementation similar to Apache Commons Collections MultiValueMap.
 * Adjust the package and imports as needed.
 */
public class MultiValueMapTest {

    private MultiValueMap<String, String> map;

    @Before
    public void setUp() {
        map = new MultiValueMap<>();
    }

    // ---------- Basic put and get ----------

    @Test
    public void testPutAndGetSingleValue() {
        map.put("key1", "value1");
        assertTrue(map.containsKey("key1"));
        assertEquals(1, map.size());
        assertEquals("value1", map.get("key1"));
    }

    @Test
    public void testPutMultipleValues() {
        map.put("key1", "value1");
        map.put("key1", "value2");
        assertTrue(map.containsKey("key1"));
        assertEquals(2, map.size());
        // Assuming get returns the first value or a collection? We'll test getCollection.
    }

    @Test
    public void testPutOverwritesOldValue() {
        map.put("key1", "value1");
        map.put("key1", "value2");
        // If put replaces the value, then size should be 1 and get returns the new value.
        // This depends on implementation. We'll test both possibilities.
        // For a standard MultiValueMap, put adds to the collection, so size increases.
        // We'll assume it adds.
        assertEquals(2, map.size());
    }

    // ---------- getCollection ----------

    @Test
    public void testGetCollectionReturnsAllValues() {
        map.put("key1", "value1");
        map.put("key1", "value2");
        java.util.Collection<String> values = map.getCollection("key1");
        assertNotNull(values);
        assertEquals(2, values.size());
        assertTrue(values.contains("value1"));
        assertTrue(values.contains("value2"));
    }

    @Test
    public void testGetCollectionForNonexistentKey() {
        java.util.Collection<String> values = map.getCollection("nonexistent");
        assertNull(values);
    }

    // ---------- containsKey and containsValue ----------

    @Test
    public void testContainsKey() {
        map.put("key1", "value1");
        assertTrue(map.containsKey("key1"));
        assertFalse(map.containsKey("key2"));
    }

    @Test
    public void testContainsValue() {
        map.put("key1", "value1");
        assertTrue(map.containsValue("value1"));
        assertFalse(map.containsValue("value2"));
    }

    // ---------- remove ----------

    @Test
    public void testRemoveKey() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.remove("key1");
        assertFalse(map.containsKey("key1"));
        assertEquals(1, map.size());
    }

    @Test
    public void testRemoveKeyValue() {
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.remove("key1", "value1");
        java.util.Collection<String> values = map.getCollection("key1");
        assertNotNull(values);
        assertEquals(1, values.size());
        assertTrue(values.contains("value2"));
    }

    @Test
    public void testRemoveNonexistentKey() {
        map.remove("nonexistent");
        // Should not throw
        assertTrue(map.isEmpty());
    }

    // ---------- clear ----------

    @Test
    public void testClear() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    // ---------- size and isEmpty ----------

    @Test
    public void testSizeEmpty() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testSizeAfterPuts() {
        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");
        assertEquals(3, map.size());
    }

    // ---------- iterator ----------

    @Test
    public void testIterator() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        java.util.Iterator<String> it = map.iterator();
        assertTrue(it.hasNext());
        String key1 = it.next();
        assertTrue(key1.equals("key1") || key1.equals("key2"));
        assertTrue(it.hasNext());
        String key2 = it.next();
        assertFalse(it.hasNext());
    }

    @Test
    public void testIteratorEmptyMap() {
        java.util.Iterator<String> it = map.iterator();
        assertFalse(it.hasNext());
    }

    // ---------- null key and null value ----------

    @Test(expected = NullPointerException.class)
    public void testPutNullKey() {
        map.put(null, "value");
    }

    @Test
    public void testPutNullValue() {
        map.put("key1", null);
        assertTrue(map.containsKey("key1"));
        assertNull(map.get("key1"));
    }

    @Test
    public void testGetNullKey() {
        try {
            map.get(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test
    public void testRemoveNullKey() {
        try {
            map.remove(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ---------- edge cases ----------

    @Test
    public void testPutSameValueTwice() {
        map.put("key1", "value1");
        map.put("key1", "value1");
        // Depending on implementation, duplicates may be allowed or not.
        // We'll test that the map handles it without error.
        assertEquals(2, map.size());
    }

    @Test
    public void testLargeNumberOfValues() {
        for (int i = 0; i < 1000; i++) {
            map.put("key", "value" + i);
        }
        assertEquals(1000, map.size());
        java.util.Collection<String> values = map.getCollection("key");
        assertEquals(1000, values.size());
    }

    @Test
    public void testMultipleKeys() {
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        assertEquals(3, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));
        assertTrue(map.containsKey("c"));
    }

    // ---------- additional fault detection tests ----------

    @Test
    public void testPutAfterRemove() {
        map.put("key1", "value1");
        map.remove("key1");
        map.put("key1", "value2");
        assertEquals("value2", map.get("key1"));
        assertEquals(1, map.size());
    }

    @Test
    public void testClearThenPut() {
        map.put("key1", "value1");
        map.clear();
        map.put("key2", "value2");
        assertFalse(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertEquals(1, map.size());
    }

    @Test
    public void testIteratorRemove() {
        map.put("key1", "value1");
        map.put("key2", "value2");
        java.util.Iterator<String> it = map.iterator();
        while (it.hasNext()) {
            String key = it.next();
            if ("key1".equals(key)) {
                it.remove();
            }
        }
        assertFalse(map.containsKey("key1"));
        assertTrue(map.containsKey("key2"));
        assertEquals(1, map.size());
    }

    @Test
    public void testGetCollectionUnmodifiable() {
        map.put("key1", "value1");
        java.util.Collection<String> values = map.getCollection("key1");
        try {
            values.add("value2");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected if collection is unmodifiable
        }
    }
}