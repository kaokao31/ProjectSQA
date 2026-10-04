package org.joda.time;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

/**
 * Test suite for DateTimeZone class.
 * Covers edge cases, boundary values, null checks, and known bug patterns.
 */
public class DateTimeZoneTest {

    private DateTimeZone defaultZone;

    @Before
    public void setUp() {
        defaultZone = DateTimeZone.getDefault();
    }

    // --- forID tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testForID_Null() {
        DateTimeZone.forID(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_EmptyString() {
        DateTimeZone.forID("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidID() {
        DateTimeZone.forID("Invalid/Zone");
    }

    @Test
    public void testForID_UTC() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertNotNull(zone);
        assertEquals("UTC", zone.getID());
    }

    @Test
    public void testForID_ValidZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        assertNotNull(zone);
        assertEquals("Europe/London", zone.getID());
    }

    // --- getOffset tests ---

    @Test
    public void testGetOffset_UTC() {
        DateTimeZone utc = DateTimeZone.UTC;
        assertEquals(0, utc.getOffset(0L));
        assertEquals(0, utc.getOffset(Long.MAX_VALUE));
        assertEquals(0, utc.getOffset(Long.MIN_VALUE));
    }

    @Test
    public void testGetOffset_BoundaryValues() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Known offset for standard time (EST) is -5 hours = -18000000 ms
        // Use a known instant in standard time (e.g., 2020-01-01T00:00:00Z = 1577836800000L)
        long instant = 1577836800000L;
        int expectedOffset = -18000000; // EST
        assertEquals(expectedOffset, zone.getOffset(instant));
    }

    @Test
    public void testGetOffset_DSTTransition() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Spring forward: 2020-03-08T02:00:00 EST -> EDT, offset changes from -5 to -4
        // Instant just before transition: 2020-03-08T06:59:59Z = 1583654399000L
        long before = 1583654399000L;
        int offsetBefore = zone.getOffset(before);
        // Instant just after transition: 2020-03-08T07:00:00Z = 1583654400000L
        long after = 1583654400000L;
        int offsetAfter = zone.getOffset(after);
        // Offset should increase by 1 hour (3600000 ms)
        assertEquals(offsetBefore + 3600000, offsetAfter);
    }

    @Test
    public void testGetOffset_NegativeInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        // Instant before epoch
        long instant = -1000000000L;
        int offset = zone.getOffset(instant);
        // Should not throw exception
        assertTrue(offset >= -43200000 && offset <= 50400000); // reasonable range
    }

    // --- isStandardOffset tests ---

    @Test
    public void testIsStandardOffset_UTC() {
        assertTrue(DateTimeZone.UTC.isStandardOffset(0L));
    }

    @Test
    public void testIsStandardOffset_DST() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Summer time (EDT) is not standard
        long summerInstant = 1593561600000L; // 2020-07-01T00:00:00Z
        assertFalse(zone.isStandardOffset(summerInstant));
        // Winter time (EST) is standard
        long winterInstant = 1577836800000L; // 2020-01-01T00:00:00Z
        assertTrue(zone.isStandardOffset(winterInstant));
    }

    // --- getMillisKeepLocal tests ---

    @Test
    public void testGetMillisKeepLocal_SameZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long instant = 1000000L;
        assertEquals(instant, zone.getMillisKeepLocal(null, instant));
    }

    @Test
    public void testGetMillisKeepLocal_DifferentZone() {
        DateTimeZone from = DateTimeZone.forID("Europe/London");
        DateTimeZone to = DateTimeZone.forID("America/New_York");
        long instant = 1577836800000L; // 2020-01-01T00:00:00Z
        long converted = from.getMillisKeepLocal(to, instant);
        // The local time in London is 00:00, so in New York it should be 5 hours earlier
        // But getMillisKeepLocal returns the millis in the new zone that gives the same local time
        // So the result should be instant + offset difference
        int offsetFrom = from.getOffset(instant);
        int offsetTo = to.getOffset(instant);
        long expected = instant + (offsetFrom - offsetTo);
        assertEquals(expected, converted);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetMillisKeepLocal_NullNewZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        zone.getMillisKeepLocal(null, 0L);
    }

    // --- equals and hashCode tests ---

    @Test
    public void testEquals_SameObject() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertTrue(zone.equals(zone));
    }

    @Test
    public void testEquals_DifferentObjectSameID() {
        DateTimeZone zone1 = DateTimeZone.forID("UTC");
        DateTimeZone zone2 = DateTimeZone.forID("UTC");
        assertTrue(zone1.equals(zone2));
    }

    @Test
    public void testEquals_DifferentID() {
        DateTimeZone zone1 = DateTimeZone.forID("UTC");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        assertFalse(zone1.equals(zone2));
    }

    @Test
    public void testEquals_Null() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertFalse(zone.equals(null));
    }

    @Test
    public void testHashCode_ConsistentWithEquals() {
        DateTimeZone zone1 = DateTimeZone.forID("America/Chicago");
        DateTimeZone zone2 = DateTimeZone.forID("America/Chicago");
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    // --- getID tests ---

    @Test
    public void testGetID_UTC() {
        assertEquals("UTC", DateTimeZone.UTC.getID());
    }

    @Test
    public void testGetID_Default() {
        assertNotNull(defaultZone.getID());
    }

    // --- getDefault / setDefault tests ---

    @Test
    public void testGetDefault_NotNull() {
        assertNotNull(DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefault_Null() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testSetDefault_Valid() {
        DateTimeZone original = DateTimeZone.getDefault();
        DateTimeZone newZone = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(newZone);
        assertEquals(newZone, DateTimeZone.getDefault());
        // Restore original
        DateTimeZone.setDefault(original);
    }

    // --- getAvailableIDs tests ---

    @Test
    public void testGetAvailableIDs_NotEmpty() {
        assertFalse(DateTimeZone.getAvailableIDs().isEmpty());
    }

    @Test
    public void testGetAvailableIDs_ContainsUTC() {
        assertTrue(DateTimeZone.getAvailableIDs().contains("UTC"));
    }

    // --- fixed offset zone tests ---

    @Test
    public void testForOffsetHours_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(0);
        assertEquals("+00:00", zone.getID());
    }

    @Test
    public void testForOffsetHours_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone.getID());
    }

    @Test
    public void testForOffsetHours_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-8);
        assertEquals("-08:00", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_OutOfRange() {
        DateTimeZone.forOffsetHours(25);
    }

    @Test
    public void testForOffsetMillis_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(0);
        assertEquals("+00:00", zone.getID());
    }

    @Test
    public void testForOffsetMillis_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000); // 1 hour
        assertEquals("+01:00", zone.getID());
    }

    @Test
    public void testForOffsetMillis_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-7200000); // -2 hours
        assertEquals("-02:00", zone.getID());
    }

    // --- toString tests ---

    @Test
    public void testToString_UTC() {
        assertEquals("UTC", DateTimeZone.UTC.toString());
    }

    @Test
    public void testToString_NonUTC() {
        DateTimeZone zone = DateTimeZone.forID("Asia/Tokyo");
        assertEquals("Asia/Tokyo", zone.toString());
    }

    // --- hasSameRules tests ---

    @Test
    public void testHasSameRules_SameZone() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        assertTrue(zone1.hasSameRules(zone2));
    }

    @Test
    public void testHasSameRules_DifferentZone() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/London");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");
        assertFalse(zone1.hasSameRules(zone2));
    }

    @Test
    public void testHasSameRules_Null() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertFalse(zone.hasSameRules(null));
    }

    // --- getOffsetFromLocal tests ---

    @Test
    public void testGetOffsetFromLocal_StandardTime() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Local time 2020-01-15T12:00:00 (standard time)
        long localMillis = 1579099200000L; // 2020-01-15T12:00:00 EST
        int offset = zone.getOffsetFromLocal(localMillis);
        assertEquals(-18000000, offset); // EST offset
    }

    @Test
    public void testGetOffsetFromLocal_DST() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Local time 2020-07-15T12:00:00 (daylight time)
        long localMillis = 1594828800000L; // 2020-07-15T12:00:00 EDT
        int offset = zone.getOffsetFromLocal(localMillis);
        assertEquals(-14400000, offset); // EDT offset
    }

    // --- getOffset(long) with extreme values ---

    @Test
    public void testGetOffset_MinLong() {
        DateTimeZone zone = DateTimeZone.forID("Pacific/Auckland");
        // Should not throw
        int offset = zone.getOffset(Long.MIN_VALUE);
        assertTrue(offset >= -43200000 && offset <= 50400000);
    }

    @Test
    public void testGetOffset_MaxLong() {
        DateTimeZone zone = DateTimeZone.forID("Pacific/Auckland");
        int offset = zone.getOffset(Long.MAX_VALUE);
        assertTrue(offset >= -43200000 && offset <= 50400000);
    }

    // --- Known bug pattern: DST gap handling ---

    @Test
    public void testGetOffsetFromLocal_DSTGap() {
        DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        // In 2019, DST ended on 2019-02-17 at midnight, clocks set back 1 hour
        // Local time 2019-02-17T00:30:00 is ambiguous (falls in gap)
        long localMillis = 1550363400000L; // approximate
        int offset = zone.getOffsetFromLocal(localMillis);
        // Should return the offset after the gap (standard time) or before? Typically after.
        // Just ensure no exception and offset is reasonable.
        assertTrue(offset == -10800000 || offset == -7200000); // -3 or -2 hours
    }

    // --- Additional edge cases ---

    @Test
    public void testForID_WithPrefix() {
        DateTimeZone zone = DateTimeZone.forID("+05:30");
        assertNotNull(zone);
        assertEquals("+05:30", zone.getID());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidPrefix() {
        DateTimeZone.forID("++05:00");
    }

    @Test
    public void testGetOffset_ReadableInstant() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Berlin");
        DateTime dt = new DateTime(2020, 6, 1, 12, 0, zone);
        int offset = zone.getOffset((ReadableInstant) dt);
        assertEquals(7200000, offset); // CEST +2h
    }

    @Test(expected = NullPointerException.class)
    public void testGetOffset_NullReadableInstant() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        zone.getOffset((ReadableInstant) null);
    }

    // --- hashCode consistency with equals ---

    @Test
    public void testHashCode_NotEqualObjects() {
        DateTimeZone zone1 = DateTimeZone.forID("UTC");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/London");
        assertNotEquals(zone1.hashCode(), zone2.hashCode());
    }

    // --- getMillisKeepLocal with null original zone ---

    @Test
    public void testGetMillisKeepLocal_NullOriginalZone() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        long instant = 1000L;
        // If original zone is null, it should treat as UTC
        assertEquals(instant, zone.getMillisKeepLocal(null, instant));
    }
}