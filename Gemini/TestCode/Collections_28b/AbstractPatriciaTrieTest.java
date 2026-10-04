package org.apache.commons.collections4.trie;

import org.junit.Test;
import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class AbstractPatriciaTrieTest {

    private static class ConcretePatriciaTrie<K, V> extends AbstractPatriciaTrie<K, V> {
        private static final long serialVersionUID = 1L;

        public ConcretePatriciaTrie(KeyAnalyzer<? super K> keyAnalyzer) {
            super(keyAnalyzer, new TreeMap<>());
        }

        @Override
        public V put(K key, V value) {
            if (key == null) {
                throw new NullPointerException("Key cannot be null");
            }
            return super.put(key, value);
        }
    }

    private static class StringKeyAnalyzer implements KeyAnalyzer<String> {
        private static final long serialVersionUID = 1L;

        @Override
        public int bitsPerElement() {
            return 16;
        }

        @Override
        public boolean isBitSet(String key, int bitIndex) {
            if (key == null) {
                return false;
            }
            int index = bitIndex / 16;
            if (index >= key.length()) {
                return false;
            }
            int bit = bitIndex % 16;
            char c = key.charAt(index);
            return ((c >> (15 - bit)) & 1) != 0;
        }

        @Override
        public int bitIndex(String key, String otherKey) {
            if (key.equals(otherKey)) {
                return KeyAnalyzer.EQUAL_BIT_KEY;
            }
            int maxLength = Math.max(key.length(), otherKey.length());
            for (int i = 0; i < maxLength * 16; i++) {
                if (isBitSet(key, i) != isBitSet(otherKey, i)) {
                    return i;
                }
            }
            return KeyAnalyzer.NULL_BIT_KEY;
        }

        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

    @Test
    public void testBasicPutAndGet() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        assertNull(trie.put("A", "Apple"));
        assertEquals("Apple", trie.get("A"));
        assertEquals("Apple", trie.put("A", "Avocado"));
        assertEquals("Avocado", trie.get("A"));
        assertEquals(1, trie.size());
    }

    @Test
    public void testPrefixMapEdgeCases() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        trie.put("Test", "1");
        trie.put("Testing", "2");
        trie.put("Tensor", "3");

        SortedMap<String, String> prefixMap = trie.prefixMap("Test");
        assertEquals(2, prefixMap.size());
        assertTrue(prefixMap.containsKey("Test"));
        assertTrue(prefixMap.containsKey("Testing"));
        assertFalse(prefixMap.containsKey("Tensor"));
    }

    @Test
    public void testSubMapOperations() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        trie.put("Apple", "1");
        trie.put("Banana", "2");
        trie.put("Cherry", "3");

        SortedMap<String, String> subMap = trie.subMap("Apple", "Cherry");
        assertEquals(2, subMap.size());
        assertTrue(subMap.containsKey("Apple"));
        assertTrue(subMap.containsKey("Banana"));
        assertFalse(subMap.containsKey("Cherry"));
    }

    @Test
    public void testHeadMapOperations() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        trie.put("Apple", "1");
        trie.put("Banana", "2");
        trie.put("Cherry", "3");

        SortedMap<String, String> headMap = trie.headMap("Cherry");
        assertEquals(2, headMap.size());
        assertTrue(headMap.containsKey("Apple"));
        assertTrue(headMap.containsKey("Banana"));
    }

    @Test
    public void testTailMapOperations() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        trie.put("Apple", "1");
        trie.put("Banana", "2");
        trie.put("Cherry", "3");

        SortedMap<String, String> tailMap = trie.tailMap("Banana");
        assertEquals(2, tailMap.size());
        assertTrue(tailMap.containsKey("Banana"));
        assertTrue(tailMap.containsKey("Cherry"));
    }

    @Test
    public void testClearAndIsEmpty() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        assertTrue(trie.isEmpty());
        trie.put("A", "1");
        assertFalse(trie.isEmpty());
        trie.clear();
        assertTrue(trie.isEmpty());
        assertEquals(0, trie.size());
    }

    @Test(expected = NullPointerException.class)
    public void testNullKeyThrowsException() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        trie.put(null, "Value");
    }

    @Test
    public void testComparator() {
        ConcretePatriciaTrie<String, String> trie = new ConcretePatriciaTrie<>(new StringKeyAnalyzer());
        Comparator<? super String> comparator = trie.comparator();
        assertNotNull(comparator);
    }
}