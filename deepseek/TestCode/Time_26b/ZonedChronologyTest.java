package org.joda.time.chrono;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.joda.time.Chronology;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationFieldType;
import org.joda.time.ReadablePartial;

public class ZonedChronologyTest {

    private DateTimeZone zoneParis;
    private DateTimeZone zoneUTC;
    private Chronology baseUTC;
    private Chronology baseISO;

    @Before
    public void setUp() {
        zoneParis = DateTimeZone.forID("Europe/Paris");
        zoneUTC = DateTimeZone.UTC;
        baseUTC = ISOChronology.getInstanceUTC();
        baseISO = ISOChronology.getInstance();
    }

    // Construction tests
    @Test
    public void testConstruction() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertNotNull(zoned);
        assertTrue(zoned instanceof ZonedChronology);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionNullBase() {
        ZonedChronology.getInstance(null, zoneParis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructionNullZone() {
        ZonedChronology.getInstance(baseUTC, null);
    }

    // Zone methods
    @Test
    public void testGetZone() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertEquals(zoneParis, zoned.getZone());
    }

    @Test
    public void testWithZone() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        Chronology withLondon = zoned.withZone(DateTimeZone.forID("Europe/London"));
        assertEquals(DateTimeZone.forID("Europe/London"), withLondon.getZone());
        assertNotSame(zoned, withLondon);
    }

