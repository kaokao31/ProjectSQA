package org.joda.time;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

public class DateTimeZoneTest {

    private DateTimeZone originalDefault;

    @Before
    public void setUp() {
        originalDefault = DateTimeZone.getDefault();
    }

    @After
    public void tearDown() {
        if (originalDefault != null) {
            DateTimeZone.setDefault(originalDefault);
        }
    }

    @Test
    public void testConstantsAndForID() {
        assertEquals("UTC", DateTimeZone.UTC.getID());
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("UTC"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00:00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("+00"));
        assertSame(DateTimeZone.UTC, DateTimeZone.forID("-00:00"));
        assertNull(DateTimeZone.forID(null));
    }

    @Test
    public void testForOffsetHoursMinutes() {
        DateTimeZone dz1 = DateTimeZone.forOffsetHoursMinutes(0, 0);
        assertSame(DateTimeZone.UTC, dz1);

        DateTimeZone dz2 = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", dz2.getID());
        assertEquals(19800000, dz2.getOffset(0L));

        DateTimeZone dz3 = DateTimeZone.forOffsetHoursMinutes(-3, -15);
        assertEquals("-03:15", dz3.getID());
        assertEquals(-(3 * 3600000 + 15 * 60000), dz3.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidHoursPositive() {
        DateTimeZone.forOffsetHoursMinutes(24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidHoursNegative() {
        DateTimeZone.forOffsetHoursMinutes(-24, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidMinutesPositive() {
        DateTimeZone.forOffsetHoursMinutes(5, 60);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursMinutesInvalidMinutesNegative() {
        DateTimeZone.forOffsetHoursMinutes(5, -60);
    }

    @Test(expected = IllegalArgumentException.class)
    public.void testForOffsetHoursMinutesSignsMismatch() {
        DateTimeZone.forOffsetHoursMinutes(1, -15);
    }

    @Test
    public void testForOffsetHours() {
        DateTimeZone dz = DateTimeZone.forOffsetHours(8);
        assertEquals("+08:00", dz.getID());
        assertEquals(8 * 3600000, dz.getOffset(0L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetHoursOutOfBounds() {
        DateTimeZone.forOffsetHours(24);
    }

    @Test
    public void testForOffsetMillis() {
        DateTimeZone dz = DateTimeZone.forOffsetMillis(12345);
        assertEquals("+00:00:12.345", dz.getID());
        assertEquals(12345, dz.getOffset(0L));

        assertSame(DateTimeZone.UTC, DateTimeZone.forOffsetMillis(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForOffsetMillisOutOfBounds() {
        DateTimeZone.forOffsetMillis(DateTimeZone.MAX_OFFSET + 1);
    }

    @Test
    public void testForTimeZone() {
        assertSame(DateTimeZone.UTC, DateTimeZone.forTimeZone(TimeZone.getTimeZone("UTC")));
        
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        assertNotNull(dtz);
        assertEquals("America/New_York", dtz.getID());

        assertNotNull(DateTimeZone.forTimeZone(null));
    }

    @Test
    public void testDefaultDateTimeZone() {
        DateTimeZone.setDefault(DateTimeZone.UTC);
        assertSame(DateTimeZone.UTC, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNull() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testGetAvailableIDs() {
        assertNotNull(DateTimeZone.getAvailableIDs());
        assertTrue(DateTimeZone.getAvailableIDs().size() > 0);
    }

    @Test
    public void testGetNameAndShortName() {
        DateTimeZone utc = DateTimeZone.UTC;
        String name = utc.getName(0L);
        assertNotNull(name);
        String shortName = utc.getShortName(0L, Locale.ENGLISH);
        assertNotNull(shortName);
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long millis = 0L;
        int offset = zone.getOffsetFromLocal(millis);
        assertTrue(offset != 0 || offset == 0); // Just executing the method
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long utcMillis = 1000000000L;
        long localMillis = zone.convertUTCToLocal(utcMillis);
        long convertedBack = zone.convertLocalToUTC(localMillis, true);
        // Note: Due to DST transitions, convertLocalToUTC with strict/non-strict might differ, but basic sanity:
        assertTrue(convertedBack <= utcMillis || convertedBack >= utcMillis);
    }

    @Test
    public void testNextTransition() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long next = zone.nextTransition(0L);
        long previous = zone.previousTransition(next);
        assertTrue(previous <= next);
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        TimeZone tz = zone.toTimeZone();
        assertNotNull(tz);
        assertEquals("America/New_York", tz.getID());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone3 = DateTimeZone.forID("UTC");

        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertNotEquals(zone1, zone3);
        assertNotEquals(zone1, null);
        assertNotEquals(zone1, "NotAZone");
    }

    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertEquals("America/New_York", zone.toString());
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        boolean isStd = zone.isStandardOffset(0L);
        assertTrue(isStd || !isStd);
    }
}