package org.joda.time;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for DateTimeZone, targeting high coverage and fault detection.
 * Focuses on getOffsetFromLocal, getOffset, and related methods.
 */
public class DateTimeZoneTest {

    private DateTimeZone utc;
    private DateTimeZone fixed;
    private DateTimeZone europeLondon;
    private DateTimeZone americaNewYork;

    @Before
    public void setUp() {
        utc = DateTimeZone.UTC;
        fixed = DateTimeZone.forOffsetHours(5); // Fixed offset +05:00
        europeLondon = DateTimeZone.forID("Europe/London");
        americaNewYork = DateTimeZone.forID("America/New_York");
    }

    // --- Basic offset tests ---

    @Test
    public void testGetOffset_UTC() {
        assertEquals(0, utc.getOffset(0L));
        assertEquals(0, utc.getOffset(Long.MAX_VALUE));
        assertEquals(0, utc.getOffset(Long.MIN_VALUE));
    }

    @Test
    public void testGetOffset_FixedZone() {
        long offsetMillis = 5 * 60 * 60 * 1000L;
        assertEquals(offsetMillis, fixed.getOffset(0L));
        assertEquals(offsetMillis, fixed.getOffset(1000000L));
        assertEquals(offsetMillis, fixed.getOffset(-1000000L));
    }

    @Test
    public void testGetOffset_NonDST() {
        // Europe/London in winter (no DST) - use a known winter instant
        long winterInstant = new DateTime(2023, 1, 15, 12, 0, utc).getMillis();
        assertEquals(0, europeLondon.getOffset(winterInstant)); // UTC+0 in winter
    }

    @Test
    public void testGetOffset_DST() {
        // Europe/London in summer (BST) - use a known summer instant
        long summerInstant = new DateTime(2023, 7, 15, 12, 0, utc).getMillis();
        assertEquals(60 * 60 * 1000L, europeLondon.getOffset(summerInstant)); // UTC+1
    }

    // --- getOffsetFromLocal tests (bug-prone) ---

    @Test
    public void testGetOffsetFromLocal_UTC() {
        assertEquals(0, utc.getOffsetFromLocal(0L));
        assertEquals(0, utc.getOffsetFromLocal(Long.MAX_VALUE));
        assertEquals(0, utc.getOffsetFromLocal(Long.MIN_VALUE));
    }

    @Test
    public void testGetOffsetFromLocal_FixedZone() {
        long offsetMillis = 5 * 60 * 60 * 1000L;
        assertEquals(offsetMillis, fixed.getOffsetFromLocal(0L));
        assertEquals(offsetMillis, fixed.getOffsetFromLocal(1000000L));
        assertEquals(offsetMillis, fixed.getOffsetFromLocal(-1000000L));
    }

    @Test
    public void testGetOffsetFromLocal_DSTOverlap() {
        // America/New_York DST overlap: fall-back at 2:00 AM EDT to 1:00 AM EST
        // Use a local time that falls in the overlap (e.g., 1:30 AM on Nov 5, 2023)
        // The method should return the earlier (summer) offset or later (winter) offset?
        // Defects4J bug: getOffsetFromLocal may return incorrect offset during overlap.
        // We test both possible outcomes.
        DateTimeZone ny = americaNewYork;
        // Local millis for 1:30 AM on Nov 5, 2023 (local time)
        // We'll construct using DateTime with zone and then get millis
        DateTime localDuringOverlap = new DateTime(2023, 11, 5, 1, 30, 0, 0, ny);
        long localMillis = localDuringOverlap.getMillis();
        int offset = ny.getOffsetFromLocal(localMillis);
        // The offset should be either -5 hours (EST) or -4 hours (EDT)
        // We can check that it's one of the two
        int offsetEST = -5 * 60 * 60 * 1000;
        int offsetEDT = -4 * 60 * 60 * 1000;
        assertTrue("Offset should be either EST or EDT during overlap",
                offset == offsetEST || offset == offsetEDT);
    }

    @Test
    public void testGetOffsetFromLocal_DSTGap() {
        // America/New_York DST gap: spring-forward at 2:00 AM EST to 3:00 AM EDT
        // Local time 2:30 AM does not exist; method should return a valid offset.
        DateTimeZone ny = americaNewYork;
        // Local millis for 2:30 AM on Mar 12, 2023 (local time) - this time does not exist
        DateTime localDuringGap = new DateTime(2023, 3, 12, 2, 30, 0, 0, ny);
        long localMillis = localDuringGap.getMillis();
        int offset = ny.getOffsetFromLocal(localMillis);
        // Should return EDT offset (-4 hours) because it's the later offset
        int offsetEDT = -4 * 60 * 60 * 1000;
        assertEquals("Offset during gap should be EDT", offsetEDT, offset);
    }

