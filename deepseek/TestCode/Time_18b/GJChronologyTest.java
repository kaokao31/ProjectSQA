package org.joda.time.chrono;

import org.junit.Test;
import static org.junit.Assert.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.ReadableInstant;

public class GJChronologyTest {

    @Test
    public void testGetInstance() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono);
    }

    @Test
    public void testGetInstance_zone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        GJChronology chrono = GJChronology.getInstance(zone);
        assertEquals(zone, chrono.getZone());
    }

    @Test
    public void testGetInstance_zone_null() {
        GJChronology chrono = GJChronology.getInstance((DateTimeZone) null);
        assertNotNull(chrono);
    }

    @Test
    public void testGetInstance_cutoverInstant() {
        Instant cutover = new Instant(1582L);
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, cutover);
        assertNotNull(chrono);
    }

    @Test
    public void testGetInstance_cutoverMillisAndMinDays() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, 1582L, 4);
        assertNotNull(chrono);
    }

    @Test
    public void testWithUTC() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.forID("Europe/London"));
        Chronology utc = chrono.withUTC();
        assertEquals(DateTimeZone.UTC, utc.getZone());
    }

    @Test
    public void testWithZone() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        Chronology london = chrono.withZone(DateTimeZone.forID("Europe/London"));
        assertEquals(DateTimeZone.forID("Europe/London"), london.getZone());
    }

    @Test
    public void testGetDateTimeMillis_validDate() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        long millis = chrono.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        assertEquals(2000, chrono.getYear(millis));
        assertEquals(1, chrono.getMonthOfYear(millis));
        assertEquals(1, chrono.getDayOfMonth(millis));
    }

    @Test
    public void testGetDateTimeMillis_aroundCutover() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        long julianMillis = chrono.getDateTimeMillis(1582, 10, 4, 0, 0, 0, 0);
        assertEquals(1582, chrono.getYear(julianMillis));
        assertEquals(10, chrono.getMonthOfYear(julianMillis));
        assertEquals(4, chrono.getDayOfMonth(julianMillis));

        long gregorianMillis = chrono.getDateTimeMillis(1582, 10, 15, 0, 0, 0, 0);
        assertEquals(1582, chrono.getYear(gregorianMillis));
        assertEquals(10, chrono.getMonthOfYear(gregorianMillis));
        assertEquals(15, chrono.getDayOfMonth(gregorianMillis));

        assertEquals(10L * 24 * 60 * 60 * 1000, gregorianMillis - julianMillis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillis_invalidDateInGap() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        chrono.getDateTimeMillis(1582, 10, 10, 0, 0, 0, 0);
    }

    @Test
    public void testMinDaysInFirstWeekDiffers() {
        GJChronology chrono1 = GJChronology.getInstance(DateTimeZone.UTC, null, 4);
        GJChronology chrono2 = GJChronology.getInstance(DateTimeZone.UTC, null, 1);
        long millis = chrono1.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        int week1 = chrono1.getWeekOfWeekyear(millis);
        int week2 = chrono2.getWeekOfWeekyear(millis);
        assertNotSame(week1, week2);
    }

    @Test
    public void testDateTimeRoundTrip() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        DateTime dt = new DateTime(2000, 6, 15, 12, 30, 45, 123, chrono);
        long millis = dt.getMillis();
        assertEquals(2000, chrono.getYear(millis));
        assertEquals(6, chrono.getMonthOfYear(millis));
        assertEquals(15, chrono.getDayOfMonth(millis));
        assertEquals(12, chrono.getHourOfDay(millis));
        assertEquals(30, chrono.getMinuteOfHour(millis));
        assertEquals(45, chrono.getSecondOfMinute(millis));
        assertEquals(123, chrono.getMillisOfSecond(millis));
    }

    @Test
    public void testSingletonSameInstance() {
        GJChronology c1 = GJChronology.getInstance(DateTimeZone.UTC);
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.UTC);
        assertSame(c1, c2);
    }

    @Test
    public void testDifferentZoneNotSame() {
        GJChronology c1 = GJChronology.getInstance(DateTimeZone.UTC);
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.forID("Europe/London"));
        assertNotSame(c1, c2);
    }

    @Test
    public void testDifferentCutoverDifferentInstance() {
        GJChronology c1 = GJChronology.getInstance(DateTimeZone.UTC, new Instant(0L), 4);
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.UTC, new Instant(1582L), 4);
        assertNotSame(c1, c2);
    }

    @Test
    public void testJulianLeapYear() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        long millis = chrono.getDateTimeMillis(1500, 2, 29, 0, 0, 0, 0);
        assertEquals(29, chrono.getDayOfMonth(millis));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGregorianNonLeapYear() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        chrono.getDateTimeMillis(1700, 2, 29, 0, 0, 0, 0);
    }

    @Test
    public void testLeapYear2000() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        long millis = chrono.getDateTimeMillis(2000, 2, 29, 0, 0, 0, 0);
        assertEquals(29, chrono.getDayOfMonth(millis));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonLeapYear1900() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        chrono.getDateTimeMillis(1900, 2, 29, 0, 0, 0, 0);
    }

    @Test
    public void testDayOfWeekKnownDate() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        // 2000-01-01 is Saturday, ISO day of week = 6
        long millis = chrono.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        assertEquals(6, chrono.getDayOfWeek(millis));
    }

    @Test
    public void testWeekyearKnownDate() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        // 2000-01-01 falls in week 52 of 1999 under ISO rules
        long millis = chrono.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        assertEquals(1999, chrono.getWeekyear(millis));
    }

    @Test
    public void testWithZoneOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(1);
        GJChronology chrono = GJChronology.getInstance(zone);
        long utcMillis = GJChronology.getInstance(DateTimeZone.UTC).getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        // Local date in zone should be the same (since instant is 00:00 UTC)
        assertEquals(2000, chrono.getYear(utcMillis));
        assertEquals(1, chrono.getMonthOfYear(utcMillis));
        assertEquals(1, chrono.getDayOfMonth(utcMillis));
    }

    @Test
    public void testGetDateTimeMillisWithDateOnly() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        long millis = chrono.getDateTimeMillis(1999, 12, 31, 0, 0, 0, 0);
        assertEquals(1999, chrono.getYear(millis));
        assertEquals(12, chrono.getMonthOfYear(millis));
        assertEquals(31, chrono.getDayOfMonth(millis));
    }

    @Test
    public void testGetDateTimeMillisMinimumTime() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        long millis = chrono.getDateTimeMillis(1, 1, 1, 0, 0, 0, 0);
        assertEquals(1, chrono.getYear(millis));
        assertEquals(1, chrono.getMonthOfYear(millis));
        assertEquals(1, chrono.getDayOfMonth(millis));
    }

    @Test
    public void testGetDateTimeMillisMaximumYear() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        long millis = chrono.getDateTimeMillis(9999, 12, 31, 23, 59, 59, 999);
        assertEquals(9999, chrono.getYear(millis));
    }

    @Test
    public void testGetName() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono.toString());
    }

    @Test
    public void testEquals() {
        GJChronology c1 = GJChronology.getInstance(DateTimeZone.UTC);
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.UTC);
        assertEquals(c1, c2);
    }

    @Test
    public void testHashCode() {
        GJChronology c1 = GJChronology.getInstance(DateTimeZone.UTC);
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.UTC);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    public void testNotEqualsDifferentZone() {
        GJChronology c1 = GJChronology.getInstance(DateTimeZone.UTC);
        GJChronology c2 = GJChronology.getInstance(DateTimeZone.forID("Europe/London"));
        assertFalse(c1.equals(c2));
    }

    @Test
    public void testGetZoneFromInstance() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        GJChronology chrono = GJChronology.getInstance(zone);
        assertEquals(zone, chrono.getZone());
    }
}