package org.apache.commons.collections.map;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class Flat3MapTest {

    @Test
    public void testEmptyMap() {
        Flat3Map map = new Flat3Map();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
        Assert.assertFalse(map.containsKey("key1"));
        Assert.assertFalse(map.containsValue("value1"));
        Assert.assertNull(map.get("key1"));
        Assert.assertNull(map.remove("key1"));

        Set<Object> keys = map.keySet();
        Assert.assertTrue(keys.isEmpty());

        Collection<Object> values = map.values();
        Assert.assertTrue(values.isEmpty());

        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        Assert.assertTrue(entries.isEmpty());
    }

    @Test
    public void testPutAndGetUpToThree() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.put("A", "1"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("1", map.get("A"));

        // Update existing key
        Assert.assertEquals("1", map.put("A", "1-updated"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("1-updated", map.get("A"));

        // Add second key
        Assert.assertNull(map.put("B", "2"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals("1-updated", map.get("A"));
        Assert.assertEquals("2", map.get("B"));

        // Add third key
        Assert.assertNull(map.put("C", "3"));
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("1-updated", map.get("A"));
        Assert.assertEquals("2", map.get("B"));
        Assert.assertEquals("3", map.get("C"));

        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("B"));
        Assert.assertTrue(map.containsKey("C"));

        Assert.assertTrue(map.containsValue("1-updated"));
        Assert.assertTrue(map.containsValue("2"));
        Assert.assertTrue(map.containsValue("3"));
    }

    @Test
    public void testExpansionBeyondThree() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        // This should trigger expansion to AbstractHashedMap / delegate mode
        map.put("D", "4");

        Assert.assertEquals(4, map.size());
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("2", map.get("B"));
        Assert.assertEquals("3", map.get("C"));
        Assert.assertEquals("4", map.get("D"));

        Assert.assertTrue(map.containsKey("A"));
        Assert.assertTrue(map.containsKey("D"));
        Assert.assertTrue(map.containsValue("4"));

        // Remove from expanded map
        Assert.assertEquals("D", map.remove("D"));
        Assert.assertEquals(3, map.size());
        Assert.assertNull(map.get("D"));
    }

    @Test
    public void testNullKeysAndValues() {
        Flat3Map map = new Flat3Map();
        Assert.assertNull(map.put(null, "null-key-val"));
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("null-key-val", map.get(null));
        Assert.assertTrue(map.containsKey(null));
        Assert.assertTrue(map.containsValue("null-key-val"));

        Assert.assertNull(map.put("K", null));
        Assert.assertEquals(2, map.size());
        Assert.assertNull(map.get("K"));
        Assert.assertTrue(map.containsKey("K"));
        Assert.assertTrue(map.containsValue(null));

        // Expand with nulls
        map.put("K2", "V2");
        map.put("K3", "V3");
        Assert.assertEquals(4, map.size());
        Assert.assertEquals("null-key-val", map.get(null));
        Assert.assertNull(map.get("K"));

        // Remove null key
        Assert.assertEquals("null-key-val", map.remove(null));
        Assert.assertEquals(3, map.size());
        Assert.assertFalse(map.containsKey(null));
    }

    @Test
    public void testPutAll() {
        Map<String, String> source = new HashMap<String, String>();
        source.put("X", "10");
        source.put("Y", "20");

        Flat3Map map = new Flat3Map();
        map.put("Z", "30");
        map.putAll(source);

        Assert.assertEquals(3, map.size());
        Assert.assertEquals("10", map.get("X"));
        Assert.assertEquals("20", map.get("Y"));
        Assert.assertEquals("30", map.get("Z"));

        // Test putAll with more than 3 elements forcing expansion
        Map<String, String> largeSource = new HashMap<String, String>();
        largeSource.put("1", "one");
        largeSource.put("2", "two");
        largeSource.put("3", "three");
        largeSource.put("4", "four");

        Flat3Map map2 = new Flat3Map();
        map2.putAll(largeSource);
        Assert.assertEquals(4, map2.size());
        Assert.assertEquals("one", map2.get("1"));
        Assert.assertEquals("four", map2.get("4"));
    }

    @Test
    public void testClear() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.clear();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
        Assert.assertNull(map.get("A"));

        // Clear expanded map
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4");
        map.clear();
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(0, map.size());
    }

    @Test
    public void testRemoveVariations() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        // Remove middle element in flat mode
        Assert.assertEquals("2", map.remove("B"));
        Assert.assertEquals(2, map.size());
        Assert.assertNull(map.get("B"));
        Assert.assertEquals("1", map.get("A"));
        Assert.assertEquals("3", map.get("C"));

        // Remove first element
        Assert.assertEquals("1", map.remove("A"));
        Assert.assertEquals(1, map.size());

        // Remove last remaining element
        Assert.assertEquals("3", map.remove("C"));
        Assert.assertEquals(0, map.size());
        Assert.assertTrue(map.isEmpty());

        // Remove non-existent
        Assert.assertNull(map.remove("X"));
    }

    @Test
    public void testClone() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");

        Flat3Map clone = (Flat3Map) map.clone();
        Assert.assertEquals(map.size(), clone.size());
        Assert.assertEquals(map.get("A"), clone.get("A"));
        Assert.assertEquals(map.get("B"), clone.get("B"));

        // Mutate original, ensure clone is independent
        map.put("A", "modified");
        Assert.assertEquals("1", clone.get("A"));

        // Test clone of expanded map
        map.put("C", "3");
        map.put("D", "4");
        Flat3Map cloneExpanded = (Flat3Map) map.clone();
        Assert.assertEquals(4, cloneExpanded.size());
        Assert.assertEquals("modified", cloneExpanded.get("A"));
        Assert.assertEquals("4", cloneExpanded.get("D"));
    }

    @Test
    public void testEqualsAndHashCode() {
        Flat3Map map1 = new Flat3Map();
        Flat3Map map2 = new Flat3Map();

        Assert.assertTrue(map1.equals(map2));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        map1.put("A", "1");
        map1.put("B", "2");
        Assert.assertFalse(map1.equals(map2));

        map2.put("B", "2");
        map2.put("A", "1");
        Assert.assertTrue(map1.equals(map2));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        // Compare with normal Map
        Map<String, String> normalMap = new HashMap<String, String>();
        normalMap.put("A", "1");
        normalMap.put("B", "2");
        Assert.assertTrue(map1.equals(normalMap));
        Assert.assertTrue(normalMap.equals(map1));

        // Expanded equals
        map1.put("C", "3");
        map1.put("D", "4");
        map2.put("C", "3");
        map2.put("D", "4");
        Assert.assertTrue(map1.equals(map2));
        Assert.assertEquals(map1.hashCode(), map2.hashCode());

        Assert.assertFalse(map1.equals(null));
        Assert.assertFalse(map1.equals("NotACloneOrMap"));
    }

    @Test
    public void testSerialization() throws Exception {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4"); // expanded

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Flat3Map deserialized = (Flat3Map) ois.readObject();
        ois.close();

        Assert.assertEquals(map.size(), deserialized.size());
        Assert.assertEquals(map.get("A"), deserialized.get("A"));
        Assert.assertEquals(map.get("D"), deserialized.get("D"));
        Assert.assertTrue(map.equals(deserialized));
    }

    @Test
    public void testKeySetOperations() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        Set<Object> keys = map.keySet();
        Assert.assertEquals(3, keys.size());
        Assert.assertTrue(keys.contains("A"));
        Assert.assertTrue(keys.contains("B"));
        Assert.assertTrue(keys.contains("C"));

        // Test iterator
        Iterator<Object> it = keys.iterator();
        int count = 0;
        while (it.hasNext()) {
            Object key = it.next();
            Assert.assertTrue(map.containsKey(key));
            count++;
        }
        Assert.assertEquals(3, count);

        // Test remove via iterator
        it = keys.iterator();
        if (it.hasNext()) {
            it.next();
            it.remove();
        }
        Assert.assertEquals(2, map.size());

        // Test clear via keySet
        keys.clear();
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testKeySetIteratorNoSuchElement() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        Set<Object> keys = map.keySet();
        Iterator<Object> it = keys.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("A", it.next());
        it.next(); // Should throw NoSuchElementException
    }

    @Test
    public void testValuesOperations() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        Collection<Object> values = map.values();
        Assert.assertEquals(3, values.size());
        Assert.assertTrue(values.contains("1"));
        Assert.assertTrue(values.contains("2"));
        Assert.assertTrue(values.contains("3"));

        Iterator<Object> it = values.iterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            count++;
        }
        Assert.assertEquals(3, count);

        // Remove via iterator
        it = values.iterator();
        if (it.hasNext()) {
            it.next();
            it.remove();
        }
        Assert.assertEquals(2, map.size());

        values.clear();
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testValuesIteratorNoSuchElement() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        Collection<Object> values = map.values();
        Iterator<Object> it = values.iterator();
        Assert.assertTrue(it.hasNext());
        Assert.assertEquals("1", it.next());
        it.next(); // Should throw
    }

    @Test
    public void testEntrySetOperations() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");

        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        Assert.assertEquals(3, entries.size());

        Iterator<Map.Entry<Object, Object>> it = entries.iterator();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            Assert.assertNotNull(entry.getKey());
            Assert.assertNotNull(entry.getValue());
            Assert.assertEquals(map.get(entry.getKey()), entry.getValue());
            
            // Test entry setValue
            if ("A".equals(entry.getKey())) {
                Object oldVal = entry.setValue("1-new");
                Assert.assertEquals("1", oldVal);
                Assert.assertEquals("1-new", map.get("A"));
            }
            count++;
        }
        Assert.assertEquals(3, count);

        // Test entry equals and hashCode
        Iterator<Map.Entry<Object, Object>> it2 = entries.iterator();
        if (it2.hasNext()) {
            Map.Entry<Object, Object> entry = it2.next();
            Assert.assertTrue(entry.equals(entry));
            Assert.assertFalse(entry.equals(null));
            Assert.assertFalse(entry.equals("NotAnEntry"));
            Assert.assertEquals(entry.hashCode(), entry.hashCode());
        }

        // Remove via iterator
        it = entries.iterator();
        if (it.hasNext()) {
            it.next();
            it.remove();
        }
        Assert.assertEquals(2, map.size());

        entries.clear();
        Assert.assertTrue(map.isEmpty());
    }

    @Test(expected = NoSuchElementException.class)
    public void testEntrySetIteratorNoSuchElement() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        Iterator<Map.Entry<Object, Object>> it = entries.iterator();
        Assert.assertTrue(it.hasNext());
        it.next();
        it.next(); // Should throw
    }

    @Test
    public void testExpandedEntrySetValue() {
        Flat3Map map = new Flat3Map();
        map.put("A", "1");
        map.put("B", "2");
        map.put("C", "3");
        map.put("D", "4"); // expanded

        Set<Map.Entry<Object, Object>> entries = map.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            if ("A".equals(entry.getKey())) {
                entry.setValue("100");
                Assert.assertEquals("100", map.get("A"));
            }
        }
    }
}