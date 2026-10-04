package org.joda.time;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for DateTimeZone with focus on bug ID 25 (DST handling and hashCode consistency).
 */
public class DateTimeZoneTest {

    private DateTimeZone utc;
    private DateTimeZone london;
    private DateTimeZone newYork;

    @Before
    public void setUp() throws Exception {
        utc = DateTimeZone.UTC;
        london = DateTimeZone.forID("Europe/London");
        newYork = DateTimeZone.forID("America/New_York");
    }

    // ---------------------------------------------------------------
    // Zone creation tests
    // ---------------------------------------------------------------

    @Test
    public void testForID_Null() {
        try {
            DateTimeZone.forID(null);
            // Should throw IllegalArgumentException
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_Invalid() {
        DateTimeZone.forID("Invalid/NonExistent");
    }

    @Test
    public void testForID_UTC() {
        assertSame(utc, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForID_Plus00() {
        assertSame(utc, DateTimeZone.forID("+00:00"));
    }

    @Test
    public void testForID_GMT() {
        DateTimeZone gmt = DateTimeZone.forID("GMT");
        assertNotNull(gmt);
        assertEquals(0, gmt.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis_Zero() {
        assertEquals(utc, DateTimeZone.forOffsetMillis(0));
    }

    @Test
    public void testForOffsetMillis_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
    }

    @Test
    public void testForOffsetMillis_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-3600000);
        assertEquals("-01:00", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_LargePositive() {
        DateTimeZone.forOffsetMillis(24 * 60 * 60 * 1000);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillis_LargeNegative() {
        DateTimeZone.forOffsetMillis(-24 * 60 * 60 * 1000);
    }

    // ---------------------------------------------------------------
    // getOffset tests (UTC fixed)
    // ---------------------------------------------------------------

    @Test
    public void testGetOffset_UTC() {
        assertEquals(0, utc.getOffset(0L));
        assertEquals(0, utc.getOffset(Long.MAX_VALUE));
        assertEquals(0, utc.getOffset(Long.MIN_VALUE));
    }

    // ---------------------------------------------------------------
    // getOffset tests (DST zones)
    // ---------------------------------------------------------------

    @Test
    public void testGetOffset_London_Winter() {
        // London winter time (UTC+0)
        long instant = 1483228800000L; // 2017-01-01T00:00:00 UTC
        assertEquals(0, london.getOffset(instant));
    }

    @Test
    public void testGetOffset_London_Summer() {
        // London summer time (UTC+1)
        long instant = 1498867200000L; // 2017-07-01T00:00:00 UTC
        assertEquals(3600000, london.getOffset(instant));
    }

    @Test
    public void testGetOffset_NewYork_Winter() {
        // New York winter (EST, UTC-5)
        long instant = 1483228800000L; // 2017-01-01T00:00:00 UTC
        assertEquals(-5 * 3600000L, newYork.getOffset(instant));
    }

    @Test
    public void testGetOffset_NewYork_Summer() {
        // New York summer (EDT, UTC-4)
        long instant = 1498867200000L; // 2017-07-01T00:00:00 UTC
        assertEquals(-4 * 3600000L, newYork.getOffset(instant));
    }

    // ---------------------------------------------------------------
    // getOffsetFromLocal tests (DST transitions - potential bug area)
    // ---------------------------------------------------------------

    @Test
    public void testGetOffsetFromLocal_UTC() {
        assertEquals(0, utc.getOffsetFromLocal(0L));
    }

    @Test
    public void testGetOffsetFromLocal_London_Winter() {
        // During winter, local time = UTC
        long localMillis = 1483228800000L; // 2017-01-01T00:00:00 local
        assertEquals(0, london.getOffsetFromLocal(localMillis));
    }

    @Test
    public void testGetOffsetFromLocal_London_Summer() {
        // During summer, local time = UTC+1, so offset = +1h
        long localMillis = 1498867200000L; // 2017-07-01T00:00:00 local
        assertEquals(3600000, london.getOffsetFromLocal(localMillis));
    }

    @Test
    public void testGetOffsetFromLocal_London_FallBack_beforeTransition() {
        // 2016-10-30 in UK: DST ends at 02:00 local (03:00 BST -> 02:00 GMT).
        // Just before transition (01:59 local BST), offset should be +1h.
        // Local time 01:59:00 BST = 2016-10-30T01:59:00 local.
        // Convert to millis from epoch.
        long localMillis = 1477807140000L; // 2016-10-30T01:59:00.000 local (BST)
        // During BST, offset is +1h. getOffsetFromLocal should return +1h.
        assertEquals(3600000, london.getOffsetFromLocal(localMillis));
    }

    @Test
    public void testGetOffsetFromLocal_London_FallBack_afterTransition() {
        // 2016-10-30 in UK: DST ends at 02:00 local (03:00 BST -> 02:00 GMT).
        // After transition, 02:00 local time is repeated; GMT offset = 0.
        // For local time 02:00:00, there are two instants: one BST (still +1h at 01:00 UTC)
        // and one GMT (0 offset). The method should return the earlier one? Joda returns the earlier? 
        // In joda-time, getOffsetFromLocal returns the offset in summer time if local is ambiguous,
        // thereby giving priority to the earlier UTC instant. So for 02:00 local, offset = 0 (winter).
        // Actually, joda's implementation: if during overlap, it returns the later offset (winter).
        // Let's test: 2016-10-30T02:00:00 local in London.
        // First occurrence: 02:00 BST (UTC+1) = 01:00 UTC
        // Second occurrence: 02:00 GMT (UTC+0) = 02:00 UTC
        // The method is known to prefer the later one (winter offset=0).
        long localMillis = 1477807200000L; // 2016-10-30T02:00:00.000 local
        int offset = london.getOffsetFromLocal(localMillis);
        // Expect 0 (winter) because of overlap handling.
        assertEquals(0, offset);
    }

    @Test
    public void testGetOffsetFromLocal_London_SpringForward() {
        // 2017-03-26 in UK: DST starts at 01:00 UTC, clocks go forward to 02:00 BST.
        // Local time from 01:00 to 01:59:59 does not exist.
        // For a non-existent local time, joda returns the offset after the gap (summer).
        // Example: local 01:30:00 on 2017-03-26 -> should give summer offset +1h.
        long localMillis = 1490398200000L; // 2017-03-26T01:30:00.000 local (does not exist)
        int offset = london.getOffsetFromLocal(localMillis);
        assertEquals(3600000, offset);
    }

    // ---------------------------------------------------------------
    // equals / hashCode tests
    // ---------------------------------------------------------------

    @Test
    public void testEquals_EqualZones() {
        DateTimeZone london2 = DateTimeZone.forID("Europe/London");
        assertEquals(london, london2);
        assertEquals(london.hashCode(), london2.hashCode());
    }

    @Test
    public void testEquals_DifferentZones() {
        assertFalse(london.equals(newYork));
        // hash codes may still be equal, but not guaranteed
    }

    @Test
    public void testEquals_WithNull() {
        assertFalse(london.equals(null));
    }

    @Test
    public void testEquals_WithDifferentObjectType() {
        assertFalse(london.equals("not a zone"));
    }

    @Test
    public void testHashCode_UTC() {
        assertEquals("UTC".hashCode(), utc.hashCode());
    }

    @Test
    public void testHashCode_SameAsID() {
        // Joda-Time's hashCode is based on getID().hashCode()
        assertEquals(london.getID().hashCode(), london.hashCode());
        assertEquals(newYork.getID().hashCode(), newYork.hashCode());
    }

    // ---------------------------------------------------------------
    // isFixed tests
    // ---------------------------------------------------------------

    @Test
    public void testIsFixed_UTC() {
        assertTrue(utc.isFixed());
    }

    @Test
    public void testIsFixed_FixedOffsetZone() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(1);
        assertTrue(fixed.isFixed());
    }

    @Test
    public void testIsFixed_DSTZone() {
        assertFalse(london.isFixed());
        assertFalse(newYork.isFixed());
    }

    // ---------------------------------------------------------------
    // getNameKey tests
    // ---------------------------------------------------------------

    @Test
    public void testGetNameKey_UTC() {
        assertNotNull(utc.getNameKey(0L));
    }

    @Test
    public void testGetNameKey_London_Winter() {
        long instant = 1483228800000L;
        assertEquals("GMT", london.getNameKey(instant));
    }

    @Test
    public void testGetNameKey_London_Summer() {
        long instant = 1498867200000L;
        assertEquals("BST", london.getNameKey(instant));
    }

    // ---------------------------------------------------------------
    // toTimeZone and fromTimeZone tests
    // ---------------------------------------------------------------

    @Test
    public void testToTimeZone_UTC() {
        java.util.TimeZone tz = utc.toTimeZone();
        assertEquals("UTC", tz.getID());
    }

    @Test
    public void testFromTimeZone_UTC() {
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        assertSame(utc, DateTimeZone.forTimeZone(tz));
    }

    @Test
    public void testFromTimeZone_Invalid() {
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("Bogus");
        // Should return UTC fallback
        assertSame(utc, DateTimeZone.forTimeZone(tz));
    }

    // ---------------------------------------------------------------
    // Conversion with milliseconds
    // ---------------------------------------------------------------

    @Test
    public void testConvertUTCToLocal() {
        long utcMillis = 0L;
        long localMillis = london.convertUTCToLocal(utcMillis);
        // London in winter (Jan 1, 1970) offset = 0? Actually Jan 1, 1970 was in GMT (winter)
        // So offset = 0.
        assertEquals(0L, localMillis);
    }

    @Test
    public void testConvertLocalToUTC() {
        long localMillis = 0L;
        long utcMillis = london.convertLocalToUTC(localMillis, false);
        // During winter, offset = 0, so utcMillis = 0
        assertEquals(0L, utcMillis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConvertLocalToUTC_DuringGap() {
        // 2017-03-26T01:30:00 local in London is a gap.
        long localMillis = 1490398200000L;
        london.convertLocalToUTC(localMillis, false);
    }

    @Test
    public void testConvertLocalToUTC_DuringOverlap() {
        // 2016-10-30T02:00:00 local in London is overlap.
        long localMillis = 1477807200000L;
        // With false, should prefer later (winter) offset => UTC = 02:00
        assertEquals(1477807200000L, london.convertLocalToUTC(localMillis, false));
        // With true, should prefer earlier (summer) offset => UTC = 01:00
        assertEquals(1477803600000L, london.convertLocalToUTC(localMillis, true));
    }

    // ---------------------------------------------------------------
    // getStandardOffset tests
    // ---------------------------------------------------------------

    @Test
    public void testGetStandardOffset_London_Winter() {
        long instant = 1483228800000L;
        assertEquals(0, london.getStandardOffset(instant));
    }

    @Test
    public void testGetStandardOffset_London_Summer() {
        // During summer, standard offset = 0, actual offset = +1h
        long instant = 1498867200000L;
        assertEquals(0, london.getStandardOffset(instant));
    }

    // ---------------------------------------------------------------
    // isStandardOffset tests
    // ---------------------------------------------------------------

    @Test
    public void testIsStandardOffset_London_Winter() {
        long instant = 1483228800000L;
        assertTrue(london.isStandardOffset(instant));
    }

    @Test
    public void testIsStandardOffset_London_Summer() {
        long instant = 1498867200000L;
        assertFalse(london.isStandardOffset(instant));
    }

    // ---------------------------------------------------------------
    // additional edge cases for bug 25 specifically: hashCode consistency
    // ---------------------------------------------------------------

    @Test
    public void testHashCode_ConsistencyWithEquals() {
        // If two zones are equal, they must have same hashCode
        DateTimeZone london2 = DateTimeZone.forID("Europe/London");
        assertEquals(london.hashCode(), london2.hashCode());
        assertEquals(london, london2);
    }

    @Test
    public void testHashCode_ForOffsetZones() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(5);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(5);
        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    // ---------------------------------------------------------------
    // tests to trigger potential NullPointerException in hashCode if ID is null
    // (ensuring constructor never allows null ID)
    // ---------------------------------------------------------------

    @Test
    public void testZoneID_NotNull() {
        assertNotNull(london.getID());
        assertNotNull(newYork.getID());
        assertNotNull(utc.getID());
    }
}