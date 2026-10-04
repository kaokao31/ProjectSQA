package org.joda.time.chrono;

import org.junit.Test;
import static org.junit.Assert.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.field.UnsupportedDurationField;

/**
 * Comprehensive JUnit 4 test suite for GJChronology, targeting line/branch coverage
 * and detection of the Defects4J Time-6 bug (withZone exception).
 */
public class GJChronologyTest {

    private static final DateTimeZone UTC = DateTimeZone.UTC;
    private static final DateTimeZone ZONE_PARIS = DateTimeZone.forID("Europe/Paris");
    private static final DateTimeZone ZONE_NY = DateTimeZone.forID("America/New_York");
    private static final DateTimeZone ZONE_AUCKLAND = DateTimeZone.forID("Pacific/Auckland");

    // ----------------------------------------------------------------
    // Singleton instance methods
    // ----------------------------------------------------------------
    @Test
    public void testGetInstanceUTC() {
        GJChronology instance = GJChronology.getInstanceUTC();
        assertNotNull("getInstanceUTC should return non-null", instance);
        assertEquals(DateTimeZone.UTC, instance.getZone());
    }

    @Test
    public void testGetInstanceWithZone() {
        GJChronology instance = GJChronology.getInstance(ZONE_PARIS);
        assertNotNull("getInstance(zone) should return non-null", instance);
        assertEquals(ZONE_PARIS, instance.getZone());
        assertEquals("instance should be GJChronology", GJChronology.class, instance.getClass());
    }

    @Test
    public void testGetInstanceWithNullZone() {
        // Defects4J: should not throw (null zone treated as default)
        try {
            GJChronology instance = GJChronology.getInstance((DateTimeZone) null);
            assertNotNull(instance);
            assertEquals(DateTimeZone.getDefault(), instance.getZone());
        } catch (Exception e) {
            fail("getInstance(null zone) should not throw: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // withZone / withUTC / getZone – key for bug detection
    // ----------------------------------------------------------------
    @Test
    public void testWithZoneFromUTC() {
        GJChronology utc = GJChronology.getInstanceUTC();
        // The bug: this call may throw IllegalArgumentException in Defects4J Time-6
        try {
            Chronology result = utc.withZone(ZONE_PARIS);
            assertNotNull(result);
            assertEquals(ZONE_PARIS, result.getZone());
            assertTrue("result should be GJChronology or wrapped", result instanceof GJChronology || result instanceof org.joda.time.chrono.ZonedChronology);
        } catch (IllegalArgumentException e) {
            fail("withZone from UTC must not throw: " + e.getMessage());
        }
    }

    @Test
    public void testWithZoneDefaultFromUTC() {
        GJChronology utc = GJChronology.getInstanceUTC();
        try {
            Chronology result = utc.withZone(DateTimeZone.getDefault());
            assertNotNull(result);
        } catch (IllegalArgumentException e) {
            fail("withZone(DateTimeZone.getDefault()) from UTC must not throw: " + e.getMessage());
        }
    }

    @Test
    public void testWithZoneToUTC() {
        GJChronology paris = GJChronology.getInstance(ZONE_PARIS);
        Chronology result = paris.withZone(UTC);
        assertNotNull(result);
        assertEquals(UTC, result.getZone());
    }

    @Test
    public void testWithUTCCalledOnInstance() {
        GJChronology paris = GJChronology.getInstance(ZONE_PARIS);
        Chronology result = paris.withUTC();
        assertNotNull(result);
        assertEquals(UTC, result.getZone());
    }

    // ----------------------------------------------------------------
    // Test cutover date handling – edge cases around 1582-10-15
    // ----------------------------------------------------------------
    @Test
    public void testBeforeCutover() {
        // Date just before Gregorian cutover (Julian date)
        LocalDate ld = new LocalDate(1582, 10, 4, GJChronology.getInstanceUTC());
        assertEquals(1582, ld.getYear());
        assertEquals(10, ld.getMonthOfYear());
        assertEquals(4, ld.getDayOfMonth());
    }

    @Test
    public void testAfterCutover() {
        // First day of Gregorian calendar
        LocalDate ld = new LocalDate(1582, 10, 15, GJChronology.getInstanceUTC());
        assertEquals(1582, ld.getYear());
        assertEquals(10, ld.getMonthOfYear());
        assertEquals(15, ld.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExactCutoverGapDate() {
        // The date October 5-14, 1582 does not exist in Gregorian, but in GJ,
        // constructing a LocalDate with a Gregorian calendar will throw.
        // GJChronology instance defaults to Gregorian for dates after Oct 15.
        // Trying to create a LocalDate with parameters that are illegal in Gregorian.
        new LocalDate(1582, 10, 10, GJChronology.getInstanceUTC());
    }

    @Test
    public void testCutoverWithDifferentZones() {
        // Ensure zone offset does not break cutover handling (common bug area)
        DateTimeZone zone = ZONE_PARIS;
        GJChronology chrono = GJChronology.getInstance(zone);
        // Date a few days after cutover
        DateTime dt = new DateTime(1582, 10, 20, 0, 0, 0, 0, chrono);
        assertEquals(1582, dt.getYear());
        assertEquals(10, dt.getMonthOfYear());
        assertEquals(20, dt.getDayOfMonth());
    }

    // ----------------------------------------------------------------
    // getDateTimeMillis variations
    // ----------------------------------------------------------------
    @Test
    public void testGetDateTimeMillisWithFields() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(2000, 6, 15, 12, 30, 0, 0);
        DateTime dt = new DateTime(millis, chrono);
        assertEquals(2000, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    @Test
    public void testGetDateTimeMillisWithYearMonthDay() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = chrono.getDateTimeMillis(2000, 6, 15, 0);
        DateTime dt = new DateTime(millis, chrono);
        assertEquals(2000, dt.getYear());
        assertEquals(6, dt.getMonthOfYear());
        assertEquals(15, dt.getDayOfMonth());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillisInvalidDate() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // Invalid day-of-month for June
        chrono.getDateTimeMillis(2000, 6, 31, 0);
    }

    // ----------------------------------------------------------------
    // Year / week year / month / day fields
    // ----------------------------------------------------------------
    @Test
    public void testGetYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = new DateTime(2000, 6, 15, 12, 0, 0, 0, chrono).getMillis();
        assertEquals(2000, chrono.getYear(millis));
        // Pre-cutover year
        long julianMillis = new DateTime(1500, 1, 1, 0, 0, 0, 0, chrono).getMillis();
        assertEquals(1500, chrono.getYear(julianMillis));
    }

    @Test
    public void testGetWeekyear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        // Known week year for Jan 1, 2000 (Saturday) -> weekyear 1999
        long millis = new DateTime(2000, 1, 1, 12, 0, 0, 0, chrono).getMillis();
        assertEquals(1999, chrono.weekyear().get(millis));
    }

    @Test
    public void testGetMonthOfYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        long millis = new DateTime(1582, 10, 4, 0, 0, 0, 0, chrono).getMillis();
        // Before cutover
        assertEquals(10, chrono.monthOfYear().get(millis));
        millis = new DateTime(1582, 10, 15, 0, 0, 0, 0, chrono).getMillis();
        // After cutover
        assertEquals(10, chrono.monthOfYear().get(millis));
    }

    // ----------------------------------------------------------------
    // Equals / hashCode
    // ----------------------------------------------------------------
    @Test
    public void testEqualsDifferentZones() {
        GJChronology chrono1 = GJChronology.getInstance(ZONE_PARIS);
        GJChronology chrono2 = GJChronology.getInstance(ZONE_NY);
        assertFalse("chronos with different zones must not be equal", chrono1.equals(chrono2));
    }

    @Test
    public void testEqualsSameZone() {
        GJChronology chrono1 = GJChronology.getInstance(ZONE_PARIS);
        GJChronology chrono2 = GJChronology.getInstance(ZONE_PARIS);
        assertTrue("chronos with same zone must be equal", chrono1.equals(chrono2));
    }

    @Test
    public void testHashCodeConsistency() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        int hc1 = chrono.hashCode();
        int hc2 = chrono.hashCode();
        assertEquals(hc1, hc2);
    }

    // ----------------------------------------------------------------
    // Edge cases with zone offset causing date transition
    // ----------------------------------------------------------------
    @Test
    public void testCutoverWithLargeNegativeOffset() {
        // Use a zone with large negative UTC offset (like -12:00)
        DateTimeZone zone = DateTimeZone.forOffsetHours(-12);
        GJChronology chrono = GJChronology.getInstance(zone);
        // date just after cutover standard time but offset might push it into cutover day
        DateTime dt = new DateTime(1582, 10, 16, 0, 0, 0, 0, chrono);
        assertNotNull(dt);
    }

    @Test
    public void testCutoverWithLargePositiveOffset() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(14);
        GJChronology chrono = GJChronology.getInstance(zone);
        DateTime dt = new DateTime(1582, 10, 15, 0, 0, 0, 0, chrono);
        assertNotNull(dt);
    }

