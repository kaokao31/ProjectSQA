package org.jfree.data.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

public class TimeSeriesTest {

    private TimeSeries timeSeries;

    @Before
    public void setUp() {
        this.timeSeries = new TimeSeries("Test Series", Year.class);
    }

    @Test
    public void testConstructorAndBasicProperties() {
        assertEquals("Test Series", timeSeries.getKey());
        assertEquals(Year.class, timeSeries.getTimePeriodClass());
        assertNull(timeSeries.getDomainDescription());
        assertNull(timeSeries.getRangeDescription());
        assertEquals(0, timeSeries.getItemCount());
        assertTrue(timeSeries.getItems().isEmpty());
        assertEquals(Integer.MAX_VALUE, timeSeries.getMaximumItemCount());
        assertEquals(Long.MAX_VALUE, timeSeries.getMaximumItemAge());
    }

    @Test
    public void testSettersAndGetters() {
        timeSeries.setDomainDescription("Domain Desc");
        assertEquals("Domain Desc", timeSeries.getDomainDescription());

        timeSeries.setRangeDescription("Range Desc");
        assertEquals("Range Desc", timeSeries.getRangeDescription());

        timeSeries.setMaximumItemCount(10);
        assertEquals(10, timeSeries.getMaximumItemCount());

        timeSeries.setMaximumItemAge(5L);
        assertEquals(5L, timeSeries.getMaximumItemAge());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullKey() {
        new TimeSeries(null, Year.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullClass() {
        new TimeSeries("Key", null);
    }

    @Test
    public void testAddAndGetItem() {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 100.0);
        assertEquals(1, timeSeries.getItemCount());
        assertEquals(100.0, timeSeries.getValue(0).doubleValue(), 0.0001);
        assertEquals(y2000, timeSeries.getTimePeriod(0));
        assertEquals(100.0, timeSeries.getValue(y2000).doubleValue(), 0.0001);

        TimeSeriesDataItem item = timeSeries.getDataItem(0);
        assertNotNull(item);
        assertEquals(100.0, item.getValue().doubleValue(), 0.0001);
        assertEquals(y2000, item.getPeriod());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPeriod() {
        timeSeries.add(null, 10.0);
    }

    @Test
    public void testAddOrUpdate() {
        Year y2000 = new Year(2000);
        timeSeries.addOrUpdate(y2000, 100.0);
        assertEquals(1, timeSeries.getItemCount());
        assertEquals(100.0, timeSeries.getValue(y2000).doubleValue(), 0.0001);

        // Update existing
        timeSeries.addOrUpdate(y2000, 200.0);
        assertEquals(1, timeSeries.getItemCount());
        assertEquals(200.0, timeSeries.getValue(y2000).doubleValue(), 0.0001);

        // Add new
        Year y2001 = new Year(2001);
        timeSeries.addOrUpdate(y2001, 300.0);
        assertEquals(2, timeSeries.getItemCount());
        assertEquals(300.0, timeSeries.getValue(y2001).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullPeriod() {
        timeSeries.addOrUpdate(null, 50.0);
    }

    @Test
    public void testAddOrUpdateWithNumber() {
        Year y2000 = new Year(2000);
        timeSeries.addOrUpdate(y2000, (Number) 150.0);
        assertEquals(1, timeSeries.getItemCount());
        assertEquals(150.0, timeSeries.getValue(y2000).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddOrUpdateNullPeriodNumber() {
        timeSeries.addOrUpdate(null, (Number) 150.0);
    }

    @Test
    public void testDelete() {
        Year y2000 = new Year(2000);
        Year y2001 = new Year(2001);
        Year y2002 = new Year(2002);

        timeSeries.add(y2000, 10.0);
        timeSeries.add(y2001, 20.0);
        timeSeries.add(y2002, 30.0);

        assertEquals(3, timeSeries.getItemCount());

        timeSeries.delete(y2001);
        assertEquals(2, timeSeries.getItemCount());
        assertNull(timeSeries.getValue(y2001));
        assertEquals(10.0, timeSeries.getValue(y2000).doubleValue(), 0.0001);
        assertEquals(30.0, timeSeries.getValue(y2002).doubleValue(), 0.0001);

        timeSeries.delete(0, 1);
        assertEquals(0, timeSeries.getItemCount());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testDeleteRangeInvalidStart() {
        timeSeries.delete(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteRangeInvalidEnd() {
        timeSeries.delete(1, 0);
    }

    @Test
    public void testUpdateByIndex() {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 10.0);
        timeSeries.update(0, 50.0);
        assertEquals(50.0, timeSeries.getValue(0).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateByIndexOutOfBounds() {
        timeSeries.update(99, 50.0);
    }

    @Test
    public void testUpdateByPeriod() {
        Year y2000 = new Year(2000);
        Year y2001 = new Year(2001);
        timeSeries.add(y2000, 10.0);
        timeSeries.update(y2000, 75.0);
        assertEquals(75.0, timeSeries.getValue(y2000).doubleValue(), 0.0001);

        // Period not in series should throw exception
        boolean thrown = false;
        try {
            timeSeries.update(y2001, 80.0);
        } catch (SeriesException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateNullPeriod() {
        timeSeries.update(null, 10.0);
    }

    @Test
    public void testAddTimeSeries() throws CloneNotSupportedException {
        TimeSeries s1 = new TimeSeries("S1", Year.class);
        s1.add(new Year(2000), 10.0);
        s1.add(new Year(2001), 20.0);

        TimeSeries s2 = new TimeSeries("S2", Year.class);
        s2.add(new Year(2001), 25.0);
        s2.add(new Year(2002), 30.0);

        s1.addAndOrUpdate(s2);
        assertEquals(3, s1.getItemCount());
        assertEquals(10.0, s1.getValue(new Year(2000)).doubleValue(), 0.0001);
        assertEquals(25.0, s1.getValue(new Year(2001)).doubleValue(), 0.0001);
        assertEquals(30.0, s1.getValue(new Year(2002)).doubleValue(), 0.0001);
    }

    @Test
    public void testCreateCopy() throws CloneNotSupportedException {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 100.0);
        timeSeries.setDomainDescription("Domain");

        TimeSeries copy = (TimeSeries) timeSeries.createCopy(0, 0);
        assertEquals(timeSeries.getKey(), copy.getKey());
        assertEquals(timeSeries.getTimePeriodClass(), copy.getTimePeriodClass());
        assertEquals(timeSeries.getDomainDescription(), copy.getDomainDescription());
        assertEquals(1, copy.getItemCount());
        assertEquals(100.0, copy.getValue(0).doubleValue(), 0.0001);
    }

    @Test(expected = CloneNotSupportedException.class)
    public void testCreateCopyInvalidRange() throws CloneNotSupportedException {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 100.0);
        timeSeries.createCopy(1, 0);
    }

    @Test
    public void testCreateCopyByPeriods() throws CloneNotSupportedException {
        Year y2000 = new Year(2000);
        Year y2001 = new Year(2001);
        Year y2002 = new Year(2002);

        timeSeries.add(y2000, 10.0);
        timeSeries.add(y2001, 20.0);
        timeSeries.add(y2002, 30.0);

        TimeSeries copy = timeSeries.createCopy(y2000, y2001);
        assertEquals(2, copy.getItemCount());
        assertEquals(10.0, copy.getValue(y2000).doubleValue(), 0.0001);
        assertEquals(20.0, copy.getValue(y2001).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNullStartPeriod() throws CloneNotSupportedException {
        timeSeries.createCopy(null, new Year(2001));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyNullEndPeriod() throws CloneNotSupportedException {
        timeSeries.createCopy(new Year(2000), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyStartAfterEndPeriod() throws CloneNotSupportedException {
        timeSeries.createCopy(new Year(2002), new Year(2000));
    }

    @Test
    public void testEqualsAndHashCode() {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 10.0);

        TimeSeries ts2 = new TimeSeries("Test Series", Year.class);
        ts2.add(y2000, 10.0);

        assertTrue(timeSeries.equals(ts2));
        assertEquals(timeSeries.hashCode(), ts2.hashCode());

        ts2.setDomainDescription("Different");
        assertFalse(timeSeries.equals(ts2));

        assertFalse(timeSeries.equals(null));
        assertFalse(timeSeries.equals("Not A Time Series"));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 10.0);
        timeSeries.setDomainDescription("Domain");

        TimeSeries clone = (TimeSeries) timeSeries.clone();
        assertNotSame(timeSeries, clone);
        assertEquals(timeSeries, clone);
    }

    @Test
    public void testMaximumItemCountEnforcement() {
        timeSeries.setMaximumItemCount(2);
        timeSeries.add(new Year(2000), 1.0);
        timeSeries.add(new Year(2001), 2.0);
        timeSeries.add(new Year(2002), 3.0); // Should drop 2000

        assertEquals(2, timeSeries.getItemCount());
        assertNull(timeSeries.getValue(new Year(2000)));
        assertEquals(2.0, timeSeries.getValue(new Year(2001)).doubleValue(), 0.0001);
        assertEquals(3.0, timeSeries.getValue(new Year(2002)).doubleValue(), 0.0001);
    }

    @Test
    public void testFindPeriodIndex() {
        Year y2000 = new Year(2000);
        Year y2002 = new Year(2002);
        Year y2001 = new Year(2001);
        Year y1999 = new Year(1999);

        timeSeries.add(y2000, 1.0);
        timeSeries.add(y2002, 2.0);

        assertEquals(0, timeSeries.getIndex(y2000));
        assertEquals(1, timeSeries.getIndex(y2002));
        assertEquals(-2, timeSeries.getIndex(y2001)); // Insertion point index encoding
        assertEquals(-1, timeSeries.getIndex(y1999));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindNullPeriodIndex() {
        timeSeries.getIndex(null);
    }

    @Test
    public void testGetValues() {
        Year y2000 = new Year(2000);
        Year y2001 = new Year(2001);
        timeSeries.add(y2000, 10.0);
        timeSeries.add(y2001, 20.0);

        List<TimeSeriesDataItem> values = timeSeries.getItems();
        assertNotNull(values);
        assertEquals(2, values.size());
    }

    @Test
    public void testAddDuplicatePeriodThrowsException() {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 10.0);
        boolean thrown = false;
        try {
            timeSeries.add(y2000, 20.0);
        } catch (SeriesException e) {
            thrown = true;
        }
        assertTrue(thrown);
    }

    @Test
    public void testAddDuplicatePeriodAllowFlag() {
        TimeSeries ts = new TimeSeries("Custom", Year.class);
        Year y2000 = new Year(2000);
        ts.add(y2000, 10.0);
        // Depending on constructor or methods, testing standard add behavior
        assertEquals(1, ts.getItemCount());
    }

    @Test
    public void testClear() {
        timeSeries.add(new Year(2000), 10.0);
        timeSeries.add(new Year(2001), 20.0);
        assertEquals(2, timeSeries.getItemCount());

        timeSeries.clear();
        assertEquals(0, timeSeries.getItemCount());
        assertTrue(timeSeries.getItems().isEmpty());
    }

    @Test
    public void testAsPrimitiveArrayMethods() {
        Year y2000 = new Year(2000);
        timeSeries.add(y2000, 123.45);

        double[] values = timeSeries.toValuesArray();
        assertNotNull(values);
        assertEquals(1, values.length);
        assertEquals(123.45, values[0], 0.0001);
    }

    @Test
    public void testSetKey() {
        timeSeries.setKey("New Key");
        assertEquals("New Key", timeSeries.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetKeyNull() {
        timeSeries.setKey(null);
    }
}