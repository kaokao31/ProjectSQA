package org.apache.commons.collections4.map;

import org.apache.commons.collections4.MapIterator;
import org.junit.Test;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class Flat3MapTest {

    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";
    private static final String KEY4 = "key4";

    private static final String VAL1 = "val1";
    private static final String VAL2 = "val2";
    private static final String VAL3 = "val3";
    private static final String VAL4 = "val4";

    @Test
    public void testDefaultConstructor() {
        Flat3Map<String, String> map = new Flat3Map<>();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get(KEY1));
        assertFalse(map.containsKey(KEY1));
        assertFalse(map.containsValue(VAL1));
    }

    @Test
    public void testConstructorWithMap() {
        Map<String, String> init = new HashMap<>();
        init.put(KEY1, VAL1);
        init.put(KEY2, VAL2);

        Flat3Map<String, String> map = new Flat3Map<>(init);
        assertEquals(2, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL2, map.get(KEY2));

        init.put(KEY3, VAL3);
        init.put(KEY4, VAL4);
        Flat3Map<String, String> mapDelegated = new Flat3Map<>(init);
        assertEquals(4, mapDelegated.size());
        assertEquals(VAL4, mapDelegated.get(KEY4));
    }

    @Test
    public void testPutAndGetTransitions() {
        Flat3Map<String, String> map = new Flat3Map<>();

        // Size 0 -> 1
        assertNull(map.put(KEY1, VAL1));
        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        assertEquals(VAL1, map.get(KEY1));
        assertTrue(map.containsKey(KEY1));
        assertTrue(map.containsValue(VAL1));

        // Update 1
        assertEquals(VAL1, map.put(KEY1, "val1_updated"));
        assertEquals(1, map.size());
        assertEquals("val1_updated", map.get(KEY1));
        map.put(KEY1, VAL1);

        // Size 1 -> 2
        assertNull(map.put(KEY2, VAL2));
        assertEquals(2, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL2, map.get(KEY2));
        assertTrue(map.containsKey(KEY2));
        assertTrue(map.containsValue(VAL2));

        // Update 2
        assertEquals(VAL2, map.put(KEY2, "val2_updated"));
        assertEquals(2, map.size());
        assertEquals("val2_updated", map.get(KEY2));
        map.put(KEY2, VAL2);

        // Size 2 -> 3
        assertNull(map.put(KEY3, VAL3));
        assertEquals(3, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL2, map.get(KEY2));
        assertEquals(VAL3, map.get(KEY3));
        assertTrue(map.containsKey(KEY3));
        assertTrue(map.containsValue(VAL3));

        // Update 3
        assertEquals(VAL3, map.put(KEY3, "val3_updated"));
        assertEquals(3, map.size());
        assertEquals("val3_updated", map.get(KEY3));
        map.put(KEY3, VAL3);

        // Size 3 -> 4 (Triggers delegation to HashedMap)
        assertNull(map.put(KEY4, VAL4));
        assertEquals(4, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL2, map.get(KEY2));
        assertEquals(VAL3, map.get(KEY3));
        assertEquals(VAL4, map.get(KEY4));
        assertTrue(map.containsKey(KEY4));
        assertTrue(map.containsValue(VAL4));

        // Update in delegated mode
        assertEquals(VAL4, map.put(KEY4, "val4_updated"));
        assertEquals(4, map.size());
        assertEquals("val4_updated", map.get(KEY4));
    }

    @Test
    public void testNullKeysAndValues() {
        Flat3Map<String, String> map = new Flat3Map<>();

        // Null key at position 1
        assertNull(map.put(null, VAL1));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertEquals(VAL1, map.get(null));

        // Null value
        assertEquals(VAL1, map.put(null, null));
        assertEquals(1, map.size());
        assertTrue(map.containsKey(null));
        assertTrue(map.containsValue(null));
        assertNull(map.get(null));

        // Add 2nd entry
        map.put(KEY2, null);
        assertEquals(2, map.size());
        assertTrue(map.containsKey(KEY2));
        assertTrue(map.containsValue(null));
        assertNull(map.get(KEY2));

        // Add 3rd entry
        map.put(KEY3, VAL3);
        assertEquals(3, map.size());
        assertTrue(map.containsKey(null));
        assertTrue(map.containsKey(KEY2));
        assertTrue(map.containsKey(KEY3));
        assertTrue(map.containsValue(VAL3));
        assertTrue(map.containsValue(null));

        // Delegate with nulls
        map.put(KEY4, VAL4);
        assertEquals(4, map.size());
        assertTrue(map.containsKey(null));
        assertNull(map.get(null));
        assertTrue(map.containsValue(null));
    }

    @Test
    public void testHashCollisionKeys() {
        class HashCollisionKey {
            private final String name;

            HashCollisionKey(String name) {
                this.name = name;
            }

            @Override
            public int hashCode() {
                return 42; // All instances share the same hash code
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (obj == null || getClass() != obj.getClass()) return false;
                HashCollisionKey other = (HashCollisionKey) obj;
                return name != null ? name.equals(other.name) : other.name == null;
            }
        }

        Flat3Map<HashCollisionKey, String> map = new Flat3Map<>();
        HashCollisionKey k1 = new HashCollisionKey("1");
        HashCollisionKey k2 = new HashCollisionKey("2");
        HashCollisionKey k3 = new HashCollisionKey("3");
        HashCollisionKey k4 = new HashCollisionKey("4");

        map.put(k1, "V1");
        map.put(k2, "V2");
        map.put(k3, "V3");

        assertEquals(3, map.size());
        assertEquals("V1", map.get(k1));
        assertEquals("V2", map.get(k2));
        assertEquals("V3", map.get(k3));
        assertTrue(map.containsKey(k2));

        map.put(k4, "V4");
        assertEquals(4, map.size());
        assertEquals("V1", map.get(k1));
        assertEquals("V4", map.get(k4));
    }

    @Test
    public void testRemoveFromDifferentPositions() {
        // Remove from size 1
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        assertEquals(VAL1, map.remove(KEY1));
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertNull(map.get(KEY1));
        assertNull(map.remove(KEY1));

        // Remove position 1 from size 2 (shifts position 2 to position 1)
        map.clear();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        assertEquals(VAL1, map.remove(KEY1));
        assertEquals(1, map.size());
        assertEquals(VAL2, map.get(KEY2));
        assertFalse(map.containsKey(KEY1));

        // Remove position 2 from size 2
        map.clear();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        assertEquals(VAL2, map.remove(KEY2));
        assertEquals(1, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertFalse(map.containsKey(KEY2));

        // Remove position 1 from size 3
        map.clear();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);
        assertEquals(VAL1, map.remove(KEY1));
        assertEquals(2, map.size());
        assertEquals(VAL2, map.get(KEY2));
        assertEquals(VAL3, map.get(KEY3));
        assertFalse(map.containsKey(KEY1));

        // Remove position 2 from size 3
        map.clear();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);
        assertEquals(VAL2, map.remove(KEY2));
        assertEquals(2, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL3, map.get(KEY3));
        assertFalse(map.containsKey(KEY2));

        // Remove position 3 from size 3
        map.clear();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);
        assertEquals(VAL3, map.remove(KEY3));
        assertEquals(2, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL2, map.get(KEY2));
        assertFalse(map.containsKey(KEY3));

        // Remove non-existing key
        assertNull(map.remove("non_existing"));
        assertEquals(2, map.size());

        // Remove in delegated mode
        map.put(KEY3, VAL3);
        map.put(KEY4, VAL4);
        assertEquals(4, map.size());
        assertEquals(VAL2, map.remove(KEY2));
        assertEquals(3, map.size());
        assertFalse(map.containsKey(KEY2));
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL3, map.get(KEY3));
        assertEquals(VAL4, map.get(KEY4));
    }

    @Test
    public void testPutAll() {
        Flat3Map<String, String> map = new Flat3Map<>();
        Map<String, String> other = new HashMap<>();
        other.put(KEY1, VAL1);
        other.put(KEY2, VAL2);

        map.putAll(other);
        assertEquals(2, map.size());
        assertEquals(VAL1, map.get(KEY1));
        assertEquals(VAL2, map.get(KEY2));

        other.clear();
        other.put(KEY3, VAL3);
        other.put(KEY4, VAL4);
        map.putAll(other);
        assertEquals(4, map.size());
        assertEquals(VAL3, map.get(KEY3));
        assertEquals(VAL4, map.get(KEY4));

        Flat3Map<String, String> target = new Flat3Map<>();
        target.putAll(map);
        assertEquals(4, target.size());
    }

    @Test
    public void testClear() {
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey(KEY1));

        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);
        map.put(KEY4, VAL4);
        assertEquals(4, map.size());
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        assertFalse(map.containsKey(KEY1));
    }

    @Test
    public void testMapIterator() {
        Flat3Map<String, String> map = new Flat3Map<>();
        MapIterator<String, String> it = map.mapIterator();
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (NoSuchElementException expected) {
        }

        try {
            it.getKey();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }

        try {
            it.getValue();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }

        try {
            it.setValue("test");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }

        try {
            it.remove();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }

        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);

        it = map.mapIterator();
        assertTrue(it.hasNext());
        assertEquals(KEY1, it.next());
        assertEquals(KEY1, it.getKey());
        assertEquals(VAL1, it.getValue());
        assertEquals(VAL1, it.setValue("new_val1"));
        assertEquals("new_val1", it.getValue());

        assertTrue(it.hasNext());
        assertEquals(KEY2, it.next());
        it.remove();
        assertEquals(2, map.size());
        assertFalse(map.containsKey(KEY2));

        try {
            it.remove();
            fail("Expected IllegalStateException on consecutive remove");
        } catch (IllegalStateException expected) {
        }

        assertTrue(it.hasNext());
        assertEquals(KEY3, it.next());
        assertFalse(it.hasNext());

        it.reset();
        assertTrue(it.hasNext());
        assertEquals(KEY1, it.next());
    }

    @Test
    public void testMapIteratorDelegated() {
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);
        map.put(KEY4, VAL4);

        MapIterator<String, String> it = map.mapIterator();
        int count = 0;
        while (it.hasNext()) {
            it.next();
            assertNotNull(it.getKey());
            assertNotNull(it.getValue());
            count++;
        }
        assertEquals(4, count);

        it.reset();
        assertTrue(it.hasNext());
        it.next();
        it.setValue("modified");
        it.remove();
        assertEquals(3, map.size());
    }

    @Test
    public void testEntrySetKeySetValues() {
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);

        Set<Map.Entry<String, String>> entries = map.entrySet();
        assertEquals(3, entries.size());
        for (Map.Entry<String, String> entry : entries) {
            assertNotNull(entry.getKey());
            assertNotNull(entry.getValue());
            if (entry.getKey().equals(KEY1)) {
                entry.setValue("val1_mod");
            }
        }
        assertEquals("val1_mod", map.get(KEY1));

        Set<String> keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains(KEY1));
        assertTrue(keys.contains(KEY2));
        assertTrue(keys.contains(KEY3));
        assertFalse(keys.contains("absent"));

        Collection<String> values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("val1_mod"));
        assertTrue(values.contains(VAL2));
        assertTrue(values.contains(VAL3));
        assertFalse(values.contains("absent"));

        // Test iterator remove on keySet
        Iterator<String> keyIt = keys.iterator();
        while (keyIt.hasNext()) {
            if (keyIt.next().equals(KEY2)) {
                keyIt.remove();
            }
        }
        assertEquals(2, map.size());
        assertFalse(map.containsKey(KEY2));

        // Test iterator remove on values
        Iterator<String> valIt = values.iterator();
        while (valIt.hasNext()) {
            if (valIt.next().equals(VAL3)) {
                valIt.remove();
            }
        }
        assertEquals(1, map.size());
        assertFalse(map.containsKey(KEY3));

        // Test iterator remove on entrySet
        Iterator<Map.Entry<String, String>> entryIt = entries.iterator();
        while (entryIt.hasNext()) {
            entryIt.next();
            entryIt.remove();
        }
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testEntrySetKeySetValuesDelegated() {
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);
        map.put(KEY4, VAL4);

        assertEquals(4, map.entrySet().size());
        assertEquals(4, map.keySet().size());
        assertEquals(4, map.values().size());

        assertTrue(map.keySet().contains(KEY4));
        assertTrue(map.values().contains(VAL4));

        map.keySet().remove(KEY4);
        assertEquals(3, map.size());
        assertFalse(map.containsKey(KEY4));

        map.values().remove(VAL3);
        assertEquals(2, map.size());
        assertFalse(map.containsKey(KEY3));
    }

    @Test
    public void testEqualsAndHashCode() {
        Flat3Map<String, String> map1 = new Flat3Map<>();
        Flat3Map<String, String> map2 = new Flat3Map<>();

        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());

        map1.put(KEY1, VAL1);
        assertNotEquals(map1, map2);

        map2.put(KEY1, VAL1);
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());

        map1.put(KEY2, VAL2);
        map1.put(KEY3, VAL3);

        map2.put(KEY3, VAL3);
        map2.put(KEY2, VAL2);
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());

        // Compare with standard Map
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put(KEY1, VAL1);
        hashMap.put(KEY2, VAL2);
        hashMap.put(KEY3, VAL3);
        assertEquals(map1, hashMap);
        assertEquals(hashMap, map1);

        // Delegated equality
        map1.put(KEY4, VAL4);
        map2.put(KEY4, VAL4);
        assertEquals(map1, map2);
        assertEquals(map1.hashCode(), map2.hashCode());

        assertNotEquals(map1, null);
        assertNotEquals(map1, "not a map");
        assertEquals(map1, map1);
    }

    @Test
    public void testClone() {
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);

        Flat3Map<String, String> cloned = map.clone();
        assertNotSame(map, cloned);
        assertEquals(map, cloned);
        assertEquals(2, cloned.size());
        assertEquals(VAL1, cloned.get(KEY1));

        // Modify original, cloned should remain unchanged
        map.put(KEY3, VAL3);
        assertEquals(3, map.size());
        assertEquals(2, cloned.size());

        // Clone delegated map
        map.put(KEY4, VAL4);
        Flat3Map<String, String> clonedDelegated = map.clone();
        assertNotSame(map, clonedDelegated);
        assertEquals(map, clonedDelegated);
        assertEquals(4, clonedDelegated.size());
        assertEquals(VAL4, clonedDelegated.get(KEY4));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSerialization() throws Exception {
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);
        map.put(KEY3, VAL3);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Flat3Map<String, String> deserialized = (Flat3Map<String, String>) ois.readObject();

        assertEquals(map, deserialized);
        assertEquals(3, deserialized.size());
        assertEquals(VAL1, deserialized.get(KEY1));
        assertEquals(VAL2, deserialized.get(KEY2));
        assertEquals(VAL3, deserialized.get(KEY3));

        // Serialization of delegated map
        map.put(KEY4, VAL4);
        baos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(baos);
        oos.writeObject(map);
        oos.close();

        bais = new ByteArrayInputStream(baos.toByteArray());
        ois = new ObjectInputStream(bais);
        Flat3Map<String, String> deserializedDelegated = (Flat3Map<String, String>) ois.readObject();

        assertEquals(map, deserializedDelegated);
        assertEquals(4, deserializedDelegated.size());
        assertEquals(VAL4, deserializedDelegated.get(KEY4));
    }

    @Test
    public void testToString() {
        Flat3Map<String, String> map = new Flat3Map<>();
        assertEquals("{}", map.toString());

        map.put(KEY1, VAL1);
        assertEquals("{" + KEY1 + "=" + VAL1 + "}", map.toString());

        map.put(KEY2, VAL2);
        assertTrue(map.toString().contains(KEY1 + "=" + VAL1));
        assertTrue(map.toString().contains(KEY2 + "=" + VAL2));

        map.put(KEY3, VAL3);
        map.put(KEY4, VAL4);
        assertTrue(map.toString().contains(KEY4 + "=" + VAL4));
    }

    @Test
    public void testEntryEqualsAndHashCode() {
        Flat3Map<String, String> map = new Flat3Map<>();
        map.put(KEY1, VAL1);
        map.put(KEY2, VAL2);

        Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
        Map.Entry<String, String> entry1 = it.next();
        Map.Entry<String, String> entry2 = it.next();

        assertEquals(entry1, entry1);
        assertNotEquals(entry1, entry2);
        assertNotEquals(entry1, null);
        assertNotEquals(entry1, "someString");

        Map<String, String> temp = new HashMap<>();
        temp.put(KEY1, VAL1);
        Map.Entry<String, String> standardEntry = temp.entrySet().iterator().next();

        assertEquals(entry1, standardEntry);
        assertEquals(entry1.hashCode(), standardEntry.hashCode());
    }
}