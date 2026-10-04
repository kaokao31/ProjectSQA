package org.apache.commons.collections4.map;

import static org.junit.Assert.*;

import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections4.OrderedMapIterator;
import org.junit.Before;
import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for ListOrderedMap.
 * Designed to achieve high coverage and detect the Defects4J bug #22
 * (inconsistency between map and list after iterator setValue).
 */
public class ListOrderedMapTest {

    private ListOrderedMap<String, String> map;

    @Before
    public void setUp() {
        map = new ListOrderedMap<>();
    }

    // ================== Basic put and consistency ==================

    @Test
    public void testPutNewEntry() {
        assertNull(map.put("A", "1"));
        assertEquals("1", map.get("A"));
        assertEquals("1", map.get(0));
        assertEquals(1, map.size());
    }

    @Test
    public void testPutOverwrite() {
        map.put("A", "1");
        assertEquals("1", map.put("A", "2"));
        assertEquals("2", map.get("A"));
        assertEquals("2", map.get(0));
        assertEquals(1, map.size());
    }

    @Test
    public void testPutMultipleEntries() {
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals(3, map.size());
        assertEquals("1", map.get(0));
        assertEquals("2", map.get(1));
        assertEquals("3", map.get(2));
        // Overwrite middle element
        map.put("B", "22");
        assertEquals("22", map.get(1));
        assertEquals(3, map.size());
    }

    @Test
    public void testPutNullKey() {
        assertNull(map.put(null, "nullValue"));
        assertEquals("nullValue", map.get(null));
        assertEquals("nullValue", map.get(0));
    }

    @Test
    public void testPutNullValue() {
        assertNull(map.put("A", null));
        assertNull(map.get("A"));
        assertNull(map.get(0));
    }

    @Test
    public void testPutNullKeyAndValue() {
        map.put(null, null);
        assertNull(map.get(null));
        assertNull(map.get(0));
    }

    // ================== putAll ==================

    @Test
    public void testPutAllEmpty() {
        map.put("A", "1");
        map.putAll(new java.util.HashMap<>());
        assertEquals(1, map.size());
        assertEquals("1", map.get(0));
    }

    @Test
    public void testPutAllNewEntries() {
        Map<String, String> src = new java.util.HashMap<>();
        src.put("B", "2");
        src.put("C", "3");
        map.put("A", "1");
        map.putAll(src);
        assertEquals(3, map.size());
        assertEquals("1", map.get(0));
        assertEquals("2", map.get(1));
        assertEquals("3", map.get(2));
    }

    @Test
    public void testPutAllOverwrite() {
        map.put("A", "1");
        map.put("B", "2");
        Map<String, String> src = new java.util.HashMap<>();
        src.put("B", "22");
        src.put("C", "3");
        map.putAll(src);
        assertEquals(3, map.size());
        assertEquals("1", map.get(0));
        assertEquals("22", map.get(1));
        assertEquals("3", map.get(2));
    }

    @Test
    public void testPutAllWithNullKey() {
        map.put("A", "1");
        Map<String, String> src = new java.util.HashMap<>();
        src.put(null, "nullVal");
        map.putAll(src);
        assertEquals(2, map.size());
        assertEquals("nullVal", map.get(null));
        assertEquals(1, map.indexOf(null));
    }

    // ================== get and indexOf ==================

    @Test
    public void testGetByKey() {
        map.put("A", "1");
        assertEquals("1", map.get("A"));
        assertNull(map.get("B"));
    }

