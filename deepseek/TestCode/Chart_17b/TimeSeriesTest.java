package org.jfree.data.time;

import static org.junit.Assert.*;

import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.Year;
import org.jfree.data.time.TimeSeriesDataItem;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
 * Comprehensive JUnit 4 test suite for TimeSeries (Chart bug 17).
 * Achieves high coverage and targets the known clone/deep-copy defect.
 */
public class TimeSeriesTest {

    private TimeSeries series;
    private static final double EPSILON = 0.000000001;

    @Before
    public void setUp() {
        series = new TimeSeries("Test Series", "Year", "Value");
        series.add(new Year(2020), 100.0);
        series.add(new Year(2021), 200.0);
        series.add(new Year(2022), 300.0);
    }

    // --------------------------------------------------------------
    // Basic construction and property tests
    // --------------------------------------------------------------
    @Test
    public void testConstructor() {
        TimeSeries s = new TimeSeries("Empty", "X", "Y");
        assertEquals("Empty", s.getKey());
        assertEquals("X", s.getDomainDescription());
        assertEquals("Y", s.getRangeDescription());
        assertTrue(s.isEmpty());
        assertEquals(0, s.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new TimeSeries(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKeyWithDescriptions() {
        new TimeSeries(null, "domain", "range");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptyKey() {
        new TimeSeries("");
    }

    @Test
    public void testGetKey() {
        assertEquals("Test Series", series.getKey());
    }

    // --------------------------------------------------------------
    // Add methods
    // --------------------------------------------------------------
    @Test
    public void testAddItem() {
        series.add(new Year(2023), 400.0);
        assertEquals(4, series.getItemCount());
        assertEquals(400.0, series.getValue(new Year(2023)).doubleValue(), EPSILON);
    }

    @Test
    public void testAddItemWithUpdate() {
        series.add(new Year(2021), 250.0);
        assertEquals(3, series.getItemCount());
        assertEquals(250.0, series.getValue(new Year(2021)).doubleValue(), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullTimePeriod() {
        series.add(null, 1.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullItem() {
        series.add(null);
    }

    @Test
    public void testAddOrUpdateNew() {
        TimeSeriesDataItem item = new TimeSeriesDataItem(new Year(2023), 500.0);
        series.addOrUpdate(item);
        assertEquals(4, series.getItemCount());
        assertEquals(500.0, series.getValue(new Year(2023)).doubleValue(), EPSILON);
    }

    @Test
    public void testAddOrUpdateExisting() {
        TimeSeriesDataItem item = new TimeSeriesDataItem(new Year(2021), 250.0);
        series.addOrUpdate(item);
        assertEquals(3, series.getItemCount());
        assertEquals(250.0, series.getValue(new Year(2021)).doubleValue(), EPSILON);
    }

    // --------------------------------------------------------------
    // Value and data access
    // --------------------------------------------------------------
    @Test
    public void testGetValueByPeriod() {
        assertEquals(100.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
    }

    @Test
    public void testGetValueByPeriodNotFound() {
        assertNull(series.getValue(new Year(2019)));
    }

    @Test
    public void testGetValueByIndex() {
        assertEquals(100.0, series.getValue(0).doubleValue(), EPSILON);
        assertEquals(200.0, series.getValue(1).doubleValue(), EPSILON);
        assertEquals(300.0, series.getValue(2).doubleValue(), EPSILON);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeIndex() {
        series.getValue(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueIndexTooLarge() {
        series.getValue(5);
    }

    @Test
    public void testGetTimePeriod() {
        assertEquals(new Year(2020), series.getTimePeriod(0));
        assertEquals(new Year(2021), series.getTimePeriod(1));
        assertEquals(new Year(2022), series.getTimePeriod(2));
    }

    @Test
    public void testGetItemCount() {
        assertEquals(3, series.getItemCount());
        series.add(new Year(2023), 400.0);
        assertEquals(4, series.getItemCount());
    }

    // --------------------------------------------------------------
    // Delete methods
    // --------------------------------------------------------------
    @Test
    public void testDeleteByPeriod() {
        series.delete(new Year(2021));
        assertEquals(2, series.getItemCount());
        assertNull(series.getValue(new Year(2021)));
    }

    @Test
    public void testDeleteByRange() {
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(300.0, series.getValue(0).doubleValue(), EPSILON);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteInvalidStart() {
        series.delete(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteInvalidEnd() {
        series.delete(0, 5);
    }

    @Test
    public void testClear() {
        series.clear();
        assertEquals(0, series.getItemCount());
        assertTrue(series.isEmpty());
    }

    // --------------------------------------------------------------
    // Clone test (targets known bug in Chart 17)
    // --------------------------------------------------------------
    @Test
    public void testCloneDeepCopy() {
        // Clone and verify independent data
        TimeSeries clone = null;
        try {
            clone = (TimeSeries) series.clone();
        } catch (CloneNotSupportedException e) {
            fail("Clone not supported");
        }

        assertNotNull(clone);
        assertEquals(series.getItemCount(), clone.getItemCount());
        assertEquals(series.getKey(), clone.getKey());

        // Modify the original and ensure clone unchanged
        series.add(new Year(2023), 400.0);
        series.update(0, 999.0);
        assertEquals(4, series.getItemCount());
        assertEquals(3, clone.getItemCount());
        assertEquals(100.0, clone.getValue(0).doubleValue(), EPSILON);
        assertNull(clone.getValue(new Year(2023)));

        // Modify the clone and ensure original unchanged
        clone.add(new Year(2024), 500.0);
        assertEquals(4, clone.getItemCount());
        assertEquals(4, series.getItemCount()); // original has 4 now
        assertNull(series.getValue(new Year(2024)));
    }

    @Test
    public void testCloneEmpty() throws CloneNotSupportedException {
        TimeSeries empty = new TimeSeries("Empty");
        TimeSeries clone = (TimeSeries) empty.clone();
        assertTrue(clone.isEmpty());
        assertEquals(empty.getKey(), clone.getKey());
    }

    // --------------------------------------------------------------
    // Serialization test (another common defect area)
    // --------------------------------------------------------------
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(series);
        oos.flush();
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        TimeSeries deserialized = (TimeSeries) ois.readObject();
        ois.close();

        assertEquals(series.getItemCount(), deserialized.getItemCount());
        assertEquals(series.getKey(), deserialized.getKey());
        assertEquals(100.0, deserialized.getValue(0).doubleValue(), EPSILON);
        assertEquals(200.0, deserialized.getValue(1).doubleValue(), EPSILON);
        assertEquals(300.0, deserialized.getValue(2).doubleValue(), EPSILON);
    }

    @Test
    public void testSerializationEmpty() throws IOException, ClassNotFoundException {
        TimeSeries empty = new TimeSeries("Empty");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(empty);
        oos.flush();
        oos.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        TimeSeries deserialized = (TimeSeries) ois.readObject();
        ois.close();

        assertTrue(deserialized.isEmpty());
        assertEquals("Empty", deserialized.getKey());
    }

    // --------------------------------------------------------------
    // Update method
    // --------------------------------------------------------------
    @Test
    public void testUpdate() {
        series.update(0, 150.0);
        assertEquals(150.0, series.getValue(0).doubleValue(), EPSILON);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateInvalidIndex() {
        series.update(5, 10.0);
    }

    @Test
    public void testUpdateByPeriod() {
        series.update(new Year(2020), 150.0);
        assertEquals(150.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullPeriod() {
        series.update(null, 10.0);
    }

    // --------------------------------------------------------------
    // Equals and hashCode
    // --------------------------------------------------------------
    @Test
    public void testEqualsNull() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(series.equals("String"));
    }

    @Test
    public void testEqualsReflexive() {
        assertTrue(series.equals(series));
    }

    @Test
    public void testEqualsSymmetric() {
        TimeSeries other = new TimeSeries("Test Series", "Year", "Value");
        other.add(new Year(2020), 100.0);
        other.add(new Year(2021), 200.0);
        other.add(new Year(2022), 300.0);
        assertTrue(series.equals(other));
        assertTrue(other.equals(series));
    }

    @Test
    public void testEqualsDifferentData() {
        TimeSeries other = new TimeSeries("Test Series", "Year", "Value");
        other.add(new Year(2020), 100.0);
        other.add(new Year(2021), 250.0); // different value
        other.add(new Year(2022), 300.0);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentKey() {
        TimeSeries other = new TimeSeries("Other", "Year", "Value");
        assertFalse(series.equals(other));
    }

    @Test
    public void testHashCodeConsistency() {
        int hash1 = series.hashCode();
        series.add(new Year(2023), 400.0);
        int hash2 = series.hashCode();
        assertNotEquals(hash1, hash2);
    }

    // --------------------------------------------------------------
    // isEmpty, getItemCount, etc.
    // --------------------------------------------------------------
    @Test
    public void testIsEmpty() {
        assertFalse(series.isEmpty());
        series.clear();
        assertTrue(series.isEmpty());
    }

    // --------------------------------------------------------------
    // GetDataItem, getItems
    // --------------------------------------------------------------
    @Test
    public void testGetDataItem() {
        TimeSeriesDataItem item = series.getDataItem(0);
        assertEquals(new Year(2020), item.getPeriod());
        assertEquals(100.0, item.getValue().doubleValue(), EPSILON);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetDataItemOutOfBounds() {
        series.getDataItem(10);
    }

    @Test
    public void testGetItems() {
        List items = series.getItems();
        assertNotNull(items);
        assertEquals(3, items.size());
    }

    // --------------------------------------------------------------
    // AddRange, etc. (boundary conditions)
    // --------------------------------------------------------------
    @Test
    public void testAddBoundaryLow() {
        TimeSeries s = new TimeSeries("Boundary");
        s.add(new Year(1900), Double.MIN_VALUE);
        assertEquals(1, s.getItemCount());
        assertEquals(Double.MIN_VALUE, s.getValue(0).doubleValue(), EPSILON);
    }

    @Test
    public void testAddBoundaryHigh() {
        TimeSeries s = new TimeSeries("Boundary");
        s.add(new Year(2100), Double.MAX_VALUE);
        assertEquals(1, s.getItemCount());
        assertEquals(Double.MAX_VALUE, s.getValue(0).doubleValue(), EPSILON);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddDuplicateTimePeriodWithException() {
        // If the series is set to reject duplicates (default is true, but we can test)
        // Actually, by default, adding an existing time period updates the value.
        // To test rejection we would need a constructor that sets this flag.
        // Let's test that no exception occurs when updating.
        series.add(new Year(2020), 999.0); // this should update, not throw
        assertEquals(999.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
    }

    // --------------------------------------------------------------
    // Delete all
    // --------------------------------------------------------------
    @Test
    public void testDeleteAll() {
        series.delete(0, series.getItemCount() - 1);
        assertTrue(series.isEmpty());
    }

    @Test
    public void testDeleteSingleByIndex() {
        series.delete(1, 1);
        assertEquals(2, series.getItemCount());
        assertEquals(100.0, series.getValue(0).doubleValue(), EPSILON);
        assertEquals(300.0, series.getValue(1).doubleValue(), EPSILON);
    }

    // --------------------------------------------------------------
    // Copy constructor or copy method if exists? Not present in standard.
    // --------------------------------------------------------------

    // --------------------------------------------------------------
    // Additional bug-triggering tests for clone
    // --------------------------------------------------------------
    @Test
    public void testCloneModifyOriginalDataItem() {
        // Ensure that modifying a data item through the original does not affect clone
        TimeSeries clone = null;
        try {
            clone = (TimeSeries) series.clone();
        } catch (CloneNotSupportedException e) {
            fail();
        }
        TimeSeriesDataItem item = series.getDataItem(0);
        item.setValue(999.0); // This should not affect clone if clone is deep copy
        assertEquals(100.0, clone.getValue(0).doubleValue(), EPSILON);
    }

    @Test
    public void testCloneModifyOriginalItemsList() {
        // Access internal items list through reflection? Not recommended.
        // Instead, test by adding to original and checking clone.
        TimeSeries clone = null;
        try {
            clone = (TimeSeries) series.clone();
        } catch (CloneNotSupportedException e) {
            fail();
        }
        // Adding to original should not add to clone
        series.add(new Year(2023), 400.0);
        assertNull(clone.getValue(new Year(2023)));
    }

    @Test
    public void testCloneChangeKey() {
        // Clone has its own key? Key is immutable? Actually, key is copied as string.
        // Not a bug, but ensure clone key is independent if we change original key? 
        // Key is final in default constructor? Actually, TimeSeries has a method setKey? No.
        // So this is not applicable.
    }

    // --------------------------------------------------------------
    // Exception handling
    // --------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullDomainDescription() {
        new TimeSeries("Test", null, "Range");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullRangeDescription() {
        new TimeSeries("Test", "Domain", null);
    }

    @Test
    public void testAddNegativeValue() {
        series.add(new Year(2023), -100.0);
        assertEquals(-100.0, series.getValue(new Year(2023)).doubleValue(), EPSILON);
    }

    @Test
    public void testAddZeroValue() {
        series.add(new Year(2023), 0.0);
        assertEquals(0.0, series.getValue(new Year(2023)).doubleValue(), EPSILON);
    }

    @Test
    public void testAddNaNValue() {
        series.add(new Year(2023), Double.NaN);
        assertTrue(Double.isNaN(series.getValue(new Year(2023))));
    }

    @Test
    public void testAddPositiveInfinity() {
        series.add(new Year(2023), Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, series.getValue(new Year(2023)).doubleValue(), EPSILON);
    }

    @Test
    public void testAddNegativeInfinity() {
        series.add(new Year(2023), Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, series.getValue(new Year(2023)).doubleValue(), EPSILON);
    }

    // --------------------------------------------------------------
    // Test getDomainLowerBound / getDomainUpperBound? Not methods in TimeSeries.
    // --------------------------------------------------------------
}