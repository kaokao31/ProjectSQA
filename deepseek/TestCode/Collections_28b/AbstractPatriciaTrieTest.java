package org.apache.commons.collections4.trie;

import org.junit.Before;
import org.junit.Test;
import java.util.SortedMap;
import static org.junit.Assert.*;

public class AbstractPatriciaTrieTest {

    private PatriciaTrie<String, String> trie;

    @Before
    public void setUp() {
        trie = new PatriciaTrie<>();
        trie.put("a", "value1");
        trie.put("ab", "value2");
        trie.put("abc", "value3");
        trie.put("b", "value4");
        trie.put("bc", "value5");
        trie.put("bcd", "value6");
        trie.put("c", "value7");
    }

    // ========== Basic Operations ==========

    @Test
    public void testPutAndGet() {
        assertEquals("value1", trie.get("a"));
        assertEquals("value2", trie.get("ab"));
        assertEquals("value3", trie.get("abc"));
        assertEquals("value4", trie.get("b"));
        assertEquals("value5", trie.get("bc"));
        assertEquals("value6", trie.get("bcd"));
        assertEquals("value7", trie.get("c"));
        assertNull(trie.get("nonexistent"));
    }

    @Test
    public void testPutDuplicateKey() {
        String old = trie.put("a", "newValue");
        assertEquals("value1", old);
        assertEquals("newValue", trie.get("a"));
    }

    @Test
    public void testContainsKey() {
        assertTrue(trie.containsKey("a"));
        assertTrue(trie.containsKey("abc"));
        assertFalse(trie.containsKey("abcd"));
        assertFalse(trie.containsKey(""));
    }

    @Test
    public void testContainsValue() {
        assertTrue(trie.containsValue("value1"));
        assertTrue(trie.containsValue("value7"));
        assertFalse(trie.containsValue("nonexistent"));
    }

    @Test
    public void testSizeAndIsEmpty() {
        assertEquals(7, trie.size());
        assertFalse(trie.isEmpty());
        trie.clear();
        assertEquals(0, trie.size());
        assertTrue(trie.isEmpty());
    }

    @Test
    public void testRemove() {
        assertEquals("value1", trie.remove("a"));
        assertNull(trie.get("a"));
        assertEquals(6, trie.size());
        assertNull(trie.remove("nonexistent"));
        assertEquals(6, trie.size());
    }

    @Test
    public void testClear() {
        trie.clear();
        assertTrue(trie.isEmpty());
        assertEquals(0, trie.size());
        assertNull(trie.get("a"));
    }

    // ========== PrefixMap Tests ==========

