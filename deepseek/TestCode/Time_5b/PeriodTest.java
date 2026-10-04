package org.jfree.time;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.Date;

public class PeriodTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStart() {
        new Period(null, new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullEnd() {
        new Period(new Date(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithStartAfterEnd() {
        new Period(new Date(2000), new Date(1000));
    }

    @Test
    public void testGetStart() {
        Date start = new Date(1000);
        Date end = new Date(2000);
        Period p = new Period(start, end);
        assertEquals(start, p.getStart());
    }

    @Test
    public void testGetEnd() {
        Date start = new Date(1000);
        Date end = new Date(2000);
        Period p = new Period(start, end);
        assertEquals(end, p.getEnd());
    }

    @Test
    public void testGetDuration() {
        Date start = new Date(1000);
        Date end = new Date(2000);
        Period p = new Period(start, end);
        assertEquals(1000, p.getDuration());
    }

    @Test
    public void testIncludes() {
        Date start = new Date(1000);
        Date end = new Date(2000);
        Period p = new Period(start, end);
        assertTrue(p.includes(new Date(1500)));
        assertFalse(p.includes(new Date(500)));
        assertFalse(p.includes(new Date(2500)));
        assertTrue(p.includes(start));
        assertTrue(p.includes(end));
    }

    @Test
    public void testOverlaps() {
        Date start1 = new Date(1000);
        Date end1 = new Date(2000);
        Period p1 = new Period(start1, end1);
        Date start2 = new Date(1500);
        Date end2 = new Date(2500);
        Period p2 = new Period(start2, end2);
        assertTrue(p1.overlaps(p2));
        assertTrue(p2.overlaps(p1));
        Date start3 = new Date(3000);
        Date end3 = new Date(4000);
        Period p3 = new Period(start3, end3);
        assertFalse(p1.overlaps(p3));
    }

    @Test
    public void testEquals() {
        Date start = new Date(1000);
        Date end = new Date(2000);
        Period p1 = new Period(start, end);
        Period p2 = new Period(start, end);
        assertEquals(p1, p2);
        assertNotSame(p1, p2);
        Period p3 = new Period(new Date(1000), new Date(2001));
        assertFalse(p1.equals(p3));
        assertFalse(p1.equals(null));
        assertFalse(p1.equals("string"));
    }

    @Test
    public void testHashCode() {
        Date start = new Date(1000);
        Date end = new Date(2000);
        Period p1 = new Period(start, end);
        Period p2 = new Period(start, end);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    @Test
    public void testCompareTo() {
        Date start1 = new Date(1000);
        Date end1 = new Date(2000);
        Period p1 = new Period(start1, end1);
        Date start2 = new Date(1500);
        Date end2 = new Date(2500);
        Period p2 = new Period(start2, end2);
        assertTrue(p1.compareTo(p2) < 0);
        assertTrue(p2.compareTo(p1) > 0);
        Period p3 = new Period(start1, end1);
        assertEquals(0, p1.compareTo(p3));
    }
}