package org.apache.commons.collections4.trie;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.Map.Entry;

import static org.junit.Assert.*;

/**
 * Test suite for UnmodifiableTrie.
 * Achieves high coverage and detects potential faults (e.g., modifiable views).
 */
public class UnmodifiableTrieTest {

    private Trie<String, String> decorated;
    private UnmodifiableTrie<String, String> unmodifiableTrie;

    @Before
    public void setUp() {
        // Use a PatriciaTrie as the decorated trie
        decorated = new PatriciaTrie<>();
        decorated.put("key1", "value1");
        decorated.put("key2", "value2");
        decorated.put("key3", "value3");
        unmodifiableTrie = new UnmodifiableTrie<>(decorated);
    }

    // ---------- Mutating methods must throw UnsupportedOperationException ----------

    @Test(expected = UnsupportedOperationException.class)
    public void testPut() {
        unmodifiableTrie.put("newKey", "newValue");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll() {
        Map<String, String> map = new java.util.HashMap<>();
        map.put("a", "1");
        unmodifiableTrie.putAll(map);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() {
        unmodifiableTrie.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        unmodifiableTrie.remove("key1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutWithNullKey() {
        // Should throw UnsupportedOperationException, not NullPointerException
        unmodifiableTrie.put(null, "value");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutWithNullValue() {
        unmodifiableTrie.put("key", null);
    }

    // ---------- Read methods must delegate to decorated trie ----------

    @Test
    public void testGet() {
        assertEquals("value1", unmodifiableTrie.get("key1"));
        assertNull(unmodifiableTrie.get("nonexistent"));
    }

    @Test
    public void testContainsKey() {
        assertTrue(unmodifiableTrie.containsKey("key1"));
        assertFalse(unmodifiableTrie.containsKey("nonexistent"));
    }

    @Test
    public void testContainsValue() {
        assertTrue(unmodifiableTrie.containsValue("value1"));
        assertFalse(unmodifiableTrie.containsValue("nonexistent"));
    }

    @Test
    public void testIsEmpty() {
        assertFalse(unmodifiableTrie.isEmpty());
        // Create an empty unmodifiable trie
        Trie<String, String> empty = new PatriciaTrie<>();
        UnmodifiableTrie<String, String> emptyUnmod = new UnmodifiableTrie<>(empty);
        assertTrue(emptyUnmod.isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(3, unmodifiableTrie.size());
        // After decorating, size should reflect decorated
        decorated.put("extra", "value");
        assertEquals(4, unmodifiableTrie.size());
    }

    // ---------- View collections must be unmodifiable ----------

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetAdd() {
        unmodifiableTrie.keySet().add("newKey");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetRemove() {
        unmodifiableTrie.keySet().remove("key1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetIteratorRemove() {
        Iterator<String> it = unmodifiableTrie.keySet().iterator();
        it.next();
        it.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValuesAdd() {
        unmodifiableTrie.values().add("newValue");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValuesRemove() {
        unmodifiableTrie.values().remove("value1");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValuesIteratorRemove() {
        Iterator<String> it = unmodifiableTrie.values().iterator();
        it.next();
        it.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetAdd() {
        unmodifiableTrie.entrySet().add(new java.util.AbstractMap.SimpleEntry<>("k", "v"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetRemove() {
        unmodifiableTrie.entrySet().remove(unmodifiableTrie.entrySet().iterator().next());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetIteratorRemove() {
        Iterator<Entry<String, String>> it = unmodifiableTrie.entrySet().iterator();
        it.next();
        it.remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetSetValue() {
        Entry<String, String> entry = unmodifiableTrie.entrySet().iterator().next();
        entry.setValue("newValue");
    }

    // ---------- Additional Trie-specific methods (if any) ----------

    @Test(expected = UnsupportedOperationException.class)
    public void testPrefixMapPut() {
        // prefixMap returns a SortedMap, which should also be unmodifiable
        unmodifiableTrie.prefixMap("key").put("newKey", "value");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPrefixMapClear() {
        unmodifiableTrie.prefixMap("key").clear();
    }

    @Test
    public void testPrefixMapRead() {
        // prefixMap should delegate read operations
        assertEquals("value1", unmodifiableTrie.prefixMap("key").get("key1"));
        assertTrue(unmodifiableTrie.prefixMap("key").containsKey("key1"));
        assertEquals(3, unmodifiableTrie.prefixMap("key").size());
    }

    // ---------- Edge cases: empty decorated trie ----------

    @Test
    public void testEmptyTrie() {
        Trie<String, String> empty = new PatriciaTrie<>();
        UnmodifiableTrie<String, String> emptyUnmod = new UnmodifiableTrie<>(empty);
        assertTrue(emptyUnmod.isEmpty());
        assertEquals(0, emptyUnmod.size());
        assertNull(emptyUnmod.get("anything"));
        assertFalse(emptyUnmod.containsKey("anything"));
        assertFalse(emptyUnmod.containsValue("anything"));
        assertTrue(emptyUnmod.keySet().isEmpty());
        assertTrue(emptyUnmod.values().isEmpty());
        assertTrue(emptyUnmod.entrySet().isEmpty());
    }

    // ---------- Verify that decorated trie is not modified after exception ----------

    @Test
    public void testDecoratedNotModifiedAfterPutThrows() {
        try {
            unmodifiableTrie.put("newKey", "newValue");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        assertFalse(decorated.containsKey("newKey"));
        assertEquals(3, decorated.size());
    }

    @Test
    public void testDecoratedNotModifiedAfterRemoveThrows() {
        try {
            unmodifiableTrie.remove("key1");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        assertTrue(decorated.containsKey("key1"));
        assertEquals(3, decorated.size());
    }

    // ---------- equals and hashCode (delegated) ----------

    @Test
    public void testEqualsAndHashCode() {
        Trie<String, String> other = new PatriciaTrie<>();
        other.put("key1", "value1");
        other.put("key2", "value2");
        other.put("key3", "value3");
        UnmodifiableTrie<String, String> otherUnmod = new UnmodifiableTrie<>(other);
        assertEquals(unmodifiableTrie, otherUnmod);
        assertEquals(unmodifiableTrie.hashCode(), otherUnmod.hashCode());
    }

    // ---------- toString (delegated) ----------

    @Test
    public void testToString() {
        String expected = decorated.toString();
        assertEquals(expected, unmodifiableTrie.toString());
    }
}