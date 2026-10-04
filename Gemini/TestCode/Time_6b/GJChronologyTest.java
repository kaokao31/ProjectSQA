package org.joda.time.chrono;

import org.junit.Test;
import org.joda.time.DateTime;
import org.joda.time.DateTimeField;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import static org.junit.Assert.*;

public class GJChronologyTest {

    @Test
    public void testInstanceCachingAndCreation() {
        GJChronology chrono1 = GJChronology.getInstance();
        assertNotNull(chrono1);

        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        GJChronology chrono2 = GJChronology.getInstance(zone);
        assertNotNull(chrono2);

        Instant instant = new Instant(0L);
        GJChronology chrono3 = GJChronology.getInstance(zone, instant);
        assertNotNull(chrono3);

        GJChronology chrono4 = GJChronology.getInstance(zone, instant.toDateTime(), 4);
        assertNotNull(chrono4);

        GJChronology chrono5 = GJChronology.getInstance(zone, (ReadableInstant) null, 4);
        assertNotNull(chrono5);
    }

    @Test
    public void testCutoverBetweenJulianAndGregorian() {
        DateTimeZone zone = DateTimeZone.UTC;
        // 1582-10-15 is standard Gregorian cutover start in Rome
        LocalDate cutoverDate = new LocalDate(1582, 10, 15, GregorianChronology.getInstance(zone));
        GJChronology chrono = GJChronology.getInstance(zone, cutoverDate);
        assertNotNull(chrono);

        assertEquals(cutoverDate.toDateTimeAtStartOfDay(zone).getMillis(), chrono.getGregorianCutover().getMillis());
    }

    @Test
    public void testWithZone() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono.withZone(DateTimeZone.UTC));
        assertSame(chrono, chrono.withZone(chrono.getZone()));
    }

    @Test
    public void testGetDateTimeMillis() {
        GJChronology chrono = GJChronology.getInstance();
        long millis = chrono.getDateTimeMillis(2020, 5, 10, 12, 30, 0, 0);
        assertTrue(millis > 0);

        long millisWithEpoch = chrono.getDateTimeMillis(2020, 5, 10, 12, 30, 0, 0);
        assertEquals(millis, millisWithEpoch);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDateTimeMillisInvalid() {
        GJChronology chrono = GJChronology.getInstance();
        // Invalid month/day combination around cutover or general
        chrono.getDateTimeMillis(1582, 10, 5, 12, 0, 0, 0);
    }

    @Test
    public void testToString() {
        GJChronology chrono = GJChronology.getInstance();
        String str = chrono.toString();
        assertNotNull(str);
        assertTrue(str.contains("GJChronology"));
    }

    @Test
    public void testEqualsAndHashCode() {
        GJChronology chrono1 = GJChronology.getInstance(DateTimeZone.UTC);
        GJChronology chrono2 = GJChronology.getInstance(DateTimeZone.UTC);
        GJChronology chrono3 = GJChronology.getInstance(DateTimeZone.forID("America/New_York"));

        assertEquals(chrono1, chrono2);
        assertEquals(chrono1.hashCode(), chrono2.hashCode());
        assertNotEquals(chrono1, chrono3);
        assertNotEquals(chrono1, "SomeOtherObject");
    }

    @Test
    public void testFieldsAndConversions() {
        GJChronology chrono = GJChronology.getInstance();
        assertNotNull(chrono.year());
        assertNotNull(chrono.monthOfYear());
        assertNotNull(chrono.dayOfMonth());
        assertNotNull(chrono.hourOfDay());
        assertNotNull(chrono.minuteOfHour());
        assertNotNull(chrono.secondOfMinute());
        assertNotNull(chrono.millisOfSecond());

        long instant = chrono.getDateTimeMillis(2000, 1, 1, 0, 0, 0, 0);
        long translated = chrono.getDateTimeMillis(GregorianChronology.getInstance(), 2000, 1, 1, 0);
        assertEquals(instant, translated);
    }

    @Test
    public void testCutoverAddFieldsBugCase() {
        // Specifically targeting potential defects in Julian/Gregorian gap calculations
        DateTimeZone zone = DateTimeZone.UTC;
        GJChronology chrono = GJChronology.getInstance(zone, new LocalDate(1582, 10, 15));
        
        LocalDateTime ldt = new LocalDateTime(1582, 10, 10, 12, 0, 0, 0, chrono);
        LocalDateTime added = ldt.plusDays(10);
        assertNotNull(added);

        LocalDateTime subbed = ldt.minusDays(10);
        assertNotNull(subbed);
    }
}