    @Test
    public void testGetByIndex() {
        map.put("A", "1");
        map.put("B", "2");
        assertEquals("1", map.get(0));
        assertEquals("2", map.get(1));
        try {
            map.get(2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testIndexOf() {
        map.put("A", "1");
        map.put("B", "2");
        assertEquals(0, map.indexOf("A"));
        assertEquals(1, map.indexOf("B"));
        assertEquals(-1, map.indexOf("C"));
        assertEquals(-1, map.indexOf(null));
        map.put(null, "null");
        assertEquals(2, map.indexOf(null));
    }

    // ================== remove ==================

    @Test
    public void testRemoveByKey() {
        map.put("A", "1");
        map.put("B", "2");
        assertEquals("1", map.remove("A"));
        assertEquals(1, map.size());
        assertEquals("2", map.get(0));
        assertNull(map.remove("A"));
    }

    @Test
    public void testRemoveByIndex() {
        map.put("A", "1");
        map.put("B", "2");
        assertEquals("1", map.remove(0));
        assertEquals(1, map.size());
        assertEquals("2", map.get(0));
    }

    @Test
    public void testRemoveNullKey() {
        map.put(null, "null");
        assertEquals("null", map.remove(null));
        assertTrue(map.isEmpty());
    }

    // ================== containsKey / containsValue ==================

    @Test
    public void testContainsKey() {
        map.put("A", "1");
        assertTrue(map.containsKey("A"));
        assertFalse(map.containsKey("B"));
        assertFalse(map.containsKey(null));
        map.put(null, "null");
        assertTrue(map.containsKey(null));
    }

    @Test
    public void testContainsValue() {
        map.put("A", "1");
        assertTrue(map.containsValue("1"));
        assertFalse(map.containsValue("2"));
    }

    // ================== clear and isEmpty ==================

    @Test
    public void testClear() {
        map.put("A", "1");
        map.put("B", "2");
        map.clear();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        try {
            map.get(0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // ================== Iterator setValue (spot bug #22) ==================

    @Test
    public void testMapIteratorSetValue() {
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        OrderedMapIterator<String, String> it = map.mapIterator();
        while (it.hasNext()) {
            it.next();
            if ("B".equals(it.getKey())) {
                it.setValue("22");
            }
        }
        // Verify map consistency
        assertEquals("22", map.get("B"));
        // Verify list consistency
        assertEquals("22", map.get(1));
        // Re-iterate through mapIterator to confirm
        OrderedMapIterator<String, String> it2 = map.mapIterator();
        while (it2.hasNext()) {
            it2.next();
            if ("B".equals(it2.getKey())) {
                assertEquals("22", it2.getValue());
            }
        }
    }

    @Test
    public void testMapIteratorSetValueFirstEntry() {
        map.put("A", "1");
        map.put("B", "2");
        OrderedMapIterator<String, String> it = map.mapIterator();
        it.next(); // A
        it.setValue("11");
        assertEquals("11", map.get("A"));
        assertEquals("11", map.get(0));
    }

    @Test
    public void testMapIteratorSetValueLastEntry() {
        map.put("A", "1");
        map.put("B", "2");
        OrderedMapIterator<String, String> it = map.mapIterator();
        it.next(); // A
        it.next(); // B
        it.setValue("22");
        assertEquals("22", map.get("B"));
        assertEquals("22", map.get(1));
    }

    @Test
    public void testMapIteratorSetValueNullKey() {
        map.put(null, "null");
        map.put("A", "1");
        OrderedMapIterator<String, String> it = map.mapIterator();
        it.next(); // null
        it.setValue("nullNew");
        assertEquals("nullNew", map.get(null));
        assertEquals("nullNew", map.get(0));
    }

    @Test
    public void testListIteratorSetValue() {
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        ListIterator<String> lit = map.listIterator();
        while (lit.hasNext()) {
            String val = lit.next();
            if ("B".equals(val)) {
                lit.set("22");
            }
        }
        // Verify map consistency
        assertEquals("22", map.get("B"));
        // Verify list consistency (by index)
        assertEquals("22", map.get(1));
        // Verify through listIterator again
        ListIterator<String> lit2 = map.listIterator();
        while (lit2.hasNext()) {
            String val = lit2.next();
            if (lit2.previousIndex() == 1) {
                assertEquals("22", val);
            }
        }
    }

    @Test
    public void testListIteratorSetValueFirstEntry() {
        map.put("A", "1");
        map.put("B", "2");
        ListIterator<String> lit = map.listIterator();
        lit.next(); // "1"
        lit.set("11");
        assertEquals("11", map.get("A"));
        assertEquals("11", map.get(0));
    }

    @Test
    public void testListIteratorSetValueLastEntry() {
        map.put("A", "1");
        map.put("B", "2");
        ListIterator<String> lit = map.listIterator();
        lit.next(); // "1"
        lit.next(); // "2"
        lit.set("22");
        assertEquals("22", map.get("B"));
        assertEquals("22", map.get(1));
    }

    // ================== listIterator (ListOrderedMap specific) ==================

    @Test
    public void testListIteratorByIndex() {
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        ListIterator<String> lit = map.listIterator(1);
        assertTrue(lit.hasNext());
        assertEquals("2", lit.next());
        assertFalse(lit.hasPrevious());
        // previous should now be "2"
        assertEquals("2", lit.previous());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testListIteratorByIndexOutOfBounds() {
        map.put("A", "1");
        map.listIterator(2);
    }

    // ================== subList (ListOrderedMap.view) ==================

    @Test
    public void testSubList() {
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        java.util.List<String> sub = map.asList().subList(1, 3);
        assertEquals(2, sub.size());
        assertEquals("2", sub.get(0));
        assertEquals("3", sub.get(1));
        // Modification through sublist should reflect in map
        sub.set(0, "22");
        assertEquals("22", map.get("B"));
        assertEquals("22", map.get(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSubListInvalid() {
        map.put("A", "1");
        map.asList().subList(0, 2);
    }

    // ================== equals and hashCode ==================

    @Test
    public void testEqualsAndHashCode() {
        ListOrderedMap<String, String> map2 = new ListOrderedMap<>();
        map.put("A", "1");
        map.put("B", "2");
        map2.put("A", "1");
        map2.put("B", "2");
        assertTrue(map.equals(map2));
        assertTrue(map2.equals(map));
        assertEquals(map.hashCode(), map2.hashCode());

        map2.put("C", "3");
        assertFalse(map.equals(map2));
    }

    // ================== toString ==================

    @Test
    public void testToString() {
        map.put("A", "1");
        map.put("B", "2");
        String str = map.toString();
        assertTrue(str.contains("A") && str.contains("1"));
        assertTrue(str.contains("B") && str.contains("2"));
    }

    // ================== clone and serialization (if applicable) ==================

    @Test
    @SuppressWarnings("unchecked")
    public void testClone() {
        map.put("A", "1");
        map.put("B", "2");
        ListOrderedMap<String, String> cloned = (ListOrderedMap<String, String>) map.clone();
        assertEquals(map, cloned);
        cloned.put("C", "3");
        assertFalse(map.equals(cloned));
        assertEquals(2, map.size());
    }

    // ================== static factory listOrderedMap ==================

    @Test
    public void testListOrderedMapFactory() {
        Map<String, String> base = new java.util.HashMap<>();
        base.put("A", "1");
        base.put("B", "2");
        ListOrderedMap<String, String> map2 = ListOrderedMap.listOrderedMap(base);
        assertEquals(2, map2.size());
        assertEquals("1", map2.get("A"));
        assertEquals("2", map2.get("B"));
        // Ensure insertion order is as expected (HashMap order is not guaranteed, but this is ok)
    }

    @Test(expected = NullPointerException.class)
    public void testListOrderedMapFactoryNull() {
        ListOrderedMap.listOrderedMap(null);
    }

    // ================== edge: put and remove with duplicate keys ==================

    @Test
    public void testPutDuplicateKeysMaintainsOrder() {
        map.put("A", "1");
        map.put("B", "2");
        map.put("A", "11"); // overwrite, but order should remain same
        assertEquals(2, map.size());
        assertEquals("11", map.get(0));
        assertEquals("2", map.get(1));
    }

    @Test
    public void testRemoveAndReinsert() {
        map.put("A", "1");
        map.put("B", "2");
        map.remove("A");
        map.put("A", "11");
        assertEquals(2, map.size());
        assertEquals("2", map.get(0));
        assertEquals("11", map.get(1));
    }

    // ================== bulk operations ==================

    @Test
    public void testKeySetAndValues() {
        map.put("A", "1");
        map.put("B", "2");
        assertTrue(map.keySet().contains("A"));
        assertTrue(map.values().contains("1"));
        assertEquals(2, map.keySet().size());
        assertEquals(2, map.values().size());
    }

    @Test
    public void testEntrySet() {
        map.put("A", "1");
        map.put("B", "2");
        assertEquals(2, map.entrySet().size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            assertNotNull(e.getKey());
            assertNotNull(e.getValue());
        }
    }

    @Test
    public void testEntrySetSetValue() {
        map.put("A", "1");
        map.put("B", "2");
        Map.Entry<String, String> entry = map.entrySet().iterator().next();
        entry.setValue("11");
        assertEquals("11", map.get(0));
        assertEquals("11", map.get("A"));
    }

    // ================== OrderedMap specific methods ==================

    @Test
    public void testFirstKeyLastKey() {
        map.put("A", "1");
        map.put("B", "2");
        assertEquals("A", map.firstKey());
        assertEquals("B", map.lastKey());
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testFirstKeyEmpty() {
        map.firstKey();
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void testLastKeyEmpty() {
        map.lastKey();
    }

    @Test
    public void testNextKeyPreviousKey() {
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        assertEquals("B", map.nextKey("A"));
        assertEquals("C", map.nextKey("B"));
        assertNull(map.nextKey("C"));
        assertNull(map.previousKey("A"));
        assertEquals("A", map.previousKey("B"));
        assertEquals("B", map.previousKey("C"));
    }

    // ================== additional coverage: asList() ==================

    @Test
    public void testAsList() {
        map.put("A", "1");
        map.put("B", "2");
        java.util.List<String> list = map.asList();
        assertEquals(2, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        // Modification through list should reflect in map
        list.set(0, "11");
        assertEquals("11", map.get("A"));
    }

    // ================== stress test for bug with many entries ==================

    @Test
    public void testMultipleSetValueThroughMapIterator() {
        for (int i = 0; i < 100; i++) {
            map.put("key" + i, "value" + i);
        }
        OrderedMapIterator<String, String> it = map.mapIterator();
        while (it.hasNext()) {
            it.next();
            if (it.getKey().equals("key50")) {
                it.setValue("newValue50");
            }
        }
        assertEquals("newValue50", map.get("key50"));
        assertEquals("newValue50", map.get(50));
    }

    @Test
    public void testMultipleSetValueThroughListIterator() {
        for (int i = 0; i < 100; i++) {
            map.put("key" + i, "value" + i);
        }
        ListIterator<String> lit = map.listIterator();
        while (lit.hasNext()) {
            String val = lit.next();
            if (val.equals("value50")) {
                lit.set("newValue50");
            }
        }
        assertEquals("newValue50", map.get("key50"));
        assertEquals("newValue50", map.get(50));
    }
}