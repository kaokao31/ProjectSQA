package org.joda.time.chrono;

import org.junit.Test;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import static org.junit.Assert.*;

public class GJChronologyTest {

    @Test
    public void testGetInstanceDefault() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono);
        assertNotNull(chrono.getZone());
    }

    @Test
    public void testGetInstanceWithZone() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        GJChronology chrono = GJChronology.getInstance(zone);
        assertNotNull(chrono);
        assertEquals(zone, chrono.getZone());
    }

    @Test
    public void testGetInstanceWithZoneAndCutover() {
        DateTimeZone zone = DateTimeZone.UTC;
        Instant cutover = new Instant(0L);
        GJChronology chrono = GJChronology.getInstance(zone, cutover);
        assertNotNull(chrono);
        assertEquals(cutover, chrono.getGregorianCutover());
    }

    @Test
    public void testGetInstanceWithFullParams() {
        DateTimeZone zone = DateTimeZone.UTC;
        Instant cutover = new Instant(0L);
        GJChronology chrono = GJChronology.getInstance(zone, cutover, 4);
        assertNotNull(chrono);
        assertEquals(cutover, chrono.getGregorianCutover());
    }

    @Test
    public void testGetInstanceUTC() {
        GJChronology chrono = GJChronology.getInstanceUTC();
        assertNotNull(chrono);
        assertEquals(DateTimeZone.UTC, chrono.getZone());
    }

    @Test
    public void testGetGregorianCutover() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono.getGregorianCutover());
    }

    @Test
    public void testWithUTC() {
        GJChronology chrono = GJChronology.getInstance();
        GJChronology utcChrono = (GJChronology) chrono.withUTC();
        assertNotNull(utcChrono);
        assertEquals(DateTimeZone.UTC, utcChrono.getZone());
    }

    @Test
    public void testWithZone() {
        GJChronology chrono = GJChronology.getInstance();
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        GJChronology parisChrono = (GJChronology) chrono.withZone(paris);
        assertNotNull(parisChrono);
        assertEquals(paris, parisChrono.getZone());

        // Same zone should return same instance
        assertSame(chrono, chrono.withZone(chrono.getZone()));
        // Null zone defaults to default zone
        assertNotNull(chrono.withZone(null));
    }

    @Test
    public void testEqualsAndHashCode() {
        GJChronology chrono1 = GJChronology.getInstance();
        GJChronology chrono2 = GJChronology.getInstance();
        assertEquals(chrono1, chrono2);
        assertEquals(chrono1.hashCode(), chrono2.hashCode());

        assertNotEquals(chrono1, null);
        assertNotEquals(chrono1, "Some String");

        GJChronology chronoUTC = GJChronology.getInstanceUTC();
        assertNotEquals(chrono1, chronoUTC);
    }

    @Test
    public void testToString() {
        GJChronology chrono = GJChronology.getInstance();
        String str = chrono.toString();
        assertNotNull(str);
        assertTrue(str.contains("GJChronology"));
    }

    @Test
    public void testCutoverTransitionDates() {
        // Test around the default cutover (1582-10-15)
        DateTime dtBefore = new DateTime(1582, 10, 4, 12, 0, GJChronology.getInstance());
        assertNotNull(dtBefore);

        DateTime dtAfter = new DateTime(1582, 10, 15, 12, 0, GJChronology.getInstance());
        assertNotNull(dtAfter);
    }

    @Test
    public void testAssembled() {
        GJChronology chrono = GJChronology.getInstance();
        // Trigger assembly fields access via methods
        assertNotNull(chrono.year());
        assertNotNull(chrono.monthOfYear());
        assertNotNull(chrono.dayOfMonth());
        assertNotNull(chrono.hourOfDay());
        assertNotNull(chrono.minuteOfHour());
        assertNotNull(chrono.secondOfMinute());
        assertNotNull(chrono.millisOfSecond());
    }

    @Test
    public void testSetYearBeforeCutover() {
        // Specific test for Bug 18: year adjustment around cutover bounds
        GJChronology chrono = GJChronology.getInstance(DateTimeZone.UTC);
        LocalDateTime ldt = new LocalDateTime(1582, 1, 1, 0, 0, chrono);
        LocalDateTime updated = ldt.withYear(1580);
        assertEquals(1580, updated.getYear());
    }

    @Test
    public void testGetDateTimeMillis() {
        GJChronology chrono = GJChronology.getInstance();
        long millis = chrono.getDateTimeMillis(2020, 5, 20, 12, 30, 0, 0);
        assertTrue(millis > 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidCutoverTransition() {
        // Trying to construct a date that falls in the gap (Oct 5-14, 1582)
        new DateTime(1582, 10, 10, 12, 0, GJChronology.getInstance());
    }
}