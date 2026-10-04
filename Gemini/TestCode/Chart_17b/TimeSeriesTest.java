package org.jfree.data.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Date;
import java.util.List;

import org.junit.Test;

public class TimeSeriesTest {

    @Test
    public void testCloneAndCreateCopyBug17() throws Exception {
        // Specifically targeting the clone/createCopy bug in TimeSeries (Chart 17)
        // where cloning a sub-range (or empty range) might cause IndexOutOfBoundsException 
        // or incorrect indices when adding/copying items.
        TimeSeries series = new TimeSeries("Test Series", Year.class);
        
        // Add some data
        series.add(new Year(2000), 100.0);
        series.add(new Year(2001), 200.0);
        series.add(new Year(2002), 300.0);
        series.add(new Year(2003), 400.0);
        series.add(new Year(2004), 500.0);

        // Test createCopy with valid bounds
        TimeSeries copy = series.createCopy(2, 4);
        assertNotNull(copy);
        assertEquals(3, copy.getItemCount());
        assertEquals(new Year(2002), copy.getTimePeriod(0));
        assertEquals(new Year(2004), copy.getTimePeriod(2));

        // Test createCopy with a single item range where start == end
        TimeSeries singleCopy = series.createCopy(2, 2);
        assertNotNull(singleCopy);
        assertEquals(1, singleCopy.getItemCount());
        assertEquals(new Year(2002), singleCopy.getTimePeriod(0));

        // Test createCopy with empty range / edge cases that often trigger Chart 17
        // If start > end, it might throw IllegalArgumentException or handle incorrectly depending on implementation.
        // Let's check standard behavior or trigger the specific branch where data.subList is called.
        try {
            series.createCopy(4, 2);
        } catch (IllegalArgumentException e) {
            // Expected if start > end in certain versions
        }

        // Clone test
        TimeSeries cloned = (TimeSeries) series.clone();
        assertNotNull(cloned);
        assertEquals(series.getItemCount(), cloned.getItemCount());
        assertNotSame(series, cloned);
    }

    @Test
    public void testBasicConstructorsAndMethods() {
        TimeSeries series = new TimeSeries("Series", Day.class);
        assertEquals("Series", series.getKey());
        assertEquals(Day.class, series.getTimePeriodClass());
        assertEquals(0, series.getItemCount());

        series.add(new Day(1, 1, 2020), 10.5);
        assertEquals(1, series.getItemCount());
        assertEquals(10.5, series.getValue(0).doubleValue(), 0.0001);

        series.update(0, 20.0);
        assertEquals(20.0, series.getValue(0).doubleValue(), 0.0001);

        series.delete(0);
        assertEquals(0, series.getItemCount());
    }

    @Test
    public void testAddOrUpdate() {
        TimeSeries series = new TimeSeries("Series", Month.class);
        Month m1 = new Month(1, 2020);
        Month m2 = new Month(2, 2020);

        series.addOrUpdate(m1, 100.0);
        assertEquals(1, series.getItemCount());
        
        // Update existing
        series.addOrUpdate(m1, 150.0);
        assertEquals(1, series.getItemCount());
        assertEquals(150.0, series.getValue(0).doubleValue(), 0.0001);

        // Add new
        series.addOrUpdate(m2, 200.0);
        assertEquals(2, series.getItemCount());
    }

    @Test
    public void testDeleteRange() {
        TimeSeries series = new TimeSeries("Series", Year.class);
        series.add(new Year(2010), 10.0);
        series.add(new Year(2011), 20.0);
        series.add(new Year(2012), 30.0);
        series.add(new Year(2013), 40.0);

        series.delete(2011, 2012);
        // Depending on implementation, delete(start, end) might use TimePeriod or indices.
        // TimeSeries has delete(int, int) for indices and delete(RegularTimePeriod, RegularTimePeriod) for periods.
        // Let's test index-based delete range if available, or period-based.
        // TimeSeries.delete(RegularTimePeriod, RegularTimePeriod) is standard.
        
        TimeSeries series2 = new TimeSeries("Series2", Year.class);
        series2.add(new Year(2010), 10.0);
        series2.add(new Year(2011), 20.0);
        series2.add(new Year(2012), 30.0);
        series2.add(new Year(2013), 40.0);
        
        series2.delete(new Year(2011), new Year(2012));
        assertEquals(2, series2.getItemCount());
        assertEquals(new Year(2010), series2.getTimePeriod(0));
        assertEquals(new Year(2013), series2.getTimePeriod(1));
    }

    @Test
    public void testMaximumItemCount() {
        TimeSeries series = new TimeSeries("Series", Millisecond.class);
        assertEquals(Integer.MAX_VALUE, series.getMaximumItemCount());
        
        series.setMaximumItemCount(2);
        assertEquals(2, series.getMaximumItemCount());

        series.add(new Millisecond(1L), 1.0);
        series.add(new Millisecond(2L), 2.0);
        series.add(new Millisecond(3L), 3.0);

        // Should have dropped the first one due to max item count = 2
        assertEquals(2, series.getItemCount());
        assertEquals(2.0, series.getValue(0).doubleValue(), 0.0001);
        assertEquals(3.0, series.getValue(1).doubleValue(), 0.0001);
    }

    @Test
    public void testFindValueIndex() {
        TimeSeries series = new TimeSeries("Series", Integer.class);
        // TimeSeries requires a subclass of RegularTimePeriod, let's use Day
        TimeSeries daySeries = new TimeSeries("DaySeries", Day.class);
        Day d1 = new Day(1, 1, 2021);
        Day d2 = new Day(3, 1, 2021);
        
        daySeries.add(d1, 1.0);
        daySeries.add(d2, 3.0);

        assertEquals(0, daySeries.getIndex(d1));
        // Index for a period not present should be negative (insertion index info)
        Day d1_half = new Day(2, 1, 2021);
        assertTrue(daySeries.getIndex(d1_half) < 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        TimeSeries s1 = new TimeSeries("Series", Day.class);
        TimeSeries s2 = new TimeSeries("Series", Day.class);

        assertTrue(s1.equals(s2));
        assertEquals(s1.hashCode(), s2.hashCode());

        s1.add(new Day(1, 1, 2020), 5.0);
        assertFalse(s1.equals(s2));

        s2.add(new Day(1, 1, 2020), 5.0);
        assertTrue(s1.equals(s2));
    }
}