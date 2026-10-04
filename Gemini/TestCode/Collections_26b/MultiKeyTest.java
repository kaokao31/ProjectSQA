package org.apache.commons.collections4.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class MultiKeyTest {

    @Test
    public void testConstructorTwoKeys() {
        MultiKey<String> mk = new MultiKey<>("A", "B");
        assertEquals(2, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertArrayEquals(new Object[]{"A", "B"}, mk.getKeys());
    }

    @Test
    public void testConstructorThreeKeys() {
        MultiKey<String> mk = new MultiKey<>("A", "B", "C");
        assertEquals(3, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertEquals("C", mk.getKey(2));
        assertArrayEquals(new Object[]{"A", "B", "C"}, mk.getKeys());
    }

    @Test
    public void testConstructorFourKeys() {
        MultiKey<String> mk = new MultiKey<>("A", "B", "C", "D");
        assertEquals(4, mk.size());
        assertArrayEquals(new Object[]{"A", "B", "C", "D"}, mk.getKeys());
    }

    @Test
    public void testConstructorFiveKeys() {
        MultiKey<String> mk = new MultiKey<>("A", "B", "C", "D", "E");
        assertEquals(5, mk.size());
        assertArrayEquals(new Object[]{"A", "B", "C", "D", "E"}, mk.getKeys());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorArrayNull() {
        new MultiKey<String>((Object[]) null);
    }

    @Test
    public void testConstructorArrayCloning() {
        String[] keys = new String[]{"A", "B", "C"};
        MultiKey<String> mk = new MultiKey<>(keys, true);
        assertEquals(3, mk.size());
        assertArrayEquals(keys, mk.getKeys());
        // Verify that the internal array is cloned (or at least stored safely)
        // Modifying the original array should not affect the MultiKey if makeClone is true,
        // or check constructor behavior. Let's test makeClone = false and true.
        
        MultiKey<String> mkClone = new MultiKey<>(keys, true);
        keys[0] = "Z";
        assertEquals("A", mkClone.getKey(0)); // Should be independent if cloned

        String[] keys2 = new String[]{"X", "Y"};
        MultiKey<String> mkNoClone = new MultiKey<>(keys2, false);
        keys2[0] = "Z";
        assertEquals("Z", mkNoClone.getKey(0)); // Not cloned, so reflects change
    }

    @Test
    public void testEqualsAndHashCode() {
        MultiKey<String> mk1 = new MultiKey<>("A", "B", "C");
        MultiKey<String> mk2 = new MultiKey<>("A", "B", "C");
        MultiKey<String> mk3 = new MultiKey<>("A", "B", "D");
        MultiKey<String> mk4 = new MultiKey<>("A", "B");

        assertTrue(mk1.equals(mk1));
        assertTrue(mk1.equals(mk2));
        assertEquals(mk1.hashCode(), mk2.hashCode());

        assertFalse(mk1.equals(mk3));
        assertFalse(mk1.equals(mk4));
        assertFalse(mk1.equals(null));
        assertFalse(mk1.equals("Some String"));
    }

    @Test
    public void testHashCodeCaching() {
        MultiKey<String> mk = new MultiKey<>("Hash", "Test");
        int h1 = mk.hashCode();
        int h2 = mk.hashCode();
        assertEquals(h1, h2);
    }

    @Test
    public void testToString() {
        MultiKey<String> mk = new MultiKey<>("A", "B", "C");
        String str = mk.toString();
        assertNotNull(str);
        assertTrue(str.contains("A"));
        assertTrue(str.contains("B"));
        assertTrue(str.contains("C"));
    }

    @Test
    public void testSerialization() throws Exception {
        MultiKey<String> mk = new MultiKey<>("Hello", "World");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(mk);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        MultiKey<String> mkDeserialized = (MultiKey<String>) ois.readObject();
        ois.close();

        assertEquals(mk, mkDeserialized);
        assertEquals(mk.hashCode(), mkDeserialized.hashCode());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyOutOfBoundsNegative() {
        MultiKey<String> mk = new MultiKey<>("A", "B");
        mk.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyOutOfBoundsTooLarge() {
        MultiKey<String> mk = new MultiKey<>("A", "B");
        mk.getKey(2);
    }

    @Test
    public void testNullKeys() {
        MultiKey<String> mk = new MultiKey<>(null, "B", null);
        assertEquals(3, mk.size());
        assertNull(mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertNull(mk.getKey(2));
        
        MultiKey<String> mk2 = new MultiKey<>(null, "B", null);
        assertEquals(mk, mk2);
        assertEquals(mk.hashCode(), mk2.hashCode());
    }
}