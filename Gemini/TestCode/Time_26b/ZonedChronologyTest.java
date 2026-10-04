package org.joda.time.chrono;

import org.junit.Test;
import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationField;
import org.joda.time.DateTimeField;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.MutablePeriod;

import static org.junit.Assert.*;

public class ZonedChronologyTest {

    @Test
    public void testGetInstance() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        ZonedChronology zoned1 = ZonedChronology.getInstance(base, zone);
        assertNotNull(zoned1);
        assertEquals(base, zoned1.getBase());
        assertEquals(zone, zoned1.getZone());

        // Test caching / identical instance
        ZonedChronology zoned2 = ZonedChronology.getInstance(base, zone);
        assertSame(zoned1, zoned2);

        // Test with UTC (should return base chronology or handle gracefully)
        try {
            ZonedChronology.getInstance(base, DateTimeZone.UTC);
            fail("Expected IllegalArgumentException for UTC");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            ZonedChronology.getInstance(null, zone);
            fail("Expected IllegalArgumentException for null chronology");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            ZonedChronology.getInstance(base, null);
            fail("Expected IllegalArgumentException for null zone");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testFieldsAndConversions() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        ZonedChronology zoned = ZonedChronology.getInstance(base, zone);

        assertNotNull(zoned.year());
        assertNotNull(zoned.monthOfYear());
        assertNotNull(zoned.dayOfMonth());
        assertNotNull(zoned.hourOfDay());
        assertNotNull(zoned.minuteOfHour());
        assertNotNull(zoned.secondOfMinute());
        assertNotNull(zoned.millisOfSecond());

        assertNotNull(zoned.eras());
        assertNotNull(zoned.centuries());
        assertNotNull(zoned.years());
        assertNotNull(zoned.months());
        assertNotNull(zoned.days());
        assertNotNull(zoned.hours());
        assertNotNull(zoned.minutes());
        assertNotNull(zoned.seconds());
        assertNotNull(zoned.millis());

        long millis = 1234567890000L;
        long convertedLocal = zoned.getDateTimeMillis(2009, 2, 13, 23, 31, 30, 0);
        assertTrue(convertedLocal != 0);

        long convertedUTC = zoned.getDateTimeMillis(2009, 2, 13, 23, 31, 30, 0);
        assertTrue(convertedUTC != 0);

        long convertedInstant = zoned.getDateTimeMillis(2009, 2, 13, 15, 23, 31, 30, 0);
        assertTrue(convertedInstant != 0);
    }

    @Test
    public void testEqualsAndHashCode() {
        Chronology base1 = ISOChronology.getInstanceUTC();
        Chronology base2 = GregorianChronology.getInstanceUTC();
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Asia/Tokyo");

        ZonedChronology z1 = ZonedChronology.getInstance(base1, zone1);
        ZonedChronology z2 = ZonedChronology.getInstance(base1, zone1);
        ZonedChronology z3 = ZonedChronology.getInstance(base2, zone1);
        ZonedChronology z4 = ZonedChronology.getInstance(base1, zone2);

        assertEquals(z1, z1);
        assertEquals(z1, z2);
        assertEquals(z1.hashCode(), z2.hashCode());

        assertNotEquals(z1, null);
        assertNotEquals(z1, "Some String");
        assertNotEquals(z1, z3);
        assertNotEquals(z1, z4);
    }

    @Test
    public void testToString() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        ZonedChronology zoned = ZonedChronology.getInstance(base, zone);
        String str = zoned.toString();
        assertNotNull(str);
        assertTrue(str.contains("ZonedChronology"));
        assertTrue(str.contains("London"));
    }

    @Test
    public void testAddAndSetOperations() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        ZonedChronology zoned = ZonedChronology.getInstance(base, zone);

        long instant = 1234567890000L;
        long addedMillis = zoned.add(instant, 1000L, 5);
        assertTrue(addedMillis != instant);

        long addedPeriod = zoned.add(new MutablePeriod(1000L), instant, 1);
        assertTrue(addedPeriod != instant);

        long subtractedPeriod = zoned.add(new MutablePeriod(1000L), instant, -1);
        assertTrue(subtractedPeriod != instant);

        int[] values = new int[]{2010, 6, 15, 12, 30, 0, 0};
        long parsed = zoned.getDateTimeMillis(values[0], values[1], values[2], values[3], values[4], values[5], values[6]);
        assertTrue(parsed != 0);
    }

    @Test
    public void testFieldAddMethodsAndBounds() {
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeZone zone = DateTimeZone.forID("Australia/Sydney");
        ZonedChronology zoned = ZonedChronology.getInstance(base, zone);

        DateTimeField hourField = zoned.hourOfDay();
        long instant = 1234567890000L;
        
        long resAdd = hourField.add(instant, 2);
        assertTrue(resAdd != instant);

        long resAddLong = hourField.add(instant, 3L);
        assertTrue(resAddLong != instant);

        int diff = hourField.getDifference(resAdd, instant);
        assertEquals(2, diff);

        long diffAsLong = hourField.getDifferenceAsLong(resAdd, instant);
        assertEquals(2L, diffAsLong);

        assertNotNull(hourField.getMinimumValue());
        assertNotNull(hourField.getMaximumValue());
    }

    @Test
    public void testSpecialDstTransitionBug26Cases() {
        // Specifically targets potential offset/zone calculation bugs around DST gaps/overlaps (Bug 26 context)
        Chronology base = ISOChronology.getInstanceUTC();
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        ZonedChronology zoned = ZonedChronology.getInstance(base, zone);

        // Spring forward transition test case
        long springForward = 1238893200000L; // approx March 2009
        DateTimeField hourField = zoned.hourOfDay();
        long adjusted = hourField.add(springForward, 1);
        assertTrue(adjusted != springForward);

        // Fall back transition test case
        long fallBack = 1257044400000L; // approx November 2009
        long adjustedFall = hourField.add(fallBack, 1);
        assertTrue(adjustedFall != fallBack);
    }
}