package org.jfree.data.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TimePeriodValuesTest {

    @Test
    public void testConstructorAndDefaults() {
        TimePeriodValues tpv = new TimePeriodValues("Test Series");
        assertEquals("Test Series", tpv.getKey());
        assertNull(tpv.getDomain());
        assertEquals(0, tpv.getItemCount());
        assertTrue(tpv.getMinStartIndex() < 0);
        assertTrue(tpv.getMaxStartIndex() < 0);
        assertTrue(tpv.getMinEndIndex() < 0);
        assertTrue(tpv.getMaxEndIndex() < 0);
        assertTrue(tpv.getMinMiddleIndex() < 0);
        assertTrue(tpv.getMaxMiddleIndex() < 0);
    }

    @Test
    public void testAddAndGet() {
        TimePeriodValues tpv = new TimePeriodValues("Series 1");
        
        RegularTimePeriod t1 = new Year(2020);
        RegularTimePeriod t2 = new Year(2021);
        RegularTimePeriod t3 = new Year(2022);

        tpv.add(t1, 100.0);
        tpv.add(new SimpleTimePeriod(1000L, 2000L), 200.0);
        tpv.add(t2,  null);
        tpv.add(new TimePeriodValue(t3, 300.0));

        assertEquals(4, tpv.getItemCount());
        assertEquals(t1, tpv.getTimePeriod(0));
        assertEquals(100.0, tpv.getValue(0).doubleValue(), 0.001);
        assertEquals(200.0, tpv.getValue(1).doubleValue(), 0.001);
        assertNull(tpv.getValue(2));
        assertEquals(300.0, tpv.getValue(3).doubleValue(), 0.001);

        TimePeriodValue val = tpv.getDataItem(0);
        assertNotNull(val);
        assertEquals(100.0, val.getValue().doubleValue(), 0.001);
    }

    @Test
    public void testUpdate() {
        TimePeriodValues tpv = new TimePeriodValues("Series 2");
        RegularTimePeriod t1 = new Year(2020);
        tpv.add(t1, 50.0);
        assertEquals(50.0, tpv.getValue(0).doubleValue(), 0.001);

        tpv.update(0, 150.0);
        assertEquals(150.0, tpv.getValue(0).doubleValue(), 0.001);
    }

    @Test
    public void testDelete() {
        TimePeriodValues tpv = new TimePeriodValues("Series 3");
        RegularTimePeriod t1 = new Year(2020);
        RegularTimePeriod t2 = new Year(2021);
        tpv.add(t1, 10.0);
        tpv.add(t2, 20.0);
        assertEquals(2, tpv.getItemCount());

        tpv.delete(0);
        assertEquals(1, tpv.getItemCount());
        assertEquals(t2, tpv.getTimePeriod(0));
        assertEquals(20.0, tpv.getValue(0).doubleValue(), 0.001);
    }

    @Test
    public void testAddOrUpdate() {
        TimePeriodValues tpv = new TimePeriodValues("Series 4");
        RegularTimePeriod t1 = new Year(2020);
        
        tpv.add(t1, 10.0);
        tpv.addOrUpdate(t1, 99.0);
        
        assertEquals(1, tpv.getItemCount());
        assertEquals(99.0, tpv.getValue(0).doubleValue(), 0.001);

        RegularTimePeriod t2 = new Year(2021);
        tpv.addOrUpdate(t2, 50.0);
        assertEquals(2, tpv.getItemCount());
        assertEquals(50.0, tpv.getValue(1).doubleValue(), 0.001);
    }

    @Test
    public void testMinMaxIndicesFault7() {
        // Specific test targeting potential min/max start/end index recalculation bugs (Defects4J Chart 7)
        TimePeriodValues tpv = new TimePeriodValues("Bounds Test");

        RegularTimePeriod p1 = new Day(1, 1, 2020);
        RegularTimePeriod p2 = new Day(5, 1, 2020);
        RegularTimePeriod p3 = new Day(3, 1, 2020);

        tpv.add(p1, 1.0);
        tpv.add(p2, 2.0);
        tpv.add(p3, 3.0);

        // Check bounds after adding out-of-order or overlapping time periods
        assertEquals(0, tpv.getMinStartIndex());
        assertEquals(1, tpv.getMaxStartIndex());
        
        tpv.delete(1); // delete p2 (which was at index 1, max start)
        // This often triggers the bug in Chart 7 where min/max indices are not correctly recalculated
        assertTrue(tpv.getMaxStartIndex() >= 0);
        assertTrue(tpv.getMaxStartIndex() < tpv.getItemCount());
    }

    @Test
    public void testCreateCopy() throws CloneNotSupportedException {
        TimePeriodValues tpv = new TimePeriodValues("Original");
        RegularTimePeriod t1 = new Year(2020);
        tpv.add(t1, 10.0);

        TimePeriodValues copy = (TimePeriodValues) tpv.createCopy(0, 0);
        assertEquals(1, copy.getItemCount());
        assertEquals(10.0, copy.getValue(0).doubleValue(), 0.001);
        assertEquals("Original", copy.getKey());
    }

    @Test
    public void testEqualsAndHashCode() {
        TimePeriodValues tpv1 = new TimePeriodValues("Test");
        TimePeriodValues tpv2 = new TimePeriodValues("Test");
        RegularTimePeriod t1 = new Year(2020);
        
        tpv1.add(t1, 10.0);
        tpv2.add(t1, 10.0);

        assertEquals(tpv1, tpv2);
        assertEquals(tpv1.hashCode(), tpv2.hashCode());

        tpv2.update(0, 20.0);
        org.junit.Assert.assertNotEquals(tpv1, tpv2);
    }
}