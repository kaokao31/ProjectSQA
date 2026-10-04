package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for DateTimeZone, targeting Defects4J bug ID 23.
 * Focus on forOffsetHoursMinutes with negative hours and positive minutes.
 */
public class DateTimeZoneTest {

    // Helper to compute expected offset millis for hours and minutes.
    private int expectedOffsetMillis(int hours, int minutes) {
        int totalMinutes;
        if (hours < 0) {
            // Negative hours with positive minutes: subtract minutes
            totalMinutes = hours * 60 - minutes;
        } else {
            totalMinutes = hours * 60 + minutes;
        }
        return totalMinutes * 60000;
    }

    // ------------------------ forOffsetHoursMinutes tests ------------------------

    @Test
    public void testForOffsetHoursMinutes_NegativeHoursPositiveMinutes_ShouldNotThrow() {
        // This case triggered ArithmeticException in buggy version
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-2, 30);
        assertNotNull(zone);
        assertEquals(expectedOffsetMillis(-2, 30), zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_NegativeHoursZeroMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-5, 0);
        assertNotNull(zone);
        assertEquals(expectedOffsetMillis(-5, 0), zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_PositiveHoursPositiveMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(2, 30);
        assertNotNull(zone);
        assertEquals(expectedOffsetMillis(2, 30), zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_PositiveHoursZeroMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(3, 0);
        assertNotNull(zone);
        assertEquals(expectedOffsetMillis(3, 0), zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_ZeroHoursPositiveMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 45);
        assertNotNull(zone);
        assertEquals(expectedOffsetMillis(0, 45), zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_ZeroHoursZeroMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, zone);
    }

    @Test
    public void testForOffsetHoursMinutes_ValidBoundaryMax() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(23, 59);
        assertNotNull(zone);
        assertEquals(expectedOffsetMillis(23, 59), zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHoursMinutes_ValidBoundaryMin() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-23, 59);
        assertNotNull(zone);
        assertEquals(expectedOffsetMillis(-23, 59), zone.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_InvalidHoursTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_InvalidHoursTooSmall() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_InvalidMinutesTooLarge() {
        DateTimeZone.forOffsetHoursMinutes(1, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_InvalidMinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutes_InvalidHoursNegativeAndMinutesOutOfRange() {
        // Minutes must be 0-59 even when hours negative
        DateTimeZone.forOffsetHoursMinutes(-1, 60);
    }

    // ------------------------ forOffsetMillis tests --------------------------------

    @Test
    public void testForOffsetMillis_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(0);
        assertSame(DateTimeZone.UTC, zone);
    }

    @Test
    public void testForOffsetMillis_Positive() {
        int millis = 5 * 60 * 60000; // +05:00
        DateTimeZone zone = DateTimeZone.forOffsetMillis(millis);
        assertNotNull(zone);
        assertEquals(millis, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis_Negative() {
        int millis = -3 * 60 * 60000; // -03:00
        DateTimeZone zone = DateTimeZone.forOffsetMillis(millis);
        assertNotNull(zone);
        assertEquals(millis, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis_LargeValue() {
        int millis = 14 * 60 * 60000; // +14:00 (maximum allowed)
        DateTimeZone zone = DateTimeZone.forOffsetMillis(millis);
        assertNotNull(zone);
        assertEquals(millis, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis_MinValue() {
        int millis = -12 * 60 * 60000; // -12:00 (minimum typical)
        DateTimeZone zone = DateTimeZone.forOffsetMillis(millis);
        assertNotNull(zone);
        assertEquals(millis, zone.getOffset(0L));
    }

    // ------------------------ forID tests ------------------------------------------

    @Test
    public void testForID_UTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertSame(DateTimeZone.UTC, zone);
    }

    @Test
    public void testForID_Offset() {
        String id = "+02:30";
        DateTimeZone zone = DateTimeZone.forID(id);
        assertNotNull(zone);
        assertEquals(id, zone.getID());
        // Verify offset matches
        int expectedMillis = expectedOffsetMillis(2, 30);
        assertEquals(expectedMillis, zone.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_Invalid() {
        DateTimeZone.forID("InvalidID");
    }

    // ------------------------ UTC constant ------------------------------------------

    @Test
    public void testUTCConstant() {
        assertNotNull(DateTimeZone.UTC);
        assertEquals("UTC", DateTimeZone.UTC.getID());
        assertEquals(0, DateTimeZone.UTC.getOffset(0L));
    }

    // ------------------------ getID tests ------------------------------------------

    @Test
    public void testGetID_ForOffsetZone() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(1, 0);
        assertEquals("+01:00", zone.getID());
    }

    @Test
    public void testGetID_ForNegativeOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-5, 30);
        assertEquals("-05:30", zone.getID());
    }

    // ------------------------ equals and hashCode ----------------------------------

    @Test
    public void testEquals_SameOffset() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHoursMinutes(2, 30);
        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(expectedOffsetMillis(2, 30));
        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test
    public void testEquals_DifferentOffset() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHoursMinutes(2, 30);
        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(2, 31);
        assertNotEquals(zone1, zone2);
    }

    @Test
    public void testEquals_Null() {
        DateTimeZone zone = DateTimeZone.UTC;
        assertFalse(zone.equals(null));
    }

    @Test
    public void testHashCode_Consistency() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-3, 45);
        int hash1 = zone.hashCode();
        int hash2 = zone.hashCode();
        assertEquals(hash1, hash2);
    }

    // ------------------------ toString ----------------------------------------------

    @Test
    public void testToString_UTC() {
        assertEquals("UTC", DateTimeZone.UTC.toString());
    }

    @Test
    public void testToString_Offset() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(11, 15);
        assertNotNull(zone.toString());
        assertTrue(zone.toString().contains("+11:15") || zone.toString().equals("[+11:15]"));
    }

    // ------------------------ getOffset at arbitrary instant ------------------------

    @Test
    public void testGetOffset_OnInstant() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(-4, 30);
        long instant = 123456789L;
        assertEquals(expectedOffsetMillis(-4, 30), zone.getOffset(instant));
    }
}