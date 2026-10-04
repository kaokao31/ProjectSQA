package org.jfree.data.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;

import java.util.Date;
import java.util.TimeZone;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TimeSeriesTest {

    private TimeSeries series;

    @Before
    public void setUp() {
        this.series = new TimeSeries("Test Series", Year.class);
    }

    @Test
    public void testConstructorAndDefaults() {
        assertEquals("Test Series", series.getKey());
        assertEquals(Year.class, series.getTimePeriodClass());
        assertEquals(0, series.getItemCount());
        assertNotNull(series.getTimePeriods());
        assertNotNull(series.getItems());
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemAge());
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
        Year y2020 = new Year(2020);
        series.add(y2020, 100.0);
        assertEquals(1, series.getItemCount());
        assertEquals(100.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(y2020, series.getTimePeriod(0));
        assertEquals(0, series.getIndex(y2020));

        Year y2021 = new Year(2021);
        series.add(y2021, 200.0);
        assertEquals(2, series.getItemCount());
        assertEquals(1, series.getIndex(y2021));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNullPeriod() {
        series.add(null, 50.0);
    }

    @Test
    public void testAddOrUpdate() {
        Year y2020 = new Year(2020);
        series.addOrUpdate(y2020, 10.0);
        assertEquals(1, series.getItemCount());
        assertEquals(10.0, series.getValue(0).doubleValue(), 0.0001);

        // Update existing
        series.addOrUpdate(y2020, 20.0);
        assertEquals(1, series.getItemCount());
        assertEquals(20.0, series.getValue(0).doubleValue(), 0.0001);

        // Add new
        Year y2021 = new Year(2021);
        series.addOrUpdate(y2021, 30.0);
        assertEquals(2, series.getItemCount());
        assertEquals(30.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testUpdateByIndex() {
        Year y2020 = new Year(2020);
        series.add(y2020, 10.0);
        series.update(0, 50.0);
        assertEquals(50.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testUpdateInvalidIndex() {
        series.update(0, 50.0);
    }

    @Test
    public void testUpdateByPeriod() {
        Year y2020 = new Year(2020);
        series.add(y2020, 10.0);
        series.update(y2020, 75.0);
        assertEquals(75.0, series.getValue(0).doubleValue(), 0.0001);
    }

    @Test(expected = SeriesException.class)
    public void testUpdatePeriodNotFound() {
        Year y2020 = new Year(2020);
        Year y2021 = new Year(2021);
        series.add(y2020, 10.0);
        series.update(y2021, 75.0);
    }

    @Test
    public void testDelete() {
        Year y2020 = new Year(2020);
        Year y2021 = new Year(2021);
        Year y2022 = new Year(2022);
        
        series.add(y2020, 10.0);
        series.add(y2021, 20.0);
        series.add(y2022, 30.0);

        series.delete(1);
        assertEquals(2, series.getItemCount());
        assertEquals(y2022, series.getTimePeriod(1));
        assertEquals(30.0, series.getValue(1).doubleValue(), 0.0001);

        series.delete(y2020);
        assertEquals(1, series.getItemCount());
        assertEquals(y2022, series.getTimePeriod(0));
    }

    @Test
    public void testClear() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 20.0);
        assertEquals(2, series.getItemCount());
        
        series.clear();
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testMaximumItemCount() {
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());

        series.add(new Year(2018), 1.0);
        series.add(new Year(2019), 2.0);
        series.add(new Year(2020), 3.0); // Should drop 2018

        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2019), series.getTimePeriod(0));
        assertEquals(new Year(2020), series.getTimePeriod(1));
    }

    @Test
    public void testMaximumItemAge() {
        series.setMaximumItemAge(1L);
        assertEquals(1L, series.getMaximumItemAge());

        series.add(new Year(2018), 1.0);
        series.add(new Year(2019), 2.0);
        series.add(new Year(2020), 3.0); // Age relative to 2020, max age 1 means keep 2019 and 2020

        assertEquals(2, series.getItemCount());
        assertEquals(new Year(2019), series.getTimePeriod(0));
        assertEquals(new Year(2020), series.getTimePeriod(1));
    }

    @Test
    public void testAddSeries() throws SeriesException {
        TimeSeries s2 = new TimeSeries("S2", Year.class);
        s2.add(new Year(2020), 100.0);
        s2.add(new Year(2021), 200.0);

        series.add(s2);
        assertEquals(2, series.getItemCount());
        assertEquals(100.0, series.getValue(new Year(2020)).doubleValue(), 0.0001);
        assertEquals(200.0, series.getValue(new Year(2021)).doubleValue(), 0.0001);
    }

    @Test
    public void testCreateCopyRange() throws CloneNotSupportedException {
        series.add(new Year(2018), 10.0);
        series.add(new Year(2019), 20.0);
        series.add(new Year(2020), 30.0);

        TimeSeries copy = series.createCopy(new Year(2019), new Year(2020));
        assertEquals(2, copy.getItemCount());
        assertEquals(new Year(2019), copy.getTimePeriod(0));
        assertEquals(new Year(2020), copy.getTimePeriod(1));
        assertEquals(20.0, copy.getValue(0).doubleValue(), 0.0001);
        assertEquals(30.0, copy.getValue(1).doubleValue(), 0.0001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateCopyInvalidRange() throws CloneNotSupportedException {
        series.createCopy(new Year(2020), new Year(2019));
    }

    @Test
    public void testEqualsAndHashCode() {
        series.add(new Year(2020), 10.0);

        TimeSeries series2 = new TimeSeries("Test Series", Year.class);
        series2.add(new Year(2020), 10.0);

        assertTrue(series.equals(series2));
        assertEquals(series.hashCode(), series2.hashCode());

        series2.add(new Year(2021), 20.0);
        assertFalse(series.equals(series2));
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        series.add(new Year(2020), 10.0);
        TimeSeries clone = (TimeSeries) series.clone();
        
        assertNotNull(clone);
        assertEquals(series, clone);
        assertTrue(series != clone);
    }

    @Test
    public void testGetMinMaxXAndY() {
        series.add(new Year(2020), 10.0);
        series.add(new Year(2021), 30.0);
        series.add(new Year(2022), 20.0);

        assertEquals(10.0, series.getMinY().doubleValue(), 0.0001);
        assertEquals(30.0, series.getMaxY().doubleValue(), 0.0001);
    }

    @Test
    public void testEmptySeriesMinMax() {
        assertNull(series.getMinY());
        assertNull(series.getMaxY());
    }

    @Test
    public void testFindPrimaryIndex() {
        series.add(new Year(2010), 1.0);
        series.add(new Year(2012), 2.0);
        series.add(new Year(2014), 3.0);

        // Exact match
        assertEquals(1, series.getIndex(new Year(2012)));

        // Non-existent match depending on implementation (usually returns negative insertion point or similar)
        int idx = series.getIndex(new Year(2011));
        assertTrue(idx < 0);
    }

    @Test
    public void testSetDomainDescriptionAndRangeDescription() {
        series.setDomainDescription("Domain");
        assertEquals("Domain", series.getDomainDescription());

        series.setRangeDescription("Range");
        assertEquals("Range", series.getRangeDescription());
    }

    @Test
    public void testAddValueOverloads() {
        series.add(new Year(2020), (Number) 55.0);
        assertEquals(55.0, series.getValue(0).doubleValue(), 0.0001);
        
        series.add(new Year(2021), 66.0);
        assertEquals(66.0, series.getValue(1).doubleValue(), 0.0001);

        series.add(new Year(2022), 77.0, false);
        assertEquals(77.0, series.getValue(2).doubleValue(), 0.0001);

        series.add(new Year(2023), (Number) 88.0, false);
        assertEquals(88.0, series.getValue(3).doubleValue(), 0.0001);
    }
}