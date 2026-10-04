package org.jfree.data.time;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit 4 test suite for TimeSeries.
 * Designed to achieve high coverage and detect common faults.
 */
public class TimeSeriesTest {

    private TimeSeries series;
    private TimeSeries emptySeries;
    private static final double EPSILON = 0.0000001;

    @Before
    public void setUp() {
        series = new TimeSeries("Test Series");
        emptySeries = new TimeSeries("Empty Series");
    }

    // ---------- Constructor Tests ----------
    @Test
    public void testConstructorWithName() {
        assertEquals("Test Series", series.getKey());
        assertNotNull(series.getItemCount());
        assertEquals(0, series.getItemCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullName() {
        new TimeSeries(null);
    }

    // ---------- add (TimePeriod, Number) ----------
    @Test
    public void testAddSingleItem() {
        Year year = new Year(2020);
        series.add(year, 100.0);
        assertEquals(1, series.getItemCount());
        assertEquals(100.0, series.getValue(year).doubleValue(), EPSILON);
    }

    @Test
    public void testAddDuplicatePeriodUpdatesValue() {
        Year year = new Year(2020);
        series.add(year, 100.0);
        series.add(year, 200.0);
        assertEquals(1, series.getItemCount());
        assertEquals(200.0, series.getValue(year).doubleValue(), EPSILON);
    }

    @Test
    public void testAddNullValue() {
        Year year = new Year(2020);
        series.add(year, null);
        assertEquals(1, series.getItemCount());
        // According to typical implementation, null values are allowed (represent missing)
        assertNull(series.getValue(year));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPeriod() {
        series.add(null, 50.0);
    }

    @Test
    public void testAddMultipleItemsSorted() {
        series.add(new Year(2020), 100.0);
        series.add(new Year(2019), 90.0);
        series.add(new Year(2021), 110.0);
        assertEquals(3, series.getItemCount());
        assertEquals(90.0, series.getValue(new Year(2019)).doubleValue(), EPSILON);
        assertEquals(100.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
        assertEquals(110.0, series.getValue(new Year(2021)).doubleValue(), EPSILON);
    }

    // ---------- add(TimeSeriesDataItem) ----------
    @Test
    public void testAddDataItem() {
        TimeSeriesDataItem item = new TimeSeriesDataItem(new Year(2020), 50.0);
        series.add(item);
        assertEquals(1, series.getItemCount());
        assertTrue(series.getItems().contains(item));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullDataItem() {
        series.add((TimeSeriesDataItem) null);
    }

    @Test
    public void testAddDuplicateDataItemUpdates() {
        TimeSeriesDataItem item1 = new TimeSeriesDataItem(new Year(2020), 50.0);
        TimeSeriesDataItem item2 = new TimeSeriesDataItem(new Year(2020), 75.0);
        series.add(item1);
        series.add(item2);
        assertEquals(1, series.getItemCount());
        assertEquals(75.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
    }

    // ---------- getItemCount, isEmpty ----------
    @Test
    public void testEmptySeriesCount() {
        assertTrue(emptySeries.isEmpty());
        assertEquals(0, emptySeries.getItemCount());
    }

    @Test
    public void testNonEmptySeriesCount() {
        series.add(new Year(2020), 1.0);
        assertFalse(series.isEmpty());
        assertEquals(1, series.getItemCount());
    }

    // ---------- getItem ----------
    @Test
    public void testGetItemValidIndex() {
        series.add(new Year(2020), 100.0);
        TimeSeriesDataItem item = series.getItem(0);
        assertNotNull(item);
        assertEquals(new Year(2020), item.getPeriod());
        assertEquals(100.0, item.getValue().doubleValue(), EPSILON);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetItemNegativeIndex() {
        emptySeries.getItem(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetItemIndexTooHigh() {
        series.add(new Year(2020), 1.0);
        series.getItem(1);
    }

    // ---------- getValue(TimeSeriesDataItem) ----------
    @Test
    public void testGetValueByPeriod() {
        series.add(new Year(2020), 200.0);
        assertEquals(200.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
    }

    @Test
    public void testGetValueForNonExistentPeriod() {
        assertNull(emptySeries.getValue(new Year(2020)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetValueWithNullPeriod() {
        series.getValue(null);
    }

    // ---------- clone ----------
    @Test
    public void testClone() throws CloneNotSupportedException {
        series.add(new Year(2020), 100.0);
        series.add(new Year(2021), 200.0);
        TimeSeries cloned = (TimeSeries) series.clone();
        assertNotNull(cloned);
        assertEquals(series.getItemCount(), cloned.getItemCount());
        assertEquals(series.getKey(), cloned.getKey());
        // Verify independence
        cloned.add(new Year(2022), 300.0);
        assertEquals(2, series.getItemCount());
        assertEquals(3, cloned.getItemCount());
    }

    @Test
    public void testCloneEmptySeries() throws CloneNotSupportedException {
        TimeSeries cloned = (TimeSeries) emptySeries.clone();
        assertNotNull(cloned);
        assertEquals(0, cloned.getItemCount());
    }

    // ---------- equals ----------
    @Test
    public void testEqualsSameObject() {
        assertTrue(series.equals(series));
    }

    @Test
    public void testEqualsNull() {
        assertFalse(series.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        assertFalse(series.equals("string"));
    }

    @Test
    public void testEqualsTwoEmptySeries() {
        TimeSeries other = new TimeSeries("Test Series");
        assertTrue(series.equals(other));
    }

    @Test
    public void testEqualsDifferentKey() {
        TimeSeries other = new TimeSeries("Other Series");
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsSameContent() {
        series.add(new Year(2020), 100.0);
        series.add(new Year(2021), 200.0);
        TimeSeries other = new TimeSeries("Test Series");
        other.add(new Year(2020), 100.0);
        other.add(new Year(2021), 200.0);
        assertTrue(series.equals(other));
    }

    @Test
    public void testEqualsDifferentValues() {
        series.add(new Year(2020), 100.0);
        TimeSeries other = new TimeSeries("Test Series");
        other.add(new Year(2020), 101.0);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsDifferentPeriods() {
        series.add(new Year(2020), 100.0);
        TimeSeries other = new TimeSeries("Test Series");
        other.add(new Year(2021), 100.0);
        assertFalse(series.equals(other));
    }

    @Test
    public void testEqualsWithNullValues() {
        series.add(new Year(2020), null);
        TimeSeries other = new TimeSeries("Test Series");
        other.add(new Year(2020), null);
        assertTrue(series.equals(other));
    }

    // ---------- hashCode ----------
    @Test
    public void testHashCodeEqualObjects() {
        series.add(new Year(2020), 100.0);
        TimeSeries other = new TimeSeries("Test Series");
        other.add(new Year(2020), 100.0);
        assertEquals(series.hashCode(), other.hashCode());
    }

    @Test
    public void testHashCodeDifferentObjects() {
        series.add(new Year(2020), 100.0);
        TimeSeries other = new TimeSeries("Test Series");
        other.add(new Year(2020), 200.0);
        assertNotEquals(series.hashCode(), other.hashCode());
    }

    // ---------- clear ----------
    @Test
    public void testClearEmptySeries() {
        emptySeries.clear();
        assertEquals(0, emptySeries.getItemCount());
    }

    @Test
    public void testClearNonEmptySeries() {
        series.add(new Year(2020), 100.0);
        series.add(new Year(2021), 200.0);
        series.clear();
        assertEquals(0, series.getItemCount());
        assertTrue(series.isEmpty());
    }

    // ---------- setMaximumItemCount ----------
    @Test
    public void testSetMaximumItemCountRemovesOldest() {
        series.setMaximumItemCount(2);
        series.add(new Year(2020), 100.0);
        series.add(new Year(2021), 200.0);
        series.add(new Year(2022), 300.0);
        assertEquals(2, series.getItemCount());
        assertNull(series.getValue(new Year(2020)));
        assertNotNull(series.getValue(new Year(2021)));
        assertNotNull(series.getValue(new Year(2022)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMaximumItemCountNegative() {
        series.setMaximumItemCount(-1);
    }

    @Test
    public void testSetMaximumItemCountZeroAllowed() {
        series.setMaximumItemCount(0);
        series.add(new Year(2020), 100.0);
        assertEquals(0, series.getItemCount());
    }

    // ---------- getTimePeriodClass ----------
    @Test
    public void testGetTimePeriodClassEmpty() {
        assertNull(emptySeries.getTimePeriodClass());
    }

    @Test
    public void testGetTimePeriodClassAfterAdd() {
        series.add(new Year(2020), 1.0);
        assertEquals(Year.class, series.getTimePeriodClass());
    }

    // ---------- hasSamePeriods ----------
    @Test
    public void testHasSamePeriodsTrue() {
        series.add(new Year(2020), 100.0);
        series.add(new Year(2021), 200.0);
        TimeSeries other = new TimeSeries("Other");
        other.add(new Year(2020), 300.0);
        other.add(new Year(2021), 400.0);
        assertTrue(series.hasSamePeriods(other));
    }

    @Test
    public void testHasSamePeriodsFalse() {
        series.add(new Year(2020), 100.0);
        TimeSeries other = new TimeSeries("Other");
        other.add(new Year(2021), 200.0);
        assertFalse(series.hasSamePeriods(other));
    }

    @Test
    public void testHasSamePeriodsEmptySeries() {
        assertTrue(emptySeries.hasSamePeriods(new TimeSeries("Other")));
    }

    // ---------- setKey ----------
    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyNull() {
        series.setKey(null);
    }

    @Test
    public void testSetKeyValid() {
        series.setKey("New Key");
        assertEquals("New Key", series.getKey());
    }

    // ---------- getItems (unmodifiable?) ----------
    @Test(expected = UnsupportedOperationException.class)
    public void testGetItemsIsUnmodifiable() {
        series.add(new Year(2020), 1.0);
        java.util.List<TimeSeriesDataItem> items = series.getItems();
        items.add(new TimeSeriesDataItem(new Year(2021), 2.0)); // should throw
    }

    // ---------- delete (range) ----------
    @Test
    public void testDeleteRange() {
        series.add(new Year(2020), 100.0);
        series.add(new Year(2021), 200.0);
        series.add(new Year(2022), 300.0);
        series.delete(0, 1);
        assertEquals(1, series.getItemCount());
        assertEquals(300.0, series.getValue(new Year(2022)).doubleValue(), EPSILON);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteRangeInvalid() {
        series.delete(0, 5);
    }

    // ---------- addOrUpdate ----------
    @Test
    public void testAddOrUpdateNewItem() {
        boolean updated = series.addOrUpdate(new Year(2020), 100.0);
        assertFalse(updated);
        assertEquals(1, series.getItemCount());
        assertEquals(100.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
    }

    @Test
    public void testAddOrUpdateExistingItem() {
        series.add(new Year(2020), 100.0);
        boolean updated = series.addOrUpdate(new Year(2020), 200.0);
        assertTrue(updated);
        assertEquals(1, series.getItemCount());
        assertEquals(200.0, series.getValue(new Year(2020)).doubleValue(), EPSILON);
    }

    // ---------- getDataItem (index) ----------
    @Test
    public void testGetDataItemByIndex() {
        series.add(new Year(2020), 10.0);
        TimeSeriesDataItem item = series.getDataItem(0);
        assertNotNull(item);
        assertEquals(new Year(2020), item.getPeriod());
    }

    // ---------- getDataItem (period) ----------
    @Test
    public void testGetDataItemByPeriod() {
        series.add(new Year(2020), 10.0);
        TimeSeriesDataItem item = series.getDataItem(new Year(2020));
        assertNotNull(item);
        assertEquals(10.0, item.getValue().doubleValue(), EPSILON);
    }

    @Test
    public void testGetDataItemByNonExistentPeriod() {
        assertNull(series.getDataItem(new Year(2020)));
    }

    // ---------- getNextTimePeriod ----------
    @Test
    public void testGetNextTimePeriod() {
        series.add(new Year(2020), 1.0);
        RegularTimePeriod next = series.getNextTimePeriod();
        assertNotNull(next);
        assertEquals(new Year(2021), next);
    }

    @Test
    public void testGetNextTimePeriodFromEmptySeries() {
        assertNull(emptySeries.getNextTimePeriod());
    }

    // ---------- getPreviousTimePeriod ----------
    @Test
    public void testGetPreviousTimePeriod() {
        series.add(new Year(2020), 1.0);
        RegularTimePeriod prev = series.getPreviousTimePeriod();
        assertNotNull(prev);
        assertEquals(new Year(2019), prev);
    }

    @Test
    public void testGetPreviousTimePeriodFromEmptySeries() {
        assertNull(emptySeries.getPreviousTimePeriod());
    }

    // ---------- getMinPeriod, getMaxPeriod ----------
    @Test
    public void testGetMinMaxPeriod() {
        series.add(new Year(2021), 1.0);
        series.add(new Year(2020), 2.0);
        series.add(new Year(2022), 3.0);
        assertEquals(new Year(2020), series.getMinPeriod());
        assertEquals(new Year(2022), series.getMaxPeriod());
    }

    @Test
    public void testGetMinMaxPeriodEmpty() {
        assertNull(emptySeries.getMinPeriod());
        assertNull(emptySeries.getMaxPeriod());
    }

    // ---------- getMinValue, getMaxValue ----------
    @Test
    public void testGetMinMaxValue() {
        series.add(new Year(2020), 50.0);
        series.add(new Year(2021), 30.0);
        series.add(new Year(2022), 100.0);
        assertEquals(30.0, series.getMinValue().doubleValue(), EPSILON);
        assertEquals(100.0, series.getMaxValue().doubleValue(), EPSILON);
    }

    @Test
    public void testGetMinMaxValueWithNulls() {
        series.add(new Year(2020), null);
        series.add(new Year(2021), 10.0);
        assertEquals(10.0, series.getMinValue().doubleValue(), EPSILON);
        assertEquals(10.0, series.getMaxValue().doubleValue(), EPSILON);
    }

    @Test
    public void testGetMinMaxValueEmpty() {
        assertNull(emptySeries.getMinValue());
        assertNull(emptySeries.getMaxValue());
    }

    // ---------- addOrUpdate (with DataItem) ----------
    @Test
    public void testAddOrUpdateWithDataItemNew() {
        TimeSeriesDataItem item = new TimeSeriesDataItem(new Year(2020), 30.0);
        boolean updated = series.addOrUpdate(item);
        assertFalse(updated);
        assertEquals(1, series.getItemCount());
    }

    // ---------- copy (not always implemented) ----------
    // Skipping copy if not in class

    // ---------- FireSeriesChange events (optional) ----------
    // Could test added listener but not required for core coverage

    // ---------- toString ----------
    @Test
    public void testToString() {
        assertNotNull(series.toString());
        assertTrue(series.toString().contains("Test Series"));
    }
}