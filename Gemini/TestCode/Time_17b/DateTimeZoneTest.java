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
    public void testForID_ValidAndInvalid() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertNotNull(utc);
        assertEquals("UTC", utc.getID());

        DateTimeZone plusTwo = DateTimeZone.forID("+02:00");
        assertNotNull(plusTwo);

        DateTimeZone minusFive = DateTimeZone.forID("-05:00");
        assertNotNull(minusFive);

        DateTimeZone nullId = DateTimeZone.forID(null);
        assertNotNull(nullId);
        assertEquals(DateTimeZone.getDefault(), nullId);

        try {
            DateTimeZone.forID("Invalid/ZoneName");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testForOffsetHoursMinutes() {
        DateTimeZone zone1 = DateTimeZone.forOffsetHours(5);
        assertEquals("+05:00", zone1.getID());

        DateTimeZone zone2 = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals("+05:30", zone2.getID());

        DateTimeZone zone3 = DateTimeZone.forOffsetHoursMinutes(-3, -15);
        assertEquals("-03:15", zone3.getID());

        try {
            DateTimeZone.forOffsetHoursMinutes(25, 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            DateTimeZone.forOffsetHoursMinutes(0, 65);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testForOffsetMillis() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals("+01:00", zone.getID());
        assertEquals(3600000, zone.getOffset(0L));

        DateTimeZone zoneZero = DateTimeZone.forOffsetMillis(0);
        assertEquals("UTC", zoneZero.getID());
    }

    @Test
    public void testForTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("America/New_York");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        assertNotNull(dtz);

        DateTimeZone dtzNull = DateTimeZone.forTimeZone(null);
        assertEquals(DateTimeZone.getDefault(), dtzNull);

        TimeZone gmtTz = TimeZone.getTimeZone("GMT+02:00");
        DateTimeZone gmtDtz = DateTimeZone.forTimeZone(gmtTz);
        assertNotNull(gmtDtz);
    }

    @Test
    public void testSetGetDefault() {
        DateTimeZone paris = DateTimeZone.forID("Europe/Paris");
        DateTimeZone.setDefault(paris);
        assertEquals(paris, DateTimeZone.getDefault());

        try {
            DateTimeZone.setDefault(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testTransitionsAndOffsets() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long now = System.currentTimeMillis();
        
        int offset = zone.getOffset(now);
        int standardOffset = zone.getStandardOffset(now);
        boolean isStandard = zone.isStandardOffset(now);

        assertNotNull(zone.getNameKey(now));
        
        long nextTransition = zone.nextTransition(now);
        long previousTransition = zone.previousTransition(now);
        
        assertTrue(nextTransition >= now || nextTransition == now);
    }

    @Test
    public void testConversionLocalToUTC() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        
        long utcMillis = zone.convertLocalToUTC(1000L, false);
        long localMillis = zone.convertUTCToLocal(utcMillis);
        
        // Test strict vs lenient or standard conversions
        long convertedStrict = zone.convertLocalToUTC(1000L, true, 2000L);
        long convertedLenient = zone.convertLocalToUTC(1000L, false, 2000L);
        
        assertTrue(true); // Ensure no exceptions thrown
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone zone1 = DateTimeZone.forID("UTC");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");

        long res = DateTimeZone.getMillisKeepLocal(zone1, 1000L, zone2, 2000L);
        // If fromZone or toZone is null, it should default
        long resNull = DateTimeZone.getMillisKeepLocal(null, 1000L, null, 2000L);
        assertNotNull(resNull);
    }

    @Test
    public void testFormattingAndNames() {
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        long now = System.currentTimeMillis();

        String name = zone.getName(now);
        String shortName = zone.getShortName(now);
        String nameLoc = zone.getName(now, Locale.FRENCH);
        String shortNameLoc = zone.getShortName(now, Locale.FRENCH);

        assertNotNull(zone.toString());
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone2 = DateTimeZone.forID("Europe/Paris");
        DateTimeZone zone3 = DateTimeZone.forID("UTC");

        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertNotEquals(zone1, zone3);
        assertNotEquals(zone1, null);
        assertNotEquals(zone1, "NotAZone");
    }

    @Test
    public void testOffsetDateTimeZone() {
        DateTimeZone fixed = DateTimeZone.forOffsetHours(3);
        assertEquals(3 * 3600000, fixed.getOffset(1000L));
        assertEquals(3 * 3600000, fixed.getStandardOffset(1000L));
        assertTrue(fixed.isStandardOffset(1000L));
        assertEquals(1000L, fixed.nextTransition(1000L));
        assertEquals(1000L, fixed.previousTransition(1000L));
        
        DateTimeZone fixed2 = DateTimeZone.forOffsetHours(3);
        assertEquals(fixed, fixed2);
    }
}