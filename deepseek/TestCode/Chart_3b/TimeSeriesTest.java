package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.util.Date;

/**
 * JUnit 4 test suite for TimeSeries class.
 * Designed for maximum coverage and fault detection.
 */
public class TimeSeriesTest {

    private TimeSeries series;
    private TimeSeries emptySeries;
    private TimePeriod period1;
    private TimePeriod period2;
    private TimePeriod period3;
    private TimePeriod duplicatePeriod;

    @Before
    public void setUp() {
        series = new TimeSeries("Test Series");
        emptySeries = new TimeSeries("Empty");
        // Create specific time periods for testing
        period1 = new Day(1, 1, 2020);
        period2 = new Day(2, 1, 2020);
        period3 = new Day(3, 1, 2020);
        duplicatePeriod = new Day(1, 1, 2020); // same as period1
        series.add(period1, 10.0);
        series.add(period2, 20.0);
        series.add(period3, 30.0);
    }

    // ---------- Constructor Tests ----------
    @Test
    public void testConstructorNonNullName() {
        TimeSeries ts = new TimeSeries("Name");
        assertNotNull(ts);
        assertEquals("Name", ts.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullName() {
        new TimeSeries(null);
    }

    // ---------- add(TimePeriod, double) Tests ----------
    @Test
    public void testAddNewItem() {
        TimePeriod newPeriod = new Day(4, 1, 2020);
        series.add(newPeriod, 40.0);
        assertEquals(4, series.getItemCount());
        assertEquals(40.0, series.getValue(newPeriod), 0.001);
    }

    @Test
    public void testAddDuplicatePeriod() {
        // Adding a duplicate period (same as period1) should update the value, not add a new item
        series.add(duplicatePeriod, 100.0);
        assertEquals(3, series.getItemCount()); // item count unchanged
        assertEquals(100.0, series.getValue(period1), 0.001); // value updated
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPeriod() {
        series.add(null, 5.0);
    }

    @Test
    public void testAddNullPeriodWithWarning() {
        // Some implementations return boolean false or log warning; here expect exception
        try {
            series.add(null, 5.0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ---------- getValue(TimePeriod) Tests ----------
    @Test
    public void testGetValueExistingPeriod() {
        assertEquals(10.0, series.getValue(period1), 0.001);
    }

    @Test
    public void testGetValueNonExistingPeriod() {
        TimePeriod nonExistent = new Day(10, 6, 2020);
        assertNull(series.getValue(nonExistent));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueNullPeriod() {
        series.getValue(null);
    }

    // ---------- getDataItem(TimePeriod) Tests ----------
    @Test
    public void testGetDataItemExistingPeriod() {
        TimeSeriesDataItem item = series.getDataItem(period1);
        assertNotNull(item);
        assertEquals(period1, item.getPeriod());
    }

    @Test
    public void testGetDataItemNonExistingPeriod() {
        TimePeriod nonExistent = new Day(10, 6, 2020);
        assertNull(series.getDataItem(nonExistent));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDataItemNullPeriod() {
        series.getDataItem(null);
    }

    // ---------- getItemCount() Tests ----------
    @Test
    public void testItemCountNonEmpty() {
        assertEquals(3, series.getItemCount());
    }

    @Test
    public void testItemCountEmpty() {
        assertEquals(0, emptySeries.getItemCount());
    }

    // ---------- remove(TimePeriod) Tests ----------
    @Test
    public void testRemoveExistingPeriod() {
        assertTrue(series.remove(period1));
        assertEquals(2, series.getItemCount());
    }

    @Test
    public void testRemoveNonExistingPeriod() {
        TimePeriod nonExistent = new Day(10, 6, 2020);
        assertFalse(series.remove(nonExistent));
        assertEquals(3, series.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveNullPeriod() {
        series.remove(null);
    }

    // ---------- removeAll() Tests ----------
    @Test
    public void testRemoveAllNonEmpty() {
        series.removeAll();
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testRemoveAllEmpty() {
        emptySeries.removeAll();
        assertEquals(0, emptySeries.getItemCount());
    }

    // ---------- clone() Tests ----------
    @Test
    public void testClone() throws CloneNotSupportedException {
        TimeSeries cloned = (TimeSeries) series.clone();
        assertNotNull(cloned);
        assertEquals(series.getItemCount(), cloned.getItemCount());
        // Ensure deep copy: modify original should not affect clone
        series.add(new Day(4, 1, 2020), 40.0);
        assertNotEquals(series.getItemCount(), cloned.getItemCount());
    }

    // ---------- equals() Tests ----------
    @Test
    public void testEqualsSameSeries() {
        assertTrue(series.equals(series));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(series.equals("SomeString"));
    }

    @Test
    public void testEqualsIdenticalContent() {
        TimeSeries other = new TimeSeries("Test Series");
        other.add(period1, 10.0);
        other.add(period2, 20.0);
        other.add(period3, 30.0);
        assertTrue(series.equals(other));
    }

    @Test
    public void testEqualsDifferentName() {
        TimeSeries other = new TimeSeries("Different Name");
        other.add(period1, 10.0);
        other.add(period2, 20.0);
        other.add(period3, 30.0);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentValues() {
        TimeSeries other = new TimeSeries("Test Series");
        other.add(period1, 999.0);
        other.add(period2, 20.0);
        other.add(period3, 30.0);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentSize() {
        TimeSeries other = new TimeSeries("Test Series");
        other.add(period1, 10.0);
        assertFalse(series.equals(other));
    }

    // ---------- hashcode() Tests ----------
    @Test
    public void testHashCodeConsistency() {
        int hash1 = series.hashCode();
        int hash2 = series.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    public void testHashCodeEqualsContract() {
        TimeSeries other = new TimeSeries("Test Series");
        other.add(period1, 10.0);
        other.add(period2, 20.0);
        other.add(period3, 30.0);
        assertEquals(series.hashCode(), other.hashCode());
    }

    // ---------- isEmpty() Tests ----------
    @Test
    public void testIsEmptyNonEmpty() {
        assertFalse(series.isEmpty());
    }

    @Test
    public void testIsEmptyEmpty() {
        assertTrue(emptySeries.isEmpty());
    }

    // ---------- getItem(int) Tests ----------
    @Test
    public void testGetItemValidIndex() {
        TimeSeriesDataItem item = series.getDataItem(0);
        assertNotNull(item);
        assertEquals(period1, item.getPeriod());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetItemNegativeIndex() {
        series.getDataItem(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetItemTooLargeIndex() {
        series.getDataItem(series.getItemCount()); // out of bounds
    }

    // ---------- setItem(TimePeriod, double) Tests ----------
    @Test
    public void testSetItemExistingPeriod() {
        assertTrue(series.setItem(period1, 999.0));
        assertEquals(999.0, series.getValue(period1), 0.001);
    }

    @Test
    public void testSetItemNewPeriod() {
        TimePeriod newPeriod = new Day(4, 1, 2020);
        assertTrue(series.setItem(newPeriod, 40.0));
        assertEquals(4, series.getItemCount());
        assertEquals(40.0, series.getValue(newPeriod), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetItemNullPeriod() {
        series.setItem(null, 5.0);
    }

    // ---------- addOrUpdate(TimePeriod, double) ----------
    @Test
    public void testAddOrUpdateNew() {
        TimePeriod newPeriod = new Day(4, 1, 2020);
        TimeSeriesDataItem result = series.addOrUpdate(newPeriod, 40.0);
        assertEquals(4, series.getItemCount());
        assertEquals(40.0, series.getValue(newPeriod), 0.001);
        assertNull(result); // new item added
    }

    @Test
    public void testAddOrUpdateExisting() {
        TimeSeriesDataItem result = series.addOrUpdate(period1, 100.0);
        assertEquals(3, series.getItemCount()); // count unchanged
        assertEquals(100.0, series.getValue(period1), 0.001);
        assertNotNull(result); // old item returned
    }

    // ---------- getTimePeriods() Tests ----------
    @Test
    public void testGetTimePeriods() {
        java.util.List periods = series.getTimePeriods();
        assertNotNull(periods);
        assertEquals(3, periods.size());
        assertTrue(periods.contains(period1));
    }

    // ---------- getMinPeriod() / getMaxPeriod() Tests ----------
    @Test
    public void testGetMinPeriod() {
        assertEquals(period1, series.getMinPeriod());
    }

    @Test
    public void testGetMaxPeriod() {
        assertEquals(period3, series.getMaxPeriod());
    }

    @Test
    public void testGetMinPeriodEmpty() {
        assertNull(emptySeries.getMinPeriod());
    }

    @Test
    public void testGetMaxPeriodEmpty() {
        assertNull(emptySeries.getMaxPeriod());
    }

    // ---------- getMinValue() / getMaxValue() Tests ----------
    @Test
    public void testGetMinValue() {
        assertEquals(10.0, series.getMinValue(), 0.001);
    }

    @Test
    public void testGetMaxValue() {
        assertEquals(30.0, series.getMaxValue(), 0.001);
    }

    @Test
    public void testGetMinValueEmpty() {
        assertNull(emptySeries.getMinValue());
    }

    @Test
    public void testGetMaxValueEmpty() {
        assertNull(emptySeries.getMaxValue());
    }

    // ---------- toArray() Tests ----------
    @Test
    public void testToArray() {
        TimeSeriesDataItem[] array = series.toArray();
        assertNotNull(array);
        assertEquals(3, array.length);
        assertEquals(10.0, array[0].getValue(), 0.001);
    }

    // ---------- toString() Tests ----------
    @Test
    public void testToString() {
        String str = series.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test Series"));
    }

    // ---------- Bug-Trigerring Stress Tests ----------
    // Edge cases with extreme values
    @Test
    public void testAddLargeValue() {
        TimePeriod p = new Day(1, 1, 2021);
        series.add(p, Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, series.getValue(p), 0.001);
    }

    @Test
    public void testAddNegativeValue() {
        TimePeriod p = new Day(1, 1, 2021);
        series.add(p, -999.99);
        assertEquals(-999.99, series.getValue(p), 0.001);
    }

    // Adding items out of chronological order tests a common bug
    @Test
    public void testAddOutOfOrder() {
        TimePeriod later = new Day(10, 1, 2020);
        TimePeriod earlier = new Day(5, 1, 2020);
        // Add later first, then earlier - some implementations expect sorted order
        series.add(later, 50.0);
        series.add(earlier, 5.0);
        // Verify that the series retains correct order
        assertEquals(5, series.getItemCount());
        // Expected order: period1, period2, period3, earlier, later? depends on implementation
        // This could trigger a bug if internal structure is not sorted
        assertTrue(series.getTimePeriod(0).compareTo(series.getTimePeriod(1)) <= 0);
    }

    // Test clone independence (deep copy)
    @Test
    public void testCloneDeepCopy() throws CloneNotSupportedException {
        TimeSeries cloned = (TimeSeries) series.clone();
        // Modify original's item
        series.add(period1, 999.0);
        // Cloned must not be affected
        assertEquals(10.0, cloned.getValue(period1), 0.001);
    }

    // Test removing middle element
    @Test
    public void testRemoveMiddle() {
        series.remove(period2);
        assertEquals(2, series.getItemCount());
        assertEquals(period1, series.getDataItem(0).getPeriod());
        assertEquals(period3, series.getDataItem(1).getPeriod());
    }
}