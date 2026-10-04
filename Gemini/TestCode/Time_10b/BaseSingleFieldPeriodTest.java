package org.joda.time.base;

import org.junit.Test;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;
import org.joda.time.MutablePeriod;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Weeks;
import org.joda.time.Months;
import org.joda.time.Years;
import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;

import static org.junit.Assert.*;

public class BaseSingleFieldPeriodTest {

    @Test
    public void testBetweenStaticMethodsAndConstructors() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, ISOChronology.getInstanceUTC());
        DateTime end = new DateTime(2020, 1, 1, 0, 0, 5, ISOChronology.getInstanceUTC());

        // Test between(ReadableInstant, ReadableInstant)
        Seconds s1 = Seconds.secondsBetween(start, end);
        assertEquals(5, s1.getSeconds());

        // Test standard between with null check or zero
        Seconds s2 = Seconds.secondsBetween(start, start);
        assertEquals(0, s2.getSeconds());
    }

    @Test
    public void testStandardBetweenMethods() {
        DateTime start = new DateTime(2020, 1, 1, 0, 0, 0, ISOChronology.getInstanceUTC());
        DateTime end = new DateTime(2020, 1, 3, 0, 0, 0, ISOChronology.getInstanceUTC());

        Days days = Days.daysBetween(start, end);
        assertEquals(2, days.getDays());

        Hours hours = Hours.hoursBetween(start, end);
        assertEquals(48, hours.getHours());

        Minutes minutes = Minutes.minutesBetween(start, end);
        assertEquals(48 * 60, minutes.getMinutes());

        Weeks weeks = Weeks.weeksBetween(new DateTime(2020, 1, 1, 0, 0, 0), new DateTime(2020, 1, 15, 0, 0, 0));
        assertEquals(2, weeks.getWeeks());

        Months months = Months.monthsBetween(new DateTime(2020, 1, 1), new DateTime(2020, 4, 1));
        assertEquals(3, months.getMonths());

        Years years = Years.yearsBetween(new DateTime(2010, 1, 1), new DateTime(2020, 1, 1));
        assertEquals(10, years.getYears());
    }

    @Test
    public void testStandardPeriodCreationFromMillis() {
        // This exercises standard protected constructor logic inside BaseSingleFieldPeriod
        Seconds s = Seconds.secondsIn(new Period(10000L));
        assertEquals(10, s.getSeconds());

        Hours h = Hours.hoursIn(new Period(7200000L));
        assertEquals(2, h.getHours());
    }

    @Test
    public void testCompareTo() {
        Seconds s1 = Seconds.seconds(5);
        Seconds s2 = Seconds.seconds(10);
        Seconds s3 = Seconds.seconds(5);

        assertTrue(s1.compareTo(s2) < 0);
        assertTrue(s2.compareTo(s1) > 0);
        assertEquals(0, s1.compareTo(s3));
    }

    @Test(expected = ClassCastException.class)
    public void testCompareToDifferentType() {
        Seconds s = Seconds.seconds(5);
        Minutes m = Minutes.minutes(5);
        // BaseSingleFieldPeriod implements Comparable<BaseSingleFieldPeriod>
        // Comparing different types should throw ClassCastException
        s.compareTo((BaseSingleFieldPeriod) (Object) m);
    }

    @Test
    public void testEqualsAndHashCode() {
        Seconds s1 = Seconds.seconds(5);
        Seconds s2 = Seconds.seconds(5);
        Seconds s3 = Seconds.seconds(10);
        Minutes m5 = Minutes.minutes(5);

        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());

        assertNotEquals(s1, s3);
        assertNotEquals(s1, m5);
        assertNotEquals(s1, null);
        assertNotEquals(s1, "Some String");
    }

    @Test
    public void testGetFieldTypeAndPeriodType() {
        Seconds s = Seconds.seconds(15);
        assertEquals(DurationFieldType.seconds(), s.getFieldType());
        assertNotNull(s.getPeriodType());
        assertEquals(15, s.getValue());
    }

    @Test
    public void standardStandardizedMethods() {
        Seconds s = Seconds.standardSeconds(20);
        assertEquals(20, s.getSeconds());
    }
}