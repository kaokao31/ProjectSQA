package org.apache.commons.collections4.trie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import org.junit.Before;
import org.junit.Test;

public class UnmodifiableTrieTest {

    private Trie<String, String> originalTrie;
    private Trie<String, String> unmodifiableTrie;

    @Before
    public void setUp() {
        originalTrie = new PatriciaTrie<String, String>();
        originalTrie.put("A", "Apple");
        originalTrie.put("B", "Banana");
        unmodifiableTrie = UnmodifiableTrie.unmodifiableTrie(originalTrie);
    }

    @Test
    public void testUnmodifiableTrieCreation() {
        assertNotNull(unmodifiableTrie);
        // If passed an already unmodifiable trie, it should return it as is or handle it
        Trie<String, String> wrappedAgain = UnmodifiableTrie.unmodifiableTrie(unmodifiableTrie);
        assertNotNull(wrappedAgain);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() {
        unmodifiableTrie.clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPut() {
        unmodifiableTrie.put("C", "Cherry");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testPutAll() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("C", "Cherry");
        unmodifiableTrie.putAll(map);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        unmodifiableTrie.remove("A");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testEntrySetRemove() {
        unmodifiableTrie.entrySet().iterator().remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetRemove() {
        unmodifiableTrie.keySet().iterator().remove();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValuesRemove() {
        unmodifiableTrie.values().iterator().remove();
    }

    @Test
    public void testReadOperations() {
        assertEquals("Apple", unmodifiableTrie.get("A"));
        assertTrue(unmodifiableTrie.containsKey("A"));
        assertTrue(unmodifiableTrie.containsValue("Banana"));
        assertFalse(unmodifiableTrie.isEmpty());
        assertEquals(2, unmodifiableTrie.size());
    }

    @Test
    public void testPrefixMap() {
        SortedMap<String, String> prefixMap = unmodifiableTrie.prefixMap("A");
        assertNotNull(prefixMap);
        assertEquals(1, prefixMap.size());
        
        // Verify prefixMap is also unmodifiable
        boolean exceptionThrown = false;
        try {
            prefixMap.clear();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue("PrefixMap should be unmodifiable", exceptionThrown);
    }

    @Test
    public void testHeadMap() {
        SortedMap<String, String> headMap = unmodifiableTrie.headMap("B");
        assertNotNull(headMap);
        
        boolean exceptionThrown = false;
        try {
            headMap.put("C", "Citrus");
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue("HeadMap should be unmodifiable", exceptionThrown);
    }

    @Test
    public void testTailMap() {
        SortedMap<String, String> tailMap = unmodifiableTrie.tailMap("A");
        assertNotNull(tailMap);
        
        boolean exceptionThrown = false;
        try {
            tailMap.clear();
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue("TailMap should be unmodifiable", exceptionThrown);
    }

    @Test
    public void testSubMap() {
        SortedMap<String, String> subMap = unmodifiableTrie.subMap("A", "B");
        assertNotNull(subMap);
        
        boolean exceptionThrown = false;
        try {
            subMap.remove("A");
        } catch (UnsupportedOperationException e) {
            exceptionThrown = true;
        }
        assertTrue("SubMap should be unmodifiable", exceptionThrown);
    }
}