    @Test
    public void testWithZoneNullReturnsBase() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        Chronology noZone = zoned.withZone(null);
        assertEquals(DateTimeZone.UTC, noZone.getZone());
        assertFalse(noZone instanceof ZonedChronology);
    }

    @Test
    public void testWithZoneSameZoneReturnsSelf() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        Chronology sameZone = zoned.withZone(zoneParis);
        assertSame(zoned, sameZone);
    }

    // withUTC() tests (targets bug 26)
    @Test
    public void testWithUTCReturnsUTCRegardlessOfOriginalZone() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        Chronology utc = zoned.withUTC();
        assertEquals("withUTC() should return a chronology with UTC zone", DateTimeZone.UTC, utc.getZone());
        assertNotSame(zoned, utc);
        // Verify behavior difference
        long epoch = 0L;
        assertEquals(0, utc.hourOfDay().get(epoch));
        assertEquals(1, zoned.hourOfDay().get(epoch));
    }

    @Test
    public void testWithUTCWhenAlreadyUTC() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneUTC);
        Chronology utc = zoned.withUTC();
        assertEquals(DateTimeZone.UTC, utc.getZone());
        // Optimization may return the same instance
    }

    @Test
    public void testWithUTCFromHalfHourZone() {
        DateTimeZone halfHourZone = DateTimeZone.forID("Asia/Kolkata");
        Chronology zoned = ZonedChronology.getInstance(baseUTC, halfHourZone);
        Chronology utc = zoned.withUTC();
        assertEquals(DateTimeZone.UTC, utc.getZone());
        long instant = new DateTime(2010, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(30, zoned.minuteOfHour().get(instant));
        assertEquals(0, utc.minuteOfHour().get(instant));
    }

    // Equals and hashCode
    @Test
    public void testEquals() {
        Chronology zoned1 = ZonedChronology.getInstance(baseUTC, zoneParis);
        Chronology zoned2 = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertEquals(zoned1, zoned2);
        assertEquals(zoned1.hashCode(), zoned2.hashCode());
    }

    @Test
    public void testNotEquals() {
        Chronology zoned1 = ZonedChronology.getInstance(baseUTC, zoneParis);
        Chronology zoned2 = ZonedChronology.getInstance(baseUTC, DateTimeZone.forID("Europe/London"));
        assertFalse(zoned1.equals(zoned2));
    }

    @Test
    public void testEqualsNull() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertFalse(zoned.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertFalse(zoned.equals("some string"));
    }

    // toString
    @Test
    public void testToString() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertNotNull(zoned.toString());
        assertTrue(zoned.toString().contains("ZonedChronology"));
        assertTrue(zoned.toString().contains("Europe/Paris"));
    }

    // Field access
    @Test
    public void testFieldsNotNull() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertNotNull(zoned.eras());
        assertNotNull(zoned.centuries());
        assertNotNull(zoned.years());
        assertNotNull(zoned.months());
        assertNotNull(zoned.weeks());
        assertNotNull(zoned.days());
        assertNotNull(zoned.halfdays());
        assertNotNull(zoned.hours());
        assertNotNull(zoned.minutes());
        assertNotNull(zoned.seconds());
        assertNotNull(zoned.millis());
        assertNotNull(zoned.year());
        assertNotNull(zoned.monthOfYear());
        assertNotNull(zoned.dayOfMonth());
        assertNotNull(zoned.dayOfWeek());
        assertNotNull(zoned.dayOfYear());
        assertNotNull(zoned.weekOfWeekyear());
        assertNotNull(zoned.weekyear());
        assertNotNull(zoned.hourOfDay());
        assertNotNull(zoned.minuteOfDay());
        assertNotNull(zoned.minuteOfHour());
        assertNotNull(zoned.secondOfDay());
        assertNotNull(zoned.secondOfMinute());
        assertNotNull(zoned.millisOfDay());
        assertNotNull(zoned.millisOfSecond());
    }

    @Test
    public void testFieldTypes() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertEquals(DateTimeFieldType.year(), zoned.year().getType());
        assertEquals(DateTimeFieldType.monthOfYear(), zoned.monthOfYear().getType());
        assertEquals(DateTimeFieldType.dayOfMonth(), zoned.dayOfMonth().getType());
    }

    @Test
    public void testDurationFieldTypes() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertEquals(DurationFieldType.years(), zoned.years().getType());
        assertEquals(DurationFieldType.months(), zoned.months().getType());
    }

    // Specific date/time values
    @Test
    public void testEpochBoundary() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        assertEquals(1970, zoned.year().get(0L));
    }

    @Test
    public void testYear() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        long instant = new DateTime(2014, 6, 15, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(2014, zoned.year().get(instant));
    }

    @Test
    public void testLeapYear() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        long instant = new DateTime(2000, 2, 29, 12, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(60, zoned.dayOfYear().get(instant));
    }

    @Test
    public void testMonthOfYear() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        long instant = new DateTime(2001, 4, 15, 10, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(4, zoned.monthOfYear().get(instant));
    }

    @Test
    public void testNegativeMillis() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        long instant = -1000000L;
        int year = zoned.year().get(instant);
        assertTrue(year < 1970);
    }

    // Time zone conversion correctness
    @Test
    public void testLocalTimeConversion() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        // Winter (UTC+1)
        long winterInstant = new DateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(1, zoned.hourOfDay().get(winterInstant));
        assertEquals(1, zoned.dayOfMonth().get(winterInstant));
        // Summer (UTC+2)
        long summerInstant = new DateTime(2000, 6, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        assertEquals(2, zoned.hourOfDay().get(summerInstant));
    }

    // DST gap handling
    @Test(expected = IllegalArgumentException.class)
    public void testDSTGapException() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        // 2020-03-29 02:30 is invalid in Paris (spring forward)
        new DateTime(2020, 3, 29, 2, 30, 0, 0, zoned);
    }

    // Add operations
    @Test
    public void testAddMillis() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        long base = new DateTime(2000, 1, 1, 0, 0, 0, 0, DateTimeZone.UTC).getMillis();
        long result = zoned.millis().add(base, 1000);
        assertEquals(base + 1000, result);
    }

    @Test
    public void testAddWrapped() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        long instant = new DateTime(2000, 1, 1, 23, 30, 0, 0, DateTimeZone.UTC).getMillis();
        long newInstant = zoned.hourOfDay().add(instant, 2);
        DateTime dt = new DateTime(newInstant, DateTimeZone.UTC);
        assertEquals(2, dt.getDayOfMonth());
        assertEquals(1, dt.getHourOfDay());
        assertEquals(30, dt.getMinuteOfHour());
    }

    // ReadablePartial support
    @Test
    public void testReadablePartial() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        LocalDate date = new LocalDate(2005, 7, 4, zoned);
        assertEquals(2005, date.getYear());
        assertEquals(7, date.getMonthOfYear());
        assertEquals(4, date.getDayOfMonth());
    }

    // DateTime construction with chronology
    @Test
    public void testDateTimeWithChronology() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        DateTime dt = new DateTime(2010, 6, 15, 12, 0, 0, 0, zoned);
        assertEquals(zoneParis, dt.getZone());
    }

    // Extreme millis (should not crash)
    @Test
    public void testExtremeMillis() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        try {
            int year = zoned.year().get(Long.MIN_VALUE);
            assertNotNull(year);
        } catch (ArithmeticException e) {
            // Acceptable
        }
    }

    // Ensure withZone preserves base
    @Test
    public void testWithZonePreservesBase() {
        Chronology zoned = ZonedChronology.getInstance(baseUTC, zoneParis);
        Chronology newZoned = zoned.withZone(DateTimeZone.forID("Asia/Tokyo"));
        assertTrue(newZoned instanceof ZonedChronology);
        assertEquals(DateTimeZone.forID("Asia/Tokyo"), newZoned.getZone());
    }
}