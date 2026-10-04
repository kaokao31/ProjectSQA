package org.apache.commons.collections.keyvalue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class MultiKeyTest {

    @Test
    public void testTwoKeyConstructor() {
        MultiKey mk = new MultiKey("a", "b");
        assertEquals(2, mk.size());
        assertEquals("a", mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }

    @Test
    public void testThreeKeyConstructor() {
        MultiKey mk = new MultiKey("a", "b", "c");
        assertEquals(3, mk.size());
        assertArrayEquals(new Object[] {"a", "b", "c"}, mk.getKeys());
    }

    @Test
    public void testFourKeyConstructor() {
        MultiKey mk = new MultiKey("a", "b", "c", "d");
        assertEquals(4, mk.size());
        assertEquals("d", mk.getKey(3));
    }

    @Test
    public void testFiveKeyConstructor() {
        MultiKey mk = new MultiKey("a", "b", "c", "d", "e");
        assertEquals(5, mk.size());
        assertArrayEquals(new Object[] {"a", "b", "c", "d", "e"}, mk.getKeys());
    }

    @Test
    public void testArrayConstructor() {
        MultiKey mk = new MultiKey(new Object[] {"a", "b"});
        assertEquals(2, mk.size());
        assertArrayEquals(new Object[] {"a", "b"}, mk.getKeys());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArrayConstructorThrows() {
        new MultiKey((Object[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullArrayConstructorWithMakeCloneThrows() {
        new MultiKey((Object[]) null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyArrayConstructorThrows() {
        new MultiKey(new Object[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyArrayConstructorWithMakeCloneThrows() {
        new MultiKey(new Object[0], false);
    }

    @Test
    public void testArrayConstructorMakeCloneTrue() {
        Object[] keys = new Object[] {"a", "b"};
        MultiKey mk = new MultiKey(keys, true);
        assertArrayEquals(new Object[] {"a", "b"}, mk.getKeys());
        keys[0] = "z";
        assertArrayEquals(new Object[] {"a", "b"}, mk.getKeys());
    }

    @Test
    public void testArrayConstructorMakeCloneFalse() {
        Object[] keys = new Object[] {"a", "b"};
        MultiKey mk = new MultiKey(keys, false);
        assertArrayEquals(new Object[] {"a", "b"}, mk.getKeys());
        assertTrue(mk.equals(new MultiKey("a", "b")));
    }

    @Test
    public void testGetKeysReturnsCopy() {
        MultiKey mk = new MultiKey("a", "b");
        Object[] keys = mk.getKeys();
        keys[0] = "z";
        assertEquals("a", mk.getKey(0));
        assertArrayEquals(new Object[] {"a", "b"}, mk.getKeys());
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        new MultiKey("a", "b").getKey(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testGetKeyIndexTooLarge() {
        new MultiKey("a", "b").getKey(2);
    }

    @Test
    public void testGetKeyNull() {
        MultiKey mk = new MultiKey((Object) null, "b");
        assertEquals(null, mk.getKey(0));
        assertEquals("b", mk.getKey(1));
    }

    @Test
    public void testEqualsWithSameObject() {
        MultiKey mk = new MultiKey("a", "b");
        assertTrue(mk.equals(mk));
    }

    @Test
    public void testEqualsEqualInstances() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testEqualsDifferentOrder() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("b", "a");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testEqualsDifferentLength() {
        MultiKey mk1 = new MultiKey("a", "b");
        MultiKey mk2 = new MultiKey("a", "b", "c");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testEqualsNull() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        MultiKey mk = new MultiKey("a", "b");
        assertFalse(mk.equals("MultiKey[a, b]"));
    }

    @Test
    public void testEqualsWithNullKeys() {
        MultiKey mk1 = new MultiKey((Object) null, "b");
        MultiKey mk2 = new MultiKey((Object) null, "b");
        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testEqualsWithOneNullOneNot() {
        MultiKey mk1 = new MultiKey((Object) null, "b");
        MultiKey mk2 = new MultiKey("a", "b");
        assertFalse(mk1.equals(mk2));
    }

    @Test
    public void testHashCodeStable() {
        MultiKey mk = new MultiKey("a", "b");
        int hash = mk.hashCode();
        assertEquals(hash, mk.hashCode());
    }

    @Test
    public void testHashCodeForAllNullKeys() {
        MultiKey mk = new MultiKey((Object) null, (Object) null);
        assertEquals(0, mk.hashCode());
    }

    @Test
    public void testHashCodeForNullAndNonNull() {
        MultiKey mk = new MultiKey((Object) null, "b");
        MultiKey mk2 = new MultiKey((Object) null, "b");
        assertEquals(mk.hashCode(), mk2.hashCode());
    }

    @Test
    public void testEqualsBetweenCloneAndNonClone() {
        Object[] keys = new Object[] {"a", "b"};
        MultiKey mk1 = new MultiKey(keys, true);
        MultiKey mk2 = new MultiKey(keys, false);
        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test
    public void testToString() {
        MultiKey mk = new MultiKey("a", "b");
        assertEquals("MultiKey[a, b]", mk.toString());
    }

    @Test
    public void testSerializationRoundTrip() throws Exception {
        MultiKey original = new MultiKey("a", "b");
        MultiKey copy = serializeDeserialize(original);
        assertNotSame(original, copy);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
        assertArrayEquals(original.getKeys(), copy.getKeys());
    }

    @Test
    public void testSerializationRoundTripWithNullKeys() throws Exception {
        MultiKey original = new MultiKey((Object) null, "b", null);
        MultiKey copy = serializeDeserialize(original);
        assertEquals(original, copy);
        assertEquals(original.hashCode(), copy.hashCode());
        assertArrayEquals(original.getKeys(), copy.getKeys());
    }

    private MultiKey serializeDeserialize(MultiKey mk) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(mk);
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        MultiKey copy = (MultiKey) ois.readObject();
        ois.close();
        return copy;
    }
}