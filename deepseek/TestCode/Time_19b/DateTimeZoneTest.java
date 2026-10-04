package org.joda.time;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.util.Set;

/**
 * JUnit 4 test suite for DateTimeZone, targeting high coverage and
 * bug detection (especially for Defects4J Time-19 in forOffsetHoursMinutes).
 */
public class DateTimeZoneTest {

    @Before
    public void setUp() {
        // No general setup needed.
    }

    // ---------- Tests for forOffsetHoursMinutes ----------

    @Test
    public void testForOffsetHoursMinutes_UTC() {
        assertSame("+00:00 should map to UTC singleton",
                DateTimeZone.UTC,
                DateTimeZone.forOffsetHoursMinutes(0, 0));
    }

    @Test
    public void testForOffsetHoursMinutes_PositiveHoursZeroMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 0);
        assertEquals("+05:00", zone.getID());
        assertEquals(5L * 3600 * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_NegativeHoursZeroMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-5, 0);
        assertEquals("-05:00", zone.getID());
        assertEquals(-5L * 3600 * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_PositiveHoursPositiveMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(1, 30);
        assertEquals("+01:30", zone.getID());
        assertEquals((1L * 3600 + 30 * 60) * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_NegativeHoursPositiveMinutes() {
        // -1 hour +30 minutes -> -30 minutes offset
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-1, 30);
        assertEquals("-00:30", zone.getID());
        assertEquals(-30L * 60 * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_ZeroHoursNegativeMinutes() {
        // Bug-revealing case: should work, but buggy version throws.
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, -30);
        assertEquals("-00:30", zone.getID());
        assertEquals(-30L * 60 * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_OneHourNegativeMinutes() {
        // 1 hour -30 minutes -> +30 minutes offset
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(1, -30);
        assertEquals("+00:30", zone.getID());
        assertEquals(30L * 60 * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_NegativeHoursNegativeMinutes() {
        // -1 hour -30 minutes -> -1:30 offset
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-1, -30);
        assertEquals("-01:30", zone.getID());
        assertEquals(-(1L * 3600 + 30 * 60) * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_MaximumPositive() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(23, 59);
        assertEquals("+23:59", zone.getID());
        assertEquals((23L * 3600 + 59 * 60) * 1000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_MaximumNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-23, 59);
        // -23 hours +59 minutes = -22 hours -1 minute = -22:01
        assertEquals("-22:01", zone.getID());
        assertEquals((-23L * 3600 + 59 * 60) * 1000, zone.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooLow() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_HoursTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesTooHigh() {
        DateTimeZone.forOffsetHoursMinutes(0, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_MinutesTooLow() {
        // -60 minutes is invalid even in fixed version (range -59..59)
        DateTimeZone.forOffsetHoursMinutes(0, -60);
    }

    @Test
    public void testForOffsetHoursMinutes_MinutesBoundaryNegative() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, -59);
        assertEquals("-00:59", zone.getID());
        assertEquals(-59L * 60 * 1000, zone.getOffset(0L));
    }

    // ---------- Tests for forOffsetMillis ----------

    @Test
    public void testForOffsetMillis_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals("-01:00", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_Excess() {
        // 48 hours -> too large
        DateTimeZone.forOffsetMillis(1000 * 60 * 60 * 24 * 2);
    }

    // ---------- Tests for getOffset ----------

    @Test
    public void testGetOffset_UTC() {
        assertEquals(0, DateTimeZone.UTC.getOffset(0L));
        assertEquals(0, DateTimeZone.UTC.getOffset(123456789L));
    }

    @Test
    public void testGetOffset_FixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 0);
        long expected = 2L * 3600 * 1000;
        assertEquals(expected, zone.getOffset(0L));
        assertEquals(expected, zone.getOffset(987654321L));
    }

    // ---------- Tests for getOffsetFromLocal ----------

    @Test
    public void testGetOffsetFromLocal_UTC() {
        assertEquals(0, DateTimeZone.UTC.getOffsetFromLocal(0L));
    }

    @Test
    public void testGetOffsetFromLocal_FixedZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-3, 0);
        long expected = -3L * 3600 * 1000;
        assertEquals(expected, zone.getOffsetFromLocal(0L));
    }

    @Test
    public void testGetOffsetFromLocal_DSTOverlap() {
        // Use America/New_York during fall-back overlap (2013-11-03)
        DateTimeZone nyc = DateTimeZone.forID("America/New_York");
        // Local time: 2013-11-03 01:30:00, which appears twice
        org.joda.time.LocalDateTime local = new org.joda.time.LocalDateTime(2013, 11, 3, 1, 30);
        long instantLocal = local.toDateTime(DateTimeZone.UTC).getMillis();
        int offset = nyc.getOffsetFromLocal(instantLocal);
        assertTrue("Offset should be -18000000 (EDT) or -14400000 (EST)",
                offset == -18000000 || offset == -14400000);
    }

    // ---------- Tests for isLocalDateTimeGap ----------

    @Test
    public void testIsLocalDateTimeGap() {
        // Use America/New_York during spring-forward gap (2013-03-10 02:30)
        DateTimeZone nyc = DateTimeZone.forID("America/New_York");
        org.joda.time.LocalDateTime local = new org.joda.time.LocalDateTime(2013, 3, 10, 2, 30);
        long instantLocal = local.toDateTime(DateTimeZone.UTC).getMillis();
        assertTrue("2013-03-10 02:30 is a gap", nyc.isLocalDateTimeGap(instantLocal));
    }

    // ---------- Tests for getAvailableIDs ----------

    @Test
    public void testGetAvailableIDs() {
        Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertFalse(ids.isEmpty());
        assertTrue(ids.contains("UTC"));
    }

    // ---------- Tests for getID ----------

    @Test
    public void testGetID() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertEquals("America/New_York", zone.getID());
    }

    // ---------- Tests for toString ----------

    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        String str = zone.toString();
        assertTrue("toString should contain the zone ID", str.contains("Europe/London"));
    }
}