    @Test
    public void testPrefixMapWithExistingPrefix() {
        SortedMap<String, String> prefixMap = trie.prefixMap("ab");
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("ab"));
        assertTrue(prefixMap.containsKey("abc"));
        assertFalse(prefixMap.containsKey("a"));
        assertFalse(prefixMap.containsKey("b"));
    }

    @Test
    public void testPrefixMapWithNonExistingPrefix() {
        // Prefix "bcd" exists as a key, but "bcde" does not
        SortedMap<String, String> prefixMap = trie.prefixMap("bcde");
        assertTrue(prefixMap.isEmpty());
    }

    @Test
    public void testPrefixMapWithPrefixThatIsNotAKey() {
        // "bce" is not a key, but "bc" is a prefix of "bcd"
        SortedMap<String, String> prefixMap = trie.prefixMap("bc");
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("bc"));
        assertTrue(prefixMap.containsKey("bcd"));
        assertFalse(prefixMap.containsKey("b"));
    }

    @Test
    public void testPrefixMapWithEmptyPrefix() {
        SortedMap<String, String> prefixMap = trie.prefixMap("");
        assertEquals(7, prefixMap.size());
        assertTrue(prefixMap.containsKey("a"));
        assertTrue(prefixMap.containsKey("c"));
    }

    @Test
    public void testPrefixMapWithFullKeyPrefix() {
        SortedMap<String, String> prefixMap = trie.prefixMap("abc");
        assertEquals(1, prefixMap.size());
        assertTrue(prefixMap.containsKey("abc"));
    }

    @Test(expected = NullPointerException.class)
    public void testPrefixMapWithNullPrefix() {
        trie.prefixMap(null);
    }

    // ========== First/Last Key Tests ==========

    @Test
    public void testFirstKey() {
        assertEquals("a", trie.firstKey());
    }

    @Test
    public void testLastKey() {
        assertEquals("c", trie.lastKey());
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testFirstKeyOnEmptyTrie() {
        PatriciaTrie<String, String> empty = new PatriciaTrie<>();
        empty.firstKey();
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testLastKeyOnEmptyTrie() {
        PatriciaTrie<String, String> empty = new PatriciaTrie<>();
        empty.lastKey();
    }

    // ========== Next/Previous Key Tests ==========

    @Test
    public void testNextKey() {
        assertEquals("ab", trie.nextKey("a"));
        assertEquals("abc", trie.nextKey("ab"));
        assertEquals("b", trie.nextKey("abc"));
        assertEquals("bc", trie.nextKey("b"));
        assertEquals("bcd", trie.nextKey("bc"));
        assertEquals("c", trie.nextKey("bcd"));
        assertNull(trie.nextKey("c"));
    }

    @Test
    public void testPreviousKey() {
        assertEquals("bcd", trie.previousKey("c"));
        assertEquals("bc", trie.previousKey("bcd"));
        assertEquals("b", trie.previousKey("bc"));
        assertEquals("abc", trie.previousKey("b"));
        assertEquals("ab", trie.previousKey("abc"));
        assertEquals("a", trie.previousKey("ab"));
        assertNull(trie.previousKey("a"));
    }

    @Test
    public void testNextKeyNonExistent() {
        assertNull(trie.nextKey("nonexistent"));
    }

    @Test
    public void testPreviousKeyNonExistent() {
        assertNull(trie.previousKey("nonexistent"));
    }

    // ========== Edge Cases ==========

    @Test
    public void testPutAndGetWithEmptyStringKey() {
        PatriciaTrie<String, String> emptyKeyTrie = new PatriciaTrie<>();
        emptyKeyTrie.put("", "empty");
        assertEquals("empty", emptyKeyTrie.get(""));
        assertTrue(emptyKeyTrie.containsKey(""));
    }

    @Test
    public void testPrefixMapWithPrefixThatMatchesAll() {
        SortedMap<String, String> prefixMap = trie.prefixMap("a");
        assertEquals(3, prefixMap.size());
        assertTrue(prefixMap.containsKey("a"));
        assertTrue(prefixMap.containsKey("ab"));
        assertTrue(prefixMap.containsKey("abc"));
    }

    @Test
    public void testPrefixMapWithPrefixThatMatchesNone() {
        SortedMap<String, String> prefixMap = trie.prefixMap("z");
        assertTrue(prefixMap.isEmpty());
    }

    @Test
    public void testRemoveAndPrefixMap() {
        trie.remove("ab");
        SortedMap<String, String> prefixMap = trie.prefixMap("a");
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("a"));
        assertTrue(prefixMap.containsKey("abc"));
        assertFalse(prefixMap.containsKey("ab"));
    }

    @Test
    public void testLargeNumberOfEntries() {
        PatriciaTrie<Integer, String> intTrie = new PatriciaTrie<>();
        for (int i = 0; i < 1000; i++) {
            intTrie.put(i, "val" + i);
        }
        assertEquals(1000, intTrie.size());
        assertEquals("val0", intTrie.get(0));
        assertEquals("val999", intTrie.get(999));
        assertNull(intTrie.get(1000));
    }

    @Test
    public void testSubMap() {
        SortedMap<String, String> subMap = trie.subMap("ab", "b");
        assertEquals(2, subMap.size());
        assertTrue(subMap.containsKey("ab"));
        assertTrue(subMap.containsKey("abc"));
        assertFalse(subMap.containsKey("b"));
    }

    @Test
    public void testHeadMap() {
        SortedMap<String, String> headMap = trie.headMap("b");
        assertEquals(3, headMap.size());
        assertTrue(headMap.containsKey("a"));
        assertTrue(headMap.containsKey("ab"));
        assertTrue(headMap.containsKey("abc"));
        assertFalse(headMap.containsKey("b"));
    }

    @Test
    public void testTailMap() {
        SortedMap<String, String> tailMap = trie.tailMap("b");
        assertEquals(4, tailMap.size());
        assertTrue(tailMap.containsKey("b"));
        assertTrue(tailMap.containsKey("bc"));
        assertTrue(tailMap.containsKey("bcd"));
        assertTrue(tailMap.containsKey("c"));
        assertFalse(tailMap.containsKey("a"));
    }

    @Test(expected = NullPointerException.class)
    public void testPutNullKey() {
        trie.put(null, "nullKey");
    }

    @Test(expected = NullPointerException.class)
    public void testGetNullKey() {
        trie.get(null);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveNullKey() {
        trie.remove(null);
    }

    @Test(expected = NullPointerException.class)
    public void testContainsKeyNull() {
        trie.containsKey(null);
    }

    @Test
    public void testContainsValueNull() {
        trie.put("nullVal", null);
        assertTrue(trie.containsValue(null));
    }
}