    @Test
    public void testGetOffsetFromLocal_NegativeOffset() {
        // A zone with negative offset (e.g., America/Anchorage)
        DateTimeZone anchorage = DateTimeZone.forID("America/Anchorage");
        long localMillis = new DateTime(2023, 6, 15, 12, 0, anchorage).getMillis();
        int offset = anchorage.getOffsetFromLocal(localMillis);
        // Should be -8 hours (AKDT) in summer
        assertEquals(-8 * 60 * 60 * 1000, offset);
    }

    @Test
    public void testGetOffsetFromLocal_ZeroMillis() {
        // At epoch, UTC offset is 0, but for other zones it's the standard offset
        assertEquals(0, utc.getOffsetFromLocal(0L));
        // Europe/London at epoch (Jan 1, 1970) was in winter (GMT)
        assertEquals(0, europeLondon.getOffsetFromLocal(0L));
    }

    // --- getStandardOffset tests ---

    @Test
    public void testGetStandardOffset_UTC() {
        assertEquals(0, utc.getStandardOffset(0L));
        assertEquals(0, utc.getStandardOffset(Long.MAX_VALUE));
        assertEquals(0, utc.getStandardOffset(Long.MIN_VALUE));
    }

    @Test
    public void testGetStandardOffset_FixedZone() {
        long offsetMillis = 5 * 60 * 60 * 1000L;
        assertEquals(offsetMillis, fixed.getStandardOffset(0L));
    }

    @Test
    public void testGetStandardOffset_DST() {
        // Europe/London in summer: standard offset is 0 (GMT), but DST offset is +1
        long summerInstant = new DateTime(2023, 7, 15, 12, 0, utc).getMillis();
        assertEquals(0, europeLondon.getStandardOffset(summerInstant));
    }

    // --- isFixed tests ---

    @Test
    public void testIsFixed_UTC() {
        assertTrue(utc.isFixed());
    }

    @Test
    public void testIsFixed_FixedZone() {
        assertTrue(fixed.isFixed());
    }

    @Test
    public void testIsFixed_DSTZone() {
        assertFalse(europeLondon.isFixed());
        assertFalse(americaNewYork.isFixed());
    }

    // --- getNameKey tests ---

    @Test
    public void testGetNameKey_UTC() {
        assertEquals("UTC", utc.getNameKey(0L));
    }

    @Test
    public void testGetNameKey_FixedZone() {
        // Fixed offset zones have name key like "+05:00"
        String key = fixed.getNameKey(0L);
        assertNotNull(key);
        assertTrue(key.startsWith("+") || key.startsWith("-"));
    }

    @Test
    public void testGetNameKey_DST() {
        // Europe/London in winter: "GMT", in summer: "BST"
        long winter = new DateTime(2023, 1, 15, 12, 0, utc).getMillis();
        long summer = new DateTime(2023, 7, 15, 12, 0, utc).getMillis();
        assertEquals("GMT", europeLondon.getNameKey(winter));
        assertEquals("BST", europeLondon.getNameKey(summer));
    }

    // --- getShortName / getName tests ---

    @Test
    public void testGetShortName_UTC() {
        assertEquals("UTC", utc.getShortName(0L));
    }

    @Test
    public void testGetName_UTC() {
        assertEquals("UTC", utc.getName(0L));
    }

    @Test
    public void testGetShortName_DST() {
        long winter = new DateTime(2023, 1, 15, 12, 0, utc).getMillis();
        long summer = new DateTime(2023, 7, 15, 12, 0, utc).getMillis();
        assertEquals("GMT", europeLondon.getShortName(winter));
        assertEquals("BST", europeLondon.getShortName(summer));
    }

    // --- forID / forOffsetHours / forOffsetMillis tests ---

    @Test(expected = IllegalArgumentException.class)
    public void testForID_Null() {
        DateTimeZone.forID(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForID_InvalidID() {
        DateTimeZone.forID("Invalid/Zone");
    }

    @Test
    public void testForID_UTC() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
    }

    @Test
    public void testForOffsetHours_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(0);
        assertEquals(0, zone.getOffset(0L));
        assertTrue(zone.isFixed());
    }

