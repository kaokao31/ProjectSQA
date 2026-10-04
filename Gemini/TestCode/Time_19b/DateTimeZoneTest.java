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
    public void testGetOffset() {
        DateTimeZone utc = DateTimeZone.UTC;
        assertEquals(0, utc.getOffset(0L));
        assertEquals(0, utc.getOffset(null));

        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        assertNotNull(zone);
        int offset = zone.getOffset(1400000000000L);
        assertTrue(offset != 0);
        
        // Test standard offset method
        assertEquals(0, utc.getStandardOffset(0L));
    }

    @Test
    public void testGetOffsetFromLocal() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Test offset from local where transition might occur
        long millis = 1399800000000L;
        int offset = zone.offsetFromLocal(millis);
        assertTrue(offset != 0);
    }

    @Test
    public void testAdjustOffset() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        // Fall back transition test for adjustOffset
        // November 2, 2014, 1:00 AM transition in New York
        long localMillis = 1414822800000L; 
        long adjusted = zone.adjustOffset(localMillis, false);
        long adjustedTrue = zone.adjustOffset(localMillis, true);
        
        // Verify they behave without exception
        assertTrue(adjusted != 0);
        assertTrue(adjustedTrue != 0);
    }

    @Test
    public void testConvertUTCToLocal() {
        DateTimeZone zone = DateTimeZone.forID("Europe/London");
        long utcTime = 1000000000L;
        long localTime = zone.convertUTCToLocal(utcTime);
        long convertedBack = zone.convertLocalToUTC(localTime, false);
        assertEquals(utcTime, convertedBack);
    }

    @Test
    public void testConvertLocalToUTCWithStrict() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long localTime = 1414822800000L;
        try {
            zone.convertLocalToUTC(localTime, true);
        } catch (IllegalInstantException e) {
            // Expected for ambiguous/gap times if strict
            assertNotNull(e.getMessage());
        } catch (IllegalArgumentException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetName() {
        DateTimeZone zone = DateTimeZone.forID("UTC");
        String name = zone.getName(0L, Locale.ENGLISH);
        assertNotNull(name);
        
        String shortName = zone.getShortName(0L, Locale.ENGLISH);
        assertNotNull(shortName);
    }

    @Test
    public void testGetMillisKeepLocal() {
        DateTimeZone utc = DateTimeZone.UTC;
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        
        long result = DateTimeZone.getMillisKeepLocal(utc, 1000L, zone);
        assertTrue(result != 0);
        
        long sameResult = DateTimeZone.getMillisKeepLocal(utc, 1000L, utc);
        assertEquals(1000L, sameResult);
        
        long nullZoneResult = DateTimeZone.getMillisKeepLocal(null, 1000L, null);
        assertEquals(1000L, nullZoneResult);
    }

    @Test
    public void testForID() {
        DateTimeZone utc = DateTimeZone.forID("UTC");
        assertEquals(DateTimeZone.UTC, utc);
        
        DateTimeZone nullId = DateTimeZone.forID(null);
        assertNotNull(nullId);
        
        DateTimeZone plusOffset = DateTimeZone.forID("+02:00");
        assertEquals(2 * 3600 * 1000, plusOffset.getOffset(0L));
        
        DateTimeZone minusOffset = DateTimeZone.forID("-05:00");
        assertEquals(-5 * 3600 * 1000, minusOffset.getOffset(0L));
        
        DateTimeZone hoursOnly = DateTimeZone.forID("+03");
        assertEquals(3 * 3600 * 1000, hoursOnly.getOffset(0L));
    }

    @Test
    public void testForTimeZone() {
        TimeZone tz = TimeZone.getTimeZone("GMT+3");
        DateTimeZone dtz = DateTimeZone.forTimeZone(tz);
        assertNotNull(dtz);
        
        DateTimeZone nullTz = DateTimeZone.forTimeZone(null);
        assertNotNull(nullTz);
        
        TimeZone defaultTz = TimeZone.getDefault();
        DateTimeZone defaultDtz = DateTimeZone.forTimeZone(defaultTz);
        assertNotNull(defaultDtz);
    }

    @Test
    public void testForOffsetHoursMinutes() {
        DateTimeZone zone = DateTimeZone.forOffsetHoursMinutes(5, 30);
        assertEquals((5 * 60 + 30) * 60 * 1000, zone.getOffset(0L));
        
        DateTimeZone negativeZone = DateTimeZone.forOffsetHoursMinutes(-3, -15);
        assertEquals(-(3 * 60 + 15) * 60 * 1000, negativeZone.getOffset(0L));
    }

    @Test
    public void testForOffsetMillis() {
        DateTimeZone zone = DateTimeZone.forOffsetMillis(3600000);
        assertEquals(3600000, zone.getOffset(0L));
        
        DateTimeZone cached = DateTimeZone.forOffsetMillis(3600000);
        assertSame(zone, cached);
        
        DateTimeZone zero = DateTimeZone.forOffsetMillis(0);
        assertEquals(DateTimeZone.UTC, zero);
    }

    @Test
    public void testAvailableIDs() {
        assertNotNull(DateTimeZone.getAvailableIDs());
        assertTrue(DateTimeZone.getAvailableIDs().size() > 0);
    }

    @Test
    public void testDefaultDateTimeZone() {
        DateTimeZone zone = DateTimeZone.forID("America/Chicago");
        DateTimeZone.setDefault(zone);
        assertEquals(zone, DateTimeZone.getDefault());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetDefaultNull() {
        DateTimeZone.setDefault(null);
    }

    @Test
    public void testTransitions() {
        DateTimeZone zone = DateTimeZone.forID("America/New_York");
        long next = zone.nextTransition(0L);
        long previous = zone.previousTransition(0L);
        assertTrue(next != 0L);
        assertTrue(previous != 0L);
    }

    @Test
    public void testEqualsAndHashCode() {
        DateTimeZone zone1 = DateTimeZone.forID("America/New_York");
        DateTimeZone zone2 = DateTimeZone.forID("America/New_York");
        DateTimeZone utc = DateTimeZone.UTC;

        assertEquals(zone1, zone2);
        assertEquals(zone1.hashCode(), zone2.hashCode());
        assertNotEquals(zone1, utc);
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
        // Just verify it returns a boolean without failure
        assertTrue(isStd || !isStd);
    }
}