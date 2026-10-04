package org.apache.commons.collections.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class MultiKeyTest {

    @Test
    public void testConstructorTwoKeys() {
        MultiKey<String> multiKey = new MultiKey<String>("A", "B");
        assertEquals(2, multiKey.size());
        assertEquals("A", multiKey.getKey(0));
        assertEquals("B", multiKey.getKey(1));
        assertArrayEquals(new Object[]{"A", "B"}, multiKey.getKeys());
    }

    @Test
    public void testConstructorThreeKeys() {
        MultiKey<Integer> multiKey = new MultiKey<Integer>(1, 2, 3);
        assertEquals(3, multiKey.size());
        assertEquals(Integer.valueOf(1), multiKey.getKey(0));
        assertEquals(Integer.valueOf(2), multiKey.getKey(1));
        assertEquals(Integer.valueOf(3), multiKey.getKey(2));
        assertArrayEquals(new Object[]{1, 2, 3}, multiKey.getKeys());
    }

    @Test
    public void testConstructorFourKeys() {
        MultiKey<String> multiKey = new MultiKey<String>("A", "B", "C", "D");
        assertEquals(4, multiKey.size());
        assertArrayEquals(new Object[]{"A", "B", "C", "D"}, multiKey.getKeys());
    }

    @Test
    public void testConstructorFiveKeys() {
        MultiKey<String> multiKey = new MultiKey<String>("A", "B", "C", "D", "E");
        assertEquals(5, multiKey.size());
        assertArrayEquals(new Object[]{"A", "B", "C", "D", "E"}, multiKey.getKeys());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayNull() {
        new MultiKey<Object>(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorArrayTooShort() {
        new MultiKey<Object>(new Object[]{ "A" });
    }

    @Test
    public void testConstructorArrayValid() {
        String[] keys = new String[] { "X", "Y", "Z" };
        MultiKey<String> multiKey = new MultiKey<String>(keys, true);
        assertEquals(3, multiKey.size());
        assertArrayEquals(keys, multiKey.getKeys());
        // Verify that the array is NOT cloned when makeClone is false
        assertSame(keys, multiKey.getKeys());
    }

    @Test
    public void testConstructorArrayClone() {
        String[] keys = new String[] { "X", "Y", "Z" };
        MultiKey<String> multiKey = new MultiKey<String>(keys, false);
        assertEquals(3, multiKey.size());
        assertArrayEquals(keys, multiKey.getKeys());
        // Verify that the array IS cloned when makeClone is true
        assertNotSame(keys, multiKey.getKeys());
    }

    @Test
    public void testGetKeysCloning() {
        MultiKey<String> multiKey = new MultiKey<String>("A", "B");
        Object[] keys1 = multiKey.getKeys();
        Object[] keys2 = multiKey.getKeys();
        // getKeys() should return a new clone every time to prevent mutation
        assertNotSame(keys1, keys2);
        assertArrayEquals(keys1, keys2);
    }

    @Test
    public void testEqualsAndHashCode() {
        MultiKey<String> mk1 = new MultiKey<String>("A", "B", "C");
        MultiKey<String> mk2 = new MultiKey<String>("A", "B", "C");
        MultiKey<String> mk3 = new MultiKey<String>("A", "B", "D");
        MultiKey<String> mk4 = new MultiKey<String>("A", "B");

        assertTrue(mk1.equals(mk1));
        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());

        assertFalse(mk1.equals(mk3));
        assertFalse(mk1.equals(mk4));
        assertFalse(mk1.equals(null));
        assertFalse(mk1.equals("Some String"));
    }

    @Test
    public void testHashCodeLazyInitialization() {
        MultiKey<String> mk = new MultiKey<String>("Key1", "Key2");
        int hash1 = mk.hashCode();
        int hash2 = mk.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testToString() {
        MultiKey<String> mk = new MultiKey<String>("A", "B");
        assertEquals("MultiKey[A, B]", mk.toString());
    }

    @Test
    public void testSerialization() throws Exception {
        MultiKey<String> mk = new MultiKey<String>("Hello", "World");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mk);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        MultiKey<String> deserialized = (MultiKey<String>) ois.readObject();
        ois.close();

        assertEquals(mk, deserialized);
        assertEquals(mk.hashCode(), deserialized.hashCode());
        assertArrayEquals(mk.getKeys(), deserialized.getKeys());
    }
}