    @Test
    public void testForOffsetHours_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(3);
        assertEquals(3 * 60 * 60 * 1000L, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetHours_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(-5);
        assertEquals(-5 * 60 * 60 * 1000L, zone.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHours_OutOfRange() {
        DateTimeZone.forOffsetHours(25);
    }

    @Test
    public void testForOffsetMillis_Zero() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(0);
        assertEquals(0, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis_Positive() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals(3600000, zone.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis_Negative() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(-7200000);
        assertEquals(-7200000, zone.getOffset(0L));
    }

    // --- getAvailableIDs tests ---

    @Test
    public void testGetAvailableIDs_NotEmpty() {
        java.util.Set<String> ids = DateTimeZone.getAvailableIDs();
        assertNotNull(ids);
        assertFalse(ids.isEmpty());
        assertTrue(ids.contains("UTC"));
        assertTrue(ids.contains("Europe/London"));
    }

    // --- hashCode and equals tests ---

    @Test
    public void testEquals_SameZone() {
        assertEquals(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
        assertEquals(fixed, DateTimeZone.forOffsetHours(5));
    }

    @Test
    public void testEquals_DifferentZone() {
        assertNotEquals(DateTimeZone.UTC, europeLondon);
    }

    @Test
    public void testHashCode_Consistent() {
        DateTimeZone zone1 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");
        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    // --- toString tests ---

    @Test
    public void testToString_UTC() {
        assertEquals("UTC", utc.toString());
    }

    @Test
    public void testToString_FixedZone() {
        String str = fixed.toString();
        assertTrue(str.startsWith("+") || str.startsWith("-"));
    }

    @Test
    public void testToString_DSTZone() {
        assertEquals("Europe/London", europeLondon.toString());
    }

    // --- Edge cases for getOffsetFromLocal (bug trigger) ---

    @Test
    public void testGetOffsetFromLocal_EdgeCase_LongMinValue() {
        // Long.MIN_VALUE is a valid instant; ensure no exception
        int offset = utc.getOffsetFromLocal(Long.MIN_VALUE);
        assertEquals(0, offset);
    }

    @Test
    public void testGetOffsetFromLocal_EdgeCase_LongMaxValue() {
        int offset = utc.getOffsetFromLocal(Long.MAX_VALUE);
        assertEquals(0, offset);
    }

    @Test
    public void testGetOffsetFromLocal_EdgeCase_NegativeLocalMillis() {
        // Negative local millis (before epoch) for a zone with DST
        DateTimeZone ny = americaNewYork;
        // Use a date in 1969 (before epoch) during DST? Actually DST rules vary.
        // Just ensure no exception and a reasonable offset.
        long localMillis = -1000000L; // some negative value
        int offset = ny.getOffsetFromLocal(localMillis);
        // Should be either -5 or -4 hours
        assertTrue(offset == -5 * 3600000 || offset == -4 * 3600000);
    }

    // --- Test for bug in Defects4J Time-17: getOffsetFromLocal during DST overlap ---
    // This test specifically targets the known bug where the method returns the wrong offset.
    @Test
    public void testGetOffsetFromLocal_DSTOverlap_BugTrigger() {
        // For America/New_York, the fall-back transition in 2023 occurs at 2:00 AM EDT to 1:00 AM EST.
        // Local time 1:30 AM occurs twice. The method should return the earlier (summer) offset
        // for the first occurrence and the later (winter) offset for the second occurrence.
        // However, the bug may cause it to always return the winter offset.
        // We test both possible local millis that correspond to the same local time.
        // We'll use a known transition: Nov 5, 2023, 2:00 AM EDT -> 1:00 AM EST.
        // The local millis for 1:30 AM EST (winter) and 1:30 AM EDT (summer) are different.
        // We can compute them using DateTime with zone.
        DateTimeZone ny = americaNewYork;
        // Summer (EDT) 1:30 AM on Nov 5, 2023 (before transition)
        DateTime summerLocal = new DateTime(2023, 11, 5, 1, 30, 0, 0, ny);
        long summerMillis = summerLocal.getMillis();
        // Winter (EST) 1:30 AM on Nov 5, 2023 (after transition) - but this local time is ambiguous.
        // Actually, the DateTime constructor will pick the first occurrence (summer) if we use the zone.
        // To get the winter occurrence, we can use withEarlierOffsetAtOverlap() or withLaterOffsetAtOverlap().
        // But those methods are not available in DateTime? Actually they are in DateTime but we can use
        // DateTimeZone's getOffsetFromLocal directly with the local millis.
        // The local millis for the winter occurrence is summerMillis + 1 hour (since clocks fall back).
        long winterMillis = summerMillis + 60 * 60 * 1000L; // add one hour to get the later occurrence
        int offsetSummer = ny.getOffsetFromLocal(summerMillis);
        int offsetWinter = ny.getOffsetFromLocal(winterMillis);
        // The offset for summerMillis should be -4 hours (EDT), for winterMillis should be -5 hours (EST)
        int offsetEDT = -4 * 60 * 60 * 1000;
        int offsetEST = -5 * 60 * 60 * 1000;
        // Note: Due to the bug, both might return EST. We assert that they are different.
        assertNotEquals("Offsets during overlap should differ", offsetSummer, offsetWinter);
        assertEquals("Summer offset should be EDT", offsetEDT, offsetSummer);
        assertEquals("Winter offset should be EST", offsetEST, offsetWinter);
    }
}