    // ----------------------------------------------------------------
    // Instant conversion (ReadableInstant) – instance methods
    // ----------------------------------------------------------------
    @Test
    public void testGetInstanceWithReadableInstant() {
        DateTime dt = new DateTime(2000, 1, 1, 0, 0, 0, 0, GJChronology.getInstanceUTC());
        GJChronology chrono = GJChronology.getInstance(dt.getZone(), (org.joda.time.ReadableInstant) null);
        assertNotNull(chrono);
    }

    @Test
    public void testGetInstanceWithReadableDuration() {
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC, (org.joda.time.ReadableDuration) null);
        assertNotNull(chrono);
    }

    // ----------------------------------------------------------------
    // Min / Max year
    // ----------------------------------------------------------------
    @Test
    public void testGetMinYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        int minYear = chrono.getMinYear();
        assertTrue("minYear should be <= -292000000", minYear <= -292000000);
        // Actually GJChronology min is -292275055 (Gregorian) or Julian minimal?
        assertTrue(minYear < -10000);
    }

    @Test
    public void testGetMaxYear() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        int maxYear = chrono.getMaxYear();
        assertTrue("maxYear should be >= 292000000", maxYear >= 292000000);
    }

    // ----------------------------------------------------------------
    // toString
    // ----------------------------------------------------------------
    @Test
    public void testToString() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        String str = chrono.toString();
        assertTrue("toString should contain 'GJChronology'", str.contains("GJChronology"));
        assertTrue("toString should contain 'UTC'", str.contains("UTC"));
    }
}