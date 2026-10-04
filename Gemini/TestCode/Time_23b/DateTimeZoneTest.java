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
    public void testGetDefaultAndSetDefault() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());

        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testForID() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertNotNull(utc);
        assertEquals("UTC", utc.getID());

        DateTimeZone sameUtc = DateTimeZone.forID("+00:00");
        assertNotNull(sameUtc);

        DateTimeZone plusOne = DateTimeZone.forID("+01:00");
        assertNotNull(plusOne);
        assertEquals("+01:00", plusOne.getID());

        DateTimeZone minusFive = DateTimeZone.forID("-05:00");
        assertNotNull(minusFive);
        assertEquals("-05:00", minusFive.getID());

        DateTimeZone nullId = DateTimeZone.forID(null);
        assertEquals(DateTimeZone.getDefault(), nullId);

        try {
            DateTimeZone.forID("Invalid/ZoneNameXYZ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testForTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        assertNotNull(dtz);
        assertEquals("America/New_York", dtz.getID());

        DateTimeZone nullTz = DateTimeZone.forTimeZone(null);
        assertEquals(DateTimeZone.getDefault(), nullTz);

        TimeZone gmtTz = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone gmtDtz = DateTimeZone.forTimeZone(gmtTz);
        assertNotNull(gmtDtz);
    }

    @Test
    public void testForOffsetMillis() {
        DateTimeZone zone1 = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone1.getID());
        assertEquals(3600000, zone1.getOffset(0L));

        DateTimeZone zone2 = DateTimeZone.forOffsetMillis(0);
        assertEquals("UTC", zone2.getID());

        try {
            DateTimeZone.forOffsetMillis(DateTimeZone.MAX_OFFSET + 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testForOffsetHours() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone.getID());
        assertEquals(18000000, zone.getOffset(0L));

        try {
            DateTimeZone.forOffsetHours(25);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testForOffsetHoursMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone.getID());

        try {
            DateTimeZone.forOffsetHoursMinutes(5, 60);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            DateTimeZone.forOffsetHoursMinutes(-5, 60);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }

        try {
            DateTimeZone.forOffsetHoursMinutes(25, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testGetAvailableIDs() {
        assertNotNull(DateTimeZone.getAvailableIDs());
        assertTrue(DateTimeZone.getAvailableIDs().size() > 0);
    }

    @Test
    public void testGetNameKey() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertEquals("UTC", utc.getNameKey(0L));
    }

    @Test
    public void testGetShortName() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertNotNull(utc.getShortName(0L, Locale.ENGLISH));
        assertNotNull(utc.getShortName(0L));
    }

    @Test
    public void testGetName() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertNotNull(utc.getName(0L, Locale.ENGLISH));
        assertNotNull(utc.getName(0L));
    }

    @Test
    public void testGetOffset() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertEquals(0, utc.getOffset(0L));

        ReadableInstant instant = null;
        assertEquals(0, utc.getOffset(instant));

        ReadableInstant instantReal = new Instant(0L);
        assertEquals(0, utc.getOffset(instantReal));
    }

    @Test
    public void testGetStandardOffset() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertEquals(0, utc.getStandardOffset(0L));
    }

    @Test
    public void testIsStandardOffset() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertTrue(utc.isStandardOffset(0L));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertEquals(0, utc.getOffsetFromLocal(0L));
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long local = zone.convertUTCToLocal(3600000L);
        assertEquals(10800000L, local);
    }

    @Test
    public void testConvertLocalToUTC() {
        DateTimeZone zone = DateTimeZone.forOffsetHours(2);
        long utc = zone.convertLocalToUTC(10800000L, false);
        assertEquals(3600000L, utc);

        long utcStrict = zone.convertLocalToUTC(10800000L, true);
        assertEquals(3600000L, utcStrict);
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(2);
        DateTimeZone zone2 = DateTimeZone.forOffsetHours(4);
        long converted = DateTimeZone.getMillisKeepLocal(zone1, 0L, zone2);
        assertEquals(7200000L, converted);

        long sameZone = DateTimeZone.getMillisKeepLocal(zone1, 1000L, zone1);
        assertEquals(1000L, sameZone);

        long nullDefaultZone = DateTimeZone.getMillisKeepLocal(null, 1000L, zone2);
        assertNotNull(nullDefaultZone);
    }

    @Test
    public void testIsLocalDateTimeGap() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        LocalDateTime ldt = new LocalDateTime(2020, 1, 1, 0, 0);
        assertFalse(utc.isLocalDateTimeGap(ldt));
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertEquals(1000L, utc.adjustOffset(1000L, false));
    }

    @Test
    public void testTransitions() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertEquals(0L, utc.nextTransition(0L));
        assertEquals(0L, utc.previousTransition(0L));
    }

    @Test
    public void testToTimeZone() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        TimeZone tz = utc.toTimeZone();
        assertNotNull(tz);
        assertEquals("GMT", tz.getID());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("UTC");
        DateTimeZone zone2 = DateTimeZone.forID("UTC");
        DateTimeZone zone3 = DateTimeZone.forOffsetHours(1);

        assertEquals(zone1, zone2);
        assertNotEquals(zone1, zone3);
        assertNotEquals(zone1, null);
        assertNotEquals(zone1, "NotAZone");

        assertEquals(zone1.hashCode(), zone2.hashCode());
    }

    @Test
    public void testToString() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        assertEquals("UTC", zone.toString());
    }
}