package org.apache.commons.collections4.keyvalue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class MultiKeyTest {

    @Test
    public void testConstructorsAndSize() {
        assertEquals(2, new MultiKey<>("a", "b").size());
        assertEquals(3, new MultiKey<>("a", "b", "c").size());
        assertEquals(4, new MultiKey<>("a", "b", "c", "d").size());
        assertEquals(5, new MultiKey<>("a", "b", "c", "d", "e").size());
        assertEquals(2, new MultiKey<>(new Object[] {"a", "b"}).size());
        assertEquals(0, new MultiKey<>(new Object[0]).size());
    }

    @Test
    public void testGetKey() {
        MultiKey<String> mk = new MultiKey<>("a", "b", "c");
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
        assertEquals("c", mk.getKey(2));
        assertNull(new MultiKey<String>("a", null).getKey(1));
    }

    @Test
    public void testGetKeysReturnsClone() {
        Object[] keys = new Object[] {"a", "b"};
        MultiKey<Object> mk = new MultiKey<>(keys);
        Object[] result = mk.getKeys();
        assertNotSame(keys, result);
        assertArrayEquals(keys, result);
        result[0] = "zzz";
        assertEquals("a", mk.getKey(0));
    }

    @Test
    public void testArrayConstructorClonesArray() {
        Object[] keys = new Object[] {"a", "b"};
        MultiKey<Object> mk = new MultiKey<>(keys);
        keys[0] = "zzz";
        assertEquals("a", mk.getKey(0));
    }

    @Test
    public void testEqualsSameInstance() {
        MultiKey<String> mk = new MultiKey<>("a", "b");
        assertTrue(mk.equals(mk));
        assertEquals(mk, mk);
    }

    @Test
    public void testEqualsNull() {
        MultiKey<String> mk = new MultiKey<>("a", "b");
        assertFalse(mk.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        MultiKey<String> mk = new MultiKey<>("a", "b");
        assertFalse(mk.equals("a,b"));
        assertFalse(mk.equals(Integer.valueOf(42)));
    }

    @Test
    public void testEqualsDifferentSize() {
        MultiKey<String> mk2 = new MultiKey<>("a", "b");
        MultiKey<String> mk3 = new MultiKey<>("a", "b", "c");
        assertFalse(mk2.equals(mk3));
        assertFalse(mk3.equals(mk2));
    }

    @Test
    public void testEqualsDifferentOrder() {
        MultiKey<String> mk1 = new MultiKey<>("a", "b");
        MultiKey<String> mk2 = new MultiKey<>("b", "a");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testEqualsWithNullKeys() {
        MultiKey<String> mk1 = new MultiKey<>((String) null, "b");
        MultiKey<String> mk2 = new MultiKey<>((String) null, "b");
        MultiKey<String> mk3 = new MultiKey<>("a", (String) null);

        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());
        assertFalse(mk1.equals(mk3));
        assertFalse(mk3.equals(mk1));
    }

    @Test
    public void testHashCodeWithNullKey() {
        MultiKey<String> mk1 = new MultiKey<>((String) null, "B");
        MultiKey<String> mk2 = new MultiKey<>((String) null, "B");
        assertEquals(mk1.hashCode(), mk2.hashCode());

        MultiKey<String> mk3 = new MultiKey<>("A", (String) null);
        MultiKey<String> mk4 = new MultiKey<>("A", (String) null);
        assertEquals(mk3.hashCode(), mk4.hashCode());
    }

    @Test
    public void testHashCodeWithAllNullKeys() {
        MultiKey<String> mk1 = new MultiKey<>((String) null, (String) null);
        MultiKey<String> mk2 = new MultiKey<>((String) null, (String) null);
        assertEquals(mk1, mk2);
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testHashCodeWithArrayContainingNull() {
        Object[] keys = new Object[] {"a", null, "c"};
        MultiKey<Object> mk1 = new MultiKey<>(keys);
        MultiKey<Object> mk2 = new MultiKey<>(new Object[] {"a", null, "c"});

        assertEquals(3, mk1.size());
        assertEquals("a", mk1.getKey(0));
        assertNull(mk1.getKey(1));
        assertEquals("c", mk1.getKey(2));
        assertArrayEquals(keys, mk1.getKeys());
        assertEquals(mk1, mk2);
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testGetKeyOutOfBounds() {
        MultiKey<String> mk = new MultiKey<>("a", "b");
        try {
            mk.getKey(-1);
            fail("Expected IndexOutOfBoundsException for negative index");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
        try {
            mk.getKey(2);
            fail("Expected IndexOutOfBoundsException for index >= size");
        } catch (final IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testNullArrayThrowsException() {
        try {
            new MultiKey<Object>((Object[]) null);
            fail("Expected NullPointerException or IllegalArgumentException for null array");
        } catch (final NullPointerException | IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testToString() {
        MultiKey<String> mk = new MultiKey<>("a", "b");
        assertNotNull(mk.toString());
        assertTrue(mk.toString().contains("a"));
        assertTrue(mk.toString().contains("b"));
    }

    @Test
    public void testSerialization() throws Exception {
        MultiKey<String> mk = new MultiKey<>("a", "b");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(mk);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Object obj = ois.readObject();
            assertTrue(obj instanceof MultiKey);
            MultiKey<?> deserialized = (MultiKey<?>) obj;
            assertEquals(mk, deserialized);
            assertEquals(mk.hashCode(), deserialized.hashCode());
        }
    }
}