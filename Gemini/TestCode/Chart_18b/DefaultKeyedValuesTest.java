package org.jfree.data;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DefaultKeyedValuesTest {

    private DefaultKeyedValues keyedValues;

    @Before
    public void setUp() {
        keyedValues = new DefaultKeyedValues();
    }

    @Test
    public void testAddValueAndGetItemCount() {
        assertEquals(0, keyedValues.getItemCount());
        keyedValues.addValue("Key1", 10.5);
        assertEquals(1, keyedValues.getItemCount());
        keyedValues.addValue("Key2", 20.0);
        assertEquals(2, keyedValues.getItemCount());
    }

    @Test
    public void testGetValue() {
        keyedValues.addValue("Key1", 100);
        keyedValues.addValue("Key2", null);

        assertEquals(100, keyedValues.getValue(0));
        assertEquals(100, keyedValues.getValue("Key1"));
        assertNull(keyedValues.getValue(1));
        assertNull(keyedValues.getValue("Key2"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeIndex() {
        keyedValues.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueOutOfBoundsIndex() {
        keyedValues.getValue(0);
    }

    @Test(expected = UnknownKeyException.class)
    public void testGetValueUnknownKey() {
        keyedValues.getValue("NonExistent");
    }

    @Test
    public void testGetKey() {
        keyedValues.addValue("Alpha", 1);
        keyedValues.addValue("Beta", 2);

        assertEquals("Alpha", keyedValues.getKey(0));
        assertEquals("Beta", keyedValues.getKey(1));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyOutOfBounds() {
        keyedValues.getKey(0);
    }

    @Test
    public void testGetIndex() {
        keyedValues.addValue("A", 10);
        keyedValues.addValue("B", 20);

        assertEquals(0, keyedValues.getIndex("A"));
        assertEquals(1, keyedValues.getIndex("B"));
        assertEquals(-1, keyedValues.getIndex("C"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetIndexNullKey() {
        keyedValues.getIndex(null);
    }

    @Test
    public void testGetKeys() {
        keyedValues.addValue("X", 1);
        keyedValues.addValue("Y", 2);

        List keys = keyedValues.getKeys();
        assertEquals(2, keys.size());
        assertTrue(keys.contains("X"));
        assertTrue(keys.contains("Y"));
    }

    @Test
    public void testSetValueUpdatesExisting() {
        keyedValues.addValue("Key1", 10);
        assertEquals(1, keyedValues.getItemCount());
        assertEquals(10, keyedValues.getValue("Key1"));

        // Update existing key
        keyedValues.setValue("Key1", 99);
        assertEquals(1, keyedValues.getItemCount());
        assertEquals(99, keyedValues.getValue("Key1"));
    }

    @Test
    public void testSetValueInsertsNew() {
        keyedValues.setValue("Key1", 50);
        assertEquals(1, keyedValues.getItemCount());
        assertEquals(50, keyedValues.getValue("Key1"));

        keyedValues.setValue("Key2", null);
        assertEquals(2, keyedValues.getItemCount());
        assertNull(keyedValues.getValue("Key2"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueNullKey() {
        keyedValues.setValue(null, 10);
    }

    @Test
    public void testRemoveValueByIndex() {
        keyedValues.addValue("A", 1);
        keyedValues.addValue("B", 2);
        keyedValues.addValue("C", 3);

        keyedValues.removeValue(1); // Removes "B"
        assertEquals(2, keyedValues.getItemCount());
        assertEquals("A", keyedValues.getKey(0));
        assertEquals("C", keyedValues.getKey(1));
        assertEquals(1, keyedValues.getValue("A"));
        assertEquals(3, keyedValues.getValue("C"));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByNegativeIndex() {
        keyedValues.removeValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByOutOfBoundsIndex() {
        keyedValues.removeValue(0);
    }

    @Test
    public void testRemoveValueByKey() {
        keyedValues.addValue("A", 1);
        keyedValues.addValue("B", 2);

        keyedValues.removeValue("A");
        assertEquals(1, keyedValues.getItemCount());
        assertEquals("B", keyedValues.getKey(0));
    }

    @Test(expected = UnknownKeyException.class)
    public void testRemoveValueByUnknownKey() {
        keyedValues.removeValue("Unknown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveValueNullKey() {
        keyedValues.removeValue(null);
    }

    @Test
    public void testSortValueAscending() {
        keyedValues.addValue("Z", 30);
        keyedValues.addValue("A", 10);
        keyedValues.addValue("M", 20);

        keyedValues.sortByValues(org.jfree.util.SortOrder.ASCENDING);

        assertEquals("A", keyedValues.getKey(0));
        assertEquals(10, keyedValues.getValue(0));
        assertEquals("M", keyedValues.getKey(1));
        assertEquals(20, keyedValues.getValue(1));
        assertEquals("Z", keyedValues.getKey(2));
        assertEquals(30, keyedValues.getValue(2));
    }

    @Test
    public void testSortValueDescending() {
        keyedValues.addValue("A", 10);
        keyedValues.addValue("Z", 30);
        keyedValues.addValue("M", 20);

        keyedValues.sortByValues(org.jfree.util.SortOrder.DESCENDING);

        assertEquals("Z", keyedValues.getKey(0));
        assertEquals(30, keyedValues.getValue(0));
        assertEquals("M", keyedValues.getKey(1));
        assertEquals(20, keyedValues.getValue(1));
        assertEquals("A", keyedValues.getKey(2));
        assertEquals(10, keyedValues.getValue(2));
    }

    @Test
    public void testSortKeysAscending() {
        keyedValues.addValue("Z", 30);
        keyedValues.addValue("A", 10);
        keyedValues.addValue("M", 20);

        keyedValues.sortByKeys(org.jfree.util.SortOrder.ASCENDING);

        assertEquals("A", keyedValues.getKey(0));
        assertEquals("M", keyedValues.getKey(1));
        assertEquals("Z", keyedValues.getKey(2));
    }

    @Test
    public void testSortKeysDescending() {
        keyedValues.addValue("A", 10);
        keyedValues.addValue("Z", 30);
        keyedValues.addValue("M", 20);

        keyedValues.sortByKeys(org.jfree.util.SortOrder.DESCENDING);

        assertEquals("Z", keyedValues.getKey(0));
        assertEquals("M", keyedValues.getKey(1));
        assertEquals("A", keyedValues.getKey(2));
    }

    @Test
    public void testEqualsAndHashCode() {
        DefaultKeyedValues kv1 = new DefaultKeyedValues();
        DefaultKeyedValues kv2 = new DefaultKeyedValues();

        assertTrue(kv1.equals(kv2));
        assertEquals(kv1.hashCode(), kv2.hashCode());

        kv1.addValue("Key1", 1);
        assertFalse(kv1.equals(kv2));

        kv2.addValue("Key1", 1);
        assertTrue(kv1.equals(kv2));
        assertEquals(kv1.hashCode(), kv2.hashCode());

        kv1.addValue("Key2", null);
        kv2.addValue("Key2", null);
        assertTrue(kv1.equals(kv2));

        assertFalse(kv1.equals(null));
        assertFalse(kv1.equals("NotAKeyedValues"));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        keyedValues.addValue("Key1", 100);
        keyedValues.addValue("Key2", 200);

        DefaultKeyedValues clone = (DefaultKeyedValues) keyedValues.clone();
        assertNotSame(keyedValues, clone);
        assertEquals(keyedValues, clone);
        assertEquals(keyedValues.getItemCount(), clone.getItemCount());
        assertEquals(keyedValues.getValue("Key1"), clone.getValue("Key1"));
    }
}