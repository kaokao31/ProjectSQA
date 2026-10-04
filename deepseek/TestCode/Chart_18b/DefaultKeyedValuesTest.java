package org.jfree.data;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jfree.data.SortOrder;

public class DefaultKeyedValuesTest {

    private DefaultKeyedValues empty;
    private DefaultKeyedValues withData;

    @Before
    public void setUp() {
        empty = new DefaultKeyedValues();
        withData = new DefaultKeyedValues();
        withData.setValue("Key1", 1.0);
        withData.setValue("Key2", 2.0);
        withData.setValue("Key3", 3.0);
    }

    @Test
    public void testGetItemCountEmpty() {
        assertEquals(0, empty.getItemCount());
    }

    @Test
    public void testGetItemCountNonEmpty() {
        assertEquals(3, withData.getItemCount());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyNegativeIndex() {
        withData.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetKeyIndexOutOfBounds() {
        withData.getKey(3);
    }

    @Test
    public void testGetKeyValidIndex() {
        assertEquals("Key1", withData.getKey(0));
        assertEquals("Key2", withData.getKey(1));
        assertEquals("Key3", withData.getKey(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeIndex() {
        withData.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueIndexOutOfBounds() {
        withData.getValue(3);
    }

    @Test
    public void testGetValueValidIndex() {
        assertEquals(1.0, withData.getValue(0), 0.0001);
        assertEquals(2.0, withData.getValue(1), 0.0001);
        assertEquals(3.0, withData.getValue(2), 0.0001);
    }

    @Test
    public void testGetValueNullKey() {
        assertNull(withData.getValue(null));
    }

    @Test
    public void testGetValueNonExistentKey() {
        assertNull(withData.getValue("NonExistent"));
    }

    @Test
    public void testGetKeys() {
        assertEquals(3, withData.getKeys().size());
        assertTrue(withData.getKeys().contains("Key1"));
    }

    @Test
    public void testGetKeysEmpty() {
        assertTrue(empty.getKeys().isEmpty());
    }

    @Test
    public void testSetValueNewKey() {
        empty.setValue("NewKey", 10.0);
        assertEquals(1, empty.getItemCount());
        assertEquals(10.0, empty.getValue(0), 0.0001);
    }

    @Test
    public void testSetValueExistingKey() {
        withData.setValue("Key1", 100.0);
        assertEquals(3, withData.getItemCount());
        assertEquals(100.0, withData.getValue(0), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValueNullKey() {
        withData.setValue(null, 5.0);
    }

    @Test
    public void testInsertValueZeroIndex() {
        withData.insertValue(0, "Inserted", 0.5);
        assertEquals(4, withData.getItemCount());
        assertEquals("Inserted", withData.getKey(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertValueNegativeIndex() {
        withData.insertValue(-1, "Negative", 1.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertValueIndexOutOfBounds() {
        withData.insertValue(4, "OutOfBounds", 1.0);
    }

    @Test
    public void testInsertValueExistingKeyMoves() {
        withData.insertValue(0, "Key2", 99.0);
        assertEquals("Key2", withData.getKey(0));
        assertEquals(99.0, withData.getValue(0), 0.0001);
        assertEquals(3, withData.getItemCount());
    }

    @Test
    public void testRemoveValueByKey() {
        withData.removeValue("Key2");
        assertEquals(2, withData.getItemCount());
        assertNull(withData.getValue("Key2"));
    }

    @Test
    public void testRemoveValueByKeyNonExistent() {
        withData.removeValue("NonExistent");
        assertEquals(3, withData.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveValueByKeyNull() {
        withData.removeValue(null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByIndexNegative() {
        withData.removeValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testRemoveValueByIndexOutOfBounds() {
        withData.removeValue(3);
    }

    @Test
    public void testRemoveValueByIndexValid() {
        withData.removeValue(1);
        assertEquals(2, withData.getItemCount());
        assertEquals("Key1", withData.getKey(0));
        assertEquals("Key3", withData.getKey(1));
    }

    @Test
    public void testClear() {
        withData.clear();
        assertEquals(0, withData.getItemCount());
    }

    @Test
    public void testSortByKeysAscending() {
        DefaultKeyedValues unsorted = new DefaultKeyedValues();
        unsorted.setValue("Z", 3.0);
        unsorted.setValue("A", 1.0);
        unsorted.setValue("M", 2.0);
        unsorted.sortByKeys(SortOrder.ASCENDING);
        assertEquals("A", unsorted.getKey(0));
        assertEquals("M", unsorted.getKey(1));
        assertEquals("Z", unsorted.getKey(2));
    }

    @Test
    public void testSortByKeysDescending() {
        DefaultKeyedValues unsorted = new DefaultKeyedValues();
        unsorted.setValue("Z", 3.0);
        unsorted.setValue("A", 1.0);
        unsorted.setValue("M", 2.0);
        unsorted.sortByKeys(SortOrder.DESCENDING);
        assertEquals("Z", unsorted.getKey(0));
        assertEquals("M", unsorted.getKey(1));
        assertEquals("A", unsorted.getKey(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSortByKeysNullOrder() {
        withData.sortByKeys(null);
    }

    @Test
    public void testSortByValuesAscending() {
        DefaultKeyedValues unsorted = new DefaultKeyedValues();
        unsorted.setValue("C", 3.0);
        unsorted.setValue("B", 1.0);
        unsorted.setValue("A", 2.0);
        unsorted.sortByValues(SortOrder.ASCENDING);
        assertEquals("B", unsorted.getKey(0));
        assertEquals("A", unsorted.getKey(1));
        assertEquals("C", unsorted.getKey(2));
    }

    @Test
    public void testSortByValuesDescending() {
        DefaultKeyedValues unsorted = new DefaultKeyedValues();
        unsorted.setValue("C", 3.0);
        unsorted.setValue("B", 1.0);
        unsorted.setValue("A", 2.0);
        unsorted.sortByValues(SortOrder.DESCENDING);
        assertEquals("C", unsorted.getKey(0));
        assertEquals("A", unsorted.getKey(1));
        assertEquals("B", unsorted.getKey(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSortByValuesNullOrder() {
        withData.sortByValues(null);
    }

    @Test
    public void testIndexOfExisting() {
        assertEquals(0, withData.getIndex("Key1"));
        assertEquals(1, withData.getIndex("Key2"));
        assertEquals(2, withData.getIndex("Key3"));
    }

    @Test
    public void testIndexOfNonExistent() {
        assertEquals(-1, withData.getIndex("NonExistent"));
    }

    @Test
    public void testIndexOfNullKey() {
        assertEquals(-1, withData.getIndex(null));
    }

    @Test
    public void testGetValueByKeyExisting() {
        assertEquals(1.0, withData.getValue("Key1"), 0.0001);
    }

    @Test
    public void testGetValueByKeyNonExistent() {
        assertNull(withData.getValue("NonExistent"));
    }

    @Test
    public void testGetValueByKeyNull() {
        assertNull(withData.getValue(null));
    }

    @Test
    public void testSetValueUpdatesExistingAndPreservesOrder() {
        withData.setValue("Key2", 22.0);
        assertEquals(3, withData.getItemCount());
        assertEquals("Key1", withData.getKey(0));
        assertEquals("Key2", withData.getKey(1));
        assertEquals(22.0, withData.getValue(1), 0.0001);
    }

    @Test
    public void testInsertValueAtEnd() {
        withData.insertValue(3, "End", 4.0);
        assertEquals(4, withData.getItemCount());
        assertEquals("End", withData.getKey(3));
    }

    @Test
    public void testInsertValueAtBeginning() {
        withData.insertValue(0, "Start", 0.0);
        assertEquals("Start", withData.getKey(0));
        assertEquals("Key1", withData.getKey(1));
    }

    @Test
    public void testRemoveValueLastElement() {
        withData.removeValue("Key3");
        assertEquals(2, withData.getItemCount());
        assertEquals("Key2", withData.getKey(1));
    }

    @Test
    public void testClearEmpty() {
        empty.clear();
        assertEquals(0, empty.getItemCount());
    }

    @Test
    public void testSortByKeysEmptyList() {
        empty.sortByKeys(SortOrder.ASCENDING);
        assertEquals(0, empty.getItemCount());
    }

    @Test
    public void testSortByValuesEmptyList() {
        empty.sortByValues(SortOrder.ASCENDING);
        assertEquals(0, empty.getItemCount());
    }

    @Test
    public void testSetValueNullValue() {
        withData.setValue("Key4", null);
        assertNull(withData.getValue("Key4"));
        assertEquals(4, withData.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertValueNullKey() {
        withData.insertValue(0, null, 5.0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testInsertValueIndexGreaterThanSize() {
        withData.insertValue(5, "TooFar", 5.0);
    }

    @Test
    public void testGetKeysCopy() {
        java.util.List keys = withData.getKeys();
        keys.add("Extra");
        assertEquals(3, withData.getItemCount());
    }

    @Test
    public void testIndexOfAfterRemove() {
        withData.removeValue("Key2");
        assertEquals(-1, withData.getIndex("Key2"));
    }
}